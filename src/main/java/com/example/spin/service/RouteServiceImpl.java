package com.example.spin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spin.controller.dto.RouteDetailResponse;
import com.example.spin.controller.dto.RouteGenerateRequest;
import com.example.spin.controller.dto.RouteGenerateResponse;
import com.example.spin.controller.dto.RouteStopResponse;
import com.example.spin.domain.ChallengeStatus;
import com.example.spin.domain.Route;
import com.example.spin.domain.RouteStop;
import com.example.spin.domain.Store;
import com.example.spin.exception.NotFoundException;
import com.example.spin.repository.RouteChallengeRepository;
import com.example.spin.repository.RouteRepository;
import com.example.spin.repository.StoreRepository;
import com.example.spin.service.claude.ClaudeRouteRecommender;
import com.example.spin.service.claude.RouteRecommendation;
import com.example.spin.util.WalkingDistanceCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    // 후보를 이 반경(도보로 무리 없는 거리) 안으로 추려서 넘겨야, AI가 구 반대편끼리 묶는 걸 막을 수 있다
    private static final int MAX_CANDIDATE_RADIUS_METERS = 2000;

    // 지역에 수천 건씩 있는 경우 반경 필터링만으로는 개수가 안 줄 수 있어(밀집 지역),
    // 프롬프트 폭주(400 prompt too long)를 막기 위해 후보 개수 자체에 상한을 둔다
    private static final int MAX_CANDIDATES = 40;

    private final StoreRepository storeRepository;
    private final RouteRepository routeRepository;
    private final RouteChallengeRepository routeChallengeRepository;
    private final ClaudeRouteRecommender claudeRouteRecommender;

    @Override
    public RouteGenerateResponse generateRoute(RouteGenerateRequest request) {
        // Claude 호출이 오래 걸리므로(수십 초) DB 트랜잭션을 걸어두지 않는다 —
        // 조회는 트랜잭션 없이, 최종 저장은 routeRepository.save()가 자체 트랜잭션으로 처리한다
        List<Store> regionStores = storeRepository.findByRegion(request.region());

        boolean hasUserLocation = request.latitude() != null && request.longitude() != null;
        List<Store> candidates = hasUserLocation
                ? filterByProximity(regionStores, request.latitude(), request.longitude())
                : filterNearAnchor(regionStores);

        RouteRecommendation recommendation =
                claudeRouteRecommender.recommend(request.region(), request.purpose(), candidates);

        // Claude는 어떤 가게를 어떤 순서로 추천하는지와 이유만 정하고,
        // 거리/도보 시간은 저장된 실제 좌표를 기준으로 우리가 직접 계산한다
        List<Store> selectedStores = recommendation.stops().stream()
                .map(stop -> findCandidate(candidates, stop.storeId()))
                .toList();

        List<Integer> walkMinutesByStop = new ArrayList<>();
        int totalDistanceMeters = 0;
        for (int i = 0; i < selectedStores.size(); i++) {
            Store current = selectedStores.get(i);
            int distanceMeters = 0;

            if (i > 0) {
                Store previous = selectedStores.get(i - 1);
                distanceMeters = WalkingDistanceCalculator.estimatedWalkingDistanceMeters(
                        previous.getLatitude(), previous.getLongitude(),
                        current.getLatitude(), current.getLongitude());
            } else if (hasUserLocation) {
                // 첫 스탑은 이전 스탑이 없으니, GPS가 있으면 사용자 현재 위치 기준으로 계산
                distanceMeters = WalkingDistanceCalculator.estimatedWalkingDistanceMeters(
                        request.latitude(), request.longitude(),
                        current.getLatitude(), current.getLongitude());
            }

            totalDistanceMeters += distanceMeters;
            walkMinutesByStop.add(WalkingDistanceCalculator.estimatedWalkingMinutes(distanceMeters));
        }
        int estimatedDurationMinutes = walkMinutesByStop.stream().mapToInt(Integer::intValue).sum();

        Route route = new Route(request.region(), request.purpose(),
                totalDistanceMeters, estimatedDurationMinutes, LocalDateTime.now());

        List<RouteStopResponse> stopResponses = new ArrayList<>();
        for (int i = 0; i < selectedStores.size(); i++) {
            Store store = selectedStores.get(i);
            String category = recommendation.stops().get(i).category();
            String reason = recommendation.stops().get(i).reason();
            int walkMinutes = walkMinutesByStop.get(i);
            int visitOrder = i + 1;

            route.getStops().add(new RouteStop(route, store, visitOrder, walkMinutes, reason, category));
            stopResponses.add(new RouteStopResponse(
                    store.getName(), category, store.getAddress(), walkMinutes, reason));
        }

        routeRepository.save(route);

        return new RouteGenerateResponse(
                totalDistanceMeters, estimatedDurationMinutes, stopResponses.size(), stopResponses);
    }

    @Override
    public RouteDetailResponse getRouteDetail(int routeId) {
        Route route = routeRepository.findByIdWithStops(routeId)
                .orElseThrow(() -> new NotFoundException("루트를 찾을 수 없습니다: " + routeId));

        List<RouteStopResponse> stopResponses = route.getStops().stream()
                .map(stop -> new RouteStopResponse(
                        stop.getStore().getName(), stop.getCategory(), stop.getStore().getAddress(),
                        stop.getTravelMinutesFromPrevious(), stop.getRecommendationReason()))
                .toList();

        long completedCount = routeChallengeRepository.countByRouteAndStatus(route, ChallengeStatus.COMPLETED);

        return new RouteDetailResponse(
                route.getId(), route.getRegion(), route.getPurpose(),
                route.getTotalDistanceMeters(), route.getEstimatedDurationMinutes(),
                stopResponses, completedCount);
    }

    private List<Store> filterByProximity(List<Store> candidates, double latitude, double longitude) {
        List<Store> sortedByDistance = candidates.stream()
                .sorted(Comparator.comparingInt(store -> WalkingDistanceCalculator.estimatedWalkingDistanceMeters(
                        latitude, longitude, store.getLatitude(), store.getLongitude())))
                .toList();

        List<Store> withinRadius = sortedByDistance.stream()
                .filter(store -> WalkingDistanceCalculator.estimatedWalkingDistanceMeters(
                        latitude, longitude, store.getLatitude(), store.getLongitude())
                        <= MAX_CANDIDATE_RADIUS_METERS)
                .toList();

        // 반경 안에 후보가 너무 적으면(한적한 동네 등) 루트 자체를 못 만드니, 거리순으로 가장 가까운 후보들로 대체한다
        // (지역 전체를 통째로 넘기면 프롬프트가 폭주할 수 있어 무제한 폴백은 하지 않는다)
        List<Store> base = withinRadius.size() >= 2 ? withinRadius : sortedByDistance;
        return base.stream().limit(MAX_CANDIDATES).toList();
    }

    // 사용자 GPS가 없을 때는 기준점이 없으니, 후보 목록의 첫 가게를 임시 기준점 삼아
    // 그 근처로 후보를 좁힌다 (그래야 구 전체 수천 건이 그대로 프롬프트에 들어가는 걸 막을 수 있다)
    private List<Store> filterNearAnchor(List<Store> candidates) {
        if (candidates.isEmpty()) {
            return candidates;
        }

        Store anchor = candidates.get(0);
        return filterByProximity(candidates, anchor.getLatitude(), anchor.getLongitude());
    }

    private Store findCandidate(List<Store> candidates, int storeId) {
        return candidates.stream()
                .filter(s -> s.getId() == storeId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "후보 목록에 없는 가게를 추천했습니다: id=" + storeId));
    }
}

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
    // 프롬프트 폭주(400 prompt too long)를 막기 위해 후보 개수 자체에 상한을 둔다.
    // 가게 1곳당 대략 50토큰 안팎이라 400개로 잡아도 2만 토큰 수준 (Anthropic 한도 20만 토큰 대비 여유 충분)
    private static final int MAX_CANDIDATES = 400;

    // 반경 안을 이만큼의 구간으로 나눠서, 가까운 곳부터 먼 곳까지 고르게 후보를 뽑는다.
    // 그냥 가까운 순으로 40개를 자르면 번화가처럼 밀집된 동네에서는 후보가 전부 한 골목에
    // 몰려버려서, Claude가 뭘 고르든 가게 간 거리가 항상 짧게 나올 수밖에 없다.
    private static final int DISTANCE_BAND_COUNT = 4;

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
        // GPS가 없으면 기준점이 없으니, 후보 목록의 첫 가게를 임시 기준점으로 삼는다
        Store anchor = regionStores.isEmpty() ? null : regionStores.get(0);
        double refLatitude = hasUserLocation ? request.latitude() : anchor != null ? anchor.getLatitude() : 0;
        double refLongitude = hasUserLocation ? request.longitude() : anchor != null ? anchor.getLongitude() : 0;

        List<Store> candidates = filterByProximity(regionStores, refLatitude, refLongitude);

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
                    store.getName(), category, store.getAddress(),
                    store.getLatitude(), store.getLongitude(), walkMinutes, reason));
        }

        Route savedRoute = routeRepository.save(route);

        return new RouteGenerateResponse(
                savedRoute.getId(), totalDistanceMeters, estimatedDurationMinutes, stopResponses.size(), stopResponses);
    }

    @Override
    public RouteDetailResponse getRouteDetail(int routeId) {
        Route route = routeRepository.findByIdWithStops(routeId)
                .orElseThrow(() -> new NotFoundException("루트를 찾을 수 없습니다: " + routeId));

        List<RouteStopResponse> stopResponses = route.getStops().stream()
                .map(stop -> new RouteStopResponse(
                        stop.getStore().getName(), stop.getCategory(), stop.getStore().getAddress(),
                        stop.getStore().getLatitude(), stop.getStore().getLongitude(),
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

        // 반경 안에 후보가 너무 적으면(한적한 동네 등) 루트 자체를 못 만드니, 거리순 전체 후보로 대체한다
        // (지역 전체를 통째로 넘기면 프롬프트가 폭주할 수 있어 무제한 폴백은 하지 않는다)
        List<Store> base = withinRadius.size() >= 2 ? withinRadius : sortedByDistance;
        return sampleAcrossDistanceBands(base);
    }

    // 이미 거리순으로 정렬된 목록을 가까운 구간부터 먼 구간까지 N등분해서 구간마다 고르게 뽑는다
    private List<Store> sampleAcrossDistanceBands(List<Store> sortedByDistance) {
        if (sortedByDistance.size() <= MAX_CANDIDATES) {
            return sortedByDistance;
        }

        List<Store> sampled = new ArrayList<>();
        int bandSize = (int) Math.ceil(sortedByDistance.size() / (double) DISTANCE_BAND_COUNT);
        int perBand = MAX_CANDIDATES / DISTANCE_BAND_COUNT;

        for (int band = 0; band < DISTANCE_BAND_COUNT; band++) {
            int from = band * bandSize;
            int to = Math.min(from + bandSize, sortedByDistance.size());
            if (from >= to) {
                continue;
            }
            List<Store> bandStores = sortedByDistance.subList(from, to);
            sampled.addAll(bandStores.subList(0, Math.min(perBand, bandStores.size())));
        }

        return sampled;
    }

    private Store findCandidate(List<Store> candidates, int storeId) {
        return candidates.stream()
                .filter(s -> s.getId() == storeId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "후보 목록에 없는 가게를 추천했습니다: id=" + storeId));
    }
}

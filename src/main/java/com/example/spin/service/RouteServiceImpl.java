package com.example.spin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final StoreRepository storeRepository;
    private final RouteRepository routeRepository;
    private final RouteChallengeRepository routeChallengeRepository;
    private final ClaudeRouteRecommender claudeRouteRecommender;

    @Override
    @Transactional
    public RouteGenerateResponse generateRoute(RouteGenerateRequest request) {
        List<Store> candidates = storeRepository.findByRegion(request.region());

        RouteRecommendation recommendation =
                claudeRouteRecommender.recommend(request.region(), request.purpose(), candidates);

        // Claude는 어떤 가게를 어떤 순서로 추천하는지와 이유만 정하고,
        // 거리/도보 시간은 저장된 실제 좌표를 기준으로 우리가 직접 계산한다
        List<Store> selectedStores = recommendation.stops().stream()
                .map(stop -> findCandidate(candidates, stop.storeId()))
                .toList();

        boolean hasUserLocation = request.latitude() != null && request.longitude() != null;

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

    private Store findCandidate(List<Store> candidates, int storeId) {
        return candidates.stream()
                .filter(s -> s.getId() == storeId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "후보 목록에 없는 가게를 추천했습니다: id=" + storeId));
    }
}

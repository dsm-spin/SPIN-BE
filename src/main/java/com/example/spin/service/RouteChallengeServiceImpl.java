package com.example.spin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spin.controller.dto.ChallengeProgressResponse;
import com.example.spin.controller.dto.ChallengeResponse;
import com.example.spin.controller.dto.CheckInRequest;
import com.example.spin.controller.dto.CheckInResponse;
import com.example.spin.controller.dto.NextDestinationResponse;
import com.example.spin.controller.dto.RouteHistoryResponse;
import com.example.spin.controller.dto.StampResponse;
import com.example.spin.domain.ChallengeStatus;
import com.example.spin.domain.CheckIn;
import com.example.spin.domain.Point;
import com.example.spin.domain.Route;
import com.example.spin.domain.RouteChallenge;
import com.example.spin.domain.RouteStop;
import com.example.spin.domain.Store;
import com.example.spin.domain.User;
import com.example.spin.exception.AlreadyCheckedInException;
import com.example.spin.exception.NotFoundException;
import com.example.spin.exception.NotOnRouteException;
import com.example.spin.repository.CheckInRepository;
import com.example.spin.repository.PointRepository;
import com.example.spin.repository.RouteChallengeRepository;
import com.example.spin.repository.RouteRepository;
import com.example.spin.repository.RouteStopRepository;
import com.example.spin.repository.StoreRepository;
import com.example.spin.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteChallengeServiceImpl implements RouteChallengeService {

    // 체크인 한 번당 적립되는 포인트
    private static final int POINTS_PER_CHECKIN = 10;

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteChallengeRepository routeChallengeRepository;
    private final CheckInRepository checkInRepository;
    private final StoreRepository storeRepository;
    private final PointRepository pointRepository;

    // 데모 기간에는 QR 값과 무관하게 아무 QR이나 찍으면 체크인되도록 하기 위한 임시 스위치.
    // 실사용 전환 시 false로 되돌리면 원래의 정확한 QR-가게 매칭 로직으로 복원된다.
    @Value("${app.demo-checkin-enabled:false}")
    private boolean demoCheckInEnabled;

    @Override
    @Transactional
    public ChallengeResponse startChallenge(String accountId, int routeId) {
        User user = findUser(accountId);
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("루트를 찾을 수 없습니다: " + routeId));

        RouteChallenge challenge = routeChallengeRepository.save(
                new RouteChallenge(route, user, ChallengeStatus.IN_PROGRESS, LocalDateTime.now(), null, null));

        return toResponse(challenge);
    }

    @Override
    @Transactional
    public CheckInResponse checkIn(String accountId, CheckInRequest request) {
        User user = findUser(accountId);

        List<RouteChallenge> inProgress = routeChallengeRepository
                .findByUserAndStatusOrderByStartedAtDesc(user, ChallengeStatus.IN_PROGRESS);

        RouteChallenge challenge;
        RouteStop routeStop;

        if (demoCheckInEnabled) {
            RouteChallenge activeChallenge = inProgress.stream().findFirst()
                    .orElseThrow(() -> new NotOnRouteException("진행 중인 도전 기록이 없습니다"));
            challenge = activeChallenge;
            routeStop = routeStopRepository.findByRouteOrderByVisitOrderAsc(activeChallenge.getRoute()).stream()
                    .filter(stop -> !checkInRepository.existsByRouteChallengeAndRouteStop(activeChallenge, stop))
                    .findFirst()
                    .orElseThrow(() -> new AlreadyCheckedInException("이미 모든 가게를 체크인했습니다"));
        } else {
            Store store = storeRepository.findByQrCode(request.qrCode())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 QR 코드입니다"));

            // QR(가게 고정값)만으로는 어느 도전 기록인지 알 수 없으니, 이 가게를 스탑으로 포함한
            // "진행 중인" 도전 기록을 최근 시작한 순으로 찾는다 (여러 개 겹치면 가장 최근 것 채택)
            challenge = null;
            routeStop = null;
            for (RouteChallenge candidate : inProgress) {
                Optional<RouteStop> stop = routeStopRepository.findByRouteAndStore(candidate.getRoute(), store);
                if (stop.isPresent()) {
                    challenge = candidate;
                    routeStop = stop.get();
                    break;
                }
            }

            if (challenge == null) {
                throw new NotOnRouteException("진행 중인 루트 중에 이 가게가 포함된 도전 기록이 없습니다");
            }

            if (checkInRepository.existsByRouteChallengeAndRouteStop(challenge, routeStop)) {
                throw new AlreadyCheckedInException("이미 체크인한 가게입니다");
            }
        }

        checkInRepository.save(new CheckIn(challenge, routeStop, LocalDateTime.now()));

        int totalStops = routeStopRepository.findByRouteOrderByVisitOrderAsc(challenge.getRoute()).size();
        int checkedInCount = checkInRepository.findByRouteChallenge(challenge).size();
        if (checkedInCount >= totalStops) {
            challenge.complete(LocalDateTime.now());
        }

        Point point = pointRepository.findByUser(user).orElseGet(() -> pointRepository.save(new Point(user)));
        point.earn(POINTS_PER_CHECKIN);

        ChallengeResponse response = toResponse(challenge);
        return new CheckInResponse(
                response.challengeId(), response.routeId(), response.status(),
                response.totalStops(), response.checkedInCount(), response.startedAt(), response.completedAt(),
                POINTS_PER_CHECKIN, point.getBalance());
    }

    @Override
    public ChallengeProgressResponse getCurrentProgress(String accountId) {
        User user = findUser(accountId);

        RouteChallenge challenge = routeChallengeRepository
                .findByUserAndStatusOrderByStartedAtDesc(user, ChallengeStatus.IN_PROGRESS)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("진행 중인 도전 기록이 없습니다"));

        List<RouteStop> stops = routeStopRepository.findByRouteOrderByVisitOrderAsc(challenge.getRoute());
        Set<Integer> checkedInStopIds = checkInRepository.findByRouteChallenge(challenge).stream()
                .map(checkIn -> checkIn.getRouteStop().getId())
                .collect(Collectors.toSet());

        List<StampResponse> stamps = stops.stream()
                .map(stop -> new StampResponse(
                        stop.getStore().getId(), stop.getStore().getName(), stop.getCategory(), stop.getVisitOrder(),
                        checkedInStopIds.contains(stop.getId())))
                .toList();

        NextDestinationResponse nextDestination = stops.stream()
                .filter(stop -> !checkedInStopIds.contains(stop.getId()))
                .findFirst()
                .map(stop -> new NextDestinationResponse(
                        stop.getStore().getId(), stop.getStore().getName(), stop.getCategory(),
                        stop.getTravelMinutesFromPrevious(),
                        stop.getStore().getLatitude(), stop.getStore().getLongitude()))
                .orElse(null);

        return new ChallengeProgressResponse(
                challenge.getId(), challenge.getRoute().getId(), challenge.getStatus().name(),
                stops.size(), checkedInStopIds.size(), stamps, nextDestination);
    }

    @Override
    public List<RouteHistoryResponse> getCompletedHistory(String accountId) {
        User user = findUser(accountId);

        return routeChallengeRepository
                .findByUserAndStatusOrderByCompletedAtDesc(user, ChallengeStatus.COMPLETED)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private User findUser(String accountId) {
        return userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 계정입니다: " + accountId));
    }

    private ChallengeResponse toResponse(RouteChallenge challenge) {
        int totalStops = challenge.getRoute().getStops().size();
        int checkedInCount = checkInRepository.findByRouteChallenge(challenge).size();

        return new ChallengeResponse(
                challenge.getId(), challenge.getRoute().getId(), challenge.getStatus().name(),
                totalStops, checkedInCount, challenge.getStartedAt(), challenge.getCompletedAt());
    }

    private RouteHistoryResponse toHistoryResponse(RouteChallenge challenge) {
        Route route = challenge.getRoute();
        List<String> storeNames = route.getStops().stream()
                .map(stop -> stop.getStore().getName())
                .toList();

        return new RouteHistoryResponse(
                challenge.getId(), route.getRegion(), route.getPurpose(),
                route.getTotalDistanceMeters(), route.getEstimatedDurationMinutes(),
                challenge.getStartedAt(), challenge.getCompletedAt(), challenge.getPhotoUrl(), storeNames);
    }
}

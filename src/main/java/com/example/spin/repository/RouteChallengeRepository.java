package com.example.spin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.spin.domain.ChallengeStatus;
import com.example.spin.domain.Route;
import com.example.spin.domain.RouteChallenge;
import com.example.spin.domain.User;

public interface RouteChallengeRepository extends JpaRepository<RouteChallenge, Integer> {

    // 새로고침 후 로그인한 유저 기준으로 특정 루트의 진행 상태를 복원
    Optional<RouteChallenge> findByUserAndRoute(User user, Route route);

    List<RouteChallenge> findByUser(User user);

    // 완주 히스토리 (완주한 것만, 최신순)
    List<RouteChallenge> findByUserAndStatusOrderByCompletedAtDesc(User user, ChallengeStatus status);

    // QR 체크인 시 challengeId 없이 "지금 진행 중인 도전" 중에서 찾기 위함 (최근 시작한 순)
    List<RouteChallenge> findByUserAndStatusOrderByStartedAtDesc(User user, ChallengeStatus status);

    // 루트 인기도("N명이 다녀갔어요")
    long countByRouteAndStatus(Route route, ChallengeStatus status);
}

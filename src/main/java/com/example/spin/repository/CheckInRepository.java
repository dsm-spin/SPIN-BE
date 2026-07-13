package com.example.spin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.spin.domain.CheckIn;
import com.example.spin.domain.RouteChallenge;
import com.example.spin.domain.RouteStop;

public interface CheckInRepository extends JpaRepository<CheckIn, Integer> {

    List<CheckIn> findByRouteChallenge(RouteChallenge routeChallenge);

    // 중복 체크인 방지 (DB 유니크 제약과 이중 방어)
    boolean existsByRouteChallengeAndRouteStop(RouteChallenge routeChallenge, RouteStop routeStop);
}

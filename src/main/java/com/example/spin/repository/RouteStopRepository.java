package com.example.spin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.spin.domain.Route;
import com.example.spin.domain.RouteStop;
import com.example.spin.domain.Store;

public interface RouteStopRepository extends JpaRepository<RouteStop, Integer> {

    List<RouteStop> findByRouteOrderByVisitOrderAsc(Route route);

    // QR 체크인 검증: 스캔된 가게가 이 루트의 스탑인지 확인
    Optional<RouteStop> findByRouteAndStore(Route route, Store store);
}

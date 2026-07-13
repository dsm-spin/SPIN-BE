package com.example.spin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.spin.domain.Route;

public interface RouteRepository extends JpaRepository<Route, Integer> {

    // 루트 카드 화면에서 스탑·가게 정보를 한 번에 그려야 해서 fetch join으로 N+1을 막는다
    @Query("select distinct r from Route r "
            + "left join fetch r.stops s "
            + "left join fetch s.store "
            + "where r.id = :id")
    Optional<Route> findByIdWithStops(Integer id);
}

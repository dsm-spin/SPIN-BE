package com.example.spin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.spin.domain.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {

    // QR 스캔 시 스캔된 코드로 가게를 찾기 위함
    Optional<Store> findByQrCode(String qrCode);

    // region은 자유 입력이라 "유성구", "대전 유성구", "유성" 등 표기가 제각각일 수 있어 양방향 부분 일치로 매칭
    @Query("select s from Store s where :region like concat('%', s.region, '%') "
            + "or s.region like concat('%', :region, '%')")
    List<Store> findByRegion(@Param("region") String region);
}

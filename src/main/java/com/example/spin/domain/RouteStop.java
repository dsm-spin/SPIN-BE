package com.example.spin.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    // 카드에 표시되는 순서(1,2,3...). 체크인 순서를 강제하지는 않음
    private int visitOrder;

    // 이전 스탑에서 이 스탑까지 이동 소요 시간(분)
    private Integer travelMinutesFromPrevious;

    // AI가 생성한 "왜 여기인지" 한 줄 추천 이유
    private String recommendationReason;

    // AI가 생성한 업종 카테고리 (예: "로스터리 카페", "이자카야")
    private String category;

    public RouteStop(Route route, Store store, int visitOrder, Integer travelMinutesFromPrevious,
            String recommendationReason, String category) {
        this(0, route, store, visitOrder, travelMinutesFromPrevious, recommendationReason, category);
    }
}

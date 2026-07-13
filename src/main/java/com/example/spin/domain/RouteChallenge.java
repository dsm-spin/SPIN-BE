package com.example.spin.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// "도전 기록": 로그인한 손님 한 명이 특정 루트를 시작한 인스턴스.
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String photoUrl;

    @OneToMany(mappedBy = "routeChallenge", cascade = CascadeType.ALL)
    private List<CheckIn> checkIns = new ArrayList<>();

    public RouteChallenge(Route route, User user, ChallengeStatus status, LocalDateTime startedAt,
            LocalDateTime completedAt, String photoUrl) {
        this(0, route, user, status, startedAt, completedAt, photoUrl, new ArrayList<>());
    }

    public void complete(LocalDateTime completedAt) {
        this.status = ChallengeStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void attachPhoto(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}

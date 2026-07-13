package com.example.spin.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"route_challenge_id", "route_stop_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_challenge_id")
    private RouteChallenge routeChallenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_stop_id")
    private RouteStop routeStop;

    private LocalDateTime checkedInAt;

    public CheckIn(RouteChallenge routeChallenge, RouteStop routeStop, LocalDateTime checkedInAt) {
        this(0, routeChallenge, routeStop, checkedInAt);
    }
}

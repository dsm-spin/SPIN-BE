package com.example.spin.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String region;

    private String purpose;

    private Integer totalDistanceMeters;

    private Integer estimatedDurationMinutes;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    @OrderBy("visitOrder ASC")
    private List<RouteStop> stops = new ArrayList<>();

    public Route(String region, String purpose, Integer totalDistanceMeters,
            Integer estimatedDurationMinutes, LocalDateTime createdAt) {
        this(0, region, purpose, totalDistanceMeters, estimatedDurationMinutes, createdAt, new ArrayList<>());
    }
}

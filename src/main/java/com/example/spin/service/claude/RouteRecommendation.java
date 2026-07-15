package com.example.spin.service.claude;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RouteRecommendation(
        @JsonPropertyDescription("서로 다른 3개의 루트 대안") List<RouteOption> options) {
}

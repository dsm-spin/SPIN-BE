package com.example.spin.service.claude;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RouteOption(
        @JsonPropertyDescription("이 루트에 포함된 가게들, 방문 순서대로") List<RecommendedStop> stops) {
}

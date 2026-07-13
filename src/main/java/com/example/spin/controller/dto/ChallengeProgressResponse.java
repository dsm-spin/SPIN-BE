package com.example.spin.controller.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChallengeProgressResponse(
        int challengeId,
        int routeId,
        String status,
        int totalStops,
        int checkedInCount,
        List<StampResponse> stamps,
        @Schema(description = "미방문 스탑 중 다음 목적지. 전부 방문했으면 null") NextDestinationResponse nextDestination) {
}

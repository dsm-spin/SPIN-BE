package com.example.spin.controller.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChallengeResponse(
        int challengeId,
        int routeId,
        @Schema(description = "IN_PROGRESS 또는 COMPLETED") String status,
        int totalStops,
        int checkedInCount,
        LocalDateTime startedAt,
        @Schema(description = "완주 전이면 null") LocalDateTime completedAt) {
}

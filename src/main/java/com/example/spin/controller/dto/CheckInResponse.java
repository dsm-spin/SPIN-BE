package com.example.spin.controller.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record CheckInResponse(
        int challengeId,
        int routeId,
        @Schema(description = "IN_PROGRESS 또는 COMPLETED") String status,
        int totalStops,
        int checkedInCount,
        LocalDateTime startedAt,
        @Schema(description = "완주 전이면 null") LocalDateTime completedAt,
        @Schema(description = "이번 체크인으로 적립된 포인트") int pointsEarned,
        @Schema(description = "적립 후 총 포인트 잔액") int totalPoints) {
}

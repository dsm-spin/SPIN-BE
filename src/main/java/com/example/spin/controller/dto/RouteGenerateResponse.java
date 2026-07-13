package com.example.spin.controller.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record RouteGenerateResponse(
        @Schema(description = "총 이동 거리(미터), 실제 좌표 기반 계산값") int totalDistanceMeters,
        @Schema(description = "예상 총 소요 시간(분)") int estimatedDurationMinutes,
        @Schema(description = "스탑 개수 (항상 3개는 아님)") int storeCount,
        List<RouteStopResponse> stops) {
}

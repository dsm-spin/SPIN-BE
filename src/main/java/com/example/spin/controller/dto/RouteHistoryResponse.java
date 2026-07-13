package com.example.spin.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

// 완주(status=COMPLETED)한 도전 기록만 히스토리에 노출된다
public record RouteHistoryResponse(
        int challengeId,
        String region,
        String purpose,
        int totalDistanceMeters,
        int estimatedDurationMinutes,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        @Schema(description = "완주 인증 사진. 미첨부 시 null") String photoUrl,
        @Schema(description = "방문 순서대로의 가게 이름 목록") List<String> storeNames) {
}

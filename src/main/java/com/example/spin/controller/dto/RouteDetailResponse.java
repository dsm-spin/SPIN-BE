package com.example.spin.controller.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

// 공유 링크(인스타 스토리 등)로 들어왔을 때 보여주는 루트 미리보기. 로그인 불필요.
public record RouteDetailResponse(
        int routeId,
        String region,
        String purpose,
        int totalDistanceMeters,
        int estimatedDurationMinutes,
        List<RouteStopResponse> stops,
        @Schema(description = "이 루트를 완주한 사람 수 (\"N명이 다녀갔어요\")") long completedCount) {
}

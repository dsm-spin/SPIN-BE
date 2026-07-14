package com.example.spin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RouteStopResponse(
        String storeName,
        @Schema(description = "업종 카테고리 (예: \"로스터리 카페\")") String category,
        String address,
        Double latitude,
        Double longitude,
        @Schema(description = "이전 스탑에서 도보로 이동하는 시간(분). 첫 스탑은 GPS 없으면 0") int walkMinutes,
        @Schema(description = "이 가게를 추천하는 한 줄 이유") String reason) {
}

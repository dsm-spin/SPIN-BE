package com.example.spin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RouteGenerateRequest(
        @Schema(description = "지역 (예: \"대전광역시 유성구\", \"유성구\"도 매칭됨)", example = "대전광역시 유성구") String region,
        @Schema(description = "가서 무엇을 할 건지 자유 텍스트", example = "친구랑 저녁") String purpose,
        @Schema(description = "사용자 현재 위치 위도. 없으면 첫 스탑까지의 거리/시간은 0으로 처리됨") Double latitude,
        @Schema(description = "사용자 현재 위치 경도. 없으면 첫 스탑까지의 거리/시간은 0으로 처리됨") Double longitude) {
}

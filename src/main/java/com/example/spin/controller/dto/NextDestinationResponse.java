package com.example.spin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// 프론트에서 latitude/longitude로 카카오맵/네이버맵 등 지도 앱 딥링크를 직접 구성하면 된다.
public record NextDestinationResponse(
        int storeId,
        String storeName,
        String category,
        @Schema(description = "직전 스탑에서 도보로 이동하는 시간(분)") int walkMinutes,
        double latitude,
        double longitude) {
}

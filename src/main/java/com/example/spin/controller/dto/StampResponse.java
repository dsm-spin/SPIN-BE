package com.example.spin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StampResponse(
        String storeName,
        String category,
        int visitOrder,
        @Schema(description = "이 스탑을 체크인했는지 여부") boolean checkedIn) {
}

package com.example.spin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CheckInRequest(
        @Schema(description = "가게에 붙은 QR 코드의 코드값 (store.qrCode)") String qrCode) {
}

package com.example.spin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthRequest(
        @Schema(description = "계정 아이디") String accountId,
        @Schema(description = "비밀번호") String password) {
}

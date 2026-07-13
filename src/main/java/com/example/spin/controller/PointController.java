package com.example.spin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spin.controller.dto.PointBalanceResponse;
import com.example.spin.service.PointService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
@Tag(name = "포인트", description = "체크인마다 적립되는 포인트 잔액 조회")
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 잔액 조회", description = "로그인한 사용자의 현재 포인트 잔액을 반환한다. 적립 내역(로그)은 없고 잔액만 관리한다.")
    @GetMapping
    public PointBalanceResponse getBalance(Authentication authentication) {
        return pointService.getBalance(authentication.getName());
    }
}

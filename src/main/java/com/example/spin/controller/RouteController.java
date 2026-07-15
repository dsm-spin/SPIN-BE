package com.example.spin.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spin.controller.dto.ChallengeResponse;
import com.example.spin.controller.dto.RouteDetailResponse;
import com.example.spin.controller.dto.RouteGenerateRequest;
import com.example.spin.controller.dto.RouteGenerateResponse;
import com.example.spin.service.RouteChallengeService;
import com.example.spin.service.RouteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/routes")
@Tag(name = "루트", description = "AI 기반 루트 생성 및 도전 시작")
public class RouteController {

    private final RouteService routeService;
    private final RouteChallengeService routeChallengeService;

    @Operation(summary = "루트 생성",
            description = "지역/목적(+선택적으로 사용자 위경도)을 기반으로 Claude가 후보 가맹점 중 방문 순서가 있는 "
                    + "서로 다른 루트 대안 3개를 추천한다. 사용자는 그중 하나를 골라 이후 \"루트 도전 시작\" API를 호출하면 된다.")
    @PostMapping
    public List<RouteGenerateResponse> generateRoute(@RequestBody RouteGenerateRequest request) {
        return routeService.generateRoute(request);
    }

    @Operation(summary = "루트 상세 조회",
            description = "공유 링크(인스타 스토리 등)로 들어왔을 때 보여주는 루트 미리보기. 로그인 불필요. "
                    + "completedCount로 \"이 루트, N명이 다녀갔어요\"를 표시한다.")
    @GetMapping("/{routeId}")
    public RouteDetailResponse getRouteDetail(@PathVariable int routeId) {
        return routeService.getRouteDetail(routeId);
    }

    @Operation(summary = "루트 도전 시작", description = "\"이 루트로 시작\" 버튼 -> 로그인한 사용자의 도전 기록(RouteChallenge)을 생성한다.")
    @PostMapping("/{routeId}/challenges")
    public ChallengeResponse startChallenge(@PathVariable int routeId, Authentication authentication) {
        return routeChallengeService.startChallenge(authentication.getName(), routeId);
    }
}

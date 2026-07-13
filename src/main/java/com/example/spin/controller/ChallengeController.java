package com.example.spin.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spin.controller.dto.ChallengeProgressResponse;
import com.example.spin.controller.dto.ChallengeResponse;
import com.example.spin.controller.dto.CheckInRequest;
import com.example.spin.controller.dto.CheckInResponse;
import com.example.spin.controller.dto.RouteHistoryResponse;
import com.example.spin.service.RouteChallengeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
@Tag(name = "도전 기록", description = "QR 체크인, 완주 히스토리")
public class ChallengeController {

    private final RouteChallengeService routeChallengeService;

    @Operation(summary = "QR 체크인",
            description = "가게 QR의 코드값만 보내면 된다 (딥링크로 앱이 열렸을 때 그대로 전달하기 위함). "
                    + "challengeId는 필요 없음 — 이 가게를 포함한, 로그인한 사용자의 '진행 중인' 도전 기록을 서버가 찾아서 체크인한다 "
                    + "(여러 개 겹치면 가장 최근 시작한 것). 진행 중인 도전에 이 가게가 없으면 400, 이미 찍은 가게면 409. "
                    + "전체 스탑을 다 찍으면 자동으로 완주(COMPLETED) 처리된다. 체크인마다 포인트가 적립되며 "
                    + "pointsEarned/totalPoints로 결과를 바로 확인할 수 있다.")
    @PostMapping("/checkins")
    public CheckInResponse checkIn(@RequestBody CheckInRequest request, Authentication authentication) {
        return routeChallengeService.checkIn(authentication.getName(), request);
    }

    @Operation(summary = "진행 상태 조회",
            description = "로그인한 사용자의 현재 진행 중인(IN_PROGRESS) 도전 기록의 스탬프 현황과 다음 목적지를 반환한다. "
                    + "nextDestination의 latitude/longitude로 프론트에서 카카오맵/네이버맵 등 지도 앱 딥링크를 직접 구성하면 된다. "
                    + "진행 중인 도전 기록이 없으면 404.")
    @GetMapping("/current")
    public ChallengeProgressResponse getCurrentProgress(Authentication authentication) {
        return routeChallengeService.getCurrentProgress(authentication.getName());
    }

    @Operation(summary = "완주 히스토리 조회", description = "로그인한 사용자가 완주(COMPLETED)한 도전 기록만 최신순으로 반환한다.")
    @GetMapping("/history")
    public List<RouteHistoryResponse> getHistory(Authentication authentication) {
        return routeChallengeService.getCompletedHistory(authentication.getName());
    }
}

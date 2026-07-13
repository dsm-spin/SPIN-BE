package com.example.spin.controller;

import com.example.spin.controller.dto.AuthRequest;
import com.example.spin.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spin.service.AuthServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "인증", description = "회원가입/로그인 (로그아웃은 POST /auth/logout, Spring Security가 직접 처리)")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public void signup(@RequestBody AuthRequest request) {
        authService.signup(request);
    }

    @Operation(summary = "로그인", description = "성공 시 세션 쿠키(JSESSIONID)와 CSRF 토큰 쿠키(XSRF-TOKEN)가 내려온다.")
    @PostMapping("/login")
    public void login(@RequestBody AuthRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.login(request, httpRequest, httpResponse);
    }
}

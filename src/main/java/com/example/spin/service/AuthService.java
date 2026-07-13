package com.example.spin.service;

import com.example.spin.controller.dto.AuthRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    void signup(AuthRequest request);

    void login(AuthRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}

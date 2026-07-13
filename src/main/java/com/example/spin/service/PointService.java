package com.example.spin.service;

import com.example.spin.controller.dto.PointBalanceResponse;

public interface PointService {

    PointBalanceResponse getBalance(String accountId);
}

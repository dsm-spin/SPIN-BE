package com.example.spin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spin.controller.dto.PointBalanceResponse;
import com.example.spin.domain.Point;
import com.example.spin.domain.User;
import com.example.spin.exception.NotFoundException;
import com.example.spin.repository.PointRepository;
import com.example.spin.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointServiceImpl implements PointService {

    private final UserRepository userRepository;
    private final PointRepository pointRepository;

    @Override
    public PointBalanceResponse getBalance(String accountId) {
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 계정입니다: " + accountId));

        int balance = pointRepository.findByUser(user).map(Point::getBalance).orElse(0);
        return new PointBalanceResponse(balance);
    }
}

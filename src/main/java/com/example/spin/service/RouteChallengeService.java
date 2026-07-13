package com.example.spin.service;

import java.util.List;

import com.example.spin.controller.dto.ChallengeProgressResponse;
import com.example.spin.controller.dto.ChallengeResponse;
import com.example.spin.controller.dto.CheckInRequest;
import com.example.spin.controller.dto.CheckInResponse;
import com.example.spin.controller.dto.RouteHistoryResponse;

public interface RouteChallengeService {

    ChallengeResponse startChallenge(String accountId, int routeId);

    CheckInResponse checkIn(String accountId, CheckInRequest request);

    ChallengeProgressResponse getCurrentProgress(String accountId);

    List<RouteHistoryResponse> getCompletedHistory(String accountId);
}

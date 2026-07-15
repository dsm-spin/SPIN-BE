package com.example.spin.service;

import java.util.List;

import com.example.spin.controller.dto.RouteDetailResponse;
import com.example.spin.controller.dto.RouteGenerateRequest;
import com.example.spin.controller.dto.RouteGenerateResponse;

public interface RouteService {

    List<RouteGenerateResponse> generateRoute(RouteGenerateRequest request);

    RouteDetailResponse getRouteDetail(int routeId);
}

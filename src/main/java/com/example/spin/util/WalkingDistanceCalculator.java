package com.example.spin.util;

public final class WalkingDistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6371000;

    // 직선거리가 아니라 도로를 따라 걷는 실제 경로는 더 길어지므로 보정
    private static final double ROUTE_DETOUR_FACTOR = 1.3;

    // 도보 평균 속도 시속 4km 기준
    private static final double WALKING_METERS_PER_MINUTE = 67;

    private WalkingDistanceCalculator() {
    }

    public static int estimatedWalkingDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double straightLineMeters = haversineMeters(lat1, lon1, lat2, lon2);
        return (int) Math.round(straightLineMeters * ROUTE_DETOUR_FACTOR);
    }

    public static int estimatedWalkingMinutes(int walkingDistanceMeters) {
        if (walkingDistanceMeters <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(walkingDistanceMeters / WALKING_METERS_PER_MINUTE));
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}

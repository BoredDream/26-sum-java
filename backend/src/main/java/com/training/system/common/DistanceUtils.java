package com.training.system.common;

/**
 * 地理距离计算工具类
 * 使用 Haversine 公式计算两点间的球面距离
 */
public class DistanceUtils {

    /** 地球半径（米） */
    private static final double EARTH_RADIUS = 6_371_000;

    private DistanceUtils() {
    }

    /**
     * Haversine 公式计算两点间距离
     *
     * @param lat1 纬度 1
     * @param lng1 经度 1
     * @param lat2 纬度 2
     * @param lng2 经度 2
     * @return 距离（米），保留两位小数
     */
    public static double haversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;
        return Math.round(distance * 100.0) / 100.0;
    }
}

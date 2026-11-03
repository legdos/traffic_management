package com.example.traffic_management.util;

/**
 * 经纬度相关工具：球面距离（km）与沿直线移动。
 */
public final class GeoUtil {

    /** 地球平均半径（km） */
    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtil() {
    }

    /**
     * Haversine 公式计算两点间大圆距离（km）。
     */
    public static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    /**
     * 从 (lat, lng) 朝目标方向前进 stepKm，返回新的坐标 [lat, lng]。
     * 剩余距离不足一步时直接落到目标点，避免在终点附近来回抖动。
     */
    public static double[] moveTowards(double lat, double lng,
                                       double targetLat, double targetLng,
                                       double stepKm) {
        double remain = distanceKm(lat, lng, targetLat, targetLng);
        if (remain <= stepKm || remain == 0) {
            return new double[]{targetLat, targetLng};
        }
        double ratio = stepKm / remain;
        return new double[]{
                lat + (targetLat - lat) * ratio,
                lng + (targetLng - lng) * ratio
        };
    }
}

package com.example.traffic_management.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 折线（路线）工具：长度计算、沿折线推进、合成"类道路"路线。
 *
 * <p>点的统一约定是 {@code double[]{lng, lat}}（经度在前），与高德接口、GeoJSON 一致；
 * 只有进出车辆实体时才转成 (lat, lng)。
 */
public final class PolylineUtil {

    private static final double KM_PER_LAT = 110.574;

    private PolylineUtil() {
    }

    /** 折线总长度（km） */
    public static double totalKm(List<double[]> points) {
        double sum = 0;
        for (int i = 1; i < points.size(); i++) {
            double[] a = points.get(i - 1);
            double[] b = points.get(i);
            sum += GeoUtil.distanceKm(a[1], a[0], b[1], b[0]);
        }
        return sum;
    }

    /**
     * 把当前位置投影到折线上，再沿折线前进 stepKm，返回新的 [lat, lng]。
     * 用投影而不是记进度索引，好处是重启后仅凭车辆坐标就能恢复位置。
     */
    public static double[] advance(List<double[]> points, double lat, double lng, double stepKm) {
        if (points == null || points.isEmpty()) {
            return new double[]{lat, lng};
        }
        if (points.size() == 1) {
            return new double[]{points.get(0)[1], points.get(0)[0]};
        }

        double kLng = 111.320 * Math.cos(Math.toRadians(lat));

        // 1. 找最近线段及其参数 t
        int bestSeg = 1;
        double bestT = 0;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 1; i < points.size(); i++) {
            double[] a = points.get(i - 1);
            double[] b = points.get(i);
            double ax = (a[0] - lng) * kLng;
            double ay = (a[1] - lat) * KM_PER_LAT;
            double bx = (b[0] - lng) * kLng;
            double by = (b[1] - lat) * KM_PER_LAT;
            double dx = bx - ax;
            double dy = by - ay;
            double len2 = dx * dx + dy * dy;
            double t = len2 == 0 ? 0 : -(ax * dx + ay * dy) / len2;
            t = Math.max(0, Math.min(1, t));
            double px = ax + t * dx;
            double py = ay + t * dy;
            double d = Math.hypot(px, py);
            if (d < bestDistance) {
                bestDistance = d;
                bestSeg = i;
                bestT = t;
            }
        }

        // 2. 累积长度，算出当前沿线的弧长坐标
        double[] cumulative = new double[points.size()];
        for (int i = 1; i < points.size(); i++) {
            double[] a = points.get(i - 1);
            double[] b = points.get(i);
            cumulative[i] = cumulative[i - 1] + GeoUtil.distanceKm(a[1], a[0], b[1], b[0]);
        }
        double segLength = cumulative[bestSeg] - cumulative[bestSeg - 1];
        double s = cumulative[bestSeg - 1] + bestT * segLength + stepKm;
        double total = cumulative[cumulative.length - 1];

        if (s >= total) {
            double[] last = points.get(points.size() - 1);
            return new double[]{last[1], last[0]};
        }
        return pointAt(points, cumulative, s);
    }

    /** 按弧长取折线上的点，返回 [lat, lng] */
    public static double[] pointAt(List<double[]> points, double[] cumulative, double s) {
        for (int i = 1; i < points.size(); i++) {
            if (s <= cumulative[i]) {
                double segLength = cumulative[i] - cumulative[i - 1];
                double t = segLength <= 0 ? 0 : (s - cumulative[i - 1]) / segLength;
                double[] a = points.get(i - 1);
                double[] b = points.get(i);
                return new double[]{
                        a[1] + (b[1] - a[1]) * t,
                        a[0] + (b[0] - a[0]) * t
                };
            }
        }
        double[] last = points.get(points.size() - 1);
        return new double[]{last[1], last[0]};
    }

    /**
     * 合成一条"像路"的折线：在起终点连线上做正弦形侧向偏移，形成平滑弯道。
     * 未配置高德 Web 服务 key 时用它兜底，保证车辆不再是生硬的直线。
     *
     * @param seed 固定随机种子，保证同一对起终点每次生成的形状一致
     */
    public static List<double[]> synthetic(double lat1, double lng1, double lat2, double lng2, long seed) {
        final int segments = 14;
        double directKm = GeoUtil.distanceKm(lat1, lng1, lat2, lng2);
        // 侧向偏移幅度：取直线距离的 12%，最大 6km，保证长距离下也看得出弯道
        double amplitude = Math.min(directKm * 0.12, 6.0);
        if (directKm <= 0.5 || amplitude <= 0) {
            List<double[]> straight = new ArrayList<>();
            straight.add(new double[]{lng1, lat1});
            straight.add(new double[]{lng2, lat2});
            return straight;
        }

        Random random = new Random(seed);
        double phase = random.nextDouble() * Math.PI;
        double amplitude2 = amplitude * 0.35;

        double kLng = 111.320 * Math.cos(Math.toRadians(lat1));
        double dyKm = (lat2 - lat1) * KM_PER_LAT;
        double dxKm = (lng2 - lng1) * kLng;
        double norm = Math.hypot(dxKm, dyKm);
        // 垂直方向单位向量（km 平面内）
        double perpX = -dyKm / norm;
        double perpY = dxKm / norm;

        List<double[]> points = new ArrayList<>(segments + 1);
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            double lat = lat1 + (lat2 - lat1) * t;
            double lng = lng1 + (lng2 - lng1) * t;
            if (i > 0 && i < segments) {
                // 两个谐波叠加：主弯 + 次弯，避免整条线是单一圆弧
                double offsetKm = amplitude * Math.sin(Math.PI * t) * Math.sin(phase + Math.PI * t)
                        + amplitude2 * Math.sin(Math.PI * 2 * t) * Math.cos(phase);
                lat += (perpY * offsetKm) / KM_PER_LAT;
                lng += (perpX * offsetKm) / kLng;
            }
            points.add(new double[]{lng, lat});
        }
        return points;
    }
}

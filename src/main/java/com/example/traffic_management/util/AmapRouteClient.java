package com.example.traffic_management.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 高德「路径规划（驾车）」Web 服务客户端。
 * 需要「Web服务」类型的 key，JS API 的 key 会返回 USERKEY_PLAT_NOMATCH。
 */
@Component
public class AmapRouteClient {

    private static final Logger log = LoggerFactory.getLogger(AmapRouteClient.class);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * @return 折线点 [[lng,lat],...]；失败抛异常，由调用方回退到合成路线
     */
    public List<double[]> driving(double lat1, double lng1, double lat2, double lng2, String key) throws Exception {
        String url = String.format(
                "https://restapi.amap.com/v3/direction/driving?origin=%.6f,%.6f&destination=%.6f,%.6f"
                        + "&extensions=base&strategy=0&key=%s",
                lng1, lat1, lng2, lat2, key);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());
        if (!"1".equals(root.path("status").asString(""))) {
            throw new IllegalStateException("高德返回错误：" + root.path("info").asString("")
                    + "(" + root.path("infocode").asString("") + ")");
        }
        JsonNode steps = root.path("route").path("paths").path(0).path("steps");
        if (!steps.isArray() || steps.size() == 0) {
            throw new IllegalStateException("高德未返回可用路线");
        }

        List<double[]> points = new ArrayList<>();
        for (JsonNode step : steps) {
            String polyline = step.path("polyline").asString("");
            for (String pair : polyline.split(";")) {
                String[] parts = pair.split(",");
                if (parts.length != 2) {
                    continue;
                }
                try {
                    double lng = Double.parseDouble(parts[0].trim());
                    double lat = Double.parseDouble(parts[1].trim());
                    double[] previous = points.isEmpty() ? null : points.get(points.size() - 1);
                    if (previous == null || previous[0] != lng || previous[1] != lat) {
                        points.add(new double[]{lng, lat});
                    }
                } catch (NumberFormatException ignored) {
                    // 单个点解析失败不影响整体
                }
            }
        }
        if (points.size() < 2) {
            throw new IllegalStateException("高德返回的折线点不足");
        }
        log.debug("高德路径规划成功：{} 个折线点", points.size());
        return points;
    }
}

package com.example.traffic_management.controller;

import com.example.traffic_management.entity.Route;
import com.example.traffic_management.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    /** 路线折线点 [[lng,lat],...]，前端用来画车辆的行驶轨迹 */
    @GetMapping("/{id}/points")
    public List<double[]> points(@PathVariable("id") Integer id) {
        return routeService.pointsOf(id);
    }

    @GetMapping("/{id}")
    public Route detail(@PathVariable("id") Integer id) {
        return routeService.getById(id);
    }

    /**
     * 前端用高德 JS API 规划出真实道路后回写折线。
     * 请求体即折线点数组：[[lng,lat],[lng,lat],...]
     */
    @PutMapping("/{id}/points")
    public String updatePoints(@PathVariable("id") Integer id, @RequestBody List<double[]> points) {
        int updated = routeService.updateGeometry(id, points);
        return updated > 0 ? "路线已更新为导航路线" : "路线不存在或折线点不足";
    }
}

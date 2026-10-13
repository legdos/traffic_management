package com.example.traffic_management.controller;

import com.example.traffic_management.entity.Mission;
import com.example.traffic_management.service.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/missions")
public class MissionController {

    @Autowired
    private MissionService missionService;

    @GetMapping
    public List<Mission> list() {
        return missionService.findAll();
    }

    @GetMapping("/{id}")
    public Mission getById(@PathVariable Integer id) {
        return missionService.findById(id);
    }

    @PostMapping("/assign/{needId}")
    public String assign(@PathVariable Integer needId) {
        Mission mission = missionService.createAndAssign(needId);
        return "任务生成成功，任务编号：" + mission.getMissionNo() + "，ID：" + mission.getId();
    }

    @PostMapping("/batch-assign")
    public String batchAssign() {
        List<Mission> missions = missionService.batchAssign();
        return "批量指派完成，共生成 " + missions.size() + " 个任务";
    }

    @PutMapping("/{id}/status")
    public String updateStatus(@PathVariable Integer id, @RequestParam String status) {
        missionService.updateStatus(id, status);
        return "任务状态更新成功";
    }
}
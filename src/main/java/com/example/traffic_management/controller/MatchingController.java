package com.example.traffic_management.controller;

import com.example.traffic_management.entity.MatchingResult;
import com.example.traffic_management.service.MatchingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matching")
public class MatchingController {
    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) { this.matchingService = matchingService; }

    @GetMapping("/score")
    public MatchingResult score(@RequestParam Integer needId, @RequestParam Integer vehicleId) {
        return matchingService.score(needId, vehicleId);
    }
}

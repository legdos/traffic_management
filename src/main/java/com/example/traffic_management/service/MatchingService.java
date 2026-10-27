package com.example.traffic_management.service;

import com.example.traffic_management.entity.MatchingResult;

public interface MatchingService {
    MatchingResult score(Integer needId, Integer vehicleId);
}

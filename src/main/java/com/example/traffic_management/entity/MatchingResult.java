package com.example.traffic_management.entity;

import java.math.BigDecimal;
import java.util.List;

public class MatchingResult {
    private Integer needId;
    private Integer vehicleId;
    private boolean eligible;
    private BigDecimal score;
    private BigDecimal distanceKm;
    private BigDecimal capacityScore;
    private BigDecimal typeScore;
    private BigDecimal distanceScore;
    private List<String> reasons;

    public Integer getNeedId() { return needId; }
    public void setNeedId(Integer value) { needId = value; }
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer value) { vehicleId = value; }
    public boolean isEligible() { return eligible; }
    public void setEligible(boolean value) { eligible = value; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal value) { score = value; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal value) { distanceKm = value; }
    public BigDecimal getCapacityScore() { return capacityScore; }
    public void setCapacityScore(BigDecimal value) { capacityScore = value; }
    public BigDecimal getTypeScore() { return typeScore; }
    public void setTypeScore(BigDecimal value) { typeScore = value; }
    public BigDecimal getDistanceScore() { return distanceScore; }
    public void setDistanceScore(BigDecimal value) { distanceScore = value; }
    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> value) { reasons = value; }
}

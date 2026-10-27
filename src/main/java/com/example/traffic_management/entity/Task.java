package com.example.traffic_management.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Task {
    private Integer id;
    private String taskName;
    private Integer needId;
    private Integer routeId;
    private Integer vehicleId;
    private String status;
    private BigDecimal matchScore;
    private Date completedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Integer getNeedId() { return needId; }
    public void setNeedId(Integer needId) { this.needId = needId; }
    public Integer getRouteId() { return routeId; }
    public void setRouteId(Integer routeId) { this.routeId = routeId; }
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getMatchScore() { return matchScore; }
    public void setMatchScore(BigDecimal matchScore) { this.matchScore = matchScore; }
    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
}

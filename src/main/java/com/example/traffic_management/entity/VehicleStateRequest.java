package com.example.traffic_management.entity;

public class VehicleStateRequest {
    private Long version;
    private String status;

    public Long getVersion() { return version; }
    public void setVersion(Long value) { version = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}

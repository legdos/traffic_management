package com.example.traffic_management.entity;

public class VehiclePositionRequest {
    private Long version;
    private Double latitude;
    private Double longitude;
    private String observedAt;

    public Long getVersion() { return version; }
    public void setVersion(Long value) { version = value; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double value) { latitude = value; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double value) { longitude = value; }
    public String getObservedAt() { return observedAt; }
    public void setObservedAt(String value) { observedAt = value; }
}

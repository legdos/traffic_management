package com.example.traffic_management.entity;

/** One assigned vehicle and the factory coordinates it should approach. */
public class SimulationTarget {
    private Integer id;
    private Double currentLatitude;
    private Double currentLongitude;
    private Long version;
    private Double targetLatitude;
    private Double targetLongitude;

    public Integer getId() { return id; }
    public void setId(Integer value) { id = value; }
    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double value) { currentLatitude = value; }
    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double value) { currentLongitude = value; }
    public Long getVersion() { return version; }
    public void setVersion(Long value) { version = value; }
    public Double getTargetLatitude() { return targetLatitude; }
    public void setTargetLatitude(Double value) { targetLatitude = value; }
    public Double getTargetLongitude() { return targetLongitude; }
    public void setTargetLongitude(Double value) { targetLongitude = value; }
}

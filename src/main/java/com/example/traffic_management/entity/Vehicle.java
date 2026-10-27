package com.example.traffic_management.entity;




public class Vehicle {

    private Integer id;

    private String bandName;

    private String licenseNumber;

    private String typeCode;
    private String maxLoad;

    private Double currentLatitude;

    private Double currentLongitude;

    private String currentAction;
    
    private Integer isDeleted;

    private String status;
    private Long version;
    private Integer routeId;
    private Integer segmentIndex;
    private Double segmentProgress;
    private java.util.Date positionAt;
    public Integer getId() { return id; }
    public void setId(Integer value) { this.id = value; }
    public String getBandName() { return bandName; }
    public void setBandName(String value) { this.bandName = value; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String value) { this.licenseNumber = value; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String value) { this.typeCode = value; }
    public String getMaxLoad() { return maxLoad; }
    public void setMaxLoad(String value) { this.maxLoad = value; }
    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double value) { this.currentLatitude = value; }
    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double value) { this.currentLongitude = value; }
    public String getCurrentAction() { return currentAction; }
    public void setCurrentAction(String value) { this.currentAction = value; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer value) { this.isDeleted = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public Long getVersion() { return version; }
    public void setVersion(Long value) { this.version = value; }
    public Integer getRouteId() { return routeId; }
    public void setRouteId(Integer value) { this.routeId = value; }
    public Integer getSegmentIndex() { return segmentIndex; }
    public void setSegmentIndex(Integer value) { this.segmentIndex = value; }
    public Double getSegmentProgress() { return segmentProgress; }
    public void setSegmentProgress(Double value) { this.segmentProgress = value; }
    public java.util.Date getPositionAt() { return positionAt; }
    public void setPositionAt(java.util.Date value) { this.positionAt = value; }
}

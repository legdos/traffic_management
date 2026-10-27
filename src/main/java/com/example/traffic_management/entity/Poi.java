package com.example.traffic_management.entity;

public class Poi {
    private Integer id;
    private String poiName;
    private String poiType;
    private String poiAddress;
    private Double poiLatitude;
    private Double poiLongitude;
    private boolean deleted;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPoiName() { return poiName; }
    public void setPoiName(String poiName) { this.poiName = poiName; }
    public String getPoiType() { return poiType; }
    public void setPoiType(String poiType) { this.poiType = poiType; }
    public String getPoiAddress() { return poiAddress; }
    public void setPoiAddress(String poiAddress) { this.poiAddress = poiAddress; }
    public Double getPoiLatitude() { return poiLatitude; }
    public void setPoiLatitude(Double poiLatitude) { this.poiLatitude = poiLatitude; }
    public Double getPoiLongitude() { return poiLongitude; }
    public void setPoiLongitude(Double poiLongitude) { this.poiLongitude = poiLongitude; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}

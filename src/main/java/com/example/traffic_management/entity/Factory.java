package com.example.traffic_management.entity;


import java.util.Date;


public class Factory {
    private Integer id;

    private String factoryName;

    private String address;

    private String phone;

    private Date createTime;

    private Integer needId;

    private Double factoryLatitude;

    private Double factoryLongitude;
    public Integer getId() { return id; }
    public void setId(Integer value) { this.id = value; }
    public String getFactoryName() { return factoryName; }
    public void setFactoryName(String value) { this.factoryName = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { this.address = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { this.createTime = value; }
    public Integer getNeedId() { return needId; }
    public void setNeedId(Integer value) { this.needId = value; }
    public Double getFactoryLatitude() { return factoryLatitude; }
    public void setFactoryLatitude(Double value) { this.factoryLatitude = value; }
    public Double getFactoryLongitude() { return factoryLongitude; }
    public void setFactoryLongitude(Double value) { this.factoryLongitude = value; }
}
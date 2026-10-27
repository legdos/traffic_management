package com.example.traffic_management.entity;




public class Need {

    // 需求ID (主键)
    private Integer id;

    // 需求内容
    private String needContent;

    // 关联工厂ID
    private Integer factoryId;

    private Integer isDeleted;

    private Integer cargoId;
    private java.math.BigDecimal cargoTotalWeight;
    private String status;
    private String requiredVehicleType;
    public Integer getId() { return id; }
    public void setId(Integer value) { this.id = value; }
    public String getNeedContent() { return needContent; }
    public void setNeedContent(String value) { this.needContent = value; }
    public Integer getFactoryId() { return factoryId; }
    public void setFactoryId(Integer value) { this.factoryId = value; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer value) { this.isDeleted = value; }
    public Integer getCargoId() { return cargoId; }
    public void setCargoId(Integer value) { this.cargoId = value; }
    public java.math.BigDecimal getCargoTotalWeight() { return cargoTotalWeight; }
    public void setCargoTotalWeight(java.math.BigDecimal value) { this.cargoTotalWeight = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getRequiredVehicleType() { return requiredVehicleType; }
    public void setRequiredVehicleType(String value) { this.requiredVehicleType = value; }
}

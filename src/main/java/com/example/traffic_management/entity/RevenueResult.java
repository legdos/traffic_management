package com.example.traffic_management.entity;

import java.math.BigDecimal;

public class RevenueResult {
    private Integer taskId;
    private BigDecimal cargoWeightT;
    private BigDecimal distanceKm;
    private BigDecimal pricePerTonKm;
    private BigDecimal fuelCostPerKm;
    private BigDecimal accidentLossRate;
    private BigDecimal transportRevenue;
    private BigDecimal fuelCost;
    private BigDecimal accidentLoss;
    private BigDecimal netRevenue;
    private String currency;

    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public BigDecimal getCargoWeightT() { return cargoWeightT; }
    public void setCargoWeightT(BigDecimal cargoWeightT) { this.cargoWeightT = cargoWeightT; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }
    public BigDecimal getPricePerTonKm() { return pricePerTonKm; }
    public void setPricePerTonKm(BigDecimal pricePerTonKm) { this.pricePerTonKm = pricePerTonKm; }
    public BigDecimal getFuelCostPerKm() { return fuelCostPerKm; }
    public void setFuelCostPerKm(BigDecimal fuelCostPerKm) { this.fuelCostPerKm = fuelCostPerKm; }
    public BigDecimal getAccidentLossRate() { return accidentLossRate; }
    public void setAccidentLossRate(BigDecimal accidentLossRate) { this.accidentLossRate = accidentLossRate; }
    public BigDecimal getTransportRevenue() { return transportRevenue; }
    public void setTransportRevenue(BigDecimal transportRevenue) { this.transportRevenue = transportRevenue; }
    public BigDecimal getFuelCost() { return fuelCost; }
    public void setFuelCost(BigDecimal fuelCost) { this.fuelCost = fuelCost; }
    public BigDecimal getAccidentLoss() { return accidentLoss; }
    public void setAccidentLoss(BigDecimal accidentLoss) { this.accidentLoss = accidentLoss; }
    public BigDecimal getNetRevenue() { return netRevenue; }
    public void setNetRevenue(BigDecimal netRevenue) { this.netRevenue = netRevenue; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}

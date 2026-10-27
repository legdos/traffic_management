package com.example.traffic_management.entity;




public class Action {
    private Integer id;
    private String actionName;
    private String actionType;
    private Integer duration;
    private java.math.BigDecimal accidentRate;
    public Integer getId() { return id; }
    public void setId(Integer value) { this.id = value; }
    public String getActionName() { return actionName; }
    public void setActionName(String value) { this.actionName = value; }
    public String getActionType() { return actionType; }
    public void setActionType(String value) { this.actionType = value; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer value) { this.duration = value; }
    public java.math.BigDecimal getAccidentRate() { return accidentRate; }
    public void setAccidentRate(java.math.BigDecimal value) { this.accidentRate = value; }
}
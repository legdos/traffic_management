package com.example.traffic_management.entity;

public class SimulationStatus {
    private boolean running;
    private long ticks;
    private int movedVehicles;
    private String lastError;

    public boolean isRunning() { return running; }
    public void setRunning(boolean value) { running = value; }
    public long getTicks() { return ticks; }
    public void setTicks(long value) { ticks = value; }
    public int getMovedVehicles() { return movedVehicles; }
    public void setMovedVehicles(int value) { movedVehicles = value; }
    public String getLastError() { return lastError; }
    public void setLastError(String value) { lastError = value; }
}

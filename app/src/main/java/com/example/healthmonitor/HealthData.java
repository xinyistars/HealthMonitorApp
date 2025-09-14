package com.example.healthmonitor;

public class HealthData {
    private long timestamp;
    private float ecgData;          // 心电信号值
    private float temperature;      // 体温
    private int heartRate;          // 当前心率
    private int heartRateMax;       // 心率最高值
    private int heartRateMin;       // 心率最低值
    private int bloodOxygen;        // 血氧浓度
    private boolean isDeviceOn;     // 设备开关状态
    private boolean ecgModuleOn;    // 心电模块状态
    private boolean tempModuleOn;   // 体温模块状态
    private boolean hrModuleOn;     // 心率模块状态
    private boolean oxModuleOn;     // 血氧模块状态

    public HealthData() {
        this.timestamp = System.currentTimeMillis();
        this.ecgData = 0f;
        this.temperature = 0f;
        this.heartRate = 0;
        this.heartRateMax = 0;
        this.heartRateMin = 0;
        this.bloodOxygen = 0;
        this.isDeviceOn = false;
        this.ecgModuleOn = false;
        this.tempModuleOn = false;
        this.hrModuleOn = false;
        this.oxModuleOn = false;
    }

    // Getter 和 Setter 方法
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public float getEcgData() { return ecgData; }
    public void setEcgData(float ecgData) { this.ecgData = ecgData; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public int getHeartRateMax() { return heartRateMax; }
    public void setHeartRateMax(int heartRateMax) { this.heartRateMax = heartRateMax; }

    public int getHeartRateMin() { return heartRateMin; }
    public void setHeartRateMin(int heartRateMin) { this.heartRateMin = heartRateMin; }

    public int getBloodOxygen() { return bloodOxygen; }
    public void setBloodOxygen(int bloodOxygen) { this.bloodOxygen = bloodOxygen; }

    public boolean isDeviceOn() { return isDeviceOn; }
    public void setDeviceOn(boolean deviceOn) { isDeviceOn = deviceOn; }

    public boolean isEcgModuleOn() { return ecgModuleOn; }
    public void setEcgModuleOn(boolean ecgModuleOn) { this.ecgModuleOn = ecgModuleOn; }

    public boolean isTempModuleOn() { return tempModuleOn; }
    public void setTempModuleOn(boolean tempModuleOn) { this.tempModuleOn = tempModuleOn; }

    public boolean isHrModuleOn() { return hrModuleOn; }
    public void setHrModuleOn(boolean hrModuleOn) { this.hrModuleOn = hrModuleOn; }

    public boolean isOxModuleOn() { return oxModuleOn; }
    public void setOxModuleOn(boolean oxModuleOn) { this.oxModuleOn = oxModuleOn; }
}
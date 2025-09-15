package com.example.healthmonitor;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DataRepository {
    private static DataRepository instance;
    private final MutableLiveData<HealthData> healthData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deviceStatus = new MutableLiveData<>();
    private final Random random = new Random();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> dataTask;
    private int currentHeartRateMax = 0;
    private int currentHeartRateMin = 0;

    private DataRepository() {
        healthData.setValue(new HealthData());
        deviceStatus.setValue(false);
    }

    public static synchronized DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    public MutableLiveData<HealthData> getHealthData() {
        return healthData;
    }

    public MutableLiveData<Boolean> getDeviceStatus() {
        return deviceStatus;
    }

    // 修改 generateRandomData 方法
    private HealthData generateRandomData() {
        HealthData data = new HealthData();

        // 获取当前设备状态
        Boolean isDeviceOn = deviceStatus.getValue();

        if (isDeviceOn != null && isDeviceOn) {
            // 设备开启时生成随机数据
            int newHeartRate = random.nextInt(40) + 60; // 60-100 bpm
            data
                    .setEcgData(generateECGWaveform());
            data
                    .setTemperature(36.5f + random.nextFloat() * 1.5f); // 36.5-38.0°C
            data
                    .setHeartRate(newHeartRate);
            data
                    .setBloodOxygen(random.nextInt(6) + 95); // 95-100%

            // 更新心率极值
            if (newHeartRate > currentHeartRateMax || currentHeartRateMax == 0) {
                currentHeartRateMax
                        = newHeartRate;
            }
            if (newHeartRate < currentHeartRateMin || currentHeartRateMin == 0) {
                currentHeartRateMin
                        = newHeartRate;
            }
            data
                    .setHeartRateMax(currentHeartRateMax);
            data
                    .setHeartRateMin(currentHeartRateMin);
        } else {
            // 设备关闭时设置默认值或空值
            data
                    .setEcgData(0f);
            data
                    .setTemperature(0f);
            data
                    .setHeartRate(0);
            data
                    .setBloodOxygen(0);
            data
                    .setHeartRateMax(0);
            data
                    .setHeartRateMin(0);
        }

        data
                .setDeviceOn(deviceStatus.getValue() != null && deviceStatus.getValue());

        // 保持模块状态
        HealthData currentData = healthData.getValue();
        if (currentData != null) {
            data
                    .setEcgModuleOn(currentData.isEcgModuleOn());
            data
                    .setTempModuleOn(currentData.isTempModuleOn());
            data
                    .setHrModuleOn(currentData.isHrModuleOn());
            data
                    .setOxModuleOn(currentData.isOxModuleOn());
        }

        return data;
    }

    private float generateECGWaveform() {
        long time = System.currentTimeMillis() % 1000;
        float timeNormalized = time / 1000.0f;

        // 模拟典型的心电波形
        if (timeNormalized < 0.2f) return (float) Math.sin(timeNormalized * 20) * 0.3f; // P波
        else if (timeNormalized < 0.3f) return (float) Math.sin((timeNormalized - 0.2f) * 50) * 1.5f; // QRS波
        else if (timeNormalized < 0.5f) return (float) Math.sin((timeNormalized - 0.3f) * 10) * 0.8f; // T波
        else return 0f + (random.nextFloat() - 0.5f) * 0.1f; // 基线噪声
    }

    public void startDataSimulation() {
        stopDataSimulation();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        dataTask = scheduler.scheduleAtFixedRate(() -> {
            if (deviceStatus.getValue() != null && deviceStatus.getValue()) {
                HealthData newData = generateRandomData();
                healthData.postValue(newData);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stopDataSimulation() {
        if (dataTask != null && !dataTask.isCancelled()) {
            dataTask.cancel(true);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
        // 重置极值
        currentHeartRateMax = 0;
        currentHeartRateMin = 0;
        healthData.postValue(new HealthData());
    }

    public void toggleDevicePower() {
        Boolean currentStatus = deviceStatus.getValue();
        boolean newStatus = currentStatus == null || !currentStatus;
        deviceStatus.postValue(newStatus);

        if (newStatus) {
            startDataSimulation();
        } else {
            stopDataSimulation();
        }
    }

    // 修改 toggleModule 方法，添加设备状态检查
    public void toggleModule(String module) {
        HealthData currentData = healthData.getValue();
        if (currentData == null) return;

        // 检查设备是否开启
        Boolean isDeviceOn = deviceStatus.getValue();
        if (isDeviceOn == null || !isDeviceOn) {
            // 设备未开启，不允许开启模块
            Log.d("DataRepository", "Device is off, cannot toggle module: " + module);
            return;
        }

        HealthData newData = new HealthData();
        // 复制当前数据
        newData.setEcgData(currentData.getEcgData());
        newData.setTemperature(currentData.getTemperature());
        newData.setHeartRate(currentData.getHeartRate());
        newData.setHeartRateMax(currentData.getHeartRateMax());
        newData.setHeartRateMin(currentData.getHeartRateMin());
        newData.setBloodOxygen(currentData.getBloodOxygen());
        newData.setDeviceOn(currentData.isDeviceOn());
        newData.setEcgModuleOn(currentData.isEcgModuleOn());
        newData.setTempModuleOn(currentData.isTempModuleOn());
        newData.setHrModuleOn(currentData.isHrModuleOn());
        newData.setOxModuleOn(currentData.isOxModuleOn());

        // 切换指定模块状态
        switch (module) {
            case "ECG":
                newData.setEcgModuleOn(!currentData.isEcgModuleOn());
                break;
            case "TEMP":
                newData.setTempModuleOn(!currentData.isTempModuleOn());
                break;
            case "HR":
                newData.setHrModuleOn(!currentData.isHrModuleOn());
                break;
            case "OX":
                newData.setOxModuleOn(!currentData.isOxModuleOn());
                break;
        }

        healthData.postValue(newData);
    }
}
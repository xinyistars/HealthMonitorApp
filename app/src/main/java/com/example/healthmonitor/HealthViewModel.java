package com.example.healthmonitor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HealthViewModel extends ViewModel {
    private final DataRepository repository;

    public HealthViewModel() {
        repository = DataRepository.getInstance();
    }

    public LiveData<HealthData> getHealthData() {
        return repository.getHealthData();
    }

    public LiveData<Boolean> getDeviceStatus() {
        return repository.getDeviceStatus();
    }

    public void toggleDevicePower() {
        repository.toggleDevicePower();
    }

    public void toggleModule(String module) {
        repository.toggleModule(module);
    }
}
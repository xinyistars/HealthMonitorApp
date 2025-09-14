package com.example.healthmonitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import android.graphics.Color;

public class MainActivity extends AppCompatActivity {

    private HealthViewModel viewModel;
    private ECGView ecgView;
    private Handler ecgHandler;
    private Runnable ecgRunnable;

    // 所有视图控件声明
    private TextView ecgValue, temperatureValue, heartRateValue, heartRateMaxValue, heartRateMinValue, bloodOxygenValue;
    private TextView ecgStatus, tempStatus, hrStatus, oxStatus;
    private Button ecgToggleBtn, tempToggleBtn, hrToggleBtn, oxToggleBtn, btnToggleTheme;
    private Switch deviceSwitch;
    private TextView connectionStatus;

    // 用于存储用户的主题选择
    private SharedPreferences sharedPreferences;



    // 根据用户偏好设置主题
    private void setThemeFromPreferences() {
        boolean isNightMode = sharedPreferences.getBoolean("nightMode", false);
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // 在类变量中添加
    private boolean isThemeChanging = false;

    // 修改 toggleTheme 方法
    private void toggleTheme() {
        isThemeChanging = true;

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            saveThemePreference(false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            saveThemePreference(true);
        }
        recreate();
    }

    // 修改 onDestroy 方法
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "=== ACTIVITY DESTROYED ===");

        try {
            if (ecgHandler != null && ecgRunnable != null) {
                ecgHandler.removeCallbacks(ecgRunnable);
            }
            // 只有在不是主题切换时才停止数据模拟
            if (!isThemeChanging) {
                DataRepository.getInstance().stopDataSimulation();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "ERROR in onDestroy: " + e.getMessage(), e);
        }
    }

    // 修改 onCreate 方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity", "=== APPLICATION START ===");
        isThemeChanging = false; // 重置标志

        // 设置布局
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "Layout set to activity_main");

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences("appPreferences", MODE_PRIVATE);

        // 设置白天/黑夜模式
        setThemeFromPreferences();

        // 初始化 Handler
        ecgHandler = new Handler(Looper.getMainLooper());

        // 延迟初始化
        new Handler().postDelayed(() -> {
            Log.d("MainActivity", "Starting delayed initialization...");
            initAllViews();
            setupListeners();
            setupViewModel();
            startECGUpdate();
            Log.d("MainActivity", "Delayed initialization completed successfully");
        }, 200);
    }
    // 保存用户的主题选择
    private void saveThemePreference(boolean isNightMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("nightMode", isNightMode);
        editor.apply();
    }

    private void initAllViews() {
        Log.d("MainActivity", "=== INITIALIZING ALL VIEWS ===");

        try {
            // 延迟初始化视图
            ecgView = findViewById(R.id.ecgView);
            deviceSwitch = findViewById(R.id.deviceSwitch);
            connectionStatus = findViewById(R.id.connectionStatus);
            ecgValue = findViewById(R.id.ecgValue);
            temperatureValue = findViewById(R.id.temperatureValue);
            heartRateValue = findViewById(R.id.heartRateValue);
            heartRateMaxValue = findViewById(R.id.heartRateMaxValue);
            heartRateMinValue = findViewById(R.id.heartRateMinValue);
            bloodOxygenValue = findViewById(R.id.bloodOxygenValue);
            ecgStatus = findViewById(R.id.ecgStatus);
            tempStatus = findViewById(R.id.tempStatus);
            hrStatus = findViewById(R.id.hrStatus);
            oxStatus = findViewById(R.id.oxStatus);
            ecgToggleBtn = findViewById(R.id.ecgToggleBtn);
            tempToggleBtn = findViewById(R.id.tempToggleBtn);
            hrToggleBtn = findViewById(R.id.hrToggleBtn);
            oxToggleBtn = findViewById(R.id.oxToggleBtn);
            btnToggleTheme = findViewById(R.id.btnToggleTheme);

        } catch (Exception e) {
            Log.e("MainActivity", "ERROR in initAllViews: " + e.getMessage(), e);
        }

        Log.d("MainActivity", "=== VIEW INITIALIZATION COMPLETE ===");
    }

    private void setupListeners() {
        Log.d("MainActivity", "=== SETTING UP LISTENERS ===");

        try {
            // 设备开关监听
            if (deviceSwitch != null) {
                deviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Log.d("MainActivity", "Device switch toggled: " + isChecked);
                        viewModel.toggleDevicePower();
                    }
                });
            }

            // 模块按钮监听
            setClickListenerSafe(ecgToggleBtn, "ECG");
            setClickListenerSafe(tempToggleBtn, "TEMP");
            setClickListenerSafe(hrToggleBtn, "HR");
            setClickListenerSafe(oxToggleBtn, "OX");

            // 添加白天/黑夜模式切换的按钮监听
            btnToggleTheme.setOnClickListener(v -> toggleTheme());

        } catch (Exception e) {
            Log.e("MainActivity", "ERROR in setupListeners: " + e.getMessage(), e);
        }

        Log.d("MainActivity", "=== LISTENERS SETUP COMPLETE ===");
    }

    private void setClickListenerSafe(Button button, final String module) {
        if (button != null) {
            button.setOnClickListener(v -> {
                Log.d("MainActivity", module + " module button clicked");
                viewModel.toggleModule(module);
            });
        }
    }

    private void setupViewModel() {
        Log.d("MainActivity", "=== SETTING UP VIEWMODEL ===");

        try {
            viewModel = new ViewModelProvider(this).get(HealthViewModel.class);
            Log.d("MainActivity", "ViewModel obtained");

            // 观察设备状态 - 正确的代码位置
            viewModel.getDeviceStatus().observe(this, isOn -> {
                if (isOn != null) {
                    Log.d("MainActivity", "Device status changed: " + isOn);
                    if (deviceSwitch != null) {
                        deviceSwitch.setChecked(isOn);
                    }
                    if (connectionStatus != null) {
                        connectionStatus.setText(isOn ? "设备运行中" : "设备已关闭");
                    }
                }
            });

            // 观察健康数据
            viewModel.getHealthData().observe(this, newData -> {
                if (newData != null) {
                    Log.d("MainActivity", "New data received: " + newData.toString());
                    updateHealthDataUI(newData);
                }
            });

        } catch (Exception e) {
            Log.e("MainActivity", "ERROR in setupViewModel: " + e.getMessage(), e);
        }

        Log.d("MainActivity", "=== VIEWMODEL SETUP COMPLETE ===");
    }

    private void updateHealthDataUI(HealthData data) {
        try {
            // 更新数值显示
            setTextSafe(ecgValue, data.isEcgModuleOn() ?
                    String.format("%.2f", data.getEcgData()) + " mV" : "-- mV");

            setTextSafe(temperatureValue, data.isTempModuleOn() ?
                    String.format("%.1f", data.getTemperature()) + " °C" : "-- °C");

            setTextSafe(heartRateValue, data.isHrModuleOn() ?
                    data.getHeartRate() + " bpm" : "-- bpm");

            setTextSafe(heartRateMaxValue, data.isHrModuleOn() ?
                    data.getHeartRateMax() + " bpm" : "-- bpm");

            setTextSafe(heartRateMinValue, data.isHrModuleOn() ?
                    data.getHeartRateMin() + " bpm" : "-- bpm");

            setTextSafe(bloodOxygenValue, data.isOxModuleOn() ?
                    data.getBloodOxygen() + " %" : "-- %");

            // 更新状态显示
            updateModuleStatusUI(data);

            // 同步心电模块状态到心电图视图
            if (ecgView != null) {
                ecgView.setEcgModuleOn(data.isEcgModuleOn());
            }

        } catch (Exception e) {
            Log.e("MainActivity", "ERROR in updateHealthDataUI: " + e.getMessage(), e);
        }
    }

    private void updateModuleStatusUI(HealthData data) {
        try {
            updateStatusViewSafe(ecgStatus, data.isEcgModuleOn());
            updateStatusViewSafe(tempStatus, data.isTempModuleOn());
            updateStatusViewSafe(hrStatus, data.isHrModuleOn());
            updateStatusViewSafe(oxStatus, data.isOxModuleOn());

            updateButtonTextSafe(ecgToggleBtn, data.isEcgModuleOn());
            updateButtonTextSafe(tempToggleBtn, data.isTempModuleOn());
            updateButtonTextSafe(hrToggleBtn, data.isHrModuleOn());
            updateButtonTextSafe(oxToggleBtn, data.isOxModuleOn());

        } catch (Exception e) {
            Log.e("MainActivity", "ERROR in updateModuleStatusUI: " + e.getMessage(), e);
        }
    }

    // 安全的辅助方法
    private void setTextSafe(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }

    private void updateStatusViewSafe(TextView statusView, boolean isOn) {
        if (statusView != null) {
            if (isOn) {
                statusView.setText("运行中");
                statusView.setBackgroundResource(R.drawable.status_bg_on);
            } else {
                statusView.setText("关闭");
                statusView.setBackgroundResource(R.drawable.status_bg_off);
            }
            statusView.setTextColor(Color.WHITE);
        }
    }

    private void updateButtonTextSafe(Button button, boolean isOn) {
        if (button != null) {
            button.setText(isOn ? "关闭" : "开启");
            int color = isOn ? android.R.color.holo_red_dark : android.R.color.holo_green_dark;
            button.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, color)));
        }
    }



    private void startECGUpdate() {
        Log.d("MainActivity", "=== STARTING ECG UPDATE ===");

        try {
            ecgRunnable = new Runnable() {
                @Override
                public void run() {
                    if (ecgView != null) {
                        ecgView.updateWaveform();
                    }
                    if (ecgHandler != null) {
                        ecgHandler.postDelayed(this, 50);
                    }
                }
            };
            ecgHandler.post(ecgRunnable);
            Log.d("MainActivity", "ECG update started");

        } catch (Exception e) {
            Log.e("MainActivity", "ERROR in startECGUpdate: " + e.getMessage(), e);
        }
    }
}
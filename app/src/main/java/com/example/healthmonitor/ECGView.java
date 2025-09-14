package com.example.healthmonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ECGView extends View {
    private final List<Float> dataPoints = new ArrayList<>();
    private final Paint paint = new Paint();
    private final Path path = new Path();
    private final Random random = new Random();
    private final int maxDataPoints = 300;
    private long lastUpdateTime = 0;
    private boolean isEcgModuleOn = false; // 改为心电模块状态

    public ECGView(Context context) {
        super(context);
        init();
    }

    public ECGView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setColor(Color.parseColor("#FF6B6B"));
        paint.setStrokeWidth(4f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        // 初始化一些模拟数据
        for (int i = 0; i < maxDataPoints; i++) {
            dataPoints.add(0f);
        }

        Log.d("ECGView", "ECGView initialized");
    }

    // 设置心电模块状态
    public void setEcgModuleOn(boolean isOn) {
        Log.d("ECGView", "ECG module status set to: " + isOn);
        this.isEcgModuleOn = isOn;
        if (!isOn) {
            clearData();
        }
        invalidate(); // 立即重绘
    }

    public void addDataPoint(float value) {
        if (!isEcgModuleOn) {
            Log.d("ECGView", "ECG module is off, skipping data point");
            return; // 心电模块关闭时不添加数据
        }

        dataPoints.add(value);
        if (dataPoints.size() > maxDataPoints) {
            dataPoints.remove(0);
        }
        invalidate(); // 重绘画布

        Log.d("ECGView", "Data point added: " + value + ", total points: " + dataPoints.size());
    }

    public void updateWaveform() {
        if (!isEcgModuleOn) {
            Log.d("ECGView", "ECG module is off, skipping waveform update");
            return; // 心电模块关闭时不更新波形
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < 50) { // 20 FPS
            return;
        }
        lastUpdateTime = currentTime;

        // 生成模拟心电波形数据
        float value = generateECGWaveform();
        addDataPoint(value);

        Log.d("ECGView", "Waveform updated, value: " + value);
    }

    private float generateECGWaveform() {
        long time = System.currentTimeMillis() % 2000;
        float timeNormalized = time / 2000.0f;

        // 模拟典型的心电波形
        if (timeNormalized < 0.2f) return (float) Math.sin(timeNormalized * 20) * 0.3f; // P波
        else if (timeNormalized < 0.3f) return (float) Math.sin((timeNormalized - 0.2f) * 50) * 1.5f; // QRS波
        else if (timeNormalized < 0.5f) return (float) Math.sin((timeNormalized - 0.3f) * 10) * 0.8f; // T波
        else return 0f + (random.nextFloat() - 0.5f) * 0.1f; // 基线噪声
    }

    public void clearData() {
        Log.d("ECGView", "Clearing ECG data");
        dataPoints.clear();
        for (int i = 0; i < maxDataPoints; i++) {
            dataPoints.add(0f);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataPoints.size() < 2) {
            Log.d("ECGView", "Not enough data points to draw: " + dataPoints.size());
            // 即使数据点不够也绘制网格
            drawGrid(canvas, getWidth(), getHeight());
            return;
        }

        float width = getWidth();
        float height = getHeight();

        if (width <= 0 || height <= 0) {
            Log.d("ECGView", "Invalid dimensions: " + width + "x" + height);
            return;
        }

        float centerY = height / 2;

        Log.d("ECGView", "Drawing waveform, points: " + dataPoints.size() + ", size: " + width + "x" + height);

        // 绘制网格
        drawGrid(canvas, width, height);

        // 绘制波形
        path.reset();
        float xStep = width / (float) (maxDataPoints - 1);

        for (int i = 0; i < dataPoints.size(); i++) {
            float x = i * xStep;
            float y = centerY - dataPoints.get(i) * height * 0.3f;

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        canvas.drawPath(path, paint);
        Log.d("ECGView", "Waveform drawing completed");
    }

    private void drawGrid(Canvas canvas, float width, float height) {
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStrokeWidth(1f);

        // 水平网格
        for (int i = 1; i < 5; i++) {
            float y = i * height / 5;
            canvas.drawLine(0, y, width, y, gridPaint);
        }

        // 垂直网格（时间轴）
        for (int i = 1; i < 10; i++) {
            float x = i * width / 10;
            canvas.drawLine(x, 0, x, height, gridPaint);
        }

        // 零位线
        gridPaint.setColor(Color.parseColor("#BDBDBD"));
        gridPaint.setStrokeWidth(2f);
        canvas.drawLine(0, height / 2, width, height / 2, gridPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = dpToPx(200); // 建议高度
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);

        Log.d("ECGView", "onMeasure - width: " + width + ", height: " + height);

        setMeasuredDimension(width, height);
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
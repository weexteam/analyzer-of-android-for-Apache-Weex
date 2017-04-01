package com.taobao.weex.analyzer.core;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.IPermissionHandler;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/8<br/>
 * Time: 上午11:21<br/>
 */

public class ShakeDetector implements SensorEventListener,IPermissionHandler{

    private static final int MAX_SAMPLES = 25;
    private static final int MIN_TIME_BETWEEN_SAMPLES_MS = 20;
    private static final int VISIBLE_TIME_RANGE_MS = 500;
    private static final int MAGNITUDE_THRESHOLD = 25;
    private static final int PERCENT_OVER_THRESHOLD_FOR_SHAKE = 66;

    private ShakeListener mShakeListener;

    @Nullable
    private SensorManager mSensorManager;
    private long mLastTimestamp;
    private int mCurrentIndex;
    @Nullable
    private double[] mMagnitudes;
    @Nullable
    private long[] mTimestamps;

    private Config mConfig;


    public ShakeDetector(@Nullable ShakeListener listener,@Nullable Config config) {
        mShakeListener = listener;
        this.mConfig = config;
    }


    public void start(@Nullable SensorManager manager) {
        if (manager == null || (mConfig != null && !isPermissionGranted(mConfig))) {
            return;
        }
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            this.mSensorManager = manager;
            mLastTimestamp = -1;
            mCurrentIndex = 0;
            mMagnitudes = new double[MAX_SAMPLES];
            mTimestamps = new long[MAX_SAMPLES];

            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }

    }

    public void stop() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mTimestamps == null || mMagnitudes == null) {
            return;
        }
        if (event.timestamp - mLastTimestamp < MIN_TIME_BETWEEN_SAMPLES_MS) {
            return;
        }

        float ax = event.values[0];
        float ay = event.values[1];
        float az = event.values[2];

        mLastTimestamp = event.timestamp;
        mTimestamps[mCurrentIndex] = event.timestamp;
        mMagnitudes[mCurrentIndex] = Math.sqrt(ax * ax + ay * ay + az * az);

        maybeDispatchShake(event.timestamp);

        mCurrentIndex = (mCurrentIndex + 1) % MAX_SAMPLES;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void maybeDispatchShake(long currentTimestamp) {
        if (mTimestamps == null || mMagnitudes == null) {
            return;
        }

        int numOverThreshold = 0;
        int total = 0;
        for (int i = 0; i < MAX_SAMPLES; i++) {
            int index = (mCurrentIndex - i + MAX_SAMPLES) % MAX_SAMPLES;
            if (currentTimestamp - mTimestamps[index] < VISIBLE_TIME_RANGE_MS) {
                total++;
                if (mMagnitudes[index] >= MAGNITUDE_THRESHOLD) {
                    numOverThreshold++;
                }
            }
        }

        if (((double) numOverThreshold) / total > PERCENT_OVER_THRESHOLD_FOR_SHAKE / 100.0 && mShakeListener != null) {
            mShakeListener.onShake();
        }
    }

    @Override
    public boolean isPermissionGranted(@NonNull Config config) {
        return config.isEnableShake();
    }

    public interface ShakeListener {
        void onShake();
    }
}














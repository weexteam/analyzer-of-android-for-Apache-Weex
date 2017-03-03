package com.taobao.weex.analyzer.core;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
public class ShakeDetectorTest {

    @Mock
    private SensorManager mSensorManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void start() throws Exception {

        ShakeDetector.ShakeListener mockListener = mock(ShakeDetector.ShakeListener.class);
        Sensor sensor = mock(Sensor.class);
        ShakeDetector detector = new ShakeDetector(mockListener);
        when(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)).thenReturn(sensor);

        detector.start(mSensorManager);

        verify(mSensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        verify(mSensorManager).registerListener(detector,sensor,SensorManager.SENSOR_DELAY_UI);
    }

    @Test
    public void stop() throws Exception {

    }


}
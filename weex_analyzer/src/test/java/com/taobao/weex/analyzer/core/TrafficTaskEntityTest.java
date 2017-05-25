package com.taobao.weex.analyzer.core;

import android.os.Process;

import com.taobao.weex.analyzer.core.traffic.TrafficSampler;
import com.taobao.weex.analyzer.core.traffic.TrafficTaskEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Process.class,TrafficSampler.class})
public class TrafficTaskEntityTest {
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(TrafficSampler.class);
        PowerMockito.mockStatic(Process.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void onTaskInit() throws Exception {

    }

    @Test
    public void onTaskRun() throws Exception {
        TrafficTaskEntity entity = new TrafficTaskEntity(200);
        entity.onTaskRun();

        PowerMockito.verifyStatic(times(2));
        Process.myUid();
        PowerMockito.verifyStatic();
        TrafficSampler.getUidRxBytes(anyInt());
        PowerMockito.verifyStatic();
        TrafficSampler.getUidTxBytes(anyInt());
    }

    @Test
    public void onTaskStop() throws Exception {

    }

}
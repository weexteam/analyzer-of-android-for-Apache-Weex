package com.taobao.weex.analyzer.core;

import android.net.TrafficStats;

import com.taobao.weex.analyzer.core.traffic.TrafficSampler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TrafficStats.class})
public class TrafficSamplerTest {
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(TrafficStats.class);
        Mockito.when(TrafficStats.getUidRxBytes(Matchers.anyInt()))
                .thenReturn(100L);

        Mockito.when(TrafficStats.getUidTxBytes(Matchers.anyInt()))
                .thenReturn(101L);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getUidRxBytes() throws Exception {
        double val = TrafficSampler.getUidRxBytes(1);
        assertEquals(val,100,0);

        PowerMockito.verifyStatic(times(2));
        TrafficStats.getUidRxBytes(1);
    }

    @Test
    public void getUidTxBytes() throws Exception {
        double val = TrafficSampler.getUidTxBytes(2);
        assertEquals(val,101,0);

        PowerMockito.verifyStatic(times(2));
        TrafficStats.getUidTxBytes(2);
    }

}
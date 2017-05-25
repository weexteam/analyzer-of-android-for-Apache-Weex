package com.taobao.weex.analyzer.core;

import com.taobao.weex.analyzer.core.memory.MemorySampler;
import com.taobao.weex.analyzer.core.memory.MemoryTaskEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(MemorySampler.class)
public class MemoryTaskEntityTest {

    private MemoryTaskEntity entity;
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(MemorySampler.class);
        Mockito.when(MemorySampler.getMemoryUsage()).thenReturn(1.5);
        entity = new MemoryTaskEntity();
    }

    @After
    public void tearDown() throws Exception {
        entity = null;
    }

    @Test
    public void onTaskRun() throws Exception {
        double result = entity.onTaskRun();
        assertEquals(result,1.5,0);
    }

}
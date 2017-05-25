package com.taobao.weex.analyzer.core;

import android.text.TextUtils;

import com.taobao.weex.analyzer.core.cpu.CpuSampler;
import com.taobao.weex.analyzer.core.cpu.CpuTaskEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({CpuSampler.class,TextUtils.class})
public class CpuTaskEntityTest {

    private static final String FAKE_CPU_RATE = "cpu  1317 473 3808 48467 308 1 4 0 0 0";
    private static final String FAKE_PID_CPU_RATE = "2815 (com.alibaba.weex) S 1163 1163 0 0 -1 1077961024 20146 367 0 0 273 214 0 3 20 0 22 0 4908 788004864 13729 4294967295 3078434816 3078440316 3218548544 3218546108 3077705499 0 4612 0 38136 4294967295 0 0 17 3 0 0 0 0 0 3078446592 3078447088 3093508096 3218549823 3218549899 3218549899 3218550756 0";

    private CpuTaskEntity mTestEntity;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(CpuSampler.class);
        Mockito.when(CpuSampler.sampleCpuRate()).thenReturn(FAKE_CPU_RATE);
        Mockito.when(CpuSampler.samplePidCpuRate()).thenReturn(FAKE_PID_CPU_RATE);

        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                System.out.println(a);
                return !(a != null && a.length() > 0);
            }
        });


        mTestEntity = new CpuTaskEntity();
    }

    @After
    public void tearDown() throws Exception {
        mTestEntity = null;
    }

    @Test
    public void onTaskInit() throws Exception {
    }

    @Test
    public void onTaskRun() throws Exception {
        CpuTaskEntity.CpuInfo cpuInfo = mTestEntity.onTaskRun();
        assertNotNull(cpuInfo);

        double pidCpuUsage = cpuInfo.pidCpuUsage;
        double pidUserCpuUsage = cpuInfo.pidUserCpuUsage;
        double pidKernelCpuUsage = cpuInfo.pidKernelCpuUsage;

        assertEquals(0,pidCpuUsage,0);
        assertEquals(0,pidUserCpuUsage,0);
        assertEquals(0,pidKernelCpuUsage,0);
    }

    @Test
    public void onTaskStop() throws Exception {
    }

}
package com.taobao.weex.analyzer.core;

import android.text.TextUtils;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.core.storage.WXPerfStorage;
import com.taobao.weex.analyzer.core.weex.Performance;
import com.taobao.weex.analyzer.core.weex.PerformanceMonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static com.taobao.weex.analyzer.core.weex.PerformanceMonitor.monitor;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PerformanceMonitor.class,TextUtils.class})
public class WXPerfStorageTest {

    private WXPerfStorage mWxPerfStorage;

    @Mock
    private WXSDKInstance mFakeInstance;

    @Before
    public void setUp() throws Exception {
        mWxPerfStorage = WXPerfStorage.getInstance();
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        mWxPerfStorage = null;
    }

    @Test
    public void savePerformance() throws Exception {
        Performance p = new Performance();
        p.templateUrl = "fake_url";

        PowerMockito.mockStatic(PerformanceMonitor.class);
        Mockito.when(monitor(mFakeInstance)).thenReturn(p);

        String pageName = mWxPerfStorage.savePerformance(mFakeInstance);

        PowerMockito.verifyStatic();
        PerformanceMonitor.monitor(mFakeInstance);

        assertEquals("fake_url",pageName);
    }

    @Test
    public void getLatestPerformance() throws Exception {
        WXPerfStorage storage = PowerMockito.spy(WXPerfStorage.getInstance());

        List<Performance> fakeList = new ArrayList<>();
        Performance p1 = new Performance();
        p1.templateUrl = "fake1";
        Performance p2 = new Performance();
        p1.templateUrl = "fake1";
        Performance p3 = new Performance();
        p1.templateUrl = "fake1";
        fakeList.add(p1);
        fakeList.add(p2);
        fakeList.add(p3);

        PowerMockito.doReturn(fakeList).when(storage,"getPerformanceList","fake1");

        Performance p = storage.getLatestPerformance("fake1");
        assertEquals(p,p3);
    }

    @Test
    public void getPerformanceList() throws Exception {

    }

}
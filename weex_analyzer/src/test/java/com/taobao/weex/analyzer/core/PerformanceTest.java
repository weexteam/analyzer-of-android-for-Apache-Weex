package com.taobao.weex.analyzer.core;

import com.taobao.weex.analyzer.core.weex.Performance;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
public class PerformanceTest {

    private Performance mFakePerformance;

    @Before
    public void setUp() throws Exception {
        mFakePerformance = new Performance();
        mFakePerformance.requestType = "fakeType";
        mFakePerformance.pageName = "fakeUrl";
    }

    @After
    public void tearDown() throws Exception {
        mFakePerformance = null;
    }

    @Test
    public void transfer() throws Exception {
        List<String> fakeList = Performance.transfer(mFakePerformance);
        assertEquals(22,fakeList.size());
        assertThat(fakeList,hasItem("requestType : fakeType"));
    }

}
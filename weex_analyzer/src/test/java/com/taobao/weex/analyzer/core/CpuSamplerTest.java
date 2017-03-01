package com.taobao.weex.analyzer.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
public class CpuSamplerTest {


    @Before
    public void setup() {
    }

    @After
    public void tearDown() {
    }


    @Test
    public void doSample() throws Exception {
        String fakeStatFile = this.getClass().getResource("/cpu/stat").getFile();
        String rawCpuRate = CpuSampler.doSample(fakeStatFile);
        assertThat(rawCpuRate,is("cpu  1317 473 3808 48467 308 1 4 0 0 0"));
    }

}
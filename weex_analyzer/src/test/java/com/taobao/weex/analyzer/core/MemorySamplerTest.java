package com.taobao.weex.analyzer.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class MemorySamplerTest {


    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getMemoryUsage() throws Exception {
        double d = MemorySampler.getMemoryUsage();
        System.out.println("usage:"+d);
        assertTrue(d>=0);
    }

    @Test
    public void maxMemory() throws Exception {
        double d = MemorySampler.maxMemory();
        System.out.println("max:"+d);
        assertTrue(d>=0);
    }

    @Test
    public void totalMemory() throws Exception {
        double d = MemorySampler.totalMemory();
        System.out.println("total:"+d);
        assertTrue(d>=0);
    }

}
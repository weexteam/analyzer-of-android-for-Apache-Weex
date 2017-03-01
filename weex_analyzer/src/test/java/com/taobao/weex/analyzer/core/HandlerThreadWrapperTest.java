package com.taobao.weex.analyzer.core;

import android.os.Handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class HandlerThreadWrapperTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getHandler() throws Exception {
        HandlerThreadWrapper mHandlerThreadWrapper = new HandlerThreadWrapper("test");
        Handler handler = mHandlerThreadWrapper.getHandler();
        assertNotNull(handler);
    }

    @Test
    public void isAlive() throws Exception {
        HandlerThreadWrapper mHandlerThreadWrapper = new HandlerThreadWrapper("test");
        boolean result = mHandlerThreadWrapper.isAlive();
        assertTrue(result);

        mHandlerThreadWrapper.quit();
        //make sure thread has quited
        Thread.sleep(200);
        result = mHandlerThreadWrapper.isAlive();
        assertFalse(result);
    }

}
package com.taobao.weex.analyzer.core;

import android.os.Handler;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(RobolectricTestRunner.class)
public class LogcatDumperTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testFilter() {
        LogcatDumper dumper = new LogcatDumper(new LogcatDumper.OnLogReceivedListener() {
            @Override
            public void onReceived(@NonNull List<LogcatDumper.LogInfo> logList) {

            }
        });

        dumper.beginDump();
        dumper.findCachedLogByNewFilters();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotNull(dumper.getHandler());
        Handler handler = dumper.getHandler();
        assertTrue(handler.hasMessages(2));


    }

}
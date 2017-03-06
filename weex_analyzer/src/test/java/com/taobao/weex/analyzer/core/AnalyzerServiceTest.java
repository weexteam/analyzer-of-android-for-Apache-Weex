package com.taobao.weex.analyzer.core;

import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;

import static org.junit.Assert.assertEquals;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml",sdk = 21)
public class AnalyzerServiceTest {
    static {
        ShadowLog.stream = System.out;
    }
    @Test
    public void onStartCommand() throws Exception {
        Intent intent = new Intent(ShadowApplication.getInstance().getApplicationContext(), AnalyzerService.class);
        ShadowApplication.getInstance().startService(intent);

        Intent i = ShadowApplication.getInstance().peekNextStartedService();
        assertEquals(i.getComponent().getClassName(),AnalyzerService.class.getCanonicalName());

    }

}
package com.taobao.weex.analyzer.core;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.taobao.weex.analyzer.core.reporter.AnalyzerService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml",sdk = 21)
public class LaunchAnalyzerReceiverTest {
    private static final String ACTION = "com.taobao.weex.analyzer.LaunchService";

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testReceiverRegistered() {
        List<ShadowApplication.Wrapper> registeredList = ShadowApplication.getInstance().getRegisteredReceivers();
        assertFalse(registeredList.isEmpty());
        boolean found = false;
        for(ShadowApplication.Wrapper wrapper : registeredList) {
            if(wrapper.broadcastReceiver.getClass().getSimpleName().equals(LaunchAnalyzerReceiver.class.getSimpleName())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }


    @Test
    public void onReceive() throws Exception {

        //one
        ShadowApplication application = ShadowApplication.getInstance();
        Intent intent = new Intent(ACTION);
        boolean result = application.hasReceiverForIntent(intent);
        assertTrue(result);

        //only one
        List<BroadcastReceiver> receivers = application.getReceiversForIntent(intent);
        assertEquals(1,receivers.size());

        //test onReceive
        intent.putExtra("c","off");
        BroadcastReceiver targetReceiver = receivers.get(0);
        targetReceiver.onReceive(application.getApplicationContext(),intent);

        Intent serviceIntent = application.getNextStoppedService();
        assertEquals(serviceIntent.getComponent().getClassName(),AnalyzerService.class.getCanonicalName());
    }

}
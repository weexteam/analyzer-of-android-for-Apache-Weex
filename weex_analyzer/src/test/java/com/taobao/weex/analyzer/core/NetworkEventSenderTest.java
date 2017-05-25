package com.taobao.weex.analyzer.core;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.taobao.weex.analyzer.core.inspector.network.NetworkEventSender;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.ShadowLocalBroadcastManager;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.shadows.support.v4.Shadows.shadowOf;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml",sdk = 21)
public class NetworkEventSenderTest {

    private LocalBroadcastManager manager;

    @Before
    public void setUp() throws Exception {
        manager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void sendMessage() throws Exception {
        assertNotNull(manager);
        ShadowLocalBroadcastManager shadowBroadcastManager = shadowOf(manager);

        NetworkEventSender sender = new NetworkEventSender(manager);
        String type = "fakeType";
        String desc = "fakeDesc";
        String title = "fakeTitle";
        String body = "fakeBody";
        Map<String,String> extendProps = new HashMap<>();

        assertEquals(0,shadowBroadcastManager.getSentBroadcastIntents().size());

        sender.sendMessage(type,title,desc,body,extendProps);

        assertEquals(1,shadowBroadcastManager.getSentBroadcastIntents().size());

        Intent intent = shadowBroadcastManager.getSentBroadcastIntents().get(0);
        assertNotNull(intent);

        assertEquals(NetworkEventSender.ACTION_NETWORK_REPORTER,intent.getAction());

        assertEquals(type,intent.getStringExtra("type"));
        assertEquals(desc,intent.getStringExtra("desc"));
        assertEquals(title,intent.getStringExtra("title"));
        assertEquals(body,intent.getStringExtra("body"));
    }

}
package com.taobao.weex.analyzer.core;

import android.support.v4.content.LocalBroadcastManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.support.v4.ShadowLocalBroadcastManager;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.shadows.support.v4.Shadows.shadowOf;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(RobolectricTestRunner.class)
public class NetworkEventInspectorTest {
    private LocalBroadcastManager manager;

    @Before
    public void setUp() throws Exception {
        manager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void createInstance() throws Exception {
        assertNotNull(manager);
        ShadowLocalBroadcastManager shadowLocalBroadcastManager = shadowOf(manager);

        assertEquals(0,shadowLocalBroadcastManager.getRegisteredBroadcastReceivers().size());

        final NetworkEventInspector.MessageBean[] beanArr = new NetworkEventInspector.MessageBean[1];
        NetworkEventInspector.createInstance(manager, new NetworkEventInspector.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(NetworkEventInspector.MessageBean msg) {
                beanArr[0] = msg;
            }
        });

        assertEquals(1,shadowLocalBroadcastManager.getRegisteredBroadcastReceivers().size());
        ShadowLocalBroadcastManager.Wrapper wrapper = shadowLocalBroadcastManager.getRegisteredBroadcastReceivers().get(0);
        assertNotNull(wrapper);
        assertEquals(NetworkEventSender.ACTION_NETWORK_REPORTER,wrapper.intentFilter.getAction(0));

        //test listener

        NetworkEventSender sender = new NetworkEventSender(RuntimeEnvironment.application);
        sender.sendMessage("fakeType","fakeTitle","fakeDesc","fakeBody",new HashMap<String, String>());

        assertNotNull(beanArr[0]);
        assertEquals("fakeType",beanArr[0].type);
        assertEquals("fakeTitle",beanArr[0].title);
        assertEquals("fakeDesc",beanArr[0].desc);
        assertEquals("fakeBody",beanArr[0].body);
        assertEquals(0,beanArr[0].extendProps.size());
    }

    @Test
    public void destroy() throws Exception {
        assertNotNull(manager);
        ShadowLocalBroadcastManager shadowLocalBroadcastManager = shadowOf(manager);

        NetworkEventInspector inspector = NetworkEventInspector.createInstance(manager, new NetworkEventInspector.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(NetworkEventInspector.MessageBean msg) {
            }
        });

        assertEquals(1,shadowLocalBroadcastManager.getRegisteredBroadcastReceivers().size());
        inspector.destroy();
        assertEquals(0,shadowLocalBroadcastManager.getRegisteredBroadcastReceivers().size());

    }

}
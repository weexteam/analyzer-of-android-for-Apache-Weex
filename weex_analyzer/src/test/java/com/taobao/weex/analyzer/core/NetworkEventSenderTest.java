package com.taobao.weex.analyzer.core;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({LocalBroadcastManager.class,TextUtils.class})
public class NetworkEventSenderTest {

    private LocalBroadcastManager manager;

    @Before
    public void setUp() throws Exception {
        manager = PowerMockito.mock(LocalBroadcastManager.class);
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                System.out.println(a);
                return !(a != null && a.length() > 0);
            }
        });
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void sendMessage() throws Exception {

        Intent intent = Mockito.mock(Intent.class);

        PowerMockito.doReturn(true).when(manager).sendBroadcast(intent);

        NetworkEventSender sender = new NetworkEventSender(manager);
        String type = "";
        String desc = "";
        String title = "";
        String body = "";
        Map<String,String> extendProps = new HashMap<>();
        sender.sendMessage(type,title,desc,body,extendProps);
//        Mockito.verify(manager).sendBroadcast(intent);
    }

}
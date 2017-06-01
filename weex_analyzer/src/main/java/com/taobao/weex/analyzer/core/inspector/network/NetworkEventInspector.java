package com.taobao.weex.analyzer.core.inspector.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.taobao.weex.analyzer.core.NetworkEventSender;

import java.util.HashMap;
import java.util.Map;


/**
 * Description:
 *
 * 负责接受网络消息 并整理后发送给NetworkInspectorView
 *
 *
 * 1. 注册localbroadcastManager
 * 2. 接受消息
 * 3. 整理
 * 4. 转发
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class NetworkEventInspector {

    private LocalBroadcastManager mLocalBroadcastManager;
    private OnMessageReceivedListener mListener;
    private CoreMessageReceiver mCoreMessageReceiver;

    private NetworkEventInspector(@NonNull Context context) {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    private NetworkEventInspector(@NonNull LocalBroadcastManager manager) {
        mLocalBroadcastManager = manager;
    }

    private void setOnMessageReceivedListener(@NonNull OnMessageReceivedListener listener) {
        this.mListener = listener;
        mCoreMessageReceiver = new CoreMessageReceiver(mListener);
        IntentFilter filter = new IntentFilter(NetworkEventSender.ACTION_NETWORK_REPORTER);
        mLocalBroadcastManager.registerReceiver(mCoreMessageReceiver, filter);
    }

    public static NetworkEventInspector createInstance(@NonNull Context context, @NonNull OnMessageReceivedListener listener) {
        NetworkEventInspector reporter = new NetworkEventInspector(context);
        reporter.setOnMessageReceivedListener(listener);
        return reporter;
    }

    @VisibleForTesting
    @NonNull
    static NetworkEventInspector createInstance(@NonNull LocalBroadcastManager manager, @NonNull OnMessageReceivedListener listener) {
        NetworkEventInspector reporter = new NetworkEventInspector(manager);
        reporter.setOnMessageReceivedListener(listener);
        return reporter;
    }

    public void destroy() {
        if (mCoreMessageReceiver != null && mLocalBroadcastManager != null) {
            mLocalBroadcastManager.unregisterReceiver(mCoreMessageReceiver);
            mCoreMessageReceiver = null;
            mLocalBroadcastManager = null;
        }
        mListener = null;
    }

    static class CoreMessageReceiver extends BroadcastReceiver {
        OnMessageReceivedListener listener;

        CoreMessageReceiver(@NonNull OnMessageReceivedListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !intent.getAction().equals(NetworkEventSender.ACTION_NETWORK_REPORTER)) {
                return;
            }

            String type = intent.getStringExtra(NetworkEventSender.INTENT_EXTRA_TYPE);

            String title = intent.getStringExtra(NetworkEventSender.INTENT_EXTRA_TITLE);
            String desc = intent.getStringExtra(NetworkEventSender.INTENT_EXTRA_DESC);
            String body = intent.getStringExtra(NetworkEventSender.INTENT_EXTRA_BODY);
            Bundle bundle = intent.getExtras();
            Map<String, String> extendProps = null;
            if (bundle != null) {
                extendProps = new HashMap<>();
                for (String key : bundle.keySet()) {
                    if (NetworkEventSender.INTENT_EXTRA_TYPE.equals(key) ||
                            NetworkEventSender.INTENT_EXTRA_DESC.equals(key) || NetworkEventSender.INTENT_EXTRA_TITLE.equals(key)
                            || NetworkEventSender.INTENT_EXTRA_BODY.equals(key)) {
                        continue;
                    }
                    extendProps.put(key, bundle.getString(key));
                }
            }
            MessageBean msg = new MessageBean(type, title, desc, extendProps, body);

            try {
                if(!TextUtils.isEmpty(msg.body)) {
                    msg.content = JSON.parseObject(msg.body.trim());
                }
            } catch (Exception e) {
            }

            if (listener != null) {
                listener.onMessageReceived(msg);
            }
        }
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(MessageBean msg);
    }

    public static class MessageBean {
        public String title;
        public String desc;
        public String type;

        @JSONField(serialize = false)
        public String body;

        public JSONObject content;

        @JSONField(serialize = false)
        public Map<String, String> extendProps;


        public MessageBean(String type, String title, String desc, Map<String, String> extendProps, String body) {
            this.type = type;
            this.title = title;
            this.desc = desc;
            this.extendProps = extendProps;
            this.body = body;
        }
    }
}

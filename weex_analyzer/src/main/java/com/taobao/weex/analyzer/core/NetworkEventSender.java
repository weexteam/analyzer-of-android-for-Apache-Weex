package com.taobao.weex.analyzer.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.util.Map;

/**
 * Description:
 * 用于发送消息
 *
 *  DO NOT MOVE ME
 * Created by rowandjj(chuyi)<br/>
 */

@SuppressWarnings("unused")
public class NetworkEventSender {

    private LocalBroadcastManager mSender;

    public NetworkEventSender(Context context) {
        mSender = LocalBroadcastManager.getInstance(context);
    }

    @VisibleForTesting
    NetworkEventSender(LocalBroadcastManager manager) {
        this.mSender = manager;
    }

    public void sendMessage(String type,String title,String desc,String body,Map<String,String> extendProps) {
        if(mSender == null) {
            return;
        }

        Intent intent = new Intent(ACTION_NETWORK_REPORTER);
        if(!TextUtils.isEmpty(type)) {
            intent.putExtra(INTENT_EXTRA_TYPE,type);
        }

        if(!TextUtils.isEmpty(title)) {
            intent.putExtra(INTENT_EXTRA_TITLE,title);
        }

        if(!TextUtils.isEmpty(desc)) {
            intent.putExtra(INTENT_EXTRA_DESC,desc);
        }

        if(!TextUtils.isEmpty(body)) {
            intent.putExtra(INTENT_EXTRA_BODY,body);
        }

        if(extendProps != null && !extendProps.isEmpty()) {
            Bundle bundle = new Bundle();

            for(Map.Entry<String,String> entry : extendProps.entrySet()) {
                bundle.putString(entry.getKey(),entry.getValue());
            }
            intent.putExtras(bundle);
        }
        mSender.sendBroadcast(intent);
    }

    public static final String INTENT_EXTRA_TYPE = "type";
    public static final String INTENT_EXTRA_TITLE = "title";
    public static final String INTENT_EXTRA_DESC = "desc";
    public static final String INTENT_EXTRA_BODY = "body";

    public static final String ACTION_NETWORK_REPORTER = "action_network_reporter";

    public static final String TYPE_REQUEST = "request";
    public static final String TYPE_RESPONSE = "response";
}

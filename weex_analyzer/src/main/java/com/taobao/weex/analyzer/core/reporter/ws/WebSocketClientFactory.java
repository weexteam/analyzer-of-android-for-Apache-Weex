package com.taobao.weex.analyzer.core.reporter.ws;


import android.support.annotation.NonNull;

import com.taobao.weex.analyzer.utils.ReflectionUtil;


public class WebSocketClientFactory {
    public static WebSocketClient create(@NonNull IWebSocketBridge bridge) {
        if (ReflectionUtil.tryGetClassForName("okhttp3.ws.WebSocketListener") != null) {
            return new OkHttp3WebSocketClient(bridge);
        } else if (ReflectionUtil.tryGetClassForName("com.squareup.okhttp.ws.WebSocketListener") != null) {
            return new OkHttpWebSocketClient(bridge);
        }
        return null;
    }
}

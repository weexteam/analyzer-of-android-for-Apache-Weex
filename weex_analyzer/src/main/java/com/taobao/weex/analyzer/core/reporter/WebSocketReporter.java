package com.taobao.weex.analyzer.core.reporter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.taobao.weex.analyzer.core.reporter.ws.IWebSocketBridge;
import com.taobao.weex.analyzer.core.reporter.ws.WebSocketClient;
import com.taobao.weex.analyzer.core.reporter.ws.WebSocketClientFactory;
import com.taobao.weex.utils.WXLogUtils;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class WebSocketReporter implements IDataReporter,IWebSocketBridge,WebSocketClient.Callback {
    private boolean isEnabled;

    @Nullable
    private WebSocketClient mSocketClient = null;
    private static final String TAG = "WebSocketReporter";

    public WebSocketReporter(boolean enabled) {
        this.isEnabled = enabled;
        mSocketClient = WebSocketClientFactory.create(this);
    }

    @Override
    public void report(@NonNull ProcessedData data) {
        if(mSocketClient != null && mSocketClient.isOpen()) {
            mSocketClient.sendText(JSON.toJSONString(data));
        }
    }

    public void connect(String url) {
        if(mSocketClient != null) {
            mSocketClient.connect(url, this);
        }
    }

    public void close(int closeReason, String reasonPhrase) {
        if(mSocketClient != null && mSocketClient.isOpen()) {
            mSocketClient.close(closeReason,reasonPhrase);
        }
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void handleMessage(String message) {
        WXLogUtils.v(TAG,message);
    }

    @Override
    public void onOpen(String response) {

    }

    @Override
    public void onFailure(Throwable cause) {

    }

    @Override
    public void onClose(int code, String reason) {

    }
}

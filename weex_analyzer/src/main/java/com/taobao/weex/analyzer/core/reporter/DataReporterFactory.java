package com.taobao.weex.analyzer.core.reporter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.taobao.weex.analyzer.core.Constants;
import com.taobao.weex.analyzer.core.reporter.ws.IWebSocketBridge;
import com.taobao.weex.analyzer.core.reporter.ws.WebSocketClient;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

class DataReporterFactory {
    private DataReporterFactory(){}

    private static final String MDS = "mds";


    static IDataReporter<String> createLogReporter(boolean enabled) {
        return new LogReporter(enabled);
    }

    static IDataReporter createWSReporter(@NonNull String from, @NonNull String deviceId,@NonNull String server,WebSocketClient.Callback callback, IWebSocketBridge bridge) {
        WebSocketReporter reporter= new WebSocketReporter(MDS.equals(from), bridge);
        reporter.connect(server,callback);
        Log.d(Constants.TAG, "try connect server:"+server);
        return reporter;
    }
}

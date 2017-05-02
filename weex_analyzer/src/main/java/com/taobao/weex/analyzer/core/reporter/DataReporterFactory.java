package com.taobao.weex.analyzer.core.reporter;

import android.support.annotation.NonNull;

import com.taobao.weex.analyzer.core.reporter.ws.IWebSocketBridge;
import com.taobao.weex.analyzer.core.reporter.ws.WebSocketClient;

import java.util.Locale;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class DataReporterFactory {
    private DataReporterFactory(){}

    private static final String REQUEST_URL_PRE = "http://pre.mds.alibaba-inc.com/api/debug/weexAnalyzer/%s/logs";
    private static final String REQUEST_URL_ONLINE = "http://mds.alibaba-inc.com/api/debug/weexAnalyzer/%s/logs";

    private static final String WS_SERVER = "ws://mds.alibaba-inc.com/api/device/%s/weexAnaylzerws";

    private static final String MDS = "mds";

    @NonNull
    public static <T> IDataReporter<T> createHttpReporter(@NonNull String from, @NonNull String deviceId) {
        return new HttpDataReporter<>(String.format(Locale.CHINA,REQUEST_URL_ONLINE,deviceId),MDS.equals(from));
    }

    public static IDataReporter<String> createLogReporter(boolean enabled) {
        return new LogReporter(enabled);
    }

    public static IDataReporter createWSReporter(@NonNull String from, @NonNull String deviceId, WebSocketClient.Callback callback, IWebSocketBridge bridge) {
        WebSocketReporter reporter= new WebSocketReporter(MDS.equals(from), bridge);
        reporter.connect(String.format(Locale.CHINA,WS_SERVER,deviceId),callback);
        return reporter;
    }
}

package com.taobao.weex.analyzer.core.reporter;

import android.support.annotation.NonNull;

import com.taobao.weex.utils.WXLogUtils;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

class HttpDataReporter<T> implements IDataReporter<T> {

    private String mRequestUrl;
    private String mDeviceId;
    private boolean isEnabled;

    private static final String TAG = "HttpDataReporter";

    HttpDataReporter(String deviceId,String requestUrl,boolean isEnabled) {
        this.mDeviceId = deviceId;
        this.mRequestUrl = requestUrl;
        this.isEnabled = isEnabled;
    }

    @Override
    public void report(@NonNull ProcessedData<T> data) {
        HttpEngine.Request request = new HttpEngine.Request();
        request.method = "POST";
        request.url = mRequestUrl;
        request.rawData = data;

        HttpEngine.asyncRequest(request, new HttpEngine.OnHttpCompletedListener() {
            @Override
            public void onHttpComplete(HttpEngine.Response response) {
                WXLogUtils.e(TAG,response.toString());
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}

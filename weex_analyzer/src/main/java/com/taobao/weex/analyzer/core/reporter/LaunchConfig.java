package com.taobao.weex.analyzer.core.reporter;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class LaunchConfig {
    private static String mFrom;
    private static String mDeviceId;

    public static void setFrom(String from) {
        mFrom = from;
    }

    public static void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }

    public static String getFrom() {
        //TODO
        mFrom = "mds";

        return mFrom;
    }

    public static String getDeviceId() {
        //TODO
        mDeviceId = "xk28sd";

        return mDeviceId;
    }
}

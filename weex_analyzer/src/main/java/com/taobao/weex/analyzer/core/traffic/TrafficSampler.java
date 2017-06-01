package com.taobao.weex.analyzer.core.traffic;

import android.net.TrafficStats;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)
 */

public class TrafficSampler {

    public static double getUidRxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : TrafficStats.getUidRxBytes(uid);
    }


    public static double getUidTxBytes(int uid) {
        return TrafficStats.getUidTxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : TrafficStats.getUidTxBytes(uid);
    }

}

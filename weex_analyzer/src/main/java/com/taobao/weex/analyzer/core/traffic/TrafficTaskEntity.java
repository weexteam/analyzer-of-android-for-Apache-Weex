package com.taobao.weex.analyzer.core.traffic;

import android.os.Process;
import android.support.annotation.NonNull;

import com.taobao.weex.analyzer.core.TaskEntity;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class TrafficTaskEntity implements TaskEntity<TrafficTaskEntity.TrafficInfo> {

    private double mTotalRxKBytes = 0;
    private double mTotalTxKBytes = 0;
    private TrafficInfo mCachedTrafficInfo;

    private int mDelayInMillis = 1000;

    public TrafficTaskEntity(int delayInMillis) {
        this.mDelayInMillis = delayInMillis;
    }

    @Override
    public void onTaskInit() {
        mCachedTrafficInfo = new TrafficInfo();
    }

    @Override
    @NonNull
    public TrafficInfo onTaskRun() {
        double txKBytes = TrafficSampler.getUidTxBytes(Process.myUid()) / 1024;
        double rxKBytes = TrafficSampler.getUidRxBytes(Process.myUid()) / 1024;

        final double txSpeed;
        final double rxSpeed;

        int ratio = mDelayInMillis / 1000;
        if (mTotalTxKBytes == 0 && mTotalRxKBytes == 0) {
            txSpeed = 0;
            rxSpeed = 0;
        } else {
            txSpeed = Math.max(0, (txKBytes - mTotalTxKBytes) / ratio);
            rxSpeed = Math.max(0, (rxKBytes - mTotalRxKBytes) / ratio);
        }
        if(mCachedTrafficInfo == null) {
            mCachedTrafficInfo = new TrafficInfo();
        }
        mCachedTrafficInfo.rxSpeed = (Math.round(rxSpeed*100)/100.0);
        mCachedTrafficInfo.txSpeed = (Math.round(txSpeed*100)/100.0);

        //update
        mTotalRxKBytes = rxKBytes;
        mTotalTxKBytes = txKBytes;
        return mCachedTrafficInfo;
    }

    @Override
    public void onTaskStop() {
        mTotalRxKBytes = 0;
        mTotalTxKBytes = 0;
        mCachedTrafficInfo = null;
    }

    public static class TrafficInfo {
        public double txSpeed;
        public double rxSpeed;
    }
}

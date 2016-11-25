package com.taobao.weex.analyzer.core;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class CpuTaskEntity implements TaskEntity<CpuTaskEntity.CpuInfo> {

    private long mTotalCpuTimeLast = 0;
    private long mPidTotalCpuTimeLast = 0;
    private long mPidUserCpuTimeLast = 0;
    private long mPidKernelCpuTimeLast = 0;

    private CpuInfo mCachedCpuInfo;

    @Override
    public void onTaskInit() {
        mTotalCpuTimeLast = 0L;
        mPidTotalCpuTimeLast = 0L;
        mPidUserCpuTimeLast = 0L;
        mPidKernelCpuTimeLast = 0L;

        mCachedCpuInfo = new CpuInfo();
    }

    @NonNull
    @Override
    public CpuInfo onTaskRun() {

        String pidCpuRate = CpuSampler.samplePidCpuRate();
        String totalCpuRate = CpuSampler.sampleCpuRate();

        if(mCachedCpuInfo == null) {
            mCachedCpuInfo = new CpuInfo();
        }

        if (TextUtils.isEmpty(pidCpuRate) || TextUtils.isEmpty(totalCpuRate)) {
            mCachedCpuInfo.pidCpuUsage = 0;
            mCachedCpuInfo.pidKernelCpuUsage = 0;
            mCachedCpuInfo.pidUserCpuUsage = 0;
            return mCachedCpuInfo;
        }

        String[] cpuInfoArray = totalCpuRate.split(" ");
        if (cpuInfoArray.length < 9) {
            mCachedCpuInfo.pidCpuUsage = 0;
            mCachedCpuInfo.pidKernelCpuUsage = 0;
            mCachedCpuInfo.pidUserCpuUsage = 0;
            return mCachedCpuInfo;
        }

        String[] pidCpuInfoList = pidCpuRate.split(" ");
        if (pidCpuInfoList.length < 17) {
            mCachedCpuInfo.pidCpuUsage = 0;
            mCachedCpuInfo.pidKernelCpuUsage = 0;
            mCachedCpuInfo.pidUserCpuUsage = 0;
            return mCachedCpuInfo;
        }

        long user = Long.parseLong(cpuInfoArray[2]);
        long nice = Long.parseLong(cpuInfoArray[3]);
        long system = Long.parseLong(cpuInfoArray[4]);
        long idle = Long.parseLong(cpuInfoArray[5]);
        long ioWait = Long.parseLong(cpuInfoArray[6]);
        long hardIrq = Long.parseLong(cpuInfoArray[7]);
        long softIrq = Long.parseLong(cpuInfoArray[8]);
        long stealTime = Long.parseLong(cpuInfoArray[9]);

        long pidUTime = Long.parseLong(pidCpuInfoList[13]);
        long pidSTime = Long.parseLong(pidCpuInfoList[14]);
        long pidCUTime = Long.parseLong(pidCpuInfoList[15]);
        long pidCSTime = Long.parseLong(pidCpuInfoList[16]);

        long cpuTime = user + nice + system + idle + ioWait + hardIrq + softIrq + stealTime;
        long pidCpuTime = pidUTime + pidSTime + pidCUTime + pidCSTime;

        final double pidCpuUsage;
        final double pidUserCpuUsage;
        final double pidKernelCpuUsage;
        if (mTotalCpuTimeLast != 0) {
            pidCpuUsage = (pidCpuTime - mPidTotalCpuTimeLast) * 100L /(double) (cpuTime - mTotalCpuTimeLast);
            pidUserCpuUsage = (pidUTime - mPidUserCpuTimeLast) * 100L /(double) (cpuTime - mTotalCpuTimeLast);
            pidKernelCpuUsage = (pidSTime - mPidKernelCpuTimeLast) * 100L /(double) (cpuTime - mTotalCpuTimeLast);
        }else{
            pidCpuUsage = 0L;
            pidUserCpuUsage = 0L;
            pidKernelCpuUsage = 0L;
        }

        mCachedCpuInfo.pidCpuUsage = pidCpuUsage;
        mCachedCpuInfo.pidUserCpuUsage = pidUserCpuUsage;
        mCachedCpuInfo.pidKernelCpuUsage = pidKernelCpuUsage;

        mTotalCpuTimeLast = cpuTime;
        mPidTotalCpuTimeLast = pidCpuTime;
        mPidUserCpuTimeLast = pidUTime;
        mPidKernelCpuTimeLast = pidSTime;

        return mCachedCpuInfo;
    }

    @Override
    public void onTaskStop() {
        mTotalCpuTimeLast = 0L;
        mPidTotalCpuTimeLast = 0L;
        mPidUserCpuTimeLast = 0L;
        mPidKernelCpuTimeLast = 0L;

        mCachedCpuInfo = null;
    }


    public static class CpuInfo {
        public double pidCpuUsage;
        public double pidUserCpuUsage;
        public double pidKernelCpuUsage;
    }
}

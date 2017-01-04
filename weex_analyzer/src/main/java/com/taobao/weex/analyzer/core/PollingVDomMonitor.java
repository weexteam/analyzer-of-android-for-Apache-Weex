package com.taobao.weex.analyzer.core;

import android.support.annotation.NonNull;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.pojo.HealthReport;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.utils.WXLogUtils;

import java.lang.ref.WeakReference;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

class PollingVDomMonitor implements IVDomMonitor{

    private PollingTask mTask;

    static boolean shouldStop;

    @Override
    public void monitor(@NonNull WXSDKInstance instance) {
        if(mTask != null) {
            mTask.stop();
        }
        mTask = new PollingTask(instance);
        mTask.start();
    }

    @Override
    public void destroy() {
        if(mTask != null) {
            mTask.stop();
        }
    }

    private static class PollingTask extends AbstractLoopTask {

        private WeakReference<WXSDKInstance> instanceRef;

        PollingTask(WXSDKInstance instance) {
            super(false,1500);
            this.instanceRef = new WeakReference<>(instance);
        }

        @Override
        protected void onRun() {
            if(shouldStop) {
                WXLogUtils.e(Constants.TAG,"polling service is destroyed");
                stop();
                return;
            }

            WXSDKInstance instance = instanceRef.get();
            if(instance == null) {
                WXLogUtils.e(Constants.TAG,"weex instance is destroyed");
                stop();
                return;
            }

            if(instance.getContext() != null && (
                    !SDKUtils.isHostRunning(instance.getContext()) || !SDKUtils.isInteractive(instance.getContext())
                    )) {
                WXLogUtils.e(Constants.TAG,"polling service is destroyed because we are in background or killed");
                stop();
                return;
            }

            try {
                VDomTracker tracker = new VDomTracker(instance);//todo 此处需优化
                HealthReport report = tracker.traverse();
                if(report != null) {
                    report.writeToConsole();
                }
            }catch (Exception e) {
                WXLogUtils.e(e.getMessage());
            }
        }

        @Override
        protected void onStop() {
        }
    }
}

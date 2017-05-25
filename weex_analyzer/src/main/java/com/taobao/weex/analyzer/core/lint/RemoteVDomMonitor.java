package com.taobao.weex.analyzer.core.lint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.core.Constants;
import com.taobao.weex.analyzer.core.reporter.AnalyzerService;
import com.taobao.weex.analyzer.pojo.HealthReport;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.utils.WXLogUtils;

import java.lang.ref.WeakReference;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class RemoteVDomMonitor implements IVDomMonitor {

    private PollingTask mTask;

    private InnerReceiver mInnerReceiver;

    private boolean shouldMonitor = false;

    public static final String ACTION_SHOULD_MONITOR = "action_should_monitor";
    public static final String EXTRA_MONITOR = "extra_monitor";
    private Context mContext;
    private WXSDKInstance mInstance;

    public RemoteVDomMonitor(@NonNull Context context) {
        this.mContext = context;
        mInnerReceiver = new InnerReceiver();
        IntentFilter filter = new IntentFilter(ACTION_SHOULD_MONITOR);
        LocalBroadcastManager.getInstance(context).registerReceiver(mInnerReceiver,filter);
    }

    @Override
    public void monitor(@NonNull WXSDKInstance instance) {
        this.mInstance = instance;
        tryStartTask();
    }

    private void tryStartTask() {
        WXLogUtils.d(Constants.TAG,"tryStartTask--->>>"+ shouldMonitor);

        if(!shouldMonitor) {
            return;
        }

        if(mTask != null) {
            mTask.stop();
        }
        mTask = new PollingTask(mInstance);
        mTask.start();
    }

    @Override
    public void destroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mInnerReceiver);
        if(mTask != null) {
            mTask.stop();
        }
    }

    private class InnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(!intent.getAction().equals(ACTION_SHOULD_MONITOR)) {
                return;
            }
            shouldMonitor = intent.getBooleanExtra(EXTRA_MONITOR,false);
            tryStartTask();
        }
    }

    private class PollingTask extends AbstractLoopTask {

        private WeakReference<WXSDKInstance> instanceRef;

        PollingTask(WXSDKInstance instance) {
            super(false,1500);
            this.instanceRef = new WeakReference<>(instance);
        }

        @Override
        protected void onRun() {

            if(!shouldMonitor) {
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
                DomTracker tracker = new DomTracker(instance);
                HealthReport report = tracker.traverse();
                if(report != null) {
                    String data = JSON.toJSONString(report);

                    Intent intent = new Intent(AnalyzerService.ACTION_DISPATCH);
                    intent.putExtra(Config.TYPE_RENDER_ANALYSIS, data);
                    intent.putExtra("type",Config.TYPE_RENDER_ANALYSIS);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            }catch (Exception e) {
                WXLogUtils.e(Constants.TAG,e.getMessage());
            }
        }

        @Override
        protected void onStop() {
        }
    }
}

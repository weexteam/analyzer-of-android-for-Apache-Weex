package com.taobao.weex.analyzer.core.lint;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.core.Constants;
import com.taobao.weex.analyzer.pojo.HealthReport;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.analyzer.view.highlight.MutipleViewHighlighter;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.utils.WXLogUtils;

import java.lang.ref.WeakReference;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class PollingVDomMonitor implements IVDomMonitor {

    private PollingTask mTask;

    //TODO we should remove it later
    public static boolean shouldStop;

    public static boolean shouldHighlight = false;

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

    private static class PollingTask extends AbstractLoopTask implements DomTracker.OnTrackNodeListener{

        private WeakReference<WXSDKInstance> instanceRef;
        MutipleViewHighlighter mViewHighlighter;

        static final int MAX_VDOM_LAYER = 14;

        PollingTask(WXSDKInstance instance) {
            super(false,1500);
            this.instanceRef = new WeakReference<>(instance);

            if(shouldHighlight) {
                mViewHighlighter = MutipleViewHighlighter.newInstance();
                mViewHighlighter.setColor(Color.parseColor("#420000ff"));
            }
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
                DomTracker tracker = new DomTracker(instance);//todo 此处需优化
                tracker.setOnTrackNodeListener(this);
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
            if(mViewHighlighter != null) {
                mViewHighlighter.clearHighlight();
            }
        }

        @Override
        public void onTrackNode(@NonNull WXComponent component, int layer) {
            if(layer < MAX_VDOM_LAYER) {
                return;
            }
            View hostView = component.getHostView();
            if(hostView == null) {
                return;
            }

            if(mViewHighlighter != null) {
                mViewHighlighter.addHighlightedView(hostView);
            }
        }
    }
}

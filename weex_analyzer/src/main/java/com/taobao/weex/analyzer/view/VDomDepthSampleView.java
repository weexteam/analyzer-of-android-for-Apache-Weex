package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.core.VDomTracker;
import com.taobao.weex.analyzer.pojo.HealthReport;


/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class VDomDepthSampleView extends DragSupportOverlayView{
    private SampleTask mTask;

    public VDomDepthSampleView(Context application) {
        super(application);
        mWidth = WindowManager.LayoutParams.MATCH_PARENT;
    }

    @NonNull
    @Override
    protected View onCreateView() {
        return View.inflate(mContext, R.layout.wxt_depth_sample_view, null);
    }

    @Override
    protected void onShown() {
        if(mTask != null){
            mTask.stop();
            mTask = null;
        }
        mTask = new SampleTask(mWholeView);
        mTask.start();
    }

    @Override
    protected void onDismiss() {
        if (mTask != null) {
            mTask.stop();
            mTask = null;
        }
    }

    public void bindInstance(WXSDKInstance instance) {
        if(mTask != null) {
            mTask.setInstance(instance);
        }
    }


    private static class SampleTask extends AbstractLoopTask {

        WXSDKInstance instance;
        TextView resultTextView;
        SampleTask(@NonNull View hostView) {
            super(false);
            mDelayMillis = 1000;
            resultTextView = (TextView) hostView.findViewById(R.id.result);
        }

        void setInstance(WXSDKInstance instance){
            this.instance = instance;
        }

        private String convertResult(boolean result) {
            return result ? "✓ " : "✕ ";
        }

        @Override
        protected void onRun() {
            if(instance == null) {
                return;
            }
            VDomTracker tracker = new VDomTracker(instance);
            HealthReport report = tracker.traverse();
            if(report == null) {
                return;
            }
            final StringBuilder builder = new StringBuilder();
            builder.append("weex-analyzer检测结果:\n");

            //////
            builder.append(convertResult(report.maxLayer < 14));
            builder.append("检测到VDOM最深嵌套层级为 ")
                    .append(report.maxLayer + 1)//1主要是为了和dev tool兼容
                    .append(",建议<14")
                    .append("\n");
            //////

            if(report.hasScroller) {
                builder.append(convertResult(true));
                builder.append("检测到该页面使用了Scroller,长列表建议使用ListView")
                        .append("\n");
            }

            //////
            if(report.hasList) {
                builder.append(convertResult(true));
                builder.append("检测到该页面使用了List,cell个数为")
                        .append(report.cellNum)
                        .append("\n");

                builder.append(convertResult(!report.hasBigCell));
                if(report.hasBigCell) {
                    builder.append("检测到页面可能存在大cell,最大的cell中包含")
                            .append(report.maxCellViewNum).append("个组件,建议按行合理拆分");
                }else {
                    builder.append("经检测，cell大小合理");
                }
            }


            runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    resultTextView.setText(builder.toString());
                }
            });
        }

        @Override
        protected void onStop() {
            instance = null;
        }
    }
}

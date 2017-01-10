package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
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
            builder.append("*weex-analyzer检测结果:\n");
            builder.append("*当前页面VDom最深层级为 ")
                    .append(report.maxLayer + 1)//1主要是为了和dev tool兼容
                    .append("\n");
            if(report.maxLayer + 1 >= 15) {
                builder.append("*层级过深，建议优化\n");
            }else {
                builder.append("*层级合理\n");
            }
            if(report.hasList) {
                builder.append("*使用了list组件,cell个数为")
                        .append(report.cellNum)
                        .append("\n");

                builder.append("*最大的cell包含了").append(report.maxCellViewNum).append("个组件\n");

                if(report.hasBigCell) {
                    builder.append("*可能存在大cell,对性能产生影响，请仔细检查\n");
                }
            }
            if(report.hasScroller) {
                builder.append("*使用了Scroller组件\n");
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

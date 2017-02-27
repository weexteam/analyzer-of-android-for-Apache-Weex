package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.core.VDomTracker;
import com.taobao.weex.analyzer.pojo.HealthReport;
import com.taobao.weex.analyzer.view.highlight.MutipleViewHighlighter;
import com.taobao.weex.ui.component.WXComponent;


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


    private static class SampleTask extends AbstractLoopTask implements VDomTracker.OnTrackNodeListener{

        WXSDKInstance instance;
        TextView resultTextView;
        MutipleViewHighlighter mViewHighlighter;

        static final int MAX_LAYER = 14;
        SampleTask(@NonNull View hostView) {
            super(false);
            mDelayMillis = 1000;
            resultTextView = (TextView) hostView.findViewById(R.id.result);
            mViewHighlighter = MutipleViewHighlighter.newInstance();
            mViewHighlighter.setColor(Color.parseColor("#420000ff"));
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
            tracker.setOnTrackNodeListener(this);
            HealthReport report = tracker.traverse();
            if(report == null) {
                return;
            }
            final StringBuilder builder = new StringBuilder();
            builder.append("weex-analyzer检测结果:\n");

            //////
            boolean deepLayer = report.maxLayer >= MAX_LAYER;
            builder.append(convertResult(!deepLayer));
            builder.append("检测到VDOM最深嵌套层级为 ")
                    .append(report.maxLayer)
                    .append(",建议<14");
            if(deepLayer && mViewHighlighter != null && mViewHighlighter.isSupport()) {
                builder.append(",深层嵌套已高亮透出");
            }
            builder.append("\n");
            //////

            if(report.hasScroller) {
                builder.append(convertResult(true));
                builder.append("检测到该页面使用了纵向的Scroller,长列表建议使用ListView")
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
                            .append(report.maxCellViewNum).append("个组件,建议按行合理拆分")
                            .append("\n");
                }else {
                    builder.append("经检测，cell大小合理")
                            .append("\n");
                }
            }

            //////
            if(report.hasEmbed) {
                int embedNum = report.embedDescList.size();

                builder.append(convertResult(true));
                builder.append("检测到该页面使用了Embed标签")
                        .append(",")
                        .append("该标签数目为")
                        .append(embedNum)
                        .append("\n");

                for(int i = 0 ; i < embedNum; i++) {
                    HealthReport.EmbedDesc desc = report.embedDescList.get(i);
                    boolean isEmbedDeep = desc.actualMaxLayer>=MAX_LAYER;
                    builder.append(convertResult(!isEmbedDeep))
                            .append("第")
                            .append(i+1)
                            .append("个embed标签地址为")
                            .append(desc.src)
                            .append(",内容最深嵌套层级为")
                            .append(desc.actualMaxLayer)
                            .append("\n");
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
            if(mViewHighlighter != null) {
                mViewHighlighter.clearHighlight();
            }
        }

        @Override
        public void onTrackNode(@NonNull WXComponent component, int layer) {
            if(layer < MAX_LAYER) {
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

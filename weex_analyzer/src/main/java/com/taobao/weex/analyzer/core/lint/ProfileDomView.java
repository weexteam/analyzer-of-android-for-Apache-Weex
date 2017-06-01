package com.taobao.weex.analyzer.core.lint;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.pojo.HealthReport;
import com.taobao.weex.analyzer.view.overlay.PermissionOverlayView;
import com.taobao.weex.analyzer.view.highlight.MutipleViewHighlighter;
import com.taobao.weex.ui.component.WXComponent;

import static com.taobao.weex.analyzer.R.id.close;


/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class ProfileDomView extends PermissionOverlayView {
    private SampleTask mTask;
    private OnCloseListener mOnCloseListener;

    public ProfileDomView(Context application,Config config) {
        super(application,true,config);
        mWidth = WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    public boolean isPermissionGranted(@NonNull Config config) {
        return !config.getIgnoreOptions().contains(Config.TYPE_RENDER_ANALYSIS);
    }

    @NonNull
    @Override
    protected View onCreateView() {
        View hostView = View.inflate(mContext, R.layout.wxt_depth_sample_view, null);
        View closeBtn = hostView.findViewById(close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isViewAttached && mOnCloseListener != null) {
                    mOnCloseListener.close(ProfileDomView.this);
                    dismiss();
                }
            }
        });

        return hostView;
    }

    public void setOnCloseListener(@Nullable OnCloseListener listener) {
        this.mOnCloseListener = listener;
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


    private static class SampleTask extends AbstractLoopTask implements DomTracker.OnTrackNodeListener{

        WXSDKInstance instance;
        TextView resultTextView;
        MutipleViewHighlighter mViewHighlighter;

        static final int MAX_VDOM_LAYER = 14;
        static final int MAX_REAL_DOM_LAYER = 20;

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
            DomTracker tracker = new DomTracker(instance);
            tracker.setOnTrackNodeListener(this);
            HealthReport report = tracker.traverse();
            if(report == null) {
                return;
            }
            final StringBuilder builder = new StringBuilder();

            //////
            builder.append(convertResult(report.maxLayerOfRealDom<MAX_REAL_DOM_LAYER))
                    .append("检测到native最深嵌套层级为 ")
                    .append(report.maxLayerOfRealDom)
                    .append("(仅统计weex自身渲染出来的层级)")
                    .append("\n");

            //////
            boolean deepLayer = report.maxLayer >= MAX_VDOM_LAYER;
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
            if(report.hasList && report.listDescMap != null) {
                int listNum = report.listDescMap.size();

                builder.append(convertResult(true));
                builder.append("检测到该页面使用了List,共")
                        .append(listNum)
                        .append("个")
                        .append("\n");

                for(HealthReport.ListDesc desc : report.listDescMap.values()) {
                    builder.append(convertResult(true))
                            .append("检测到ref为'")
                            .append(desc.ref)
                            .append("'的list,其cell个数为")
                            .append(desc.cellNum)
                            .append("\n");
//                            .append(",预估高度为")
//                            .append(desc.totalHeight)
//                            .append("px\n");
                }

                builder.append(convertResult(!report.hasBigCell));
                if(report.hasBigCell) {
                    builder.append("检测到页面可能存在大cell,最大的cell中包含")
                            .append(report.componentNumOfBigCell).append("个组件,建议按行合理拆分")
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
                    boolean isEmbedDeep = desc.actualMaxLayer>= MAX_VDOM_LAYER;
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

//            builder.append(convertResult(true))
//                    .append("检测到当前内容高度约为")
//                    .append(report.estimateContentHeight)
//                    .append("px,约等于")
//                    .append(report.estimatePages)
//                    .append("屏")
//                    .append("\n");


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

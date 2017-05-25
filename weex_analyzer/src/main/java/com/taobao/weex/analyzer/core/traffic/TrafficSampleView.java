package com.taobao.weex.analyzer.core.traffic;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.analyzer.view.chart.DynamicChartViewController;
import com.taobao.weex.analyzer.view.overlay.PermissionOverlayView;
import com.taobao.weex.analyzer.view.chart.ChartView;
import com.taobao.weex.analyzer.view.chart.LegendRenderer;
import com.taobao.weex.analyzer.view.chart.TimestampLabelFormatter;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class TrafficSampleView extends PermissionOverlayView {

    private SampleTrafficTask mSampleTrafficTask;

    private DynamicChartViewController mChartViewController;

    private OnCloseListener mOnCloseListener;

    public void setOnCloseListener(@Nullable OnCloseListener listener) {
        this.mOnCloseListener = listener;
    }

    public TrafficSampleView(Context application,Config config) {
        super(application,true,config);

        mWidth = WindowManager.LayoutParams.MATCH_PARENT;
        mHeight = (int) ViewUtils.dp2px(application, 150);
    }

    @Override
    public boolean isPermissionGranted(@NonNull Config config) {
        return !config.getIgnoreOptions().contains(Config.TYPE_TRAFFIC);
    }

    @NonNull
    @Override
    protected View onCreateView() {
        //prepare chart view
        int expectMaxTraffic = 64;
        mChartViewController = new DynamicChartViewController.Builder(mContext)
                .title(mContext.getResources().getString(R.string.wxt_traffic))
                .titleOfAxisX(null)
                .titleOfAxisY("kb/s")
                .labelColor(Color.WHITE)
                .backgroundColor(Color.parseColor("#ba000000"))
                .lineColor(Color.parseColor("#BACDDC39"))
                .lineTitle("rx")
                .lineTitle2("tx")
                .lineColor2(Color.parseColor("#6B673AB7"))
                .isFill(true)
                .fillColor(Color.parseColor("#BACDDC39"))
                .fillColor2(Color.parseColor("#6B673AB7"))
                .numXLabels(5)
                .minX(0)
                .maxX(20)
                .numYLabels(5)
                .minY(0)
                .maxY(expectMaxTraffic)
                .labelFormatter(new TimestampLabelFormatter())
                .maxDataPoints(20 + 2)
                .build();

        //show legend
        LegendRenderer legendRenderer = ((ChartView)mChartViewController.getChartView()).getLegendRenderer();
        legendRenderer.setTextColor(Color.WHITE);
        legendRenderer.setVisible(true);
        legendRenderer.setBackgroundColor(Color.TRANSPARENT);
        legendRenderer.setAlign(LegendRenderer.LegendAlign.TOP);
        legendRenderer.setMargin((int) ViewUtils.dp2px(mContext,10));

        FrameLayout frameLayout = new FrameLayout(mContext);
        View chartView = mChartViewController.getChartView();
        frameLayout.addView(chartView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        //add close btn. it's ugly here,we can expend chart view to support close btn.
        TextView closeBtn = new TextView(mContext);
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setText(mContext.getResources().getString(R.string.wxt_close));
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnCloseListener != null && isViewAttached) {
                    mOnCloseListener.close(TrafficSampleView.this);
                    dismiss();
                }
            }
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) ViewUtils.dp2px(mContext, 50), (int) ViewUtils.dp2px(mContext, 30));
        params.gravity = Gravity.RIGHT;
        frameLayout.addView(closeBtn, params);

        return frameLayout;
    }

    @Override
    protected void onShown() {
        if (mSampleTrafficTask != null) {
            mSampleTrafficTask.stop();
            mSampleTrafficTask = null;
        }
        mSampleTrafficTask = new TrafficSampleView.SampleTrafficTask(mChartViewController, SDKUtils.isDebugMode(mContext));
        mSampleTrafficTask.start();
    }

    @Override
    protected void onDismiss() {
        if (mSampleTrafficTask != null) {
            mSampleTrafficTask.stop();
            mSampleTrafficTask = null;
        }
    }

    private static class SampleTrafficTask extends AbstractLoopTask {

        private boolean isDebug;
        private DynamicChartViewController mController;

        private static final float LOAD_FACTOR = 0.5F;

        private int mAxisXValue = -1;

        private TrafficTaskEntity mEntity;
        private static final int DELAY_IN_MILLIS = 1000;

        SampleTrafficTask(DynamicChartViewController controller, boolean isDebug) {
            super(false, DELAY_IN_MILLIS);
            this.isDebug = isDebug;
            this.mController = controller;
            mEntity = new TrafficTaskEntity(DELAY_IN_MILLIS);
        }

        @Override
        protected void onStart() {
            mEntity.onTaskInit();
        }

        @Override
        protected void onRun() {
            final double txSpeed;
            final double rxSpeed;

            TrafficTaskEntity.TrafficInfo info = mEntity.onTaskRun();
            txSpeed = info.txSpeed;
            rxSpeed = info.rxSpeed;
            if (isDebug) {
                Log.d("weex-analyzer", "network[tx:" + txSpeed + "kb/s,rx:" + rxSpeed + "kb/s]");
            }

            mAxisXValue++;

            runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    double max = Math.max(rxSpeed,txSpeed);

                    if (checkIfNeedUpdateYAxis(max)) {
                        mController.updateAxisY(mController.getMinY(), (mController.getMaxY() - mController.getMinY()) * 2, 0);
                    }
                    mController.appendPointAndInvalidate(mAxisXValue, rxSpeed);
                    mController.appendPointAndInvalidate2(mAxisXValue,txSpeed);
                }
            });
        }

        private boolean checkIfNeedUpdateYAxis(double traffic) {
            double currentMaxY = (mController.getMaxY() - mController.getMinY());
            return currentMaxY * LOAD_FACTOR <= traffic;
        }

        @Override
        protected void onStop() {
            mEntity.onTaskStop();
        }
    }
}

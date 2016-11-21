package com.taobao.weex.analyzer.view;

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

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.core.MemorySampler;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.analyzer.view.chart.TimestampLabelFormatter;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/17<br/>
 * Time: 下午3:45<br/>
 */

public class MemorySampleView extends DragSupportOverlayView {

    private DynamicChartViewController mChartViewController;
    private SampleMemoryTask mTask;

    private OnCloseListener mOnCloseListener;

    public MemorySampleView(Context application) {
        super(application);
        mWidth = WindowManager.LayoutParams.MATCH_PARENT;
        mHeight = (int) ViewUtils.dp2px(application, 150);
    }

    public void setOnCloseListener(@Nullable OnCloseListener listener){
        this.mOnCloseListener = listener;
    }

    @NonNull
    @Override
    protected View onCreateView() {
        //prepare chart view
        double maxMemory = MemorySampler.maxMemory();
        double totalMemory = MemorySampler.totalMemory();
        double maxY = Math.min(totalMemory * 2, maxMemory);
        mChartViewController = new DynamicChartViewController.Builder(mContext)
                .title(mContext.getResources().getString(R.string.wxt_memory))
                .titleOfAxisX(null)
                .titleOfAxisY("MB")
                .labelColor(Color.WHITE)
                .backgroundColor(Color.parseColor("#ba000000"))
                .lineColor(Color.parseColor("#BACDDC39"))
                .isFill(true)
                .fillColor(Color.parseColor("#BACDDC39"))
                .numXLabels(5)
                .minX(0)
                .maxX(20)
                .numYLabels(5)
                .minY(0)
                .maxY(ViewUtils.findSuitableVal(maxY,4))//step = verticalLabelsNum-1
                .labelFormatter(new TimestampLabelFormatter())
                .maxDataPoints(20 + 2)
                .build();

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
                if(mOnCloseListener != null && isViewAttached){
                    mOnCloseListener.close(MemorySampleView.this);
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
        if(mTask != null){
            mTask.stop();
            mTask = null;
        }
        mTask = new SampleMemoryTask(mChartViewController, SDKUtils.isDebugMode(mContext));
        mTask.start();
    }

    @Override
    protected void onDismiss() {
        if (mTask != null) {
            mTask.stop();
            mTask = null;
            mChartViewController = null;
        }
    }

    private static class SampleMemoryTask extends AbstractLoopTask {

        private DynamicChartViewController mController;
        private int mAxisXValue = -1;
        private static final float LOAD_FACTOR = 0.75F;
        private boolean isDebug;

        SampleMemoryTask(@NonNull DynamicChartViewController controller, boolean isDebug) {
            super(false,1000);
            this.mController = controller;
            this.isDebug = isDebug;
        }

        @Override
        protected void onRun() {
            if (mController == null) {
                return;
            }
            mAxisXValue++;
            final double memoryUsed = MemorySampler.getMemoryUsage();
            if(isDebug) {
                Log.d("weex-analyzer", "memory usage : "+ memoryUsed + "MB");
            }
            runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (checkIfNeedUpdateYAxis(memoryUsed)) {
                        mController.updateAxisY(0, (mController.getMaxY() - mController.getMinY()) * 2, 0);
                    }
                    mController.appendPointAndInvalidate(mAxisXValue, memoryUsed);
                }
            });
        }

        private boolean checkIfNeedUpdateYAxis(double memoryUsed) {
            double currentMaxY = (mController.getMaxY() - mController.getMinY());
            return currentMaxY * LOAD_FACTOR <= memoryUsed;
        }

        @Override
        protected void onStop() {
            mController = null;
        }
    }

}














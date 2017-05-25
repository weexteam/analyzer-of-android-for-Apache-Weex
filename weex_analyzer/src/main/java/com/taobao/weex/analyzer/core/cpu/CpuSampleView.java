package com.taobao.weex.analyzer.core.cpu;

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
import com.taobao.weex.analyzer.view.chart.TimestampLabelFormatter;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/8<br/>
 * Time: 下午12:20<br/>
 */

public class CpuSampleView extends PermissionOverlayView {

    private SampleCpuTask mSampleCpuTask;
    private DynamicChartViewController mChartViewController;

    private OnCloseListener mOnCloseListener;

    public void setOnCloseListener(@Nullable OnCloseListener listener) {
        this.mOnCloseListener = listener;
    }

    public CpuSampleView(Context application,Config config) {
        super(application,true,config);

        mWidth = WindowManager.LayoutParams.MATCH_PARENT;
        mHeight = (int) ViewUtils.dp2px(application, 150);
    }

    @NonNull
    @Override
    protected View onCreateView() {
        //prepare chart view
        int expectMaxCpuUsage = 40;//%
        mChartViewController = new DynamicChartViewController.Builder(mContext)
                .title(mContext.getResources().getString(R.string.wxt_cpu))
                .titleOfAxisX(null)
                .titleOfAxisY("cpu(%)")
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
                .maxY(expectMaxCpuUsage)
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
                if (mOnCloseListener != null && isViewAttached) {
                    mOnCloseListener.close(CpuSampleView.this);
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
        if(mSampleCpuTask != null){
            mSampleCpuTask.stop();
            mSampleCpuTask = null;
        }
        mSampleCpuTask = new SampleCpuTask(mChartViewController, SDKUtils.isDebugMode(mContext));
        mSampleCpuTask.start();
    }

    @Override
    protected void onDismiss() {
        if (mSampleCpuTask != null) {
            mSampleCpuTask.stop();
            mSampleCpuTask = null;
        }
    }

    @Override
    public boolean isPermissionGranted(@NonNull Config config) {
        return !config.getIgnoreOptions().contains(Config.TYPE_CPU);
    }

    private static class SampleCpuTask extends AbstractLoopTask {
        private int mAxisXValue = -1;
        private DynamicChartViewController mController;
        private boolean isDebug = false;
        private static final float LOAD_FACTOR = 0.75F;

        private CpuTaskEntity mEntity;

        SampleCpuTask(DynamicChartViewController controller, boolean isDebug) {
            super(false,1000);
            this.mController = controller;
            this.isDebug = isDebug;

            mEntity = new CpuTaskEntity();
        }

        @Override
        protected void onStart() {
            mEntity.onTaskInit();
        }

        @Override
        protected void onRun() {
            CpuTaskEntity.CpuInfo cpuInfo = mEntity.onTaskRun();
            final double pidCpuUsage = cpuInfo.pidCpuUsage;
            final double pidUserCpuUsage = cpuInfo.pidUserCpuUsage;
            final double pidKernelCpuUsage = cpuInfo.pidKernelCpuUsage;

            if (isDebug) {
                Log.d("weex-analyzer", "cpu usage:" + pidCpuUsage + "% [user " + pidUserCpuUsage + ",kernel " + pidKernelCpuUsage + "]");
            }


            mAxisXValue++;
            runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (checkIfNeedUpdateYAxis(pidCpuUsage)) {
                        mController.updateAxisY(mController.getMinY(), Math.max(100,(mController.getMaxY() - mController.getMinY()) + 10), 0);
                    }
                    mController.appendPointAndInvalidate(mAxisXValue, pidCpuUsage);
                }
            });
        }

        private boolean checkIfNeedUpdateYAxis(double cpuUsage) {
            double currentMaxY = (mController.getMaxY() - mController.getMinY());
            return currentMaxY * LOAD_FACTOR <= cpuUsage;
        }

        @Override
        protected void onStop() {
            mEntity.onTaskStop();
        }
    }
}

package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.core.CpuSampler;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.analyzer.view.chart.TimestampLabelFormatter;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/8<br/>
 * Time: 下午12:20<br/>
 */

public class CpuSampleView extends DragSupportOverlayView {

    private SampleCpuTask mSampleCpuTask;
    private DynamicChartViewController mChartViewController;

    private OnCloseListener mOnCloseListener;

    public void setOnCloseListener(@Nullable OnCloseListener listener) {
        this.mOnCloseListener = listener;
    }

    public CpuSampleView(Context application) {
        super(application);

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
                .lineColor(Color.parseColor("#CDDC39"))
                .isFill(true)
                .fillColor(Color.parseColor("#CDDC39"))
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

    private static class SampleCpuTask extends AbstractLoopTask {

        private long mTotalCpuTimeLast = 0;
        private long mPidTotalCpuTimeLast = 0;
        private long mPidUserCpuTimeLast = 0;
        private long mPidKernelCpuTimeLast = 0;

        private int mAxisXValue = -1;
        private DynamicChartViewController mController;
        private boolean isDebug = false;
        private static final float LOAD_FACTOR = 0.75F;

        SampleCpuTask(DynamicChartViewController controller, boolean isDebug) {
            super(false,1000);
            this.mController = controller;
            this.isDebug = isDebug;
        }

        @Override
        protected void onStart() {
            mTotalCpuTimeLast = 0L;
            mPidTotalCpuTimeLast = 0L;
            mPidUserCpuTimeLast = 0L;
            mPidKernelCpuTimeLast = 0L;
        }

        @Override
        protected void onRun() {
            String pidCpuRate = CpuSampler.samplePidCpuRate();
            String totalCpuRate = CpuSampler.sampleCpuRate();

            if (TextUtils.isEmpty(pidCpuRate) || TextUtils.isEmpty(totalCpuRate)) {
                return;
            }

            String[] cpuInfoArray = totalCpuRate.split(" ");
            if (cpuInfoArray.length < 9) {
                return;
            }

            String[] pidCpuInfoList = pidCpuRate.split(" ");
            if (pidCpuInfoList.length < 17) {
                return;
            }

            long user = Long.parseLong(cpuInfoArray[2]);
            long nice = Long.parseLong(cpuInfoArray[3]);
            long system = Long.parseLong(cpuInfoArray[4]);
            long idle = Long.parseLong(cpuInfoArray[5]);
            long ioWait = Long.parseLong(cpuInfoArray[6]);
            long hardIrq = Long.parseLong(cpuInfoArray[7]);
            long softIrq = Long.parseLong(cpuInfoArray[8]);
            long stealTime = Long.parseLong(cpuInfoArray[9]);

            long pidUTime = Long.parseLong(pidCpuInfoList[13]);
            long pidSTime = Long.parseLong(pidCpuInfoList[14]);
            long pidCUTime = Long.parseLong(pidCpuInfoList[15]);
            long pidCSTime = Long.parseLong(pidCpuInfoList[16]);

            long cpuTime = user + nice + system + idle + ioWait + hardIrq + softIrq + stealTime;
            long pidCpuTime = pidUTime + pidSTime + pidCUTime + pidCSTime;

            final long pidCpuUsage;
            if (mTotalCpuTimeLast != 0) {
                pidCpuUsage = (pidCpuTime - mPidTotalCpuTimeLast) * 100L / (cpuTime - mTotalCpuTimeLast);

                long pidUserCpuUsage = (pidUTime - mPidUserCpuTimeLast) * 100L / (cpuTime - mTotalCpuTimeLast);
                long pidKernelCpuUsage = (pidSTime - mPidKernelCpuTimeLast) * 100L / (cpuTime - mTotalCpuTimeLast);
                if (isDebug) {
                    Log.d("weex-analyzer", "cpu usage:" + pidCpuUsage + "% [user " + pidUserCpuUsage + ",kernel " + pidKernelCpuUsage + "]");
                }
            }else{
                pidCpuUsage = 0L;
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

            mTotalCpuTimeLast = cpuTime;
            mPidTotalCpuTimeLast = pidCpuTime;
            mPidUserCpuTimeLast = pidUTime;
            mPidKernelCpuTimeLast = pidSTime;
        }

        private boolean checkIfNeedUpdateYAxis(double cpuUsage) {
            double currentMaxY = (mController.getMaxY() - mController.getMinY());
            return currentMaxY * LOAD_FACTOR <= cpuUsage;
        }

        @Override
        protected void onStop() {
            mTotalCpuTimeLast = 0L;
            mPidTotalCpuTimeLast = 0L;
            mPidUserCpuTimeLast = 0L;
            mPidKernelCpuTimeLast = 0L;
        }
    }
}

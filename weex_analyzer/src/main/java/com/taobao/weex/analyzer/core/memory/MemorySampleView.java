package com.taobao.weex.analyzer.core.memory;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.taobao.weex.analyzer.view.chart.TimestampLabelFormatter;
import com.taobao.weex.analyzer.view.overlay.PermissionOverlayView;
import com.taobao.weex.utils.WXLogUtils;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/17<br/>
 * Time: 下午3:45<br/>
 */

public class MemorySampleView extends PermissionOverlayView {

    private DynamicChartViewController mChartViewController;
    private SampleMemoryTask mTask;

    private OnCloseListener mOnCloseListener;

    public MemorySampleView(Context application, Config config) {
        super(application, true, config);
        mWidth = WindowManager.LayoutParams.MATCH_PARENT;
        mHeight = (int) ViewUtils.dp2px(application, 220);
    }

    @Override
    public boolean isPermissionGranted(@NonNull Config config) {
        return !config.getIgnoreOptions().contains(Config.TYPE_MEMORY);
    }

    public void setOnCloseListener(@Nullable OnCloseListener listener) {
        this.mOnCloseListener = listener;
    }

    @NonNull
    @Override
    protected View onCreateView() {
        View wrapper = LayoutInflater.from(mContext).inflate(R.layout.wxt_memory_view, null);
        FrameLayout frameLayout = (FrameLayout) wrapper.findViewById(R.id.container);

        TextView close = (TextView) wrapper.findViewById(R.id.close);
        TextView gc = (TextView) wrapper.findViewById(R.id.gc);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnCloseListener != null && isViewAttached) {
                    mOnCloseListener.close(MemorySampleView.this);
                    dismiss();
                }
            }
        });

        gc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            MemorySampler.tryForceGC();
                        } catch (Exception e) {
                            WXLogUtils.e(e.getMessage());
                        }
                        return null;
                    }
                }.execute();
            }
        });

        //prepare chart view
        double maxMemory = MemorySampler.maxMemory();
        double totalMemory = MemorySampler.totalMemory();
        double maxY = Math.min(totalMemory * 2, maxMemory);
        mChartViewController = new DynamicChartViewController.Builder(mContext)
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
                .maxY(ViewUtils.findSuitableVal(maxY, 4))//step = verticalLabelsNum-1
                .labelFormatter(new TimestampLabelFormatter())
                .maxDataPoints(20 + 2)
                .build();

        View chartView = mChartViewController.getChartView();
        frameLayout.addView(chartView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        return wrapper;
    }


    @Override
    protected void onShown() {
        if (mTask != null) {
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

        private MemoryTaskEntity mEntity;

        SampleMemoryTask(@NonNull DynamicChartViewController controller, boolean isDebug) {
            super(false, 1000);
            this.mController = controller;
            this.isDebug = isDebug;

            mEntity = new MemoryTaskEntity();
        }

        @Override
        protected void onStart() {
            mEntity.onTaskInit();
        }

        @Override
        protected void onRun() {
            if (mController == null) {
                return;
            }
            mAxisXValue++;
            final double memoryUsed = mEntity.onTaskRun();
            if (isDebug) {
                Log.d("weex-analyzer", "memory usage : " + memoryUsed + "MB");
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
            mEntity.onTaskStop();
            mController = null;
        }
    }

}














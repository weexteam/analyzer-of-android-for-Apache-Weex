package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.Performance;
import com.taobao.weex.analyzer.core.reporter.IDataReporter;
import com.taobao.weex.analyzer.core.reporter.LaunchConfig;
import com.taobao.weex.analyzer.core.reporter.MDSDataReporterFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/3<br/>
 * Time: 下午4:25<br/>
 */

public class WXPerformanceAnalysisView extends AbstractAlertView {

    private WXPerfItemView mPerfItemView;
    private WXPerfHistoryItemView mPerfHistoryItemView;

    private List<Performance> mHistoryPerfList;
    private Performance mCurPerformance;

    @Nullable
    private IDataReporter<Performance> mDataReporter;

    private AtomicInteger mCounter = new AtomicInteger(0);

    public WXPerformanceAnalysisView(Context context, @NonNull Performance curPerformance, @NonNull List<Performance> historyPerfs) {
        super(context);
        this.mCurPerformance = curPerformance;
        this.mHistoryPerfList = historyPerfs;
        String from = LaunchConfig.getFrom();
        String deviceId = LaunchConfig.getDeviceId();

        if (!TextUtils.isEmpty(from) && !TextUtils.isEmpty(deviceId)) {
            mDataReporter = MDSDataReporterFactory.create(from, deviceId);
        }
    }

    @Override
    protected void onInitView(@NonNull Window window) {
        mPerfItemView = (WXPerfItemView) window.findViewById(R.id.panel_cur_perf);
        mPerfHistoryItemView = (WXPerfHistoryItemView) window.findViewById(R.id.panel_history_perf);

        final TextView btnCurPanel = (TextView) window.findViewById(R.id.btn_cur_panel);
        final TextView btnHistoryPanel = (TextView) window.findViewById(R.id.btn_history_panel);

        window.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnCurPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnHistoryPanel.setTextColor(Color.BLACK);
                btnHistoryPanel.setBackgroundColor(Color.WHITE);
                btnCurPanel.setTextColor(Color.WHITE);
                btnCurPanel.setBackgroundColor(Color.parseColor("#03A9F4"));
                mPerfItemView.setVisibility(View.VISIBLE);
                mPerfHistoryItemView.setVisibility(View.GONE);
            }
        });

        btnHistoryPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCurPanel.setTextColor(Color.BLACK);
                btnCurPanel.setBackgroundColor(Color.WHITE);
                btnHistoryPanel.setTextColor(Color.WHITE);
                btnHistoryPanel.setBackgroundColor(Color.parseColor("#03A9F4"));

                mPerfHistoryItemView.setVisibility(View.VISIBLE);
                mPerfItemView.setVisibility(View.GONE);

            }
        });
    }

    @Override
    protected void onShown() {
        mPerfItemView.inflateData(mCurPerformance);
        mPerfHistoryItemView.inflateData(mHistoryPerfList);

        if (mDataReporter != null && mCurPerformance != null && mDataReporter.isEnabled()) {
            mDataReporter.report(new IDataReporter.ProcessedDataBuilder<Performance>()
                    .sequenceId(mCounter.getAndIncrement())
                    .data(mCurPerformance)
                    .deviceId(LaunchConfig.getDeviceId())
                    .type(IDataReporter.TYPE_WEEX_PERFORMANCE_STATISTICS)
                    .build()

            );
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wxt_weex_perf_analysis_view;
    }
}

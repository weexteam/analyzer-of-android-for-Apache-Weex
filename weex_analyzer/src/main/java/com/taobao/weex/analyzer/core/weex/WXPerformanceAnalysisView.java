package com.taobao.weex.analyzer.core.weex;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.view.alert.PermissionAlertView;

import java.util.List;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/3<br/>
 * Time: 下午4:25<br/>
 */

public class WXPerformanceAnalysisView extends PermissionAlertView {

    private WXPerfItemView mPerfItemView;
    private WXPerfHistoryItemView mPerfHistoryItemView;

    private List<Performance> mHistoryPerfList;
    private Performance mCurPerformance;

    public WXPerformanceAnalysisView(Context context, @NonNull Performance curPerformance, @NonNull List<Performance> historyPerfs, Config config) {
        super(context,config);
        this.mCurPerformance = curPerformance;
        this.mHistoryPerfList = historyPerfs;
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
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wxt_weex_perf_analysis_view;
    }

    @Override
    public boolean isPermissionGranted(@NonNull Config config) {
        return !config.getIgnoreOptions().contains(Config.TYPE_WEEX_PERFORMANCE_STATISTICS);
    }
}

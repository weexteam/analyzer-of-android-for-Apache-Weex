package com.taobao.weex.analyzer.core.weex;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;
import android.widget.Toast;

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.analyzer.view.overlay.AbstractBizItemView;
import com.taobao.weex.analyzer.view.chart.ChartView;
import com.taobao.weex.analyzer.view.chart.DataPoint;
import com.taobao.weex.analyzer.view.chart.DataPointInterface;
import com.taobao.weex.analyzer.view.chart.GridLabelRenderer;
import com.taobao.weex.analyzer.view.chart.LegendRenderer;
import com.taobao.weex.analyzer.view.chart.LineGraphSeries;
import com.taobao.weex.analyzer.view.chart.OnDataPointTapListener;
import com.taobao.weex.analyzer.view.chart.Series;
import com.taobao.weex.analyzer.view.chart.Viewport;

import java.util.List;
import java.util.Locale;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/3<br/>
 * Time: 下午5:31<br/>
 */

public class WXPerfHistoryItemView extends AbstractBizItemView<List<Performance>> implements OnDataPointTapListener {

    private ChartView mGraphView;
    private TextView mAverageVal;

    public WXPerfHistoryItemView(Context context) {
        super(context);
    }

    public WXPerfHistoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WXPerfHistoryItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void prepareView() {
        mGraphView = (ChartView) findViewById(R.id.chart);
        mAverageVal = (TextView) findViewById(R.id.average);

        GridLabelRenderer labelRenderer = mGraphView.getGridLabelRenderer();
        Viewport viewport = mGraphView.getViewport();

        viewport.setScalable(false);
        viewport.setScalableY(false);

        viewport.setScrollable(false);
        viewport.setScrollableY(false);

        //theme config
        mGraphView.setBackgroundColor(Color.WHITE);
        labelRenderer.setHorizontalLabelsColor(Color.BLACK);
        labelRenderer.setVerticalLabelsColor(Color.BLACK);
        labelRenderer.setHorizontalAxisTitleColor(Color.BLACK);
        labelRenderer.setVerticalAxisTitleColor(Color.BLACK);
        mGraphView.setTitleColor(Color.BLACK);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wxt_panel_history_perf_view;
    }

    @Override
    public void inflateData(List<Performance> data) {
        int sampleSize = data.size();
        if (sampleSize == 0) {
            return;
        }

        double maxY = 480;//480 unit for y axis
        int verticalPart = 8;

        DataPoint[] ctPoints = new DataPoint[sampleSize];//communicate time points
        DataPoint[] ttPoints = new DataPoint[sampleSize]; //total time points
        DataPoint[] nwtPoints = new DataPoint[sampleSize];//network time points

        long fsrTotal = 0,ttTotal = 0,nwTotal = 0;

        for (int i = 0; i < sampleSize; i++) {
            Performance p = data.get(i);
            ctPoints[i] = new DataPoint(i,p.communicateTime);
            ttPoints[i] = new DataPoint(i,p.screenRenderTime);
            nwtPoints[i] = new DataPoint(i,p.networkTime);
            maxY = Math.max(Math.max(p.totalTime,p.networkTime),Math.max(p.communicateTime,maxY));

            fsrTotal += p.communicateTime;
            ttTotal += p.screenRenderTime;
            nwTotal += p.networkTime;
        }

        Viewport viewport = mGraphView.getViewport();
        GridLabelRenderer labelRenderer = mGraphView.getGridLabelRenderer();

        labelRenderer.setHumanRounding(false);

        //axis config
        labelRenderer.setNumHorizontalLabels(sampleSize+1);
        labelRenderer.setNumVerticalLabels(verticalPart+1);

        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(sampleSize);

        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(ViewUtils.findSuitableVal(maxY,verticalPart));

        LineGraphSeries<DataPoint> ctSeries = new LineGraphSeries<>(ctPoints);
        LineGraphSeries<DataPoint> ttSeries = new LineGraphSeries<>(ttPoints);
        LineGraphSeries<DataPoint> nwtSeries = new LineGraphSeries<>(nwtPoints);

        ctSeries.setTitle("createInstance时间");
        ttSeries.setTitle("首屏时间");
        nwtSeries.setTitle("网络时间");

        ctSeries.setOnDataPointTapListener(this);
        ttSeries.setOnDataPointTapListener(this);
        nwtSeries.setOnDataPointTapListener(this);

        //series config
        ctSeries.setColor(Color.parseColor("#E91E63"));//pink
        ttSeries.setColor(Color.parseColor("#9C27B0"));//purple
        nwtSeries.setColor(Color.parseColor("#CDDC39"));//lime

        ctSeries.setDrawDataPoints(true);
        ttSeries.setDrawDataPoints(true);
        nwtSeries.setDrawDataPoints(true);

        ctSeries.setAnimated(true);
        ttSeries.setAnimated(true);
        nwtSeries.setAnimated(true);


        //add series to chart
        mGraphView.addSeries(ctSeries);
        mGraphView.addSeries(ttSeries);
        mGraphView.addSeries(nwtSeries);


        //legend config
        LegendRenderer legendRenderer = mGraphView.getLegendRenderer();
        legendRenderer.setVisible(true);
        legendRenderer.setBackgroundColor(Color.TRANSPARENT);
        legendRenderer.setAlign(LegendRenderer.LegendAlign.TOP);

        mAverageVal.setText(String.format(Locale.CHINA,getContext().getResources().getString(R.string.wxt_average),
                (fsrTotal/(float)sampleSize),
                (ttTotal/(float)sampleSize),
                (nwTotal/(float)sampleSize)
        ));

    }

    @Override
    public void onTap(Series series, DataPointInterface dataPoint) {
        Toast.makeText(getContext(),series.getTitle()+"("+dataPoint.getX()+","+dataPoint.getY()+")",Toast.LENGTH_SHORT).show();
    }
}

package com.taobao.weex.analyzer.view.chart;

/**
 * Listener for the tap event which will be
 * triggered when the user touches on a datapoint.
 */
public interface OnDataPointTapListener {
    /**
     * gets called when the user touches on a datapoint.
     *
     * @param series the corresponding series
     * @param dataPoint the data point that was tapped on
     */
    void onTap(Series series, DataPointInterface dataPoint);
}

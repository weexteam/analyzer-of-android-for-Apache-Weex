package com.taobao.weex.analyzer.view.chart;

import android.graphics.Canvas;

import java.util.Iterator;

public interface Series<E extends DataPointInterface> {
    /**
     * @return the lowest x-value of the data
     */
    public double getLowestValueX();

    /**
     * @return the highest x-value of the data
     */
    public double getHighestValueX();

    /**
     * @return the lowest y-value of the data
     */
    public double getLowestValueY();

    /**
     * @return the highest y-value of the data
     */
    public double getHighestValueY();

    /**
     * getPerformanceList the values for a specific range. It is
     * important that the data comes in the sorted order
     * (from lowest to highest x-value).
     *
     * @param from the minimal x-value
     * @param until the maximal x-value
     * @return  all datapoints between the from and until x-value
     *          including the from and until data points.
     */
    public Iterator<E> getValues(double from, double until);

    /**
     * Plots the series to the viewport.
     * You have to care about overdrawing.
     * This method may be called 2 times: one for
     * the default scale and one time for the
     * second scale.
     *
     * @param graphView corresponding graphview
     * @param canvas canvas to draw on
     * @param isSecondScale true if the drawing is for the second scale
     */
    public void draw(ChartView graphView, Canvas canvas);

    /**
     * @return the title of the series. Used in the legend
     */
    public String getTitle();

    /**
     * @return  the color of the series. Used in the legend and should
     *          be used for the plotted points or lines.
     */
    public int getColor();

    /**
     * set a listener for tap on a data point.
     *
     * @param l listener
     */
    public void setOnDataPointTapListener(OnDataPointTapListener l);

    /**
     * called by the tap detector in order to trigger
     * the on tap on datapoint event.
     *
     * @param x pixel
     * @param y pixel
     */
    void onTap(float x, float y);

    /**
     * called when the series was added to a graph
     *
     * @param graphView graphview
     */
    void onGraphViewAttached(ChartView graphView);

    /**
     * @return whether there are data points
     */
    boolean isEmpty();
}

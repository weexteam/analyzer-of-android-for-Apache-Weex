package com.taobao.weex.analyzer.view.chart;

/**
 * Interface to use as label formatter.
 * Implement this in order to generate
 * your own labels format.
 */
public interface LabelFormatter {
    /**
     * converts a raw number as input to
     * a formatted string for the label.
     *
     * @param value raw input number
     * @param isValueX  true if it is a value for the x axis
     *                  false if it is a value for the y axis
     * @return the formatted number as string
     */
    public String formatLabel(double value, boolean isValueX);

    /**
     * will be called in order to have a
     * reference to the current viewport.
     * This is useful if you need the bounds
     * to generate your labels.
     * You store this viewport in as member variable
     * and access it e.g. in the {@link #formatLabel(double, boolean)}
     * method.
     *
     * @param viewport the used viewport
     */
    public void setViewport(Viewport viewport);
}

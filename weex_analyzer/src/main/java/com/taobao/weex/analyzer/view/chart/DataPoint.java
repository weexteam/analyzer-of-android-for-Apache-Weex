package com.taobao.weex.analyzer.view.chart;

import java.io.Serializable;

/**
 * default data point implementation.
 * This stores the x and y values.
 */
public class DataPoint implements DataPointInterface, Serializable {
    private static final long serialVersionUID=1428263322645L;

    private double x;
    private double y;

    public DataPoint(double x, double y) {
        this.x=x;
        this.y=y;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "["+x+"/"+y+"]";
    }
}

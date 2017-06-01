package com.taobao.weex.analyzer.view.chart;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

/**
 * Description:
 * a helper class for create realtime and dynamic chart view.
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/17<br/>
 * Time: 下午7:18<br/>
 */

public class DynamicChartViewController {

    private ChartView mChartView;
    private int mMaxPoints;

    private DynamicChartViewController(ChartView chartView) {
        this.mChartView = chartView;
    }

    /**
     * 设置最大缓存坐标点数
     */
    public void setMaxPoints(int maxPoints) {
        this.mMaxPoints = maxPoints;
    }


    /**
     * 获取图表视图
     */
    public View getChartView() {
        return mChartView;
    }


    /**
     * 更新y轴配置
     */
    public void updateAxisY(double minY, double maxY, int numLabels) {
        if (mChartView == null) {
            return;
        }
        Viewport viewport = mChartView.getViewport();
        GridLabelRenderer render = mChartView.getGridLabelRenderer();
        viewport.setYAxisBoundsManual(true);
        if (numLabels > 0) {
            render.setNumVerticalLabels(numLabels);
        }
        viewport.setMinY(minY);
        viewport.setMaxY(maxY);
    }

    /**
     * 向图表中增加点
     */
    @SuppressWarnings("unchecked")
    public void appendPointAndInvalidate(double x, double y) {
        LineGraphSeries<DataPoint> series = (LineGraphSeries<DataPoint>) mChartView.getSeries().get(0);
        series.appendData(new DataPoint(x, y), true, mMaxPoints);
    }


    @SuppressWarnings("unchecked")
    public void appendPointAndInvalidate2(double x, double y) {
        if(mChartView.getSeries().size() >= 2){
            LineGraphSeries<DataPoint> series = (LineGraphSeries<DataPoint>) mChartView.getSeries().get(1);
            series.appendData(new DataPoint(x, y), true, mMaxPoints);
        }
    }


    public double getMaxY() {
        if (mChartView == null) {
            return 0L;
        }
        return mChartView.getViewport().getMaxY(false);
    }

    public double getMaxX() {
        if (mChartView == null) {
            return 0L;
        }
        return mChartView.getViewport().getMaxX(false);
    }


    public double getMinX() {
        if (mChartView == null) {
            return 0L;
        }
        return mChartView.getViewport().getMinX(false);
    }


    public double getMinY() {
        if (mChartView == null) {
            return 0L;
        }
        return mChartView.getViewport().getMinY(false);
    }


    /**
     * Helper class for create chart view
     */
    public static class Builder {
        private int mViewBackground = Color.BLACK;
        private int mLabelColor = Color.WHITE;
        private int mAxisColor = Color.WHITE;
        private int mLineColor = Color.GREEN;
        private int mFillColor = Color.GREEN;

        private boolean isFill = false;

        private double mMinX = -1;
        private double mMinY = -1;
        private double mMaxX = -1;
        private double mMaxY = -1;

        private int mNumXLabels = 0;
        private int mNumYLabels = 0;

        private String mXAxisTitle = null;
        private String mYAxisTitle = null;

        private String mTitle;

        private LabelFormatter mLabelFormatter;
        private Context mContext;

        private int mMaxDataPoints = 0;

        private String mSeriesTitle;
        private String mSeries2Title;

        private int mLine2Color = Color.BLUE;
        private int mFill2Color = Color.BLUE;

        public Builder(Context context) {
            this.mContext = context;
        }


        public DynamicChartViewController build() {
            ChartView realView = new ChartView(mContext);
            realView.setTouchEnabled(false);
            GridLabelRenderer labelRenderer = realView.getGridLabelRenderer();

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            realView.addSeries(series);

            Viewport viewport = realView.getViewport();


            realView.setBackgroundColor(mViewBackground);

            labelRenderer.setHorizontalLabelsColor(mLabelColor);
            labelRenderer.setVerticalLabelsColor(mLabelColor);

            labelRenderer.setHorizontalAxisTitleColor(mLabelColor);
            labelRenderer.setVerticalAxisTitleColor(mLabelColor);

            realView.setTitleColor(mLabelColor);

            labelRenderer.setGridColor(mAxisColor);

            series.setColor(mLineColor);

            if(!TextUtils.isEmpty(mSeriesTitle)) {
                series.setTitle(mSeriesTitle);
            }

            //we have another line
            LineGraphSeries<DataPoint> series2 = null;
            if(!TextUtils.isEmpty(mSeries2Title)) {
                series2 = new LineGraphSeries<>();
                realView.addSeries(series2);

                series2.setTitle(mSeries2Title);
                series2.setColor(mLine2Color);
            }


            series.setDrawBackground(isFill);

            if (isFill) {
                series.setBackgroundColor(mFillColor);
            }

            if(series2 != null) {
                series2.setDrawBackground(isFill);
                if(isFill) {
                    series2.setBackgroundColor(mFill2Color);
                }
            }

            if (mMinX != -1 && mMaxX != -1) {
                viewport.setXAxisBoundsManual(true);
                viewport.setMinX(mMinX);
                viewport.setMaxX(mMaxX);
            } else {
                viewport.setXAxisBoundsManual(false);
            }


            if (mMinY != -1 && mMaxY != -1) {
                viewport.setYAxisBoundsManual(true);
                viewport.setMinY(mMinY);
                viewport.setMaxY(mMaxY);
            } else {
                viewport.setYAxisBoundsManual(false);
            }

            if (mNumXLabels != 0) {
                labelRenderer.setNumHorizontalLabels(mNumXLabels);
            }

            if (mNumYLabels != 0) {
                labelRenderer.setNumVerticalLabels(mNumYLabels);
            }

            if (mXAxisTitle != null) {
                labelRenderer.setHorizontalAxisTitle(mXAxisTitle);
            }

            if (mYAxisTitle != null) {
                labelRenderer.setVerticalAxisTitle(mYAxisTitle);
            }

            if (mTitle != null) {
                realView.setTitle(mTitle);
            }

            if (mLabelFormatter != null) {
                labelRenderer.setLabelFormatter(mLabelFormatter);
                labelRenderer.setHumanRounding(false);
            }

            viewport.setScalable(false);
            viewport.setScalableY(false);

            viewport.setScrollable(false);
            viewport.setScrollableY(false);

            DynamicChartViewController controller = new DynamicChartViewController(realView);
            if (mMaxDataPoints > 0) {
                controller.setMaxPoints(mMaxDataPoints);
            }
            return controller;
        }

        public Builder labelFormatter(@Nullable LabelFormatter formatter) {
            this.mLabelFormatter = formatter;
            return this;
        }

        public Builder title(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder lineTitle(@Nullable String title) {
            this.mSeriesTitle = title;
            return this;
        }

        public Builder lineTitle2(@Nullable String title) {
            this.mSeries2Title = title;
            return this;
        }

        /**
         * 图表背景色
         */
        public Builder backgroundColor(@ColorInt int background) {
            this.mViewBackground = background;
            return this;
        }

        public Builder fillColor(@ColorInt int fillColor) {
            this.mFillColor = fillColor;
            return this;
        }

        public Builder fillColor2(@ColorInt int fillColor) {
            this.mFill2Color = fillColor;
            return this;
        }

        /**
         * 坐标文字颜色
         */
        public Builder labelColor(@ColorInt int labelColor) {
            this.mLabelColor = labelColor;
            return this;
        }

        /**
         * 坐标轴线颜色
         */
        public Builder axisColor(@ColorInt int axisColor) {
            this.mAxisColor = axisColor;
            return this;
        }

        /**
         * 折线颜色
         */
        public Builder lineColor(@ColorInt int lineColor) {
            this.mLineColor = lineColor;
            return this;
        }

        /**
         * 折线2颜色
         * */
        public Builder lineColor2(@ColorInt int line2Color) {
            this.mLine2Color = line2Color;
            return this;
        }

        /**
         * 是否填充
         */
        public Builder isFill(boolean isFill) {
            this.isFill = isFill;
            return this;
        }

        /**
         * x轴最小值
         */
        public Builder minX(double minX) {
            this.mMinX = minX;
            return this;
        }

        /**
         * x轴最大值
         */
        public Builder maxX(double maxX) {
            this.mMaxX = maxX;
            return this;
        }

        /**
         * y轴最小值
         */
        public Builder minY(double minY) {
            this.mMinY = minY;
            return this;
        }

        /**
         * y轴最大值
         */
        public Builder maxY(double maxY) {
            this.mMaxY = maxY;
            return this;
        }

        /**
         * x轴显示多少坐标点
         */
        public Builder numXLabels(int numXLabels) {
            this.mNumXLabels = numXLabels;
            return this;
        }

        /**
         * y轴显示多少坐标点
         */
        public Builder numYLabels(int numYLabels) {
            this.mNumYLabels = numYLabels;
            return this;
        }

        /**
         * x轴title
         */
        public Builder titleOfAxisX(@Nullable String axisXTitle) {
            this.mXAxisTitle = axisXTitle;
            return this;
        }

        /**
         * y轴title
         */
        public Builder titleOfAxisY(@Nullable String axisYTitle) {
            this.mYAxisTitle = axisYTitle;
            return this;
        }


        /**
         * 图标上最多显示多少点
         */
        public Builder maxDataPoints(int maxDataPoints) {
            this.mMaxDataPoints = maxDataPoints;
            return this;
        }


    }
}
















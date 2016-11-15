package com.taobao.weex.analyzer.view.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import java.util.ArrayList;
import java.util.List;

/**
 *
 * https://github.com/jjoe64/GraphView
 * * @author jjoe64
 *
 * modified: remove some unused code such as second scale/other chart type...
 */
public class ChartView extends View {
    /**
     * Class to wrap style options that are general
     * to graphs.
     *
     */
    private static final class Styles {
        /**
         * The font size of the title that can be displayed
         * above the graph.
         *
         * @see ChartView#setTitle(String)
         */
        float titleTextSize;

        /**
         * The font color of the title that can be displayed
         * above the graph.
         *
         * @see ChartView#setTitle(String)
         */
        int titleColor;
    }

    /**
     * Helper class to detect tap events on the
     * graph.
     *
     */
    private class TapDetector {
        /**
         * save the time of the last down event
         */
        private long lastDown;

        /**
         * point of the tap down event
         */
        private PointF lastPoint;

        /**
         * to be called to process the events
         *
         * @param event
         * @return true if there was a tap event. otherwise returns false.
         */
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastDown = System.currentTimeMillis();
                lastPoint = new PointF(event.getX(), event.getY());
            } else if (lastDown > 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
                if (Math.abs(event.getX() - lastPoint.x) > 60
                        || Math.abs(event.getY() - lastPoint.y) > 60) {
                    lastDown = 0;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (System.currentTimeMillis() - lastDown < 400) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * whether touch event can received or not
     * */
    private boolean isTouchEnabled = true;

    private List<Series> mSeries;

    /**
     * the renderer for the grid and labels
     */
    private GridLabelRenderer mGridLabelRenderer;

    /**
     * viewport that holds the current bounds of
     * view.
     */
    private Viewport mViewport;

    /**
     * title of the graph that will be shown above
     */
    private String mTitle;

    /**
     * wraps the general styles
     */
    private Styles mStyles;

    /**
     * tap detector
     */
    private TapDetector mTapDetector;

    /**
     * renderer for the legend
     */
    private LegendRenderer mLegendRenderer;

    /**
     * paint for the graph title
     */
    private Paint mPaintTitle;

    /**
     * paint for the preview (in the SDK)
     */
    private Paint mPreviewPaint;

    /**
     * Initialize the GraphView view
     * @param context
     */
    public ChartView(Context context) {
        super(context);
        init();
    }

    /**
     * Initialize the GraphView view.
     *
     * @param context
     * @param attrs
     */
    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the GraphView view
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * initialize the internal objects.
     * This method has to be called directly
     * in the constructors.
     */
    protected void init() {
        mPreviewPaint = new Paint();
        mPreviewPaint.setTextAlign(Paint.Align.CENTER);
        mPreviewPaint.setColor(Color.BLACK);
        mPreviewPaint.setTextSize(50);

        mStyles = new Styles();
        mViewport = new Viewport(this);
        mGridLabelRenderer = new GridLabelRenderer(this);
        mLegendRenderer = new LegendRenderer(this);

        mSeries = new ArrayList<>();
        mPaintTitle = new Paint();

        mTapDetector = new TapDetector();

        loadStyles();
    }

    /**
     * loads the font
     */
    protected void loadStyles() {
        mStyles.titleColor = mGridLabelRenderer.getHorizontalLabelsColor();
        mStyles.titleTextSize = mGridLabelRenderer.getTextSize();
    }

    /**
     * @return the renderer for the grid and labels
     */
    public GridLabelRenderer getGridLabelRenderer() {
        return mGridLabelRenderer;
    }

    /**
     * Add a new series to the graph. This will
     * automatically redraw the graph.
     * @param s the series to be added
     */
    public void addSeries(Series s) {
        s.onGraphViewAttached(this);
        mSeries.add(s);
        onDataChanged(false, false);
    }

    public List<Series> getSeries() {
        // TODO immutable array
        return mSeries;
    }

    public void onDataChanged(boolean keepLabelsSize, boolean keepViewport) {
        // adjustSteps grid system
        mViewport.calcCompleteRange();
        mGridLabelRenderer.invalidate(keepLabelsSize, keepViewport);
        postInvalidate();
    }

    /**
     * draw all the stuff on canvas
     *
     * @param canvas
     */
    protected void drawGraphElements(Canvas canvas) {
        if (android.os.Build.VERSION.SDK_INT >= 11 && !canvas.isHardwareAccelerated()) {
            Log.d("ChartView","use android:hardwareAccelerated=\"true\" for better performance");
        }

        try {
            drawTitle(canvas);
            mViewport.drawFirst(canvas);
            mGridLabelRenderer.draw(canvas);
            for (Series s : mSeries) {
                s.draw(this, canvas);
            }
            mViewport.draw(canvas);
            mLegendRenderer.draw(canvas);
        }catch (Exception e) {
            Log.d("ChartView", e.getMessage());
        }
    }

    /**
     * will be called from Android system.
     *
     * @param canvas Canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            canvas.drawColor(Color.rgb(200, 200, 200));
            canvas.drawText("GraphView: No Preview available", canvas.getWidth()/2, canvas.getHeight()/2, mPreviewPaint);
        } else {
            drawGraphElements(canvas);
        }
    }

    /**
     * Draws the Graphs title that will be
     * shown above the viewport.
     * Will be called by GraphView.
     *
     * @param canvas Canvas
     */
    protected void drawTitle(Canvas canvas) {
        if (mTitle != null && mTitle.length()>0) {
            mPaintTitle.setColor(mStyles.titleColor);
            mPaintTitle.setTextSize(mStyles.titleTextSize);
            mPaintTitle.setTextAlign(Paint.Align.CENTER);
            float x = canvas.getWidth()/2;
            float y = mPaintTitle.getTextSize();
            canvas.drawText(mTitle, x, y, mPaintTitle);
        }
    }

    /**
     * Calculates the height of the title.
     *
     * @return  the actual size of the title.
     *          if there is no title, 0 will be
     *          returned.
     */
    protected int getTitleHeight() {
        if (mTitle != null && mTitle.length()>0) {
            return (int) mPaintTitle.getTextSize();
        } else {
            return 0;
        }
    }

    /**
     * @return the viewport of the Graph.
     * @see Viewport
     */
    public Viewport getViewport() {
        return mViewport;
    }

    /**
     * Called by Android system if the size
     * of the view was changed. Will recalculate
     * the viewport and labels.
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onDataChanged(false, false);
    }

    /**
     * @return  the space on the left side of the
     *          view from the left border to the
     *          beginning of the graph viewport.
     */
    public int getGraphContentLeft() {
        int border = getGridLabelRenderer().getStyles().padding;
        return border + getGridLabelRenderer().getLabelVerticalWidth() + getGridLabelRenderer().getVerticalAxisTitleWidth();
    }

    /**
     * @return  the space on the top of the
     *          view from the top border to the
     *          beginning of the graph viewport.
     */
    public int getGraphContentTop() {
        int border = getGridLabelRenderer().getStyles().padding + getTitleHeight();
        return border;
    }

    /**
     * @return  the height of the graph viewport.
     */
    public int getGraphContentHeight() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphheight = getHeight() - (2 * border) - getGridLabelRenderer().getLabelHorizontalHeight() - getTitleHeight();
        graphheight -= getGridLabelRenderer().getHorizontalAxisTitleHeight();
        return graphheight;
    }

    /**
     * @return  the width of the graph viewport.
     */
    public int getGraphContentWidth() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphwidth = getWidth() - (2 * border) - getGridLabelRenderer().getLabelVerticalWidth();
        return graphwidth;
    }

    /**
     * will be called from Android system.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isTouchEnabled){
            return false;
        }

        boolean b = mViewport.onTouchEvent(event);
        boolean a = super.onTouchEvent(event);

        // is it a click?
        if (mTapDetector.onTouchEvent(event)) {
            for (Series s : mSeries) {
                s.onTap(event.getX(), event.getY());
            }
        }

        return b || a;
    }

    public void setTouchEnabled(boolean isTouchEnabled){
        this.isTouchEnabled = isTouchEnabled;
    }

    public boolean isTouchEnabled(){
        return isTouchEnabled;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        mViewport.computeScroll();
    }

    public LegendRenderer getLegendRenderer() {
        return mLegendRenderer;
    }

    /**
     * use a specific legend renderer
     *
     * @param mLegendRenderer the new legend renderer
     */
    public void setLegendRenderer(LegendRenderer mLegendRenderer) {
        this.mLegendRenderer = mLegendRenderer;
    }

    /**
     * @return  the title that will be shown
     *          above the graph.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Set the title of the graph that will
     * be shown above the graph's viewport.
     *
     * @param mTitle the title
     * @see #setTitleColor(int) to set the font color
     * @see #setTitleTextSize(float) to set the font size
     */
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    /**
     * @return the title font size
     */
    public float getTitleTextSize() {
        return mStyles.titleTextSize;
    }

    /**
     * Set the title's font size
     *
     * @param titleTextSize font size
     * @see #setTitle(String)
     */
    public void setTitleTextSize(float titleTextSize) {
        mStyles.titleTextSize = titleTextSize;
    }

    /**
     * @return font color of the title
     */
    public int getTitleColor() {
        return mStyles.titleColor;
    }

    /**
     * Set the title's font color
     *
     * @param titleColor font color of the title
     * @see #setTitle(String)
     */
    public void setTitleColor(int titleColor) {
        mStyles.titleColor = titleColor;
    }

    /**
     * Removes all series of the graph.
     */
    public void removeAllSeries() {
        mSeries.clear();
        onDataChanged(false, false);
    }

    /**
     * Remove a specific series of the graph.
     * This will also re-render the graph, but
     * without recalculating the viewport and
     * label sizes.
     * If you want this, you have to call {@link #onDataChanged(boolean, boolean)}
     * manually.
     *
     * @param series
     */
    public void removeSeries(Series<?> series) {
        mSeries.remove(series);
        onDataChanged(false, false);
    }
}

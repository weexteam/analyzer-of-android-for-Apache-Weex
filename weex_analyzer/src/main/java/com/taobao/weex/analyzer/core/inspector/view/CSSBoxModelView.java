package com.taobao.weex.analyzer.core.inspector.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.taobao.weex.analyzer.utils.ViewUtils;

/**
 * Description:
 *  绘制css盒模型
 * Created by rowandjj(chuyi)<br/>
 */

public class CSSBoxModelView extends View {

    private static final int COLOR_MARGIN = 0xBAFACC9E;
    private static final int COLOR_BORDER = 0xBAFCDC9B;
    private static final int COLOR_PADDING = 0xBAC3CE8A;
    private static final int COLOR_CONTENT = 0xBA12B6C1;

    private static final int COLOR_TEXT = 0xFFFFFFFF;


    private static final String TEXT_MARGIN = "margin";
    private static final String TEXT_BORDER = "border";
    private static final String TEXT_PADDING = "padding";

    private static final String TEXT_UNKNOWN = "?";
    private static final String TEXT_ZERO = "0";
    private static final String TEXT_NULL = "-";


    private final float DEFAULT_VIEW_HEIGHT = ViewUtils.dp2px(getContext(),160);
    private final float DEFAULT_VIEW_WIDTH = ViewUtils.dp2px(getContext(),200);

    private final float DEFAULT_BOX_GAP = ViewUtils.dp2px(getContext(),45);


    private float mViewMinWidth = DEFAULT_VIEW_WIDTH;
    private float mViewMinHeight = DEFAULT_VIEW_HEIGHT;

    private Paint mShapePaint;
    private Paint mTextPaint;

    private RectF mOuterBounds;
    private RectF mBorderBounds;
    private RectF mPaddingBounds;
    private RectF mContentBounds;

    private float mTextOffsetX;
    private float mTextOffsetY;

    private PathEffect mPathEffect;

    private Rect mCachedTextBounds;


    // 动态文案
    private String mMarginLeftText;
    private String mMarginTopText;
    private String mMarginRightText;
    private String mMarginBottomText;

    private String mPaddingLeftText;
    private String mPaddingTopText;
    private String mPaddingRightText;
    private String mPaddingBottomText;

    private String mBorderLeftText;
    private String mBorderTopText;
    private String mBorderRightText;
    private String mBorderBottomText;


    private String mWidthText;
    private String mHeightText;

    private boolean isNative = false;


    public CSSBoxModelView(Context context) {
        super(context);
        init();
    }

    public CSSBoxModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CSSBoxModelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mShapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShapePaint.setStrokeWidth(ViewUtils.dp2px(getContext(),1));
        mShapePaint.setDither(true);
        mShapePaint.setColor(COLOR_TEXT);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(ViewUtils.sp2px(getContext(), 12));
        mTextPaint.setColor(COLOR_TEXT);
        mTextPaint.setTypeface(Typeface.DEFAULT);

        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        mTextOffsetY = -(metrics.descent+metrics.ascent)/2.0f;
        mTextOffsetX = (metrics.descent-metrics.ascent)/2.0f;

        mCachedTextBounds = new Rect();

        mViewMinWidth = DEFAULT_VIEW_WIDTH;
        mViewMinHeight = DEFAULT_VIEW_HEIGHT;

        mPathEffect = new DashPathEffect(new float[]{5,5,5,5},1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float width = Math.max(getWidth(),mViewMinWidth);
        float height = Math.max(getHeight(),mViewMinHeight);

        mOuterBounds = new RectF(getPaddingLeft(),getPaddingTop(),getPaddingLeft()+width,getPaddingTop()+height);
        mBorderBounds = new RectF(getPaddingLeft(),getPaddingTop(),getPaddingLeft()+width-DEFAULT_BOX_GAP,getPaddingTop()+3*height/4.0f);
        mPaddingBounds = new RectF(getPaddingLeft(),getPaddingTop(),getPaddingLeft()+width-DEFAULT_BOX_GAP*2,getPaddingTop()+height/2.0f);
        mContentBounds = new RectF(getPaddingLeft(),getPaddingTop(),getPaddingLeft()+width-DEFAULT_BOX_GAP*3,getPaddingTop()+height/4.0f);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = (int) (mViewMinWidth+getPaddingLeft()+getPaddingRight());
        int height = (int) (mViewMinHeight+getPaddingTop()+getPaddingBottom());
        setMeasuredDimension(getResolvedSize(width, widthMeasureSpec), getResolvedSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //step1 绘制背景
        mShapePaint.setStyle(Paint.Style.FILL);

        //最外层背景
        canvas.save();
        mShapePaint.setColor(COLOR_MARGIN);
        //使用clipRect避免重绘
        canvas.clipRect((mOuterBounds.width()-mBorderBounds.width())/2.0f,(mOuterBounds.height()-mBorderBounds.height())/2.0f,
                (mOuterBounds.width()+mBorderBounds.width())/2.0f,(mOuterBounds.height()+mBorderBounds.height())/2.0f, Region.Op.DIFFERENCE);
        canvas.drawRect(mOuterBounds,mShapePaint);
        canvas.restore();

        //border背景
        canvas.save();
        canvas.translate((mOuterBounds.width()-mBorderBounds.width())/2.0f,(mOuterBounds.height()-mBorderBounds.height())/2.0f);
        mShapePaint.setColor(COLOR_BORDER);
        canvas.clipRect((mBorderBounds.width()-mPaddingBounds.width())/2.0f,(mBorderBounds.height()-mPaddingBounds.height())/2.0f,
                (mBorderBounds.width()+mPaddingBounds.width())/2.0f,(mBorderBounds.height()+mPaddingBounds.height())/2.0f, Region.Op.DIFFERENCE);
        canvas.drawRect(mBorderBounds,mShapePaint);
        canvas.restore();

        //padding背景
        canvas.save();
        canvas.translate((mOuterBounds.width()-mPaddingBounds.width())/2.0f,(mOuterBounds.height()-mPaddingBounds.height())/2.0f);
        mShapePaint.setColor(COLOR_PADDING);
        canvas.clipRect((mPaddingBounds.width()-mContentBounds.width())/2.0f,(mPaddingBounds.height()-mContentBounds.height())/2.0f,
                (mPaddingBounds.width()+mContentBounds.width())/2.0f,(mPaddingBounds.height()+mContentBounds.height())/2.0f, Region.Op.DIFFERENCE);
        canvas.drawRect(mPaddingBounds,mShapePaint);
        canvas.restore();

        //content背景
        canvas.save();
        canvas.translate((mOuterBounds.width()-mContentBounds.width())/2.0f,(mOuterBounds.height()-mContentBounds.height())/2.0f);
        mShapePaint.setColor(COLOR_CONTENT);
        canvas.drawRect(mContentBounds,mShapePaint);
        canvas.restore();

        //step2 绘制框

        mShapePaint.setColor(COLOR_TEXT);
        mShapePaint.setStyle(Paint.Style.STROKE);
        mShapePaint.setPathEffect(mPathEffect);

        canvas.save();
        //最外层框
        canvas.drawRect(mOuterBounds,mShapePaint);
        //border框
        canvas.translate((mOuterBounds.width()-mBorderBounds.width())/2.0f,(mOuterBounds.height()-mBorderBounds.height())/2.0f);
        mShapePaint.setPathEffect(null);
        canvas.drawRect(mBorderBounds,mShapePaint);
        //padding框
        canvas.translate((mBorderBounds.width()-mPaddingBounds.width())/2.0f,(mBorderBounds.height()-mPaddingBounds.height())/2.0f);
        mShapePaint.setPathEffect(mPathEffect);
        canvas.drawRect(mPaddingBounds,mShapePaint);
        //content框
        canvas.translate((mPaddingBounds.width()-mContentBounds.width())/2.0f,(mPaddingBounds.height()-mContentBounds.height())/2.0f);
        mShapePaint.setPathEffect(null);
        canvas.drawRect(mContentBounds,mShapePaint);
        canvas.restore();

        //step3 绘制文案
        canvas.save();

        //最外层文案
        canvas.drawText(TEXT_MARGIN,mTextOffsetX+mOuterBounds.left,(mOuterBounds.height()-mBorderBounds.height())/4.0f+mTextOffsetY,mTextPaint);

        //margin-left
        String tempText = prepareText(mMarginLeftText,TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mOuterBounds.left+(mOuterBounds.width()-mBorderBounds.width())/4.0f-mCachedTextBounds.width()/2.0f,mOuterBounds.top+mOuterBounds.height()/2.0f+mTextOffsetY,mTextPaint);
        //margin-top
        tempText = prepareText(mMarginTopText,TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mOuterBounds.left+mOuterBounds.width()/2.0f-mCachedTextBounds.width()/2.0f,
                mOuterBounds.top+(mOuterBounds.height()-mBorderBounds.height())/4.0f+mTextOffsetY,mTextPaint);
        //margin-right
        tempText = prepareText(mMarginRightText,TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mOuterBounds.width()-(mOuterBounds.width()-mBorderBounds.width())/4.0f-mCachedTextBounds.width()/2.0f,
                mOuterBounds.top+mOuterBounds.height()/2.0f+mTextOffsetY,mTextPaint);
        //margin-bottom
        tempText = prepareText(mMarginBottomText,TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mOuterBounds.left+mOuterBounds.width()/2.0f-mCachedTextBounds.width()/2.0f,
                mOuterBounds.bottom-(mOuterBounds.height()-mBorderBounds.height())/4.0f+mTextOffsetY,mTextPaint);

        //border文案
        canvas.translate((mOuterBounds.width()-mBorderBounds.width())/2.0f,(mOuterBounds.height()-mBorderBounds.height())/2.0f);
        canvas.drawText(TEXT_BORDER,mTextOffsetX,(mBorderBounds.height()-mPaddingBounds.height())/4.0f+mTextOffsetY,mTextPaint);
        //border-left
        tempText = prepareText(mBorderLeftText,isNative?TEXT_NULL:TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mBorderBounds.left+(mBorderBounds.width()-mPaddingBounds.width())/4.0f-mCachedTextBounds.width()/2.0f,
                mBorderBounds.top+mBorderBounds.height()/2.0f+mTextOffsetY,mTextPaint);
        //border-top
        tempText = prepareText(mBorderTopText,isNative?TEXT_NULL:TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mBorderBounds.left+mBorderBounds.width()/2.0f-mCachedTextBounds.width()/2.0f,
                mBorderBounds.top+(mBorderBounds.height()-mPaddingBounds.height())/4.0f+mTextOffsetY,mTextPaint);
        //border-right
        tempText = prepareText(mBorderRightText,isNative?TEXT_NULL:TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mBorderBounds.width()-(mBorderBounds.width()-mPaddingBounds.width())/4.0f-mCachedTextBounds.width()/2.0f,
                mBorderBounds.top+mBorderBounds.height()/2.0f+mTextOffsetY,mTextPaint);
        //border-bottom
        tempText = prepareText(mBorderBottomText,isNative?TEXT_NULL:TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mBorderBounds.left+mBorderBounds.width()/2.0f-mCachedTextBounds.width()/2.0f,
                mBorderBounds.bottom-(mBorderBounds.height()-mPaddingBounds.height())/4.0f+mTextOffsetY,mTextPaint);

        //padding文案
        canvas.translate((mBorderBounds.width()-mPaddingBounds.width())/2.0f,(mBorderBounds.height()-mPaddingBounds.height())/2.0f);
        canvas.drawText(TEXT_PADDING,mTextOffsetX/2.0f,(mPaddingBounds.height()-mContentBounds.height())/4.0f+mTextOffsetY,mTextPaint);
        //padding-left
        tempText = prepareText(mPaddingLeftText,TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mPaddingBounds.left+(mPaddingBounds.width()-mContentBounds.width())/4.0f-mCachedTextBounds.width()/2.0f,
                mPaddingBounds.top+mPaddingBounds.height()/2.0f+mTextOffsetY,mTextPaint);
        //padding-top
        tempText = prepareText(mPaddingTopText,TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mPaddingBounds.left+mPaddingBounds.width()/2.0f-mCachedTextBounds.width()/2.0f,
                mPaddingBounds.top+(mPaddingBounds.height()-mContentBounds.height())/4.0f+mTextOffsetY,mTextPaint);
        //padding-right
        tempText = prepareText(mPaddingRightText,TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mPaddingBounds.width()-(mPaddingBounds.width()-mContentBounds.width())/4.0f-mCachedTextBounds.width()/2.0f,
                mPaddingBounds.top+mPaddingBounds.height()/2.0f+mTextOffsetY,mTextPaint);
        //padding-bottom
        tempText = prepareText(mPaddingBottomText,TEXT_ZERO);
        mTextPaint.getTextBounds(tempText,0,tempText.length(),mCachedTextBounds);
        canvas.drawText(tempText,mPaddingBounds.left+mPaddingBounds.width()/2.0f-mCachedTextBounds.width()/2.0f,
                mPaddingBounds.bottom-(mPaddingBounds.height()-mContentBounds.height())/4.0f+mTextOffsetY,mTextPaint);

        //content文案
        canvas.translate((mPaddingBounds.width()-mContentBounds.width())/2.0f,(mPaddingBounds.height()-mContentBounds.height())/2.0f);

        String content = prepareText(mWidthText,TEXT_UNKNOWN)+" x "+prepareText(mHeightText,TEXT_UNKNOWN);
        mTextPaint.getTextBounds(content,0,content.length(),mCachedTextBounds);
        canvas.drawText(content,mContentBounds.width()/2.0f-mCachedTextBounds.width()/2.0f,
                mContentBounds.height()/2.0f+mTextOffsetY,mTextPaint);

        canvas.restore();
    }

    private String prepareText(@Nullable String text, @NonNull String placeHolder) {
        return TextUtils.isEmpty(text) || "0".equals(text) ? placeHolder : text;
    }


    private int getResolvedSize(int desiredSize, int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int size;
        if(mode == MeasureSpec.EXACTLY){
            size = specSize;
        }else{
            size = desiredSize;
            if(mode == MeasureSpec.AT_MOST){
                size = Math.min(desiredSize,specSize);
            }
        }
        return size;
    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
    }

    public String getMarginTopText() {
        return mMarginTopText;
    }

    public void setMarginTopText(String mMarginTopText) {
        this.mMarginTopText = mMarginTopText;
    }

    public String getMarginLeftText() {
        return mMarginLeftText;
    }

    public void setMarginLeftText(String mMarginLeftText) {
        this.mMarginLeftText = mMarginLeftText;
    }

    public String getMarginRightText() {
        return mMarginRightText;
    }

    public void setMarginRightText(String mMarginRightText) {
        this.mMarginRightText = mMarginRightText;
    }

    public String getMarginBottomText() {
        return mMarginBottomText;
    }

    public void setMarginBottomText(String mMarginBottomText) {
        this.mMarginBottomText = mMarginBottomText;
    }

    public String getPaddingLeftText() {
        return mPaddingLeftText;
    }

    public void setPaddingLeftText(String mPaddingLeftText) {
        this.mPaddingLeftText = mPaddingLeftText;
    }

    public String getPaddingTopText() {
        return mPaddingTopText;
    }

    public void setPaddingTopText(String mPaddingTopText) {
        this.mPaddingTopText = mPaddingTopText;
    }

    public String getPaddingRightText() {
        return mPaddingRightText;
    }

    public void setPaddingRightText(String mPaddingRightText) {
        this.mPaddingRightText = mPaddingRightText;
    }

    public String getPaddingBottomText() {
        return mPaddingBottomText;
    }

    public void setPaddingBottomText(String mPaddingBottomText) {
        this.mPaddingBottomText = mPaddingBottomText;
    }

    public String getWidthText() {
        return mWidthText;
    }

    public void setWidthText(String mWidthText) {
        this.mWidthText = mWidthText;
    }

    public String getHeightText() {
        return mHeightText;
    }

    public void setHeightText(String mHeightText) {
        this.mHeightText = mHeightText;
    }

    public String getBorderBottomText() {
        return mBorderBottomText;
    }

    public void setBorderBottomText(String mBorderBottomText) {
        this.mBorderBottomText = mBorderBottomText;
    }

    public String getBorderRightText() {
        return mBorderRightText;
    }

    public void setBorderRightText(String mBorderRightText) {
        this.mBorderRightText = mBorderRightText;
    }

    public String getBorderTopText() {
        return mBorderTopText;
    }

    public void setBorderTopText(String mBorderTopText) {
        this.mBorderTopText = mBorderTopText;
    }

    public String getBorderLeftText() {
        return mBorderLeftText;
    }

    public void setBorderLeftText(String mBorderLeftText) {
        this.mBorderLeftText = mBorderLeftText;
    }
}

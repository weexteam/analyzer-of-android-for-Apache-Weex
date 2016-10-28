package com.taobao.weex.analyzer.core;

import android.support.annotation.Nullable;
import android.view.View;

import com.taobao.weex.analyzer.view.ScalpelFrameLayout;

/**
 * Description:
 * <p>
 * show 3d layer for native view.
 * <p>
 * inspired by https://github.com/JakeWharton/scalpel.
 * thanks Jake.
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/21<br/>
 * Time: 上午11:40<br/>
 */

public class ScalpelViewController {

    private boolean isScalpelEnabled = false;
    private boolean isDrawId = false;
    private boolean isDrawViewName = false;

    private ScalpelFrameLayout mScalpelLayout;

    private OnToggleListener mOnToggleListener;

    private ScalpelFrameLayout.OnDrawViewNameListener mOnDrawViewNameListener;

    public ScalpelViewController() {
        this(false,false,true);
    }

    /**
     * @param enabled whether display 3d layer or not by default.
     * @param isDrawId whether display view id or not.
     * @param isDrawViewName whether display view name or not.
     */
    public ScalpelViewController(boolean enabled,boolean isDrawId, boolean isDrawViewName) {
        this.isScalpelEnabled = enabled;
        this.isDrawId = isDrawId;
        this.isDrawViewName = isDrawViewName;
    }

    public void setOnToggleListener(OnToggleListener listener){
        this.mOnToggleListener = listener;
    }

    public void setOnDrawViewNameListener(ScalpelFrameLayout.OnDrawViewNameListener listener){
        this.mOnDrawViewNameListener = listener;
    }

    public View wrapView(@Nullable View view) {
        if (view == null) {
            return null;
        }
        mScalpelLayout = new ScalpelFrameLayout(view.getContext());
        mScalpelLayout.setDrawIds(isDrawId);
        mScalpelLayout.setDrawViewNames(isDrawViewName);

        if(mOnDrawViewNameListener != null){
            mScalpelLayout.setOnDrawViewNameListener(mOnDrawViewNameListener);
        }

        mScalpelLayout.addView(view);
        mScalpelLayout.setLayerInteractionEnabled(isScalpelEnabled);
        return mScalpelLayout;
    }

    public boolean isScalpelEnabled() {
        return isScalpelEnabled;
    }

    public void setScalpelEnabled(boolean enabled) {
        this.isScalpelEnabled = enabled;
        if (mScalpelLayout != null) {
            mScalpelLayout.setLayerInteractionEnabled(isScalpelEnabled);
            if(mOnToggleListener != null){
                mOnToggleListener.onToggle(mScalpelLayout, isScalpelEnabled);
            }
        }
    }


    public boolean isDrawIdEnabled(){
        return isDrawId;
    }

    public void setDrawId(boolean enabled){
        this.isDrawId = enabled;
        if(mScalpelLayout != null){
            mScalpelLayout.setDrawIds(isDrawId);
        }
    }

    public boolean isDrawViewNameEnabled(){
        return isDrawViewName;
    }

    public void setDrawViewName(boolean enabled){
        this.isDrawViewName = enabled;
        if(mScalpelLayout != null){
            mScalpelLayout.setDrawViewNames(isDrawViewName);
        }
    }


    public void toggleScalpelEnabled() {
        this.isScalpelEnabled = !isScalpelEnabled;
        if(mScalpelLayout == null){
            return;
        }
        setScalpelEnabled(isScalpelEnabled);
    }

    public interface OnToggleListener {
        void onToggle(View view, boolean isScalpelEnabled);
    }

}

package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;

import com.taobao.weex.analyzer.utils.ViewUtils;

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

    private SimpleOverlayView mSwitchView;

    private ScalpelFrameLayout mScalpelLayout;

    private OnToggleListener mOnToggleListener;

    private ScalpelFrameLayout.OnDrawViewNameListener mOnDrawViewNameListener;

    private Context mContext;

    public ScalpelViewController(Context context) {
        this(false, false, true, context);
    }

    /**
     * @param enabled        whether display 3d layer or not by default.
     * @param isDrawId       whether display view id or not.
     * @param isDrawViewName whether display view name or not.
     */
    public ScalpelViewController(boolean enabled, boolean isDrawId, boolean isDrawViewName, Context context) {
        this.isScalpelEnabled = enabled;
        this.isDrawId = isDrawId;
        this.isDrawViewName = isDrawViewName;
        this.mContext = context;

        mSwitchView = new SimpleOverlayView.Builder(mContext, "close")
                .enableDrag(false)
                .listener(new SimpleOverlayView.OnClickListener() {
                    @Override
                    public void onClick(@NonNull IOverlayView view) {
                        setScalpelEnabled(false);
                    }
                })
                .gravity(Gravity.RIGHT | Gravity.TOP)
                .y((int) ViewUtils.dp2px(mContext, 60))
                .build();
    }

    public void setOnToggleListener(OnToggleListener listener) {
        this.mOnToggleListener = listener;
    }

    public void setOnDrawViewNameListener(ScalpelFrameLayout.OnDrawViewNameListener listener) {
        this.mOnDrawViewNameListener = listener;
    }

    public View wrapView(@Nullable View view) {
        if (view == null) {
            return null;
        }
        mScalpelLayout = new ScalpelFrameLayout(view.getContext());
        mScalpelLayout.setDrawIds(isDrawId);
        mScalpelLayout.setDrawViewNames(isDrawViewName);

        if (mOnDrawViewNameListener != null) {
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
            if (enabled) {
                //show
                mSwitchView.show();
            } else {
                //dismiss
                mSwitchView.dismiss();
            }
            if (mOnToggleListener != null) {
                mOnToggleListener.onToggle(mScalpelLayout, isScalpelEnabled);
            }
        }
    }


    public boolean isDrawIdEnabled() {
        return isDrawId;
    }

    public void setDrawId(boolean enabled) {
        this.isDrawId = enabled;
        if (mScalpelLayout != null) {
            mScalpelLayout.setDrawIds(isDrawId);
        }
    }

    public boolean isDrawViewNameEnabled() {
        return isDrawViewName;
    }

    public void setDrawViewName(boolean enabled) {
        this.isDrawViewName = enabled;
        if (mScalpelLayout != null) {
            mScalpelLayout.setDrawViewNames(isDrawViewName);
        }
    }

    public void toggleScalpelEnabled() {
        this.isScalpelEnabled = !isScalpelEnabled;
        if (mScalpelLayout == null) {
            return;
        }
        setScalpelEnabled(isScalpelEnabled);
    }

    public void pause() {
        if(mSwitchView != null && isScalpelEnabled){
            mSwitchView.dismiss();
        }
    }

    public void resume(){
        if(mSwitchView != null && isScalpelEnabled){
            mSwitchView.show();
        }
    }

    public interface OnToggleListener {
        void onToggle(View view, boolean isScalpelEnabled);
    }

}

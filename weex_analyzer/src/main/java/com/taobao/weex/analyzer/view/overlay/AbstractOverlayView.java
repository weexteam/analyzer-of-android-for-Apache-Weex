package com.taobao.weex.analyzer.view.overlay;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/12<br/>
 * Time: 下午8:35<br/>
 */

public abstract class AbstractOverlayView implements IOverlayView {

    protected Context mContext;
    protected WindowManager mWindowManager;
    protected View mWholeView;
    protected boolean isViewAttached;

    protected int mGravity;
    protected int mX;
    protected int mY;

    protected int mWidth;
    protected int mHeight;



    public AbstractOverlayView(Context application) {
        mContext = application;
        mWindowManager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        mGravity = Gravity.TOP|Gravity.LEFT;
        mX = mY = 0;

        mWidth = WindowManager.LayoutParams.WRAP_CONTENT;
        mHeight = WindowManager.LayoutParams.WRAP_CONTENT;
    }



    @Override
    public boolean isViewAttached() {
        return isViewAttached;
    }

    @Override
    public void show() {
        try {
            mWholeView = onCreateView();
            onViewCreated(mWholeView);
            int w = mWidth;
            int h = mHeight;

            int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

            //no permission needed when use TYPE_TOAST.
            int type = WindowManager.LayoutParams.TYPE_TOAST;
            //since window can not received touch event before kitkat when use TYPE_TOAST,
            //so we use TYPE_PHONE instead.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            //since android7.1.1 only allow add one toast window at a time for a uid,
            // so we use TYPE_PHONE instead.
            if(Build.VERSION.SDK_INT >= 25) {
                type = WindowManager.LayoutParams.TYPE_PHONE;
            }


            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSLUCENT);
            layoutParams.gravity = mGravity;
            layoutParams.x = mX;
            layoutParams.y = mY;
            mWindowManager.addView(mWholeView, layoutParams);
            isViewAttached = true;

            onShown();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract @NonNull View onCreateView();

    protected abstract void onShown();

    protected abstract void onDismiss();

    protected void onViewCreated(@NonNull View hostView){
        //nothing
    }

    protected void onDestroy(){
        //nothing
    }

    @Override
    public void dismiss() {
        try {
            if (mWindowManager != null && mWholeView != null && isViewAttached) {
                // remove view
                mWindowManager.removeView(mWholeView);
                isViewAttached = false;
                onDismiss();
            }
            onDestroy();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

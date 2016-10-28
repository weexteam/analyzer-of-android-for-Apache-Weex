package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/13<br/>
 * Time: 上午11:14<br/>
 */
public abstract class DragSupportOverlayView extends AbstractOverlayView implements View.OnTouchListener {

    private int mCurrentX = 0;
    private int mCurrentY = 0;

    private float mDx, mDy, downX, downY;
    private static boolean hasMoved = false;

    public DragSupportOverlayView(Context application) {
        super(application);
    }


    @Override
    protected void onViewCreated(@NonNull View hostView) {
        super.onViewCreated(hostView);

        mCurrentX = mX;
        mCurrentY = mY;

        //intercept touch event
        mWholeView.setOnTouchListener(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                mDx = mCurrentX - event.getRawX();
                mDy = mCurrentY - event.getRawY();
                downX = event.getX();
                downY = event.getY();
                hasMoved = false;

                break;
            case MotionEvent.ACTION_MOVE:
                mCurrentX = (int) (event.getRawX() + mDx);
                mCurrentY = (int) (event.getRawY() + mDy);
                if (isValidMove(mContext, event.getX() - downX) || isValidMove(mContext, event.getY() - downY)) {
                    if(mWindowManager != null && mWholeView != null){
                        WindowManager.LayoutParams newParams = (WindowManager.LayoutParams) mWholeView.getLayoutParams();
                        newParams.x = mCurrentX;
                        newParams.y = mCurrentY;
                        mWindowManager.updateViewLayout(mWholeView,newParams);
                    }
                    hasMoved = true;
                }
                break;
        }

        return false;
    }

    private static boolean isValidMove(Context context, float distance) {
        return hasMoved || ViewConfiguration.get(context).getScaledTouchSlop() < Math.abs(distance);
    }

}

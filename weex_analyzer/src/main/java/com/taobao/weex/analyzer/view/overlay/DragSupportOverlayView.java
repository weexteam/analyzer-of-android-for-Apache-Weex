package com.taobao.weex.analyzer.view.overlay;

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
    private boolean isDragEnabled = true;

    public DragSupportOverlayView(Context application) {
        super(application);
    }

    public DragSupportOverlayView(Context application,boolean enableDrag){
        this(application);
        this.isDragEnabled = enableDrag;
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
        if(!isDragEnabled){
            return false;
        }
        int action = event.getAction();
        switch (action) {
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
                    updateViewPosition(mCurrentX,mCurrentY);
                    hasMoved = true;
                }
                break;
        }

        return false;
    }

    private static boolean isValidMove(Context context, float distance) {
        return hasMoved || ViewConfiguration.get(context).getScaledTouchSlop() < Math.abs(distance);
    }

    protected void updateViewPosition(int x, int y) {
        if(mWholeView == null || mWindowManager == null){
            return;
        }
        WindowManager.LayoutParams newParams = (WindowManager.LayoutParams) mWholeView.getLayoutParams();
        newParams.x = x;
        newParams.y = y;

        mWindowManager.updateViewLayout(mWholeView, newParams);
        mCurrentX = x;
        mCurrentY = y;
    }

    public void setDragEnabled(boolean enabled){
        this.isDragEnabled = enabled;
    }

}

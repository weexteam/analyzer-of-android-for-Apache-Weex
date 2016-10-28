package com.taobao.weex.analyzer.core;

import android.support.annotation.NonNull;
import android.view.View;

import com.taobao.weex.analyzer.view.IOverlayView;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/18<br/>
 * Time: 下午1:13<br/>
 */

public abstract class AbstractLoopTask implements IOverlayView.ITask, Runnable {

    private boolean isStop = true;
    protected View mHostView;

    private static final int DEFAULT_DELAY_MILLIS = 500;

    protected int mDelayMillis;

    public AbstractLoopTask(@NonNull View hostView) {
        this.mHostView = hostView;
        mDelayMillis = DEFAULT_DELAY_MILLIS;
    }

    @Override
    public void run() {
        if (isStop) {
            return;
        }
        onRun();
        mHostView.postDelayed(this, mDelayMillis);
    }

    @Override
    public void start() {
        if (!isStop) {
            stop();
        }
        isStop = false;
        onStart();
        mHostView.post(this);
    }

    @Override
    public void stop() {
        isStop = true;
        onStop();
    }

    protected void onStart() {
        //none
    }

    protected abstract void onRun();

    protected abstract void onStop();
}
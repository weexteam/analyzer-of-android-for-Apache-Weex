package com.taobao.weex.analyzer.core;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.taobao.weex.analyzer.view.overlay.IOverlayView;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/18<br/>
 * Time: 下午1:13<br/>
 */

public abstract class AbstractLoopTask implements IOverlayView.ITask, Runnable {

    private boolean isStop = true;

    private static final int DEFAULT_DELAY_MILLIS = 500;//ms

    protected int mDelayMillis;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    private HandlerThreadWrapper mHandlerThreadWrapper;

    private final boolean isRunInMainThread;


    public AbstractLoopTask(boolean runInMainThread) {
        mDelayMillis = DEFAULT_DELAY_MILLIS;
        this.isRunInMainThread = runInMainThread;
    }

    public AbstractLoopTask(boolean runInMainThread,int delayMillis){
        this.isRunInMainThread = runInMainThread;
        this.mDelayMillis = delayMillis;
    }

    @SuppressWarnings("unused")
    public void setDelayInMillis(int millis) {
        this.mDelayMillis = millis;
    }

    @SuppressWarnings("unused")
    public int getDelayInMillis() {
        return this.mDelayMillis;
    }

    @Override
    public void run() {
        if (isStop) {
            return;
        }
        try {
            onRun();
        }catch (Exception e){
            e.printStackTrace();
        }
        if (isRunInMainThread) {
            mUIHandler.postDelayed(this, mDelayMillis);
        } else {
            if (mHandlerThreadWrapper != null && mHandlerThreadWrapper.isAlive()) {
                mHandlerThreadWrapper.getHandler().postDelayed(this, mDelayMillis);
            }
        }
    }

    @Override
    public void start() {
        if (!isStop) {
            stop();
        }
        isStop = false;
        try {
            onStart();
        }catch (Exception e){
            e.printStackTrace();
        }

        if (isRunInMainThread) {
            mUIHandler.post(this);
        } else {
            if (mHandlerThreadWrapper == null) {
                mHandlerThreadWrapper = new HandlerThreadWrapper("wx-analyzer-" + this.getClass().getSimpleName());
            } else {
                if (!mHandlerThreadWrapper.isAlive()) {
                    mHandlerThreadWrapper = new HandlerThreadWrapper("wx-analyzer-" + this.getClass().getSimpleName());
                } else {
                    mHandlerThreadWrapper.getHandler().removeCallbacksAndMessages(null);
                }
            }
            mHandlerThreadWrapper.getHandler().post(this);
        }
    }

    @Override
    public void stop() {
        isStop = true;
        onStop();
        if (mHandlerThreadWrapper != null) {
            mHandlerThreadWrapper.quit();
            mHandlerThreadWrapper = null;
        }
        mUIHandler.removeCallbacksAndMessages(null);
    }

    protected void onStart() {
        //none
    }

    protected abstract void onRun();

    protected abstract void onStop();

    protected void runOnUIThread(@NonNull final Runnable runnable) {
        if(mUIHandler != null){
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
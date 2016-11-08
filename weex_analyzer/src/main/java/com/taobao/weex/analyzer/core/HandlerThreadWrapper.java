package com.taobao.weex.analyzer.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/7<br/>
 * Time: 下午8:07<br/>
 */

public class HandlerThreadWrapper {
    private Handler mHandler;

    private HandlerThread mHandlerThread;

    public HandlerThreadWrapper(@NonNull String threadName) {
        mHandlerThread = new HandlerThread(threadName);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public @NonNull Handler getHandler() {
        return this.mHandler;
    }

    public boolean isAlive(){
        if(mHandlerThread == null){
            return false;
        }
        return mHandlerThread.isAlive();
    }

    public void quit() {
        if(mHandlerThread != null){
            if(mHandler != null){
                mHandler.removeCallbacksAndMessages(null);
            }
            mHandlerThread.quit();
        }
    }
}

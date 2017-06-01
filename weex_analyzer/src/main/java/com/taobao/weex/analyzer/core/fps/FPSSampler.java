package com.taobao.weex.analyzer.core.fps;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Choreographer;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/9<br/>
 * Time: 上午9:56<br/>
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class FPSSampler implements Choreographer.FrameCallback {
    private Choreographer mChoreographer;

    private long mFirstFrameTime = -1;
    private long mLastFrameTime = -1;

    private int mNumFrameCallbacks = 0;

    private boolean bShouldStop = false;

    private static final float DEVICE_REFRESH_RATE_IN_MS = 16.67F;

    public FPSSampler(@NonNull Choreographer choreographer) {
        this.mChoreographer = choreographer;
    }

    public void start() {
        bShouldStop = false;
        mChoreographer.postFrameCallback(this);
    }

    public void stop() {
        bShouldStop = true;
        mChoreographer.removeFrameCallback(this);
    }

    public void reset() {
        mFirstFrameTime = -1;
        mLastFrameTime = -1;
        mNumFrameCallbacks = 0;
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if(bShouldStop){
            return;
        }
        long currentTimeMillis = frameTimeNanos;
        if(mFirstFrameTime == -1){
            mFirstFrameTime = currentTimeMillis;
        }else{
            mNumFrameCallbacks++;
        }

        mLastFrameTime = currentTimeMillis;

        //loop
        mChoreographer.postFrameCallback(this);
    }

    public int getExpectedNumFrames() {
        double totalTimeMillis = (int) ((double) mLastFrameTime - mFirstFrameTime) / 1000000;
        return (int) (totalTimeMillis/DEVICE_REFRESH_RATE_IN_MS);
    }

    public int getNumFrames(){
        return mNumFrameCallbacks;
    }

    public double getFPS(){
        if(mLastFrameTime == mFirstFrameTime){
            return 0;
        }
        return ((double) (getNumFrames()) * 1e9) / (mLastFrameTime - mFirstFrameTime);
    }


    public static boolean isSupported(){
        return Build.VERSION.SDK_INT >= 16;
    }

}

package com.taobao.weex.analyzer.core.fps;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Choreographer;

import com.taobao.weex.analyzer.core.TaskEntity;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class FpsTaskEntity implements TaskEntity<Double> {

    private FPSSampler mFpsChecker;

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onTaskInit() {
        this.mFpsChecker = new FPSSampler(Choreographer.getInstance());
        mFpsChecker.reset();
        mFpsChecker.start();
    }

    @NonNull
    @Override
    public Double onTaskRun() {
        if(mFpsChecker == null) {
            onTaskInit();
        }
        Double fps = mFpsChecker.getFPS();
        mFpsChecker.reset();
        return fps;
    }

    @Override
    public void onTaskStop() {
        mFpsChecker.stop();
        mFpsChecker = null;
    }
}

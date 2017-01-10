package com.taobao.weex.analyzer.core;

import android.support.annotation.NonNull;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class FpsTaskEntity implements TaskEntity<Double> {

    private FPSSampler mFpsChecker;

    @Override
    public void onTaskInit() {
        this.mFpsChecker = new FPSSampler();
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

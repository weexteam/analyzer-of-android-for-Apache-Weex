package com.taobao.weex.analyzer.core;

import android.support.annotation.NonNull;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class MemoryTaskEntity implements TaskEntity<Double> {

    @Override
    public void onTaskInit() {
    }

    @NonNull
    @Override
    public Double onTaskRun() {
        return MemorySampler.getMemoryUsage();
    }

    @Override
    public void onTaskStop() {
    }
}

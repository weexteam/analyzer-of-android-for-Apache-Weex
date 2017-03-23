package com.taobao.weex.analyzer.core.reporter;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.utils.WXLogUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public abstract class ReportSupportLoopTask<T> extends AbstractLoopTask {

    private IDataReporter<T> mDataReporter;
    private AtomicInteger mSequenceId = new AtomicInteger(0);

    private static final String TAG = "Reporter";

    public ReportSupportLoopTask(boolean runInMainThread) {
        super(runInMainThread);
        this.mDataReporter = newDataReporter();
    }

    public ReportSupportLoopTask(boolean runInMainThread, int delayMillis) {
        super(runInMainThread, delayMillis);
        this.mDataReporter = newDataReporter();
    }

    @Nullable
    protected IDataReporter<T> newDataReporter() {
        String from = LaunchConfig.getFrom();
        String deviceId = LaunchConfig.getDeviceId();

        if (!TextUtils.isEmpty(from) && !TextUtils.isEmpty(deviceId)) {
            return MDSDataReporterFactory.create(from, deviceId);
        } else {
            return null;
        }
    }

    protected void reportIfNeeded(@Nullable IDataReporter.ProcessedData<T> data) {
        if(data == null || mDataReporter == null) {
            return;
        }
        try {
            if(mDataReporter.isEnabled()) {
                mDataReporter.report(data);
            }
        }catch (Exception e) {
            WXLogUtils.e(TAG,e.getMessage());
        }
    }

    protected int getSequenceId() {
        return mSequenceId.get();
    }

    protected int generateSequenceId() {
        return mSequenceId.getAndIncrement();
    }

    @Override
    @CallSuper
    protected void onStop() {
        mDataReporter = null;
        mSequenceId.getAndSet(0);
    }
}

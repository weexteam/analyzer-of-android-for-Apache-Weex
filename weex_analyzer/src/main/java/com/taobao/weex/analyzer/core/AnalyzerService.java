package com.taobao.weex.analyzer.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.taobao.weex.analyzer.utils.SDKUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class AnalyzerService extends Service {

    private TaskImpl mTask;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LaunchAnalyzerReceiver.TAG, "service start success");
        if (mTask == null) {
            createTask();
        }
        mTask.start();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTask != null) {
            mTask.stop();
        }
        Log.d(LaunchAnalyzerReceiver.TAG, "service is destroyed");
    }

    private void createTask() {
        mTask = new TaskImpl(this, 1000);
    }

    private static class TaskImpl extends AbstractLoopTask {
        private WeakReference<Context> mHostRef;

        private List<TaskEntity> mTaskEntities;

        TaskImpl(@NonNull Context context, int delayMillis) {
            super(false, delayMillis);
            mHostRef = new WeakReference<>(context);

            mTaskEntities = new ArrayList<>();
            mTaskEntities.add(new CpuTaskEntity());
            mTaskEntities.add(new TrafficTaskEntity(delayMillis));
            mTaskEntities.add(new MemoryTaskEntity());
            mTaskEntities.add(new FpsTaskEntity());

        }

        @Override
        protected void onStart() {
            if (mTaskEntities != null && !mTaskEntities.isEmpty()) {
                for (TaskEntity entity : mTaskEntities) {
                    entity.onTaskInit();
                }
            }
        }

        @Override
        protected void onRun() {
            if (((mHostRef.get() != null) && !SDKUtils.isHostRunning(mHostRef.get())) ||
                    ((mHostRef.get() != null) && !SDKUtils.isInteractive(mHostRef.get()))) {
                Log.d(LaunchAnalyzerReceiver.TAG, "service is stopped because we are in background or killed");
                ((Service) mHostRef.get()).stopSelf();
                return;
            }

            CpuTaskEntity.CpuInfo cpuInfo = null;
            TrafficTaskEntity.TrafficInfo trafficInfo = null;
            double memoryUsage = 0;
            double fps = 0;

            if (mTaskEntities != null && !mTaskEntities.isEmpty()) {
                for (TaskEntity entity : mTaskEntities) {
                    if (entity instanceof CpuTaskEntity) {
                        cpuInfo = (CpuTaskEntity.CpuInfo) entity.onTaskRun();
                    } else if (entity instanceof TrafficTaskEntity) {
                        trafficInfo = (TrafficTaskEntity.TrafficInfo) entity.onTaskRun();
                    } else if (entity instanceof MemoryTaskEntity) {
                        memoryUsage = (double) entity.onTaskRun();
                    } else if (entity instanceof FpsTaskEntity) {
                        fps = (double) entity.onTaskRun();
                    }
                }
            }

            if (cpuInfo != null) {
                Log.d(LaunchAnalyzerReceiver.TAG, "cpu usage(total :" + String.format("%.2f", cpuInfo.pidCpuUsage) +
                        "% user : " + String.format("%.2f", cpuInfo.pidUserCpuUsage) + "% kernel : " + String.format("%.2f", cpuInfo.pidKernelCpuUsage) + "%)\r\n");
            }
            Log.d(LaunchAnalyzerReceiver.TAG, "memory usage : " + String.format("%.2f", memoryUsage) + "MB\r\n");
            Log.d(LaunchAnalyzerReceiver.TAG, "fps : " + String.format("%.2f", fps) + "\r\n");

            if (trafficInfo != null) {
                Log.d(LaunchAnalyzerReceiver.TAG, "traffic speed(rx :" +
                        String.format("%.2f", trafficInfo.rxSpeed) + "kb/s tx : " + String.format("%.2f", trafficInfo.txSpeed) + "kb/s)\r\n\r\n");
            }
        }

        @Override
        protected void onStop() {
            if (mTaskEntities != null && !mTaskEntities.isEmpty()) {
                for (TaskEntity entity : mTaskEntities) {
                    entity.onTaskStop();
                }
            }
        }
    }

}
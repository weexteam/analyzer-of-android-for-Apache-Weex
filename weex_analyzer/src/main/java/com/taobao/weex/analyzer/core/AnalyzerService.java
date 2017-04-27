package com.taobao.weex.analyzer.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.core.reporter.DataReporterFactory;
import com.taobao.weex.analyzer.core.reporter.IDataReporter;
import com.taobao.weex.analyzer.core.reporter.LogReporter;
import com.taobao.weex.analyzer.utils.SDKUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class AnalyzerService extends Service {

    private TaskImpl mTask;

    @Nullable
    private IDataReporter reporter;

    public static final String ATS = "ats";
    public static final String MDS = "mds";

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
        Log.d(Constants.TAG, "service start success");
        String from = intent.getStringExtra("from");
        if (mTask == null) {
            createTask();
        }
        if(ATS.equals(from)) {
            reporter = DataReporterFactory.createLogReporter(true);
        } else if(MDS.equals(from)) {
            //TODO
        }
        mTask.setReporter(reporter);
        mTask.start();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTask != null) {
            mTask.stop();
        }
        Log.d(Constants.TAG, "service is destroyed");
    }

    private void createTask() {
        mTask = new TaskImpl(this, 1000, reporter);
    }

    private static class TaskImpl extends AbstractLoopTask {
        private WeakReference<Context> mHostRef;

        private List<TaskEntity> mTaskEntities;
        @Nullable
        private IDataReporter mReporter;

        TaskImpl(@NonNull Context context, int delayMillis,@Nullable IDataReporter reporter) {
            super(false, delayMillis);
            mHostRef = new WeakReference<>(context);

            mTaskEntities = new ArrayList<>();
            mTaskEntities.add(new CpuTaskEntity());
            mTaskEntities.add(new TrafficTaskEntity(delayMillis));
            mTaskEntities.add(new MemoryTaskEntity());
            if(FPSSampler.isSupported()) {
                mTaskEntities.add(new FpsTaskEntity());
            }
            this.mReporter = reporter;

        }

        void setReporter(@Nullable IDataReporter reporter) {
            this.mReporter = reporter;
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
                Log.d(Constants.TAG, "service is stopped because we are in background or killed");
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

            if(mReporter == null || !mReporter.isEnabled()) {
                return;
            }

            if(mReporter instanceof LogReporter) {
                if (cpuInfo != null) {
                    String cpuStr = "cpu usage(total :" + String.format(Locale.CHINA,"%.2f", cpuInfo.pidCpuUsage) +
                            "% user : " + String.format(Locale.CHINA,"%.2f", cpuInfo.pidUserCpuUsage) + "% kernel : " + String.format(Locale.CHINA,"%.2f", cpuInfo.pidKernelCpuUsage) + "%)\r\n";
                    ((LogReporter) mReporter).report(new IDataReporter.ProcessedDataBuilder<String>().type(Config.TYPE_CPU).data(cpuStr).build());
                }

                String memStr = "memory usage : " + String.format(Locale.CHINA,"%.2f", memoryUsage) + "MB\r\n";
                ((LogReporter) mReporter).report(new IDataReporter.ProcessedDataBuilder<String>().type(Config.TYPE_MEMORY).data(memStr).build());

                String fpsStr = "fps : " + String.format(Locale.CHINA,"%.2f", fps) + "\r\n";
                ((LogReporter) mReporter).report(new IDataReporter.ProcessedDataBuilder<String>().type(Config.TYPE_FPS).data(fpsStr).build());


                if (trafficInfo != null) {
                    String trafficStr = "traffic speed(rx :" +
                            String.format(Locale.CHINA,"%.2f", trafficInfo.rxSpeed) + "kb/s tx : " + String.format(Locale.CHINA,"%.2f", trafficInfo.txSpeed) + "kb/s)\r\n\r\n";
                    ((LogReporter) mReporter).report(new IDataReporter.ProcessedDataBuilder<String>().type(Config.TYPE_TRAFFIC).data(trafficStr).build());
                }
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
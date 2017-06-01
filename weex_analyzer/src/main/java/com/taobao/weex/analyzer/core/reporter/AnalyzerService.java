package com.taobao.weex.analyzer.core.reporter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.WeexDevOptions;
import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.core.Constants;
import com.taobao.weex.analyzer.core.cpu.CpuTaskEntity;
import com.taobao.weex.analyzer.core.fps.FPSSampler;
import com.taobao.weex.analyzer.core.fps.FpsTaskEntity;
import com.taobao.weex.analyzer.core.memory.MemoryTaskEntity;
import com.taobao.weex.analyzer.core.inspector.network.NetworkEventInspector;
import com.taobao.weex.analyzer.core.weex.Performance;
import com.taobao.weex.analyzer.core.TaskEntity;
import com.taobao.weex.analyzer.core.traffic.TrafficTaskEntity;
import com.taobao.weex.analyzer.core.lint.RemoteVDomMonitor;
import com.taobao.weex.analyzer.core.reporter.ws.IWebSocketBridge;
import com.taobao.weex.analyzer.core.reporter.ws.WebSocketClient;
import com.taobao.weex.analyzer.pojo.HealthReport;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.utils.WXLogUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class AnalyzerService extends Service implements WebSocketClient.Callback, IWebSocketBridge {

    private TaskImpl mTask;

    @Nullable
    private IDataReporter reporter;

    private WSConfig config;

    public static final String ATS = "ats";
    public static final String MDS = "mds";

    private DispatchReceiver mDispatchReceiver;

    private NetworkEventInspector mInspector;

    public static final String ACTION_DISPATCH = "cmd.dispatch";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createTask();
        mDispatchReceiver = new DispatchReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mDispatchReceiver,new IntentFilter(ACTION_DISPATCH));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Constants.TAG, "service start success");
        String from = intent.getStringExtra("from");
        if (mTask == null) {
            createTask();
        }
        if (ATS.equals(from)) {
            reporter = DataReporterFactory.createLogReporter(true);
        } else if (MDS.equals(from)) {

            final String id = intent.getStringExtra(WeexDevOptions.EXTRA_DEVICE_ID);
            String wsUrl = intent.getStringExtra(WeexDevOptions.EXTRA_WS_URL);

            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(wsUrl)) {
                reporter = DataReporterFactory.createWSReporter(MDS, id, wsUrl ,this, this);
                mTask.setDeviceId(id);

                if(mDispatchReceiver != null) {
                    mDispatchReceiver.setup((WebSocketReporter) reporter,id);
                }

                if(mInspector != null) {
                    mInspector.destroy();
                }
                mInspector = NetworkEventInspector.createInstance(this, new NetworkEventInspector.OnMessageReceivedListener() {
                    @Override
                    public void onMessageReceived(NetworkEventInspector.MessageBean msg) {
                        if(config == null || !config.isNetworkInspectorEnabled) {
                            return;
                        }
                        if (reporter != null && msg != null && reporter.isEnabled() && reporter instanceof WebSocketReporter) {
                            ((WebSocketReporter)reporter).report(new IDataReporter.ProcessedDataBuilder<NetworkEventInspector.MessageBean>()
//                                    .sequenceId(mCounter.getAndIncrement())
                                    .data(msg)
                                    .deviceId(id)
                                    .type(Config.TYPE_MTOP_INSPECTOR)
                                    .build()
                            );
                        }
                    }
                });
            }
        }
        config = new WSConfig();
        mTask.setReporter(reporter);
        mTask.setConfig(config);
        if(mDispatchReceiver != null) {
            mDispatchReceiver.setConfig(config);
        }
        mTask.start();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTask != null) {
            mTask.stop();
            mTask.setReporter(null);
        }
        if (reporter != null && reporter instanceof WebSocketReporter) {
            ((WebSocketReporter) reporter).close(-1, "close");
        }
        Log.d(Constants.TAG, "service is destroyed");
        if(mDispatchReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mDispatchReceiver);
        }
        if(mInspector != null) {
            mInspector.destroy();
        }
    }

    private void createTask() {
        mTask = new TaskImpl(this, 1000, reporter);
    }

    @Override
    public void onOpen(String response) {
        WXLogUtils.d(Constants.TAG,"onOpen:"+response);
    }

    @Override
    public void onFailure(Throwable cause) {
        Log.d(Constants.TAG, this + " service is stopped because of bad webSocket:"+cause.getMessage());
    }

    @Override
    public void onClose(int code, String reason) {
    }

    @Override
    public void handleMessage(String message) {
        if (TextUtils.isEmpty(message) || config == null) {
            return;
        }

        try {
            WSMessage wsMessage = JSON.parseObject(message, WSMessage.class);
            if (wsMessage == null) {
                return;
            }

            if("switcher".equals(wsMessage.type)) {
                List<String> switchers = wsMessage.switchers;
                String action = wsMessage.action;
                for (String switcher : switchers) {
                    updateConfig(config, switcher, "open".equals(action));
                }
            }
        } catch (Exception e) {
            WXLogUtils.e(Constants.TAG, e.getMessage());
        }
    }

    private void updateConfig(WSConfig config, String type, boolean status) {
        WXLogUtils.d(Constants.TAG,"config>>>>type:"+type+",status:"+status);
        if (Config.TYPE_MEMORY.equals(type)) {
            config.isMemoryEnabled = status;
        } else if (Config.TYPE_CPU.equals(type)) {
            config.isCPUEnabled = status;
        } else if (Config.TYPE_FPS.equals(type)) {
            config.isFPSEnabled = status;
        } else if (Config.TYPE_TRAFFIC.equals(type)) {
            config.isTrafficEnabled = status;
        } else if (Config.TYPE_WEEX_PERFORMANCE_STATISTICS.equals(type)) {
            config.isPerformanceEnabled = status;
        } else if (Config.TYPE_RENDER_ANALYSIS.equals(type)) {
            config.isRenderAnalysisEnabled = status;
            Intent intent = new Intent(RemoteVDomMonitor.ACTION_SHOULD_MONITOR);
            intent.putExtra(RemoteVDomMonitor.EXTRA_MONITOR,config.isRenderAnalysisEnabled);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } else if (Config.TYPE_MTOP_INSPECTOR.equals(type)) {
            config.isNetworkInspectorEnabled = status;
        }
    }

    public static class WSMessage {
        public String type;
        public String action;
        public List<String> switchers;
    }

    public static class LifecycleEvent {
        public String type;//lifecycle
        public String status;
        public String pageName;
        public LifecycleEvent(String type, String status, String pageName) {
            this.type = type;
            this.status = status;
            this.pageName = pageName;
        }
    }

    public static class WSConfig {
        boolean isCPUEnabled;
        boolean isMemoryEnabled;
        boolean isFPSEnabled;
        boolean isTrafficEnabled;
        boolean isPerformanceEnabled;
        boolean isNetworkInspectorEnabled;
        boolean isRenderAnalysisEnabled;
    }


    private static class DispatchReceiver extends BroadcastReceiver {

        private WebSocketReporter reporter;
        private String deviceId;
        private WSConfig config;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(!intent.getAction().equals(ACTION_DISPATCH)) {
                return;
            }

            if(reporter == null || !reporter.isEnabled() || config == null) {
                return;
            }

            String type = intent.getStringExtra("type");

            if(Config.TYPE_WEEX_PERFORMANCE_STATISTICS.equals(type)) {
                String weex_performance = intent.getStringExtra(Config.TYPE_WEEX_PERFORMANCE_STATISTICS);
                if(!TextUtils.isEmpty(weex_performance) && config.isPerformanceEnabled) {
                    Performance performance = JSON.parseObject(weex_performance,Performance.class);
                    reporter.report(new IDataReporter.ProcessedDataBuilder<Performance>()
                            .deviceId(deviceId)
                            .data(performance)
                            .type(Config.TYPE_WEEX_PERFORMANCE_STATISTICS)
                            .build()
                    );
                }
            } else if("lifecycle".equals(type)) {
                String status = intent.getStringExtra("status");
                String pageName = intent.getStringExtra("pageName");//可能为null
                if(!TextUtils.isEmpty(status)) {
                    LifecycleEvent event = new LifecycleEvent(type,status,pageName);
                    reporter.report(new IDataReporter.ProcessedDataBuilder<LifecycleEvent>()
                            .deviceId(deviceId)
                            .data(event)
                            .type("lifecycle")
                            .build()
                    );
                }
            } else if(Config.TYPE_RENDER_ANALYSIS.equals(type)) {
                String healthReport = intent.getStringExtra(Config.TYPE_RENDER_ANALYSIS);
                if(!TextUtils.isEmpty(healthReport) && config.isRenderAnalysisEnabled) {
                    HealthReport report = JSON.parseObject(healthReport,HealthReport.class);
                    reporter.report(new IDataReporter.ProcessedDataBuilder<HealthReport>()
                            .deviceId(deviceId)
                            .data(report)
                            .type(Config.TYPE_RENDER_ANALYSIS)
                            .build()
                    );
                }
            }

        }

        void setup(WebSocketReporter reporter, String deviceId) {
            this.reporter = reporter;
            this.deviceId = deviceId;
        }

        void setConfig(WSConfig config) {
            this.config = config;
        }
    }

    private static class TaskImpl extends AbstractLoopTask {
        private WeakReference<Context> mHostRef;

        private List<TaskEntity> mTaskEntities;
        @Nullable
        private IDataReporter mReporter;

        private WSConfig mConfig;

        private String deviceId;

        private AtomicInteger mSequenceId = new AtomicInteger(0);


        TaskImpl(@NonNull Context context, int delayMillis, @Nullable IDataReporter reporter) {
            super(false, delayMillis);
            mHostRef = new WeakReference<>(context);

            mTaskEntities = new ArrayList<>();
            mTaskEntities.add(new CpuTaskEntity());
            mTaskEntities.add(new TrafficTaskEntity(delayMillis));
            mTaskEntities.add(new MemoryTaskEntity());
            if (FPSSampler.isSupported()) {
                mTaskEntities.add(new FpsTaskEntity());
            }
            this.mReporter = reporter;

        }

        void setReporter(@Nullable IDataReporter reporter) {
            this.mReporter = reporter;
        }

        void setConfig(@NonNull WSConfig config) {
            this.mConfig = config;
        }

        void setDeviceId(@NonNull String deviceId) {
            this.deviceId = deviceId;
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

            if (mReporter == null || !mReporter.isEnabled()) {
                return;
            }

            if (mReporter instanceof LogReporter) {
                analyzeForATS((LogReporter) mReporter);
            } else if (mReporter instanceof WebSocketReporter) {
                WebSocketReporter realReporter = (WebSocketReporter) mReporter;
                if(mConfig != null) {
                    analyzeForMDS(realReporter,mConfig);
                } else {
                    WXLogUtils.e(Constants.TAG,"config is null");
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

        private void analyzeForMDS(@NonNull WebSocketReporter reporter, @NonNull WSConfig config) {
            if (mTaskEntities != null && !mTaskEntities.isEmpty()) {
                for (TaskEntity entity : mTaskEntities) {
                    if (entity instanceof CpuTaskEntity && config.isCPUEnabled) {
                        CpuTaskEntity.CpuInfo cpuInfo = (CpuTaskEntity.CpuInfo) entity.onTaskRun();
                        reporter.report(new IDataReporter.ProcessedDataBuilder<Double>()
                                .type(Config.TYPE_CPU)
                                .deviceId(deviceId)
                                .data((Math.round(cpuInfo.pidCpuUsage*100)/100.0))
                                .sequenceId(generateSequenceId())
                                .build());
                    } else if (entity instanceof TrafficTaskEntity && config.isTrafficEnabled) {
                        TrafficTaskEntity.TrafficInfo trafficInfo = (TrafficTaskEntity.TrafficInfo) entity.onTaskRun();
                        reporter.report(new IDataReporter.ProcessedDataBuilder<TrafficTaskEntity.TrafficInfo>()
                                .type(Config.TYPE_TRAFFIC)
                                .deviceId(deviceId)
                                .data(trafficInfo)
                                .sequenceId(generateSequenceId())
                                .build());
                    } else if (entity instanceof MemoryTaskEntity && config.isMemoryEnabled) {
                        double memoryUsage = (double) entity.onTaskRun();
                        reporter.report(new IDataReporter.ProcessedDataBuilder<Double>()
                                .type(Config.TYPE_MEMORY)
                                .deviceId(deviceId)
                                .data(memoryUsage)
                                .sequenceId(generateSequenceId())
                                .build());
                    } else if (entity instanceof FpsTaskEntity && config.isFPSEnabled) {
                        double fps = (double) entity.onTaskRun();
                        reporter.report(new IDataReporter.ProcessedDataBuilder<Double>()
                                .type(Config.TYPE_FPS)
                                .deviceId(deviceId)
                                .data(fps)
                                .sequenceId(generateSequenceId())
                                .build());
                    }
                }
            }
        }

        private void analyzeForATS(@NonNull LogReporter reporter) {
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
                String cpuStr = "cpu usage(total :" + String.format(Locale.CHINA, "%.2f", cpuInfo.pidCpuUsage) +
                        "% user : " + String.format(Locale.CHINA, "%.2f", cpuInfo.pidUserCpuUsage) + "% kernel : " + String.format(Locale.CHINA, "%.2f", cpuInfo.pidKernelCpuUsage) + "%)\r\n";
                reporter.report(new IDataReporter.ProcessedDataBuilder<String>().type(Config.TYPE_CPU).data(cpuStr).build());
            }

            String memStr = "memory usage : " + String.format(Locale.CHINA, "%.2f", memoryUsage) + "MB\r\n";
            reporter.report(new IDataReporter.ProcessedDataBuilder<String>().type(Config.TYPE_MEMORY).data(memStr).build());

            String fpsStr = "fps : " + String.format(Locale.CHINA, "%.2f", fps) + "\r\n";
            reporter.report(new IDataReporter.ProcessedDataBuilder<String>().type(Config.TYPE_FPS).data(fpsStr).build());


            if (trafficInfo != null) {
                String trafficStr = "traffic speed(rx :" +
                        String.format(Locale.CHINA, "%.2f", trafficInfo.rxSpeed) + "kb/s tx : " + String.format(Locale.CHINA, "%.2f", trafficInfo.txSpeed) + "kb/s)\r\n\r\n";
                reporter.report(new IDataReporter.ProcessedDataBuilder<String>().type(Config.TYPE_TRAFFIC).data(trafficStr).build());
            }
        }

        private int generateSequenceId() {
            return mSequenceId.getAndIncrement();
        }

    }

}
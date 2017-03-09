package com.taobao.weex.analyzer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.core.DevOptionsConfig;
import com.taobao.weex.analyzer.core.FPSSampler;
import com.taobao.weex.analyzer.core.JSExceptionCatcher;
import com.taobao.weex.analyzer.core.Performance;
import com.taobao.weex.analyzer.core.PollingVDomMonitor;
import com.taobao.weex.analyzer.core.RemoteDebugManager;
import com.taobao.weex.analyzer.core.ShakeDetector;
import com.taobao.weex.analyzer.core.StandardVDomMonitor;
import com.taobao.weex.analyzer.core.VDomController;
import com.taobao.weex.analyzer.core.WXPerfStorage;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.analyzer.view.CpuSampleView;
import com.taobao.weex.analyzer.view.DevOption;
import com.taobao.weex.analyzer.view.EntranceView;
import com.taobao.weex.analyzer.view.FpsSampleView;
import com.taobao.weex.analyzer.view.IOverlayView;
import com.taobao.weex.analyzer.view.IResizableView;
import com.taobao.weex.analyzer.view.InspectorView;
import com.taobao.weex.analyzer.view.LogView;
import com.taobao.weex.analyzer.view.MemorySampleView;
import com.taobao.weex.analyzer.view.NetworkInspectorView;
import com.taobao.weex.analyzer.view.PerfSampleOverlayView;
import com.taobao.weex.analyzer.view.ScalpelFrameLayout;
import com.taobao.weex.analyzer.view.ScalpelViewController;
import com.taobao.weex.analyzer.view.SettingsActivity;
import com.taobao.weex.analyzer.view.StorageView;
import com.taobao.weex.analyzer.view.TrafficSampleView;
import com.taobao.weex.analyzer.view.ProfileDomView;
import com.taobao.weex.analyzer.view.WXPerformanceAnalysisView;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <p> Created by rowandjj(chuyi)<br/> Date: 2016/11/5<br/> Time: 下午3:25<br/>
 */

public class WeexDevOptions implements IWXDevOptions {
    private Context mContext;

    private ShakeDetector mShakeDetector;
    private DevOptionsConfig mConfig;

    private LogView mLogView;

    private MemorySampleView mMemorySampleView;
    private CpuSampleView mCpuSampleView;
    private FpsSampleView mFpsSampleView;
    private TrafficSampleView mTrafficSampleView;

    private String mCurPageName;

    private ScalpelViewController mScalpelViewController;
    private PerfSampleOverlayView mPerfMonitorOverlayView;
    private ProfileDomView mProfileDomView;
    private InspectorView mInspectorView;

    private NetworkInspectorView mNetworkInspectorView;

    private boolean shown = false;
    private List<DevOption> mExtraOptions = null;

    private VDomController mVdomController;

    private WXSDKInstance mInstance;

    private static final String ACTION_LAUNCH = "action_launch_analyzer";
    public static final String EXTRA_FROM = "from";

    private LaunchUIReceiver mLaunchUIReceiver;


    public WeexDevOptions(@NonNull Context context) {

        this.mContext = context;

        mConfig = new DevOptionsConfig(context);
        mPerfMonitorOverlayView = new PerfSampleOverlayView(context);
        mProfileDomView = new ProfileDomView(context);
        mProfileDomView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    mConfig.setVdomDepthEnabled(false);
                }
            }
        });

        mNetworkInspectorView = new NetworkInspectorView(context);
        mNetworkInspectorView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    mConfig.setNetworkInspectorEnabled(false);
                }
            }
        });

        mNetworkInspectorView.setOnSizeChangedListener(new IResizableView.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(@IResizableView.Size int size) {
                mConfig.setNetworkInspectorViewSize(size);
            }
        });

        mLogView = new LogView(context);
        mLogView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    mConfig.setLogOutputEnabled(false);
                }
            }
        });

        mLogView.setOnLogConfigChangedListener(new LogView.OnLogConfigChangedListener() {
            @Override
            public void onLogLevelChanged(int level) {
                mConfig.setLogLevel(level);
            }

            @Override
            public void onLogFilterChanged(String filterName) {
                mConfig.setLogFilter(filterName);
            }
        });

        mLogView.setOnSizeChangedListener(new IResizableView.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(@IResizableView.Size int size) {
                mConfig.setLogViewSize(size);
            }
        });


        mMemorySampleView = new MemorySampleView(context);
        mMemorySampleView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    mConfig.setMemoryChartEnabled(false);
                }
            }
        });

        mCpuSampleView = new CpuSampleView(context);
        mCpuSampleView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    mConfig.setCpuChartEnabled(false);
                }
            }
        });

        mTrafficSampleView = new TrafficSampleView(context);
        mTrafficSampleView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    mConfig.setTrafficChartEnabled(false);
                }
            }
        });

        mFpsSampleView = new FpsSampleView(context);
        mFpsSampleView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    mConfig.setFpsChartEnabled(false);
                }
            }
        });

        mInspectorView = new InspectorView(context);
        mInspectorView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    mConfig.setViewInspectorEnabled(false);
                }
            }
        });

        //prepare shake detector
        mShakeDetector = new ShakeDetector(new ShakeDetector.ShakeListener() {
            @Override
            public void onShake() {
                showDevOptions();
            }
        });

        mVdomController = new VDomController(new PollingVDomMonitor(), new StandardVDomMonitor());

        mLaunchUIReceiver = new LaunchUIReceiver(new OnLaunchListener() {
            @Override
            public void onLaunch(@NonNull String from) {
                showDevOptions();
            }
        });
    }

    public static void launchByBroadcast(@NonNull Context context, @NonNull String from) {
        //todo 使用from字段区分来源
        Intent intent = new Intent(ACTION_LAUNCH);
        intent.putExtra(EXTRA_FROM,from);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    private List<DevOption> registerDefaultOptions() {

        List<DevOption> options = new ArrayList<>();

        options.add(new DevOption("weex性能指标", R.drawable.wxt_icon_performance, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (mCurPageName == null) {
                    Toast.makeText(mContext, "internal error", Toast.LENGTH_SHORT).show();
                    return;
                }
                Performance performance = WXPerfStorage.getInstance().getLatestPerformance(mCurPageName);
                List<Performance> list = WXPerfStorage.getInstance().getPerformanceList(mCurPageName);

                if (performance == null) {
                    return;
                }

                WXPerformanceAnalysisView view = new WXPerformanceAnalysisView(mContext, performance, list);
                view.show();
            }
        }));

        options.add(new DevOption("视图审查", R.drawable.wxt_icon_view_inspector, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (mConfig.isViewInspectorEnabled()) {
                    mConfig.setViewInspectorEnabled(false);
                    mInspectorView.dismiss();
                } else {
                    mConfig.setViewInspectorEnabled(true);
                    mInspectorView.show();
                    mInspectorView.bindInstance(mInstance);
                }
            }
        },true));

        options.add(new DevOption("渲染性能分析", R.drawable.wxt_icon_render_analysis, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (mConfig.isVDomDepthEnabled()) {
                    mConfig.setVdomDepthEnabled(false);
                    mProfileDomView.dismiss();
                } else {
                    mConfig.setVdomDepthEnabled(true);
                    mProfileDomView.show();
                    mProfileDomView.bindInstance(mInstance);
                }
            }
        }, true));

        options.add(new DevOption("MTOP", R.drawable.wxt_icon_mtop, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (mConfig.isNetworkInspectorEnabled()) {
                    mConfig.setNetworkInspectorEnabled(false);
                    mNetworkInspectorView.dismiss();
                } else {
                    mConfig.setNetworkInspectorEnabled(true);
                    mNetworkInspectorView.setViewSize(mConfig.getNetworkInspectorViewSize());
                    mNetworkInspectorView.show();
                }
            }
        }, true));

        options.add(new DevOption("weex storage", R.drawable.wxt_icon_storage, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                StorageView mStorageView = new StorageView(mContext);
                mStorageView.show();
            }
        }));

        options.add(new DevOption("3d视图", R.drawable.wxt_icon_3d_rotation, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (mScalpelViewController != null) {
                    mScalpelViewController.toggleScalpelEnabled();
                }
            }
        }, true));
        options.add(new DevOption("日志", R.drawable.wxt_icon_log, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (mConfig.isLogOutputEnabled()) {
                    mConfig.setLogOutputEnabled(false);
                    mLogView.dismiss();
                } else {
                    mConfig.setLogOutputEnabled(true);
                    mLogView.setLogLevel(mConfig.getLogLevel());
                    mLogView.setFilterName(mConfig.getLogFilter());
                    mLogView.setViewSize(mConfig.getLogViewSize());
                    mLogView.show();
                }
            }
        }, true));
        options.add(new DevOption("内存", R.drawable.wxt_icon_memory, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (mConfig.isMemoryChartEnabled()) {
                    mConfig.setMemoryChartEnabled(false);
                    mMemorySampleView.dismiss();
                } else {
                    mConfig.setMemoryChartEnabled(true);
                    mMemorySampleView.show();
                }
            }
        }, true));
        options.add(new DevOption("CPU", R.drawable.wxt_icon_cpu, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (mConfig.isCPUChartEnabled()) {
                    mConfig.setCpuChartEnabled(false);
                    mCpuSampleView.dismiss();
                } else {
                    mConfig.setCpuChartEnabled(true);
                    mCpuSampleView.show();
                }
            }
        }, true));
        options.add(new DevOption("fps", R.drawable.wxt_icon_fps, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (!FPSSampler.isSupported()) {
                    Toast.makeText(mContext, "your device is not support.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mConfig.isFpsChartEnabled()) {
                    mConfig.setFpsChartEnabled(false);
                    mFpsSampleView.dismiss();
                } else {
                    mConfig.setFpsChartEnabled(true);
                    mFpsSampleView.show();
                }
            }
        }, true));
        options.add(new DevOption("流量", R.drawable.wxt_icon_traffic, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (mConfig.isTrafficChartEnabled()) {
                    mConfig.setTrafficChartEnabled(false);
                    mTrafficSampleView.dismiss();
                } else {
                    mConfig.setTrafficChartEnabled(true);
                    mTrafficSampleView.show();
                }
            }
        }, true));

        options.add(new DevOption("综合性能", R.drawable.wxt_icon_multi_performance, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                if (mConfig.isPerfCommonEnabled()) {
                    mConfig.setPerfCommonEnabled(false);
                    mPerfMonitorOverlayView.dismiss();
                } else {
                    mConfig.setPerfCommonEnabled(true);
                    mPerfMonitorOverlayView.show();
                }
            }
        }, true));


        options.add(new DevOption("js远程调试", R.drawable.wxt_icon_debug, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                RemoteDebugManager.getInstance().toggle(mContext);
            }
        }));

        options.add(new DevOption("配置", R.drawable.wxt_icon_settings, new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                SettingsActivity.launch(mContext);
            }
        }));
        return options;
    }

    private void showDevOptions() {
        if (shown) {
            return;
        }

        if (mContext == null) {
            return;
        }

        if ((mContext instanceof Activity) && ((Activity) mContext).isFinishing()) {
            return;
        }

        EntranceView.Creator creator = new EntranceView.Creator(mContext)
                .injectOptions(registerDefaultOptions());

        if (mExtraOptions != null && !mExtraOptions.isEmpty()) {
            creator.injectOptions(mExtraOptions);
        }

        EntranceView e = creator.create();

        e.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                shown = false;
            }
        });
        e.show();
        shown = true;
    }

    @SuppressWarnings("unused")
    public void registerExtraOption(@NonNull DevOption option) {
        if (mExtraOptions == null) {
            mExtraOptions = new ArrayList<>();
        }
        mExtraOptions.add(option);
    }

    @SuppressWarnings("unused")
    public void registerExtraOption(@NonNull String optionName, int iconRes, @NonNull final Runnable runnable) {
        DevOption option = new DevOption();
        option.listener = new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e(DevOptionsConfig.TAG, e.getMessage());
                }
            }
        };
        option.iconRes = iconRes;
        option.optionName = optionName;
        registerExtraOption(option);
    }


    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onReceiveTouchEvent(MotionEvent ev) {
        if(ev == null) {
            return;
        }
        if(mInspectorView != null && mConfig.isViewInspectorEnabled()) {
            mInspectorView.receiveTouchEvent(ev);
        }
    }


    @Override
    public void onResume() {
        mShakeDetector.start((SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mLaunchUIReceiver,new IntentFilter(ACTION_LAUNCH));

        if (mConfig.isPerfCommonEnabled()) {
            mPerfMonitorOverlayView.show();
        } else {
            mPerfMonitorOverlayView.dismiss();
        }

        if (mConfig.isVDomDepthEnabled()) {
            mProfileDomView.show();
            mProfileDomView.bindInstance(mInstance);
        } else {
            mProfileDomView.dismiss();
        }

        if(mConfig.isViewInspectorEnabled()) {
            mInspectorView.show();
            mInspectorView.bindInstance(mInstance);
        } else {
            mInspectorView.dismiss();
        }

        if (mConfig.isNetworkInspectorEnabled()) {
            mNetworkInspectorView.setViewSize(mConfig.getNetworkInspectorViewSize());
            mNetworkInspectorView.show();
        } else {
            mNetworkInspectorView.dismiss();
        }

        if (mConfig.isLogOutputEnabled()) {
            mLogView.setLogLevel(mConfig.getLogLevel());
            mLogView.setFilterName(mConfig.getLogFilter());
            mLogView.setViewSize(mConfig.getLogViewSize());
            mLogView.show();
        } else {
            mLogView.dismiss();
        }

        if (mConfig.isMemoryChartEnabled()) {
            mMemorySampleView.show();
        } else {
            mMemorySampleView.dismiss();
        }

        if (mConfig.isCPUChartEnabled()) {
            mCpuSampleView.show();
        } else {
            mCpuSampleView.dismiss();
        }

        if (mConfig.isFpsChartEnabled()) {
            mFpsSampleView.show();
        } else {
            mFpsSampleView.dismiss();
        }

        if (mConfig.isTrafficChartEnabled()) {
            mTrafficSampleView.show();
        } else {
            mTrafficSampleView.dismiss();
        }

        if (mScalpelViewController != null) {
            mScalpelViewController.resume();
        }
    }

    @Override
    public void onPause() {
        mShakeDetector.stop();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mLaunchUIReceiver);

        if (mConfig.isPerfCommonEnabled()) {
            mPerfMonitorOverlayView.dismiss();
        }

        if (mConfig.isVDomDepthEnabled()) {
            mProfileDomView.dismiss();
        }

        if(mConfig.isViewInspectorEnabled()) {
            mInspectorView.dismiss();
        }

        if (mConfig.isNetworkInspectorEnabled()) {
            mNetworkInspectorView.dismiss();
        }

        if (mConfig.isLogOutputEnabled()) {
            mLogView.dismiss();
        }

        if (mConfig.isMemoryChartEnabled()) {
            mMemorySampleView.dismiss();
        }

        if (mConfig.isFpsChartEnabled()) {
            mFpsSampleView.dismiss();
        }

        if (mConfig.isCPUChartEnabled()) {
            mCpuSampleView.dismiss();
        }

        if (mConfig.isTrafficChartEnabled()) {
            mTrafficSampleView.dismiss();
        }

        if (mScalpelViewController != null) {
            mScalpelViewController.pause();
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
        if (mVdomController != null) {
            mVdomController.destroy();
            mVdomController = null;
        }
    }


    @Override
    public void onWeexRenderSuccess(@Nullable WXSDKInstance instance) {
        if (instance == null) {
            return;
        }
        this.mInstance = instance;
        mCurPageName = WXPerfStorage.getInstance().savePerformance(instance);

        if (mVdomController != null) {
            mVdomController.monitor(instance);
        }

        if (mProfileDomView != null) {
            mProfileDomView.bindInstance(instance);
        }

        if(mInspectorView != null) {
            mInspectorView.bindInstance(instance);
        }
    }

    @Override
    public View onWeexViewCreated(WXSDKInstance instance, View view) {
        if (instance == null || view == null || view.getContext() == null) {
            return null;
        }

        if (view.getParent() != null) {
            return view;
        }

        mScalpelViewController = new ScalpelViewController(mContext);
        mScalpelViewController.setOnToggleListener(new ScalpelViewController.OnToggleListener() {
            @Override
            public void onToggle(View view, boolean isScalpelEnabled) {
                Toast.makeText(mContext, "3d layer is " + (isScalpelEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            }
        });

        mScalpelViewController.setOnDrawViewNameListener(new ScalpelFrameLayout.OnDrawViewNameListener() {
            @Nullable
            @Override
            public String onDrawViewName(@NonNull View view, @NonNull String rawClazzName) {
                //custom filter
                for (String name : DevOptionsConfig.WHITE_SCALPEL_VIEW_NAMES) {
                    if (rawClazzName.equalsIgnoreCase(name)) {
                        return rawClazzName;
                    }
                }
                return null;
            }
        });

        return mScalpelViewController.wrapView(view);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!SDKUtils.isEmulator()) {
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showDevOptions();
            return true;
        }

        return false;
    }

    @Override
    public void onException(WXSDKInstance instance, String errCode, String msg) {
        if (mConfig != null && mConfig.isShownJSException()) {
            try {
                JSExceptionCatcher.catchException(mContext, instance, errCode, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class LaunchUIReceiver extends BroadcastReceiver {
        private OnLaunchListener listener;
        public LaunchUIReceiver(@NonNull OnLaunchListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null && ACTION_LAUNCH.equals(intent.getAction())) {
                String from = intent.getStringExtra(EXTRA_FROM);
                if(listener != null && !TextUtils.isEmpty(from)) {
                    listener.onLaunch(from);
                }
            }
        }
    }

    interface OnLaunchListener {
        void onLaunch(@NonNull String from);
    }

}

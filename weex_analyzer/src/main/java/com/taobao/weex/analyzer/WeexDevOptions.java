package com.taobao.weex.analyzer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.core.DevOptionsConfig;
import com.taobao.weex.analyzer.core.FPSSampler;
import com.taobao.weex.analyzer.core.JSExceptionCatcher;
import com.taobao.weex.analyzer.core.Performance;
import com.taobao.weex.analyzer.core.RemoteDebugManager;
import com.taobao.weex.analyzer.core.ShakeDetector;
import com.taobao.weex.analyzer.core.VDomController;
import com.taobao.weex.analyzer.core.WXPerfStorage;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.analyzer.view.CpuSampleView;
import com.taobao.weex.analyzer.view.DevOption;
import com.taobao.weex.analyzer.view.EntranceView;
import com.taobao.weex.analyzer.view.FpsSampleView;
import com.taobao.weex.analyzer.view.IOverlayView;
import com.taobao.weex.analyzer.view.LogView;
import com.taobao.weex.analyzer.view.MemorySampleView;
import com.taobao.weex.analyzer.view.PerfSampleOverlayView;
import com.taobao.weex.analyzer.view.ScalpelFrameLayout;
import com.taobao.weex.analyzer.view.ScalpelViewController;
import com.taobao.weex.analyzer.view.SettingsActivity;
import com.taobao.weex.analyzer.view.StorageView;
import com.taobao.weex.analyzer.view.TrafficSampleView;
import com.taobao.weex.analyzer.view.WXPerformanceAnalysisView;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/5<br/>
 * Time: 下午3:25<br/>
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

    private boolean shown = false;
    private List<DevOption> mExtraOptions = null;

    private VDomController mVdomController;

    public WeexDevOptions(@NonNull Context context){

        this.mContext = context;

        mConfig = new DevOptionsConfig(context);
        mPerfMonitorOverlayView = new PerfSampleOverlayView(context);

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

            @Override
            public void onLogSizeChanged(@LogView.Size int size) {
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
                if(host != null){
                    mConfig.setCpuChartEnabled(false);
                }
            }
        });

        mTrafficSampleView = new TrafficSampleView(context);
        mTrafficSampleView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if(host != null){
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

        //prepare shake detector
        mShakeDetector = new ShakeDetector(new ShakeDetector.ShakeListener() {
            @Override
            public void onShake() {
                showDevOptions();
            }
        });

        mVdomController = new VDomController();
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

                if(performance == null){
                    return;
                }

                WXPerformanceAnalysisView view = new WXPerformanceAnalysisView(mContext,performance,list);
                view.show();
            }
        }));
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
        },true));
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
        },true));
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
        },true));
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
        },true));
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
        },true));
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
        },true));

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
        },true));

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

    private void showDevOptions(){
        if(shown){
            return;
        }

        if(mContext == null){
            return;
        }

        if((mContext instanceof Activity) && ((Activity)mContext).isFinishing()){
            return;
        }

        EntranceView.Creator creator = new EntranceView.Creator(mContext)
                .injectOptions(registerDefaultOptions());

        if(mExtraOptions != null && !mExtraOptions.isEmpty()) {
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
        if(mExtraOptions == null) {
            mExtraOptions = new ArrayList<>();
        }
        mExtraOptions.add(option);
    }

    @SuppressWarnings("unused")
    public void registerExtraOption(@NonNull String optionName, int iconRes,@NonNull final Runnable runnable) {
        DevOption option = new DevOption();
        option.listener = new DevOption.OnOptionClickListener() {
            @Override
            public void onOptionClick() {
                try {
                    runnable.run();
                }catch (Exception e) {
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
    public void onResume() {
        mShakeDetector.start((SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE));

        if (mConfig.isPerfCommonEnabled()) {
            mPerfMonitorOverlayView.show();
        } else {
            mPerfMonitorOverlayView.dismiss();
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

        if(mConfig.isCPUChartEnabled()){
            mCpuSampleView.show();
        }else{
            mCpuSampleView.dismiss();
        }

        if (mConfig.isFpsChartEnabled()) {
            mFpsSampleView.show();
        } else {
            mFpsSampleView.dismiss();
        }

        if(mConfig.isTrafficChartEnabled()) {
            mTrafficSampleView.show();
        } else {
            mTrafficSampleView.dismiss();
        }

        if(mScalpelViewController != null){
            mScalpelViewController.resume();
        }
    }

    @Override
    public void onPause() {
        mShakeDetector.stop();

        if (mConfig.isPerfCommonEnabled()) {
            mPerfMonitorOverlayView.dismiss();
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

        if(mScalpelViewController != null){
            mScalpelViewController.pause();
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
        if(mVdomController != null) {
            mVdomController.destroy();
            mVdomController = null;
        }
    }


    @Override
    public void onWeexRenderSuccess(@Nullable WXSDKInstance instance) {
        if (instance == null) {
            return;
        }
        mCurPageName = WXPerfStorage.getInstance().savePerformance(instance);

        if(mVdomController != null) {
            mVdomController.trackVDom(instance);
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
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

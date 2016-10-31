package com.taobao.weex.analyzer;

import android.app.AlertDialog;
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
import com.taobao.weex.analyzer.core.FPSChecker;
import com.taobao.weex.analyzer.core.JSExceptionCatcher;
import com.taobao.weex.analyzer.core.Performance;
import com.taobao.weex.analyzer.core.RemoteDebugManager;
import com.taobao.weex.analyzer.core.ScalpelViewController;
import com.taobao.weex.analyzer.core.ShakeDetector;
import com.taobao.weex.analyzer.core.StorageHacker;
import com.taobao.weex.analyzer.core.WXPerfStorage;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.analyzer.view.CompatibleAlertDialogBuilder;
import com.taobao.weex.analyzer.view.FpsChartView;
import com.taobao.weex.analyzer.view.IOverlayView;
import com.taobao.weex.analyzer.view.LogView;
import com.taobao.weex.analyzer.view.MemoryChartView;
import com.taobao.weex.analyzer.view.PerfCommonOverlayView;
import com.taobao.weex.analyzer.view.ScalpelFrameLayout;
import com.taobao.weex.analyzer.view.StorageView;
import com.taobao.weex.analyzer.view.WXHistoryChartView;
import com.taobao.weex.analyzer.view.WXPerformanceView;

import java.util.LinkedHashMap;
import java.util.List;

import static com.taobao.weex.analyzer.core.DevOptionsConfig.CONFIG_REMOTE_SERVER;
import static com.taobao.weex.analyzer.core.DevOptionsConfig.SHOW_PERF_WEEX_ONLY;
import static com.taobao.weex.analyzer.core.DevOptionsConfig.SHOW_STORAGE_INFO;
import static com.taobao.weex.analyzer.core.DevOptionsConfig.TOGGLE_FPS_CHART;
import static com.taobao.weex.analyzer.core.DevOptionsConfig.TOGGLE_JS_REMOTE_DEBUG;
import static com.taobao.weex.analyzer.core.DevOptionsConfig.TOGGLE_LOG_OUTPUT;
import static com.taobao.weex.analyzer.core.DevOptionsConfig.TOGGLE_MEMORY_CHART;
import static com.taobao.weex.analyzer.core.DevOptionsConfig.TOGGLE_PERF_COMMON;
import static com.taobao.weex.analyzer.core.DevOptionsConfig.TOGGLE_SHOWN_JS_EXCEPTION;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/9/30<br/>
 * Time: 下午12:26<br/>
 */

/**
 * will be create in every weex activity
 */
public final class WeexDevOptions implements IWXDevOptions{
    private ShakeDetector mShakeDetector;
    private AlertDialog mDevOptionsDialog;
    private PerfCommonOverlayView mPerfMonitorOverlayView;
    private DevOptionsConfig mConfig;
    private Context mContext;

    private LogView mLogView;

    private MemoryChartView mMemoryChartView;
    private FpsChartView mFpsChartView;

    private String mCurPageName;

    private ScalpelViewController mScalpelViewController;

    private static final String TAG = "WeexDevOptions";

    public WeexDevOptions(@NonNull Context context) {
        this.mContext = context;
        mConfig = new DevOptionsConfig(context);
        mPerfMonitorOverlayView = new PerfCommonOverlayView(context);

        mLogView = new LogView(context);
        mLogView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    host.dismiss();
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


        mMemoryChartView = new MemoryChartView(context);
        mMemoryChartView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    host.dismiss();
                    mConfig.setMemoryChartEnabled(false);
                }
            }
        });

        mFpsChartView = new FpsChartView(context);
        mFpsChartView.setOnCloseListener(new IOverlayView.OnCloseListener() {
            @Override
            public void close(IOverlayView host) {
                if (host != null) {
                    host.dismiss();
                    mConfig.setFpsChartEnabled(false);
                }
            }
        });

        //prepare shake detector
        mShakeDetector = new ShakeDetector(new ShakeDetector.ShakeListener() {
            @Override
            public void onShake() {
                showDevDialog(mContext);
            }
        });
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onStart() {

    }

    /**
     * todo:
     * 1. 菜单可配置
     * 2. 支持悬浮窗显示在整个应用可见期间
     */
    @Override
    public void onResume() {
        //允许摇一摇
        mShakeDetector.start((SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE));

        //性能悬浮窗

        if (mConfig.isPerfCommonEnabled()) {
            mPerfMonitorOverlayView.show();
        } else {
            mPerfMonitorOverlayView.dismiss();
        }

        //log悬浮窗
        if (mConfig.isLogOutputEnabled()) {
            mLogView.setLogLevel(mConfig.getLogLevel());
            mLogView.setFilterName(mConfig.getLogFilter());
            mLogView.setViewSize(mConfig.getLogViewSize());
            mLogView.show();
        } else {
            mLogView.dismiss();
        }

        if (mConfig.isMemoryChartEnabled()) {
            mMemoryChartView.show();
        } else {
            mMemoryChartView.dismiss();
        }

        if (mConfig.isFpsChartEnabled()) {
            mFpsChartView.show();
        } else {
            mFpsChartView.dismiss();
        }
    }

    @Override
    public void onPause() {
        //关闭摇一摇
        mShakeDetector.stop();
        //关闭悬浮窗
        if (mConfig.isPerfCommonEnabled()) {
            mPerfMonitorOverlayView.dismiss();
        }

        if (mConfig.isLogOutputEnabled()) {
            mLogView.dismiss();
        }

        if (mConfig.isMemoryChartEnabled()) {
            mMemoryChartView.dismiss();
        }

        if (mConfig.isFpsChartEnabled()) {
            mFpsChartView.dismiss();
        }
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onWeexRenderSuccess(@Nullable WXSDKInstance instance) {
        if (instance == null) {
            return;
        }
        mCurPageName = WXPerfStorage.getInstance().savePerformance(instance);
    }

    @Override
    public @Nullable View onWeexViewCreated(WXSDKInstance instance, View view) {
        if (instance == null || view == null || view.getContext() == null) {
            return null;
        }

        if (view.getParent() != null) {
            Log.d(TAG, "can not replace wx root view to scalpelLayout because it had a parent already.");
            return view;
        }

        mScalpelViewController = new ScalpelViewController();
        mScalpelViewController.setOnToggleListener(new ScalpelViewController.OnToggleListener() {
            @Override
            public void onToggle(View view, boolean isScalpelEnabled) {
                Toast.makeText(mContext, "3d layer is " + (isScalpelEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();

                if (isScalpelEnabled) {
                    // clear other overlay view


                }

            }
        });

        mScalpelViewController.setOnDrawViewNameListener(new ScalpelFrameLayout.OnDrawViewNameListener() {
            @Nullable
            @Override
            public String onDrawViewName(@NonNull View view, @NonNull String rawClazzName) {
//                if(rawClazzName.equalsIgnoreCase("WXRecyclerView")){
////                    view.setBackgroundColor(Color.RED);
//                }

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
            showDevDialog(mContext);
            return true;
        }

        return false;
    }

    @Override
    public void onException(WXSDKInstance instance, String errCode, String msg) {
        if (mConfig != null && mConfig.isShownJSException()) {
            JSExceptionCatcher.catchException(mContext, instance, errCode, msg);
        }
    }

    private void showDevDialog(Context context) {
        if (mDevOptionsDialog != null) {
            return;
        }

        LinkedHashMap<String, DevOptionsConfig.OptionSelectListener> options = new LinkedHashMap<>();

        options.put(SHOW_PERF_WEEX_ONLY, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
//                if (mConfig.isPerfWeexOnlyEnabled()) {
//                    mConfig.setPerfWeexOnlyEnabled(false);
//
//                } else {
//                    mConfig.setPerfWeexOnlyEnabled(true);
//                }
                if (mCurPageName == null) {
                    Toast.makeText(mContext, "internal error", Toast.LENGTH_SHORT).show();
                    return;
                }
                Performance performance = WXPerfStorage.getInstance().getLatestPerformance(mCurPageName);
                WXPerformanceView performanceView = new WXPerformanceView(mContext, performance);
                performanceView.show();
            }
        });

        options.put(DevOptionsConfig.SHOW_HISTORY_PERF_STATISTICS, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
                if (mCurPageName == null) {
                    Toast.makeText(mContext, "no history data", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Performance> list = WXPerfStorage.getInstance().getPerformanceList(mCurPageName);
                WXHistoryChartView view = new WXHistoryChartView(mContext, list);
                view.show();
            }
        });

        options.put(DevOptionsConfig.TOGGLE_3D_LAYER, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
                //todo close others overlay
                if (mScalpelViewController != null) {
                    mScalpelViewController.toggleScalpelEnabled();
                }
            }
        });

        options.put(SHOW_STORAGE_INFO, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
                StorageView mStorageView = new StorageView(mContext,new StorageHacker(mContext));
                mStorageView.show();
            }
        });

        options.put(TOGGLE_LOG_OUTPUT, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
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
        });

        options.put(TOGGLE_PERF_COMMON, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
                if (mConfig.isPerfCommonEnabled()) {
                    mConfig.setPerfCommonEnabled(false);
                    mPerfMonitorOverlayView.dismiss();
                } else {
                    mConfig.setPerfCommonEnabled(true);
                    mPerfMonitorOverlayView.show();

                }

            }
        });


        options.put(TOGGLE_MEMORY_CHART, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
                if (mConfig.isMemoryChartEnabled()) {
                    mConfig.setMemoryChartEnabled(false);
                    mMemoryChartView.dismiss();
                } else {
                    mConfig.setMemoryChartEnabled(true);
                    mMemoryChartView.show();
                }
            }
        });


        options.put(TOGGLE_FPS_CHART, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
                if (!FPSChecker.isSupported()) {
                    Toast.makeText(mContext, "your device is not support.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mConfig.isFpsChartEnabled()) {
                    mConfig.setFpsChartEnabled(false);
                    mFpsChartView.dismiss();
                } else {
                    mConfig.setFpsChartEnabled(true);
                    mFpsChartView.show();
                }
            }
        });

        options.put(TOGGLE_SHOWN_JS_EXCEPTION, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
                if (mConfig.isShownJSException()) {
                    mConfig.setShownJSException(false);
                    Toast.makeText(mContext, mContext.getString(R.string.wxt_closed), Toast.LENGTH_SHORT).show();
                } else {
                    mConfig.setShownJSException(true);
                    Toast.makeText(mContext, mContext.getString(R.string.wxt_opened), Toast.LENGTH_SHORT).show();
                }
            }
        });


        options.put(TOGGLE_JS_REMOTE_DEBUG, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
                RemoteDebugManager.getInstance().toggle(mContext);
            }
        });

        options.put(CONFIG_REMOTE_SERVER, new DevOptionsConfig.OptionSelectListener() {
            @Override
            public void onSelectOption() {
                RemoteDebugManager.getInstance().requestDebugServer(mContext, false);
            }
        });


        final DevOptionsConfig.OptionSelectListener[] listeners = options.values().toArray(new DevOptionsConfig.OptionSelectListener[0]);
        mDevOptionsDialog = new CompatibleAlertDialogBuilder(context)
                .setItems(options.keySet().toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listeners[which].onSelectOption();
                        mDevOptionsDialog = null;
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mDevOptionsDialog = null;
                    }
                })
                .setTitle(DevOptionsConfig.DEV_OPTIONS)
                .create();

        mDevOptionsDialog.show();

    }

}

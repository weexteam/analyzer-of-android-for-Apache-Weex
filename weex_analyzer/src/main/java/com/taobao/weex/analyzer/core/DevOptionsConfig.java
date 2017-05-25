package com.taobao.weex.analyzer.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.taobao.weex.analyzer.view.overlay.IResizableView;

import java.util.Arrays;
import java.util.List;

/**
 * Description: <p> Created by rowandjj(chuyi)<br/> Date: 16/10/8<br/> Time: 下午12:45<br/>
 */
public class DevOptionsConfig {

    private SharedPreferences mSharedPreferences;
    private static final String DEV_CONFIG_NAME = "weex_dev_config";
    private static final String CONFIG_PERF_COMMON = "config_perf_common";//性能悬浮窗
    private static final String CONFIG_VDOM_DEPTH = "config_vdom_depth";
    private static final String CONFIG_NETWORK_INSPECTOR = "config_network_inspector";
    private static final String CONFIG_VIEW_INSPECTOR = "config_view_inspector";


    private static final String CONFIG_LOG_OUTPUT = "config_log_output";
    private static final String CONFIG_JS_EXCEPTION = "config_js_exception";
    private static final String CONFIG_EXCEPTION_NOTIFICATION = "config_exception_notification";

    private static final String CONFIG_MEMORY_CHART = "config_mem_chart";
    private static final String CONFIG_FPS_CHART = "config_fps_chart";
    private static final String CONFIG_CPU_CHART = "config_cpu_chart";
    private static final String CONFIG_TRAFFIC_CHART = "config_traffic_chart";


    private static final String CONFIG_LOG_LEVEL = "config_log_level";
    private static final String CONFIG_LOG_FILTER = "config_log_filter";
    private static final String CONFIG_LOG_VIEW_SIZE = "config_log_view_size";
    private static final String CONFIG_INSPECTOR_VIEW_SIZE = "config_inspector_view_size";

    public static final String TAG = "weex-analyzer";

    private static DevOptionsConfig sConfig;
    private DevOptionsConfig(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(DEV_CONFIG_NAME, Context.MODE_PRIVATE);
    }

    public static DevOptionsConfig getInstance(@NonNull Context context) {
        if(sConfig == null) {
            synchronized (DevOptionsConfig.class) {
                if(sConfig == null) {
                    sConfig = new DevOptionsConfig(context);
                }
            }
        }
        return sConfig;
    }

    /**
     * 设置是否展示性能悬浮窗(fps/memory)
     */
    public void setPerfCommonEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(CONFIG_PERF_COMMON, enabled).apply();
    }


    public boolean isPerfCommonEnabled() {
        return mSharedPreferences.getBoolean(CONFIG_PERF_COMMON, false);
    }

    public void setVdomDepthEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(CONFIG_VDOM_DEPTH, enabled).apply();
    }

    public boolean isVDomDepthEnabled() {
        return mSharedPreferences.getBoolean(CONFIG_VDOM_DEPTH, false);
    }

    public void setViewInspectorEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(CONFIG_VIEW_INSPECTOR, enabled).apply();
    }

    public boolean isViewInspectorEnabled() {
        return mSharedPreferences.getBoolean(CONFIG_VIEW_INSPECTOR, false);
    }

    public boolean isNetworkInspectorEnabled() {
        return mSharedPreferences.getBoolean(CONFIG_NETWORK_INSPECTOR, false);
    }

    public void setNetworkInspectorEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(CONFIG_NETWORK_INSPECTOR, enabled).apply();
    }

    /**
     * 设置是否展示日志
     */
    public void setLogOutputEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(CONFIG_LOG_OUTPUT, enabled).apply();
    }

    public boolean isLogOutputEnabled() {
        return mSharedPreferences.getBoolean(CONFIG_LOG_OUTPUT, false);
    }

    /**
     * 设置是否展示内存曲线
     */
    public void setMemoryChartEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(CONFIG_MEMORY_CHART, enabled).apply();
    }


    public boolean isMemoryChartEnabled() {
        return mSharedPreferences.getBoolean(CONFIG_MEMORY_CHART, false);
    }

    /**
     * 设置是否展示cpu曲线
     */
    public void setCpuChartEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(CONFIG_CPU_CHART, enabled).apply();
    }

    public boolean isCPUChartEnabled() {
        return mSharedPreferences.getBoolean(CONFIG_CPU_CHART, false);
    }


    /**
     * 设置是否展示流量曲线
     */
    public void setTrafficChartEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(CONFIG_TRAFFIC_CHART, enabled).apply();
    }

    public boolean isTrafficChartEnabled() {
        return mSharedPreferences.getBoolean(CONFIG_TRAFFIC_CHART, false);
    }

    /**
     * 设置是否展示fps变化曲线
     */
    public void setFpsChartEnabled(boolean enabled) {
        mSharedPreferences.edit().putBoolean(CONFIG_FPS_CHART, enabled).apply();
    }

    public boolean isFpsChartEnabled() {
        return mSharedPreferences.getBoolean(CONFIG_FPS_CHART, false);
    }

    public void setShownJSException(boolean shown) {
        mSharedPreferences.edit().putBoolean(CONFIG_JS_EXCEPTION, shown).apply();
    }

    public boolean isShownJSException() {
        return mSharedPreferences.getBoolean(CONFIG_JS_EXCEPTION, true);
    }

    public void setAllowExceptionNotification(boolean enabled) {
        mSharedPreferences.edit().putBoolean(CONFIG_EXCEPTION_NOTIFICATION, enabled).apply();
    }

    public boolean isAllowExceptionNotification() {
        return mSharedPreferences.getBoolean(CONFIG_EXCEPTION_NOTIFICATION, true);
    }


    public void setLogLevel(int level) {
        mSharedPreferences.edit().putInt(CONFIG_LOG_LEVEL, level).apply();
    }

    public int getLogLevel() {
        return mSharedPreferences.getInt(CONFIG_LOG_LEVEL, Log.VERBOSE);
    }

    public void setLogFilter(String filter) {
        mSharedPreferences.edit().putString(CONFIG_LOG_FILTER, filter).apply();
    }

    public String getLogFilter() {
        return mSharedPreferences.getString(CONFIG_LOG_FILTER, null);
    }

    public void setLogViewSize(@IResizableView.Size int size) {
        mSharedPreferences.edit().putInt(CONFIG_LOG_VIEW_SIZE, size).apply();
    }

    @IResizableView.Size
    public int getLogViewSize() {
        return mSharedPreferences.getInt(CONFIG_LOG_VIEW_SIZE, IResizableView.Size.MEDIUM);
    }

    public void setNetworkInspectorViewSize(@IResizableView.Size int size) {
        mSharedPreferences.edit().putInt(CONFIG_INSPECTOR_VIEW_SIZE, size).apply();
    }

    @IResizableView.Size
    public int getNetworkInspectorViewSize() {
        return mSharedPreferences.getInt(CONFIG_INSPECTOR_VIEW_SIZE, IResizableView.Size.MEDIUM);
    }


    public static final List<String> WHITE_SCALPEL_VIEW_NAMES = Arrays.asList("WXRecyclerView", "WXScrollView", "WXFrameLayout");


}

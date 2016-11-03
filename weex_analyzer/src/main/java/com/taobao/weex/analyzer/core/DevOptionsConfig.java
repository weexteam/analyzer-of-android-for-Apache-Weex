package com.taobao.weex.analyzer.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.taobao.weex.analyzer.view.LogView;

import java.util.Arrays;
import java.util.List;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/8<br/>
 * Time: 下午12:45<br/>
 */
public class DevOptionsConfig {

    private SharedPreferences mSharedPreferences;
    private static final String DEV_CONFIG_NAME = "weex_dev_config";
    private static final String CONFIG_PERF_COMMON = "config_perf_common";//性能悬浮窗
    private static final String CONFIG_LOG_OUTPUT = "config_log_output";
    private static final String CONFIG_JS_EXCEPTION = "config_js_exception";

    private static final String CONFIG_MEMORY_CHART = "config_mem_chart";
    private static final String CONFIG_FPS_CHART = "config_fps_chart";


    private static final String CONFIG_LOG_LEVEL = "config_log_level";
    private static final String CONFIG_LOG_FILTER = "config_log_filter";
    private static final String CONFIG_LOG_VIEW_SIZE = "config_log_view_size";


    public DevOptionsConfig(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(DEV_CONFIG_NAME, Context.MODE_PRIVATE);
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
        return mSharedPreferences.getBoolean(CONFIG_JS_EXCEPTION,true);
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

    public void setLogViewSize(@LogView.Size int size) {
        mSharedPreferences.edit().putInt(CONFIG_LOG_VIEW_SIZE, size).apply();
    }

    public
    @LogView.Size
    int getLogViewSize() {
        return mSharedPreferences.getInt(CONFIG_LOG_VIEW_SIZE, LogView.Size.MEDIUM);
    }


    public static final String DEV_OPTIONS = "weex开发者选项";

    public static final String TOGGLE_PERF_COMMON = "显示/关闭性能悬浮窗";
    public static final String SHOW_PERF_WEEX_ONLY = "显示weex性能指标";
    public static final String SHOW_STORAGE_INFO = "weex storage查看";
    public static final String TOGGLE_LOG_OUTPUT = "显示/关闭日志悬浮窗";
    public static final String TOGGLE_MEMORY_CHART = "显示/关闭内存占用折线图";
    public static final String TOGGLE_FPS_CHART = "显示/关闭fps折线图";
    public static final String TOGGLE_3D_LAYER = "显示/关闭3d视图";
    public static final String TOGGLE_SHOWN_JS_EXCEPTION = "允许/禁止js异常弹框";
    public static final String TOGGLE_JS_REMOTE_DEBUG = "js远程调试";
    public static final String CONFIG_REMOTE_SERVER = "远程调试服务器ip配置";



    public static final List<String> WHITE_SCALPEL_VIEW_NAMES = Arrays.asList("WXRecyclerView", "WXScrollView", "WXFrameLayout");

    public interface OptionSelectListener {
        void onSelectOption();
    }

}

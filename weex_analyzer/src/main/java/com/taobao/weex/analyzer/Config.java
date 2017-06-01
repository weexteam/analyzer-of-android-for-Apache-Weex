package com.taobao.weex.analyzer;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.taobao.weex.analyzer.core.logcat.LogConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class Config {

    public static final String TYPE_WEEX_PERFORMANCE_STATISTICS = "weex_performance_statistics";
    public static final String TYPE_VIEW_INSPECTOR = "view_inspector";
    public static final String TYPE_RENDER_ANALYSIS = "render_analysis";
    public static final String TYPE_MTOP_INSPECTOR = "mtop_inspector";
    public static final String TYPE_STORAGE = "storage";
    public static final String TYPE_3D = "3d";
    public static final String TYPE_LOG = "log";
    public static final String TYPE_MEMORY = "memory";
    public static final String TYPE_CPU = "cpu";
    public static final String TYPE_FPS = "fps";
    public static final String TYPE_TRAFFIC = "traffic";
    public static final String TYPE_ALL_PERFORMANCE = "all_performance";
    public static final String TYPE_DEBUG = "debug";
    public static final String TYPE_EXTERNAL_CONFIG = "external_config";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TYPE_CPU,TYPE_FPS,TYPE_MEMORY,TYPE_TRAFFIC,
            TYPE_WEEX_PERFORMANCE_STATISTICS,TYPE_VIEW_INSPECTOR,
            TYPE_RENDER_ANALYSIS,TYPE_MTOP_INSPECTOR,TYPE_STORAGE,
            TYPE_3D,TYPE_LOG,TYPE_ALL_PERFORMANCE,TYPE_DEBUG,TYPE_EXTERNAL_CONFIG})
    @interface Option {
    }

    private boolean enableShake;
    private List<String> ignoreOptions;
    private LogConfig logConfig;

    public void setEnableShake(boolean enableShake) {
        this.enableShake = enableShake;
    }

    public void setIgnoreOptions(List<String> ignoreOptions) {
        this.ignoreOptions = ignoreOptions;
    }

    public boolean isEnableShake() {
        return enableShake;
    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(LogConfig logConfig) {
        this.logConfig = logConfig;
    }

    @NonNull
    public List<String> getIgnoreOptions() {
        if(ignoreOptions == null || ignoreOptions.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(ignoreOptions);
    }

    public Config() {
    }


    public static class Builder {
        private boolean enableShake;
        private List<String> ignoreOptions;
        private LogConfig logConfig;
        public Builder() {
            this.ignoreOptions = new ArrayList<>();
        }

        @SuppressWarnings("unused")
        public Builder enableShake(boolean enableShake) {
            this.enableShake = enableShake;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder ignoreOption(@Option String ignoreOption) {
            this.ignoreOptions.add(ignoreOption);
            return this;
        }

        public Builder logConfig(LogConfig config) {
            this.logConfig = config;
            return this;
        }

        public Config build() {
            Config config = new Config();
            config.setEnableShake(enableShake);
            config.setIgnoreOptions(ignoreOptions);
            config.setLogConfig(logConfig);
            return config;
        }
    }
}

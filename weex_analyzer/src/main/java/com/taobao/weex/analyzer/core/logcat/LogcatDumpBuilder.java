package com.taobao.weex.analyzer.core.logcat;

import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

class LogcatDumpBuilder {

    private LogcatDumper.OnLogReceivedListener logReceivedListener;
    private List<LogcatDumper.Rule> ruleList;
    private int level = 0;
    private boolean isCacheEnabled;
    private int cacheLimit;

    LogcatDumpBuilder() {
    }

    public LogcatDumpBuilder listener(LogcatDumper.OnLogReceivedListener listener) {
        this.logReceivedListener = listener;
        return this;
    }

    public LogcatDumpBuilder rule(@Nullable LogcatDumper.Rule rule) {
        if (rule == null) {
            return this;
        }
        if (ruleList == null) {
            ruleList = new LinkedList<>();
        }
        ruleList.add(rule);
        return this;
    }


    public LogcatDumpBuilder level(int level) {
        this.level = level;
        return this;
    }

    LogcatDumpBuilder enableCache(boolean enabled) {
        this.isCacheEnabled = enabled;
        return this;
    }

    LogcatDumpBuilder cacheLimit(int limit){
        cacheLimit = limit;
        return this;
    }




    public LogcatDumper build() {
        LogcatDumper dumper = new LogcatDumper(logReceivedListener);
        dumper.setLevel(level);
        dumper.setCacheLimit(cacheLimit);
        dumper.setCacheEnabled(isCacheEnabled);
        if (ruleList != null) {
            for (LogcatDumper.Rule rule : ruleList) {
                dumper.addRule(rule);
            }
        }
        return dumper;
    }

}
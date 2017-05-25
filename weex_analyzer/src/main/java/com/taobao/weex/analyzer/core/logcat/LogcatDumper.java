package com.taobao.weex.analyzer.core.logcat;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.weex.analyzer.core.DevOptionsConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/11<br/>
 * Time: 下午1:55<br/>
 */

public class LogcatDumper implements Handler.Callback {

    private ExecutorService mExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "wx_analyzer_logcat_dumper");
        }
    });

    private static final int MESSAGE_DUMP_LOG = 1;
    private static final int MESSAGE_FILTER_LOG = 2;

    private Handler mHandler;
    private DumpLogRunnable mDumpLogRunnable;
    private OnLogReceivedListener mListener;
    private int mLevel;

    private List<Rule> mRules;

    private static final int DEFAULT_CACHE_LIMIT = 1000;

    private int mCacheLimit;
    private boolean isCacheEnabled = true;
    private volatile LinkedList<LogInfo> mCachedLogList;

    LogcatDumper(@Nullable OnLogReceivedListener listener) {
        this.mListener = listener;
        this.mCacheLimit = DEFAULT_CACHE_LIMIT;
        mRules = new LinkedList<>();
        mCachedLogList = new LinkedList<>();
    }

    public void addRule(@Nullable Rule rule) {
        if (rule == null) {
            return;
        }
        mRules.add(rule);
    }


    public void setLevel(int level) {
        this.mLevel = level;
    }

    public boolean removeRule(@Nullable Rule rule) {
        if (rule == null) {
            return false;
        }
        return mRules.remove(rule);
    }

    public boolean removeRule(@Nullable String ruleName){
        if(TextUtils.isEmpty(ruleName)){
            return false;
        }
        Rule r = new Rule(ruleName,"");
        return mRules.remove(r);
    }

    public void removeAllRule() {
        mRules.clear();
    }

    public void setCacheLimit(int cacheLimit) {
        if (cacheLimit <= 0) {
            return;
        }
        this.mCacheLimit = cacheLimit;
    }

    public int getCacheLimit() {
        return mCacheLimit;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.isCacheEnabled = cacheEnabled;
    }

    public boolean isCacheEnabled() {
        return isCacheEnabled;
    }


    public void beginDump() {
        mHandler = new Handler(Looper.getMainLooper(), this);
        mDumpLogRunnable = new DumpLogRunnable(true);
        execute(mDumpLogRunnable);
    }

    public void findCachedLogByNewFilters() {
        if (mHandler == null || !isCacheEnabled) {
            return;
        }

        execute(new Runnable() {
            @Override
            public void run() {
                List<LogInfo> result = filterCachedLog();
                Message msg = Message.obtain();
                msg.what = MESSAGE_FILTER_LOG;
                msg.obj = result;
                if (mHandler != null) {
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    public synchronized void clearCachedLog() {
        if (mCachedLogList != null) {
            mCachedLogList.clear();
        }
        execute(new Runnable() {
            @Override
            public void run() {
                clearLog();
            }
        });
    }

    @VisibleForTesting
    @Nullable
    public Handler getHandler() {
        return mHandler;
    }

    public void destroy() {
        if (mDumpLogRunnable != null) {
            mDumpLogRunnable.destroy();
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if (mExecutor != null) {
            mExecutor.shutdown();
        }

        mHandler = null;
        mExecutor = null;
        mDumpLogRunnable = null;

        mCachedLogList.clear();
        mCachedLogList = null;
    }

    private synchronized void cacheLog(@NonNull LogInfo logInfo) {
        if (mCachedLogList == null) {
            return;
        }
        if (mCachedLogList.size() >= mCacheLimit) {
            mCachedLogList.removeFirst();
        }
        mCachedLogList.add(logInfo);
    }

    private synchronized List<LogInfo> filterCachedLog() {
        if (mCachedLogList == null || mCachedLogList.isEmpty()) {
            return Collections.emptyList();
        }
        List<LogInfo> resultList = new ArrayList<>();

        for (LogInfo info : mCachedLogList) {
            int level = info.level;
            String message = info.message;
            if (checkLevel(level) && checkRule(message)) {
                resultList.add(info);
            }
        }
        return resultList;
    }


    @Override
    @SuppressWarnings("unchecked")
    public boolean handleMessage(Message msg) {
        if (mListener == null) {
            return false;
        }
        if (msg.what == MESSAGE_DUMP_LOG) {
            LogInfo log = (LogInfo) msg.obj;
            mListener.onReceived(Collections.singletonList(log));
        } else if (msg.what == MESSAGE_FILTER_LOG) {
            List<LogInfo> list = (List<LogInfo>) msg.obj;
            mListener.onReceived(list);
        }

        return false;
    }


    private void clearLog() {
        Process tempProcess = null;
        try {
            tempProcess = Runtime.getRuntime().exec("logcat -c");
            Thread.sleep(500);
        } catch (Exception e) {
            Log.d(DevOptionsConfig.TAG, e.getMessage());
        } finally {
            if (tempProcess != null) {
                tempProcess.destroy();
            }
        }
    }


    private boolean checkRule(String log) {
        //accept all log by default
        if (mRules.isEmpty()) {
            return true;
        }

        //must accept all rules
        for (Rule rule : mRules) {
            if (!rule.accept(log)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkLevel(int level) {
        if (this.mLevel == 0 || this.mLevel == Log.VERBOSE) {
            return true;
        }
        return level == this.mLevel;
    }

    private int getLevel(@NonNull String log) {
        if(log.length() < 20){
            return 'V';
        }
        char level = log.charAt(19);
        switch (level) {
            case 'V':
                return Log.VERBOSE;
            case 'I':
                return Log.INFO;
            case 'D':
                return Log.DEBUG;
            case 'W':
                return Log.WARN;
            case 'E':
                return Log.ERROR;
        }
        return 0;
    }

    private void dumpLog(@NonNull String log, int level) {
        if (mHandler == null) {
            return;
        }
        LogInfo info = new LogInfo();
        info.message = log;
        info.level = level;
        Message message = Message.obtain();
        message.what = MESSAGE_DUMP_LOG;
        message.obj = info;
        mHandler.sendMessage(message);
    }

    private void execute(Runnable command) {
        if (mExecutor != null) {
            mExecutor.execute(command);
        }
    }

    public interface OnLogReceivedListener {
        void onReceived(@NonNull List<LogInfo> logList);
    }


    public class LogInfo {
        public String message;
        public int level;

        public LogInfo() {
        }

        public LogInfo(String message, int level) {
            this.message = message;
            this.level = level;
        }
    }


    private class DumpLogRunnable implements Runnable {

        private Process logProcess;
        private boolean isAllowClear;

        DumpLogRunnable(boolean allowClear) {
            this.isAllowClear = allowClear;
        }

        @Override
        public void run() {
            BufferedReader reader;
            try {

                if (isAllowClear) {
                    clearLog();
                }

                //https://developer.android.com/studio/command-line/logcat.html
                logProcess = Runtime.getRuntime().exec("logcat -v time");
                reader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    int level = getLevel(line);
                    if (checkLevel(level) && checkRule(line)) {
                        dumpLog(line, level);
                    }

                    if (isCacheEnabled) {
                        cacheLog(new LogInfo(line, level));
                    }
                }
            } catch (IOException e) {
                Log.e(DevOptionsConfig.TAG,e.getMessage());
            }
        }

        void destroy() {
            try {
                if (logProcess != null) {
                    logProcess.destroy();
                }
            } catch (Exception e) {
                Log.e(DevOptionsConfig.TAG, e.getMessage());
            }
        }

    }


    public static class Rule {

        private String name;
        private String filter;

        public Rule(@Nullable String name, @Nullable String filter) {
            this.name = name;
            this.filter = filter;
        }

        public boolean accept(@NonNull String log) {
            if (TextUtils.isEmpty(filter)) {
                return true;
            }
            return log.contains(filter);
        }

        public String getName() {
            return name;
        }

        public String getFilter() {
            return filter;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Rule rule = (Rule) o;

            return name != null ? name.equals(rule.name) : rule.name == null;

        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }


}

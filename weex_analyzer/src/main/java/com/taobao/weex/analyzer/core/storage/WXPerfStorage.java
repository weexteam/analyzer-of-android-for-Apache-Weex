package com.taobao.weex.analyzer.core.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.core.weex.Performance;
import com.taobao.weex.analyzer.core.weex.PerformanceMonitor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/19<br/>
 * Time: 上午10:45<br/>
 */

public class WXPerfStorage {

    private static final int DEFAULT_SAMPLE_NUM = 6;

    private Map<String, LinkedList<Performance>> mHistoryData;
    private int mSampleNum;

    private WXPerfStorage() {
        mHistoryData = new HashMap<>();
        mSampleNum = DEFAULT_SAMPLE_NUM;
    }

    private static WXPerfStorage sInstance;

    public static WXPerfStorage getInstance() {
        if (sInstance == null) {
            synchronized (WXPerfStorage.class) {
                if (sInstance == null) {
                    sInstance = new WXPerfStorage();
                }
            }
        }
        return sInstance;
    }

    /**
     * save weex performance data to storage.
     *
     * @return pageName of this {@link WXSDKInstance}
     */
    public String savePerformance(@NonNull WXSDKInstance instance) {
        String pageName = null;

        Performance performance = PerformanceMonitor.monitor(instance);
        if (performance != null) {
            pageName = WXPerfStorage.fetchPageName(instance, performance);
            put(pageName, performance);
        }

        return pageName;
    }

    public
    @Nullable
    Performance getLatestPerformance(@Nullable String pageName) {
        List<Performance> cachedList = getPerformanceList(pageName);
        if (cachedList.size() == 0) {
            return null;
        }
        return cachedList.get(cachedList.size() - 1);
    }

    public
    @NonNull
    List<Performance> getPerformanceList(@Nullable String pageName) {
        if (TextUtils.isEmpty(pageName) || mHistoryData == null) {
            return Collections.emptyList();
        }
        List<Performance> cachedList = mHistoryData.get(pageName);
        if (cachedList == null || cachedList.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(cachedList);
    }

    private
    @Nullable
    static String fetchPageName(@NonNull WXSDKInstance instance, @NonNull Performance performance) {
        String templateUrl = performance.templateUrl;
        if (!TextUtils.isEmpty(templateUrl)) {
            return templateUrl;
        }

        try {
            Method method = instance.getClass().getDeclaredMethod("getBundleUrl");
            method.setAccessible(true);
            templateUrl = (String) method.invoke(instance);
            method.setAccessible(false);
        } catch (Exception e) {
        }
        if (!TextUtils.isEmpty(templateUrl)) {
            return templateUrl;
        }
        return performance.pageName;
    }


    private void put(@Nullable String pageName, @Nullable Performance performance) {
        if (TextUtils.isEmpty(pageName) || performance == null || mHistoryData == null) {
            return;
        }
        LinkedList<Performance> list = mHistoryData.get(pageName);
        if (list == null) {
            list = new LinkedList<>();
            mHistoryData.put(pageName, list);
        }

        if (list.size() >= mSampleNum) {
            list.removeFirst();
        }

        list.add(performance);
    }


    public void setSampleNum(int sampleNum) {
        if (sampleNum <= 0) {
            return;
        }
        this.mSampleNum = sampleNum;
    }

    public int getSampleNum() {
        return this.mSampleNum;
    }

}

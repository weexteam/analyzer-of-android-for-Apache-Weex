package com.taobao.weex.analyzer.core.weex;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.common.WXPerformance;
import com.taobao.weex.utils.WXLogUtils;

import java.lang.reflect.Field;

/**
 * Description:
 *
 *
 *
 *
 *
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/9/30<br/>
 * Time: 下午2:30<br/>
 */
public class PerformanceMonitor {

    public static @Nullable
    Performance monitor(@Nullable WXSDKInstance instance){
        if(instance == null){
            return null;
        }
        WXPerformance rawPerformance = null;
        try {
            Class clazz = WXSDKInstance.class;
            Field field = clazz.getDeclaredField("mWXPerformance");
            field.setAccessible(true);
            rawPerformance = (WXPerformance) field.get(instance);
        } catch (Exception e) {
            WXLogUtils.e(e.getMessage());
        }

        if(rawPerformance != null){
             return filter(rawPerformance);
        }
        return null;
    }


    private static Performance filter(@NonNull WXPerformance rawPerformance) {
        Performance p = new Performance();
        p.localReadTime = rawPerformance.localReadTime;
        p.JSLibSize = rawPerformance.JSLibSize;
        p.JSLibInitTime = rawPerformance.JSLibInitTime;
        p.JSTemplateSize = rawPerformance.JSTemplateSize;
        p.templateLoadTime = rawPerformance.templateLoadTime;
        p.communicateTime = rawPerformance.communicateTime;
        p.screenRenderTime = rawPerformance.screenRenderTime;
        p.callNativeTime = rawPerformance.callNativeTime;
        p.firstScreenJSFExecuteTime = rawPerformance.firstScreenJSFExecuteTime;
        p.batchTime = rawPerformance.batchTime;
        p.parseJsonTime = rawPerformance.parseJsonTime;
        p.updateDomObjTime = rawPerformance.updateDomObjTime;
        p.applyUpdateTime = rawPerformance.applyUpdateTime;
        p.cssLayoutTime = rawPerformance.cssLayoutTime;
        p.totalTime = rawPerformance.totalTime;
        p.networkTime = rawPerformance.networkTime;
        p.pureNetworkTime = rawPerformance.pureNetworkTime;
        p.actualNetworkTime = rawPerformance.actualNetworkTime;
        p.componentCount = rawPerformance.componentCount;
        p.JSLibVersion = rawPerformance.JSLibVersion;
        p.WXSDKVersion = rawPerformance.WXSDKVersion;
        p.pageName = rawPerformance.pageName;
        p.templateUrl = rawPerformance.templateUrl;
        p.requestType = rawPerformance.requestType;
        p.connectionType = rawPerformance.connectionType;
        return p;
    }

}

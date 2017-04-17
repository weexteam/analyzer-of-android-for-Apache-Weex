package com.taobao.weex.analyzer.core.debug;

import android.support.annotation.NonNull;

import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.bridge.WXBridgeManager;
import com.taobao.weex.utils.WXLogUtils;

import java.lang.reflect.Method;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class DebugTool {

    private static final String TAG = "DebugTool";

    private DebugTool() {
    }

    public static void startRemoteDebug(@NonNull String serverAddress) {
        try {
            WXEnvironment.sRemoteDebugProxyUrl = serverAddress;
            WXEnvironment.sRemoteDebugMode = true;
            WXSDKEngine.reload();
        }catch (Exception e) {
            WXLogUtils.e(TAG,e.getMessage());
        }
    }

    public static boolean stopRemoteDebug() {
        try {
            WXBridgeManager manager = WXBridgeManager.getInstance();
            Method method = manager.getClass().getDeclaredMethod("stopRemoteDebug");
            method.setAccessible(true);
            method.invoke(manager);
            return true;
        }catch (Exception e) {
            WXLogUtils.e(TAG,e.getMessage());
            return false;
        }
    }
}

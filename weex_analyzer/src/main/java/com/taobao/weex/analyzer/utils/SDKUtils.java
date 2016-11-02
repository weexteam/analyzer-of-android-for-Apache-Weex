package com.taobao.weex.analyzer.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.taobao.weex.WXSDKEngine;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/9/30<br/>
 * Time: 下午2:43<br/>
 */

public class SDKUtils {

    private SDKUtils(){}

    public static boolean isWXInitialized(){
        return WXSDKEngine.isInitialized();
    }


    public static boolean isEmulator() {
        //http://stackoverflow.com/questions/2799097/how-can-i-detect-when-an-android-application-is-running-in-the-emulator
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    public static void copyToClipboard(@NonNull Context context, @Nullable String text, boolean allowNotification){
        if(TextUtils.isEmpty(text)){
            return;
        }
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copied text", text);
        manager.setPrimaryClip(clip);
        if(allowNotification){
            Toast.makeText(context,"copied to clipboard success",Toast.LENGTH_SHORT).show();
        }
    }


}

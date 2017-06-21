package com.taobao.weex.analyzer.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.taobao.weex.utils.WXLogUtils;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class DeviceUtils {

    private static final String TAG = "DeviceUtils";

    private DeviceUtils(){
    }

    @NonNull
    public static String getAppName(@NonNull Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            int stringId = info.labelRes;
            return stringId == 0 ? info.nonLocalizedLabel.toString() : context.getString(stringId);
        }catch (Exception e) {
            WXLogUtils.e(TAG,e.getMessage());
            return "UNKNOWN";
        }
    }

    @NonNull
    public static String getAppVersion(@NonNull Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),0);
            return info.versionName;
        }catch (Exception e) {
            return "null";
        }
    }

    /**
     * TODO 不需要
     *
     * */
    @Deprecated
    public static String getMyIp(@NonNull Context context) {
        String ip = "0.0.0.0";
        return ip;
    }

    @NonNull
    public static String getOSType() {
        return "Android";
    }

    @NonNull
    public static String getOSVersion() {
        String versionName = "unknown";
        try {
            versionName = "Android " + String.valueOf(Build.VERSION.RELEASE);
        } catch (Exception e) {
            WXLogUtils.e(TAG,e.getMessage());
        }
        return versionName;
    }

    @NonNull
    public static String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            try {
                return capitalize(model);
            }catch (Exception e) {
                return "unknown";
            }
        }

        try {
            return capitalize(manufacturer) + " " + model;
        }catch (Exception e) {
            return "unknown";
        }
    }

    @NonNull
    public static String getDeviceId(@NonNull Context context) {
        try {
            String id = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            return id;
        }catch (Exception e) {
            WXLogUtils.e(TAG,e.getMessage());
            return "null";
        }
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

}

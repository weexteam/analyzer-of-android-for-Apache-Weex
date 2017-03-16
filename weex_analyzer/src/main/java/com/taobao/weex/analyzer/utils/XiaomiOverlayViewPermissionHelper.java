package com.taobao.weex.analyzer.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Build;
import android.support.annotation.NonNull;

import com.taobao.weex.utils.WXLogUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class XiaomiOverlayViewPermissionHelper {

    private static final String TAG = "PermissionHelper";

    public static boolean isPermissionGranted(@NonNull Context context) {
        if(!"Xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
            return true;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = manager.getClass().getDeclaredMethod("checkOp", int.class, int.class, String.class);
                int property = (Integer) method.invoke(manager, 24/*AppOpsManager.OP_SYSTEM_ALERT_WINDOW*/,
                        Binder.getCallingUid(), context.getApplicationContext().getPackageName());
                return AppOpsManager.MODE_ALLOWED == property;
            }catch (Throwable e) {
                WXLogUtils.e(TAG,e.getMessage());
                return true;
            }
        } else {
            return true;
        }
    }

    public static void requestPermission(@NonNull Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        if ("V5".equalsIgnoreCase(getProperty())) {
            PackageInfo pInfo = null;
            try {
                pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            } catch (Exception e) {
                WXLogUtils.e(TAG,e.getMessage());
                return;
            }
            intent.setClassName("com.miui.securitycenter", "com.miui.securitycenter.permission.AppPermissionsEditor");
            intent.putExtra("extra_package_uid", pInfo.applicationInfo.uid);
        } else {
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intent.putExtra("extra_pkgname", context.getPackageName());
        }
        if (isActivityAvailable(context, intent)) {
            context.startActivity(intent);
        } else {
            WXLogUtils.e(TAG, "request permission for xiaomi failed");
        }
    }

    private static String getProperty() {
        String property = "null";
        if (!"Xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
            return property;
        }
        try {
            Class<?> spClazz = Class.forName("android.os.SystemProperties");
            Method method = spClazz.getDeclaredMethod("get", String.class, String.class);
            method.setAccessible(true);
            property = (String) method.invoke(spClazz, "ro.miui.ui.version.name", null);
        } catch (Exception e) {
            WXLogUtils.e(TAG,e.getMessage());
        }
        return property;
    }

    private static boolean isActivityAvailable(@NonNull Context cxt,@NonNull Intent intent) {
        PackageManager pm = cxt.getPackageManager();
        if (pm == null) {
            return false;
        }
        List<ResolveInfo> list = pm.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list != null && list.size() > 0;
    }
}

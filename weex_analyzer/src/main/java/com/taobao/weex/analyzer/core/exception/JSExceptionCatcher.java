package com.taobao.weex.analyzer.core.exception;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.DevOptionsConfig;
import com.taobao.weex.analyzer.view.alert.CompatibleAlertDialogBuilder;

import java.util.Locale;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/10/25<br/>
 * Time: 下午2:45<br/>
 */

public class JSExceptionCatcher {
    private static final int ID = 0x1001;
    private JSExceptionCatcher() {
    }

    @Nullable
    public static AlertDialog catchException(@Nullable Context context, @Nullable DevOptionsConfig config, @Nullable WXSDKInstance instance, @Nullable String errCode, @Nullable String msg) {
        if (context == null) {
            return null;
        }

        if(errCode == null && msg == null){
            return null;
        }

        if(config != null && config.isAllowExceptionNotification()) {
            sendNotification(context,instance,errCode,msg);
        }

        AlertDialog.Builder builder = new CompatibleAlertDialogBuilder(context);
        builder.setTitle("WeexAnalyzer捕捉到异常");
        builder.setMessage(String.format(Locale.CHINA,"errorCode : %s\nerrorMsg : %s\n",TextUtils.isEmpty(errCode) ? "unknown" : errCode, TextUtils.isEmpty(msg) ? "unknown" : msg));
        builder.setPositiveButton("okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    private static void sendNotification(@NonNull Context context,@Nullable WXSDKInstance instance,@Nullable String errCode,@Nullable String msg) {
        String bundleUrl = null;
        if(instance != null) {
            bundleUrl = instance.getBundleUrl();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.wxt_icon_debug)
                .setContentTitle("WeexAnalyzer捕捉到异常")
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(String.format(Locale.CHINA,"page : %s\nerrorCode : %s\nerrorMsg : %s\n",TextUtils.isEmpty(bundleUrl) ? "unknown" : bundleUrl,
                                    TextUtils.isEmpty(errCode) ? "unknown" : errCode, TextUtils.isEmpty(msg) ? "unknown" : msg)))
                .setContentText(String.format(Locale.CHINA,"errorCode : %s,errorMsg : %s",TextUtils.isEmpty(errCode) ? "unknown" : errCode, TextUtils.isEmpty(msg) ? "unknown" : msg));

        if(Build.VERSION.SDK_INT >= 21) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        builder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID,builder.build());
    }


}

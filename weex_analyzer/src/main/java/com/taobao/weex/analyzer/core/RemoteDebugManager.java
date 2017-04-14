package com.taobao.weex.analyzer.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.view.CompatibleAlertDialogBuilder;
import com.taobao.weex.bridge.WXBridgeManager;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/10/25<br/>
 * Time: 下午5:09<br/>
 */

public class RemoteDebugManager {

    private static RemoteDebugManager sManager;

    private boolean isEnabled = false;

    private static final String sRemoteUrlTemplate = "ws://%s:8088/debugProxy/native";
    private String mServerIP = null;


    private RemoteDebugManager() {
    }


    public static RemoteDebugManager getInstance() {
        if (sManager == null) {
            synchronized (RemoteDebugManager.class) {
                if (sManager == null) {
                    sManager = new RemoteDebugManager();
                }
            }
        }
        return sManager;
    }


    public void toggle(Context context) {
        try {
            isEnabled = !isEnabled;
            if (isEnabled) {
                startRemoteJSDebug(context);
            } else {
                stopRemoteJSDebug(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestDebugServer(final Context context, final boolean autoStart) {
        AlertDialog.Builder builder = new CompatibleAlertDialogBuilder(context);
        final EditText editText = new EditText(context);

        if (!TextUtils.isEmpty(mServerIP)) {
            editText.setHint(mServerIP);
        } else {
            editText.setHint("127.0.0.1");
        }

        builder.setView(editText);
        builder.setTitle("Debug server ip configuration");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = editText.getText().toString();
                if(TextUtils.isEmpty(temp)) {
                    Toast.makeText(context, "ip can not be null", Toast.LENGTH_SHORT).show();
                }else{
                    mServerIP = temp.trim();
                    dialog.dismiss();

                    if (autoStart) {
                        startRemoteJSDebug(context);
                    }
                }
            }
        });

    }

    private void startRemoteJSDebug(Context context) {
        try {
            if (!TextUtils.isEmpty(mServerIP)) {
                WXEnvironment.sRemoteDebugProxyUrl = String.format(Locale.CHINA, sRemoteUrlTemplate, mServerIP);
            } else {
                requestDebugServer(context, true);
                isEnabled = false;
                return;
            }
            WXEnvironment.sRemoteDebugMode = true;
            WXSDKEngine.reload();
            isEnabled = true;
            Toast.makeText(context, context.getString(R.string.wxt_opened), Toast.LENGTH_SHORT).show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRemoteJSDebug(Context context) {
        try {
            WXBridgeManager manager = WXBridgeManager.getInstance();
            Method method = manager.getClass().getDeclaredMethod("stopRemoteDebug");
            method.setAccessible(true);
            method.invoke(manager);
            Toast.makeText(context, "close success", Toast.LENGTH_SHORT).show();
        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "close failed", Toast.LENGTH_SHORT).show();
        }
    }


}

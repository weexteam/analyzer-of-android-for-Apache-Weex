package com.taobao.weex.analyzer.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.weex.analyzer.utils.SDKUtils;

/**
 * Description:
 *
 *  monitor cpu & fps & memory
 *
 *  usage:
 *
 *    1. adb command:
 *       - start: adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e c on
 *       - stop: adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e c off
 *
 *    2. shake device and start it by click option
 *
 *  notice:
 *    make sure your host app is running and interactive or the service is not started.
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class LaunchAnalyzerReceiver extends BroadcastReceiver {
    private static final String ACTION = "com.taobao.weex.analyzer.LaunchService";
    static final String TAG = "weex-analyzer";
    private static final String CMD = "c";
    private static final String CMD_ON = "on";
    private static final String CMD_OFF = "off";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION.equals(intent.getAction())) {
            return;
        }
        String cmd = intent.getStringExtra(CMD);
        if(TextUtils.isEmpty(cmd) || CMD_ON.equals(cmd)) {
            performStart(context);
        } else if(CMD_OFF.equals(cmd)) {
            performStop(context);
        } else {
            Log.d(TAG,"illegal command. use [adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e c on] to start");
        }
    }

    private void performStart(@NonNull Context context) {
        if(!SDKUtils.isHostRunning(context) || !SDKUtils.isInteractive(context)) {
            Log.d(TAG,"service start failed(host app is not in foreground,is your app running?)");
            return;
        }

        Intent intent = new Intent(context,AnalyzerService.class);
        context.startService(intent);
    }

    private void performStop(@NonNull Context context) {
        Intent intent = new Intent(context,AnalyzerService.class);
        context.stopService(intent);
    }

}

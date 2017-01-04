package com.taobao.weex.analyzer.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.utils.WXLogUtils;

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
    private static final String CMD_PERFORMANCE = "c";
    private static final String CMD_TRACKER_STANDARD = "d";
    private static final String CMD_TRACKER_POLLING = "f";

    private static final String CMD_ON = "on";
    private static final String CMD_OFF = "off";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION.equals(intent.getAction())) {
            return;
        }
        //性能数据
        String cmd_performance = intent.getStringExtra(CMD_PERFORMANCE);
        if(!TextUtils.isEmpty(cmd_performance)) {
            if(CMD_ON.equals(cmd_performance)) {
                performStart(context);
            } else if(CMD_OFF.equals(cmd_performance)) {
                performStop(context);
            } else{
                Log.d(Constants.TAG,"illegal command. use [adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e c on] to fetch performance data");
            }
        }


        //vdom调优 监听模式
        String cmd_tracker_standard = intent.getStringExtra(CMD_TRACKER_STANDARD);
        if(!TextUtils.isEmpty(cmd_tracker_standard)) {
            if(CMD_ON.equals(cmd_tracker_standard)) {
                VDomController.isStandardMode = true;
            } else if(CMD_OFF.equals(cmd_tracker_standard)) {
                VDomController.isStandardMode = false;
            } else {
                Log.d(Constants.TAG,"illegal command. use [adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e d on] to start vdom tracker");
            }
        }

        //vdom调优 轮询模式
        String cmd_tracker_polling = intent.getStringExtra(CMD_TRACKER_POLLING);
        if(!TextUtils.isEmpty(cmd_tracker_polling)) {
            if(CMD_ON.equals(cmd_tracker_polling)) {
                VDomController.isPollingMode = true;
                PollingVDomMonitor.shouldStop = false;
            } else if(CMD_OFF.equals(cmd_tracker_polling)) {
                VDomController.isPollingMode = false;
                PollingVDomMonitor.shouldStop = true;
            } else {
                Log.d(Constants.TAG,"illegal command. use [adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e f on] to start vdom tracker(polling mode)");
            }
        }
    }

    private void performStart(@NonNull Context context) {
        if(!SDKUtils.isHostRunning(context) || !SDKUtils.isInteractive(context)) {
            Log.d(Constants.TAG,"service start failed(host app is not in foreground,is your app running?)");
            return;
        }
        WXLogUtils.d(Constants.TAG,"analyzer service will start...");
        Intent intent = new Intent(context,AnalyzerService.class);
        context.startService(intent);
    }

    private void performStop(@NonNull Context context) {
        Intent intent = new Intent(context,AnalyzerService.class);
        context.stopService(intent);
    }

}

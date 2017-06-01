package com.taobao.weex.analyzer.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.weex.analyzer.WeexDevOptions;
import com.taobao.weex.analyzer.core.debug.DebugTool;
import com.taobao.weex.analyzer.core.lint.PollingVDomMonitor;
import com.taobao.weex.analyzer.core.lint.VDomController;
import com.taobao.weex.analyzer.core.reporter.AnalyzerService;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.utils.WXLogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 *  1. monitor cpu & fps & memory
 *  2. monitor weex page layer/list/big cell and so on
 *
 *
 *  usage:
 *
 *    1. adb command(monitor cpu & fps & memory):
 *       - start: adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e c on
 *       - stop: adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e c off
 *
 *    2.  adb command(monitor weex page layer/list/big cell and so on):
 *       - start: adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e d on
 *       - stop: adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e d off
 *
 *       - start: adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e f on
 *       - stop: adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e f off
 *
 *    3. launch main ui
 *      - start: adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e launch true [from mds]
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

    private static final String CMD_LAUNCH = "launch";

    private static final String TRUE = "true";

    private static final String CMD_WX_SERVER = "wx_server";


    private static final String CMD_ON = "on";
    private static final String CMD_OFF = "off";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION.equals(intent.getAction())) {
            return;
        }
        //性能数据
        String cmd_performance = intent.getStringExtra(CMD_PERFORMANCE);
        //vdom调优 监听模式
        String cmd_tracker_standard = intent.getStringExtra(CMD_TRACKER_STANDARD);
        //vdom调优 轮询模式
        String cmd_tracker_polling = intent.getStringExtra(CMD_TRACKER_POLLING);
        // 启动服务
        String cmd_launch = intent.getStringExtra(CMD_LAUNCH);

        String cmd_wx_server = intent.getStringExtra(CMD_WX_SERVER);

        if(!TextUtils.isEmpty(cmd_performance)) {
            if(CMD_ON.equals(cmd_performance)) {
                performStart(context,AnalyzerService.ATS, null);
            } else if(CMD_OFF.equals(cmd_performance)) {
                performStop(context);
            } else{
                Log.d(Constants.TAG,"illegal command. use [adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e c on] to fetch performance data");
            }
        } else if(!TextUtils.isEmpty(cmd_tracker_standard)) {
            if(CMD_ON.equals(cmd_tracker_standard)) {
                VDomController.isStandardMode = true;
                VDomController.isPollingMode = false;
                PollingVDomMonitor.shouldHighlight = false;
            } else if(CMD_OFF.equals(cmd_tracker_standard)) {
                VDomController.isStandardMode = false;
            } else {
                Log.d(Constants.TAG,"illegal command. use [adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e d on] to start vdom tracker");
            }
        } else if(!TextUtils.isEmpty(cmd_tracker_polling)) {
            String highlightEnabled = intent.getStringExtra("h");
            if(CMD_ON.equals(cmd_tracker_polling)) {
                VDomController.isPollingMode = true;
                VDomController.isStandardMode = false;
                PollingVDomMonitor.shouldStop = false;
                PollingVDomMonitor.shouldHighlight = CMD_ON.equals(highlightEnabled);
            } else if(CMD_OFF.equals(cmd_tracker_polling)) {
                VDomController.isPollingMode = false;
                PollingVDomMonitor.shouldStop = true;
                PollingVDomMonitor.shouldHighlight = false;
            } else {
                Log.d(Constants.TAG,"illegal command. use [adb shell am broadcast -a com.taobao.weex.analyzer.LaunchService -e f on] to start vdom tracker(polling mode)");
            }
        } else if(!TextUtils.isEmpty(cmd_launch)) {
            if(TRUE.equals(cmd_launch)) {
                String from = intent.getStringExtra(WeexDevOptions.EXTRA_FROM);
                String deviceId = intent.getStringExtra(WeexDevOptions.EXTRA_DEVICE_ID);
                String wsUrl = intent.getStringExtra(WeexDevOptions.EXTRA_WS_URL);

                WXLogUtils.d(Constants.TAG,"from:"+from+",deviceId:"+deviceId+",wxurl:"+wsUrl);

                Map<String,String> extras = new HashMap<>();
                extras.put(WeexDevOptions.EXTRA_DEVICE_ID, deviceId);
                extras.put(WeexDevOptions.EXTRA_WS_URL,wsUrl);
                performStart(context,from,extras);

            }
        } else if(!TextUtils.isEmpty(cmd_wx_server)) {
            DebugTool.startRemoteDebug(cmd_wx_server);
        }
    }

    private void performStart(@NonNull Context context, String from, Map<String,String> extras) {
        if(!SDKUtils.isHostRunning(context) || !SDKUtils.isInteractive(context)) {
            Log.d(Constants.TAG,"service start failed(host app is not in foreground,is your app running?)");
            return;
        }
        WXLogUtils.d(Constants.TAG,"analyzer service will start...");
        Intent intent = new Intent(context,AnalyzerService.class);
        intent.putExtra("from",from);
        if(extras != null) {
            for(Map.Entry<String,String> me : extras.entrySet()) {
                intent.putExtra(me.getKey(),me.getValue());
            }
        }

        context.startService(intent);
    }

    private void performStop(@NonNull Context context) {
        Intent intent = new Intent(context,AnalyzerService.class);
        context.stopService(intent);
    }

}

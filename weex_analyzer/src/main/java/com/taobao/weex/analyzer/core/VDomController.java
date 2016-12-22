package com.taobao.weex.analyzer.core;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.pojo.HealthReport;
import com.taobao.weex.utils.WXLogUtils;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class VDomController implements Handler.Callback{
    private HandlerThreadWrapper mHandlerThreadWrapper;
    private static final int TRACK = 1;

    public VDomController() {
        mHandlerThreadWrapper = new HandlerThreadWrapper("vdom-tracker",this);
    }

    public void trackVDom(@NonNull WXSDKInstance instance){
        if(mHandlerThreadWrapper.isAlive()) {
            Message msg = Message.obtain();
            msg.what = TRACK;
            msg.obj = instance;
            mHandlerThreadWrapper.getHandler().sendMessage(msg);
        }
    }

    public void destroy() {
        if(mHandlerThreadWrapper != null) {
            mHandlerThreadWrapper.quit();
            mHandlerThreadWrapper = null;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(msg.what == TRACK) {
            try {
                VDomTracker tracker = new VDomTracker((WXSDKInstance) msg.obj);
                HealthReport report = tracker.traverse();
                if(report != null) {
                    report.writeToConsole();
                }
            }catch (Exception e) {
                WXLogUtils.e(e.getMessage());
            }
            return true;
        }
        return false;
    }
}

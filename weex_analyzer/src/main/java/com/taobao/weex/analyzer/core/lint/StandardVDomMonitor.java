package com.taobao.weex.analyzer.core.lint;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.core.HandlerThreadWrapper;
import com.taobao.weex.analyzer.pojo.HealthReport;
import com.taobao.weex.utils.WXLogUtils;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class StandardVDomMonitor implements IVDomMonitor, Handler.Callback {

    private HandlerThreadWrapper mHandlerThreadWrapper;
    private WXSDKInstance mInstance;
    private LayoutChangeListener mLayoutChangeListener;

    private static final int TRACK = 1;

    private static final String TAG = "VDomController";

    public StandardVDomMonitor() {
        mHandlerThreadWrapper = new HandlerThreadWrapper("vdom-tracker",this);
        mLayoutChangeListener = new LayoutChangeListener();
    }

    @Override
    public void monitor(@NonNull WXSDKInstance instance) {
        this.mInstance = instance;
//        View view = instance.getGodCom().getHostView();
        View view = instance.getContainerView();
        if(view == null) {
            WXLogUtils.e(TAG,"host view is null");
            return;
        }

        view.getViewTreeObserver().addOnGlobalLayoutListener(mLayoutChangeListener);
    }

    @Override
    public void destroy() {
        if(mHandlerThreadWrapper != null) {
            mHandlerThreadWrapper.quit();
            mHandlerThreadWrapper = null;
        }

        if(mInstance != null && mLayoutChangeListener != null) {
            View hostView = mInstance.getContainerView();
            if(hostView != null) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    hostView.getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutChangeListener);
                }else {
                    hostView.getViewTreeObserver().removeOnGlobalLayoutListener(mLayoutChangeListener);
                }
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(msg.what == TRACK) {
            try {
                DomTracker tracker = new DomTracker((WXSDKInstance) msg.obj);//todo 此处需优化
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

    private class LayoutChangeListener implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {
            if(mInstance == null) {
                WXLogUtils.e("detect layout change but instance is null");
                return;
            }

            WXLogUtils.d(TAG,"we detect that layout has changed for instance " + mInstance.getInstanceId());
            if(mHandlerThreadWrapper.isAlive()) {
                Message msg = Message.obtain();
                msg.what = TRACK;
                msg.obj = mInstance;
                mHandlerThreadWrapper.getHandler().sendMessage(msg);
            }
        }
    }


}

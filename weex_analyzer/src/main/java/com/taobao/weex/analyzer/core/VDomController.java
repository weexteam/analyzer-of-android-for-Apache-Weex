package com.taobao.weex.analyzer.core;

import android.support.annotation.NonNull;

import com.taobao.weex.WXSDKInstance;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class VDomController implements IVDomMonitor {
    static boolean isPollingMode = false;
    static boolean isStandardMode = false;

    private PollingVDomMonitor mPollingVDomMonitor;
    private StandardVDomMonitor mStandardVDomMonitor;

    public VDomController() {
        mPollingVDomMonitor = new PollingVDomMonitor();
        mStandardVDomMonitor = new StandardVDomMonitor();
    }

    @Override
    public void monitor(@NonNull WXSDKInstance instance) {
        if (isPollingMode) {
            mPollingVDomMonitor.monitor(instance);
        } else if(isStandardMode) {
            mStandardVDomMonitor.monitor(instance);
        }
    }

    @Override
    public void destroy() {
        mPollingVDomMonitor.destroy();
        mStandardVDomMonitor.destroy();
    }
}

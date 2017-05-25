package com.taobao.weex.analyzer.core.lint;

import android.support.annotation.NonNull;

import com.taobao.weex.WXSDKInstance;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class VDomController implements IVDomMonitor {
    public static boolean isPollingMode = false;
    public static boolean isStandardMode = false;

    private PollingVDomMonitor mPollingVDomMonitor;
    private StandardVDomMonitor mStandardVDomMonitor;

    public VDomController(@NonNull PollingVDomMonitor pollingVDomMonitor,@NonNull StandardVDomMonitor standardVDomMonitor) {
        mPollingVDomMonitor = pollingVDomMonitor;
        mStandardVDomMonitor = standardVDomMonitor;
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

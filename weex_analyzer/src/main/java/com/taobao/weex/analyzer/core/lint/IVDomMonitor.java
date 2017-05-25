package com.taobao.weex.analyzer.core.lint;

import android.support.annotation.NonNull;

import com.taobao.weex.WXSDKInstance;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public interface IVDomMonitor {
    public void monitor(@NonNull final WXSDKInstance instance);
    public void destroy();
}

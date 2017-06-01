package com.taobao.weex.analyzer.view.alert;

import android.content.Context;

import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.IPermissionHandler;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public abstract class PermissionAlertView extends AbstractAlertView implements IPermissionHandler {
    private Config mConfig;
    public PermissionAlertView(Context context, Config config) {
        super(context);
        this.mConfig = config;
    }

    @Override
    public void show() {
        if(mConfig != null && !isPermissionGranted(mConfig)) {
            return;
        }
        super.show();
    }
}
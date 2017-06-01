package com.taobao.weex.analyzer.view.overlay;

import android.content.Context;

import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.IPermissionHandler;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public abstract class PermissionOverlayView extends DragSupportOverlayView implements IPermissionHandler{
    protected Config mConfig;

    public PermissionOverlayView(Context application) {
        super(application);
        mConfig = null;
    }

    public PermissionOverlayView(Context application, boolean enableDrag) {
        super(application, enableDrag);
        mConfig = null;
    }

    public PermissionOverlayView(Context application, boolean enableDrag, Config config) {
        super(application, enableDrag);
        mConfig = config;
    }

    @Override
    public void show() {
        if(mConfig != null && !isPermissionGranted(mConfig)) {
            return;
        }
        super.show();
    }
}

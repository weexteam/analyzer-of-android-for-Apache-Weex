package com.taobao.weex.analyzer;

import android.support.annotation.NonNull;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public interface IPermissionHandler {
    boolean isPermissionGranted(@NonNull Config config);
}

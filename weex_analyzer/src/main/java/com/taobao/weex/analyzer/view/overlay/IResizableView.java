package com.taobao.weex.analyzer.view.overlay;

import android.support.annotation.IntDef;
import android.view.View;

import com.taobao.weex.analyzer.core.logcat.LogView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public interface IResizableView {

    @IntDef({LogView.Size.SMALL, LogView.Size.MEDIUM, LogView.Size.LARGE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Size {
        int SMALL = 0;
        int MEDIUM = 1;
        int LARGE = 2;
    }

    interface OnSizeChangedListener {
        void onSizeChanged(@Size int size);
    }

    void setViewSize(@LogView.Size int size, View contentView, boolean allowFireEvent);
}

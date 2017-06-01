package com.taobao.weex.analyzer.view.overlay;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;

import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.core.logcat.LogView;
import com.taobao.weex.analyzer.utils.ViewUtils;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public abstract class AbstractResizableOverlayView extends PermissionOverlayView implements IResizableView {

    private OnSizeChangedListener mOnSizeChangedListener;
    protected int mViewSize = LogView.Size.MEDIUM;

    public AbstractResizableOverlayView(Context application, Config config) {
        super(application,true,config);
    }

    public void setOnSizeChangedListener(@NonNull OnSizeChangedListener listener) {
        this.mOnSizeChangedListener = listener;
    }

    public void setViewSize(@Size int size) {
        this.mViewSize = size;
    }

    @Override
    public void setViewSize(@Size int size, @Nullable View contentView, boolean allowFireEvent) {
        if (contentView == null) {
            return;
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        if (params == null) {
            return;
        }

        //hard code here..
        final int heightSmall = (int) ViewUtils.dp2px(mContext, 200);
        final int heightMedium = (int) ViewUtils.dp2px(mContext, 350);
        final int heightLarge = FrameLayout.LayoutParams.MATCH_PARENT;

        int height = params.height;
        switch (size) {
            case LogView.Size.SMALL:
                height = heightSmall;
                break;
            case LogView.Size.MEDIUM:
                height = heightMedium;
                break;
            case LogView.Size.LARGE:
                height = heightLarge;
                break;
        }
        if (height != params.height) {
            params.height = height;
            contentView.setLayoutParams(params);
            if (mOnSizeChangedListener != null && allowFireEvent) {
                mOnSizeChangedListener.onSizeChanged(size);
            }
        }
    }

}

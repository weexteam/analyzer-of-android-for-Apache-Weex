package com.taobao.weex.analyzer.core.inspector.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.analyzer.view.overlay.PermissionOverlayView;
import com.taobao.weex.analyzer.view.highlight.ViewHighlighter;

import java.util.concurrent.Executors;

import static com.taobao.weex.analyzer.R.id.close;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class InspectorView extends PermissionOverlayView {

    private ViewHighlighter mViewHighlighter;
    private GestureDetector mGestureDetector;
    private ViewInspectorManager mInspectorManager;
    private OnCloseListener mOnCloseListener;

    private static final int INSPECTOR_COLOR = 0x420000ff;

    private WXInspectorItemView mVirtualInspectorItemView;
    private WXInspectorItemView mNativeInspectorItemView;

    private TextView virtualDomBtn,nativeLayoutBtn;

    private View closeBtn;
    private TextView mTips;

    private static final int BTN_ENABLED_COLOR = 0xBCCDDC39;
    private static final int BTN_DISABLED_COLOR = 0x00ffffff;

    public InspectorView(Context application, Config config) {
        super(application,true,config);
        mWidth = WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    public boolean isPermissionGranted(@NonNull Config config) {
        return !config.getIgnoreOptions().contains(Config.TYPE_VIEW_INSPECTOR);
    }

    @NonNull
    @Override
    protected View onCreateView() {
        View hostView = View.inflate(mContext, R.layout.wxt_inspector_view, null);

        mVirtualInspectorItemView = (WXInspectorItemView) hostView.findViewById(R.id.panel_virtual_dom);
        mVirtualInspectorItemView.setType(WXInspectorItemView.TYPE_VIRTUAL_DOM);

        mNativeInspectorItemView = (WXInspectorItemView) hostView.findViewById(R.id.panel_native_layout);
        mNativeInspectorItemView.setType(WXInspectorItemView.TYPE_NATIVE_LAYOUT);

        virtualDomBtn = (TextView) hostView.findViewById(R.id.btn_panel_virtual_dom);
        nativeLayoutBtn = (TextView) hostView.findViewById(R.id.btn_panel_native_layout);
        closeBtn = hostView.findViewById(close);
        mTips = (TextView) hostView.findViewById(R.id.tips);

        virtualDomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVirtualInspectorItemView.setVisibility(View.VISIBLE);
                mNativeInspectorItemView.setVisibility(View.GONE);
                virtualDomBtn.setBackgroundColor(BTN_ENABLED_COLOR);
                nativeLayoutBtn.setBackgroundColor(BTN_DISABLED_COLOR);
            }
        });

        nativeLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVirtualInspectorItemView.setVisibility(View.GONE);
                mNativeInspectorItemView.setVisibility(View.VISIBLE);
                nativeLayoutBtn.setBackgroundColor(BTN_ENABLED_COLOR);
                virtualDomBtn.setBackgroundColor(BTN_DISABLED_COLOR);
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isViewAttached && mOnCloseListener != null) {
                    mOnCloseListener.close(InspectorView.this);
                    dismiss();
                }
            }
        });

        return hostView;
    }

    public void setOnCloseListener(@Nullable OnCloseListener listener) {
        this.mOnCloseListener = listener;
    }

    public void bindInstance(@Nullable WXSDKInstance instance) {
        if (mInspectorManager != null) {
            mInspectorManager.setInstance(instance);
        }
    }

    public void receiveTouchEvent(@NonNull MotionEvent event) {
        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    //nope
                }

                @Override
                public void onShowPress(MotionEvent e) {
                    if (mInspectorManager != null) {
                        mInspectorManager.inspectByMotionEvent(e);
                    }
                }
            });
        }
        //delegate all touch event
        mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onShown() {
        mInspectorManager = ViewInspectorManager.newInstance(Executors.newSingleThreadExecutor(),
                new ViewPropertiesSupplier(), new ViewInspectorManager.OnInspectorListener() {
                    @Override
                    public void onInspectorSuccess(@NonNull ViewInspectorManager.InspectorInfo info) {
                        //we should be called in main thread
                        notifyOnInspectorSuccess(info);
                    }

                    @Override
                    public void onInspectorFailed(@NonNull String msg) {
                        //did not hit
                    }
                });
    }

    private void notifyOnInspectorSuccess(@NonNull ViewInspectorManager.InspectorInfo info) {
        if (info.targetView == null || mWholeView == null) {
            return;
        }
        if (mViewHighlighter == null) {
            mViewHighlighter = ViewHighlighter.newInstance();
        }
        //highlight target view
        mViewHighlighter.setHighlightedView(info.targetView, INSPECTOR_COLOR);

        //render overlay view

        if(info.targetComponent != null) {
            String tips = ViewUtils.getComponentName(info.targetComponent);
            mTips.setText("tips:你选中了weex元素["+tips+"]");
        } else {
            if(mContext != null && info.targetView != null) {
                mTips.setText("tips:你选中了native元素["+info.targetView.getClass().getSimpleName()+"]");
            }
        }

        if(info.virtualViewInfo != null) {
            mVirtualInspectorItemView.inflateData(info);
        }

        if(info.nativeViewInfo != null) {
            mNativeInspectorItemView.inflateData(info);
        }
    }



    @Override
    protected void onDismiss() {
        mGestureDetector = null;
        if (mInspectorManager != null) {
            mInspectorManager.destroy();
            mInspectorManager = null;
        }
        if(mViewHighlighter != null) {
            mViewHighlighter.clearHighlight();
        }
        mViewHighlighter = null;
    }
}


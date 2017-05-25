package com.taobao.weex.analyzer.core.inspector.view;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.annotation.JSONField;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXEmbed;
import com.taobao.weex.ui.component.WXVContainer;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public final class ViewInspectorManager {

    private OnInspectorListener mListener;
    private ViewPropertiesSupplier mSupplier;
    private ExecutorService mExecutor;
    private WXSDKInstance mInstance;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ViewInspectorManager(@NonNull ExecutorService executor, @NonNull ViewPropertiesSupplier supplier, @NonNull OnInspectorListener listener) {
        this.mListener = listener;
        this.mSupplier = supplier;
        this.mExecutor = executor;
    }

    public void setInstance(@Nullable WXSDKInstance instance) {
        this.mInstance = instance;
    }

    @NonNull
    public static ViewInspectorManager newInstance(@NonNull ExecutorService executor,@NonNull ViewPropertiesSupplier supplier,@NonNull OnInspectorListener listener) {
        return new ViewInspectorManager(executor,supplier,listener);
    }


    void inspectByMotionEvent(@NonNull final MotionEvent event) {
        //1. 后序遍历视图树，寻找target view
        //2. 遍历virtual dom树，寻找target virtual view
        //3. 收集信息
        //4. callback
        if(mExecutor == null || mInstance == null) {
            return;
        }

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mInstance == null) {
                    return;
                }
                WXComponent rootComponent = mInstance.getRootComponent();
                if(rootComponent == null) {
                    return;
                }
                View rootView = rootComponent.getHostView();
                if(rootView == null) {
                    return;
                }
                View targetView = findPossibleTouchedView(rootView,event.getRawX(),event.getRawY());
                if(targetView != null) {
                    WXComponent targetComponent = findBoundComponentBy(targetView,rootComponent);

                    final InspectorInfo inspectorInfo = new InspectorInfo();
                    inspectorInfo.targetView = targetView;
                    inspectorInfo.targetComponent = targetComponent;
                    inspectorInfo.nativeViewInfo = Collections.emptyMap();
                    inspectorInfo.virtualViewInfo = Collections.emptyMap();

                    if(targetComponent != null) {
                        inspectorInfo.simpleName = ViewUtils.getComponentName(targetComponent);
                    } else {
                        inspectorInfo.simpleName = targetView.getClass().getSimpleName();
                    }

                    if(mSupplier != null) {
                        Map<String,String> nativeViewInfo = mSupplier.supplyPropertiesFromNativeView(targetView);
                        Map<String,String> virtualViewInfo = null;
                        if(targetComponent != null) {
                            virtualViewInfo = mSupplier.supplyPropertiesFromVirtualView(targetComponent);
                        }
                        inspectorInfo.nativeViewInfo = nativeViewInfo;
                        inspectorInfo.virtualViewInfo = virtualViewInfo;
                    }

                    if(mListener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onInspectorSuccess(inspectorInfo);
                            }
                        });
                    }
                } else {
                    if(mListener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onInspectorFailed("target view not found");
                            }
                        });
                    }
                }
            }
        });
    }

    @Nullable
    private View findPossibleTouchedView(@NonNull View rootView,float x,float y) {
        Deque<View> deque = new ArrayDeque<>();
        deque.add(rootView);
        View target = null;
        int[] location = new int[2];
        while(!deque.isEmpty()) {
            View view = deque.removeFirst();
            view.getLocationInWindow(location);
            if(x > location[0] && x < (location[0]+view.getWidth()) && y > location[1] && y < (location[1]+view.getHeight())) {
                target = view;
            }
            if(view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0,len = group.getChildCount(); i < len; i++) {
                    View child = group.getChildAt(i);
                    deque.add(child);
                }
            }
        }
        return target;
    }

    @Nullable
    private WXComponent findBoundComponentBy(@NonNull View targetView,@NonNull WXComponent rootComponent) {
        Deque<WXComponent> deque = new ArrayDeque<>();
        deque.add(rootComponent);
        WXComponent targetComponent = null;

        while (!deque.isEmpty()) {
            WXComponent component = deque.removeFirst();

            View view = component.getHostView();
            if(view != null && view.equals(targetView)) {
                targetComponent = component;
            }

            //we should take embed into account
            if(component instanceof WXEmbed) {
                WXComponent nestedRootComponent = ViewUtils.getNestedRootComponent((WXEmbed) component);
                if(nestedRootComponent != null) {
                    deque.add(nestedRootComponent);
                }
            } else if(component instanceof WXVContainer) {
                WXVContainer container = (WXVContainer) component;
                for(int i = 0,len = container.getChildCount(); i < len; i++) {
                    WXComponent c = container.getChild(i);
                    deque.add(c);
                }
            }
        }

        return targetComponent;
    }

    public void destroy() {
        if(mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }
        mSupplier = null;
        mListener = null;
        mInstance = null;
        if(mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    interface OnInspectorListener {
        void onInspectorSuccess(@NonNull InspectorInfo info);
        void onInspectorFailed(@NonNull String msg);
    }


    public static class InspectorInfo {
        public Map<String,String> virtualViewInfo;
        public Map<String,String> nativeViewInfo;
        @JSONField(serialize = false)
        public WXComponent targetComponent;
        @JSONField(serialize = false)
        public View targetView;
        public String simpleName;

        @Override
        public String toString() {
            return "InspectorInfo{" +
                    "virtualViewInfo=" + virtualViewInfo +
                    ", nativeViewInfo=" + nativeViewInfo +
                    ", targetComponent=" + targetComponent +
                    ", targetView=" + targetView +
                    ", simpleName='" + simpleName + '\'' +
                    '}';
        }
    }
}

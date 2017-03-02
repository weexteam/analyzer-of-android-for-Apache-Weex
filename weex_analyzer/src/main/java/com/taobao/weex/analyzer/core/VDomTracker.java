package com.taobao.weex.analyzer.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.pojo.HealthReport;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.ui.component.WXA;
import com.taobao.weex.ui.component.WXBasicComponentType;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXDiv;
import com.taobao.weex.ui.component.WXEmbed;
import com.taobao.weex.ui.component.WXHeader;
import com.taobao.weex.ui.component.WXImage;
import com.taobao.weex.ui.component.WXInput;
import com.taobao.weex.ui.component.WXLoading;
import com.taobao.weex.ui.component.WXScroller;
import com.taobao.weex.ui.component.WXSlider;
import com.taobao.weex.ui.component.WXSwitch;
import com.taobao.weex.ui.component.WXText;
import com.taobao.weex.ui.component.WXVContainer;
import com.taobao.weex.ui.component.WXVideo;
import com.taobao.weex.ui.component.list.HorizontalListComponent;
import com.taobao.weex.ui.component.list.WXCell;
import com.taobao.weex.ui.component.list.WXListComponent;
import com.taobao.weex.ui.view.WXEditText;
import com.taobao.weex.utils.WXLogUtils;
import com.taobao.weex.utils.WXViewUtils;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class VDomTracker {
    private WXSDKInstance mWxInstance;
    private Deque<LayeredVDomNode> mLayeredQueue;
    private ObjectPool<LayeredVDomNode> mObjectPool;

    private static final String TAG = "VDomTracker";

    private static final Map<Class, String> sVDomMap;
    private OnTrackNodeListener mOnTrackNodeListener;

    private static final int START_LAYER = 2;//instance为第一层，rootComponent为第二层

    static {
        sVDomMap = new HashMap<>();
        sVDomMap.put(WXComponent.class, "component");
        sVDomMap.put(WXText.class, WXBasicComponentType.TEXT);
        sVDomMap.put(WXVContainer.class, WXBasicComponentType.CONTAINER);
        sVDomMap.put(WXDiv.class, WXBasicComponentType.DIV);
        sVDomMap.put(WXEditText.class, WXBasicComponentType.TEXTAREA);
        sVDomMap.put(WXA.class, WXBasicComponentType.A);
        sVDomMap.put(WXInput.class, WXBasicComponentType.INPUT);
        sVDomMap.put(WXLoading.class, WXBasicComponentType.LOADING);
        sVDomMap.put(WXScroller.class, WXBasicComponentType.SCROLLER);
        sVDomMap.put(WXSwitch.class, WXBasicComponentType.SWITCH);
        sVDomMap.put(WXSlider.class, WXBasicComponentType.SLIDER);
        sVDomMap.put(WXVideo.class, WXBasicComponentType.VIDEO);
        sVDomMap.put(WXImage.class, WXBasicComponentType.IMAGE);
        sVDomMap.put(WXHeader.class, WXBasicComponentType.HEADER);
        sVDomMap.put(WXEmbed.class, WXBasicComponentType.EMBED);
        sVDomMap.put(WXListComponent.class, WXBasicComponentType.LIST);
        sVDomMap.put(HorizontalListComponent.class, WXBasicComponentType.HLIST);
        sVDomMap.put(WXCell.class, WXBasicComponentType.CELL);
    }

    public interface OnTrackNodeListener {
        void onTrackNode(@NonNull WXComponent component, int layer);
    }

    public VDomTracker(@NonNull WXSDKInstance instance) {
        this.mWxInstance = instance;
        mLayeredQueue = new ArrayDeque<>();
        mObjectPool = new ObjectPool<LayeredVDomNode>(10) {
            @Override
            LayeredVDomNode newObject() {
                return new LayeredVDomNode();
            }
        };
    }

    public void setOnTrackNodeListener(OnTrackNodeListener listener) {
        this.mOnTrackNodeListener = listener;
    }

    @Nullable
    public HealthReport traverse() {
        long start = System.currentTimeMillis();
        if (SDKUtils.isInUiThread()) {
            WXLogUtils.e(TAG, "illegal thread...");
            return null;
        }

        WXComponent godComponent = mWxInstance.getRootComponent();
        if (godComponent == null) {
            WXLogUtils.e(TAG, "god component not found");
            return null;
        }

        LayeredVDomNode layeredNode = mObjectPool.obtain();
        layeredNode.set(godComponent, getComponentName(godComponent), START_LAYER);

        mLayeredQueue.add(layeredNode);

        HealthReport report = new HealthReport(mWxInstance.getBundleUrl());

        while (!mLayeredQueue.isEmpty()) {
            LayeredVDomNode domNode = mLayeredQueue.removeFirst();
            WXComponent component = domNode.component;
            int layer = domNode.layer;

            report.maxLayer = Math.max(report.maxLayer,layer);

            //如果节点被染色，需要计算其该染色子树的最大深度
            if(!TextUtils.isEmpty(domNode.tint)) {
                for(HealthReport.EmbedDesc desc : report.embedDescList) {
                    if(desc.src != null && desc.src.equals(domNode.tint)) {
                        desc.actualMaxLayer = Math.max(desc.actualMaxLayer,(layer-desc.beginLayer));
                    }
                }
            }

            if (mOnTrackNodeListener != null && component != null) {
                mOnTrackNodeListener.onTrackNode(component, layer);
            }

            if (component instanceof WXListComponent) {
                report.hasList = true;
            } else if (component instanceof WXScroller) {
                if(component.getDomObject() != null && component.getDomObject().getAttrs() != null
                        && "vertical".equals(component.getDomObject().getAttrs().getScrollDirection())) {
                    report.hasScroller = true;
                }

            } else if (component instanceof WXCell) {
                report.cellNum++;
                if(((WXCell) component).getHostView() != null) {
                    int height = ((WXCell) component).getHostView().getMeasuredHeight();
                    report.hasBigCell |= isBigCell(height);
                }

                report.maxCellViewNum = Math.max(report.maxCellViewNum,getCellViewNum(component));
            } else if(component instanceof WXEmbed) {
                report.hasEmbed = true;
            }

            //restore to pool for later use
            domNode.clear();
            mObjectPool.recycle(domNode);

            if(component instanceof WXEmbed) {

                if(report.embedDescList == null) {
                    report.embedDescList = new ArrayList<>();
                }
                HealthReport.EmbedDesc desc = new HealthReport.EmbedDesc();
                desc.src = ((WXEmbed)component).getSrc();
                desc.beginLayer = layer;

                report.embedDescList.add(desc);

                //add nested component to layeredQueue
                WXComponent nestedRootComponent = getNestedRootComponent((WXEmbed) component);
                if(nestedRootComponent != null) {
                    LayeredVDomNode childNode = mObjectPool.obtain();
                    childNode.set(nestedRootComponent, getComponentName(nestedRootComponent), layer+1);
                    mLayeredQueue.add(childNode);

                    //tint
                    childNode.tint = desc.src;
                }
            } else if (component instanceof WXVContainer) {
                WXVContainer container = (WXVContainer) component;
                for (int i = 0, count = container.childCount(); i < count; i++) {
                    WXComponent child = container.getChild(i);
                    LayeredVDomNode childNode = mObjectPool.obtain();
                    childNode.set(child, getComponentName(child), layer + 1);

                    //if parent is tinted,then tint it's children
                    if(!TextUtils.isEmpty(domNode.tint)) {
                        childNode.tint = domNode.tint;
                    }

                    mLayeredQueue.add(childNode);

                }
            }
        }

        long end = System.currentTimeMillis();
        WXLogUtils.d(TAG,"[traverse] elapse time :"+(end-start)+"ms");
        return report;

    }

    @Nullable
    private WXComponent getNestedRootComponent(@NonNull WXEmbed embed) {
        try {
            Class embedClazz = embed.getClass();
            Field field = embedClazz.getDeclaredField("mNestedInstance");
            field.setAccessible(true);
            WXSDKInstance nestedInstance = (WXSDKInstance) field.get(embed);
            if(nestedInstance == null) {
                return null;
            }
            return nestedInstance.getRootComponent();

        }catch (Exception e) {
            WXLogUtils.e(TAG,e.getMessage());
        }
        return null;
    }

    private int getCellViewNum(@NonNull WXComponent cellNode) {
        Deque<WXComponent> deque = new ArrayDeque<>();
        deque.add(cellNode);
        int viewNum = 0;
        while (!deque.isEmpty()) {
            WXComponent node = deque.removeFirst();
            viewNum++;
            if (node instanceof WXVContainer) {
                WXVContainer container = (WXVContainer) node;
                for (int i = 0, count = container.childCount(); i < count; i++) {
                    deque.add(container.getChild(i));
                }
            }
        }
        return viewNum;
    }

    private boolean isBigCell(float maxHeight) {
        if (maxHeight <= 0) {
            return false;
        }
        return maxHeight > WXViewUtils.getScreenHeight() * 2 / 3.0;
    }

    @NonNull
    private String getComponentName(@NonNull WXComponent component) {
        String name = sVDomMap.get(component.getClass());
        return TextUtils.isEmpty(name) ? "component" : name;
    }

    private static class LayeredVDomNode {
        WXComponent component;
        String simpleName;
        int layer;

        String tint;

        void set(WXComponent component, String simpleName, int layer) {
            this.component = component;
            this.layer = layer;
            this.simpleName = simpleName;
        }

        void clear() {
            component = null;
            layer = -1;
            simpleName = null;
        }
    }

    private static abstract class ObjectPool<T> {
        private final Deque<T> mPool;

        ObjectPool(int capacity) {
            capacity = Math.max(0, capacity);
            this.mPool = new ArrayDeque<>(capacity);
            for (int i = 0; i < capacity; i++) {
                this.mPool.add(newObject());
            }
        }

        abstract T newObject();

        T obtain() {
            return mPool.isEmpty() ? newObject() : mPool.removeLast();
        }

        void recycle(@NonNull T obj) {
            mPool.addLast(obj);
        }

    }

}

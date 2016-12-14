package com.taobao.weex.analyzer.core;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.taobao.weex.WXSDKInstance;
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

import java.util.ArrayDeque;
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
    private Deque<VDomNode> mLayeredQueue;
    private ObjectPool<VDomNode> mObjectPool;

    private static final String TAG = "VDomTracker";

    private static final Map<Class,String> sVDomMap;
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

    public VDomTracker(@NonNull WXSDKInstance instance) {
        this.mWxInstance = instance;
        mLayeredQueue = new ArrayDeque<>();
        mObjectPool = new ObjectPool<VDomNode>(10) {
            @Override
            VDomNode newObject() {
                return new VDomNode();
            }
        };
    }

    public void traverse() {
        WXComponent godComponent = mWxInstance.getGodCom();
        if(godComponent == null) {
            return;
        }

        VDomNode layeredNode = mObjectPool.obtain();
        layeredNode.set(godComponent,getComponentName(godComponent),1);

        mLayeredQueue.add(layeredNode);

        while (!mLayeredQueue.isEmpty()) {
            VDomNode domNode = mLayeredQueue.removeFirst();
            WXComponent component = domNode.component;
            int layer = domNode.layer;
            String name = domNode.simpleName;

            //todo
            WXLogUtils.e(TAG,"component: "+ name + ",layer: "+ layer);

            //restore to pool
            domNode.clear();
            mObjectPool.recycle(domNode);

            if(component instanceof WXVContainer) {
                WXVContainer container = (WXVContainer) component;
                for (int i = 0,count = container.childCount(); i < count; i++) {
                    WXComponent child = container.getChild(i);
                    VDomNode childNode = mObjectPool.obtain();
                    childNode.set(child,getComponentName(child),layer+1);

                    mLayeredQueue.add(childNode);
                }
            }
        }
    }

    @NonNull
    private String getComponentName(@NonNull WXComponent component){
        String name = sVDomMap.get(component.getClass());
        return TextUtils.isEmpty(name) ? "component" : name;
    }

    private static class VDomNode {
        WXComponent component;
        String simpleName;
        int layer;

        void set(WXComponent component,String simpleName, int layer) {
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

        ObjectPool(int capacity){
            capacity = Math.max(0,capacity);
            this.mPool = new ArrayDeque<>(capacity);
            for (int i = 0; i < capacity; i++) {
                this.mPool.add(newObject());
            }
        }

        abstract T newObject();

        T obtain(){
            return mPool.isEmpty() ? newObject() : mPool.removeLast();
        }

        void recycle(@NonNull T obj) {
            mPool.addLast(obj);
        }

    }

}

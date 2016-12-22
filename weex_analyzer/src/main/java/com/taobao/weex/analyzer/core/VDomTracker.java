package com.taobao.weex.analyzer.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.analyzer.pojo.HealthReport;
import com.taobao.weex.analyzer.pojo.NodeInfo;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.dom.WXDomObject;
import com.taobao.weex.dom.WXStyle;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
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

    private Map<WXComponent, NodeInfo> mCachedMap;
    private List<WXComponent> mCachedList;

    private static final String TAG = "VDomTracker";

    private static final Map<Class, String> sVDomMap;

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
        mObjectPool = new ObjectPool<LayeredVDomNode>(10) {
            @Override
            LayeredVDomNode newObject() {
                return new LayeredVDomNode();
            }
        };
    }

    /**
     * todo : need async
     */
    @Nullable
    public HealthReport traverse() {
        if(SDKUtils.isInUiThread()) {
            WXLogUtils.e(TAG,"illegal thread...");
            return null;
        }

        WXComponent godComponent = mWxInstance.getGodCom();
        if (godComponent == null) {
            WXLogUtils.e(TAG, "god component not found");
            return null;
        }

        LayeredVDomNode layeredNode = mObjectPool.obtain();
        layeredNode.set(godComponent, getComponentName(godComponent), 1);

        mLayeredQueue.add(layeredNode);

        if (mCachedMap == null) {
            mCachedMap = new HashMap<>();
        }
        Map<WXComponent, NodeInfo> map = mCachedMap;

        HealthReport report = new HealthReport(mWxInstance.getBundleUrl());

        if (mCachedList == null) {
            mCachedList = new ArrayList<>();
        }
        List<WXComponent> cellList = mCachedList;

        while (!mLayeredQueue.isEmpty()) {
            LayeredVDomNode domNode = mLayeredQueue.removeFirst();
            WXComponent component = domNode.component;
            int layer = domNode.layer;

            if ("WXListComponent".equals(component.getClass().getSimpleName())) {
                report.hasList = true;
            }
            if ("WXScroller".equals(component.getClass().getSimpleName())) {
                report.hasScroller = true;
            }
            if ("WXCell".equals(component.getClass().getSimpleName())) {
                cellList.add(component);
            }

            NodeInfo nodeInfo = map.get(component);
            if (nodeInfo == null) {
                nodeInfo = createNode(domNode);
                map.put(component, nodeInfo);
            }

            //restore to pool for later use
            domNode.clear();
            mObjectPool.recycle(domNode);

            if (component instanceof WXVContainer) {
                WXVContainer container = (WXVContainer) component;
                for (int i = 0, count = container.childCount(); i < count; i++) {
                    WXComponent child = container.getChild(i);
                    LayeredVDomNode childNode = mObjectPool.obtain();
                    childNode.set(child, getComponentName(child), layer + 1);
                    report.maxLayer = layer + 1;

                    mLayeredQueue.add(childNode);

                    NodeInfo childNodeInfo = createNode(childNode);

                    if (nodeInfo.children == null) {
                        nodeInfo.children = new ArrayList<>();
                    }
                    nodeInfo.children.add(childNodeInfo);
                    map.put(child, childNodeInfo);
                }
            }
        }

        NodeInfo tree = map.get(godComponent);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            WXLogUtils.e(e.getMessage());
        }

        boolean isBigCell = false;
        for (WXComponent com : cellList) {
            if(com.getHostView() != null) {
                isBigCell = isBigCell(com.getHostView().getMeasuredHeight());
            }
            NodeInfo cellNode = map.get(com);
            int viewNum = getCellViewNum(cellNode);
            if(isBigCell) {
                WXLogUtils.d(Constants.TAG,"[warning]please do not use big cell in list component or will cause terrible user experience");
                WXLogUtils.d(Constants.TAG, JSON.toJSONString(cellNode, SerializerFeature.PrettyFormat));
            }
            report.maxCellViewNum = Math.max(report.maxCellViewNum, viewNum);
        }

        cellList.clear();
        map.clear();
        report.vdom = tree;
        report.hasBigCell = isBigCell;
        return report;

    }

    private int getCellViewNum(NodeInfo cellNode) {
        Deque<NodeInfo> deque = new ArrayDeque<>();
        deque.add(cellNode);
        int viewNum = 0;
        while (!deque.isEmpty()) {
            NodeInfo node = deque.removeFirst();
            viewNum++;
            if(node.children != null && !node.children.isEmpty()) {
                for(NodeInfo n : node.children) {
                    deque.add(n);
                }
            }
        }
        return viewNum;
    }

    private boolean isBigCell(float maxHeight) {
        if(maxHeight <= 0) {
            return false;
        }
        return maxHeight > WXViewUtils.getScreenHeight()*2/3.0;
    }

    @NonNull
    private String getComponentName(@NonNull WXComponent component) {
        String name = sVDomMap.get(component.getClass());
        return TextUtils.isEmpty(name) ? "component" : name;
    }

    private NodeInfo createNode(@NonNull LayeredVDomNode layeredNode) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.layer = layeredNode.layer;
        nodeInfo.simpleName = layeredNode.simpleName;
        nodeInfo.realName = layeredNode.component.getClass().getName();

        WXDomObject domObject = layeredNode.component.getDomObject();
        WXStyle styles = null;
        if (domObject != null) {
            styles = domObject.getStyles();
        }
        if (styles != null && !styles.isEmpty()) {
            nodeInfo.styles = Collections.unmodifiableMap(styles);
        }
        return nodeInfo;
    }

    private static class LayeredVDomNode {
        WXComponent component;
        String simpleName;
        int layer;

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

    private static class CellInfo {
        int viewNum;
        boolean isBigCell;
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

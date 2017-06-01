package com.taobao.weex.analyzer.pojo;

import android.support.annotation.NonNull;
import android.util.Log;

import com.alibaba.fastjson.annotation.JSONField;
import com.taobao.weex.analyzer.core.Constants;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class HealthReport {
    /**
     * 是否使用list
     */
    public boolean hasList;
    /**
     * 是否使用scroller
     */
    public boolean hasScroller;
    /**
     * 是否使用大cell
     */
    public boolean hasBigCell;
    /**
     * 最深嵌套层级(virtual dom)
     */
    @JSONField(name = "maxLayerOfVDom")
    public int maxLayer;

    /**
     * native view层级
     * */
    public int maxLayerOfRealDom;
    /**
     * cell下view个数
     */
    @JSONField(serialize = false)
    public int maxCellViewNum;

    @JSONField(serialize = false)
    public int componentNumOfBigCell;

    /**
     * 扩展字段
     */
    @JSONField(serialize = false)
    public Map<String, String> extendProps;

    /**
     * 是否包含embed标签
     */
    public boolean hasEmbed;

    /**
     * 预估总高度
     * */
    public int estimateContentHeight;

    /**
     * 预估屏数
     * */
    public String estimatePages;

    public List<EmbedDesc> embedDescList;

    public Map<String/*ref*/,ListDesc> listDescMap;

    private String bundleUrl;

    public HealthReport() {
    }

    public HealthReport(@NonNull String bundleUrl) {
        this.bundleUrl = bundleUrl;
    }

    public void writeToConsole() {
        Log.d(Constants.TAG, "health report(" + bundleUrl + ")");
        Log.d(Constants.TAG, "[health report] maxLayer:" + maxLayer);
        Log.d(Constants.TAG, "[health report] maxLayerOfRealDom:" + maxLayerOfRealDom);
        Log.d(Constants.TAG, "[health report] hasList:" + hasList);
        Log.d(Constants.TAG, "[health report] hasScroller:" + hasScroller);
        Log.d(Constants.TAG, "[health report] hasBigCell:" + hasBigCell);
        Log.d(Constants.TAG, "[health report] maxCellViewNum:" + maxCellViewNum);

        if(listDescMap != null && !listDescMap.isEmpty()) {
            Log.d(Constants.TAG, "[health report] listNum:" + listDescMap.size());
            for (ListDesc desc : listDescMap.values()) {
                Log.d(Constants.TAG, "[health report] listDesc: (ref:" + desc.ref + ",cellNum:"+desc.cellNum
                        + ",totalHeight:" + desc.totalHeight + "px)");
            }
        }

        Log.d(Constants.TAG, "[health report] hasEmbed:" + hasEmbed);


        if (embedDescList != null && !embedDescList.isEmpty()) {
            Log.d(Constants.TAG, "[health report] embedNum:" + embedDescList.size());
            for (EmbedDesc desc : embedDescList) {
                Log.d(Constants.TAG, "[health report] embedDesc: (src:" + desc.src + ",layer:" + desc.actualMaxLayer + ")");

            }
        }

        Log.d(Constants.TAG,"[health report] estimateContentHeight:"+estimateContentHeight+"px"+",estimatePages:"+estimatePages);


        Log.d(Constants.TAG, "\n");

        if (extendProps != null) {
            for (Map.Entry<String, String> me : extendProps.entrySet()) {
                Log.d(Constants.TAG, "[health report] " + me.getKey() + ":" + me.getValue() + ")");
            }
        }
    }

    public static class EmbedDesc {
        /**
         * embed标签的源
         */
        public String src;
        /**
         * embed标签起始的层级
         */
        public int beginLayer;
        /**
         * embed自身内容实际层级(embed嵌套embed的情况，不计算子embed标签深度)
         */
        public int actualMaxLayer;
    }

    public static class ListDesc {
        public String ref;//list唯一标识
        public int totalHeight;//list估计高度
        public int cellNum;//此list的cell个数
    }

}

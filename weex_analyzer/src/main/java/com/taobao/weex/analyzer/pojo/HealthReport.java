package com.taobao.weex.analyzer.pojo;

import android.support.annotation.NonNull;
import android.util.Log;

import com.taobao.weex.analyzer.core.Constants;

import java.util.Map;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class HealthReport {
    /**vdom树*/
    public NodeInfo vdom;
    /**是否使用list*/
    public boolean hasList;
    /**是否使用scroller*/
    public boolean hasScroller;
    /**是否使用大cell*/
    public boolean hasBigCell;
    /**最深嵌套层级*/
    public int maxLayer;
    /**cell下view个数*/
    public int maxCellViewNum;
    /**当前cell个数*/
    public int cellNum;
    /**扩展字段*/
    public Map<String,String> extendProps;

    /**是否包含embed标签*/
    public boolean hasEmbed;

    private String bundleUrl;

    public HealthReport(@NonNull String bundleUrl){
        this.bundleUrl = bundleUrl;
    }

    public void writeToConsole() {
        Log.d(Constants.TAG,"health report("+bundleUrl+")");
        Log.d(Constants.TAG,"[health report] maxLayer:" + maxLayer);
        Log.d(Constants.TAG,"[health report] hasList:" + hasList);
        Log.d(Constants.TAG,"[health report] hasScroller:" + hasScroller);
        Log.d(Constants.TAG,"[health report] hasEmbed:"+hasEmbed);
        Log.d(Constants.TAG,"[health report] hasBigCell:" + hasBigCell);
        Log.d(Constants.TAG,"[health report] maxCellViewNum:" + maxCellViewNum);
        Log.d(Constants.TAG,"[health report] cellNum:" + cellNum);
        Log.d(Constants.TAG,"\n");

        if(extendProps != null) {
            for(Map.Entry<String,String> me : extendProps.entrySet()) {
                Log.d(Constants.TAG,"[health report] " + me.getKey() + ":" + me.getValue() + ")");
            }
        }
    }
}

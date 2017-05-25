package com.taobao.weex.analyzer.core.weex;

import com.taobao.weex.WXEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/1<br/>
 * Time: 上午9:29<br/>
 */

public class Performance {

    /**
     * Time spent for reading, time unit is ms.
     */
    public double localReadTime;

    /**
     * URL used for rendering view, optional
     */
    public String templateUrl;

    public String pageName = null;

    /**
     * Size of JavaScript framework, the unit is KB
     */
    public double JSLibSize;

    /**
     * Time of initial JavaScript library
     */
    public long JSLibInitTime;

    /**
     * Size of JavaScript template
     */
    public double JSTemplateSize;

    public long templateLoadTime;

    /**
     * Time used for
     * {@link com.taobao.weex.bridge.WXBridgeManager#createInstance(String, String, Map, String)}
     */
    public long communicateTime;

    /**
     * Time spent when rendering first screen
     */
    public long screenRenderTime;

    public long sdkInitTime = WXEnvironment.sSDKInitTime;

    /**
     * Call native Time spent when rendering first screen
     */
    public long callNativeTime;

    /**
     * Create Instance Time spent when rendering first screen
     */
    public long firstScreenJSFExecuteTime;

    /**
     * Call native Time spent when rendering first screen
     */
    public long batchTime;

    /**
     * Call native Time spent when rendering first screen
     */
    public long parseJsonTime;

    /**
     *  UpdateDomObj Time spent when rendering first screen
     */
    public long updateDomObjTime;

    /**
     *  ApplyUpdate Time spent when rendering first screen
     */
    public long applyUpdateTime;


    /**
     *  CssLayout Time spent when rendering first screen
     */
    public long cssLayoutTime;

    /**
     * Time spent, the unit is micro second
     */
    public double totalTime;

    /**
     * load bundle js time, unite ms
     */
    public long networkTime;

    /**
     * pure network time;
     */
    public long pureNetworkTime;

    public long actualNetworkTime;

    /**
     * component Count
     */
    public long componentCount;

    /**
     * Version of JavaScript libraray
     */
    public String JSLibVersion = WXEnvironment.JS_LIB_SDK_VERSION;

    /**
     * Version of Weex SDK
     */
    public String WXSDKVersion = WXEnvironment.WXSDK_VERSION;


    public String requestType;

    public String connectionType;

    public static List<String> transfer(Performance performance){
        List<String> list = new ArrayList<>();
        list.add("pageName : " + performance.pageName);
        list.add("templateUrl : " + performance.templateUrl);
        list.add("jslib version : " + performance.JSLibVersion);
        list.add("component count : "+ performance.componentCount);
        list.add("JSLibInitTime : "+ performance.JSLibInitTime);
        list.add("JSLibSize : "+ performance.JSLibSize);
        list.add("JSTemplateSize : "+ performance.JSTemplateSize);
        list.add("localReadTime : "+ performance.localReadTime);
        list.add("templateLoadTime : "+ performance.templateLoadTime);

        list.add("actualNetworkTime : "+ performance.actualNetworkTime);
        list.add("pureNetworkTime : "+ performance.pureNetworkTime);
        list.add("networkTime : "+ performance.networkTime);

        list.add("cssLayoutTime : "+ performance.cssLayoutTime);
        list.add("applyUpdateTime : "+ performance.applyUpdateTime);
        list.add("updateDomObjTime : "+ performance.updateDomObjTime);
        list.add("parseJsonTime : "+ performance.parseJsonTime);
        list.add("batchTime : "+ performance.batchTime);

        list.add("firstScreenJSFExecuteTime : "+ performance.firstScreenJSFExecuteTime);
        list.add("callNativeTime : "+ performance.callNativeTime);

        list.add("communicateTime : "+ performance.communicateTime);
        list.add("requestType : " + performance.requestType);
        list.add("connectionType : " + performance.connectionType);
        return Collections.unmodifiableList(list);
    }


    @Override
    public String toString() {
        return "Performance{" +
                "localReadTime=" + localReadTime +
                ", templateUrl='" + templateUrl + '\'' +
                ", pageName='" + pageName + '\'' +
                ", JSLibSize=" + JSLibSize +
                ", JSLibInitTime=" + JSLibInitTime +
                ", JSTemplateSize=" + JSTemplateSize +
                ", templateLoadTime=" + templateLoadTime +
                ", communicateTime=" + communicateTime +
                ", screenRenderTime=" + screenRenderTime +
                ", sdkInitTime=" + sdkInitTime +
                ", callNativeTime=" + callNativeTime +
                ", firstScreenJSFExecuteTime=" + firstScreenJSFExecuteTime +
                ", batchTime=" + batchTime +
                ", parseJsonTime=" + parseJsonTime +
                ", updateDomObjTime=" + updateDomObjTime +
                ", applyUpdateTime=" + applyUpdateTime +
                ", cssLayoutTime=" + cssLayoutTime +
                ", totalTime=" + totalTime +
                ", networkTime=" + networkTime +
                ", pureNetworkTime=" + pureNetworkTime +
                ", actualNetworkTime=" + actualNetworkTime +
                ", componentCount=" + componentCount +
                ", JSLibVersion='" + JSLibVersion + '\'' +
                ", WXSDKVersion='" + WXSDKVersion + '\'' +
                ", requestType='" + requestType + '\'' +
                ", connectionType='" + connectionType + '\'' +
                '}';
    }
}

package com.taobao.weex.analyzer.view;

import android.support.annotation.DrawableRes;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/4<br/>
 * Time: 下午3:40<br/>
 */

public class DevOption {
    public String optionName;
    @DrawableRes public int iconRes;
    public OnOptionClickListener listener;
    public boolean isOverlayView;
    public boolean isPermissionGranted = true;

    public DevOption(){
    }

    public DevOption(String optionName,int iconRes, OnOptionClickListener listener){
        this(optionName,iconRes,listener,false);
    }

    public DevOption(String optionName, int iconRes, OnOptionClickListener listener, boolean isOverlayView) {
        this(optionName,iconRes,listener,isOverlayView,true);
    }

    public DevOption(String optionName, int iconRes, OnOptionClickListener listener, boolean isOverlayView, boolean isPermissionGranted) {
        this.optionName = optionName;
        this.iconRes = iconRes;
        this.listener = listener;
        this.isOverlayView = isOverlayView;
        this.isPermissionGranted = isPermissionGranted;
    }

    public interface OnOptionClickListener {
        void onOptionClick();
    }
}

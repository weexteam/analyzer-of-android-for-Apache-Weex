package com.taobao.weex.analyzer.view;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

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

    public DevOption(){
    }

    public DevOption(String optionName,int iconRes){
        this.optionName = optionName;
        this.iconRes = iconRes;
    }

    public DevOption(String optionName,int iconRes, OnOptionClickListener listener){
        this(optionName,iconRes);
        this.listener = listener;
    }


    public interface OnOptionClickListener {
        void onOptionClick(@NonNull String optionName);
    }
}

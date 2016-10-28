package com.taobao.weex.analyzer.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/1<br/>
 * Time: 下午3:49<br/>
 */

public class ViewUtils {

    private ViewUtils(){}

    public static float dp2px(Context context,int dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,context.getResources().getDisplayMetrics());
    }

    public static float sp2px(Context context,int sp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,sp,context.getResources().getDisplayMetrics());
    }

    public static double findSuitableVal(double value,int step){
        if(value <= 0 || step <= 0){
            return 0;
        }
        int temp = (int) value;
        while (temp % step != 0){
            temp++;
        }
        return temp;
    }
}

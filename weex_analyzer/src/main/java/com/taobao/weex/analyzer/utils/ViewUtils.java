package com.taobao.weex.analyzer.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXEmbed;
import com.taobao.weex.utils.WXLogUtils;

import java.lang.reflect.Field;

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

    @Nullable
    public static WXComponent getNestedRootComponent(@NonNull WXEmbed embed) {
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
            WXLogUtils.e(e.getMessage());
        }
        return null;
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

package com.taobao.weex.analyzer.core;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.taobao.weex.dom.ImmutableDomObject;
import com.taobao.weex.ui.component.WXComponent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class ViewPropertiesSupplier {

    @NonNull
    public Map<String,String> supplyPropertiesFromVirtualView(@NonNull WXComponent component) {
        ImmutableDomObject domObject = component.getDomObject();
        if(domObject == null ) {
            return Collections.emptyMap();
        }
        Map<String,String> result = new LinkedHashMap<>();

        if(domObject.getStyles() != null) {
            for (Map.Entry<String, Object> entry : domObject.getStyles().entrySet()) {
                if (entry.getValue() != null) {
                    result.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        if(domObject.getAttrs() != null) {
            for(Map.Entry<String,Object> entry : domObject.getAttrs().entrySet()) {
                if(entry.getValue() != null) {
                    result.put(entry.getKey(),entry.getValue().toString());
                }
            }
        }

        return Collections.unmodifiableMap(result);
    }

    @NonNull
    public Map<String,String> supplyPropertiesFromNativeView(@NonNull View view) {
        Map<String,String> result = new LinkedHashMap<>();
        result.put(BoxModelConstants.LEFT, String.valueOf(view.getLeft()));
        result.put(BoxModelConstants.TOP, String.valueOf(view.getTop()));
        result.put(BoxModelConstants.RIGHT, String.valueOf(view.getRight()));
        result.put(BoxModelConstants.BOTTOM, String.valueOf(view.getBottom()));

        result.put(BoxModelConstants.WIDTH, String.valueOf(view.getWidth()));
        result.put(BoxModelConstants.HEIGHT, String.valueOf(view.getHeight()));

        result.put(BoxModelConstants.PADDING_LEFT, String.valueOf(view.getPaddingLeft()));
        result.put(BoxModelConstants.PADDING_TOP, String.valueOf(view.getPaddingTop()));
        result.put(BoxModelConstants.PADDING_RIGHT, String.valueOf(view.getPaddingRight()));
        result.put(BoxModelConstants.PADDING_BOTTOM, String.valueOf(view.getPaddingBottom()));

        result.put(BoxModelConstants.VISIBILITY, (view.getVisibility() == View.VISIBLE ? "visible":"invisible"));

        ViewGroup.LayoutParams params = view.getLayoutParams();
        if(params != null && params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
            result.put(BoxModelConstants.MARGIN_LEFT, String.valueOf(marginLayoutParams.leftMargin));
            result.put(BoxModelConstants.MARGIN_TOP, String.valueOf(marginLayoutParams.topMargin));
            result.put(BoxModelConstants.MARGIN_RIGHT, String.valueOf(marginLayoutParams.rightMargin));
            result.put(BoxModelConstants.MARGIN_BOTTOM, String.valueOf(marginLayoutParams.bottomMargin));
        }

        result.put(BoxModelConstants.BORDER_LEFT_WIDTH, String.valueOf(0));
        result.put(BoxModelConstants.BORDER_RIGHT_WIDTH, String.valueOf(0));
        result.put(BoxModelConstants.BORDER_TOP_WIDTH, String.valueOf(0));
        result.put(BoxModelConstants.BORDER_BOTTOM_WIDTH, String.valueOf(0));

        //todo background color
        //text color
        //text

        return Collections.unmodifiableMap(result);
    }


    public static class BoxModelConstants {
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String TOP = "top";
        public static final String LEFT = "left";
        public static final String RIGHT = "right";
        public static final String BOTTOM = "bottom";
        public static final String MARGIN_TOP = "margin-top";
        public static final String MARGIN_LEFT = "margin-left";
        public static final String MARGIN_RIGHT = "margin-right";
        public static final String MARGIN_BOTTOM = "margin-bottom";
        public static final String PADDING_TOP = "padding-top";
        public static final String PADDING_LEFT = "padding-left";
        public static final String PADDING_RIGHT = "padding-right";
        public static final String PADDING_BOTTOM = "padding-bottom";
        public static final String BORDER_LEFT_WIDTH = "border-left-width";
        public static final String BORDER_RIGHT_WIDTH = "border-right-width";
        public static final String BORDER_TOP_WIDTH = "border-top-width";
        public static final String BORDER_BOTTOM_WIDTH = "border-bottom-width";

        public static final String VISIBILITY = "visibility";
    }

}

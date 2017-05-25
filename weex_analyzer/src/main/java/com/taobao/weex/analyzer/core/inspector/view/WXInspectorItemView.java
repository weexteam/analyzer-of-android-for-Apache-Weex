package com.taobao.weex.analyzer.core.inspector.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.view.overlay.AbstractBizItemView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class WXInspectorItemView extends AbstractBizItemView<ViewInspectorManager.InspectorInfo> {
    private TextView mContent;
    private CSSBoxModelView mBoxView;

    public static final String TYPE_VIRTUAL_DOM = "virtual_dom";
    public static final String TYPE_NATIVE_LAYOUT = "native_layout";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TYPE_VIRTUAL_DOM,TYPE_NATIVE_LAYOUT})
    public @interface Type {
    }

    @Type
    private String mType;

    public WXInspectorItemView(Context context) {
        super(context);
    }

    public WXInspectorItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WXInspectorItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void prepareView() {
        mContent = (TextView) findViewById(R.id.content);
        mBoxView = (CSSBoxModelView) findViewById(R.id.box_model);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wxt_panel_inspector_view;
    }

    public void setType(@Type String type) {
        this.mType = type;
    }

    @Override
    public void inflateData(ViewInspectorManager.InspectorInfo data) {
        if(TYPE_VIRTUAL_DOM.equals(mType)) {
            mBoxView.setNative(false);
            applyInspectorInfoToBoxView(data.virtualViewInfo,mBoxView);
            StringBuilder builder = new StringBuilder();

            for(Map.Entry<String,String> entry : data.virtualViewInfo.entrySet()) {
                if(!TextUtils.isEmpty(entry.getValue()) && !"0".equals(entry.getValue())) {
                    builder.append(entry.getKey())
                            .append(" : ")
                            .append(entry.getValue())
                            .append("\n");
                }
            }
            mContent.setText(builder.toString());
        } else if(TYPE_NATIVE_LAYOUT.equals(mType)) {
            mBoxView.setNative(true);
            applyInspectorInfoToBoxView(data.nativeViewInfo,mBoxView);
            StringBuilder builder = new StringBuilder();
            for(Map.Entry<String,String> entry : data.nativeViewInfo.entrySet()) {
                if(!TextUtils.isEmpty(entry.getValue()) && !"0".equals(entry.getValue())) {
                    builder.append(entry.getKey())
                            .append(" : ")
                            .append(entry.getValue())
                            .append("\n");
                }
            }
            mContent.setText(builder.toString());
        }
    }


    @VisibleForTesting
    static String getPureValue(@Nullable String rawValue) {
        if(rawValue == null || "".equals(rawValue.trim())) {
            return "0";
        }
        // 四舍五入 去掉小数点
        // 去掉px/wx等单位
        String digits = rawValue.replaceAll("[^0-9.-]", "");
        int dotIndex = digits.indexOf('.');
        int len = digits.length();
        if(dotIndex >= 0) {
            if (len-1 > dotIndex) {
                try {
                    double d = Double.valueOf(digits);
                    d = Math.round(d);
                    return String.valueOf((int)d);
                }catch (Exception e) {
                    return digits.substring(0,dotIndex);
                }
            } else {
                return digits.substring(0,dotIndex);
            }
        } else {
            return digits;
        }
    }


    private void applyInspectorInfoToBoxView(@NonNull Map<String,String> inspectorInfo,@NonNull CSSBoxModelView boxView) {
        if(!TextUtils.isEmpty(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN))) {
            String margin = inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN);
            margin = getPureValue(margin);
            boxView.setMarginLeftText(margin);
            boxView.setMarginRightText(margin);
            boxView.setMarginTopText(margin);
            boxView.setMarginBottomText(margin);
        } else {
            boxView.setMarginLeftText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN_LEFT)));
            boxView.setMarginRightText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN_RIGHT)));
            boxView.setMarginTopText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN_TOP)));
            boxView.setMarginBottomText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN_BOTTOM)));
        }

        if(!TextUtils.isEmpty(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_WIDTH))){
            String border = inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_WIDTH);
            border = getPureValue(border);
            boxView.setBorderLeftText(border);
            boxView.setBorderRightText(border);
            boxView.setBorderTopText(border);
            boxView.setBorderBottomText(border);
        } else {
            boxView.setBorderLeftText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_LEFT_WIDTH)));
            boxView.setBorderRightText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_RIGHT_WIDTH)));
            boxView.setBorderTopText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_TOP_WIDTH)));
            boxView.setBorderBottomText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_BOTTOM_WIDTH)));
        }

        if(!TextUtils.isEmpty(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING))){
            String padding = inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING);
            padding = getPureValue(padding);
            boxView.setPaddingLeftText(padding);
            boxView.setPaddingRightText(padding);
            boxView.setPaddingTopText(padding);
            boxView.setPaddingBottomText(padding);
        } else {
            boxView.setPaddingLeftText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING_LEFT)));
            boxView.setPaddingRightText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING_RIGHT)));
            boxView.setPaddingTopText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING_TOP)));
            boxView.setPaddingBottomText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING_BOTTOM)));
        }

        boxView.setWidthText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.WIDTH)));
        boxView.setHeightText(getPureValue(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.HEIGHT)));

        boxView.invalidate();
    }


}

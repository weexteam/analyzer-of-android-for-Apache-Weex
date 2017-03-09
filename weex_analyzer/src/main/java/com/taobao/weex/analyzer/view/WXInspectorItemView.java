package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.ViewInspectorManager;
import com.taobao.weex.analyzer.core.ViewPropertiesSupplier;
import com.taobao.weex.analyzer.utils.ViewUtils;

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
        return R.layout.wxt_panel_inspector_virtual_dom;
    }

    public void setType(@Type String type) {
        this.mType = type;
    }

    @Override
    protected void inflateData(ViewInspectorManager.InspectorInfo data) {
        if(TYPE_VIRTUAL_DOM.equals(mType)) {
            applyInspectorInfoToBoxView(data.virtualViewInfo,mBoxView);
            StringBuilder builder = new StringBuilder();
            if(data.targetComponent != null) {
                builder.append("component name")
                        .append(" : ")
                        .append(ViewUtils.getComponentName(data.targetComponent))
                        .append("\n");
            }

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


    private void applyInspectorInfoToBoxView(@NonNull Map<String,String> inspectorInfo,@NonNull CSSBoxModelView boxView) {
        if(!TextUtils.isEmpty(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN))) {
            String margin = inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN);
            boxView.setMarginLeftText(margin);
            boxView.setMarginRightText(margin);
            boxView.setMarginTopText(margin);
            boxView.setMarginBottomText(margin);
        } else {
            boxView.setMarginLeftText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN_LEFT));
            boxView.setMarginRightText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN_RIGHT));
            boxView.setMarginTopText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN_TOP));
            boxView.setMarginBottomText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.MARGIN_BOTTOM));
        }

        if(!TextUtils.isEmpty(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_WIDTH))){
            String border = inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_WIDTH);
            boxView.setBorderLeftText(border);
            boxView.setBorderRightText(border);
            boxView.setBorderTopText(border);
            boxView.setBorderBottomText(border);
        } else {
            boxView.setBorderLeftText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_LEFT_WIDTH));
            boxView.setBorderRightText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_RIGHT_WIDTH));
            boxView.setBorderTopText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_TOP_WIDTH));
            boxView.setBorderBottomText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.BORDER_BOTTOM_WIDTH));
        }

        if(!TextUtils.isEmpty(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING))){
            String padding = inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING);
            boxView.setPaddingLeftText(padding);
            boxView.setPaddingRightText(padding);
            boxView.setPaddingTopText(padding);
            boxView.setPaddingBottomText(padding);
        } else {
            boxView.setPaddingLeftText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING_LEFT));
            boxView.setPaddingRightText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING_RIGHT));
            boxView.setPaddingTopText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING_TOP));
            boxView.setPaddingBottomText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.PADDING_BOTTOM));
        }

        boxView.setWidthText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.WIDTH));
        boxView.setHeightText(inspectorInfo.get(ViewPropertiesSupplier.BoxModelConstants.HEIGHT));

        boxView.invalidate();
    }


}

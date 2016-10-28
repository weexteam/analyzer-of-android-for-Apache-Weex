package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.taobao.weex.analyzer.utils.ViewUtils;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/24<br/>
 * Time: 下午2:52<br/>
 */

public class SimpleOverlayView extends DragSupportOverlayView {

    public interface OnClickListener{
        void onClick(@NonNull IOverlayView view);
    }

    private OnClickListener mOnClickListener;
    private String mTitle;

    public SimpleOverlayView(Context application,@NonNull String title) {
        super(application);
        this.mTitle = title;

        mWidth = (int) ViewUtils.dp2px(mContext,40);
        mHeight = (int) ViewUtils.dp2px(mContext,25);
    }

    public void setOnClickListener(@Nullable OnClickListener listener){
        this.mOnClickListener = listener;
    }

    @NonNull
    @Override
    protected View onCreateView() {
        TextView textView = new TextView(mContext);
        textView.setTextColor(Color.WHITE);
        textView.setBackgroundColor(Color.parseColor("#ba000000"));
        textView.setGravity(Gravity.CENTER);
        textView.setText(mTitle);
        if(mOnClickListener != null){
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.onClick(SimpleOverlayView.this);
                }
            });
        }
        return textView;
    }

    @Override
    protected void onShown() {
    }

    @Override
    protected void onDismiss() {
    }
}

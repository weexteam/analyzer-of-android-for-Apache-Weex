package com.taobao.weex.analyzer.view.overlay;

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

    public interface OnClickListener {
        void onClick(@NonNull IOverlayView view);
    }

    private OnClickListener mOnClickListener;
    private String mTitle;
    private int mBackgroundColor = Color.parseColor("#ba000000");
    private int mTextColor = Color.WHITE;

    private SimpleOverlayView(Context application, @NonNull String title) {
        super(application);
        this.mTitle = title;

        mWidth = (int) ViewUtils.dp2px(mContext, 40);
        mHeight = (int) ViewUtils.dp2px(mContext, 25);
    }

    void setOnClickListener(@Nullable OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    @NonNull
    @Override
    protected View onCreateView() {
        TextView textView = new TextView(mContext);
        textView.setTextColor(mTextColor);
        textView.setBackgroundColor(mBackgroundColor);
        textView.setGravity(Gravity.CENTER);
        textView.setText(mTitle);
        if (mOnClickListener != null) {
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


    public static class Builder {
        String title;
        Context context;

        int backgroundColor = Color.parseColor("#ba000000");
        int textColor = Color.WHITE;
        int x;
        int y;
        int width;
        int height;
        int gravity;
        OnClickListener listener;
        boolean enableDrag = true;

        public Builder(@NonNull Context context, @NonNull String title) {
            this.context = context;
            this.title = title;
        }

        public Builder backgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder textColor(int textColor){
            this.textColor = textColor;
            return this;
        }

        public Builder x(int x){
            this.x = x;
            return this;
        }

        public Builder y(int y){
            this.y = y;
            return this;
        }

        public Builder width(int width){
            this.width = width;
            return this;
        }

        public Builder height(int height){
            this.height = height;
            return this;
        }

        public Builder gravity(int gravity){
            this.gravity = gravity;
            return this;
        }

        public Builder listener(OnClickListener listener){
            this.listener = listener;
            return this;
        }

        public Builder enableDrag(boolean enableDrag){
            this.enableDrag = enableDrag;
            return this;
        }

        public SimpleOverlayView build(){
            SimpleOverlayView overlayView = new SimpleOverlayView(context,title);
            if(listener != null){
                overlayView.setOnClickListener(listener);
            }
            if(gravity != 0){
                overlayView.mGravity = gravity;
            }
            if(x > 0){
                overlayView.mX = x;
            }
            if(y > 0){
                overlayView.mY = y;
            }
            if(width > 0){
                overlayView.mWidth = width;
            }
            if(height > 0){
                overlayView.mHeight = height;
            }
            overlayView.mBackgroundColor = backgroundColor;
            overlayView.mTextColor = textColor;
            overlayView.setDragEnabled(enableDrag);
            return overlayView;

        }
    }
}

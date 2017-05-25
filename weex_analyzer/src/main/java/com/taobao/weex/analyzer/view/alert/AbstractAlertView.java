package com.taobao.weex.analyzer.view.alert;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/19<br/>
 * Time: 上午11:40<br/>
 */
public abstract class AbstractAlertView extends Dialog implements IAlertView {

    public AbstractAlertView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.requestFeature(Window.FEATURE_NO_TITLE);

        int layoutId = getLayoutResId();
        if(layoutId != 0){
            setContentView(layoutId);
        }else{
            View contentView = onCreateView();
            if(contentView != null){
                setContentView(contentView);
            }else{
                throw new IllegalArgumentException("initialize failed.check if you have call onCreateView or getLayoutResId");
            }
        }

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        onInitView(window);
    }

    @Override
    public void show() {
        super.show();

        onShown();

        Window window = getWindow();
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.horizontalMargin = 0;
        lp.verticalMargin = 0;
        window.setAttributes(lp);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        onDismiss();
    }

    protected abstract void onInitView(@NonNull Window window);

    protected abstract @LayoutRes int getLayoutResId();

    protected void onShown(){
        //none
    }

    protected void onDismiss() {
        //none
    }

    protected @Nullable View onCreateView(){
        //none
        return null;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}

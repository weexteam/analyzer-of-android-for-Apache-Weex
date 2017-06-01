package com.taobao.weex.analyzer.view.overlay;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/3<br/>
 * Time: 下午4:49<br/>
 */

public abstract class AbstractBizItemView<T> extends FrameLayout {
    public AbstractBizItemView(Context context) {
        super(context);
        init();
    }

    public AbstractBizItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AbstractBizItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(getLayoutResId(), this, true);
        prepareView();
    }

    protected abstract void prepareView();


    protected abstract @LayoutRes int getLayoutResId();


    public void inflateData(T data) {

    }
}

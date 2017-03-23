package com.taobao.weex.analyzer;

import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.taobao.weex.WXSDKInstance;

/**
 * Description: <p> Created by rowandjj(chuyi)<br/> Date: 2016/10/27<br/> Time: 下午4:56<br/>
 */

public interface IWXDevOptions {
    void onCreate();

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();

    void onWeexRenderSuccess(@Nullable WXSDKInstance instance);

    View onWeexViewCreated(WXSDKInstance instance, View view);

    boolean onKeyUp(int keyCode, KeyEvent event);

    void onException(WXSDKInstance instance, String errCode, String msg);

    public void onReceiveTouchEvent(MotionEvent ev);
}

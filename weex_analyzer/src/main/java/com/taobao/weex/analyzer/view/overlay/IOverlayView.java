package com.taobao.weex.analyzer.view.overlay;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/12<br/>
 * Time: 上午10:52<br/>
 */

public interface IOverlayView {
    boolean isViewAttached();

    void show();

    void dismiss();

    interface OnCloseListener {
        void close(IOverlayView host);
    }

    interface ITask{
        void start();
        void stop();
    }
}

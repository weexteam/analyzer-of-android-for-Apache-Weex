package com.taobao.weex.analyzer.core.logcat;

import com.taobao.weex.analyzer.view.overlay.IResizableView;

import java.util.List;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class LogConfig {
    private boolean showLogLevelPanel = true;
    private boolean showLogFilterPanel = true;
    private boolean showSearchPanel = true;

    private int viewSize = -1;

    private List<String> mCustomRule;

    public boolean isShowLogLevelPanel() {
        return showLogLevelPanel;
    }

    public void setShowLogLevelPanel(boolean showLogLevelPanel) {
        this.showLogLevelPanel = showLogLevelPanel;
    }

    public boolean isShowLogFilterPanel() {
        return showLogFilterPanel;
    }

    public void setShowLogFilterPanel(boolean showLogFilterPanel) {
        this.showLogFilterPanel = showLogFilterPanel;
    }

    public boolean isShowSearchPanel() {
        return showSearchPanel;
    }

    public void setShowSearchPanel(boolean showSearchPanel) {
        this.showSearchPanel = showSearchPanel;
    }

    public List<String> getCustomRule() {
        return mCustomRule;
    }

    public void setCustomRule(List<String> customRule) {
        this.mCustomRule = customRule;
    }

    public int getViewSize() {
        return viewSize;
    }

    public void setViewSize(@IResizableView.Size int viewSize) {
        this.viewSize = viewSize;
    }
}

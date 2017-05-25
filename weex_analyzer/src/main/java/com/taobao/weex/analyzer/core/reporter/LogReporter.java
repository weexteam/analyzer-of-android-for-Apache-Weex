package com.taobao.weex.analyzer.core.reporter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.taobao.weex.analyzer.core.Constants;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

class LogReporter implements IDataReporter<String> {

    private boolean isEnabled;

    LogReporter(boolean enabled) {
        this.isEnabled = enabled;
    }

    @Override
    public void report(@NonNull ProcessedData<String> data) {
        if(data.getData() != null) {
            Log.d(Constants.TAG,data.getData());
        }
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}

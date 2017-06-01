package com.taobao.weex.analyzer.core.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.DevOptionsConfig;
import com.taobao.weex.analyzer.core.debug.RemoteDebugManager;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/6<br/>
 * Time: 下午9:04<br/>
 */

public class SettingsActivity extends Activity {

    private CheckBox mCbJSException;
    private CheckBox mCbExceptionNotification;
    private View mBtnConfigDebugIP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wxt_activity_settings);

        final DevOptionsConfig config = DevOptionsConfig.getInstance(this);

        mCbJSException = (CheckBox) findViewById(R.id.cb_js_exception);
        mCbExceptionNotification = (CheckBox) findViewById(R.id.cb_allow_exception_notification);
        mBtnConfigDebugIP = findViewById(R.id.btn_config_debug_ip);

        mCbJSException.setChecked(config.isShownJSException());
        mCbExceptionNotification.setChecked(config.isAllowExceptionNotification());
        mCbJSException.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                config.setShownJSException(isChecked);
            }
        });
        mCbExceptionNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                config.setAllowExceptionNotification(isChecked);
            }
        });

        mBtnConfigDebugIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    RemoteDebugManager.getInstance().requestDebugServer(v.getContext(), false);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public static void launch(@NonNull Context context){
        Intent intent = new Intent(context,SettingsActivity.class);
        context.startActivity(intent);
    }
}

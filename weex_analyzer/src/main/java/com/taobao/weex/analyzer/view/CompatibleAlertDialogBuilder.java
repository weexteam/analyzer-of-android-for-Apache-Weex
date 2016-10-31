package com.taobao.weex.analyzer.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.view.WindowManager;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/10/31<br/>
 * Time: 上午11:56<br/>
 */

public class CompatibleAlertDialogBuilder extends AlertDialog.Builder {
    public CompatibleAlertDialogBuilder(Context context) {
        super(context);
    }

    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();
        try {
            if (dialog.getWindow() != null) {
                int type = WindowManager.LayoutParams.TYPE_TOAST;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    type = WindowManager.LayoutParams.TYPE_PHONE;
                }
                dialog.getWindow().setType(type);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return dialog;
    }
}

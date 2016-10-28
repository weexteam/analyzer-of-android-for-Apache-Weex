package com.taobao.weex.analyzer.core;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.appfram.storage.DefaultWXStorage;
import com.taobao.weex.appfram.storage.IWXStorageAdapter;
import com.taobao.weex.appfram.storage.WXSQLiteOpenHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/10/26<br/>
 * Time: 下午1:03<br/>
 */

public class StorageHacker {

    public interface OnLoadListener {
        void onLoad(List<StorageInfo> list);
    }

    public interface OnRemoveListener {
        void onRemoved(boolean status);
    }

    private IWXStorageAdapter mStorageAdapter;
    private Context mContext;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public StorageHacker(Context context) {
        mStorageAdapter = WXSDKEngine.getIWXStorageAdapter();
        this.mContext = context;
    }


    @SuppressWarnings("unchecked")
    public void fetch(@Nullable final OnLoadListener listener) {
        if (listener == null) {
            return;
        }

        if (mStorageAdapter == null) {
            listener.onLoad(Collections.<StorageInfo>emptyList());
            return;
        }

        if (!(mStorageAdapter instanceof DefaultWXStorage)) {
            //todo here we can return k-v list.
            listener.onLoad(Collections.<StorageInfo>emptyList());
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<StorageInfo> resultList = new ArrayList<>();

                //todo we don't need reflect in the future
                WXSQLiteOpenHelper helper = null;
                Cursor c = null;
                try {
                    Constructor<WXSQLiteOpenHelper> constructor = WXSQLiteOpenHelper.class.getDeclaredConstructor(Context.class);
                    constructor.setAccessible(true);
                    helper = constructor.newInstance(mContext);

                    Method method = WXSQLiteOpenHelper.class.getDeclaredMethod("getDatabase");
                    method.setAccessible(true);
                    SQLiteDatabase db = (SQLiteDatabase) method.invoke(helper);

                    c = db.query("default_wx_storage", new String[]{"key", "value", "timestamp"}, null, null, null, null, null);

                    while (c.moveToNext()) {
                        StorageInfo info = new StorageInfo();
                        info.key = c.getString(c.getColumnIndex("key"));
                        info.value = c.getString(c.getColumnIndex("value"));
                        info.timestamp = c.getString(c.getColumnIndex("timestamp"));
                        resultList.add(info);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onLoad(resultList);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(c != null){
                        c.close();
                    }
                    if(helper != null){
                        helper.closeDatabase();
                    }
                }

            }
        }).start();

    }

    public void remove(@Nullable final String key, @Nullable final OnRemoveListener listener) {
        if(listener == null || TextUtils.isEmpty(key)){
            return;
        }

        if (mStorageAdapter == null) {
            listener.onRemoved(false);
            return;
        }

        if (!(mStorageAdapter instanceof DefaultWXStorage)) {
            listener.onRemoved(false);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DefaultWXStorage storage = (DefaultWXStorage) mStorageAdapter;
                    Method method = storage.getClass().getDeclaredMethod("performRemoveItem",String.class);
                    if(method != null){
                        method.setAccessible(true);
                        final boolean result = (boolean) method.invoke(storage,key);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onRemoved(result);
                            }
                        });
                        method.setAccessible(false);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public static class StorageInfo {
        public String key;
        public String value;
        public String timestamp;
    }
}

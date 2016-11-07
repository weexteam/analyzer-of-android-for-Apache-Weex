package com.taobao.weex.analyzer.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/7<br/>
 * Time: 上午9:46<br/>
 */

public class LifecycleAwareUtil implements Application.ActivityLifecycleCallbacks {

    private static LifecycleAwareUtil sInstance = null;
    private boolean foreground = false;
    private boolean paused = true;

    private List<Listener> listeners = new CopyOnWriteArrayList<>();
    private Runnable check;
    private Handler handler = new Handler();

    private static final String TAG = "LifecycleAwareUtil";


    public static LifecycleAwareUtil init(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new LifecycleAwareUtil();
            if (context instanceof Application) {
                ((Application) context).registerActivityLifecycleCallbacks(sInstance);
            } else if (context.getApplicationContext() instanceof Application) {
                ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(sInstance);
            }else {
                throw new IllegalStateException("LifecycleAwareUtil is not initialised.[can't obtain application object]");
            }
        }
        return sInstance;
    }


    public static LifecycleAwareUtil get() {
        if (sInstance == null) {
            throw new IllegalStateException("LifecycleAwareUtil is not initialised.");
        }
        return sInstance;
    }

    public static void destroy(@NonNull Context context) {
        if (sInstance == null) {
            return;
        }
        sInstance.removeAllListeners();
        if (context instanceof Application) {
            ((Application) context).unregisterActivityLifecycleCallbacks(sInstance);
        } else if(context.getApplicationContext() instanceof Application) {
            ((Application) context.getApplicationContext()).unregisterActivityLifecycleCallbacks(sInstance);
        }
        sInstance = null;
    }

    public boolean isForeground() {
        return foreground;
    }

    public boolean isBackground() {
        return !foreground;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(TAG,"onActivityCreated,"+activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG,"onActivityResumed,"+activity.getClass().getSimpleName());

        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;

        if (check != null)
            handler.removeCallbacks(check);

        if (wasBackground) {
            for (Listener l : listeners) {
                try {
                    l.onPageForeground();
                } catch (Exception e) {
                    Log.e(TAG, "Listener threw exception!", e);
                }
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(TAG,"onActivityPaused,"+activity.getClass().getSimpleName());

        paused = true;

        if (check != null)
            handler.removeCallbacks(check);

        handler.post(check = new Runnable() {
            @Override
            public void run() {
                if (foreground && paused) {
                    foreground = false;
                    for (Listener l : listeners) {
                        try {
                            l.onPageBackground();
                        } catch (Exception exc) {
                            Log.e(TAG, "Listener threw exception!", exc);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(TAG,"onActivityDestroyed,"+activity.getClass().getSimpleName());


    }


    public interface Listener {
        void onPageForeground();

        void onPageBackground();

    }
}

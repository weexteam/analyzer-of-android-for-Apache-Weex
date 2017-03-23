package com.taobao.weex.analyzer.view.highlight;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public abstract class MutipleViewHighlighter {
    public static MutipleViewHighlighter newInstance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return new MutipleViewHighlighter.OverlayHighlighter();
        } else {
            return new MutipleViewHighlighter.NoOpHighlighter();
        }
    }

    public abstract void clearHighlight();

    public abstract void addHighlightedView(View view);

    public abstract void setColor(@ColorInt int color);

    public boolean isSupport() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    private static final class NoOpHighlighter extends MutipleViewHighlighter {
        @Override
        public void clearHighlight() {
        }

        @Override
        public void addHighlightedView(View view) {
        }

        @Override
        public void setColor(@ColorInt int color) {
        }
    }

    private static class HighLightedView {
        ViewHighlightOverlays highlightOverlays;
        WeakReference<View> viewRef;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static final class OverlayHighlighter extends MutipleViewHighlighter {
        private final Handler mHandler;
        private AtomicInteger mContentColor = new AtomicInteger();
        private CopyOnWriteArrayList<HighLightedView> mViewListToHighlight = new CopyOnWriteArrayList<>();

        OverlayHighlighter() {
            mHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void clearHighlight() {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideHighlightViewOnUiThread();
                }
            }, 0);
        }


        @Override
        public void addHighlightedView(View view) {
            if (view == null) {
                return;
            }
            if (mViewListToHighlight != null && contains(view)) {
                return;
            }
            mHandler.removeCallbacksAndMessages(null);
            HighLightedView node = new HighLightedView();
            node.viewRef = new WeakReference<>(view);

            node.highlightOverlays = ViewHighlightOverlays.newInstance();
            mViewListToHighlight.add(node);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    highlightViewOnUiThread();
                }
            }, 0);

        }

        @Override
        public void setColor(@ColorInt int color) {
            mContentColor.set(color);
        }

        private boolean contains(@NonNull View view) {
            if (mViewListToHighlight == null || mViewListToHighlight.isEmpty()) {
                return false;
            }
            for (HighLightedView highLightedView : mViewListToHighlight) {
                if (highLightedView.viewRef != null && view.equals(highLightedView.viewRef.get())) {
                    return true;
                }
            }
            return false;
        }

        private void highlightViewOnUiThread() {
            for (HighLightedView highLightedView : mViewListToHighlight) {
                if (highLightedView != null && highLightedView.highlightOverlays != null && highLightedView.viewRef != null
                        && highLightedView.viewRef.get() != null) {
                    highLightedView.highlightOverlays.removeHighlight(highLightedView.viewRef.get());
                    highLightedView.highlightOverlays.highlightView(highLightedView.viewRef.get(), mContentColor.get());
                }
            }
        }

        private void hideHighlightViewOnUiThread() {
            for (HighLightedView highLightedView : mViewListToHighlight) {
                if (highLightedView != null && highLightedView.highlightOverlays != null && highLightedView.viewRef != null
                        && highLightedView.viewRef.get() != null) {
                    highLightedView.highlightOverlays.removeHighlight(highLightedView.viewRef.get());
                }
            }
        }
    }
}

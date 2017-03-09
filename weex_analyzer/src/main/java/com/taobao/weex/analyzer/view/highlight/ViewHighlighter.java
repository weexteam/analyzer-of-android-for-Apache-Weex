package com.taobao.weex.analyzer.view.highlight;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ViewHighlighter {

  public static ViewHighlighter newInstance() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      return new OverlayHighlighter();
    } else {
      return new NoopHighlighter();
    }
  }

  protected ViewHighlighter() {
  }

  public abstract void clearHighlight();

  public abstract void setHighlightedView(View view, int color);

  public abstract boolean hasHighlight(View itemView);

  private static final class NoopHighlighter extends ViewHighlighter {
    @Override
    public void clearHighlight() {
    }

    @Override
    public void setHighlightedView(View view, int color) {
    }

    @Override
    public boolean hasHighlight(View itemView) {
      return false;
    }
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  private static final class OverlayHighlighter extends ViewHighlighter {

    private final Handler mHandler;
    private final ViewHighlightOverlays mHighlightOverlays = ViewHighlightOverlays.newInstance();

    // Only assigned on the UI thread
    private View mHighlightedView;

    private AtomicReference<View> mViewToHighlight = new AtomicReference<View>();
    private AtomicInteger mContentColor = new AtomicInteger();

    private final Runnable mHighlightViewOnUiThreadRunnable = new Runnable() {
      @Override
      public void run() {
        highlightViewOnUiThread();
      }
    };

    public OverlayHighlighter() {
      mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void clearHighlight() {
      setHighlightedViewImpl(null, 0);
    }

    @Override
    public void setHighlightedView(View view, int color) {
      setHighlightedViewImpl(view, color);
    }

    @Override
    public boolean hasHighlight(View itemView) {
      return mHighlightedView != null && mHighlightedView.equals(itemView);
    }

    private void setHighlightedViewImpl(@Nullable View view, int color) {
      mHandler.removeCallbacks(mHighlightViewOnUiThreadRunnable);
      mViewToHighlight.set(view);
      mContentColor.set(color);
      mHandler.postDelayed(mHighlightViewOnUiThreadRunnable, 5);
    }

    private void highlightViewOnUiThread() {
      final View viewToHighlight = mViewToHighlight.getAndSet(null);
      if (viewToHighlight == mHighlightedView) {
        return;
      }

      if (mHighlightedView != null) {
        mHighlightOverlays.removeHighlight(mHighlightedView);
      }

      if (viewToHighlight != null) {
        mHighlightOverlays.highlightView(viewToHighlight, mContentColor.get());
      }

      mHighlightedView = viewToHighlight;
    }
  }

}

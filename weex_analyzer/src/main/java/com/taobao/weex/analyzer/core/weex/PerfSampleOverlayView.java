package com.taobao.weex.analyzer.core.weex;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Choreographer;
import android.view.View;
import android.widget.TextView;

import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.core.fps.FPSSampler;
import com.taobao.weex.analyzer.core.memory.MemorySampler;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.view.overlay.PermissionOverlayView;

import java.util.Locale;

/**
 * Description:
 * <p>
 * show memory & fps & dropped frame
 * <p>
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/12<br/>
 * Time: 下午8:55<br/>
 */

public class PerfSampleOverlayView extends PermissionOverlayView {

    private InvalidateUITask mTask;

    public PerfSampleOverlayView(Context application, Config config) {
        super(application,true,config);
    }

    @NonNull
    @Override
    protected View onCreateView() {
        View hostView = View.inflate(mContext, R.layout.wxt_perf_overlay_view, null);

        mX = (int) (mContext.getResources().getDisplayMetrics().widthPixels - ViewUtils.dp2px(mContext, 120));
        mY = (int) (mContext.getResources().getDisplayMetrics().heightPixels - ViewUtils.dp2px(mContext, 64));

        return hostView;
    }

    @Override
    protected void onShown() {
        if(mTask != null){
            mTask.stop();
            mTask = null;
        }
        mTask = new InvalidateUITask(mWholeView);
        mTask.start();
    }

    @Override
    protected void onDismiss() {
        if (mTask != null) {
            mTask.stop();
            mTask = null;
        }
    }

    @Override
    public boolean isPermissionGranted(@NonNull Config config) {
        return !config.getIgnoreOptions().contains(Config.TYPE_ALL_PERFORMANCE);
    }


    private static class InvalidateUITask extends AbstractLoopTask {

        private TextView mMemoryText, mFpsValueText, mFrameSkippedText;
        private FPSSampler mFpsChecker;

        private int mTotalFrameDropped = 0;

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        InvalidateUITask(@NonNull View hostView) {
            super(false);
            mDelayMillis = 1000;
            this.mFpsChecker = new FPSSampler(Choreographer.getInstance());
            this.mMemoryText = (TextView) hostView.findViewById(R.id.memory_usage);
            this.mFpsValueText = (TextView) hostView.findViewById(R.id.fps_value);
            this.mFrameSkippedText = (TextView) hostView.findViewById(R.id.frame_skiped);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        protected void onStart() {
            super.onStart();
            if (mFpsChecker == null) {
                mFpsChecker = new FPSSampler(Choreographer.getInstance());
            }
            mFpsChecker.reset();
            mFpsChecker.start();
        }

        @Override
        protected void onRun() {
            //check memory
            final double usedMemInMB = MemorySampler.getMemoryUsage();
            final double fps;
            if(Build.VERSION.SDK_INT >= 16){
                fps = mFpsChecker.getFPS();
                mTotalFrameDropped += Math.max(mFpsChecker.getExpectedNumFrames() - mFpsChecker.getNumFrames(), 0);
                mFpsChecker.reset();
            }else{
                fps = 0;
            }

            runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    mMemoryText.setText(String.format(Locale.CHINA, "memory : %.2fMB", usedMemInMB));
                    if (Build.VERSION.SDK_INT >= 16) {
                        mFpsValueText.setText(String.format(Locale.CHINA, "fps : %.2f", fps));
                        mFrameSkippedText.setText("" + mTotalFrameDropped + " dropped so far");
                    }else{
                        mFpsValueText.setText("fps : ??");
                        mFrameSkippedText.setText("?? dropped so far");
                    }
                }
            });
        }

        @Override
        protected void onStop() {
            mTotalFrameDropped = 0;
            mFpsChecker.stop();
            mFpsChecker = null;
        }
    }

}

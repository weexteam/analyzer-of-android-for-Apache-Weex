package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.taobao.weex.analyzer.core.AbstractLoopTask;
import com.taobao.weex.analyzer.core.FPSChecker;
import com.taobao.weex.analyzer.core.MemoryChecker;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.analyzer.R;

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

public class PerfCommonOverlayView extends DragSupportOverlayView {

    private InvalidateUITask mTask;

    public PerfCommonOverlayView(Context application) {
        super(application);
    }

    @NonNull
    @Override
    protected View onCreateView() {
        View hostView = View.inflate(mContext, R.layout.perf_overlay_view, null);

        mX = (int) (mContext.getResources().getDisplayMetrics().widthPixels - ViewUtils.dp2px(mContext, 120));
        mY = (int) (mContext.getResources().getDisplayMetrics().heightPixels - ViewUtils.dp2px(mContext, 64));

        return hostView;
    }

    @Override
    protected void onShown() {
        if (mTask == null) {
            mTask = new InvalidateUITask(mWholeView);
            mTask.start();
        }
    }

    @Override
    protected void onDismiss() {
        if (mTask != null) {
            mTask.stop();
            mTask = null;
        }
    }


    private static class InvalidateUITask extends AbstractLoopTask {

        private TextView mMemoryText, mFpsValueText, mFrameSkippedText;
        private FPSChecker mFpsChecker;

        private int mTotalFrameDropped = 0;


        InvalidateUITask(@NonNull View hostView) {
            super(hostView);
            mDelayMillis = 1000;
            this.mFpsChecker = new FPSChecker();
            this.mMemoryText = (TextView) hostView.findViewById(R.id.memory_usage);
            this.mFpsValueText = (TextView) hostView.findViewById(R.id.fps_value);
            this.mFrameSkippedText = (TextView) hostView.findViewById(R.id.frame_skiped);
        }

        @Override
        protected void onStart() {
            super.onStart();
            if (mFpsChecker == null) {
                mFpsChecker = new FPSChecker();
            }
            mFpsChecker.reset();
            mFpsChecker.start();
        }

        @Override
        protected void onRun() {
            //check memory
            double usedMemInMB = MemoryChecker.checkMemoryUsage();
            mMemoryText.setText(String.format(Locale.CHINA, "memory : %.2fMB", usedMemInMB));

            if (Build.VERSION.SDK_INT >= 16) {
                //check fps
                double fps = mFpsChecker.getFPS();
                mTotalFrameDropped += Math.max(mFpsChecker.getExpectedNumFrames() - mFpsChecker.getNumFrames(), 0);
                mFpsChecker.reset();

                mFpsValueText.setText(String.format(Locale.CHINA, "fps : %.2f", fps));
                mFrameSkippedText.setText("" + mTotalFrameDropped + " dropped so far");
            } else {
                mFpsValueText.setText("fps : ??");
                mFrameSkippedText.setText("?? dropped so far");
            }
        }

        @Override
        protected void onStop() {
            mTotalFrameDropped = 0;
            mFpsChecker.stop();
            mFpsChecker = null;
        }
    }

}

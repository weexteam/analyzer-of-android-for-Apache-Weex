package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.analyzer.utils.XiaomiOverlayViewPermissionHelper;
import com.taobao.weex.analyzer.view.alert.AbstractAlertView;
import com.taobao.weex.utils.WXLogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <p> Created by rowandjj(chuyi)<br/> Date: 2016/11/4<br/> Time: 下午1:58<br/>
 */

public class EntranceView extends AbstractAlertView {

    private RecyclerView mList;
    private List<DevOption> mDevOptions;
    private static final String TAG = "EntranceView";

    public EntranceView(Context context) {
        super(context);
    }

    @Override
    protected void onInitView(@NonNull Window window) {
        Context context = getContext();
        mList = (RecyclerView) window.findViewById(R.id.list);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false);
        mList.setLayoutManager(layoutManager);
        mList.addItemDecoration(new Decoration(Color.parseColor("#e0e0e0"), (int) ViewUtils.dp2px(context, 1), 3));

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    protected void onShown() {

        if (mDevOptions == null) {
            return;
        }

        List<DevOption> options = mDevOptions;
        EntranceViewAdapter adapter = new EntranceViewAdapter(getContext(), options);
        mList.setAdapter(adapter);
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wxt_entrance_layout;
    }


    public void registerDevOption(@Nullable DevOption option) {
        if (option == null) {
            return;
        }
        if (mDevOptions == null) {
            mDevOptions = new ArrayList<>();
        }
        mDevOptions.add(option);
    }

    public void registerDevOptions(List<DevOption> options) {
        if (options == null || options.isEmpty()) {
            return;
        }
        if (mDevOptions == null) {
            mDevOptions = new ArrayList<>();
        }

        for(DevOption option : options) {
            if(option.isPermissionGranted) {
                mDevOptions.add(option);
            }
        }
    }

    public static class Creator {
        private List<DevOption> options;
        private Context context;

        public Creator(@NonNull Context context) {
            this.context = context;
            this.options = new ArrayList<>();
        }

        public Creator injectOptions(List<DevOption> options) {
            this.options.addAll(options);
            return this;
        }

        public Creator injectOption(DevOption option) {
            this.options.add(option);
            return this;
        }

        public EntranceView create() {
            EntranceView entranceView = new EntranceView(context);
            entranceView.registerDevOptions(options);
            return entranceView;
        }
    }


    class EntranceViewAdapter extends RecyclerView.Adapter<ViewHolder> {

        List<DevOption> mOptions;
        Context mContext;

        EntranceViewAdapter(Context context, List<DevOption> options) {
            this.mOptions = options;
            this.mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.wxt_option_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(mOptions.get(position));
        }

        @Override
        public int getItemCount() {
            return mOptions == null ? 0 : mOptions.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView optionName;
        ImageView optionIcon;

        DevOption curOption;

        ViewHolder(View itemView) {
            super(itemView);
            optionName = (TextView) itemView.findViewById(R.id.option_name);
            optionIcon = (ImageView) itemView.findViewById(R.id.option_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (curOption != null && curOption.listener != null) {
                        try {
                            if (curOption.isOverlayView) {
                                if(!XiaomiOverlayViewPermissionHelper.isPermissionGranted(v.getContext())) {
                                    Toast.makeText(v.getContext(),"检测到使用了小米手机，可能需要你手动开启悬浮窗权限",Toast.LENGTH_LONG).show();
                                    XiaomiOverlayViewPermissionHelper.requestPermission(v.getContext());
                                    return;
                                } else if(Build.VERSION.SDK_INT >= 25 && !SDKUtils.canDrawOverlays(getContext())) {
                                    WXLogUtils.d(TAG, "we have no permission to draw overlay views.");
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:" + getContext().getPackageName()));
                                    getContext().startActivity(intent);
                                    Toast.makeText(getContext(),"please granted overlay permission before use this option",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            curOption.listener.onOptionClick();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        EntranceView.this.dismiss();
                    }
                }
            });
        }

        void bind(DevOption option) {
            this.curOption = option;
            if (!TextUtils.isEmpty(option.optionName)) {
                optionName.setText(option.optionName);
            }
            if (option.iconRes != 0) {
                optionIcon.setImageResource(option.iconRes);
            }
        }
    }


    static class Decoration extends RecyclerView.ItemDecoration {
        Paint paint;
        int color;
        int size;
        int spanCount;

        Decoration(int color, int size, int spanCount) {
            this.color = color;
            this.size = size;
            this.spanCount = spanCount;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            createPaint();
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                float dividerLeft = child.getX();
                float dividerRight = child.getX() + child.getWidth();
                float dividerTop = child.getY();
                float dividerBottom = child.getY() + child.getHeight();
                //left
                if ((i + 1) % spanCount == 1) {
                    c.drawRect(dividerLeft, dividerTop, dividerLeft + size, dividerBottom, paint);
                }
                //right
                c.drawRect(dividerRight - size, dividerTop, dividerRight, dividerBottom, paint);
                //top
                c.drawRect(dividerLeft, dividerTop, dividerRight, dividerTop + size, paint);
                //bottom
                if (childCount - (i + 1) < spanCount) {
                    c.drawRect(dividerLeft, dividerBottom, dividerRight, dividerBottom + size, paint);
                }

            }
        }


        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
        }

        private void createPaint() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(size);
            paint.setColor(color);
        }
    }

}

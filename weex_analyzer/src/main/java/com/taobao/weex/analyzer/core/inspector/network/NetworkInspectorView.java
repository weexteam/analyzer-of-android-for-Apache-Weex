package com.taobao.weex.analyzer.core.inspector.network;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.NetworkEventSender;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.analyzer.view.overlay.AbstractResizableOverlayView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class NetworkInspectorView extends AbstractResizableOverlayView {

    private NetworkEventListAdapter mAdapter;

    private NetworkEventInspector mNetworkEventInspector;
    private OnCloseListener mOnCloseListener;

    private boolean isSizeMenuOpened;

    public NetworkInspectorView(Context application, Config config) {
        super(application, config);
        mWidth = WindowManager.LayoutParams.MATCH_PARENT;
    }

    public void setOnCloseListener(@Nullable OnCloseListener listener) {
        this.mOnCloseListener = listener;
    }

    @NonNull
    @Override
    protected View onCreateView() {
        View wholeView = View.inflate(mContext, R.layout.wxt_network_inspector_view, null);


        //clear
        View clear = wholeView.findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isViewAttached && mAdapter != null) {
                    mAdapter.clear();
                }
            }
        });

        //hold
        final View hold = wholeView.findViewById(R.id.hold);
        hold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter == null) {
                    return;
                }

                if (isViewAttached) {
                    if (mAdapter.isHoldModeEnabled()) {
                        mAdapter.setHoldModeEnabled(false);
                        ((TextView) hold).setText("hold(off)");
                    } else {
                        mAdapter.setHoldModeEnabled(true);
                        ((TextView) hold).setText("hold(on)");
                    }
                }
            }
        });

        //close
        View close = wholeView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isViewAttached && mOnCloseListener != null) {
                    mOnCloseListener.close(NetworkInspectorView.this);
                    dismiss();
                }
            }
        });


        //recycler view
        final RecyclerView networkEventList = (RecyclerView) wholeView.findViewById(R.id.list);
        networkEventList.setLayoutManager(new LinearLayoutManager(mContext));

        mAdapter = new NetworkEventListAdapter(mContext, networkEventList);
        networkEventList.setAdapter(mAdapter);

        //size
        final TextView sizeBtn = (TextView) wholeView.findViewById(R.id.size);
        final ViewGroup sizeContent = (ViewGroup) wholeView.findViewById(R.id.size_content);
        RadioGroup sizeGroup = (RadioGroup) wholeView.findViewById(R.id.height_group);

        sizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSizeMenuOpened = !isSizeMenuOpened;
                if (isSizeMenuOpened) {
                    sizeContent.setVisibility(View.VISIBLE);
                } else {
                    sizeContent.setVisibility(View.GONE);
                }
            }
        });

        setViewSize(mViewSize, networkEventList, false);
        switch (mViewSize) {
            case Size.SMALL:
                ((RadioButton) wholeView.findViewById(R.id.height_small)).setChecked(true);
                break;
            case Size.MEDIUM:
                ((RadioButton) wholeView.findViewById(R.id.height_medium)).setChecked(true);
                break;
            case Size.LARGE:
                ((RadioButton) wholeView.findViewById(R.id.height_large)).setChecked(true);
                break;
        }


        sizeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.height_small) {
                    mViewSize = Size.SMALL;
                } else if (checkedId == R.id.height_medium) {
                    mViewSize = Size.MEDIUM;
                } else if (checkedId == R.id.height_large) {
                    mViewSize = Size.LARGE;
                }
                setViewSize(mViewSize, networkEventList, true);
            }
        });

        return wholeView;
    }

    @Override
    protected void onShown() {
        mNetworkEventInspector = NetworkEventInspector.createInstance(mContext, new NetworkEventInspector.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(NetworkEventInspector.MessageBean msg) {
                if (mAdapter != null) {
                    mAdapter.addMessage(msg);
                }
            }
        });
    }

    @Override
    protected void onDismiss() {
        if (mNetworkEventInspector != null) {
            mNetworkEventInspector.destroy();
            mNetworkEventInspector = null;
        }
    }

    @Override
    public boolean isPermissionGranted(@NonNull Config config) {
        return !config.getIgnoreOptions().contains(Config.TYPE_MTOP_INSPECTOR);
    }

    private static class NetworkEventListAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<NetworkEventInspector.MessageBean> mMessageList;
        private Context mContext;

        private boolean isHoldMode = false;
        private RecyclerView list;

        NetworkEventListAdapter(Context context, RecyclerView list) {
            mMessageList = new ArrayList<>();
            this.mContext = context;
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.wxt_item_message, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(mMessageList.get(position));
        }

        @Override
        public int getItemCount() {
            return mMessageList == null ? 0 : mMessageList.size();
        }

        void addMessage(@NonNull NetworkEventInspector.MessageBean msg) {
            mMessageList.add(msg);
            notifyItemInserted(mMessageList.size());

            if (!isHoldMode) {
                try {
                    list.smoothScrollToPosition(this.getItemCount() - 1);
                } catch (Exception e) {
                    //ignored
                }
            }
        }

        void clear() {
            mMessageList.clear();
            notifyDataSetChanged();
        }

        void setHoldModeEnabled(boolean enabled) {
            this.isHoldMode = enabled;
        }

        boolean isHoldModeEnabled() {
            return this.isHoldMode;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private NetworkEventInspector.MessageBean mCurMessage;

        private TextView bodyView;
        private TextView typeView;
        private TextView titleView;
        private TextView descView;
        private TextView timestampView;

        private View line;

        private static SimpleDateFormat sFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        ViewHolder(View itemView) {
            super(itemView);
            bodyView = (TextView) itemView.findViewById(R.id.body);
            typeView = (TextView) itemView.findViewById(R.id.type);
            titleView = (TextView) itemView.findViewById(R.id.title);
            descView = (TextView) itemView.findViewById(R.id.desc);
            timestampView = (TextView) itemView.findViewById(R.id.timestamp);
            line = itemView.findViewById(R.id.line);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mCurMessage != null) {
                        try {
                            if (mCurMessage.type != null && mCurMessage.type.equalsIgnoreCase(NetworkEventSender.TYPE_REQUEST)) {
                                if (!TextUtils.isEmpty(mCurMessage.title)) {
                                    SDKUtils.copyToClipboard(v.getContext(), mCurMessage.title, true);
                                }
                            } else if (mCurMessage.type != null && mCurMessage.type.equalsIgnoreCase(NetworkEventSender.TYPE_RESPONSE)) {
                                if (!TextUtils.isEmpty(mCurMessage.body)) {
                                    SDKUtils.copyToClipboard(v.getContext(), mCurMessage.body, true);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });
        }

        void bind(@NonNull NetworkEventInspector.MessageBean msg) {
            this.mCurMessage = msg;

            typeView.setText(TextUtils.isEmpty(msg.type) ? "UNKNOWN" : msg.type.toUpperCase());
            titleView.setText(TextUtils.isEmpty(msg.title) ? "null" : msg.title);

            String desc = TextUtils.isEmpty(msg.desc) ? "null" : msg.desc.toUpperCase();
            if (TextUtils.isEmpty(msg.type)) {
                descView.setText(desc);

                bodyView.setTextColor(Color.WHITE);
                typeView.setTextColor(Color.WHITE);
                titleView.setTextColor(Color.WHITE);
                descView.setTextColor(Color.WHITE);
                timestampView.setTextColor(Color.WHITE);

            } else if (NetworkEventSender.TYPE_REQUEST.equalsIgnoreCase(msg.type)) {
                descView.setText("Method(" + desc + ")");

                bodyView.setTextColor(Color.parseColor("#2196F3"));
                typeView.setTextColor(Color.parseColor("#2196F3"));
                titleView.setTextColor(Color.parseColor("#2196F3"));
                descView.setTextColor(Color.parseColor("#2196F3"));
                timestampView.setTextColor(Color.parseColor("#2196F3"));
            } else if (NetworkEventSender.TYPE_RESPONSE.equalsIgnoreCase(msg.type)) {
                descView.setText("Code(" + desc + ")");

                bodyView.setTextColor(Color.parseColor("#FFEB3B"));
                typeView.setTextColor(Color.parseColor("#FFEB3B"));
                titleView.setTextColor(Color.parseColor("#FFEB3B"));
                descView.setTextColor(Color.parseColor("#FFEB3B"));
                timestampView.setTextColor(Color.parseColor("#FFEB3B"));
            }

            timestampView.setText(now());

            if (!TextUtils.isEmpty(msg.body)) {
                try {
                    if(msg.content != null) {
                        bodyView.setText(JSON.toJSONString(msg.content, true));
                    } else {
                        bodyView.setText(msg.body);
                    }
                } catch (Exception e) {
                    bodyView.setText(msg.body);
                }
                line.setVisibility(View.VISIBLE);
                bodyView.setVisibility(View.VISIBLE);
            } else {
                line.setVisibility(View.GONE);
                bodyView.setVisibility(View.GONE);
            }
        }

        private static String now() {
            return sFormatter.format(new Date());
        }
    }
}

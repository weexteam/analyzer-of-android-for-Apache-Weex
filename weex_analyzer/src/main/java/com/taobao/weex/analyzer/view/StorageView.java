package com.taobao.weex.analyzer.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.StorageHacker;

import java.util.List;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/2<br/>
 * Time: 上午10:06<br/>
 */

public class StorageView extends AbstractAlertView {

    private PerformanceViewAdapter mAdapter;
    private StorageHacker mStorageHacker;

    private RecyclerView mStorageList;

    public StorageView(Context context, StorageHacker storageHacker) {
        super(context);
        this.mStorageHacker = storageHacker;
    }


    @Override
    protected void onShown() {
        if (mStorageHacker == null) {
            return;
        }

        mStorageHacker.fetch(new StorageHacker.OnLoadListener() {
            @Override
            public void onLoad(List<StorageHacker.StorageInfo> list) {
                if(mAdapter == null){
                    mAdapter = new PerformanceViewAdapter(getContext(), list);
                    mStorageList.setAdapter(mAdapter);
                }else{
                    mAdapter.refreshData(list);
                }
            }
        });

    }

    @Override
    protected void onInitView(@NonNull Window window) {
        window.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mStorageList = (RecyclerView) window.findViewById(R.id.list);
        mStorageList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.storage_view;
    }

    //-------


    private class PerformanceViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context mContext;
        private List<StorageHacker.StorageInfo> mStorageData;

        PerformanceViewAdapter(@NonNull Context context, @NonNull List<StorageHacker.StorageInfo> data) {
            this.mContext = context;
            mStorageData = data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_storage, parent, false);
            ViewHolder holder = new ViewHolder(view);
            holder.setOnItemLongClickListener(new ViewHolder.OnItemLongClickListener() {
                @Override
                public void onItemClick(final int position, final String key) {
                    AlertDialog.Builder builder = new CompatibleAlertDialogBuilder(mContext);
                    builder.setTitle("Alert");
                    builder.setMessage("remove key ("+key+") from weex storage ?");
                    builder.setPositiveButton("yes", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //remove
                            if (mStorageHacker != null && key != null) {
                                mStorageHacker.remove(key, new StorageHacker.OnRemoveListener() {
                                    @Override
                                    public void onRemoved(boolean status) {
                                        if (status) {
                                            mStorageData.remove(position);
                                            notifyDataSetChanged();
                                            if (mContext != null) {
                                                Toast.makeText(mContext, "remove success", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            if (mContext != null) {
                                                Toast.makeText(mContext, "remove failed", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    }
                                });
                            }
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("no", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ViewHolder) {
                StorageHacker.StorageInfo info = mStorageData.get(position);
                ((ViewHolder) holder).bind(info, position % 2 != 0);
            }
        }

        @Override
        public int getItemCount() {
            if (mStorageData == null) {
                return 0;
            }
            return mStorageData.size();
        }

        void refreshData(@NonNull List<StorageHacker.StorageInfo> list) {
            mStorageData.clear();
            mStorageData.addAll(list);
            this.notifyDataSetChanged();
        }

    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mKeyText;
        private TextView mValueText;
        private TextView mTimestampText;

        private StorageHacker.StorageInfo mCurStorageInfo;
        private OnItemLongClickListener mListener;

        ViewHolder(View itemView) {
            super(itemView);
            mKeyText = (TextView) itemView.findViewById(R.id.key);
            mValueText = (TextView) itemView.findViewById(R.id.value);
            mTimestampText = (TextView) itemView.findViewById(R.id.timestamp);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null) {
                        int pos = getAdapterPosition();//todo
                        mListener.onItemClick(pos, mCurStorageInfo.key);
                    }
                    return true;
                }
            });
        }

        void bind(@NonNull StorageHacker.StorageInfo info, boolean flag) {
            this.mCurStorageInfo = info;
            itemView.setBackgroundColor(flag ? Color.parseColor("#E0E0E0") : Color.WHITE);
            mValueText.setText(info.value);
            mKeyText.setText(info.key);
            mTimestampText.setText(info.timestamp);
        }

        void setOnItemLongClickListener(OnItemLongClickListener listener) {
            this.mListener = listener;
        }

        interface OnItemLongClickListener {
            void onItemClick(int position, String key);
        }
    }

}


package com.taobao.weex.analyzer.core.weex;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.analyzer.view.overlay.AbstractBizItemView;
import com.taobao.weex.utils.WXViewUtils;

import java.util.List;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/3<br/>
 * Time: 下午5:05<br/>
 */

public class WXPerfItemView extends AbstractBizItemView<Performance> {



    private RecyclerView mPerformanceList;

    public WXPerfItemView(Context context) {
        super(context);
    }

    public WXPerfItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WXPerfItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void prepareView() {


        mPerformanceList = (RecyclerView) findViewById(R.id.overlay_list);
        mPerformanceList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wxt_panel_cur_perf_view;
    }

    @Override
    public void inflateData(Performance data) {

        PerformanceViewAdapter adapter = new PerformanceViewAdapter(getContext(), data);
        mPerformanceList.setAdapter(adapter);
    }


    //---------------
    private static class PerformanceViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context mContext;
        private List<String> mValues;
        private Performance rawPerformance;

        PerformanceViewAdapter(@NonNull Context context, @NonNull Performance performance) {
            this.mContext = context;
            this.rawPerformance = performance;
            mValues = transfer(performance);
        }

        private List<String> transfer(Performance performance) {
            return Performance.transfer(performance);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(createItemView(mContext,parent,viewType),viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ViewHolder) {
                if(holder.getItemViewType() == ViewType.TYPE_ITEM) {
                    ((ViewHolder) holder).bind(mValues.get(position-1));
                } else {
                    ((ViewHolder) holder).bindHeader(rawPerformance);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size()+1;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0) {
                return ViewType.TYPE_HEADER;
            } else {
                return ViewType.TYPE_ITEM;
            }
        }

        View createItemView(Context context,ViewGroup parent, int viewType) {
            View itemView;
            if(viewType == ViewType.TYPE_ITEM) {
                itemView = new TextView(context);
                RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);

                int margin = WXViewUtils.dip2px(5);
                params.topMargin = params.bottomMargin = margin;
                ((TextView)itemView).setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                ((TextView)itemView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                itemView.setLayoutParams(params);
                itemView.setPadding((int) ViewUtils.dp2px(context,15),0,0,0);
                ((TextView)itemView).setTextColor(Color.BLACK);
            } else {
                itemView = LayoutInflater.from(context).inflate(R.layout.wxt_cur_perf_header,parent,false);
            }

            return itemView;
        }
    }

    private static class ViewType {
        static final int TYPE_HEADER = 100;
        static final int TYPE_ITEM = 101;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mValueText;

        private TextView jsfmVersionView;
        private TextView sdkVersionView;
        private TextView renderTimeView;
        private TextView sdkInitTime;
        private TextView networkTime;
        private TextView pageNameView;
        private TextView jsTemplateSizeView;


        ViewHolder(View itemView, int viewType) {
            super(itemView);
            if(viewType == ViewType.TYPE_ITEM) {
                mValueText = (TextView) itemView;
            } else {
                jsfmVersionView = (TextView) itemView.findViewById(R.id.text_jsfm_version);
                sdkVersionView = (TextView) itemView.findViewById(R.id.text_version_sdk);
                renderTimeView = (TextView) itemView.findViewById(R.id.text_screen_render_time);
                sdkInitTime = (TextView) itemView.findViewById(R.id.text_sdk_init_time);
                networkTime = (TextView) itemView.findViewById(R.id.text_network_time);
                pageNameView = (TextView) itemView.findViewById(R.id.page_name);
                jsTemplateSizeView = (TextView) itemView.findViewById(R.id.text_template_size);
            }
        }

        void bind(@NonNull String value) {
            mValueText.setText(value);
        }

        void bindHeader(Performance performance) {
            pageNameView.setText("页面名称: " + performance.pageName + "");
            sdkVersionView.setText("Weex Sdk版本: " + performance.WXSDKVersion + "");
            sdkInitTime.setText("Weex SDK初始化时间 : " + performance.sdkInitTime + "ms");
            jsfmVersionView.setText("JSFramework版本 : " + performance.JSLibVersion+"");
            renderTimeView.setText("首屏时间 : " + performance.screenRenderTime + "ms");
            networkTime.setText("网络时间 : "+ performance.networkTime + "ms");
            jsTemplateSizeView.setText("jsBundle大小 : " + performance.JSTemplateSize + "KB");
        }
    }
}

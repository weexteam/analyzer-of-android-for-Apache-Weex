package com.taobao.weex.analyzer.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.core.Performance;
import com.taobao.weex.utils.WXViewUtils;

import java.util.List;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/3<br/>
 * Time: 下午5:05<br/>
 */

public class WXPerfItemView extends AbstractBizItemView<Performance>{

    private TextView jsfmVersionView;
    private TextView sdkVersionView;
    private TextView renderTimeView;
    private TextView sdkInitTime;
    private TextView networkTime;
    private TextView pageNameView;
    private TextView jsTemplateSizeView;

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
        jsfmVersionView = (TextView) findViewById(R.id.text_jsfm_version);
        sdkVersionView = (TextView) findViewById(R.id.text_version_sdk);
        renderTimeView = (TextView) findViewById(R.id.text_screen_render_time);
        sdkInitTime = (TextView) findViewById(R.id.text_sdk_init_time);
        networkTime = (TextView) findViewById(R.id.text_network_time);
        pageNameView = (TextView) findViewById(R.id.page_name);
        jsTemplateSizeView = (TextView) findViewById(R.id.text_template_size);

        mPerformanceList = (RecyclerView) findViewById(R.id.overlay_list);
        mPerformanceList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wxt_panel_cur_perf_view;
    }

    @Override
    protected void inflateData(Performance data) {
        pageNameView.setText("页面名称: " + data.pageName + "");
        sdkVersionView.setText("Weex Sdk版本: " + data.WXSDKVersion + "");
        sdkInitTime.setText("Weex SDK初始化时间 : " + data.sdkInitTime + "ms");
        jsfmVersionView.setText("JSFramework版本 : " + data.JSLibVersion+"");
        renderTimeView.setText("首屏时间 : " + data.screenRenderTime + "ms");
        networkTime.setText("网络时间 : "+ data.networkTime + "ms");
        jsTemplateSizeView.setText("jsBundle大小 : " + data.JSTemplateSize + "KB");
        PerformanceViewAdapter adapter = new PerformanceViewAdapter(getContext(), data);
        mPerformanceList.setAdapter(adapter);
    }


    //---------------
    private static class PerformanceViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context mContext;
        private List<String> mValues;

        PerformanceViewAdapter(@NonNull Context context, @NonNull Performance performance) {
            this.mContext = context;
            mValues = transfer(performance);
        }

        private List<String> transfer(Performance performance) {
            return Performance.transfer(performance);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(createItemView(mContext));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ViewHolder) {
                ((ViewHolder) holder).bind(mValues.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        View createItemView(Context context) {
            TextView itemView = new TextView(context);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);

            int margin = WXViewUtils.dip2px(5);
            params.topMargin = params.bottomMargin = margin;

            itemView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            itemView.setLayoutParams(params);
            itemView.setTextColor(Color.BLACK);
            return itemView;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mValueText;

        ViewHolder(View itemView) {
            super(itemView);
            mValueText = (TextView) itemView;
        }

        void bind(@NonNull String value) {
            mValueText.setText(value);
        }
    }
}

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
import android.widget.Toast;

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

public class WXPerfItemView extends AbstractBizItemView<Performance> implements View.OnLongClickListener{

    private TextView totalTimeView;
    private TextView sdkVersionView;
    private TextView renderTimeView;
    private TextView sdkInitTime;
    private TextView networkTime;

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
        totalTimeView = (TextView) findViewById(R.id.text_total_time);
        sdkVersionView = (TextView) findViewById(R.id.text_version_sdk);
        renderTimeView = (TextView) findViewById(R.id.text_screen_render_time);
        sdkInitTime = (TextView) findViewById(R.id.text_sdk_init_time);
        networkTime = (TextView) findViewById(R.id.text_network_time);

        totalTimeView.setOnLongClickListener(this);
        renderTimeView.setOnLongClickListener(this);
        sdkVersionView.setOnLongClickListener(this);
        sdkInitTime.setOnLongClickListener(this);
        networkTime.setOnLongClickListener(this);

        mPerformanceList = (RecyclerView) findViewById(R.id.overlay_list);
        mPerformanceList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wxt_panel_cur_perf_view;
    }

    @Override
    protected void inflateData(Performance data) {
        totalTimeView.setText("totalTime : " + data.totalTime + "ms");
        sdkVersionView.setText("weex sdk version : " + data.WXSDKVersion + "");
        renderTimeView.setText("firstScreenRenderTime : " + data.screenRenderTime + "ms");
        sdkInitTime.setText("sdk init time : " + data.sdkInitTime + "ms(only once)");
        networkTime.setText("networkTime : "+ data.networkTime + "ms");
        PerformanceViewAdapter adapter = new PerformanceViewAdapter(getContext(), data);
        mPerformanceList.setAdapter(adapter);
    }

    @Override
    public boolean onLongClick(View v) {
        Context context = v.getContext();
        if(context == null){
            return false;
        }
        int id = v.getId();

        if(id == R.id.text_total_time){
            Toast.makeText(context,context.getString(R.string.wxt_explain_total_time),Toast.LENGTH_LONG).show();
        }else if(id == R.id.text_screen_render_time){
            Toast.makeText(context,context.getString(R.string.wxt_explain_scr_time),Toast.LENGTH_LONG).show();
        }else if(id == R.id.text_version_sdk){
            Toast.makeText(context,context.getString(R.string.wxt_explain_wx_sdk_ver),Toast.LENGTH_LONG).show();
        }else if(id == R.id.text_sdk_init_time){
            Toast.makeText(context,context.getString(R.string.wxt_explain_sdk_init),Toast.LENGTH_LONG).show();
        }else if(id == R.id.text_network_time){
            Toast.makeText(context,context.getString(R.string.wxt_explain_network_time),Toast.LENGTH_LONG).show();
        }

        return true;
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

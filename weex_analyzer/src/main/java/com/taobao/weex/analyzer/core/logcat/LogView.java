package com.taobao.weex.analyzer.core.logcat;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.taobao.weex.analyzer.Config;
import com.taobao.weex.analyzer.R;
import com.taobao.weex.analyzer.utils.SDKUtils;
import com.taobao.weex.analyzer.utils.ViewUtils;
import com.taobao.weex.analyzer.view.overlay.AbstractResizableOverlayView;
import com.taobao.weex.analyzer.view.alert.CompatibleAlertDialogBuilder;
import com.taobao.weex.analyzer.view.overlay.IOverlayView;
import com.taobao.weex.analyzer.view.overlay.SimpleOverlayView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LogView extends AbstractResizableOverlayView {

    private LogcatDumper mLogcatDumper;

    private OnCloseListener mOnCloseListener;
    private OnLogConfigChangedListener mConfigChangeListener;
    private onStatusChangedListener mStatusChangedListener;

    private SimpleOverlayView mCollapsedView;

    private static final Map<String, LogcatDumper.Rule> sDefaultRules;

    private static final String FILTER_JS_LOG = "jsLog";
    private static final String FILTER_CALL_NATIVE = "callNative";
    private static final String FILTER_CALL_JS = "callJS";
    private static final String FILTER_ALL = "all";
    private static final String FILTER_EXCEPTION = "reportJSException";
    private static final String FILTER_CUSTOM = "custom";

    private int mLogLevel;
    private String mFilterName;

    private LogListAdapter mLogAdapter;

    private boolean isSettingOpened;
    private boolean isSizeMenuOpened;

    private String mCurKeyword;

    private String mCustomRuleName;


    static {
        sDefaultRules = new HashMap<>();
        sDefaultRules.put(FILTER_JS_LOG, new LogcatDumper.Rule(FILTER_JS_LOG, "jsLog"));
        sDefaultRules.put(FILTER_CALL_NATIVE, new LogcatDumper.Rule(FILTER_CALL_NATIVE, "callNative"));
        sDefaultRules.put(FILTER_CALL_JS, new LogcatDumper.Rule(FILTER_CALL_JS, "callJS"));
        sDefaultRules.put(FILTER_ALL, new LogcatDumper.Rule(FILTER_ALL, null));
        sDefaultRules.put(FILTER_EXCEPTION, new LogcatDumper.Rule(FILTER_EXCEPTION, "reportJSException"));
    }


    public interface OnLogConfigChangedListener {
        void onLogLevelChanged(int level);

        void onLogFilterChanged(String filterName);
    }

    public interface onStatusChangedListener {
        void onCollapsed();

        void onExpanded();
    }

    public LogView(Context application, Config config) {
        super(application,config);
        mWidth = WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    public boolean isPermissionGranted(@NonNull Config config) {
        return !config.getIgnoreOptions().contains(Config.TYPE_LOG);
    }

    public void setOnCloseListener(@Nullable OnCloseListener listener) {
        this.mOnCloseListener = listener;
    }

    public void setOnLogConfigChangedListener(@Nullable OnLogConfigChangedListener listener) {
        this.mConfigChangeListener = listener;
    }

    public void setOnStatusChangedListener(@Nullable onStatusChangedListener listener) {
        this.mStatusChangedListener = listener;
    }

    public void setLogLevel(int level) {
        this.mLogLevel = level;
    }

    public void setFilterName(String filterName) {
        this.mFilterName = filterName;
    }

    @NonNull
    @Override
    protected View onCreateView() {
        View wholeView = View.inflate(mContext, R.layout.wxt_log_view, null);

        final View hold = wholeView.findViewById(R.id.hold);
        View clear = wholeView.findViewById(R.id.clear);
        View close = wholeView.findViewById(R.id.close);
        View inputKeyword = wholeView.findViewById(R.id.btn_input_keyword);
        final View clearKeyword = wholeView.findViewById(R.id.btn_clear_keyword);
        final TextView curKeyword = (TextView) wholeView.findViewById(R.id.text_cur_keyword);

        RadioGroup levelGroup = (RadioGroup) wholeView.findViewById(R.id.level_group);
        RadioGroup ruleGroup = (RadioGroup) wholeView.findViewById(R.id.rule_group);
        RadioGroup sizeGroup = (RadioGroup) wholeView.findViewById(R.id.height_group);

        final TextView settings = (TextView) wholeView.findViewById(R.id.settings);
        View collapse = wholeView.findViewById(R.id.collapse);
        final ViewGroup settingContent = (ViewGroup) wholeView.findViewById(R.id.setting_content);
        final RecyclerView logList = (RecyclerView) wholeView.findViewById(R.id.list);

        final TextView sizeBtn = (TextView) wholeView.findViewById(R.id.size);
        final ViewGroup sizeContent = (ViewGroup) wholeView.findViewById(R.id.size_content);

        //config
        View logLevelPanel = wholeView.findViewById(R.id.log_level_panel);
        View logFilterPanel = wholeView.findViewById(R.id.log_filter_panel);
        View searchPanel = wholeView.findViewById(R.id.search_panel);
        View customFilterPanel = wholeView.findViewById(R.id.custom_filter_panel);

        RadioGroup customFilterGroup = (RadioGroup) wholeView.findViewById(R.id.custom_filter_group);

        if(mConfig != null && mConfig.getLogConfig() != null) {
            LogConfig logConfig = mConfig.getLogConfig();

            if(logConfig.isShowLogLevelPanel()) {
                logLevelPanel.setVisibility(View.VISIBLE);
            } else {
                logLevelPanel.setVisibility(View.GONE);
            }

            if(logConfig.isShowLogFilterPanel()) {
                logFilterPanel.setVisibility(View.VISIBLE);
            } else {
                logFilterPanel.setVisibility(View.GONE);
            }

            if(logConfig.isShowSearchPanel()) {
                searchPanel.setVisibility(View.VISIBLE);
            } else {
                searchPanel.setVisibility(View.GONE);
            }

            if(logConfig.getViewSize() != -1) {
                this.mViewSize = logConfig.getViewSize();
            }

            //custom rule
            List<String> customRule = new ArrayList<>();
            if(logConfig.getCustomRule() != null) {
                customRule.addAll(logConfig.getCustomRule());
            }
            customRule.add(0,FILTER_ALL);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            if(!customRule.isEmpty() && customRule.size() > 1) {
                customFilterPanel.setVisibility(View.VISIBLE);
                final int[] ids = new int[customRule.size()];
                final LogcatDumper.Rule[] rules = new LogcatDumper.Rule[customRule.size()];
                RadioButton[] btns = new RadioButton[customRule.size()];

                for(int i = 0; i < customRule.size(); i++) {
                    String ruleStr = customRule.get(i);
                    if(TextUtils.isEmpty(ruleStr)) {
                        continue;
                    }
                    if(FILTER_ALL.equals(ruleStr)) {
                        rules[i] = sDefaultRules.get(FILTER_ALL);
                    } else {
                        rules[i] = new LogcatDumper.Rule(ruleStr,ruleStr);
                    }
                    RadioButton btn = (RadioButton) inflater.inflate(R.layout.wxt_styleable_radio_btn,customFilterGroup,false);
                    btns[i] = btn;
                    ids[i] = ViewUtils.generateViewId();
                    btn.setId(ids[i]);
                    btn.setText(ruleStr);
                    customFilterGroup.addView(btn);
                }
                btns[0].setChecked(true);

                customFilterGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                        if (mLogcatDumper == null) {
                            return;
                        }

                        if (mLogAdapter != null) {
                            mLogAdapter.clear();
                        }
                        mLogcatDumper.removeAllRule();

                        for(int i = 0; i < ids.length; i++) {
                            int id = ids[i];
                            if(id == checkedId) {
                                String ruleName = rules[i].getName();
                                if(ruleName != null && !ruleName.equals(mCustomRuleName)) {
                                    mCustomRuleName = ruleName;
                                    mLogcatDumper.addRule(rules[i]);
                                    mLogcatDumper.findCachedLogByNewFilters();
                                }
                                break;
                            }
                        }
                    }
                });

            } else {
                customFilterPanel.setVisibility(View.GONE);
            }
        } else {
            customFilterPanel.setVisibility(View.GONE);
        }


        sizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSizeMenuOpened = !isSizeMenuOpened;
                if(isSizeMenuOpened){
                    sizeContent.setVisibility(View.VISIBLE);
                }else{
                    sizeContent.setVisibility(View.GONE);
                }
            }
        });

        //setting init
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSettingOpened = !isSettingOpened;
                if (isSettingOpened) {
                    settingContent.setVisibility(View.VISIBLE);
                } else {
                    settingContent.setVisibility(View.GONE);
                }
            }
        });

        collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCollapse();
            }
        });

        //view size

        setViewSize(mViewSize, logList, false);
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
                setViewSize(mViewSize, logList, true);
            }
        });

        inputKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final EditText editText = new EditText(v.getContext());
                editText.setTextColor(Color.BLACK);
                mWholeView.setVisibility(View.GONE);
                new CompatibleAlertDialogBuilder(v.getContext())
                        .setTitle("input keyword")
                        .setView(editText)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mWholeView.setVisibility(View.VISIBLE);
                                String text = editText.getText().toString();
                                if(TextUtils.isEmpty(text)){
                                   return;
                                }
                                mCurKeyword = text;
                                performSearch(mCurKeyword);
                                curKeyword.setText(String.format(Locale.CHINA,v.getContext().getString(R.string.wxt_current_keyword_format),mCurKeyword));

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mWholeView.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();

            }
        });

        clearKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurKeyword = null;
                curKeyword.setText(String.format(Locale.CHINA,v.getContext().getString(R.string.wxt_current_keyword_format),""));
                if(mLogcatDumper != null){
                    if(mLogcatDumper.removeRule(FILTER_CUSTOM)){
                        mLogcatDumper.findCachedLogByNewFilters();
                    }
                }
            }
        });


        //init recyclerView
        logList.setLayoutManager(new LinearLayoutManager(mContext));
        mLogAdapter = new LogListAdapter(mContext, logList);
        logList.setAdapter(mLogAdapter);


        switch (mLogLevel) {
            case Log.VERBOSE:
                ((RadioButton) wholeView.findViewById(R.id.level_v)).setChecked(true);
                break;
            case Log.INFO:
                ((RadioButton) wholeView.findViewById(R.id.level_i)).setChecked(true);
                break;
            case Log.DEBUG:
                ((RadioButton) wholeView.findViewById(R.id.level_d)).setChecked(true);
                break;
            case Log.WARN:
                ((RadioButton) wholeView.findViewById(R.id.level_w)).setChecked(true);
                break;
            case Log.ERROR:
                ((RadioButton) wholeView.findViewById(R.id.level_e)).setChecked(true);
                break;
        }

        if (mFilterName == null) {
            mFilterName = FILTER_ALL;
        }

        switch (mFilterName) {
            case FILTER_ALL:
                ((RadioButton) wholeView.findViewById(R.id.rule_all)).setChecked(true);
                break;
            case FILTER_CALL_JS:
                ((RadioButton) wholeView.findViewById(R.id.rule_calljs)).setChecked(true);
                break;
            case FILTER_CALL_NATIVE:
                ((RadioButton) wholeView.findViewById(R.id.rule_callnative)).setChecked(true);
                break;
            case FILTER_JS_LOG:
                ((RadioButton) wholeView.findViewById(R.id.rule_jslog)).setChecked(true);
                break;
            case FILTER_EXCEPTION:
                ((RadioButton) wholeView.findViewById(R.id.rule_exception)).setChecked(true);
                break;
        }


        levelGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mLogcatDumper == null) {
                    return;
                }

                if (mLogAdapter != null) {
                    mLogAdapter.clear();
                }

                int level = mLogLevel;
                if (checkedId == R.id.level_i) {
                    level = Log.INFO;
                } else if (checkedId == R.id.level_v) {
                    level = Log.VERBOSE;
                } else if (checkedId == R.id.level_d) {
                    level = Log.DEBUG;
                } else if (checkedId == R.id.level_e) {
                    level = Log.ERROR;
                } else if (checkedId == R.id.level_w) {
                    level = Log.WARN;
                }

                if (level != mLogLevel) {
                    mLogLevel = level;
                    mLogcatDumper.setLevel(mLogLevel);
                    if (mConfigChangeListener != null) {
                        mConfigChangeListener.onLogLevelChanged(mLogLevel);
                    }
                }


                // history cached log will be filtered by new rules
                mLogcatDumper.findCachedLogByNewFilters();
            }
        });

        ruleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mLogcatDumper == null) {
                    return;
                }

                if (mLogAdapter != null) {
                    mLogAdapter.clear();
                }

                mLogcatDumper.removeAllRule();
                mCurKeyword = null;
                if(curKeyword != null){
                    curKeyword.setText(String.format(Locale.CHINA,mContext.getString(R.string.wxt_current_keyword_format),""));
                }
                String filterName = mFilterName;
                if (checkedId == R.id.rule_all) {
                    filterName = FILTER_ALL;
                } else if (checkedId == R.id.rule_jslog) {
                    filterName = FILTER_JS_LOG;
                    mLogcatDumper.addRule(sDefaultRules.get(FILTER_JS_LOG));
                } else if (checkedId == R.id.rule_calljs) {
                    filterName = FILTER_CALL_JS;
                    mLogcatDumper.addRule(sDefaultRules.get(FILTER_CALL_JS));
                } else if (checkedId == R.id.rule_callnative) {
                    filterName = FILTER_CALL_NATIVE;
                    mLogcatDumper.addRule(sDefaultRules.get(FILTER_CALL_NATIVE));
                } else if (checkedId == R.id.rule_exception) {
                    filterName = FILTER_EXCEPTION;
                    mLogcatDumper.addRule(sDefaultRules.get(FILTER_EXCEPTION));
                }

                if (!filterName.equals(mFilterName)) {
                    mFilterName = filterName;
                    if (mConfigChangeListener != null) {
                        mConfigChangeListener.onLogFilterChanged(mFilterName);
                    }
                }

                mLogcatDumper.findCachedLogByNewFilters();
            }
        });


        hold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLogAdapter == null) {
                    return;
                }

                if (isViewAttached) {
                    if (mLogAdapter.isHoldModeEnabled()) {
                        mLogAdapter.setHoldModeEnabled(false);
                        ((TextView) hold).setText("hold(off)");
                    } else {
                        mLogAdapter.setHoldModeEnabled(true);
                        ((TextView) hold).setText("hold(on)");
                    }
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isViewAttached && mLogAdapter != null) {
                    mLogAdapter.clear();
                    //maybe we need clear cache here
                    if (mLogcatDumper != null) {
                        mLogcatDumper.clearCachedLog();
                    }
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isViewAttached && mOnCloseListener != null) {
                    mOnCloseListener.close(LogView.this);
                    dismiss();
                }
            }
        });

        return wholeView;
    }

    @Override
    protected void onShown() {
        mLogcatDumper = new LogcatDumpBuilder()
                .listener(new LogcatDumper.OnLogReceivedListener() {
                    @Override
                    public void onReceived(@NonNull List<LogcatDumper.LogInfo> logList) {
                        if (mLogAdapter != null) {
                            mLogAdapter.addLog(logList);
                        }
                    }
                })
                .level(mLogLevel)
                .enableCache(true)
                .cacheLimit(1000)
                .build();

        LogcatDumper.Rule rule = null;
        if (mFilterName != null) {
            rule = sDefaultRules.get(mFilterName);
        }

        if (rule != null) {
            mLogcatDumper.addRule(rule);
        }

        mLogcatDumper.beginDump();
    }

    @Override
    protected void onDismiss() {
        if (mLogcatDumper != null) {
            mLogcatDumper.destroy();
            mLogcatDumper = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (mCollapsedView != null) {
            mCollapsedView.dismiss();
//            mCollapsedView = null;
        }
    }

    private void performSearch(@Nullable String keyword) {
        if(TextUtils.isEmpty(keyword) || mLogcatDumper == null || mLogAdapter == null){
            return;
        }
        mLogAdapter.clear();
        mLogcatDumper.removeRule(FILTER_CUSTOM);
        mLogcatDumper.addRule(new LogcatDumper.Rule(FILTER_CUSTOM,keyword));
        mLogcatDumper.findCachedLogByNewFilters();
    }

    private void performCollapse() {
        //callback
        if (mStatusChangedListener != null) {
            mStatusChangedListener.onCollapsed();
        }

        //dismiss current view
        dismiss();

        //show collapse view
        if (mCollapsedView == null) {
            mCollapsedView = new SimpleOverlayView.Builder(mContext,"Log")
                    .listener(new SimpleOverlayView.OnClickListener() {
                        @Override
                        public void onClick(@NonNull IOverlayView view) {
                            mCollapsedView.dismiss();
                            if (mStatusChangedListener != null) {
                                mStatusChangedListener.onExpanded();
                            }
                            LogView.this.show();
                        }
                    })
                    .build();
        }
        mCollapsedView.show();
    }

    private static class LogListAdapter extends RecyclerView.Adapter {

        private List<LogcatDumper.LogInfo> mLogData;
        private Context mContext;

        private boolean isHoldMode = false;

        private RecyclerView mList;

        LogListAdapter(@NonNull Context context, @NonNull RecyclerView list) {
            this.mContext = context;
            mLogData = new ArrayList<>();
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.wxt_item_log, parent, false);
            return new LogViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof LogViewHolder) {
                ((LogViewHolder) holder).bind(mLogData.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mLogData.size();
        }

        void addLog(@NonNull LogcatDumper.LogInfo log) {
            mLogData.add(log);
            notifyItemInserted(mLogData.size());
        }

        void addLog(@NonNull List<LogcatDumper.LogInfo> list) {
            if (list.size() == 1) {
                addLog(list.get(0));
            } else {
                int size = mLogData.size();
                mLogData.addAll(list);
                notifyItemRangeInserted(size, list.size());
            }

            if (!isHoldMode) {
                try {
                    mList.smoothScrollToPosition(this.getItemCount() - 1);
                } catch (Exception e) {
                    //ignored
                }
            }
        }

        void clear() {
            mLogData.clear();
            notifyDataSetChanged();
        }

        void setHoldModeEnabled(boolean enabled) {
            this.isHoldMode = enabled;
        }

        boolean isHoldModeEnabled() {
            return this.isHoldMode;
        }
    }


    private static class LogViewHolder extends RecyclerView.ViewHolder {

        private TextView mText;
        private LogcatDumper.LogInfo mCurLog;

        LogViewHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.text_log);

            mText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mCurLog != null) {
                        try {
                            SDKUtils.copyToClipboard(v.getContext(), mCurLog.message, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });
        }

        void bind(LogcatDumper.LogInfo log) {
            mCurLog = log;
            switch (log.level) {
                case Log.VERBOSE:
                    mText.setTextColor(Color.parseColor("#FFFFFF"));
                    break;
                case Log.INFO:
                    mText.setTextColor(Color.parseColor("#2196F3"));

                    break;
                case Log.DEBUG:
                    mText.setTextColor(Color.parseColor("#4CAF50"));

                    break;
                case Log.WARN:
                    mText.setTextColor(Color.parseColor("#FFEB3B"));
                    break;
                case Log.ERROR:
                    mText.setTextColor(Color.parseColor("#F44336"));
                    break;
                default:
                    mText.setTextColor(Color.parseColor("#FFFFFF"));

            }
            mText.setText(log.message);
        }
    }

}

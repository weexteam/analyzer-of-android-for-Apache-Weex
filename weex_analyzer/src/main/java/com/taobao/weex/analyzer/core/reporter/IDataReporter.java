package com.taobao.weex.analyzer.core.reporter;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.taobao.weex.analyzer.Config.TYPE_CPU;
import static com.taobao.weex.analyzer.Config.TYPE_FPS;
import static com.taobao.weex.analyzer.Config.TYPE_MEMORY;
import static com.taobao.weex.analyzer.Config.TYPE_MTOP_INSPECTOR;
import static com.taobao.weex.analyzer.Config.TYPE_RENDER_ANALYSIS;
import static com.taobao.weex.analyzer.Config.TYPE_TRAFFIC;
import static com.taobao.weex.analyzer.Config.TYPE_VIEW_INSPECTOR;
import static com.taobao.weex.analyzer.Config.TYPE_WEEX_PERFORMANCE_STATISTICS;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public interface IDataReporter<T> {

    String API_VERSION = "1";

    void report(@NonNull ProcessedData<T> data);

    boolean isEnabled();

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TYPE_CPU,TYPE_FPS,TYPE_MEMORY,TYPE_TRAFFIC,
            TYPE_WEEX_PERFORMANCE_STATISTICS,TYPE_VIEW_INSPECTOR,
            TYPE_RENDER_ANALYSIS,TYPE_MTOP_INSPECTOR})
    @interface OptionType {
    }

    class ProcessedData<T> {
        private int sequenceId;
        private String deviceId;
        private String version;
        private long timestamp;
        @OptionType
        private String type;
        private T data;

        ///TODO 当前页面

        public int getSequenceId() {
            return sequenceId;
        }

        public void setSequenceId(int sequenceId) {
            this.sequenceId = sequenceId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    final class ProcessedDataBuilder<T> {
        private int mSequenceId;
        private String mDeviceId;
        private long mTimestamp;
        private String mType;
        private T mData;
        private String mVersion;

        public ProcessedDataBuilder() {
            this.mTimestamp = System.currentTimeMillis();
            this.mVersion = API_VERSION;
        }

        public ProcessedDataBuilder<T> sequenceId(int sequenceId) {
            this.mSequenceId = sequenceId;
            return this;
        }

        public ProcessedDataBuilder<T> deviceId(String deviceId) {
            this.mDeviceId = deviceId;
            return this;
        }

        public ProcessedDataBuilder<T> type(@OptionType String type) {
            this.mType = type;
            return this;
        }

        public ProcessedDataBuilder<T> data(T data) {
            this.mData = data;
            return this;
        }

        public ProcessedData<T> build() {
            ProcessedData<T> processedData = new ProcessedData<>();
            processedData.sequenceId = mSequenceId;
            processedData.deviceId = mDeviceId;
            processedData.timestamp = mTimestamp;
            processedData.data = mData;
            processedData.type = mType;
            processedData.version = mVersion;
            return processedData;
        }
    }

}

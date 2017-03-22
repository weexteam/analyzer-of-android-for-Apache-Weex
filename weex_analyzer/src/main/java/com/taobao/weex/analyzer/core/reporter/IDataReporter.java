package com.taobao.weex.analyzer.core.reporter;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

public interface IDataReporter<T> {

    String TYPE_WEEX_PERFORMANCE_STATISTICS = "weex_performance_statistics";
    String TYPE_VIEW_INSPECTOR = "view_inspector";
    String TYPE_RENDER_ANALYSIS = "render_analysis";
    String TYPE_MTOP_INSPECTOR = "mtop_inspector";
    String TYPE_MEMORY = "memory";
    String TYPE_CPU = "cpu";
    String TYPE_FPS = "fps";
    String TYPE_TRAFFIC = "traffic";

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

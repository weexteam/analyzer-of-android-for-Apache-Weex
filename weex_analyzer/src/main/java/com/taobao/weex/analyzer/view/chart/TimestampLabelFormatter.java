package com.taobao.weex.analyzer.view.chart;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/17<br/>
 * Time: 下午1:54<br/>
 */

public class TimestampLabelFormatter extends DefaultLabelFormatter {
    public TimestampLabelFormatter() {
    }

    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            return formatTime(value);
        } else {
            return super.formatLabel(value, false);
        }
    }

    private String formatTime(double value) {
        if(value < 60){
            return ((int)value)+"s";
        }

        int seconds = (int) (value % 60);
        int minutes = (int) ((value / 60) % 60);
        int hours = (int) (value / (60 * 60));

        StringBuilder builder = new StringBuilder();

        if(hours > 0){
            builder.append(hours).append("h");
        }
        if(minutes > 0){
            builder.append(minutes).append("m");
        }
        if(seconds > 0){
            builder.append(seconds).append("s");
        }
        return builder.toString();
    }

}

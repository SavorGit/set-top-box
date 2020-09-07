package com.savor.ads.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    /**
     * 时间戳转成字符串
     * @param seconds
     * @return
     */
    public static String formatSeconds(long seconds){
        String standardTime;
        if (seconds <= 0){
            standardTime = "00:00";
        } else if (seconds < 60) {
            standardTime = String.format(Locale.getDefault(), "00:%02d", seconds % 60);
        } else if (seconds < 3600) {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        } else {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
        }
        return standardTime;
    }

    public static String formatSecondsToMin(long seconds){
        String standardTime="00:00";
        if (seconds==0){
            return standardTime;
        }
        standardTime=String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        return standardTime;
    }
}

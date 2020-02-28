package com.savor.ads.utils;

import android.content.Context;

import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.log.LogReportUtil;

/**
 * 技术日志上报
 * Created by zhanghq on 2016/12/17.
 */
public class TechnicalLogReporter {
    private static final String TAG = "TechnicalLogReporter";

    /**
     * SD卡拔出日志
     */
    public static void sdcardRemoved(Context context) {
        LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()), Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                String.valueOf(System.currentTimeMillis()), "take_out", "sdcard", "",
                "", Session.get(context).getVersionName(), Session.get(context).getAdsPeriod(),
                Session.get(context).getBirthdayOndemandPeriod(), "");
    }

    /**
     * SD卡拔出日志
     */
    public static void sdcardMounted(Context context) {
        LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()), Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                String.valueOf(System.currentTimeMillis()), "take_in", "sdcard", "",
                "", Session.get(context).getVersionName(), Session.get(context).getAdsPeriod(),
                Session.get(context).getBirthdayOndemandPeriod(), "");
    }

    /**
     * Md5校验失败日志
     *
     * @param vid 错误的视频ID
     */
    public static void md5Failed(Context context, String vid) {
        LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()), Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                String.valueOf(System.currentTimeMillis()), "check_failed", "md5", vid,
                "", Session.get(context).getVersionName(), Session.get(context).getAdsPeriod(),
                Session.get(context).getBirthdayOndemandPeriod(), "");
    }

    /**
     * 广告更新日志
     *
     * @param newVersion 升级后的广告期数
     */
    public static void adUpdate(Context context, String newVersion) {
        LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()), Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                String.valueOf(System.currentTimeMillis()), "update", "ads", "",
                "", Session.get(context).getVersionName(), newVersion,
                Session.get(context).getBirthdayOndemandPeriod(), "");
    }

    /**
     * 点播更新日志
     *
     * @param newVersion 升级后的点播视频期数
     */
    public static void vodUpdate(Context context, String newVersion) {
        LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()), Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                String.valueOf(System.currentTimeMillis()), "update", "vod", "",
                "", Session.get(context).getVersionName(), Session.get(context).getAdsPeriod(),
                Session.get(context).getBirthdayOndemandPeriod(), "");
    }

    /**
     * APK更新日志
     *
     * @param newVersion 升级后的apk版本号
     */
    public static void apkUpdate(Context context, String newVersion) {
        LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()), Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                String.valueOf(System.currentTimeMillis()), "update", "apk", "",
                "", Session.get(context).getVersionName(), Session.get(context).getAdsPeriod(),
                Session.get(context).getBirthdayOndemandPeriod(), "");
    }

    /**
     * rom更新日志
     *
     * @param newVersion 升级后的rom版本号
     */
    public static void romUpdate(Context context, String newVersion) {
        LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()), Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                String.valueOf(System.currentTimeMillis()), "update", "rom", "",
                "", Session.get(context).getVersionName(), Session.get(context).getAdsPeriod(),
                Session.get(context).getBirthdayOndemandPeriod(), "");
    }
}

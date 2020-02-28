package com.savor.ads.utils;

public class ZmengAdsResponseCode {
    /**
     * 有广告返回
     */
    public static final int SUCCESS = 0;

    /**
     * requestId格式不对
     */
    public static final int MISSING_REQUESTID = 101;

    /**
     * 没有对应尺寸广告位
     */
    public static final int MISSING_SCREEN_SIZE = 102;
    /**
     * 没有备案或已删除
     */
    public static final int ERROR_UDID = 103;
    /**
     * 没有在投的广告计划
     */
    public static final int NO_PUT_AD = 104;

    /**
     * 设备已下线
     */
    public static final int DEVICE_OFF = 105;
    /**
     * 没有匹配的广告
     */
    public static final int NO_MATCHING_AD = 202;

    /**
     * 无广告
     */
    public static final int NO_AD = 204;
    /**
     * 设备类型信息缺失
     */
    public static final int OS_ERROR = 500;
}

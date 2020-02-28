package com.savor.ads.utils;

public class BaiduAdsResponseCode {
    /**
     * 成功
     */
    public static final int SUCCESS = 0;

    /**
     * 请求参数缺失
     */
    public static final int MISSING_REQUEST = 100001;

    /**
     * 使用API 版本信息缺失
     */
    public static final int MISSING_API_VERSION = 102000;
    /**
     * API主版本信息缺失
     */
    public static final int MISSING_API_VERSION_MAJOR = 102010;
    /**
     * API版本信息错误
     */
    public static final int ERROR_API_VERSION = 102011;

    /**
     * APP_ID信息缺失
     */
    public static final int MISSING_APP_ID = 103010;
    /**
     * APP_ID信息错误，MSSP未收录
     */
    public static final int ERROR_APP_ID = 103011;

    /**
     * 设备信息缺失
     */
    public static final int MISSING_DEVICE_INFO = 104000;
    /**
     * 设备类型信息缺失
     */
    public static final int MISSING_DEVICE_TYPE = 104010;
    /**
     * 设备类型信息错误
     */
    public static final int ERROR_DEVICE_TYPE = 104011;
    /**
     * 操作系统信息缺失
     */
    public static final int MISSING_OS_TYPE = 104020;
    /**
     * 操作系统信息错误
     */
    public static final int ERROR_OS_TYPE = 104021;
    /**
     * 操作系统版本信息缺失
     */
    public static final int MISSING_OS_VERSION = 104030;
    /**
     * 操作系统主版本信息缺失
     */
    public static final int MISSING_OS_VERSION_MAJOR = 104040;
    /**
     * 厂商信息缺失
     */
    public static final int MISSING_VENDOR = 104050;
    /**
     * 设备型号信息缺失
     */
    public static final int MISSING_MODEL = 104060;
    /**
     * 设备唯一标识符缺失
     */
    public static final int MISSING_UDID = 104070;
    /**
     * 设备唯一标识符未备案
     */
    public static final int ERROR_UDID = 104071;
    /**
     * 设备屏幕尺寸信息缺失
     */
    public static final int MISSING_SCREEN_SIZE = 104090;
    /**
     * 设备屏幕尺寸宽度缺失
     */
    public static final int MISSING_SCREEN_SIZE_WIDTH = 104100;
    /**
     * 设备屏幕尺寸高度缺失
     */
    public static final int MISSING_SCREEN_SIZE_HEIGHT = 104110;
    /**
     * 设备唯一标识符类型缺失
     */
    public static final int MISSING_UDID_ID_TYPE = 104120;
    /**
     * 设备唯一标识符类型错误
     */
    public static final int ERROR_UDID_ID_TYPE = 104121;
    /**
     * 设备唯一标识符ID值缺失
     */
    public static final int MISSING_UDID_ID = 104130;
    /**
     * 设备mac不符合约定格式
     */
    public static final int ERROR_FORMAT_MAC = 104140;

    /**
     * 网络环境信息缺失
     */
    public static final int MISSING_NETWORK_INFO = 105000;
    /**
     * 网络地址信息缺失
     */
    public static final int MISSING_IPV4 = 105010;
    /**
     * 网络地址信息格式错误
     */
    public static final int ERROR_FORMAT_IPV4 = 105011;
    /**
     * 网络连接类型缺失
     */
    public static final int MISSING_CONNECTION_TYPE = 105020;
    /**
     * 网络连接类型错误
     */
    public static final int ERROR_CONNECTION_TYPE = 105021;
    /**
     * 网络运营商类型缺失
     */
    public static final int MISSING_OPERATOR_TYPE = 105030;
    /**
     * 网络运营商类型错误
     */
    public static final int ERROR_OPERATOR_TYPE = 105031;
    /**
     * Wi-Fi热点地址信息缺失
     */
    public static final int MISSING_AP_MAC = 105040;
    /**
     * Wi-Fi热点地址信息格式错误
     */
    public static final int ERROR_FORMAT_AP_MAC = 105041;
    /**
     * Wi-Fi热点信号强度信息缺失
     */
    public static final int MISSING_RSSI = 105050;
    /**
     * Wi-Fi热点名称缺失
     */
    public static final int MISSING_AP_NAME = 105060;
    /**
     * Wi-Fi连接状态信息缺失
     */
    public static final int MISSING_AP_CONNECTION = 105070;

    /**
     * 坐标类型信息缺失
     */
    public static final int MISSING_COORDINATE_TYPE = 106000;
    /**
     * 坐标类型信息错误
     */
    public static final int ERROR_COORDINATE_TYPE = 106001;
    /**
     * 经度信息缺失
     */
    public static final int MISSING_LONGITUDE = 106010;
    /**
     * 纬度信息缺失
     */
    public static final int MISSING_LATITUDE = 106020;
    /**
     * 定位时间戳信息缺失
     */
    public static final int MISSING_GPS_TIMESTAMP = 106030;

    /**
     * 广告位ID缺失
     */
    public static final int MISSING_ADSLOT_ID = 107000;
    /**
     * 广告位ID未收录
     */
    public static final int ERROR_ADSLOT_ID = 107001;
    /**
     * 广告位ID与APP_ID不匹配
     */
    public static final int NOT_MATCH_ADSLOT_ID = 107003;
    /**
     * 广告位尺寸信息缺失
     */
    public static final int MISSING_ADSLOT_SIZE = 107010;
    /**
     * 广告位信息缺失
     */
    public static final int MISSING_ADSLOT = 107040;

    /**
     * 时段流量丢弃(仅户外、出行)
     */
    public static final int FLOW_DROP_BY_TIME_CONTORL = 400000;

    /**
     * 请求处理正确，无广告返回
     */
    public static final int NO_AD = 200000;
    /**
     * 请求处理正确，无广告返回(无预算导致)
     */
    public static final int AD_NO_DATA = 201000;
    /**
     * 广告无签名
     */
    public static final int AD_NO_SIGN = 201010;
    /**
     * 广告创意类型信息丢失
     */
    public static final int MISSING_CRETIVE_TYPE = 201020;
    /**
     * 广告创意类型信息无法识别
     */
    public static final int ERROR_CRETIVE_TYPE = 201021;
    /**
     * 广告动作类型信息丢失
     */
    public static final int MISSING_INTERATION_TYPE = 201030;
    /**
     * 广告动作类型信息无法识别
     */
    public static final int ERROR_INTERATION_TYPE = 201031;
    /**
     * 曝光汇报地址丢失
     */
    public static final int MISSING_WIN_NOTICE_URL = 201040;
    /**
     * 曝光汇报地址异常
     */
    public static final int ERROR_WIN_NOTICE_URL_SIZE = 201041;
    /**
     * 点击响应地址丢失
     */
    public static final int MISSING_CLICK_URL = 201050;
    /**
     * 推广标题丢失
     */
    public static final int MISSING_TITLE = 201060;
    /**
     * 推广描述丢失
     */
    public static final int MISSING_DESCRIPTION = 201070;
    /**
     * 推广应用包名丢失
     */
    public static final int MISSING_APP_PACKAGE = 201080;
    /**
     * 推广应用包大小丢失
     */
    public static final int MISSING_APP_SIZE = 201090;
    /**
     * 推广图标丢失
     */
    public static final int MISSING_ICON_SRC = 201100;
    /**
     * 推广图片丢失
     */
    public static final int MISSING_IMAGE_SRC = 201110;
    /**
     * 广告json串错误
     */
    public static final int AD_BAD_JSON = 201111;
}

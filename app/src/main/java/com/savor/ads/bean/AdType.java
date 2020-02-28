package com.savor.ads.bean;

/**
 * 广告请求类型
 */
public enum AdType {

    /**
     * 随机返回图片或者视频
     */
    UNKNOWN(0),

    /**
     * 图片类型
     */
    IMAGE(1),

    /**
     * 视频类型
     */
    VIDEO(2);

    private int mValue;

    AdType(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}

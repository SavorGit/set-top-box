package com.savor.ads.bean;

/**
 * 广告请求类.
 */

public class AdRequestInfo {

    private AdType type;
    private int duration;
    private int matWidth;
    private int matHeight;

    public AdType getType() {
        return type;
    }

    /**
     * 设置广告请求类型，具体请参考AdType
     */
    public void setType(AdType type) {
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

    /**
     * 设置广告时长，单位秒
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMatWidth() {
        return matWidth;
    }

    /**
     * 设置广告素材宽度，单位px
     */
    public void setMatWidth(int matWidth) {
        this.matWidth = matWidth;
    }

    public int getMatHeight() {
        return matHeight;
    }

    /**
     * 设置广告素材高度，单位px
     */
    public void setMatHeight(int matHeight) {
        this.matHeight = matHeight;
    }
}

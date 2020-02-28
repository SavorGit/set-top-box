package com.savor.ads.bean;

public class AdPayloadBean {
    /**广告内容URL**/
    private String url;
    /**宽度（像素）**/
    private int width;
    /**高度（像素）**/
    private int height;
    /**广告内容签名（MD5，确保信息安全）**/
    private String sign;
    /**效果通知URL**/
    private String track_url;
    /**广告位ID**/
    private String slot_id;
    /**播放时长，秒**/
    private int show_time;
    /**过期时间，yyyyMMdd HH:mm:SS**/
    private String expire_time;
    /**文件大小KB**/
    private String file_size;
    /**广告类型，IMG - 图片，VDO - 视频**/
    private String type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTrack_url() {
        return track_url;
    }

    public void setTrack_url(String track_url) {
        this.track_url = track_url;
    }

    public String getSlot_id() {
        return slot_id;
    }

    public void setSlot_id(String slot_id) {
        this.slot_id = slot_id;
    }

    public int getShow_time() {
        return show_time;
    }

    public void setShow_time(int show_time) {
        this.show_time = show_time;
    }

    public String getExpire_time() {
        return expire_time;
    }

    public void setExpire_time(String expire_time) {
        this.expire_time = expire_time;
    }

    public String getFile_size() {
        return file_size;
    }

    public void setFile_size(String file_size) {
        this.file_size = file_size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

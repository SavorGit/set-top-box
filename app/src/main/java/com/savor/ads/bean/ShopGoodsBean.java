package com.savor.ads.bean;

public class ShopGoodsBean {
    //期刊号
    private String period;
    private long goods_id;
    private String chinese_name;
    /**10优选,20我的活动,30积分兑换现金 40秒杀商品*/
    private int type;
    private String location_id;
    private String oss_path;
    private String mediaPath;
    private String name;
    /**1视频 2 图片*/
    private int media_type;
    private String md5;
    private String duration;
    private String qrcode_url;
    /**
     * 商品活动销售二维码本地地址
     */
    private String qrcode_path;
    private String createTime;
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(long goods_id) {
        this.goods_id = goods_id;
    }

    public String getChinese_name() {
        return chinese_name;
    }

    public void setChinese_name(String chinese_name) {
        this.chinese_name = chinese_name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    public String getOss_path() {
        return oss_path;
    }

    public void setOss_path(String oss_path) {
        this.oss_path = oss_path;
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMedia_type() {
        return media_type;
    }

    public void setMedia_type(int media_type) {
        this.media_type = media_type;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getQrcode_url() {
        return qrcode_url;
    }

    public void setQrcode_url(String qrcode_url) {
        this.qrcode_url = qrcode_url;
    }

    public String getQrcode_path() {
        return qrcode_path;
    }

    public void setQrcode_path(String qrcode_path) {
        this.qrcode_path = qrcode_path;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}

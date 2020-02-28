package com.savor.ads.bean;

public class ActivityGoodsBean {
    //期刊号
    private String period;
    private long goods_id;
    private String chinese_name;
    private String price;
    /**10优选,20我的活动,30积分兑换现金 40秒杀商品*/
    private int type;
    /**1节目单播放 2非节目单播放*/
    private int play_type;
    private String oss_path;
    private String mediaPath;
    private String name;
    /**1视频 2 图片*/
    private int media_type;
    private String md5;
    private String duration;
    private String qrcode_url;
    /**是否店内有货 1有货 0无货*/
    private String is_storebuy;
    private String start_date;
    private String end_date;
    private String createTime;
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    /**
     * 商品活动销售二维码本地地址
     */
    private String qrcode_path;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPlay_type() {
        return play_type;
    }

    public void setPlay_type(int play_type) {
        this.play_type = play_type;
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

    public String getIs_storebuy() {
        return is_storebuy;
    }

    public void setIs_storebuy(String is_storebuy) {
        this.is_storebuy = is_storebuy;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
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

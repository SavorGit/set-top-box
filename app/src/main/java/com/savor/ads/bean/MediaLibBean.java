package com.savor.ads.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bichao on 2016/12/12.
 */

public class MediaLibBean implements Serializable {
    private long id;
    //视频ID
    private String vid;
    //商品ID
    private int goods_id;
    //视频检验值
    private String md5;
    //视频文件名称
    private String name;
    //视 频内容名称
    private String chinese_name;
    //业务类型 ads,adv,pro
    private String type;
    //资源属性类型 1视频,2图片,21图集
    private int media_type;
    //视频后缀名
    private String suffix;
    //视频时长
    private String duration;
    //商品价格
    private String price;
    /**1节目单播放 2非节目单播放*/
    private int play_type;
    /**是否店内有货 1有货 0无货*/
    private int is_storebuy;
    //期刊号
    private String period;
    //播放顺序排序
    private int order;
    private String taskId;
    private String url;
    private String area_id;

    private String mediaPath;

    private String location_id;
    private String start_date;
    private String end_date;

    /** ADMaster 曝光*/
    private String admaster_sin;
    /**下载状态 0：未下载； 1：已下载； 2：下载中*/
    private int download_state;
    /**聚屏类型：1.百度**/
    private String tpmedia_id;
    /**百度返回md5值**/
    private String tp_md5;
    /**oss资源路径**/
    private String oss_path;
    /**节目单中加入图集*/
    private List<MediaItemBean> subdata;

    /**0：不显示  1：显示**/
    private int is_sapp_qrcode;
    /**1:投屏前，2：投屏后**/
    private int play_position;

    private String createTime;
    /**
     * 商品活动销售二维码本地地址
     */
    private String qrcode_path;
    /**
     * 商品活动销售二维码网络地址
     */
    private String qrcode_url;
    private String nickName;
    private String avatarUrl;
    /**type:类型 1热播内容(上大屏内容) 2发现内容*/
    private int selectContentType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public int getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(int goods_id) {
        this.goods_id = goods_id;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChinese_name() {
        return chinese_name;
    }

    public void setChinese_name(String chinese_name) {
        this.chinese_name = chinese_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMedia_type() {
        return media_type;
    }

    public void setMedia_type(int media_type) {
        this.media_type = media_type;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getPlay_type() {
        return play_type;
    }

    public void setPlay_type(int play_type) {
        this.play_type = play_type;
    }

    public int getIs_storebuy() {
        return is_storebuy;
    }

    public void setIs_storebuy(int is_storebuy) {
        this.is_storebuy = is_storebuy;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArea_id() {
        return area_id;
    }

    public void setArea_id(String area_id) {
        this.area_id = area_id;
    }

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
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

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public String getAdmaster_sin() {
        return admaster_sin;
    }

    public void setAdmaster_sin(String admaster_sin) {
        this.admaster_sin = admaster_sin;
    }

    public int getDownload_state() {
        return download_state;
    }

    public void setDownload_state(int download_state) {
        this.download_state = download_state;
    }

    public String getTpmedia_id() {
        return tpmedia_id;
    }

    public void setTpmedia_id(String tpmedia_id) {
        this.tpmedia_id = tpmedia_id;
    }

    public String getTp_md5() {
        return tp_md5;
    }

    public void setTp_md5(String tp_md5) {
        this.tp_md5 = tp_md5;
    }

    public String getOss_path() {
        return oss_path;
    }

    public void setOss_path(String oss_path) {
        this.oss_path = oss_path;
    }

    public List<MediaItemBean> getSubdata() {
        return subdata;
    }

    public void setSubdata(List<MediaItemBean> subdata) {
        this.subdata = subdata;
    }

    public int getIs_sapp_qrcode() {
        return is_sapp_qrcode;
    }

    public void setIs_sapp_qrcode(int is_sapp_qrcode) {
        this.is_sapp_qrcode = is_sapp_qrcode;
    }

    public int getPlay_position() {
        return play_position;
    }

    public void setPlay_position(int play_position) {
        this.play_position = play_position;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getQrcode_path() {
        return qrcode_path;
    }

    public void setQrcode_path(String qrcode_path) {
        this.qrcode_path = qrcode_path;
    }

    public String getQrcode_url() {
        return qrcode_url;
    }

    public void setQrcode_url(String qrcode_url) {
        this.qrcode_url = qrcode_url;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getSelectContentType() {
        return selectContentType;
    }

    public void setSelectContentType(int selectContentType) {
        this.selectContentType = selectContentType;
    }
}


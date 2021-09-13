package com.savor.ads.bean;

import java.util.List;

/**
 * 该实体类用于发现接口和用户精选热播内容接口公用,通过type来区分接口数据
 */
public class SelectContentBean {
    /**数据标识ID*/
    private long id;
    /**播放时间*/
    private int duration;
    /**资源属性类型 1视频,2图片,21图集*/
    private int media_type;
    private String period;
    /**精选上大屏开始时间*/
    private String start_date;
    /**精选上大屏结束时间*/
    private String end_date;
    /**详细信息*/
    private List<MediaItemBean> subdata;
    private String createTime;
    private String nickName;
    private String avatarUrl;
    /**type:类型 1热播内容(上大屏内容) 2发现内容*/
    private int type;
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMedia_type() {
        return media_type;
    }

    public void setMedia_type(int media_type) {
        this.media_type = media_type;
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

    public List<MediaItemBean> getSubdata() {
        return subdata;
    }

    public void setSubdata(List<MediaItemBean> subdata) {
        this.subdata = subdata;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

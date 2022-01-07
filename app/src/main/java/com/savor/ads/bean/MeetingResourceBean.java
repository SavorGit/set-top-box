package com.savor.ads.bean;

public class MeetingResourceBean {

    private int id;
    private String name;
    private String oss_path;
    private String md5;
    private String start_date;
    private String end_date;
    //类型：1企业宣传片 2祝福视频
    private int type;
    //媒体类型：1视频 2图片3其他 4音频 5字体 （2和4和5 文件md5值取文件全部内容进行计算）
    private int media_type;
    private String media_path;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOss_path() {
        return oss_path;
    }

    public void setOss_path(String oss_path) {
        this.oss_path = oss_path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMedia_type() {
        return media_type;
    }

    public void setMedia_type(int media_type) {
        this.media_type = media_type;
    }

    public String getMedia_path() {
        return media_path;
    }

    public void setMedia_path(String media_path) {
        this.media_path = media_path;
    }
}

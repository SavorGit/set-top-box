package com.savor.ads.bean;

/**
 * 生日点播相关视频
 */
public class BirthdayOndemandBean {
    //资源id
    private String media_id;
    //资源名称
    private String media_name;
    //中文名称
    private String name;
    //后缀名
    private String surfix;
    //oss资源完整路径
    private String oss_url;
    //oss地址
    private String oss_path;
    //资源存储路径
    private String media_path;
    //文件md5值
    private String md5;
    //类型
    private int type;

    public String getMedia_id() {
        return media_id;
    }

    public void setMedia_id(String media_id) {
        this.media_id = media_id;
    }

    public String getMedia_name() {
        return media_name;
    }

    public void setMedia_name(String media_name) {
        this.media_name = media_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurfix() {
        return surfix;
    }

    public void setSurfix(String surfix) {
        this.surfix = surfix;
    }

    public String getOss_url() {
        return oss_url;
    }

    public void setOss_url(String oss_url) {
        this.oss_url = oss_url;
    }

    public String getOss_path() {
        return oss_path;
    }

    public void setOss_path(String oss_path) {
        this.oss_path = oss_path;
    }

    public String getMedia_path() {
        return media_path;
    }

    public void setMedia_path(String media_path) {
        this.media_path = media_path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

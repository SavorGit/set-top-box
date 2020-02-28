package com.savor.ads.bean;

import java.io.Serializable;
/**
 * 4g投屏实体
 */
public class Push4GProjection implements Serializable{
    //mac地址
    private String box_mac;
    //资源地址
    private String resource_url;
    //资源名称
    private String resource_name;
    //资源类型：1图片，2视频
    private int resource_type;

    public String getBox_mac() {
        return box_mac;
    }

    public void setBox_mac(String box_mac) {
        this.box_mac = box_mac;
    }

    public String getResource_url() {
        return resource_url;
    }

    public void setResource_url(String resource_url) {
        this.resource_url = resource_url;
    }

    public String getResource_name() {
        return resource_name;
    }

    public void setResource_name(String resource_name) {
        this.resource_name = resource_name;
    }

    public int getResource_type() {
        return resource_type;
    }

    public void setResource_type(int resource_type) {
        this.resource_type = resource_type;
    }
}

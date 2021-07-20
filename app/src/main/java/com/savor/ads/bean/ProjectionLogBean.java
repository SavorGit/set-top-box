package com.savor.ads.bean;

import java.io.Serializable;

public class ProjectionLogBean implements Serializable {

    private String action;
    private String serial_number;
    private String box_mac;
    private String duration;
    private String forscreen_char;
    private String forscreen_id;
    private String mobile_brand;
    private String mobile_model;
    private String openid;
    private String resource_id;
    private String resource_size;
    private String resource_type;
    private String media_path;
    private String media_screenshot_path;
    //0：未上传，1：已上传
    private String upload;
    //0：一投，1：重投
    private String repeat;
    private String small_app_id;
    //文件总共有多少页
    private int pages;
    private String create_time;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getBox_mac() {
        return box_mac;
    }

    public void setBox_mac(String box_mac) {
        this.box_mac = box_mac;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getForscreen_char() {
        return forscreen_char;
    }

    public void setForscreen_char(String forscreen_char) {
        this.forscreen_char = forscreen_char;
    }

    public String getForscreen_id() {
        return forscreen_id;
    }

    public void setForscreen_id(String forscreen_id) {
        this.forscreen_id = forscreen_id;
    }

    public String getMobile_brand() {
        return mobile_brand;
    }

    public void setMobile_brand(String mobile_brand) {
        this.mobile_brand = mobile_brand;
    }

    public String getMobile_model() {
        return mobile_model;
    }

    public void setMobile_model(String mobile_model) {
        this.mobile_model = mobile_model;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getResource_id() {
        return resource_id;
    }

    public void setResource_id(String resource_id) {
        this.resource_id = resource_id;
    }

    public String getResource_size() {
        return resource_size;
    }

    public void setResource_size(String resource_size) {
        this.resource_size = resource_size;
    }

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }

    public String getMedia_path() {
        return media_path;
    }

    public void setMedia_path(String media_path) {
        this.media_path = media_path;
    }

    public String getMedia_screenshot_path() {
        return media_screenshot_path;
    }

    public void setMedia_screenshot_path(String media_screenshot_path) {
        this.media_screenshot_path = media_screenshot_path;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getSmall_app_id() {
        return small_app_id;
    }

    public void setSmall_app_id(String small_app_id) {
        this.small_app_id = small_app_id;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }
}

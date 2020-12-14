package com.savor.ads.bean;

import java.io.Serializable;

public class ProjectionLogDetail implements Serializable {

    private String duration;
    private String resource_id;
    private String resource_size;
    private String media_path;
    private String media_screenshot_path;
    //0：未上传，1：已上传
    private int upload;
    private String small_app_id;
    private String create_time;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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

    public int getUpload() {
        return upload;
    }

    public void setUpload(int upload) {
        this.upload = upload;
    }

    public String getSmall_app_id() {
        return small_app_id;
    }

    public void setSmall_app_id(String small_app_id) {
        this.small_app_id = small_app_id;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }
}

package com.savor.ads.bean;

import java.io.Serializable;

public class BigImgBean implements Serializable {

    private String filenameId;
    private String thumbnailPath;
    private String bigPath;
    //统计使用
    private String serial_number;
    private String forscreen_id;
    private String deviceId;

    public String getFilenameId() {
        return filenameId;
    }

    public void setFilenameId(String filenameId) {
        this.filenameId = filenameId;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getBigPath() {
        return bigPath;
    }

    public void setBigPath(String bigPath) {
        this.bigPath = bigPath;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getForscreen_id() {
        return forscreen_id;
    }

    public void setForscreen_id(String forscreen_id) {
        this.forscreen_id = forscreen_id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}

package com.savor.ads.bean;

/**
 * Created by gaowen on 2017/10/18.
 */

public class MediaDevice {

    private int deviceType;
    private int osType;
    private String osVersion;
    private String vendor;
    private String model;
    private int screenWidth;
    private int screenHeight;

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getOsType() {
        return osType;
    }

    public void setOsType(int osType) {
        this.osType = osType;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public enum DeviceType {

        PHONE(1),
        TABLET(2),
        SMART_TV(3),
        OUTDOOR_SCREEN(4);

        private int mValue;

        DeviceType(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }

    public enum OsType {

        ANDROID(1),
        IOS(2),
        WINDOWS(3);

        private int mValue;

        OsType(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }
}

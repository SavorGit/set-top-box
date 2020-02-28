package com.jar.savor.box.vo;

/**
 * Created by zhang.haiqiang on 2017/7/27.
 */

public class QueryStatusResponseVo extends BaseResponse {
    private int status;
    private String deviceId;
    private String deviceName;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}

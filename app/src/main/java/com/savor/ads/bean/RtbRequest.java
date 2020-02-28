package com.savor.ads.bean;

/**
 * Created by gaowen on 2017/10/18.
 */

public class RtbRequest {

    private String playCode;
    private String requestId;
    private String positionId;
    private String channelId;
    private String token;
    private MediaDevice device;
    private MediaUdId udid;
    private MediaNetWork network;
    private AdSlot adSlot;

    public String getPlayCode() {
        return playCode;
    }

    public void setPlayCode(String playCode) {
        this.playCode = playCode;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MediaDevice getDevice() {
        return device;
    }

    public void setDevice(MediaDevice device) {
        this.device = device;
    }

    public MediaUdId getUdid() {
        return udid;
    }

    public void setUdid(MediaUdId udid) {
        this.udid = udid;
    }

    public MediaNetWork getNetwork() {
        return network;
    }

    public void setNetwork(MediaNetWork network) {
        this.network = network;
    }

    public AdSlot getAdSlot() {
        return adSlot;
    }

    public void setAdSlot(AdSlot adSlot) {
        this.adSlot = adSlot;
    }
}

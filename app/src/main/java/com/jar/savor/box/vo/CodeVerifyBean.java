package com.jar.savor.box.vo;

/**
 * Created by zhanghq on 2017/3/24.
 */

public class CodeVerifyBean {
    private String box_id;
    private String box_ip;
    private String box_mac;
    private String hotel_id;
    private String room_id;
    private String ssid;

    public String getBox_id() {
        return box_id;
    }

    public void setBox_id(String box_id) {
        this.box_id = box_id;
    }

    public String getBox_ip() {
        return box_ip;
    }

    public void setBox_ip(String box_ip) {
        this.box_ip = box_ip;
    }

    public String getBox_mac() {
        return box_mac;
    }

    public void setBox_mac(String box_mac) {
        this.box_mac = box_mac;
    }

    public String getHotel_id() {
        return hotel_id;
    }

    public void setHotel_id(String hotel_id) {
        this.hotel_id = hotel_id;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
}

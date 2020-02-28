package com.savor.ads.log;

/**
 * 小程序码显示日志
 */
public class QRCodeLogBean {

    /**
     * 盒子ID
     */
    private String boxId = "";

    /**
     * 酒楼ID
     */
    private String hotel_id;
    /**
     * 包间ID
     */
    private String room_id;
    /**
     * 日志时间
     */
    private String time = "";
    /**
     * 动作,start ,end
     */
    private String action = "";

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}

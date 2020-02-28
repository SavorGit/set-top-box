package com.savor.ads.log;


public class LogReportParam {
    /**
     * 视频UUID
     */
    private String UUid = "";
    /**
     * 盒子ID
     */
    private String boxId = "";
    /**
     * 日志生成的小时
     */
    private String logHour;
    /**
     * 酒楼ID
     */
    private String hotel_id;
    /**
     * 包间ID
     */
    private String room_id;
    /**
     * 媒体文件ID
     */
    private String media_id = "";
    /**
     * 日志时间
     */
    private String time = "";
    /**
     * 动作
     */
    private String action = "";//"poweron","start","pause","resume","end"
    /**
     * 视频类型
     */
    private String type = "";//"Ads","TV","Phone"
    /**
     * 手机ID
     */
    private String mobile_id;
    /**
     * APK版本号
     */
    private String apk_version;
    /**
     * 广告视频期号
     */
    private String adsPeriod;
    /**
     * 生日歌视频期号
     */
    private String birthdayPeriod;
    /**
     * 通用参数
     */
    private String custom;

    public LogReportParam() {
        time = System.currentTimeMillis() + "";
    }

    public String getHotel_id() {
        return hotel_id == null ? "" : hotel_id;
    }

    public void setHotel_id(String hotel_id) {
        this.hotel_id = hotel_id;
    }

    public String getRoom_id() {
        return room_id == null ? "" : room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getUUid() {
        return UUid == null ? "" : UUid;
    }

    public void setUUid(String UUid) {
        this.UUid = UUid;
    }

    public String getMedia_id() {
        return media_id == null ? "" : media_id;
    }

    public void setMedia_id(String media_id) {
        this.media_id = media_id;
    }

    public String getTime() {
        return time == null ? "" : time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAction() {
        return action == null ? "" : action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMobile_id() {
        return mobile_id == null ? "" : mobile_id;
    }

    public void setMobile_id(String mobile_id) {
        this.mobile_id = mobile_id;
    }

    public String getApk_version() {
        return apk_version == null ? "" : apk_version;
    }

    public void setApk_version(String apk_version) {
        this.apk_version = apk_version;
    }

    public String getAdsPeriod() {
        return adsPeriod == null ? "" : adsPeriod;
    }

    public void setAdsPeriod(String adsPeriod) {
        this.adsPeriod = adsPeriod;
    }

    public String getBirthdayPeriod() {
        return birthdayPeriod;
    }

    public void setBirthdayPeriod(String birthdayPeriod) {
        this.birthdayPeriod = birthdayPeriod;
    }

    public String getCustom() {
        return custom == null ? "" : custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public String getBoxId() {
        return boxId == null ? "" : boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    public String getLogHour() {
        return logHour == null ? "" : logHour;
    }

    public void setLogHour(String logHour) {
        this.logHour = logHour;
    }
}

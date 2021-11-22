package com.savor.ads.bean;

import java.util.ArrayList;

/**
 * Created by zhanghq on 2017/2/7.
 */

public class BoxInitBean {
    /**电视切换时间*/
    private int switch_time;
    /**音量大小*/
    private int volume;
    /**酒楼ID*/
    private String hotel_id;
    /**包间ID*/
    private String room_id;
    /**酒楼名称*/
    private String hotel_name;
    /**包间名称*/
    private String room_name;
    /**地区ID*/
    private String area_id;
    /**logo地址*/
    private String logo_url;
    /**logo检验MD5*/
    private String logo_md5;
    /**视频投屏加载图地址*/
    private String loading_img_url;
    /**视频投屏加载图检验MD5*/
    private String loading_img_md5;
    /**OSS桶ID*/
    private String ossBucketName;
    /**盒子名称*/
    private String box_name;
    /**盒子ID*/
    private String box_id;
    /**包间类型*/
    private String room_type;

    /** 轮播音量*/
    private int ads_volume;
    /** 投屏音量*/
    private int project_volume;
    /**夏新电视投屏音量*/
    private int forscreen_volume;
    /** 点播音量*/
    private int demand_volume;
    /** 电视节目音量*/
    private int tv_volume;

    private ArrayList<VersionInfo> playbill_version_list;
    private ArrayList<VersionInfo> demand_version_list;
    private ArrayList<VersionInfo> logo_version_list;
    private ArrayList<VersionInfo> loading_version_list;
    private ArrayList<VersionInfo> apk_version_list;
    private ArrayList<VersionInfo> small_web_version_list;
    /**电视信号**/
    private ArrayList<Television> tv_list;
    /**开机视频*/
    private BootVideo bootvideo;

    private String sys_volume;

    public void setSwitch_time(int switch_time) {
        this.switch_time = switch_time;
    }

    public int getSwitch_time() {
        return switch_time;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getVolume() {
        return volume;
    }

    public void setHotel_id(String hotel_id) {
        this.hotel_id = hotel_id;
    }

    public String getHotel_id() {
        return hotel_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setHotel_name(String hotel_name) {
        this.hotel_name = hotel_name;
    }

    public String getHotel_name() {
        return hotel_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public String getRoom_name() {
        return room_name;
    }

    public String getArea_id() {
        return area_id;
    }

    public void setArea_id(String area_id) {
        this.area_id = area_id;
    }

    public void setLogo_url(String logo_url) {
        this.logo_url = logo_url;
    }

    public String getLogo_url() {
        return logo_url;
    }

    public void setLogo_md5(String logo_md5) {
        this.logo_md5 = logo_md5;
    }

    public String getLogo_md5() {
        return logo_md5;
    }

    public String getLoading_img_url() {
        return loading_img_url;
    }

    public void setLoading_img_url(String loading_img_url) {
        this.loading_img_url = loading_img_url;
    }

    public String getLoading_img_md5() {
        return loading_img_md5;
    }

    public void setLoading_img_md5(String loading_img_md5) {
        this.loading_img_md5 = loading_img_md5;
    }

    public void setOssBucketName(String ossBucketName) {
        this.ossBucketName = ossBucketName;
    }

    public String getOssBucketName() {
        return ossBucketName;
    }

    public String getBox_name() {
        return box_name;
    }

    public void setBox_name(String box_name) {
        this.box_name = box_name;
    }

    public String getRoom_type() {
        return room_type;
    }

    public void setRoom_type(String room_type) {
        this.room_type = room_type;
    }

    public String getBox_id() {
        return box_id;
    }

    public void setBox_id(String box_id) {
        this.box_id = box_id;
    }

    public int getAds_volume() {
        return ads_volume;
    }

    public void setAds_volume(int ads_volume) {
        this.ads_volume = ads_volume;
    }

    public int getProject_volume() {
        return project_volume;
    }

    public void setProject_volume(int project_volume) {
        this.project_volume = project_volume;
    }

    public int getForscreen_volume() {
        return forscreen_volume;
    }

    public void setForscreen_volume(int forscreen_volume) {
        this.forscreen_volume = forscreen_volume;
    }

    public int getDemand_volume() {
        return demand_volume;
    }

    public void setDemand_volume(int demand_volume) {
        this.demand_volume = demand_volume;
    }

    public int getTv_volume() {
        return tv_volume;
    }

    public void setTv_volume(int tv_volume) {
        this.tv_volume = tv_volume;
    }

    public ArrayList<VersionInfo> getPlaybill_version_list() {
        return playbill_version_list;
    }

    public void setPlaybill_version_list(ArrayList<VersionInfo> playbill_version_list) {
        this.playbill_version_list = playbill_version_list;
    }

    public ArrayList<VersionInfo> getDemand_version_list() {
        return demand_version_list;
    }

    public void setDemand_version_list(ArrayList<VersionInfo> demand_version_list) {
        this.demand_version_list = demand_version_list;
    }

    public ArrayList<VersionInfo> getLogo_version_list() {
        return logo_version_list;
    }

    public void setLogo_version_list(ArrayList<VersionInfo> logo_version_list) {
        this.logo_version_list = logo_version_list;
    }

    public ArrayList<VersionInfo> getLoading_version_list() {
        return loading_version_list;
    }

    public void setLoading_version_list(ArrayList<VersionInfo> loading_version_list) {
        this.loading_version_list = loading_version_list;
    }

    public ArrayList<VersionInfo> getApk_version_list() {
        return apk_version_list;
    }

    public void setApk_version_list(ArrayList<VersionInfo> apk_version_list) {
        this.apk_version_list = apk_version_list;
    }

    public ArrayList<VersionInfo> getSmall_web_version_list() {
        return small_web_version_list;
    }

    public void setSmall_web_version_list(ArrayList<VersionInfo> small_web_version_list) {
        this.small_web_version_list = small_web_version_list;
    }

    public ArrayList<Television> getTv_list() {
        return tv_list;
    }

    public void setTv_list(ArrayList<Television> tv_list) {
        this.tv_list = tv_list;
    }

    public BootVideo getBootvideo() {
        return bootvideo;
    }

    public void setBootvideo(BootVideo bootvideo) {
        this.bootvideo = bootvideo;
    }

    public String getSys_volume() {
        return sys_volume;
    }

    public void setSys_volume(String sys_volume) {
        this.sys_volume = sys_volume;
    }
}

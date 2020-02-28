package com.savor.ads.bean;

import java.io.Serializable;

/**
 * 单机版盒子使用
 * Created by bichao on 2017/12/12.
 */

public class BoxBean implements Serializable{

    /**盒子名称*/
    private String box_name;
    /**盒子ID*/
    private String box_id;
    /**盒子MAC*/
    private String box_mac;
    /**切换时间*/
    private int switch_time;
    /**电视声音*/
    private int volume;
    /**包间ID*/
    private String room_id;
    /** 轮播音量*/
    private int ads_volume;
    /** 投屏音量*/
    private int project_volume;
    /** 点播音量*/
    private int demand_volume;
    /** 电视节目音量*/
    private int tv_volume;

    public String getBox_name() {
        return box_name;
    }

    public void setBox_name(String box_name) {
        this.box_name = box_name;
    }

    public String getBox_id() {
        return box_id;
    }

    public void setBox_id(String box_id) {
        this.box_id = box_id;
    }

    public String getBox_mac() {
        return box_mac;
    }

    public void setBox_mac(String box_mac) {
        this.box_mac = box_mac;
    }

    public int getSwitch_time() {
        return switch_time;
    }

    public void setSwitch_time(int switch_time) {
        this.switch_time = switch_time;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
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
}

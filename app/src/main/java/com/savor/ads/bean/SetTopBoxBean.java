package com.savor.ads.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bichao on 2016/12/12.
 */

public class SetTopBoxBean implements Serializable{
    //盒子ID
    private String box_id;
    //盒子MAC地址
    private String box_mac;
    //切换时间
    private int  switch_time;
    //视频title
    private String subtitle;
    //期刊号
    private String period;
    //声音设置
    private int volume;
    //盒子位置
    private String position;
    //盒子描述
    private String description;
    /**通过MAC匹配所在包间*/
    private List<RoomBean> room_info;
    //所在酒楼信息
    private BoiteBean boite;
    /**U盘更新媒体库*/
    private ArrayList<MediaLibBean> play_list;
    /**媒体库*/
    private ArrayList<ProgramBean> playbill_list;
    /** 发布时间*/
    private String pub_time;

    private UpgradeInfo version;

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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RoomBean> getRoom_info() {
        return room_info;
    }

    public void setRoom_info(List<RoomBean> room_info) {
        this.room_info = room_info;
    }

    public BoiteBean getBoite() {
        return boite;
    }

    public void setBoite(BoiteBean boite) {
        this.boite = boite;
    }

    public String getPub_time() {
        return pub_time;
    }

    public void setPub_time(String pub_time) {
        this.pub_time = pub_time;
    }

    public ArrayList<MediaLibBean> getPlay_list() {
        return play_list;
    }

    public void setPlay_list(ArrayList<MediaLibBean> play_list) {
        this.play_list = play_list;
    }

    public ArrayList<ProgramBean> getPlaybill_list() {
        return playbill_list;
    }

    public void setPlaybill_list(ArrayList<ProgramBean> playbill_list) {
        this.playbill_list = playbill_list;
    }

    public UpgradeInfo getVersion() {
        return version;
    }

    public void setVersion(UpgradeInfo version) {
        this.version = version;
    }
}

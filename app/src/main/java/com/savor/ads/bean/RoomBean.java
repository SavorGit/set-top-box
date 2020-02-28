package com.savor.ads.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 包间实体
 * Created by bichao on 2016/12/12.
 */

public class RoomBean implements Serializable{
    //包间ID
    private String room_id;
    //包间名称
    private String room_name;
    //包间号
    private String room_num;
    //包间类型
    private String room_type;
    /**通过MAC循环匹配盒子那个包间*/
    private List<BoxBean> box_list;
    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getRoom_type() {
        return room_type;
    }

    public void setRoom_type(String room_type) {
        this.room_type = room_type;
    }

    public String getRoom_num() {
        return room_num;
    }

    public void setRoom_num(String room_num) {
        this.room_num = room_num;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public List<BoxBean> getBox_list() {
        return box_list;
    }

    public void setBox_list(List<BoxBean> box_list) {
        this.box_list = box_list;
    }
}

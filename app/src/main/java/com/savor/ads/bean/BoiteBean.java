package com.savor.ads.bean;

import java.io.Serializable;

/**
 * 酒楼信息
 * Created by bichao on 2016/12/12.
 */

public class BoiteBean implements Serializable{

    private String hotel_id;
    private String hotel_name;
    private String address;
    private String area_id;
    public String getHotel_id() {
        return hotel_id;
    }

    public void setHotel_id(String hotel_id) {
        this.hotel_id = hotel_id;
    }

    public String getHotel_name() {
        return hotel_name;
    }

    public void setHotel_name(String hotel_name) {
        this.hotel_name = hotel_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getArea_id() {
        return area_id;
    }

    public void setArea_id(String area_id) {
        this.area_id = area_id;
    }
}

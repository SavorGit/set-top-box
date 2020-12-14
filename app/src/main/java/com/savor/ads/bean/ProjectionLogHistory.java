package com.savor.ads.bean;

import java.io.Serializable;
import java.util.List;

public class ProjectionLogHistory implements Serializable {

    private String action;
    private String serial_number;
    private String box_mac;
    private String forscreen_char;
    private String forscreen_id;
    private String mobile_brand;
    private String mobile_model;
    private String openid;
    private String resource_type;
    private List<ProjectionLogDetail> list;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getBox_mac() {
        return box_mac;
    }

    public void setBox_mac(String box_mac) {
        this.box_mac = box_mac;
    }

    public String getForscreen_char() {
        return forscreen_char;
    }

    public void setForscreen_char(String forscreen_char) {
        this.forscreen_char = forscreen_char;
    }

    public String getForscreen_id() {
        return forscreen_id;
    }

    public void setForscreen_id(String forscreen_id) {
        this.forscreen_id = forscreen_id;
    }

    public String getMobile_brand() {
        return mobile_brand;
    }

    public void setMobile_brand(String mobile_brand) {
        this.mobile_brand = mobile_brand;
    }

    public String getMobile_model() {
        return mobile_model;
    }

    public void setMobile_model(String mobile_model) {
        this.mobile_model = mobile_model;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }

    public List<ProjectionLogDetail> getList() {
        return list;
    }

    public void setList(List<ProjectionLogDetail> list) {
        this.list = list;
    }
}

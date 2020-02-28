package com.savor.ads.bean;


import java.io.Serializable;

/**
 * 电视机信息
 * Created by 张海强 on 2018/07/05 0007 下午 2:46.
 */

public class Television implements Serializable {


    /*** 主键id*/
    private Long tv_id;

    /*** 机顶盒id*/
    private long box_id;

    /*** 电视型号*/
    private String tv_Brand;

    /*** 电视尺寸*/
    private String tv_size;

    /*** 电视信号类型*/
    private String tv_source;

    /***新加属性*/
    private Byte flag;
    private Byte state;

    public Long getTv_id() {
        return tv_id;
    }

    public void setTv_id(Long tv_id) {
        this.tv_id = tv_id;
    }

    public long getBox_id() {
        return box_id;
    }

    public void setBox_id(long box_id) {
        this.box_id = box_id;
    }

    public String getTv_Brand() {
        return tv_Brand;
    }

    public void setTv_Brand(String tv_Brand) {
        this.tv_Brand = tv_Brand;
    }

    public String getTv_size() {
        return tv_size;
    }

    public void setTv_size(String tv_size) {
        this.tv_size = tv_size;
    }

    public String getTv_source() {
        return tv_source;
    }

    public void setTv_source(String tv_source) {
        this.tv_source = tv_source;
    }

    public Byte getFlag() {
        return flag;
    }

    public void setFlag(Byte flag) {
        this.flag = flag;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }
}

package com.savor.ads.bean;

import java.util.List;

public class JDmomediaBean {
    //本次请求唯一标识
    private String request_id;
    //广告素材唯一标识
    private int ad_key;
    //播放检测地址集
    private List<List<String>> ad_tracking;

    private JDmomediaMaterial material;

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public int getAd_key() {
        return ad_key;
    }

    public void setAd_key(int ad_key) {
        this.ad_key = ad_key;
    }

    public List<List<String>> getAd_tracking() {
        return ad_tracking;
    }

    public void setAd_tracking(List<List<String>> ad_tracking) {
        this.ad_tracking = ad_tracking;
    }

    public JDmomediaMaterial getMaterial() {
        return material;
    }

    public void setMaterial(JDmomediaMaterial material) {
        this.material = material;
    }
}

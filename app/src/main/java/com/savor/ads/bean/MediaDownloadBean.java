package com.savor.ads.bean;

/**
 * Created by Administrator on 2018/3/12.
 */

public class MediaDownloadBean {
    private String media_id;
    private String order;
    private int state;

    public String getMedia_id() {
        return media_id;
    }

    public void setMedia_id(String media_id) {
        this.media_id = media_id;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}

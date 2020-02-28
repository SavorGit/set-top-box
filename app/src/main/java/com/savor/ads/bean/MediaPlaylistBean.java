package com.savor.ads.bean;

/**
 * Created by Administrator on 2018/3/12.
 */

public class MediaPlaylistBean {
    private String media_id;
    private int order;
    private String type;

    public String getMedia_id() {
        return media_id;
    }

    public void setMedia_id(String media_id) {
        this.media_id = media_id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.savor.ads.bean;

import java.util.List;

public class LocalLifeAdsResult {

    private String period;
    private List<MediaLibBean> media_list;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<MediaLibBean> getMedia_list() {
        return media_list;
    }

    public void setMedia_list(List<MediaLibBean> media_list) {
        this.media_list = media_list;
    }
}

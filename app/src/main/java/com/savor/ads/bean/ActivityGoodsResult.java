package com.savor.ads.bean;

import java.util.List;

public class ActivityGoodsResult {

    private String period;
    private List<ActivityGoodsBean> datalist;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<ActivityGoodsBean> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<ActivityGoodsBean> datalist) {
        this.datalist = datalist;
    }
}

package com.savor.ads.bean;

import java.util.List;

public class ShopGoodsResult {

    private String period;
    private List<ShopGoodsBean> datalist;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<ShopGoodsBean> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<ShopGoodsBean> datalist) {
        this.datalist = datalist;
    }
}

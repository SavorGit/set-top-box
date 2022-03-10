package com.savor.ads.bean;

import java.util.List;

public class SeckillGoodsResult {

    private List<SeckillGoodsBean> datalist;
    private String[] roll_content;

    public List<SeckillGoodsBean> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<SeckillGoodsBean> datalist) {
        this.datalist = datalist;
    }

    public String[] getRoll_content() {
        return roll_content;
    }

    public void setRoll_content(String[] roll_content) {
        this.roll_content = roll_content;
    }
}

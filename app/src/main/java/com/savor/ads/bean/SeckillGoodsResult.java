package com.savor.ads.bean;

import java.util.List;

public class SeckillGoodsResult {

    private List<SeckillGoodsBean> datalist;
    private String[] roll_content;
    private int left_pop_wind;//是否展示灯笼窗口||0:不展示 1:展示
    private int marquee;//是否展示跑马灯窗口||0:不展示 1:展示

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

    public int getLeft_pop_wind() {
        return left_pop_wind;
    }

    public void setLeft_pop_wind(int left_pop_wind) {
        this.left_pop_wind = left_pop_wind;
    }

    public int getMarquee() {
        return marquee;
    }

    public void setMarquee(int marquee) {
        this.marquee = marquee;
    }
}

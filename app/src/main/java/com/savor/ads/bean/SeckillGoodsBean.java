package com.savor.ads.bean;

public class SeckillGoodsBean {

    private int goods_id;
    private int remain_time;
    private String image;
    private String price;
    private String line_price;
    private String[] roll_content;

    public int getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(int goods_id) {
        this.goods_id = goods_id;
    }

    public int getRemain_time() {
        return remain_time;
    }

    public void setRemain_time(int remain_time) {
        this.remain_time = remain_time;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLine_price() {
        return line_price;
    }

    public void setLine_price(String line_price) {
        this.line_price = line_price;
    }

    public String[] getRoll_content() {
        return roll_content;
    }

    public void setRoll_content(String[] roll_content) {
        this.roll_content = roll_content;
    }
}

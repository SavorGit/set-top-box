package com.savor.ads.bean;

import java.io.Serializable;

/**
 * Created by zhanghq on 2017/5/9.
 */

public class PrizeItem implements Serializable {
    private int prize_id;
    private String prize_name;
    private int prize_num;
    private int prize_pos;
    private int prize_level;

    public int getPrize_id() {
        return prize_id;
    }

    public void setPrize_id(int prize_id) {
        this.prize_id = prize_id;
    }

    public String getPrize_name() {
        return prize_name;
    }

    public void setPrize_name(String prize_name) {
        this.prize_name = prize_name;
    }

    public int getPrize_num() {
        return prize_num;
    }

    public void setPrize_num(int prize_num) {
        this.prize_num = prize_num;
    }

    public int getPrize_pos() {
        return prize_pos;
    }

    public void setPrize_pos(int prize_pos) {
        this.prize_pos = prize_pos;
    }

    public int getPrize_level() {
        return prize_level;
    }

    public void setPrize_level(int prize_level) {
        this.prize_level = prize_level;
    }
}

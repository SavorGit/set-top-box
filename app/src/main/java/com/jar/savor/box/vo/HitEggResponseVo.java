package com.jar.savor.box.vo;

/**
 * Created by zhanghq on 2017/5/8.
 */

public class HitEggResponseVo extends BaseResponse {
    /** 砸蛋进度*/
    private int progress;
    /** 是否已砸开 0否1是*/
    private int done;
    /** 是否中奖 0否1是*/
    private int win;
    /** 奖项ID*/
    private int prize_id;
    /** 奖项名*/
    private String prize_name;
    /** 抽奖时间*/
    private String prize_time;
    private int prize_level;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getDone() {
        return done;
    }

    public void setDone(int done) {
        this.done = done;
    }

    public int getWin() {
        return win;
    }

    public void setWin(int win) {
        this.win = win;
    }

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

    public String getPrize_time() {
        return prize_time;
    }

    public void setPrize_time(String prize_time) {
        this.prize_time = prize_time;
    }

    public int getPrize_level() {
        return prize_level;
    }

    public void setPrize_level(int prize_level) {
        this.prize_level = prize_level;
    }
}

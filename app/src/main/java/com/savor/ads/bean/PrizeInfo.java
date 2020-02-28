package com.savor.ads.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhanghq on 2017/5/9.
 */

public class PrizeInfo implements Serializable {
    private String date_time;
    private List<PrizeItem> prize;
    private List<AwardTime> award_time;

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public List<PrizeItem> getPrize() {
        return prize;
    }

    public void setPrize(List<PrizeItem> prize) {
        this.prize = prize;
    }

    public List<AwardTime> getAward_time() {
        return award_time;
    }

    public void setAward_time(List<AwardTime> award_time) {
        this.award_time = award_time;
    }
}

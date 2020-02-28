package com.savor.ads.bean;

import com.savor.tvlibrary.AtvChannel;

import java.util.ArrayList;

/**
 * Created by zhanghq on 2016/12/27.
 */

public class TvProgramGiecResponse {

    private ArrayList<AtvChannel> tvChannelList;

    private int lockingChannelNum;

    public ArrayList<AtvChannel> getTvChannelList() {
        return tvChannelList;
    }

    public void setTvChannelList(ArrayList<AtvChannel> tvChannelList) {
        this.tvChannelList = tvChannelList;
    }

    public int getLockingChannelNum() {
        return lockingChannelNum;
    }

    public void setLockingChannelNum(int lockingChannelNum) {
        this.lockingChannelNum = lockingChannelNum;
    }
}

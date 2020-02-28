package com.savor.ads.bean;

import java.util.ArrayList;

/**
 * Created by zhanghq on 2016/12/27.
 */

public class TvProgramResponse {

    private ArrayList<AtvProgramInfo> tvChannelList;

    private int lockingChannelNum;

    public ArrayList<AtvProgramInfo> getTvChannelList() {
        return tvChannelList;
    }

    public void setTvChannelList(ArrayList<AtvProgramInfo> tvChannelList) {
        this.tvChannelList = tvChannelList;
    }

    public int getLockingChannelNum() {
        return lockingChannelNum;
    }

    public void setLockingChannelNum(int lockingChannelNum) {
        this.lockingChannelNum = lockingChannelNum;
    }
}

package com.savor.ads.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by bichao on 2016/12/12.
 */

public class DownloadDetailRequestBean implements Serializable {
    private String period;
    private ArrayList<MediaDownloadBean> list;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public ArrayList<MediaDownloadBean> getList() {
        return list;
    }

    public void setList(ArrayList<MediaDownloadBean> list) {
        this.list = list;
    }
}


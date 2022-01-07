package com.savor.ads.bean;

import java.util.List;

public class MeetingResourceResult {

    private String period;
    private List<MeetingResourceBean> datalist;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<MeetingResourceBean> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<MeetingResourceBean> datalist) {
        this.datalist = datalist;
    }
}

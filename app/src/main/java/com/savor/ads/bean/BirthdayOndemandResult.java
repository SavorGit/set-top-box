package com.savor.ads.bean;

import java.util.List;

public class BirthdayOndemandResult {

    private String period;

    private List<BirthdayOndemandBean> datalist;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<BirthdayOndemandBean> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<BirthdayOndemandBean> datalist) {
        this.datalist = datalist;
    }
}

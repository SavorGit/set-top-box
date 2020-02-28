package com.savor.ads.bean;

import java.util.List;

public class WelcomeResourceResult {

    private String period;
    private List<WelcomeResourceBean> datalist;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<WelcomeResourceBean> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<WelcomeResourceBean> datalist) {
        this.datalist = datalist;
    }
}

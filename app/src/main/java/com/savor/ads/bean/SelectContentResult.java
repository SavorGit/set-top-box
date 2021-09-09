package com.savor.ads.bean;

import java.util.List;

/**
 * 用户精选内容
 */
public class SelectContentResult {

    private String period;
    private List<String> list;
    private List<SelectContentBean> datalist;
    /**type:类型 1热播内容(上大屏内容) 2发现内容*/
    private int type;
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public List<SelectContentBean> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<SelectContentBean> datalist) {
        this.datalist = datalist;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

package com.savor.ads.bean;

import java.io.Serializable;

/**
 * Created by bichao on 2016/12/19.
 */

public class UpgradeResult implements Serializable{

    private int code;
    private String msg;
    private UpgradeInfo result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public UpgradeInfo getResult() {
        return result;
    }

    public void setResult(UpgradeInfo result) {
        this.result = result;
    }
}

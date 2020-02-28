package com.savor.ads.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/20.
 */

public class SetBoxTopResult implements Serializable{

    private int code;
    private String msg;
    private SetTopBoxBean result;

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

    public SetTopBoxBean getResult() {
        return result;
    }

    public void setResult(SetTopBoxBean result) {
        this.result = result;
    }
}

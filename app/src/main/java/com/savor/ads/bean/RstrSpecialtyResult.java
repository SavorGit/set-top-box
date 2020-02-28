package com.savor.ads.bean;

/**
 * Created by zhang.haiqiang on 2017/12/5.
 */

public class RstrSpecialtyResult {
    private int code;
    private String msg;
    private RstrSpecialtyOuterBean result;

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

    public RstrSpecialtyOuterBean getResult() {
        return result;
    }

    public void setResult(RstrSpecialtyOuterBean result) {
        this.result = result;
    }
}

package com.savor.ads.bean;

/**
 * Created by zhanghq on 2017/2/8.
 */

public class BoxInitResult {
    private int code;
    private String msg;
    private BoxInitBean result;

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

    public BoxInitBean getResult() {
        return result;
    }

    public void setResult(BoxInitBean result) {
        this.result = result;
    }
}

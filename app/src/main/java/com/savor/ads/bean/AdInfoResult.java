package com.savor.ads.bean;

/**
 * Created by gaowen on 2017/10/19.
 */

public class AdInfoResult {

    private int code;
    private String message;

    private AdInfo data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AdInfo getData() {
        return data;
    }

    public void setData(AdInfo data) {
        this.data = data;
    }
}

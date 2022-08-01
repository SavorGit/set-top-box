package com.savor.ads.bean;

import java.util.List;

/**
 * Created by zhanghq on 2017/2/8.
 */

public class SysVolumeResult {
    private int code;
    private String msg;
    private SysVolumeList result;

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

    public SysVolumeList getResult() {
        return result;
    }

    public void setResult(SysVolumeList result) {
        this.result = result;
    }
}

package com.savor.ads.bean;

import java.io.Serializable;

/**
 * 节目单
 * Created by bichao on 2017/10/12.
 */

public class ProgramBeanResult implements Serializable{

    private int code;
    private String msg;
    private ProgramBean result;

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

    public ProgramBean getResult() {
        return result;
    }

    public void setResult(ProgramBean result) {
        this.result = result;
    }
}

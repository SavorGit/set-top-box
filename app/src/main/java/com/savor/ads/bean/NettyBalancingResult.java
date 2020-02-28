package com.savor.ads.bean;

import java.io.Serializable;

public class NettyBalancingResult implements Serializable{

    private String result;
    private String req_id;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getReq_id() {
        return req_id;
    }

    public void setReq_id(String req_id) {
        this.req_id = req_id;
    }
}

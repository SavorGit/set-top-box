package com.savor.ads.bean;

import java.io.Serializable;

/**
 * Created by zhang.haiqiang on 2018/1/10.
 */

public class PushRTBItem implements Serializable {
    private String id;
    private long remain_time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getRemain_time() {
        return remain_time;
    }

    public void setRemain_time(long remain_time) {
        this.remain_time = remain_time;
    }
}

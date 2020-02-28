package com.savor.ads.bean;

import java.io.Serializable;

/**
 * Created by zhang.haiqiang on 2017/6/12.
 */

public class PptVideo implements Serializable {
    private String name;
    private int exist;
    private long length;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExist() {
        return exist;
    }

    public void setExist(int exist) {
        this.exist = exist;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}

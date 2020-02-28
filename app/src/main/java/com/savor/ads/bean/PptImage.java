package com.savor.ads.bean;

import java.io.Serializable;

/**
 * Created by zhang.haiqiang on 2017/6/12.
 */

public class PptImage implements Serializable {
    private String name;
//    private int index;
    private int exist;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public int getIndex() {
//        return index;
//    }
//
//    public void setIndex(int index) {
//        this.index = index;
//    }

    public int getExist() {
        return exist;
    }

    public void setExist(int exist) {
        this.exist = exist;
    }
}

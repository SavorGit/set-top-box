package com.jar.savor.box.vo;

import com.savor.ads.bean.PptImage;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zhanghq on 2017/3/14.
 */

public class PptRequestVo implements Serializable{
    private String name;
    private int duration;
    private int interval;
    private ArrayList<PptImage> images;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public ArrayList<PptImage> getImages() {
        return images;
    }

    public void setImages(ArrayList<PptImage> images) {
        this.images = images;
    }
}

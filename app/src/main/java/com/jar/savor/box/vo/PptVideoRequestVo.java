package com.jar.savor.box.vo;

import com.savor.ads.bean.PptVideo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zhanghq on 2017/3/14.
 */

public class PptVideoRequestVo implements Serializable{
    private String name;
    private int duration;
    private ArrayList<PptVideo> videos;

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

    public ArrayList<PptVideo> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<PptVideo> videos) {
        this.videos = videos;
    }
}

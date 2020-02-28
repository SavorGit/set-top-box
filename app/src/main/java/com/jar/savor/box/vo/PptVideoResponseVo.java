package com.jar.savor.box.vo;

import com.savor.ads.bean.PptVideo;

import java.util.ArrayList;

/**
 * Created by zhang.haiqiang on 2017/6/12.
 */

public class PptVideoResponseVo extends BaseResponse {
    private ArrayList<PptVideo> videos;

    public ArrayList<PptVideo> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<PptVideo> videos) {
        this.videos = videos;
    }
}

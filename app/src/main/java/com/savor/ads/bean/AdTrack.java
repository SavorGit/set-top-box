package com.savor.ads.bean;

import java.util.List;

/**
 * Created by gaowen on 2017/11/3.
 */

public class AdTrack {

    private int type;
    private List<String> trackList;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<String> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<String> trackList) {
        this.trackList = trackList;
    }
}

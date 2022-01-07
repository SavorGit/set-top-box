package com.savor.ads.bean;

import java.io.Serializable;
import java.util.List;

public class MeetingLoopPlayBean implements Serializable {

    private Integer[] resource_ids;
    private List<String> videoPaths;
    private MeetingWelcomeBean welcome;

    public Integer[] getResource_ids() {
        return resource_ids;
    }

    public void setResource_ids(Integer[] resource_ids) {
        this.resource_ids = resource_ids;
    }

    public List<String> getVideoPaths() {
        return videoPaths;
    }

    public void setVideoPaths(List<String> videoPaths) {
        this.videoPaths = videoPaths;
    }

    public MeetingWelcomeBean getWelcome() {
        return welcome;
    }

    public void setWelcome(MeetingWelcomeBean welcome) {
        this.welcome = welcome;
    }
}

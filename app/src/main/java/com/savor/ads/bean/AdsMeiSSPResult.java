package com.savor.ads.bean;

public class AdsMeiSSPResult {
    //曝光回调地址
    private String[] impression;
    private AdsMeiSSPBean image;
    private AdsMeiSSPBean video;

    public String[] getImpression() {
        return impression;
    }

    public void setImpression(String[] impression) {
        this.impression = impression;
    }

    public AdsMeiSSPBean getImage() {
        return image;
    }

    public void setImage(AdsMeiSSPBean image) {
        this.image = image;
    }

    public AdsMeiSSPBean getVideo() {
        return video;
    }

    public void setVideo(AdsMeiSSPBean video) {
        this.video = video;
    }
}

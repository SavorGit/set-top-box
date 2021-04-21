package com.savor.ads.bean;

import java.io.Serializable;

public class BigImgBean implements Serializable {

    private String filenameId;
    private String thumbnailPath;
    private String bigPath;

    public String getFilenameId() {
        return filenameId;
    }

    public void setFilenameId(String filenameId) {
        this.filenameId = filenameId;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getBigPath() {
        return bigPath;
    }

    public void setBigPath(String bigPath) {
        this.bigPath = bigPath;
    }
}

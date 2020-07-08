package com.savor.ads.bean;

import java.io.File;

public class MediaFileBean {

    private String url;
    private File cacheFile;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }
}

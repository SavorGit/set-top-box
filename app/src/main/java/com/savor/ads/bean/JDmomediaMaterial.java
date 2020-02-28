package com.savor.ads.bean;

public class JDmomediaMaterial {
    //素材名称
    private String title;
    //素材类型，image图片;video视频
    private String type;
    //素材宽度
    private int width;
    //素材高度
    private int height;
    //素材的下载地址
    private String url;
    //素材单次播放时长,单位秒
    private int show_time;
    //素材MD5信息
    private String md5;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getShow_time() {
        return show_time;
    }

    public void setShow_time(int show_time) {
        this.show_time = show_time;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}

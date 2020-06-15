package com.savor.ads.bean;

public class ProjectionImg {

    /**投屏图片url**/
    private String url;
    /**投屏图片名称**/
    private String filename;
    /**投屏图片序列**/
    private int order;
    /**投图片ID*/
    private String img_id;
    /**投视频ID*/
    private String video_id;
    /**文件大小*/
    private long resource_size;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getImg_id() {
        return img_id;
    }

    public void setImg_id(String img_id) {
        this.img_id = img_id;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public long getResource_size() {
        return resource_size;
    }

    public void setResource_size(long resource_size) {
        this.resource_size = resource_size;
    }
}

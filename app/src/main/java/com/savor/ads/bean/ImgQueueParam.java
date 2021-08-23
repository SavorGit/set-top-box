package com.savor.ads.bean;

public class ImgQueueParam {
    private String action;
    private String forscreen_id;
    private String index;
    private String fileName;
    private String position;
    private String resource_type;
    private String totalSize;
    private String chunkSize;
    private String totalChunks;
    private byte[] inputContent;
    private String serial_number;
    //0:原图，1:缩略图
    private String thumbnail;
    private String filePath;
    //本次投屏总共投了几张图
    private String forscreen_nums;
    private String size;
    private String deviceId;
    private int is_share;
    private String public_text;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getForscreen_id() {
        return forscreen_id;
    }

    public void setForscreen_id(String forscreen_id) {
        this.forscreen_id = forscreen_id;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public String getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(String chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(String totalChunks) {
        this.totalChunks = totalChunks;
    }

    public byte[] getInputContent() {
        return inputContent;
    }

    public void setInputContent(byte[] inputContent) {
        this.inputContent = inputContent;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getForscreen_nums() {
        return forscreen_nums;
    }

    public void setForscreen_nums(String forscreen_nums) {
        this.forscreen_nums = forscreen_nums;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getIs_share() {
        return is_share;
    }

    public void setIs_share(int is_share) {
        this.is_share = is_share;
    }

    public String getPublic_text() {
        return public_text;
    }

    public void setPublic_text(String public_text) {
        this.public_text = public_text;
    }
}

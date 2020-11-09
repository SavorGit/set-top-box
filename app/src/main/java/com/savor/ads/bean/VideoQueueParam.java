package com.savor.ads.bean;

public class VideoQueueParam {

    private String forscreen_id;
    private String index;
    private String fileName;
    private String position;
    private String totalSize;
    private String chunkSize;
    private String totalChunks;
    private byte[] inputContent;

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
}

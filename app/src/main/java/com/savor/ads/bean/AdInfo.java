package com.savor.ads.bean;


import java.io.Serializable;
import java.util.List;

/**
 * 广告文件实体类.
 */

public class AdInfo implements Serializable {
    private String requestId;
    private long positionId;
    private int channelId;
    private long planId;
    private String fileName;
    private String matUrl;
    private int matWidth;
    private int matHeight;
    private int matType;
    private String matMd5;
    private int duration;
    private long expTime;
    private List<String> winNoticeUrlList;
    private List<AdTrack> adTrackList;
    private String clickUrl;
    private int errorCode;
    private int adFrom;
    private String filePath;

    /**
     * 广告请求标识
     * @return
     */
    public String getRequestId() {
        return requestId;
    }

    void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 媒体位标识
     * @return
     */
    public long getPositionId() {
        return positionId;
    }

    void setPositionId(long positionId) {
        this.positionId = positionId;
    }

    /**
     * 渠道id
     * @return
     */
    public int getChannelId() {
        return channelId;
    }

    void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    /**
     * rtb广告标识
     * @return
     */
    public long getPlanId() {
        return planId;
    }

    void setPlanId(long planId) {
        this.planId = planId;
    }

    /**
     * 广告素材的网络地址
     * @return
     */
    public String getMatUrl() {
        return matUrl;
    }

    void setMatUrl(String matUrl) {
        this.matUrl = matUrl;
    }

    /**
     * 广告素材的宽， 单位px
     * @return
     */
    public int getMatWidth() {
        return matWidth;
    }

    void setMatWidth(int matWidth) {
        this.matWidth = matWidth;
    }
    /**
     * 广告素材的高， 单位px
     * @return
     */
    public int getMatHeight() {
        return matHeight;
    }

    void setMatHeight(int matHeight) {
        this.matHeight = matHeight;
    }

    /**
     * 广告素材的类型
     * @return
     */
    public int getMatType() {
        return matType;
    }

    public void setMatType(int matType) {
        this.matType = matType;
    }

    /**
     * 广告素材的MD5值
     * @return
     */
    public String getMatMd5() {
        return matMd5;
    }

    void setMatMd5(String matMd5) {
        this.matMd5 = matMd5;
    }

    /**
     * 素材播放时长，单位秒
     * @return
     */
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * 素材过期时间，单位秒
     * @return
     */
    public long getExpTime() {
        return expTime;
    }

    void setExpTime(long expTime) {
        this.expTime = expTime;
    }

    public List<String> getWinNoticeUrlList() {
        return winNoticeUrlList;
    }

    public void setWinNoticeUrlList(List<String> winNoticeUrlList) {
        this.winNoticeUrlList = winNoticeUrlList;
    }

    public List<AdTrack> getAdTrackList() {
        return adTrackList;
    }

    public void setAdTrackList(List<AdTrack> adTrackList) {
        this.adTrackList = adTrackList;
    }

    /**
     * 点击跳转url
     * @return
     */
    public String getClickUrl() {
        return clickUrl;
    }

    void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }

    /**
     * 广告效果监播url
     * @return
     */
    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     *素材文件名
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    int getAdFrom() {
        return adFrom;
    }

    void setAdFrom(int adFrom) {
        this.adFrom = adFrom;
    }

    /**
     * 获取文件本地路径
     * @return
     */
    public String getFilePath() {
        return filePath;
    }

    void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

package com.savor.ads.bean;

import com.google.protobuf.ByteString;

import java.util.List;

public class BaiduAdLocalBean extends MediaLibBean {
    private ByteString mediaRemotePath;
    private List<ByteString> winNoticeUrlList;
    private List<ByteString> thirdMonitorUrlList;
    private int expireTime;
    private String matMd5;

    public ByteString getMediaRemotePath() {
        return mediaRemotePath;
    }

    public void setMediaRemotePath(ByteString mediaRemotePath) {
        this.mediaRemotePath = mediaRemotePath;
    }

    public List<ByteString> getWinNoticeUrlList() {
        return winNoticeUrlList;
    }

    public void setWinNoticeUrlList(List<ByteString> winNoticeUrlList) {
        this.winNoticeUrlList = winNoticeUrlList;
    }

    public List<ByteString> getThirdMonitorUrlList() {
        return thirdMonitorUrlList;
    }

    public void setThirdMonitorUrlList(List<ByteString> thirdMonitorUrlList) {
        this.thirdMonitorUrlList = thirdMonitorUrlList;
    }

    public String getMatMd5() {
        return matMd5;
    }

    public void setMatMd5(String matMd5) {
        this.matMd5 = matMd5;
    }

    public BaiduAdLocalBean(MediaLibBean bean) {
        setVid(bean.getVid());
        setMediaPath(bean.getMediaPath());
        setMd5(bean.getMd5());
        setAdmaster_sin(bean.getAdmaster_sin());
        setArea_id(bean.getArea_id());
        setChinese_name(bean.getChinese_name());
        setDownload_state(bean.getDownload_state());
        setDuration(bean.getDuration());
        setEnd_date(bean.getEnd_date());
        setId(bean.getId());
        setLocation_id(bean.getLocation_id());
        setName(bean.getName());
        setOrder(bean.getOrder());
        setPeriod(bean.getPeriod());
        setStart_date(bean.getStart_date());
        setSuffix(bean.getSuffix());
        setTaskId(bean.getTaskId());
        setTp_md5(bean.getTp_md5());
        setTpmedia_id(bean.getTpmedia_id());
        setType(bean.getType());
        setUrl(bean.getUrl());
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }
}

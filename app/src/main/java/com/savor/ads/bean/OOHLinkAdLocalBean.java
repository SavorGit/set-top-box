package com.savor.ads.bean;

import java.util.List;

public class OOHLinkAdLocalBean extends MediaLibBean {

    private List<String> winNoticeUrlList;
    private List<AdTrack> adTrackList;
    private long expireTime;

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

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public OOHLinkAdLocalBean(MediaLibBean bean) {
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

}

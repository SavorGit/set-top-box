package com.savor.ads.bean;

import java.util.List;

import tianshu.ui.api.ZmtAPI;

public class ZmengAdLocalBean extends MediaLibBean {
    private List<String> winNoticeUrlList;
    private List<ZmtAPI.AdTracking> AdTrackingList;
    private int expireTime;

    public List<String> getWinNoticeUrlList() {
        return winNoticeUrlList;
    }

    public void setWinNoticeUrlList(List<String> winNoticeUrlList) {
        this.winNoticeUrlList = winNoticeUrlList;
    }

    public List<ZmtAPI.AdTracking> getAdTrackingList() {
        return AdTrackingList;
    }

    public void setAdTrackingList(List<ZmtAPI.AdTracking> adTrackingList) {
        AdTrackingList = adTrackingList;
    }

    public ZmengAdLocalBean(MediaLibBean bean) {
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

package com.savor.ads.bean;

import java.util.List;

public class JDmomediaLocalBean extends MediaLibBean{

    private List<List<String>> ad_tracking;

    public List<List<String>> getAd_tracking() {
        return ad_tracking;
    }

    public void setAd_tracking(List<List<String>> ad_tracking) {
        this.ad_tracking = ad_tracking;
    }

    public JDmomediaLocalBean(MediaLibBean bean) {
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

package com.savor.ads.bean;


public class MeiAdLocalBean extends MediaLibBean {

    private String impression;

    public String getImpression() {
        return impression;
    }

    public void setImpression(String impression) {
        this.impression = impression;
    }

    public MeiAdLocalBean(MediaLibBean bean) {
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

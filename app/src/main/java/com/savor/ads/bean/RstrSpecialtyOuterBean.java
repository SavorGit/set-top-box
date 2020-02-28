package com.savor.ads.bean;

import java.util.List;

/**
 * Created by zhang.haiqiang on 2017/12/5.
 */

public class RstrSpecialtyOuterBean {
    /**期号信息*/
    private VersionInfo version;
    /**媒体库*/
    private List<RstrSpecialty> media_lib;

    public VersionInfo getVersion() {
        return version;
    }

    public void setVersion(VersionInfo version) {
        this.version = version;
    }

    public List<RstrSpecialty> getMedia_lib() {
        return media_lib;
    }

    public void setMedia_lib(List<RstrSpecialty> media_lib) {
        this.media_lib = media_lib;
    }
}

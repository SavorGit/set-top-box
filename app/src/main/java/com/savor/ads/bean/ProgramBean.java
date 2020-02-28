package com.savor.ads.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 节目单对象
 * Created by bichao on 2017/10/12.
 */

public class ProgramBean implements Serializable{

    private VersionInfo version;
    private List<MediaLibBean> media_lib;
    /** 节目单号*/
    private String menu_num;

    public VersionInfo getVersion() {
        return version;
    }

    public void setVersion(VersionInfo version) {
        this.version = version;
    }

    public List<MediaLibBean> getMedia_lib() {
        return media_lib;
    }

    public void setMedia_lib(List<MediaLibBean> media_lib) {
        this.media_lib = media_lib;
    }

    public String getMenu_num() {
        return menu_num;
    }

    public void setMenu_num(String menu_num) {
        this.menu_num = menu_num;
    }
}

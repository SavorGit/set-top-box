package com.savor.ads.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by bichao on 2016/12/12.
 */

public class PlaylistDetailRequestBean implements Serializable {
    private String menu_num;
    private ArrayList<MediaPlaylistBean> list;

    public String getMenu_num() {
        return menu_num;
    }

    public void setMenu_num(String menu_num) {
        this.menu_num = menu_num;
    }

    public ArrayList<MediaPlaylistBean> getList() {
        return list;
    }

    public void setList(ArrayList<MediaPlaylistBean> list) {
        this.list = list;
    }
}


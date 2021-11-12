package com.savor.ads.bean;

import java.io.Serializable;

public class PartakeUser implements Serializable {

    private String openid;
    private String avatarUrl;
    private String nickName;
    /**
     * 是否中奖，1：中奖，0：未中奖
     * */
    private int is_lottery;

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getIs_lottery() {
        return is_lottery;
    }

    public void setIs_lottery(int is_lottery) {
        this.is_lottery = is_lottery;
    }
}

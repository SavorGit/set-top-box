package com.savor.ads.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 霸王菜抽奖
 */
public class PartakeDishBean implements Serializable{

    private List<PartakeUser> partake_user;
    private PartakeLottery lottery;

    public List<PartakeUser> getPartake_user() {
        return partake_user;
    }

    public void setPartake_user(List<PartakeUser> partake_user) {
        this.partake_user = partake_user;
    }

    public PartakeLottery getLottery() {
        return lottery;
    }

    public void setLottery(PartakeLottery lottery) {
        this.lottery = lottery;
    }
}

package com.savor.ads.bean;

import java.io.Serializable;
import java.util.List;

public class LotteryResult implements Serializable {

    private List<PartakeUser> partake_user;
    private List<PartakeLottery> lottery;

    public List<PartakeUser> getPartake_user() {
        return partake_user;
    }

    public void setPartake_user(List<PartakeUser> partake_user) {
        this.partake_user = partake_user;
    }

    public List<PartakeLottery> getLottery() {
        return lottery;
    }

    public void setLottery(List<PartakeLottery> lottery) {
        this.lottery = lottery;
    }
}

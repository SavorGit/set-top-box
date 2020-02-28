package com.jar.savor.box.vo;

/**
 * Created by zhanghq on 2016/12/22.
 */
public class VolumeResponseVo extends BaseResponse {
    private static final long serialVersionUID = -256648149146196066L;
    private int vol;

    public VolumeResponseVo() {
    }

    public int getVol() {
        return this.vol;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }
}

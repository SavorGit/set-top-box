package com.jar.savor.box.vo;

/**
 * Created by zhanghq on 2016/12/22.
 */
public class RotateResponseVo extends BaseResponse {
    private static final long serialVersionUID = -8167549459162001700L;
    private int rotatevalue;

    public RotateResponseVo() {
    }

    public int getRotateValue() {
        return this.rotatevalue;
    }

    public void setRotateValue(int rotateValue) {
        this.rotatevalue = rotateValue;
    }
}
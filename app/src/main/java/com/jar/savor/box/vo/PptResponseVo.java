package com.jar.savor.box.vo;

import com.savor.ads.bean.PptImage;

import java.util.ArrayList;

/**
 * Created by zhang.haiqiang on 2017/6/12.
 */

public class PptResponseVo extends BaseResponse {
    private ArrayList<PptImage> images;

    public ArrayList<PptImage> getImages() {
        return images;
    }

    public void setImages(ArrayList<PptImage> images) {
        this.images = images;
    }
}

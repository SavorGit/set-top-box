package com.jar.savor.box.vo;

/**
 * Created by zhang.haiqiang on 2018/2/2.
 */

public class SpecialtyResponseBean {
    /** 找到的特色菜数量*/
    private int founded_count;
    /** 未找到的特色菜ID*/
    private String failed_ids;

    public int getFounded_count() {
        return founded_count;
    }

    public void setFounded_count(int founded_count) {
        this.founded_count = founded_count;
    }

    public String getFailed_ids() {
        return failed_ids;
    }

    public void setFailed_ids(String failed_ids) {
        this.failed_ids = failed_ids;
    }
}

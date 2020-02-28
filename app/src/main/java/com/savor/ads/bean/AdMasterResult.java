package com.savor.ads.bean;

/**
 * Created by Administrator on 2018/1/10.
 */

public class AdMasterResult{

    private int update_time;
    private String file;
    private String md5;

    public int getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(int update_time) {
        this.update_time = update_time;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}

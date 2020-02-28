package com.jar.savor.box.vo;

/**
 * Created by zhanghq on 2017/3/24.
 */

public class ResponseT1<T> {
    private T content;
    private String info;
    private int result;

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}

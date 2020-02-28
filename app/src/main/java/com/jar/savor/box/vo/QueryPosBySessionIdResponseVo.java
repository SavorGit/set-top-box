package com.jar.savor.box.vo;

import java.io.Serializable;

/**
 * Created by zhanghq on 2016/12/22.
 */

public class QueryPosBySessionIdResponseVo implements Serializable {
    private static final long serialVersionUID = 6922151567069094539L;
    private int result;
    private int pos;

    public QueryPosBySessionIdResponseVo() {
    }

    public int getResult() {
        return this.result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getPos() {
        return this.pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}

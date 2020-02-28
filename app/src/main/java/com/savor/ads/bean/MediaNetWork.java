package com.savor.ads.bean;

/**
 * Created by gaowen on 2017/10/18.
 */

public class MediaNetWork {

    private String ipv4;
    private int connectionType;
    private int operatorType;
    private String cellularId;

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    public int getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(int operatorType) {
        this.operatorType = operatorType;
    }

    public String getCellularId() {
        return cellularId;
    }

    public void setCellularId(String cellularId) {
        this.cellularId = cellularId;
    }
}

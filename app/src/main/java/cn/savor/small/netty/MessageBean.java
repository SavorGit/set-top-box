package cn.savor.small.netty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuoDuo on 2016/12/10.
 */
public class MessageBean implements Serializable {

    public static enum Action {
//        /**客户端心跳包请求*/
//        CLIENT_HEART_REQ,
//        /**服务端心跳包回应*/
//        SERVER_HEART_RESP,
        /**服务端数据包请求*/
        SERVER_ORDER_REQ,
        /**客户端数据包回应*/
        CLIENT_ORDER_RESP,
//        /**客户端数据包请求*/
//        CLIENT_ORDER_REQ,
//        /**服务端数据包回应*/
//        SERVER_ORDER_RESP
        /**服务端心跳包请求*/
        HEART_SERVER_TO_CLIENT,
        /**客户端心跳包回应*/
        HEART_CLENT_TO_SERVER,
        HEART_CLIENT_FIRST_REGISTER
    }

    private Action cmd = null;
    private List<String> content = null;
    private String serialnumber = null;
    private String ip = null;
    private String mac = null;

    public List<String> getContent() {
        if (this.content == null)
            this.content = new ArrayList<String>();
        return this.content;
    }

    public String getSerialnumber() {
        return serialnumber;
    }

    public void setSerialnumber(String serialnumber) {
        this.serialnumber = serialnumber;
    }

    public Action getCmd() {
        return cmd;
    }

    public void setCmd(Action cmd) {
        this.cmd = cmd;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}

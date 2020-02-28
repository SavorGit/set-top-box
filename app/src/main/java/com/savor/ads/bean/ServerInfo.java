package com.savor.ads.bean;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.Serializable;

/**
 * 小平台交互所需信息
 * Created by zhanghq on 2016/12/8.
 */

public class ServerInfo implements Serializable {
    /**
     * 小平台IP
     */
    @SerializedName("ip")
    String serverIp;
    /**
     * 小平台Netty服务IP
     */
    @SerializedName("netty_port")
    int nettyPort;
    /**
     * 小平台命令IP
     */
    @SerializedName("command_port")
    int commandPort;
    /**
     * 小平台下载服务IP
     */
    @SerializedName("download_port")
    int downloadPort;

    /**
     * 设置来源
     * 1：ssdp；2：http；3：手动设置
     */
    int source;

    public ServerInfo(String serverIp, int nettyPort, int httpPort, int downloadPort, int source) {
        this.serverIp = serverIp;
        this.nettyPort = nettyPort > 0 ? nettyPort : 8009;
        this.commandPort = httpPort > 0 ? httpPort : 8080;
        this.downloadPort = downloadPort > 0 ? downloadPort : 8080;
        this.source = source;
    }

    public ServerInfo(String serverIp, int source) {
        this(serverIp, -1, -1, -1, source);
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getNettyPort() {
        return nettyPort;
    }

    public void setNettyPort(int nettyPort) {
        this.nettyPort = nettyPort;
    }

    public int getCommandPort() {
        return commandPort;
    }

    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
    }

    public int getDownloadPort() {
        return downloadPort;
    }

    public void setDownloadPort(int downloadPort) {
        this.downloadPort = downloadPort;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getDownloadUrl(){
        String url = "http://"+ serverIp+":"+downloadPort+ File.separator;
        return  url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerInfo that = (ServerInfo) o;

        if (nettyPort != that.nettyPort) return false;
        if (commandPort != that.commandPort) return false;
        if (downloadPort != that.downloadPort) return false;
        if (source != that.source) return false;
        return serverIp != null ? serverIp.equals(that.serverIp) : that.serverIp == null;

    }

    @Override
    public int hashCode() {
        int result = serverIp != null ? serverIp.hashCode() : 0;
        result = 31 * result + nettyPort;
        result = 31 * result + commandPort;
        result = 31 * result + downloadPort;
        result = 31 * result + source;
        return result;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "serverIp='" + serverIp + '\'' +
                ", nettyPort=" + nettyPort +
                ", commandPort=" + commandPort +
                ", downloadPort=" + downloadPort +
                ", source=" + source +
                '}';
    }
}

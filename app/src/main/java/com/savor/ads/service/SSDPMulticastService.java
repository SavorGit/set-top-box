package com.savor.ads.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.savor.ads.bean.ServerInfo;
import com.savor.ads.core.Session;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.IPAddressUtils;
import com.savor.ads.utils.LogUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


/**
 * SSDP发送Service
 */
@Deprecated
public class SSDPMulticastService extends IntentService {
    /**
     * SSDP发送周期
     */
    private static final int MULTICAST_DURATION = 1000 * 3;

    private static final int PORT = 11900;
    private static final String IP_TARGET = "238.255.255.250";

    /** 组播类型，box*/
    private static final String TYPE_LABEL_PREFIX = "Savor-Type:";
    /** 机顶盒IP*/
    private static final String BOX_IP_LABEL_PREFIX = "Savor-Box-HOST:";
    /** 包间ID*/
    private static final String ROOM_ID_PREFIX = "Savor-Room-ID:";
    /** MAC地址*/
    private static final String BOX_MAC_PREFIX = "Savor-Box-MAC:";
    /** 小平台IP*/
    private static final String IP_LABEL_PREFIX = "Savor-HOST:";
    /** 以下为小平台端口信息*/
    private static final String NETTY_PORT_LABEL_PREFIX = "Savor-Port-Netty:";
    private static final String COMMAND_PORT_LABEL_PREFIX = "Savor-Port-Command:";
    private static final String DOWNLOAD_PORT_LABEL_PREFIX = "Savor-Port-Download:";
    /** 酒楼ID前缀*/
    private static final String HOTEL_ID_PREFIX = "Savor-Hotel-ID:";
    private static final String CRLF = "\r\n";

    private MulticastSocket mSocketSend;

    public SSDPMulticastService() {
        super("SSDPMulticastService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while (true) {
            LogUtils.v("开始发送一次SSDP");
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiManager.MulticastLock multicastLock = wm.createMulticastLock("sendLock");
            multicastLock.setReferenceCounted(false);
            multicastLock.acquire();

            try {
                mSocketSend = new MulticastSocket();
//                mSocketSend.setLoopbackMode(true);
//                mSocketSend.setTimeToLive(0);
                mSocketSend.joinGroup(InetAddress.getByName(IP_TARGET));

                // 拼接message
                String msg = TYPE_LABEL_PREFIX + ConstantValues.SSDP_CONTENT_TYPE + CRLF +
                        BOX_IP_LABEL_PREFIX + IPAddressUtils.getLocalIPAddress() + CRLF +
                        ROOM_ID_PREFIX + Session.get(this).getRoomId() + CRLF +
                        BOX_MAC_PREFIX + Session.get(this).getEthernetMac() + CRLF +
                        HOTEL_ID_PREFIX + Session.get(this).getBoiteId() + CRLF;
                ServerInfo serverInfo = Session.get(this).getServerInfo();
                if (serverInfo != null) {
                    msg += IP_LABEL_PREFIX + serverInfo.getServerIp() + CRLF +
                            NETTY_PORT_LABEL_PREFIX + serverInfo.getNettyPort() + CRLF +
                            COMMAND_PORT_LABEL_PREFIX + serverInfo.getCommandPort() + CRLF +
                            DOWNLOAD_PORT_LABEL_PREFIX + serverInfo.getDownloadPort() + CRLF;
                }

                DatagramPacket packetSend = new DatagramPacket(msg.getBytes(), msg.length(), InetAddress.getByName(IP_TARGET), PORT);
                mSocketSend.send(packetSend);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e("SSDP发送异常：" + e.getMessage());
            }

            multicastLock.release();
            if (mSocketSend != null && !mSocketSend.isClosed()) {
                mSocketSend.close();
            }
            LogUtils.v("完成发送一次SSDP");

            // 休息
            try {
                Thread.sleep(MULTICAST_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d("onDestroy");

    }
}

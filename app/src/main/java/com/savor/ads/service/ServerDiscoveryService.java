package com.savor.ads.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.savor.ads.bean.ServerInfo;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import cn.savor.small.netty.NettyClient;


/**
 * 组播发现小平台类
 */
@Deprecated
public class ServerDiscoveryService extends Service {

    private static final int PORT_LISTENING = 11900;
    private static final String IP_TARGET = "238.255.255.250";

    private static final int DATA_RECEIVE_SIZE = 1024;


    private static final String TYPE_LABEL_PREFIX = "Savor-Type:";
    private static final String IP_LABEL_PREFIX = "Savor-HOST:";
    private static final String NETTY_PORT_LABEL_PREFIX = "Savor-Port-Netty:";
    private static final String COMMAND_PORT_LABEL_PREFIX = "Savor-Port-Command:";
    private static final String DOWNLOAD_PORT_LABEL_PREFIX = "Savor-Port-Download:";
    private static final String CRLF = "\r\n";


    private MulticastSocket mSocketReceive;

    private boolean mIsExecuting;

    private boolean mIsTimeout;
    private Handler mHandler = new Handler();

    public ServerDiscoveryService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.v("ServerDiscoveryService onStartCommand! mIsExecuting is " + mIsExecuting);
        LogFileUtil.write("ServerDiscoveryService onStartCommand! mIsExecuting is " + mIsExecuting);
        if (!mIsExecuting) {
            mIsExecuting = true;
            mIsTimeout = false;
            startReceive();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startReceive() {
        LogUtils.v("ServerDiscoveryService startReceive");
        LogFileUtil.write("ServerDiscoveryService startReceive");

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsTimeout = true;
                closeSocketReceive();
            }
        }, 1000 * 5);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final WifiManager.MulticastLock multicastLock = wm.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(false);
        multicastLock.acquire();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initSocket();
                    String type = null;
                    String address = null;
                    int nettyPort = -1, commandPort = -1, downloadPort = -1;

                    do {
                        DatagramPacket packetReceived = new DatagramPacket(new byte[DATA_RECEIVE_SIZE], DATA_RECEIVE_SIZE);
                        try {
                            LogUtils.v("ServerDiscoveryService waiting for multicast msg");
//                            LogFileUtil.write("ServerDiscoveryService waiting for multicast msg");
                            mSocketReceive.receive(packetReceived);

                            final String msgReceived = new String(packetReceived.getData(), 0, packetReceived.getLength()).trim();
                            LogUtils.v("收到的消息是：" + msgReceived + "\ngetHostAddress:" + packetReceived.getAddress().getHostAddress());
//                            LogFileUtil.write("收到的消息是：" + msgReceived + "\ngetHostAddress:" + packetReceived.getAddress().getHostAddress());

                            if (msgReceived.length() > 0) {
                                type = parseStringMetadata(msgReceived, TYPE_LABEL_PREFIX);
                                // 忽略自己发出的组播
                                if (ConstantValues.SSDP_CONTENT_TYPE.equals(type)) {
                                    continue;
                                }

                                // 解析并保存小平台信息到Session
                                address = parseStringMetadata(msgReceived, IP_LABEL_PREFIX);
                                nettyPort = parseIntMetadata(msgReceived, NETTY_PORT_LABEL_PREFIX);
                                commandPort = parseIntMetadata(msgReceived, COMMAND_PORT_LABEL_PREFIX);
                                downloadPort = parseIntMetadata(msgReceived, DOWNLOAD_PORT_LABEL_PREFIX);
                                LogUtils.v("type：" + type + " address:" + address + " nettyPort:" + nettyPort +
                                        " commandPort:" + commandPort + " downloadPort:" + downloadPort);
                            }

                            if (!TextUtils.isEmpty(address) && nettyPort > 0 && commandPort > 0 && downloadPort > 0) {
                                ServerInfo serverInfo = new ServerInfo(address, nettyPort, commandPort, downloadPort, 1);

                                handleServerInfo(serverInfo);

                                break;
                            }
                        } catch (IOException ex) {
                            LogUtils.e("组播监听异常");
                            ex.printStackTrace();
                            String title = (ex.getLocalizedMessage() != null) ? (ex.getClass().getName() + ": " + ex.getLocalizedMessage()) : ex.getClass().getName();
                            StringBuilder sb = new StringBuilder("ServerDiscoveryService 组播监听异常: " + title + "\r\n");
                            for (StackTraceElement traceElement : ex.getStackTrace())
                                sb.append("\tat " + traceElement + "\r\n");
//                            LogFileUtil.write(sb.toString());
                        }

                        try {
                            Thread.sleep(1000 * 3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (!mIsTimeout);


                } catch (IOException ex) {
                    ex.printStackTrace();
                    LogUtils.e("加入组播异常");
                    String title = (ex.getLocalizedMessage() != null) ? (ex.getClass().getName() + ": " + ex.getLocalizedMessage()) : ex.getClass().getName();
                    StringBuilder sb = new StringBuilder("ServerDiscoveryService 启动接收组播异常: " + title + "\r\n");
                    for (StackTraceElement traceElement : ex.getStackTrace())
                        sb.append("\tat " + traceElement + "\r\n");
//                    LogFileUtil.write(sb.toString());
                } finally {
                    closeSocketReceive();

                    multicastLock.release();

                    stopSelf();
                    mIsExecuting = false;
                }
            }
        }).start();
    }

    private void initSocket() throws IOException {
        LogFileUtil.write("ServerDiscoveryService will joinGroup:"+IP_TARGET);
        mSocketReceive = new MulticastSocket(PORT_LISTENING);
        mSocketReceive.setLoopbackMode(true);
        mSocketReceive.setTimeToLive(0);
        mSocketReceive.joinGroup(InetAddress.getByName(IP_TARGET));
    }

    private void handleServerInfo(ServerInfo serverInfo) {
        if (!serverInfo.equals(Session.get(ServerDiscoveryService.this).getServerInfo())) {
            LogUtils.e("SSDP发现小平台信息与原信息不符，将重设小平台相关接口地址、并重连Netty");
            LogFileUtil.write("SSDP发现小平台信息与原信息不符，将重设小平台相关接口地址、并重连Netty");

            Session.get(ServerDiscoveryService.this).setServerInfo(serverInfo);
            AppApi.resetSmallPlatformInterface(ServerDiscoveryService.this);

            // 重设NettyClient ip、端口号
            // NettyClient.get() != null意味着在MainActivity已经初始化Netty并开始连接
//            if (NettyClient.get() != null) {
//                NettyClient.get().setServer(serverInfo.getNettyPort(), serverInfo.getServerIp());
//            } else {
//                Intent intent = new Intent(this, MessageService.class);
//                startService(intent);
//            }
        }
    }

    private void closeSocketReceive() {
        if (mSocketReceive != null && !mSocketReceive.isClosed()) {
            mSocketReceive.close();
        }
    }

    private int parseIntMetadata(String data, String labelPrefix) {
        int metadata = -1;
        if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(labelPrefix)) {
            // Label开始
            int startIndex = data.indexOf(labelPrefix) + labelPrefix.length();
            // Label以换行结束时换行符的位置，endIndex可能是该项为message最末尾
            int endIndex = data.indexOf(CRLF, startIndex);
            if (startIndex >= 0 && (endIndex > startIndex || endIndex == -1)) {
                try {
                    if (endIndex == -1) {
                        metadata = Integer.parseInt(data.substring(startIndex).trim());
                    } else {
                        metadata = Integer.parseInt(data.substring(startIndex, endIndex).trim());
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return metadata;
    }

    private String parseStringMetadata(String data, String labelPrefix) {
        String metadata = null;
        if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(labelPrefix)) {
            // Label开始
            int startIndex = data.indexOf(labelPrefix) + labelPrefix.length();
            // Label以换行结束时换行符的位置，endIndex可能是该项为message最末尾
            int endIndex = data.indexOf(CRLF, startIndex);
            if (startIndex >= 0 && (endIndex > startIndex || endIndex == -1)) {
                metadata = data.substring(startIndex, endIndex).trim();
            }
        }
        return metadata;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.v("ServerDiscoveryService onDestroy!");
    }
}

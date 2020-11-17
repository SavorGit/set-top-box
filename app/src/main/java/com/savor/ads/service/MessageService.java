package com.savor.ads.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;


import com.savor.ads.BuildConfig;
import com.savor.ads.SavorApplication;
import com.savor.ads.bean.ServerInfo;
import com.savor.ads.activity.MainActivity;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;

import cn.savor.small.netty.NettyClient;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

/**
 * 启动Netty服务
 * Created by bichao on 2016/12/08.
 */
@Deprecated
public class MessageService extends IntentService implements NettyClient.NettyMessageCallback {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    private Context context;
    private Session session;
    public MessageService() {
        super("");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        session = Session.get(context);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.d("MessageService onHandleIntent");
        LogFileUtil.write("MessageService onHandleIntent");
        try {
            LogUtils.d("启动NettyService");
            LogFileUtil.write("启动NettyService");
            fetchMessage();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d("MessageService onDestroy");
        LogFileUtil.write("MessageService onDestroy");
    }

    private void fetchMessage() throws InterruptedException {
        try{
            ServerInfo serverInfo =session.getServerInfo();
            LogUtils.d("MessageService fetchMessage");
            if (serverInfo != null) {
                LogUtils.d("MessageService serverInfo != null");
                NettyClient.init(serverInfo.getNettyPort(),serverInfo.getServerIp(), this, /*Session.get(this).getEthernetMac()*/ getApplicationContext());
                NettyClient.get().start();

            } else {
                LogUtils.d("MessageService serverInfo == null");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveServerMessage(String msg, String code) {
        if (ConstantValues.NETTY_SHOW_QRCODE_COMMAND.equals(msg)) {
            LogUtils.d("收到显示二维码指令");
            LogFileUtil.write("收到显示二维码指令");

//            if (!(ActivitiesManager.getInstance().getCurrentActivity() instanceof MainActivity)) {
//                if (getApplication() instanceof SavorApplication) {
//                    ((SavorApplication) getApplication()).showQrCodeWindow(code);
//                }
//            }

        }
    }

    @Override
    public void onConnected() {
        LogUtils.d("Netty连接成功");
        session.setConnectedToSP(true);
    }

    @Override
    public void onReconnect() {
        LogUtils.d("Netty开始重连");
        session.setConnectedToSP(false);

        if (session.getServerInfo() == null || session.getServerInfo().getSource() != 3) {
            // 清空ServerInfo
            session.setServerInfo(null);

            LogUtils.d("启动ServerDiscoveryService发现是否小平台IP已改变");
            // Netty开始重连，启动ServerDiscoveryService发现是否小平台IP已改变
            Intent intent = new Intent(this, ServerDiscoveryService.class);
            startService(intent);

            LogUtils.w("将发HTTP请求去发现小平台信息");
            LogFileUtil.write("MessageService 将发HTTP请求去发现小平台信息");
            AppApi.getSpIp(this, new ApiRequestListener() {
                @Override
                public void onSuccess(AppApi.Action method, Object obj) {
                    LogUtils.w("HTTP接口发现小平台信息");
                    LogFileUtil.write("MessageService HTTP接口发现小平台信息");
                    if (obj instanceof ServerInfo) {
                        handleServerIp((ServerInfo) obj);
                    }
                }

                @Override
                public void onError(AppApi.Action method, Object obj) {
                    LogUtils.w("HTTP接口发现小平台信息失败");
                    LogFileUtil.write("MessageService HTTP接口发现小平台信息失败");
                }

                @Override
                public void onNetworkFailed(AppApi.Action method) {
                    LogUtils.w("HTTP接口发现小平台信息失败");
                    LogFileUtil.write("MessageService HTTP接口发现小平台信息失败");
                }
            });
        }
    }

    private void handleServerIp(ServerInfo serverInfo) {
        if (serverInfo != null && !TextUtils.isEmpty(serverInfo.getServerIp()) && serverInfo.getNettyPort() > 0 && serverInfo.getCommandPort() > 0 && serverInfo.getDownloadPort() > 0 &&
                (Session.get(this).getServerInfo() == null || Session.get(this).getServerInfo().getSource() != 1)) {
            LogUtils.w("将使用HTTP拿到的信息重置小平台信息");
            LogFileUtil.write("MessageService 将使用HTTP拿到的信息重置小平台信息");
            serverInfo.setSource(2);
            if (serverInfo.getServerIp().contains("*")) {
                serverInfo.setServerIp(serverInfo.getServerIp().split("\\*")[0]);
            }
            Session.get(this).setServerInfo(serverInfo);
            AppApi.resetSmallPlatformInterface(this);

            // 重设NettyClient ip、端口号
            // NettyClient.get() != null意味着在MainActivity已经初始化Netty并开始连接
            if (NettyClient.get() != null) {
                NettyClient.get().setServer(serverInfo.getNettyPort(), serverInfo.getServerIp());
            } else {
                Intent intent = new Intent(this, MessageService.class);
                startService(intent);
            }
        }
    }


}

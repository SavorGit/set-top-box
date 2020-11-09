package com.savor.ads.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.savor.ads.core.Session;
import com.savor.ads.service.socket.WebSocketVideoHandler;
import com.savor.ads.service.socket.WriteFileThread;
import com.savor.ads.utils.LogUtils;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;


public class WebSocketService extends Service {

    private Context mContext;
    private Session session;
    private Handler handler = new Handler(Looper.getMainLooper());
    public WebSocketService(){

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        session = Session.get(mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startWebSocket();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startWebSocket(){

//        InetSocketAddress addr=new InetSocketAddress("0.0.0.0",9999);
        Server server = new Server(9999);

        /* webSocket的handler */
        WebSocketVideoHandler test = new WebSocketVideoHandler(mContext);

        ContextHandler context = new ContextHandler();
        /* 路径 */
        context.setContextPath("/android");
        context.setHandler(test);
        server.setHandler(context);
        try {
            /* 启动服务端 */
            server.start();
//            server.join();
            LogUtils.d("WebSocket------启动成功");
        } catch (Exception e) {
            LogUtils.d("WebSocket------启动失败");
            e.printStackTrace();
        }

        new WriteFileThread(mContext).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

/**
 * Copyright (c) 2016, Stupid Bird and/or its affiliates. All rights reserved.
 * STUPID BIRD PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @Project : netty
 * @Package : net.lizhaoweb.netty
 * @author <a href="http://www.lizhaoweb.net">李召(John.Lee)</a>
 * @EMAIL 404644381@qq.com
 * @Time : 13:35
 */
package cn.savor.small.netty;

import android.content.Context;
import android.util.Log;

import com.savor.ads.core.Session;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author bichao
 * @version 1.0.0.0.1
 * @notes Created on 2016年12月08日<br>
 * Revision of last commit:$Revision$<br>
 * Author of last commit:$Author$<br>
 * Date of last commit:$Date$<br>
 */

public class MiniProNettyClient {
    static int PORT;
    static String HOST;
    private MiniNettyMsgCallback callback;
    private Context mContext;
    private static MiniProNettyClient instance;
    private Channel channel;
    private NioEventLoopGroup workGroup = new NioEventLoopGroup(2);
    private Bootstrap bootstrap;
    public static void init(int port, String host, MiniNettyMsgCallback callback, Context context) {
        instance = new MiniProNettyClient(port, host, callback, context);
    }

    /**
     * get前请务必确保init了
     * @return
     */
    public static MiniProNettyClient get() {
        return instance;
    }

    private MiniProNettyClient(int port, String host, MiniNettyMsgCallback c, Context context) {
        this.PORT = port;
        this.HOST = host;
        this.callback = c;
        mContext = context;
    }

    public  void start() {
        try {
            bootstrap = new Bootstrap();
            bootstrap
                    .group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("client SocketChannel.....................................");
                            ch.pipeline().addLast("ping", new IdleStateHandler(60, 60, 80, TimeUnit.SECONDS));
                            //添加POJO对象解码器 禁止缓存类加载器
                            ch.pipeline().addLast(new ObjectDecoder(1024*1024, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
                            //设置发送消息编码器
                            ch.pipeline().addLast(new ObjectEncoder());

                            ch.pipeline().addLast(new MiniProNettyClientHandler(MiniProNettyClient.this,callback,mContext));
                        }
                    });
            connect();

        } catch (Exception e) {
            Log.d("测试netty启动-netty-start异常",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void connect(){
        Log.d("测试netty启动","---connect");
        if (channel != null && channel.isActive()) {
            return;
        }
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,1000*10);
        ChannelFuture future = bootstrap.connect(HOST, PORT);
        LogUtils.d("miniProgram--client connection.................host="+HOST+",port="+PORT+",future="+future.isSuccess());

        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if (futureListener.isSuccess()) {
                    channel = futureListener.channel();
                    System.out.println("Connect to server successfully!");
                    Log.d("测试netty启动","success|channel="+channel);
                } else {
                    System.out.println("Failed to connect to server, try connect after 10s");
                    Log.d("测试netty启动","failed");
                    futureListener.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            connect();
                        }
                    }, 60, TimeUnit.SECONDS);
                }
            }
        });
    }

    public interface MiniNettyMsgCallback {
        void onReceiveMiniServerMsg(String msg, String content);
        void onMiniConnected();
        void onMiniCloseIcon();
    }
}

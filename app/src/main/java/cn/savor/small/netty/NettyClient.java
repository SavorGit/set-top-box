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

import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
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

public class NettyClient {
    static int port;
    static String host;
    private NettyMessageCallback callback;
    private Context mContext;
    private static NettyClient instance;
    private Channel channel;
    private NioEventLoopGroup workGroup = new NioEventLoopGroup(2);
    private Bootstrap bootstrap;
    public static void init(int port, String host, NettyMessageCallback callback, Context context) {
        instance = new NettyClient(port, host, callback, context);
    }

    /**
     * get前请务必确保init了
     * @return
     */
    public static NettyClient get() {
        return instance;
    }

    private NettyClient(int port, String host, NettyMessageCallback c, Context context) {
        this.port = port;
        this.host = host;
        this.callback = c;
        mContext = context;
    }

    public void setServer(int port, String host) {
        this.port = port;
        this.host = host;
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
                            ch.pipeline().addLast(new ObjectDecoder(1024*5, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
                            //设置发送消息编码器
                            ch.pipeline().addLast(new ObjectEncoder());

                            ch.pipeline().addLast(new NettyClientHandler(NettyClient.this,callback,mContext));
                        }
                    });
            connect();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void connect(){
        System.out.println("client connection.................");
        if (channel != null && channel.isActive()) {
            return;
        }

        ChannelFuture future = bootstrap.connect(host, port);
        System.out.println("client connection.................host="+host+",port="+port+",future="+future.isSuccess());

        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if (futureListener.isSuccess()) {
                    channel = futureListener.channel();
                    System.out.println("Connect to server successfully!");
                } else {
                    System.out.println("Failed to connect to server, try connect after 10s");

                    futureListener.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            connect();
                        }
                    }, 1, TimeUnit.SECONDS);
                }
            }
        });
    }


    public interface NettyMessageCallback {
        void onReceiveServerMessage(String msg, String code);
        void onConnected();
        void onReconnect();
    }
}

/**
 * Copyright (c) 2020, Stupid Bird and/or its affiliates. All rights reserved.
 * STUPID BIRD PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @Project : jetty
 * @Package : jetty.websocket
 * @author <a href="http://www.lizhaoweb.net">李召(John.Lee)</a>
 * @EMAIL 404644381@qq.com
 * @Time : 19:29
 */
package com.savor.ads.service.socket;

import android.content.Context;

import com.savor.ads.SavorApplication;
import com.savor.ads.utils.LogUtils;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

/**
 * @author <a href="http://www.lizhaoweb.cn">李召(John.Lee)</a>
 * @version 1.0.0.0.1
 * @EMAIL 404644381@qq.com
 * @notes Created on 2020年10月22日<br>
 * Revision of last commit:$Revision$<br>
 * Author of last commit:$Author$<br>
 * Date of last commit:$Date$<br>
 */
public class WebSocketServerMain {
    public static void main(String args[]) {
        Server server = new Server(9999);
        /* webSocket的handler */
        WebSocketVideoHandler test = new WebSocketVideoHandler();


        ContextHandler context = new ContextHandler();
        /* 路径 */
        context.setContextPath("/android");
        context.setHandler(test);
        server.setHandler(context);
        try {
            /* 启动服务端 */
            server.start();
//            server.join();
//            LogUtils.d("WebSocket------启动成功");
            System.out.println("WebSocket------启动成功");
        } catch (Exception e) {
//            LogUtils.d("WebSocket------启动失败");
            System.out.println("WebSocket------启动失败");
            e.printStackTrace();
        }

    }
}

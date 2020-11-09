/**
 * Copyright (c) 2020, Stupid Bird and/or its affiliates. All rights reserved.
 * STUPID BIRD PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @Project : jetty
 * @Package : jetty.websocket
 * @author <a href="http://www.lizhaoweb.net">李召(John.Lee)</a>
 * @EMAIL 404644381@qq.com
 * @Time : 19:26
 */
package com.savor.ads.service.socket;


import android.content.Context;

import com.jar.savor.box.interfaces.OnRemoteOperationListener;
import com.savor.ads.callback.ProjectOperationListener;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;

/**
 * @author <a href="http://www.lizhaoweb.cn">李召(John.Lee)</a>
 * @version 1.0.0.0.1
 * @EMAIL 404644381@qq.com
 * @notes Created on 2020年10月22日<br>
 * Revision of last commit:$Revision$<br>
 * Author of last commit:$Author$<br>
 * Date of last commit:$Date$<br>
 */
public class MyAdvancedEchoCreator implements org.eclipse.jetty.websocket.servlet.WebSocketCreator {

    AnnotatedEchoSocket annotatedEchoSocket;
    public MyAdvancedEchoCreator() {
        annotatedEchoSocket = new AnnotatedEchoSocket();
    }
    public MyAdvancedEchoCreator(Context context) {
        annotatedEchoSocket = new AnnotatedEchoSocket();
        annotatedEchoSocket.setOnRemoteOpreationListener(ProjectOperationListener.getInstance(context));
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        for (String sub : req.getSubProtocols()) {
            /**
             *   官方的Demo，这里可以根据相应的参数做判断，使用什么样的websocket
             */

        }

        // 没有有效的请求，忽略它
        return annotatedEchoSocket;

    }
}

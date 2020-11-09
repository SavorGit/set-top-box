/**
 * Copyright (c) 2020, Stupid Bird and/or its affiliates. All rights reserved.
 * STUPID BIRD PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @Project : jetty
 * @Package : jetty.websocket
 * @author <a href="http://www.lizhaoweb.net">李召(John.Lee)</a>
 * @EMAIL 404644381@qq.com
 * @Time : 14:53
 */
package com.savor.ads.service.socket;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="http://www.lizhaoweb.cn">李召(John.Lee)</a>
 * @version 1.0.0.0.1
 * @EMAIL 404644381@qq.com
 * @notes Created on 2020年10月27日<br>
 * Revision of last commit:$Revision$<br>
 * Author of last commit:$Author$<br>
 * Date of last commit:$Date$<br>
 */
public class Global {

    public static boolean shutdown = false;
    public static String webAppHome = System.getProperty("user.home") + "/jetty/webapps";
    public static String serverHost = "127.0.0.1";
    public static int serverPort = 7778;
    public static Map<String, FileInfo> fileInfoMap = new ConcurrentHashMap<>();
    public static long startTotalTime = -1;
}

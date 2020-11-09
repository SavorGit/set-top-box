/**
 * Copyright (c) 2020, Stupid Bird and/or its affiliates. All rights reserved.
 * STUPID BIRD PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @Project : jetty
 * @Package : jetty.websocket.server
 * @author <a href="http://www.lizhaoweb.net">李召(John.Lee)</a>
 * @EMAIL 404644381@qq.com
 * @Time : 14:11
 */
package com.savor.ads.service.socket;


import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author <a href="http://www.lizhaoweb.cn">李召(John.Lee)</a>
 * @version 1.0.0.0.1
 * @EMAIL 404644381@qq.com
 * @notes Created on 2020年10月27日<br>
 * Revision of last commit:$Revision$<br>
 * Author of last commit:$Author$<br>
 * Date of last commit:$Date$<br>
 */
public class JSONObjectQueueUtil {

    private static Queue<JSONObject> queue = new LinkedList<>();

    public static boolean push(JSONObject e) {
        return queue.add(e);
    }

    public static synchronized JSONObject pop() {
        return queue.poll();
    }

    public static synchronized int getSize() {
        return queue.size();
    }
}

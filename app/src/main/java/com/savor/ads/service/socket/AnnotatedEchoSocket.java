/**
 * Copyright (c) 2020, Stupid Bird and/or its affiliates. All rights reserved.
 * STUPID BIRD PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @Project : jetty
 * @Package : jetty.websocket
 * @author <a href="http://www.lizhaoweb.net">李召(John.Lee)</a>
 * @EMAIL 404644381@qq.com
 * @Time : 19:27
 */
package com.savor.ads.service.socket;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jar.savor.box.interfaces.OnRemoteOperationListener;
import com.savor.ads.bean.VideoQueueParam;
import com.savor.ads.utils.LogUtils;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author <a href="http://www.lizhaoweb.cn">李召(John.Lee)</a>
 * @version 1.0.0.0.1
 * @EMAIL 404644381@qq.com
 * @notes Created on 2020年10月22日<br>
 * Revision of last commit:$Revision$<br>
 * Author of last commit:$Author$<br>
 * Date of last commit:$Date$<br>
 */
@WebSocket(maxTextMessageSize = 128 * 1024 * 1024, maxBinaryMessageSize = 128 * 1024 * 1024)
public class AnnotatedEchoSocket {
    static String filename = null;
    static long startTime = -1;
    public static final String TAG = AnnotatedEchoSocket.class.getSimpleName();
    private static OnRemoteOperationListener listener;
    private int currentAction = 2;
    private String avatarUrl;
    private String nickName;
    private Session mSession;
    ConcurrentLinkedQueue<VideoQueueParam> queue = new ConcurrentLinkedQueue<>();
    public void setOnRemoteOpreationListener(OnRemoteOperationListener opreationListener) {
        listener = opreationListener;
    }
    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        if (session.isOpen()) {
            this.mSession = session;
            //System.out.printf("返回消息 [%s]%n","ss");
//            session.getRemote().sendString("服务器发送数据： 测试001");
            System.out.println("============================================");
//            Future<Void> fut;
//            fut = session.getRemote().sendStringByFuture("Hello");
//            try {
//                fut.get(2, TimeUnit.SECONDS);

//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

//                fut = session.getRemote().sendStringByFuture(df.format(new Date()));
//                fut.get(2, TimeUnit.SECONDS);
//            } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                e.printStackTrace();
//            }
        }

    }

    @OnWebSocketMessage
    public void onMessage(Session session,String msg) {
//        LogUtils.d(TAG+":onMessage--服务器已经收到消息 " + msg);
        if (Global.startTotalTime < 1) {
            Global.startTotalTime = System.currentTimeMillis();
        }
        long msgStartTime = System.currentTimeMillis();
        try{
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
//            Object object = gson.fromJson(msg,new TypeToken<JSONObject>(){}.getType());
//        FileInfo fileInfo = null;
            JSONObject jsonObject = new JSONObject(msg);
//        if (object instanceof JSONObject) {
//            JSONObject jsonObject = (JSONObject) object;
            jsonObject.put("Session", session);
            jsonObject.put("RemoteEndpoint", session.getRemote());
            jsonObject.put("RemoteSocketAddress", session.getRemoteAddress());
            jsonObject.put("RemoteAddress", session.getRemoteAddress().getAddress());
            jsonObject.put("RemotePort", session.getRemoteAddress().getPort());
            JSONObjectQueueUtil.push(jsonObject);
//        }
//            GsonBuilder builder = new GsonBuilder();
//            Gson gson = builder.create();
//            Object object = gson.fromJson(msg,new TypeToken<VideoParam>(){}.getType());
//            if (object instanceof VideoParam) {
//                VideoParam videoParam = (VideoParam) object;
//                avatarUrl = videoParam.getForm_data().getAvatarUrl();
//                nickName = videoParam.getForm_data().getNickName();
//                String filename = videoParam.getFilename();
//                LogUtils.d(TAG+":onMessage--filename = " + filename);
//                long fileSize = videoParam.getVideo_size();
//                LogUtils.d(TAG+":onMessage--video_size = " + fileSize);
//                String range = videoParam.getSection();
//                LogUtils.d(TAG+":onMessage--section = " + range);
//                long startPos = Long.valueOf(range.split(",")[0]);
//                long endPos = Long.valueOf(range.split(",")[1]);
//                String filePart = videoParam.getVideo_param();
//                LogUtils.d(TAG+":onMessage--video_param = " + filePart);
//                VideoQueueParam param = new VideoQueueParam();
//                param.setLongVideSize(fileSize);
//                param.setFilePart(filePart);
//                param.setStartPos(startPos);
//                param.setEndPos(endPos);
//                RandomAccessFile outRandomAccessFile;
//                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
//                File videoFile = new File(path+filename+".mp4");
//                outRandomAccessFile = new RandomAccessFile(videoFile, "rwd");
//                if (!videoFile.exists() || videoFile.length() < 1) {
//                    outRandomAccessFile.setLength(fileSize);
//                }
//                new Thread(new VideoCreater(queue,param)).start();
//                new Thread(new VideoWriter(queue,outRandomAccessFile)).start();

                /*******************************************/
//                RandomAccessFile outRandomAccessFile = null;
//                try {
//                    byte[] dataBytes = Base64.decode(filePart, Base64.DEFAULT);
//                    int length = (int) (endPos - startPos + 1);
//                      if (length > dataBytes.length) {
//                        LogUtils.d(TAG+":onMessage--数据区间错误");
//                        return;
//                    }
//                    String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
//                    File videoFile = new File(path+filename+".mp4");
//                    outRandomAccessFile = new RandomAccessFile(videoFile, "rwd");
//                    if (!videoFile.exists() || videoFile.length() < 1) {
//                        outRandomAccessFile.setLength(fileSize);
//                    }
//                    outRandomAccessFile.seek(startPos);
//                    outRandomAccessFile.write(dataBytes, 0, length);
//                    LogUtils.d(TAG+":onMessage--写入成功");
//                    String forscreen_id = System.currentTimeMillis()+"";
//
//                    session.getRemote().sendStringByFuture("1000");
//                    String block = videoParam.getFile_block_name().split("_")[1];
//                    int count = videoParam.getAll_file_block();
//                    if (!block.contains("end")&&Integer.valueOf(block)==(count-1)&&listener!=null){
//                        AnnotatedEchoSocket.listener.showVideo(videoFile.getAbsolutePath(), true,forscreen_id,avatarUrl,nickName,null,currentAction, GlobalValues.FROM_SERVICE_REMOTE);
//                    }
                        session.getRemote().sendString("1000");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        session.getRemote().sendString("1001");
                    } catch (IOException e1) {
                        // nothing
                    }
//                    try {
//                        if (outRandomAccessFile != null)
//                            outRandomAccessFile.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
                /*******************************************/
//                LogUtils.d(TAG+":onMessage--结束");
//            }
//            LogUtils.d(TAG+":onMessage--object"+object);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }

    //    @OnWebSocketMessage
    public void onWebSocketBinary(Session session, int a, String s, ByteBuffer byteBuffer) {

    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        LogUtils.d(TAG+":onClose");
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        LogUtils.d(TAG+":onError---出错了");
        t.printStackTrace();
    }

}

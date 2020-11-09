/**
 * Copyright (c) 2020, Stupid Bird and/or its affiliates. All rights reserved.
 * STUPID BIRD PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @Project : jetty
 * @Package : jetty.websocket.server
 * @author <a href="http://www.lizhaoweb.net">李召(John.Lee)</a>
 * @EMAIL 404644381@qq.com
 * @Time : 14:43
 */
package com.savor.ads.service.socket;

import android.content.Context;
import android.util.Base64;

import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.GlobalValues;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.Date;


/**
 * @author <a href="http://www.lizhaoweb.cn">李召(John.Lee)</a>
 * @version 1.0.0.0.1
 * @EMAIL 404644381@qq.com
 * @notes Created on 2020年10月27日<br>
 * Revision of last commit:$Revision$<br>
 * Author of last commit:$Author$<br>
 * Date of last commit:$Date$<br>
 */
public class WriteFileThread extends Thread {
    private Context mContext;
    public WriteFileThread(Context context){
        this.mContext = context;
    }
    @Override
    public void run() {
        while (!Global.shutdown) {
            if (JSONObjectQueueUtil.getSize() < 1) {
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    // nothing
                }
                continue;
            }
//            System.out.printf("[%s] ", dateFormat.format(new Date()));
            long startTime = System.currentTimeMillis();
            FileInfo fileInfo = null;
            JSONObject jsonObject = JSONObjectQueueUtil.pop();
            try {

                RemoteEndpoint remoteEndpoint = (RemoteEndpoint) jsonObject.get("RemoteEndpoint");
                String RemoteAddress = jsonObject.getString("RemoteAddress");

                JSONObject formData = jsonObject.getJSONObject("form_data");
                String userId = formData.getString("openid");
                System.out.printf("%s ", userId);
                final String boxMac = formData.getString("box_mac");
                System.out.printf("%s ", boxMac);

                final String filename = jsonObject.getString("filename");
                System.out.printf("%s | filename=%s ", AnnotatedEchoSocket.filename, filename);
                String fileInfoKey = RemoteAddress + "_" + filename + "_" + userId + "_" + boxMac;
                fileInfo = Global.fileInfoMap.get(fileInfoKey);
                if (fileInfo == null) {
                    fileInfo = new FileInfo();
                    fileInfo.setStartTime(System.currentTimeMillis());
                    fileInfo.setUsername(RemoteAddress);
                }
                fileInfo.setFilename(filename);
                Long fileSize = jsonObject.getLong("video_size");
                fileInfo.setFileSize(fileSize);
                System.out.printf("| video_size=%s ", fileSize);
                String range = jsonObject.getString("section");
                System.out.printf("| section=%s ", range);
                String filePart = jsonObject.getString("video_param");
//            System.out.println("video_param = " + filePart);
                String dBlockNumber = jsonObject.getString("file_block_name");
                System.out.printf("%s", dBlockNumber);
                Integer allBlockNumber = jsonObject.getInt("all_file_block");
                System.out.printf("/%s ", allBlockNumber);

                RandomAccessFile outRandomAccessFile = null;
                try {
                    byte[] dataBytes = Base64.decode(filePart, Base64.DEFAULT);

                    String[] rangeSplits = range.split(",");
                    int length = (int) (Long.parseLong(rangeSplits[1]) - Long.parseLong(rangeSplits[0]) + 1);
                    fileInfo.addBufferSize(length);

                    if (length > dataBytes.length) {
                        System.out.printf("| 数据区间错误");
//                        JSONObjectQueueUtil.push(jsonObject);
                        return;
                    }
                    String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                    File outFile = new File(path + filename + ".mp4");
//                File outFile = new File("C:\\Users\\admin\\jetty-webapps\\test.mp4");
//                File outFile = new File("C:\\Users\\admin\\jetty-webapps\\" + filename + ".mp4");
                    outRandomAccessFile = new RandomAccessFile(outFile, "rwd");
                    if (!outFile.exists() || outFile.length() < 1) {
                        startTime = System.currentTimeMillis();
                        AnnotatedEchoSocket.filename = filename;
                        outRandomAccessFile.setLength(fileSize);
                    }
                    outRandomAccessFile.seek(Integer.parseInt(rangeSplits[0]));
                    String forscreen_id = System.currentTimeMillis()+"";
                    String avatarUrl = "";
                    String nickName = "";
                    int currentAction = 2;
                    if ("file_3".equals(dBlockNumber)) {
                        ProjectOperationListener.getInstance(mContext).showVideo(outFile.getAbsolutePath(), true,forscreen_id,avatarUrl,nickName,null,currentAction, GlobalValues.FROM_SERVICE_REMOTE);
//                    HttpRequest.sendPost("http://admin.littlehotspot.com/test/forscreenhelpvideo", String.format("f=%s&b=%s", URLEncoder.encode("http://192.168.168.20:7778/" + filename + ".mp4", "UTF-8"), "123456"));
//                        new Thread() {
//                            @Override
//                            public void run() {
//                                try {
//                                    String apiUrl = "http://admin.littlehotspot.com/test/forscreenhelpvideo";
//                                    String parameters = String.format("f=%s&box=%s", URLEncoder.encode("http://" + serverHost + ":" + serverPort + "/" + filename + ".mp4", "UTF-8"), boxMac);
//
//                                    System.out.println("=================================>" + apiUrl + "?" + parameters);
//                                    HttpRequest.sendPost(apiUrl, parameters);
//                                    AppApi.po
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }.start();
//                        System.out.printf("\tCallBox");
                    }
                    outRandomAccessFile.write(dataBytes, 0, length);
                    System.out.printf("\t成功");
                    Global.fileInfoMap.put(fileInfoKey, fileInfo);
                } finally {
                    try {
                        if (outRandomAccessFile != null)
                            outRandomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                double speed = -1;
                String fileInfoString = null;
                long totalTime = -1;
                if (fileInfo != null) {
                    fileInfoString = fileInfo.toString();
                    totalTime = System.currentTimeMillis() - fileInfo.getStartTime();
                    speed = fileInfo.getBufferSize() / totalTime * 1000 / Constant._1K;
                }
                System.out.printf("\t结束.耗时 %dms %s. 总耗时 %dms/%dms. 写入速度:%sMB/s", System.currentTimeMillis() - startTime, fileInfoString, totalTime, System.currentTimeMillis() - Global.startTotalTime, speed);
            } catch (Exception e) {
                JSONObjectQueueUtil.push(jsonObject);
                e.printStackTrace();
            }
            System.out.println();
        }
    }
}

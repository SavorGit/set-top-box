/**
 * Copyright (c) 2020, Stupid Bird and/or its affiliates. All rights reserved.
 * STUPID BIRD PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @Project : jetty
 * @Package : jetty.http
 * @author <a href="http://www.lizhaoweb.net">李召(John.Lee)</a>
 * @EMAIL 404644381@qq.com
 * @Time : 19:25
 */
package com.savor.ads.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.jar.savor.box.interfaces.OnRemoteOperationListener;
import com.jar.savor.box.vo.BaseResponse;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogUtils;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;


/**
 * @author <a href="http://www.lizhaoweb.cn">李召(John.Lee)</a>
 * @version 1.0.0.0.1
 * @EMAIL 404644381@qq.com
 * @notes Created on 2020年10月27日<br>
 * Revision of last commit:$Revision$<br>
 * Author of last commit:$Author$<br>
 * Date of last commit:$Date$<br>
 */
@MultipartConfig
public class MultiPartitionServlet extends AbstractHandler {
    private static final String TAG = MultiPartitionServlet.class.getSimpleName();
    private static Pattern integerPattern = Pattern.compile("^\\d+$");
//    int count;
//    String preFilename;
    private static ArrayList<String> ids = new ArrayList<>();
    private Context mContext;
    public MultiPartitionServlet(Context context){
        mContext = context;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        LogUtils.w("***********一次请求处理开始...***********");
//        LogUtils.d("target = " + target);
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(200);
        baseRequest.setHandled(true);

        RandomAccessFile outRandomAccessFile = null;
        InputStream inputStream = null;
        String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);

        try {
            String filePart = null;
            inputStream = request.getInputStream();
            String identifier = request.getParameter("identifier");
            String index = request.getParameter("index");
            String fileName = request.getParameter("fileName");
            String position = request.getParameter("position");
            String totalSize = request.getParameter("totalSize");
            String chunkSize = request.getParameter("chunkSize");
            String totalChunks = request.getParameter("totalChunks");
            File outFile = new File(path + fileName + ".mp4");
            String action = request.getPathInfo();
//            if (action.contains("finish")){
//                String forscreen_id = System.currentTimeMillis()+"";
//                String avatarUrl = "";
//                String nickName = "";
//                int currentAction = 2;
//                ProjectOperationListener.getInstance(mContext).showVideo(outFile.getAbsolutePath(), true,forscreen_id,avatarUrl,nickName,null,currentAction, GlobalValues.FROM_SERVICE_REMOTE);
//                return;
//            }

            int partIndex = -1, partsCount = -1;
            long fileLength = -1, partSize = -1;
            if (StringUtil.isNotBlank(index)) {
                partIndex = Integer.parseInt(index);
            }
            if (StringUtil.isNotBlank(totalSize)) {
                fileLength = Long.parseLong(totalSize);
            }
            if (StringUtil.isNotBlank(chunkSize)) {
                partSize = Long.parseLong(chunkSize);
            }
            if (StringUtil.isNotBlank(totalChunks)) {
                partsCount = Integer.parseInt(totalChunks);
            }
            if (ids.size()==4) {
                String forscreen_id = System.currentTimeMillis()+"";
                String avatarUrl = "";
                String nickName = "";
                int currentAction = 2;
                ids.clear();
//                String path2 = AppUtils.getFilePath(AppUtils.StorageFile.cache);
//                String baseurl = "http://192.168.99.2:8080/projection/";
//                ProjectOperationListener.getInstance(mContext).showVideo(path2,baseurl+fileName+".mp4", true,forscreen_id,avatarUrl,nickName, GlobalValues.FROM_SERVICE_REMOTE);
                ProjectOperationListener.getInstance(mContext).showVideo(outFile.getAbsolutePath(), true,forscreen_id,avatarUrl,nickName,currentAction, GlobalValues.FROM_SERVICE_REMOTE);
            }
            outRandomAccessFile = new RandomAccessFile(outFile, "rwd");
            if (!outFile.exists() || outFile.length() < 1) {
                outRandomAccessFile.setLength(fileLength);
            }
            byte[] byteCache = new byte[4096];
            if (position != null && integerPattern.matcher(position).find()) {
//                LogUtils.d(TAG+"position===="+position);
                outRandomAccessFile.seek(Long.parseLong(position));

                InputStream inputStream1 = request.getInputStream();
                if (inputStream == null) {
                    inputStream1 = request.getPart("video_param").getInputStream();
                }
                int len = -1;

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                while ((len = inputStream1.read(byteCache)) > -1) {
                    byteArrayOutputStream.write(byteCache, 0, len);
                }
                filePart = new String(byteArrayOutputStream.toByteArray());

                byte[] dataBytes = android.util.Base64.decode(filePart, Base64.DEFAULT);
                outRandomAccessFile.write(dataBytes, 0, dataBytes.length);
//                LogUtils.d(TAG+"if写入完成===="+dataBytes.length);
            } else {
                outRandomAccessFile.seek(partIndex * partSize);
                int readLength = 0;
                while ((readLength = inputStream.read(byteCache)) > -1) {
                    outRandomAccessFile.write(byteCache, 0, readLength);
                }
//                LogUtils.d(TAG+"else写入完成====");
                if (!TextUtils.isEmpty(index)&&(index.equals("0")||index.equals("1")||index.equals("2")||index.equals("3"))){
                    ids.add(index);
                }
            }
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setMsg("保存成功");
            baseResponse.setCode(1000);
            String resp = new Gson().toJson(baseResponse);
            response.getWriter().println(resp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outRandomAccessFile != null)
                outRandomAccessFile.close();
            if (inputStream != null)
                inputStream.close();
        }

    }
}

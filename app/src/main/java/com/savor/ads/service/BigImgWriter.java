package com.savor.ads.service;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.savor.ads.bean.ImgQueueParam;
import com.savor.ads.bean.VideoQueueParam;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.utils.GlobalValues;

import org.eclipse.jetty.util.StringUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import tv.danmaku.ijk.media.exo2.RangeManager;
import tv.danmaku.ijk.media.exo2.RangeManagerFactory;

public class BigImgWriter implements Runnable {
    private static final String TAG = "BigImgWriter";

    private Context mContext;
    private RandomAccessFile randomAccessFile;
    private String basePath;
    private ConcurrentLinkedQueue<ImgQueueParam> queue;
    private HashMap<String,RandomAccessFile> imgMap;
    private HashMap<String,Integer> imgCount;
    private HashMap<String,String> imgCreateTime;
    private boolean overTime;
    private Handler mHander = new Handler(Looper.getMainLooper());
    private String forscreen_id;
    private RemoteService.ToPlayInterface playListener;
    public BigImgWriter(String screen_id,ConcurrentLinkedQueue<ImgQueueParam> q, String basePath) {
        this.forscreen_id = screen_id;
        this.queue = q;
        this.basePath = basePath;
        overTime = true;
        mHander.postDelayed(overTimeRunnable, 1000 * 30);
        imgMap = new HashMap<>();
        imgCount = new HashMap<>();
        imgCreateTime = new HashMap<>();
    }

    public Runnable overTimeRunnable = ()->overTime = false;

    public void setToPlayListener(RemoteService.ToPlayInterface toPlayListener){
        this.playListener = toPlayListener;
    }

    @Override
    public void run() {
        try {
//            File outFile = new File(basePath);
//            //TODO: 已经上传过的文件判断是否完整，避免重复上传
//            if (outFile.exists()) {
//                outFile.delete();
//            }
//            this.randomAccessFile = new RandomAccessFile(new File(basePath), "rws");

            while (!queue.isEmpty() || overTime) {
                if (!forscreen_id.equals(GlobalValues.CURRRNT_PROJECT_ID)){
                    break;
                }
                ImgQueueParam param = queue.poll();
                if (param == null) {
//                    Log.d(TAG,"queue队列为空,等待数据传入，10秒无数据退出线程");
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, e.getMessage());
                    }
                    continue;
                }

                mHander.removeCallbacks(overTimeRunnable);
                mHander.postDelayed(overTimeRunnable, 1000 * 30);

                String index = param.getIndex();
                String position = param.getPosition();
                String totalSize = param.getTotalSize();
                String totalChunks = param.getTotalChunks();
                byte[] inputByte = param.getInputContent();
                int totalParts = -1;//totalParts不包含尾部
                long fileLength = -1;
                long positionLong = -1;

                if (StringUtil.isNotBlank(totalSize)) {
                    fileLength = Long.parseLong(totalSize);
                }
                if (!TextUtils.isEmpty(totalChunks)) {
                    totalParts = Integer.valueOf(totalChunks);
                }
                if (!TextUtils.isEmpty(position)){
                    positionLong = Long.valueOf(position);
                }
                String fileName;
                if (param.getThumbnail().equals("0")){
                    fileName = param.getFileName();
                }else {
                    fileName = "t_"+param.getFileName();
                }
                File file = new File(basePath+fileName);
                if (!file.exists()){
                    this.randomAccessFile = new RandomAccessFile(file, "rws");
                    imgMap.put(fileName,randomAccessFile);
                    randomAccessFile.setLength(fileLength);
                    imgCreateTime.put(fileName,System.currentTimeMillis()+"");
                }else{
                    this.randomAccessFile = imgMap.get(fileName);
                }


                byte[] filePart = Base64.decode(inputByte, Base64.DEFAULT);
                randomAccessFile.seek(positionLong);
                randomAccessFile.write(filePart);

                if (imgCount.containsKey(fileName)){
                    imgCount.put(fileName,imgCount.get(fileName)+1);
                }else{
                    imgCount.put(fileName,1);
                }

                if (imgCount.get(fileName) == totalParts) {
                    param.setFilePath(basePath+fileName);
                    param.setStartTime(imgCreateTime.get(fileName));
                    param.setFileName(fileName);
                    if (playListener!=null){
                        this.playListener.playProjection(param);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                    randomAccessFile = null;
                }
                imgMap.clear();
                imgCount.clear();
                imgCreateTime.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

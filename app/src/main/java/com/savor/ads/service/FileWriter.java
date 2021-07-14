package com.savor.ads.service;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.savor.ads.bean.FileQueueParam;
import com.savor.ads.bean.VideoQueueParam;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.utils.GlobalValues;

import org.eclipse.jetty.util.StringUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentLinkedQueue;

import tv.danmaku.ijk.media.exo2.RangeManager;
import tv.danmaku.ijk.media.exo2.RangeManagerFactory;

public class FileWriter implements Runnable {
    private static final String TAG = "FileWriter";

    private Context mContext;
    private RandomAccessFile randomAccessFile;
    private String filePath;
    private ConcurrentLinkedQueue<FileQueueParam> queue;
    //已经写入多少块了
    private int writedParts;
    private boolean overTime;
    private Handler mHander = new Handler(Looper.getMainLooper());
    private String avatarUrl = null;
    private String nickName = null;
    private String forscreen_id;
    private RemoteService.ToPlayInterface playListener;
    public FileWriter(Context context, String screen_id, ConcurrentLinkedQueue<FileQueueParam> q, String outPath) {
        this.mContext = context;
        this.forscreen_id = screen_id;
        this.queue = q;
        this.filePath = outPath;
        overTime = true;
        mHander.postDelayed(overTimeRunnable, 1000 * 30);
        writedParts = 0;
        GlobalValues.bytesNotWrite = Long.MAX_VALUE;
    }
    public void setUserInfo(String avatarUrl,String nickName){
        this.avatarUrl = avatarUrl;
        this.nickName = nickName;
    }

    public Runnable overTimeRunnable = ()->overTime = false;

    public void setToPlayListener(RemoteService.ToPlayInterface toPlayListener){
        this.playListener = toPlayListener;
    }

    @Override
    public void run() {
        try {
            File outFile = new File(filePath);
            //TODO: 已经上传过的文件判断是否完整，避免重复上传
            if (outFile.exists()) {
                outFile.delete();
            }
            this.randomAccessFile = new RandomAccessFile(new File(filePath), "rws");


            while (!queue.isEmpty() || overTime) {
                if (!forscreen_id.equals(GlobalValues.CURRENT_FORSCREEN_ID)){
                    break;
                }
                FileQueueParam param = queue.poll();
                if (param == null) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, e.getMessage());
                    }
                    continue;
                }
                Log.d(TAG, "循环开始，queue.size===" + queue.size());
                mHander.removeCallbacks(overTimeRunnable);
                mHander.postDelayed(overTimeRunnable, 1000 * 30);

                String index = param.getIndex();
                String position = param.getPosition();
                String totalSize = param.getTotalSize();
                String chunkSize = param.getChunkSize();
                String totalChunks = param.getTotalChunks();
                byte[] inputByte = param.getInputContent();

                int partIndex = -1, totalParts = -1;//totalParts不包含尾部
                long fileLength = -1, partSize = -1;
                long positionLong = -1;
                if (StringUtil.isNotBlank(index)) {
                    partIndex = Integer.parseInt(index);
                }
                if (StringUtil.isNotBlank(totalSize)) {
                    fileLength = Long.parseLong(totalSize);
                }
                if (StringUtil.isNotBlank(chunkSize)) {
                    partSize = Long.parseLong(chunkSize);
                }
                if (!TextUtils.isEmpty(totalChunks)) {
                    totalParts = Integer.valueOf(totalChunks);
                }
                if (!TextUtils.isEmpty(position)){
                    positionLong = Long.valueOf(position);
                }

                if (TextUtils.isEmpty(index)) {
                    randomAccessFile.setLength(fileLength);
                }
                Log.d(TAG, "第{" + index + "}段写入开始 total=" + totalSize);

                byte[] filePart = Base64.decode(inputByte, Base64.DEFAULT);
                randomAccessFile.seek(positionLong);
                randomAccessFile.write(filePart);
                writedParts++;
                if (writedParts==totalParts){
                    Log.d(TAG,"上传完成，退出while,totalParts="+totalParts+",writedParts="+writedParts);
                    if (playListener!=null){
                        this.playListener.playProjection(param);
//                        ProjectOperationListener.getInstance(mContext).showVideo(filePath,true,forscreen_id,avatarUrl,nickName, GlobalValues.FROM_SERVICE_REMOTE);
                    }
                    break;
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable clearNotWriteByte = ()->GlobalValues.bytesNotWrite = 0;

    private void sendddddd(String path) {
        Intent intent = new Intent();
        intent.setAction("panhouye");
        intent.putExtra("path", path);
        mContext.sendBroadcast(intent);
    }


}

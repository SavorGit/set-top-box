package com.savor.ads.utils;

import android.content.Context;

import com.savor.ads.okhttp.coreProgress.download.ProgressDownloader;
import com.savor.ads.okhttp.coreProgress.download.ProgressResponseListener;

import java.io.File;

/**
 * Created by Administrator on 2016/12/9.
 */

public class DownloadManagerBak implements ProgressResponseListener{

    private Context mContext;
    private long brealPoints;
    private long totalBytes;
    private long contentLength;

    private String url;
    private File file;

    private static DownloadManagerBak instance;

    ProgressDownloader downloader = null;
    public DownloadManagerBak(Context context){
        this.mContext = context;
    }

    public static DownloadManagerBak get(Context context){
        if (instance ==null){
            instance = new DownloadManagerBak(context);
        }
        return instance;
    }


    public void startDownload(String url,File file){
//        downloader = new ProgressDownloader(url,file,this);
//        downloader.download(this.brealPoints);
    }

    @Override
    public void onPreExecute(long contentLength) {
        // 文件总长只需记录一次，要注意断点续传后的contentLength只是剩余部分的长度
        if (this.contentLength == 0L) {
            this.contentLength = contentLength;
//            progressBar.setMax((int) (contentLength / 1024));
        }
    }

    @Override
    public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
        //加上断点的长度
        this.totalBytes = bytesRead+brealPoints;
        if (done){
            System.out.print("下载完成");
        }
    }

    @Override
    public void FailedDownload() {
        brealPoints = totalBytes;
    }
}

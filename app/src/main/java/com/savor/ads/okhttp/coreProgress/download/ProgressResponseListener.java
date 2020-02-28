package com.savor.ads.okhttp.coreProgress.download;

/**
 * 响应体进度回调接口，比如用于文件下载中
 * 20160614
 */
public interface ProgressResponseListener {

    /**
     * 总字节数
     * @param contentLength
     */
    void onPreExecute(long contentLength);
    /**
     * @param bytesRead     已下载字节数
     * @param contentLength 总字节数
     * @param done          是否下载完成
     */
    void onResponseProgress(long bytesRead, long contentLength, boolean done);

    void FailedDownload();
}


package com.savor.ads.oss;

import android.content.Context;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.Range;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.savor.ads.BuildConfig;
import com.savor.ads.core.Session;
import com.savor.ads.log.LogUploadService;
import com.savor.ads.service.MiniProgramNettyService;
import com.savor.ads.service.MiniProgramNettyService.DownloadProgressListener;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by bichao on 12/21/16.
 */
public class OSSUtils {
    public static final String SAVOR_LAST_MODIFIED = "Savor-Last-Modified";
    private Context context;
    private OSS oss;
    private OSS ossNative;
    /**桶名称**/
    private String bucketName;
    /**阿里云上传日志所用**/
    private String objectKey;
    /**阿里云下载视频所用*/
    private String objectKey2;
    private String uploadFilePath;
    private LogUploadService.UploadCallback mUploadCallback;
    private File localFile;
    private boolean isDownloaded;
    private int tryCount = 0;
    private DownloadProgressListener downloadProgressListener;
    public OSSUtils(Context context,String bucketName, String objectKey, String uploadFilePath, LogUploadService.UploadCallback result) {
        this.context = context;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadFilePath = uploadFilePath;
        this.mUploadCallback = result;
        initOSSClient();
    }

    public OSSUtils(Context context,String bucketName, String objectKey, String uploadFilePath) {
        this.context = context;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadFilePath = uploadFilePath;
        initOSSClient();
    }

    public OSSUtils(Context context,String bucketName, String objectKey,File file,boolean isNative) {
        this.context = context;
        this.bucketName = bucketName;
        this.objectKey2 = objectKey;
        this.localFile = file;
        if (isNative){
            initNativeOSSClient();
        }else {
            initOSSClient();
        }
    }

    void initOSSClient(){
        if (oss!=null){
            return;
        }
        String accessKeyId = StreamUtils.getOSSValue(context,OSSValues.accessKeyId);
        String accessKeySecret = StreamUtils.getOSSValue(context,OSSValues.accessKeySecret);
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId,accessKeySecret);

        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        conf.setUserAgentMark("hotelId="+Session.get(context).getBoiteId()+"/boxMac="+Session.get(context).getEthernetMac());
        OSSLog.enableLog();
        oss = new OSSClient(context, BuildConfig.OSS_ENDPOINT, credentialProvider, conf);
    }

    void initNativeOSSClient(){
        if (ossNative!=null){
            return;
        }
        String accessKeyId = StreamUtils.getOSSValue(context,OSSValues.accessKeyId);
        String accessKeySecret = StreamUtils.getOSSValue(context,OSSValues.accessKeySecret);
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId,accessKeySecret);

        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        conf.setUserAgentMark("hotelId="+Session.get(context).getBoiteId()+"/boxMac="+Session.get(context).getEthernetMac());
        OSSLog.enableLog();
        ossNative = new OSSClient(context, BuildConfig.OSS_NATIVE_ENDPOINT, credentialProvider, conf);
    }
    public void setDownloadProgressListener(DownloadProgressListener listener){
        downloadProgressListener = listener;
    }

    /**
     * 同步上传文件
     */
    public boolean syncUploadFile(){
        boolean flag = false;
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(bucketName, objectKey, uploadFilePath);
        File uploadFile = new File(uploadFilePath);
        Date lastModifiedDate = new Date(uploadFile.lastModified());
        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(org.apache.commons.io.FileUtils);
        metadata.setContentLength(uploadFile.length());
        metadata.setLastModified(lastModifiedDate);
        metadata.setExpirationTime(lastModifiedDate);
        metadata.addUserMetadata(SAVOR_LAST_MODIFIED, DateUtil.formatRfc822Date(lastModifiedDate));
        put.setMetadata(metadata);

        try {
            PutObjectResult putResult = oss.putObject(put);
            Log.d("PutObject", "UploadSuccess");
            Log.d("ETag", putResult.getETag());
            Log.d("RequestId", putResult.getRequestId());
            flag = true;
        }catch (ClientException e){
            e.printStackTrace();
        }catch (ServiceException e){
            // 服务异常。
            Log.e("RequestId", e.getRequestId());
            Log.e("ErrorCode", e.getErrorCode());
            Log.e("HostId", e.getHostId());
            Log.e("RawMessage", e.getRawMessage());
        }

        return flag;
    }


    /**
     * 阿里云OSS异步上传文件
     */
    public void asyncUploadFile(){
        File uploadFile = new File(uploadFilePath);
        Date lastModifiedDate = new Date(uploadFile.lastModified());
        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(org.apache.commons.io.FileUtils);
        metadata.setContentLength(uploadFile.length());
        metadata.setLastModified(lastModifiedDate);
        metadata.setExpirationTime(lastModifiedDate);
        metadata.addUserMetadata(SAVOR_LAST_MODIFIED, DateUtil.formatRfc822Date(lastModifiedDate));
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(bucketName, objectKey, uploadFilePath,metadata);
        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                if (downloadProgressListener!=null){
                    downloadProgressListener.getDownloadProgress(currentSize,totalSize);
                }
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");
                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());
                mUploadCallback.isSuccessOSSUpload(true);
            }
            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
                mUploadCallback.isSuccessOSSUpload(false);
            }
        });
    }
    /**
     * OSS同步下载方法
     */
    public boolean syncDownload() {
        isDownloaded = false;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        //构造下载文件请求
        GetObjectRequest get = new GetObjectRequest(bucketName, objectKey2);
        //设置下载进度回调
        get.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize, false);
                if (downloadProgressListener!=null){
                    downloadProgressListener.getDownloadProgress(currentSize,totalSize);
                }
            }
        });
        try {
            // 同步执行下载请求，返回结果
            GetObjectResult getResult = oss.getObject(get);
            Log.d("Content-Length", "" + getResult.getContentLength());
            outputStream = new FileOutputStream(localFile);
            // 获取文件输入流
            inputStream = getResult.getObjectContent();
            byte[] buffer = new byte[2048];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                // 处理下载的数据，比如图片展示或者写入文件等
                outputStream.write(buffer, 0, len);
                outputStream.flush();
            }
            // 下载后可以查看文件元信息
            ObjectMetadata metadata = getResult.getMetadata();
            Log.d("ContentType", metadata.getContentType());
            isDownloaded = true;
        } catch (ClientException e1) {
            // 本地异常如网络异常等
            e1.printStackTrace();
            LogUtils.d("OSSUtils:url="+objectKey2);
            LogUtils.d("OSSUtils:ClientException="+e1.getMessage());
        } catch (ServiceException e2) {
            // 服务异常
            Log.e("RequestId", e2.getRequestId());
            Log.e("ErrorCode", e2.getErrorCode());
            Log.e("HostId", e2.getHostId());
            Log.e("RawMessage", e2.getRawMessage());
            LogUtils.d("OSSUtils:ServiceException="+e2.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.d("OSSUtils:IOException="+e.getMessage());
        }finally {
            try {
                if (inputStream!=null){
                    inputStream.close();
                }
                if (outputStream!=null){
                    outputStream.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return isDownloaded;
    }

    /**
     * OSS原生节点同步下载方法
     * @return
     */
    public boolean syncNativeOSSDownload() {

        isDownloaded = false;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            //构造下载文件请求
            GetObjectRequest get = new GetObjectRequest(bucketName, objectKey2);
            //设置下载进度回调
            get.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
                @Override
                public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {

                    OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize, false);
                    if (downloadProgressListener!=null){
                        downloadProgressListener.getDownloadProgress(currentSize,totalSize);
                    }
                }
            });
            // 同步执行下载请求，返回结果
            GetObjectResult getResult = ossNative.getObject(get);
            Log.d("Content-Length", "" + getResult.getContentLength());
            outputStream = new FileOutputStream(localFile);
            // 获取文件输入流
            inputStream = getResult.getObjectContent();
            byte[] buffer = new byte[2048];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                // 处理下载的数据，比如图片展示或者写入文件等
                outputStream.write(buffer, 0, len);
                outputStream.flush();
            }
            // 下载后可以查看文件元信息
            ObjectMetadata metadata = getResult.getMetadata();
            Log.d("ContentType", metadata.getContentType());
            Log.d("requestid", getResult.getRequestId());
            isDownloaded = true;
            tryCount = 0;
        } catch (ClientException e1) {
            // 本地异常如网络异常等
            e1.printStackTrace();
            if (tryCount<10){
                tryCount ++;
                try {
                    Thread.sleep(1000);
                    syncNativeOSSDownload();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            LogUtils.d("OSSUtils:url="+objectKey2);
            LogUtils.d("OSSUtils:ClientException="+e1.getMessage());
        } catch (ServiceException e2) {
            if (tryCount<10){
                tryCount ++;
                try {
                    Thread.sleep(1000);
                    syncNativeOSSDownload();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            // 服务异常
            Log.e("RequestId", e2.getRequestId());
            Log.e("ErrorCode", e2.getErrorCode());
            Log.e("HostId", e2.getHostId());
            Log.e("RawMessage", e2.getRawMessage());
            LogUtils.d("OSSUtils:ServiceException="+e2.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.d("OSSUtils:IOException="+e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (inputStream!=null){
                    inputStream.close();
                }
                if (outputStream!=null){
                    outputStream.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return isDownloaded;
    }

}

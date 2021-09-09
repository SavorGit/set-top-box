package com.savor.ads.okhttp.coreProgress.download;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.dialog.BoxInfoDialog;
import com.savor.ads.log.LogParamValues;
import com.savor.ads.core.Session;
import com.savor.ads.log.LogReportUtil;
import com.savor.ads.service.HandleMediaDataService;
import com.savor.ads.service.MiniProgramNettyService;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogFileUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/12/9.
 * Modify  by bichao on 2019-06-25 13:41
 */

public class FileDownloader {
    public static final String TAG = "FileDownloader";
    public final static int CONNECT_TIMEOUT =60;
    public final static int READ_TIMEOUT=10;
    public final static int WRITE_TIMEOUT=10;
    private Context context;
    private Session session;
    private String url;
    private OkHttpClient client;
    //下载文件存储的位置
    private String filePath;
    private String fileName;
    private long fileSize;
    //下载失败重复下载次数，最多3次
//    private int downloadCount=0;
    private Call call;
    private boolean standard;
//    public static int count =0;
    Handler handler = new Handler(Looper.getMainLooper());

    public FileDownloader(Context context, String url, String filePath, String fileName){
        this.context = context;
        session = Session.get(context);
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
        this.standard = false;
        //在下载、暂停后的继续下载中可复用同一个client对象
        client = getProgressClient();
    }

    public FileDownloader(Context context, String url, String filePath, String fileName, boolean standard){
        this.context = context;
        session = Session.get(context);
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
        this.standard = standard;
        client = getProgressClient();
    }

    //每次下载需要新建新的Call对象
    private Call newRangeCall(long startPoints){
        Request request = new Request.Builder()
                .url(url)
                .header("Range","bytes="+startPoints+"-")//断点续传下载需要用到的，提示下载的区域
                .build();
        return client.newCall(request);
    }
    private Call newCall(){
        Request request = new Request.Builder()
                .url(url)
                .build();
        return client.newCall(request);
    }


    public OkHttpClient getProgressClient(){
        //拦截器，用上ProgressResponseBody
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(),fileName))
                        .build();
            }
        };
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS)//设置连接超时时间
                .build();
    }

    public boolean downloadByRange(){
        handler.removeCallbacks(downloadStateRunnable);
        long startIndex = 0;
        boolean flag = false;
        try {
            File cacheFile = new File(filePath,fileName+ ConstantValues.CACHE);
            long startTime = System.currentTimeMillis();
            GlobalValues.isDownload = true;
            GlobalValues.currentDownlaodFileName = fileName;
            downloadState();
            if (cacheFile.exists()){
                RandomAccessFile cacheAccessFile = new RandomAccessFile(cacheFile,"rwd");
                try {
                    startIndex = cacheAccessFile.length();
                }catch (NumberFormatException e){
                        e.printStackTrace();
                }
                call = newRangeCall(startIndex);
                Response response = call.execute();
                if (response.code()==206){
                    flag = saveRangeFile(response,startIndex,cacheFile);
                }
            }else{
                call = newCall();
                Response response = call.execute();
                if (response.code() == 200) {
                    flag = saveRangeFile(response,startIndex,cacheFile);
                }
            }
            String useTime = String.valueOf(System.currentTimeMillis()-startTime);
            if (flag){
                GlobalValues.isDownload = false;
                downloadState();
                String resourceSize = String.valueOf(new File(filePath+fileName).length());
                String mUUID = String.valueOf(System.currentTimeMillis());
                if (standard){
                    LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.download, LogParamValues.standard_size,resourceSize);
                    LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.download, LogParamValues.standard_duration,useTime);
                }else {
                    LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.download, LogParamValues.speed_size,resourceSize);
                    LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.download,LogParamValues.speed_duration,useTime);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    private boolean saveRangeFile(Response response,long startIndex,File cacheFile) throws IOException{
        LogFileUtil.writeDownloadLog("下载文件--开始--fileName="+fileName+",fileLength="+startIndex);
        boolean flag;
        InputStream is = null;
        RandomAccessFile tmpAccessFile = new RandomAccessFile(cacheFile, "rw");// 获取前面已创建的文
        tmpAccessFile.seek(startIndex);
        try {
            is = response.body().byteStream();// 获取流
            byte[] buffer = new byte[1024*1024*3];
            int length;
            boolean isBreak = false;
            while ((length = is.read(buffer)) > 0) {//读取流
                if (AppUtils.isInProjection()||GlobalValues.completionRate ==-1){
                    isBreak = true;
                    break;
                }
                tmpAccessFile.write(buffer, 0, length);
            }
            if (isBreak){
                flag = false;
            }else{
                flag = true;
            }
        }catch (Exception e){
            flag = false;
            e.printStackTrace();
        }finally {
            if (is!=null){
                is.close();
            }
            close(response);
        }
        if (flag){
            cacheFile.renameTo(new File(filePath+fileName));
            LogFileUtil.writeDownloadLog("下载文件--完成--fileName="+fileName+",fileLength="+tmpAccessFile.length());
        }
        return flag;
    }

    private void downloadState(){
        try {
            Activity activity = ActivitiesManager.getInstance().getSpecialActivity(AdsPlayerActivity.class);
            Service service = null;
            if (context instanceof Service){
                service = (Service)context;
            }
            if (activity!=null&&service !=null&&service instanceof HandleMediaDataService){
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    ((AdsPlayerActivity) activity).showDownloadState();
                }else {
                    handler.post(()->((AdsPlayerActivity) activity).showDownloadState());
                }
                handler.postDelayed(downloadStateRunnable,500);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private Runnable downloadStateRunnable = ()->downloadState();

    /**
     * 关闭资源
     *
     * @param closeables
     */
    private void close(Closeable... closeables) {
        int length = closeables.length;
        try {
            for (int i = 0; i < length; i++) {
                Closeable closeable = closeables[i];
                if (null != closeable){
                    closeables[i].close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < length; i++) {
                closeables[i] = null;
            }
        }
    }
}

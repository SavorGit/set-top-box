package com.savor.ads.okhttp.coreProgress.download;

import android.text.TextUtils;
import android.util.Log;

import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2016/12/9.
 * Modify  by bichao on 2019-06-25 13:41
 */

public class ProgressDownloader {
    public static final String TAG = "ProgressDownloader";
    public final static int CONNECT_TIMEOUT =60;
    public final static int READ_TIMEOUT=10;
    public final static int WRITE_TIMEOUT=10;
    private String url;
    private OkHttpClient client;
    //下载文件存储的位置
    private File file;
    private String filePath;
    private String fileName;
    private Call call;

    public ProgressDownloader (String url,File destination){
        this.url = url;
        this.file = destination;
        //在下载、暂停后的继续下载中可复用同一个client对象
        client = getProgressClient();
    }

    public ProgressDownloader (String url,String filePath,String fileName){
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
        //在下载、暂停后的继续下载中可复用同一个client对象
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

    //startsPoint指定开始下载的点
//    public boolean download(final long startsPoint) throws Exception{
//        call = newRangeCall(startsPoint);
//
//        Response response = call.execute();
//        if(response!=null&&response.code()==200){
//            save(response,0);
//            response.close();
//        }else{
//            return false;
//        }
//
//        return  true;
//    }


    public boolean downloadByRange(){
        long startIndex = 0;
        boolean flag = false;
        try {
            File cacheFile = new File(filePath,fileName+ ConstantValues.CACHE);
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
                    saveRangeFile(response,startIndex,cacheFile);
                }
            }else{
                call = newCall();
                Response response = call.execute();
                if (response.code() == 200) {
                    saveRangeFile(response,startIndex,cacheFile);
                }
            }
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    private void saveRangeFile(Response response,long startIndex,File cacheFile) throws IOException{
        LogFileUtil.writeDownloadLog("下载文件--开始--fileName="+fileName+",fileLength="+startIndex);
        boolean flag;
        InputStream is = null;
        RandomAccessFile tmpAccessFile = new RandomAccessFile(cacheFile, "rw");// 获取前面已创建的文
        tmpAccessFile.seek(startIndex);
        try {
            is = response.body().byteStream();// 获取流
            byte[] buffer = new byte[1024*1024*3];
            int length;
            while ((length = is.read(buffer)) > 0) {//读取流
                tmpAccessFile.write(buffer, 0, length);
            }
            flag = true;
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

    }

//    private void save(Response response,long startsPoint) throws Exception {
//        ResponseBody body = response.body();
//        InputStream inputStream = body.byteStream();
////        FileChannel channelOut = null;
////        //随机访问文件，可以指定断点续传的起始位置
////        RandomAccessFile randomAccessFile = null;
//        FileOutputStream outputStream = null;
//        try{
////            randomAccessFile = new RandomAccessFile(destination,"rwd");
////            //Chanel NIO中的用法，由于RandomAccessFile没有使用缓存策略，直接使用会使得下载速度变慢，亲测缓存下载3.3秒的文件，用普通的RandomAccessFile需要20多秒。
////            channelOut = randomAccessFile.getChannel();
////            //内存映射，直接使用RandomAccessFile，使用其seek方法指定下载的起始位置，使用缓存下载，在这里指定下载位置
////            MappedByteBuffer mappedByteBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE,startsPoint,body.contentLength());
//            outputStream = new FileOutputStream(file);
//            byte[] buffer = new byte[4096];
//            int len;
//            while ((len = inputStream.read(buffer))!=-1){
////                ByteBuffer bf  = mappedByteBuffer.put(buffer,0,len);
////                LogUtils.v("while---------" + url+"   " + randomAccessFile.length());
//                outputStream.write(buffer, 0, len);
//                outputStream.flush();
//
////                try {
////                    Thread.sleep(1);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//            }
//        }catch (Exception e){
//            if (progressResponseListener != null) {
//                progressResponseListener.FailedDownload();
//            }
//            e.printStackTrace();
//            throw new Exception();
//        }finally {
//            try {
//                inputStream.close();
////                if (channelOut != null) {
////                    channelOut.close();
////                }
////                if (randomAccessFile != null) {
////                    randomAccessFile.close();
////                }
//                if (outputStream != null) {
//                    outputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }

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

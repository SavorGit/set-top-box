package com.savor.ads.log;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.savor.ads.BuildConfig;
import com.savor.ads.core.Session;
import com.savor.ads.oss.OSSValues;
import com.savor.ads.oss.OSSUtils;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;

import java.io.File;
import java.text.ParseException;
import java.util.Hashtable;

public class LogUploadService {

    private final static String TAG = "LogUploadSer";
    private static Hashtable<String, String> mLogLocalList = new Hashtable<String, String>();
    private Context context;
    private Session session;
    private OSS oss;

    public LogUploadService(Context context) {
        this.context = context;
        session = Session.get(context);

    }

    public void start() {

        new Thread() {
            @Override
            public void run() {

                try {
                    sleep(1000 * 60 * 10);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                //只保留loged目录下面当月以及上月的日志
                File[] files = new File(AppUtils.getFilePath(AppUtils.StorageFile.loged)).listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.getName().endsWith(".blog")) {
                            file.delete();
                            continue;
                        }

                        String name = file.getName();
                        String[] split = name.split("_");
                        String currentMonth = AppUtils.getCurTime("yyyyMM");
                        String logMonth = null;
                        /*if (split.length == 4) {    // 老版日志命名结构，例：43_FCD5D900B8B6_2017061415_12.blog
                            logMonth = split[2].substring(0, 6);
                        } else */if (split.length == 2||split.length==3) {     // 新版日志命名结构，例：FCD5D900B8B6_2017061415.blog
                            logMonth = split[1].substring(0, 6);               //单机：FCD5D900B8B6_2017061415_standalone.blog
                        } else {
                            file.delete();
                            continue;
                        }
                        if (!TextUtils.isEmpty(logMonth)) {
                            int diff = 0;
                            try {
                                diff = AppUtils.calculateMonthDiff(logMonth, currentMonth, "yyyyMM");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            // 删除大于1个月的日志
                            if (diff > 1) {
                                file.delete();
                            }
                        }
                    }
                }
                if (AppUtils.isNetworkAvailable(context)){
//                    uploadLotteryRecordFile();
                    while (true) {
                        uploadFile();
                        uploadQRCodeLogFile();
                        AppUtils.deleteMeetingResourceByDate(context);
                        try {
                            Thread.sleep(1000 * 60 * 10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
        }.start();

    }


    private void uploadFile() {
        File[] files = getAllLogInfo(AppUtils.StorageFile.log);
        if (files != null && files.length > 0) {
            for (File file : files) {
                final String name = file.getName();
                final String path = file.getPath();
                if (file.isFile()) {
                    String[] split = name.split("_");
                    if (split.length != 2 && split.length != 3) {
                        continue;
                    }
                    if (name.endsWith(".zip")){
                        file.delete();
                        continue;
                    }
                    final String time = split[1].substring(0, 10);
                    if (time.equals(AppUtils.getCurTime("yyyyMMddHH"))) {
                        continue;
                    }
                    final String archivePath = path + ".zip";

                    if (!TextUtils.isEmpty(session.getOssAreaId())) {

                        File sourceFile = new File(path);
                        final File zipFile = new File(archivePath);
                        try {
                            AppUtils.zipFile(sourceFile, zipFile, zipFile.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (zipFile.exists()) {
                            String localFilePath = archivePath.substring(1);
                            String ossFilePath = null;
                            if (name.contains(ConstantValues.STANDALONE)){
                                ossFilePath = OSSValues.uploadStandaloneFilePath + session.getOssAreaId() + File.separator +
                                        AppUtils.getCurTime("yyyyMMdd") + File.separator + name + ".zip";
                            }else{
                                ossFilePath = OSSValues.uploadFilePath + session.getOssAreaId() + File.separator +
                                        AppUtils.getCurTime("yyyyMMdd") + File.separator + name + ".zip";
                            }


                            new OSSUtils(context,
                                    BuildConfig.OSS_BUCKET_NAME,
                                    ossFilePath,
                                    localFilePath,
                                    new UploadCallback() {
                                        @Override
                                        public void isSuccessOSSUpload(boolean flag) {
                                            if (flag) {
                                                afterOSSUpload(name, time);
                                            }
                                            if (zipFile.exists()) {
                                                zipFile.delete();
                                            }
                                        }
                                    }).asyncUploadFile();
                        }
                    }
                }
            }
        }

    }

    /**
     * 上传小程序码显示的日志
     */
    private void uploadQRCodeLogFile(){
        File[] files = getAllLogInfo(AppUtils.StorageFile.qrcode_log);
        if (files != null && files.length > 0) {
            for (File file : files) {
                final String name = file.getName();
                final String path = file.getPath();
                if (file.isFile()) {
                    String[] split = name.split("_");

                    final String time = split[1].substring(0, 10);
                    if (time.equals(AppUtils.getCurTime("yyyyMMddHH"))) {
                        continue;
                    }
                    final String archivePath = path + ".zip";

                    if (!TextUtils.isEmpty(session.getOssAreaId())) {

                        File sourceFile = new File(path);
                        final File zipFile = new File(archivePath);
                        try {
                            AppUtils.zipFile(sourceFile, zipFile, zipFile.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (zipFile.exists()) {
                            String localFilePath = archivePath.substring(1, archivePath.length());
                            String ossFilePath = OSSValues.uploadQRCodePath + session.getOssAreaId() + File.separator + AppUtils.getCurTime("yyyyMMdd") + File.separator + name + ".zip";
                            new OSSUtils(context,
                                    BuildConfig.OSS_BUCKET_NAME,
                                    ossFilePath,
                                    localFilePath,
                                    new UploadCallback() {
                                        @Override
                                        public void isSuccessOSSUpload(boolean flag) {
                                            if (flag) {
//                                                afterOSSUpload(name, time);
                                            }
                                            if (zipFile.exists()) {
                                                zipFile.delete();
                                            }
                                        }
                                    }).asyncUploadFile();
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取log目录下所有日志
     */
    private File[] getAllLogInfo(AppUtils.StorageFile storage) {
        String path = AppUtils.getFilePath(storage);
        File[] files = new File(path).listFiles();
        if (files == null || files.length <= 0)
            return null;
        for (File f : files) {
            if (f.isFile() && f.exists()) {
                String filePath = f.getPath();
                String fileName = f.getName();
                if (fileName.contains(".zip")) {
                    f.delete();
                    continue;
                }
            }
        }
        files = new File(path).listFiles();
        return files;
    }

    private void afterOSSUpload(String fileName, String time) {
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(time)) {
            return;
        }
        String filepath = AppUtils.getFilePath(AppUtils.StorageFile.log) + fileName;
        String currentTime = AppUtils.getCurTime("yyyyMMddHH");
        if (!time.equals(currentTime) && new File(filepath).exists()) {
            String deskPath = AppUtils.getFilePath(AppUtils.StorageFile.loged);
            new File(filepath).renameTo(new File(deskPath + fileName));
        }

    }


    public interface UploadCallback {
        void isSuccessOSSUpload(boolean flag);
    }


    public  void copyLogToUSBDriver(AppUtils.StorageFile storageFile){
        String path = AppUtils.getFilePath(storageFile);
        switch (storageFile){
            case log:

                break;
            case loged:

                break;
        }
    }

}

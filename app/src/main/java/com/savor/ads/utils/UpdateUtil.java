package com.savor.ads.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;


import com.amlogic.update.OtaUpgradeUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.savor.ads.BuildConfig;
import com.savor.ads.bean.BoxInitResult;
import com.savor.ads.bean.JsonBean;
import com.savor.ads.bean.ServerInfo;
import com.savor.ads.bean.UpgradeInfo;
import com.savor.ads.bean.UpgradeResult;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.okhttp.coreProgress.download.ProgressDownloader;
import com.savor.ads.oss.OSSUtils;
import com.savor.ads.oss.OSSValues;


import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

public class UpdateUtil{

    private Context mContext;
    private Session session;
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    public UpdateUtil(Context context) {
        mContext = context;
        session = Session.get(context);
    }

    /**
     * 获取升级信息
     * @return
     */
    public UpgradeInfo getUpgradeInfoFromServer(){
        UpgradeInfo upgradeInfo = null;
        try {
            JsonBean jsonBean = AppApi.upgradeInfo(mContext, apiRequestListener, session.getVersionCode());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code") != AppApi.HTTP_RESPONSE_STATE_SUCCESS) {
                LogUtils.d("接口返回的状态不对,code=" + jsonObject.getInt("code"));
                return upgradeInfo;
            }
            Object result = gson.fromJson(jsonObject.getJSONObject("result").toString(), new TypeToken<UpgradeInfo>() {
            }.getType());
            if (result instanceof UpgradeInfo){
                upgradeInfo =  (UpgradeInfo)result;
                if (ConstantValues.VIRTUAL.equals(jsonBean.getSmallType())){
                    upgradeInfo.setVirtual(true);
                }else{
                    upgradeInfo.setVirtual(false);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return upgradeInfo;
    }

    /**
     * 下载apk文件
     * @param oss_url
     * @param basePath
     * @param apkName
     * @return
     */
    public boolean dowloadApkFile(String oss_url,String basePath,String apkName){
        try {
            return new ProgressDownloader(mContext,oss_url,basePath, apkName,true).downloadByRange();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //处理升级结果
    public void handleUpdateResult(File file,String md5){
        byte[] fRead;
        String md5Value = null;
        try {
            fRead = FileUtils.readFileToByteArray(file);
            md5Value = AppUtils.getMD5(fRead);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //比较本地文件MD5是否与服务器文件一致，如果一致则启动安装
        String fileName = file.getName();
        if (ConstantValues.ROM_DOWNLOAD_FILENAME.equals(fileName)) {
            if (md5Value != null && md5Value.equals(md5)) {
                //升级ROM
                if (!AppUtils.isMstar()) {
                    updateRom(file);
                }
            }
        } else if (ConstantValues.APK_DOWNLOAD_FILENAME.equals(fileName)) {
            if (md5Value != null && md5Value.equals(md5)) {
                //升级APK
                if (AppUtils.isMstar()) {
                    updateApk(file);
                } else if(AppUtils.isGiec()){
                    updateApk4Giec(file);
                }else if (AppUtils.isSVT()){
                    updateApk4SVT(file);
                }
            }
        }
    }

    public static boolean updateApk(File file) {
        if (file.length() <= 0) {
            file.delete();
            LogFileUtil.writeException(new Throwable("apk update fatal, updateapksamples.apk length is 0"));
            return false;
        }

        boolean isflag = false;
        try {
            Process proc = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
            try {
                dos.writeBytes("mount -o remount rw /system\n");
                dos.flush();
                String catCommand = "cat " + file.getPath() + " > /system/app/1.apk\n";
                dos.writeBytes(catCommand);
                dos.flush();

                Thread.sleep(5000);
                File file1 = new File("/system/app/1.apk");
                if (file1.length() > 0) {
//                    dos.writeBytes("rm -r " + file.getPath() + "\n");
//                    dos.flush();
                    dos.writeBytes("mv /system/app/1.apk /system/app/savormedia.apk\n");
                    dos.flush();
                    Thread.sleep(1000);
//                        dos.writeBytes("reboot\n");
//                        dos.flush();
                    isflag = true;
                } else {
                    file.delete();
                    file1.delete();
                    LogFileUtil.writeException(new Throwable("apk update fatal, 1.apk length is 0"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dos.close();
            }

            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            proc.destroy();

            if (isflag) {
                ShellUtils.reboot();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return isflag;
    }

    public static boolean updateApk4Giec(File file) {
        if (file.length() <= 0) {
            file.delete();
            LogFileUtil.writeException(new Throwable("apk update fatal, updateapksamples.apk length is 0"));
            return false;
        }

        boolean isflag = false;
        Process proc = null;
        String targetPath = ConstantValues.APK_INSTALLED_PATH_GIEC + "savormedia.apk";
        try {
            proc = Runtime.getRuntime().exec("su");
            try {
                DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
                dos.writeBytes("mount -o remount,rw /system\n");
                dos.flush();

                String catCommand = "cat " + file.getPath() + " > " + targetPath + "\n";
                dos.writeBytes(catCommand);
                dos.flush();
                Thread.sleep(2000);

                dos.writeBytes("chmod 755 " + targetPath + "\n");
                dos.flush();
                Thread.sleep(1000);
                isflag = true;

//                String catCommand = "cat " + file.getPath() + " > " + tempPath + "\n";
//                dos.writeBytes(catCommand);
//                dos.flush();
//                Thread.sleep(2000);
//
////                file.delete();
//
//                File file1 = new File(tempPath);
//                if (file1.length() > 0) {
//
//                    dos.writeBytes("mv " + tempPath + " " + targetPath + "\n");
//                    dos.flush();
//                    Thread.sleep(1000);
//
//                    dos.writeBytes("chmod 755 " + targetPath + "\n");
//                    dos.flush();
//                    Thread.sleep(1000);
//                    isflag = true;
//                } else {
//                    file1.delete();
//                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (proc != null) {
                try {
                    proc.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (isflag) {
            ShellUtils.reboot();
        }
        return isflag;
    }

    public static boolean updateApk4SVT(File file) {
        if (file.length() <= 0) {
            file.delete();
            LogFileUtil.writeException(new Throwable("apk update fatal, updateapksamples.apk length is 0"));
            return false;
        }

        boolean isflag = false;
        Process proc = null;
        String targetPath = ConstantValues.APK_INSTALLED_PATH_SVT + "savormedia.apk";
        try {
            proc = Runtime.getRuntime().exec("su");
            try {
                DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
                dos.writeBytes("mount -o remount,rw /system\n");
                dos.flush();

                String catCommand = "cat " + file.getPath() + " > " + targetPath + "\n";
                dos.writeBytes(catCommand);
                dos.flush();
                Thread.sleep(2000);

                dos.writeBytes("chmod 755 " + targetPath + "\n");
                dos.flush();
                Thread.sleep(1000);
                isflag = true;

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                LogFileUtil.writeException(new Throwable(e.getMessage()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogFileUtil.writeException(new Throwable(e.getMessage()));
        } finally {
            if (proc != null) {
                try {
                    proc.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (isflag) {
            ShellUtils.reboot();
        }
        return isflag;
    }




    private void updateRom(final File file) {
        if (file == null || !file.exists()) {
            return;
        }
        final GiecUpdateSystem giecUpdateSystem = new GiecUpdateSystem(mContext);
        final OtaUpgradeUtils otaUpgradeUtils = new OtaUpgradeUtils(mContext);
        final int updateMode = giecUpdateSystem.createAmlScript(file.getAbsolutePath(), false, false);
        if (giecUpdateSystem != null) {
            giecUpdateSystem.write2File();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                giecUpdateSystem.copyBKFile();
                otaUpgradeUtils.setDeleteSource(false);
                otaUpgradeUtils.upgrade(file, progressListener, updateMode);
            }
        }).start();
    }

    ApiRequestListener apiRequestListener = new ApiRequestListener() {
        @Override
        public void onSuccess(AppApi.Action method, Object obj) {

        }

        @Override
        public void onError(AppApi.Action method, Object obj) {

        }

        @Override
        public void onNetworkFailed(AppApi.Action method) {

        }
    };

    OtaUpgradeUtils.ProgressListener progressListener = new OtaUpgradeUtils.ProgressListener() {
        @Override
        public void onProgress(int i) {

        }

        @Override
        public void onVerifyFailed(int i, Object o) {

        }

        @Override
        public void onCopyProgress(int i) {

        }

        @Override
        public void onCopyFailed(int i, Object o) {

        }

        @Override
        public void onStopProgress(int i) {

        }
    };
}

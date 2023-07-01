package com.savor.ads.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;


import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.amlogic.update.OtaUpgradeUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.savor.ads.BuildConfig;
import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.activity.MainActivity;
import com.savor.ads.activity.PartakeDishDrawActivity;
import com.savor.ads.bean.JsonBean;
import com.savor.ads.bean.UpgradeInfo;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.okhttp.coreProgress.download.FileDownloader;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

public class UpdateUtil{
    private static final String PACKAGE_INSTALLED_ACTION = "com.savor.ads.install";
    private static final String TAG = UpdateUtil.class.getName();
    private static Context mContext;
    private Session session;
    private static Handler handler = new Handler(Looper.getMainLooper());
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
            return new FileDownloader(mContext,oss_url,basePath, apkName,true).downloadByRange();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //处理升级结果
    @SuppressLint("NewApi")
    public void handleUpdateResult(File file, String md5){
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
        if (ConstantValues.APK_DOWNLOAD_FILENAME.equals(fileName)) {
            if (md5Value != null && md5Value.equals(md5)) {
                //升级APK
                if (AppUtils.isMstar()) {
                    updateApk(file);
                } else if(AppUtils.isGiec()){
                    updateApk4Giec(file);
                }else if (AppUtils.isWang()){
                    updateApk4Trump4k(file);
                }else if (AppUtils.isSMART_CLOUD_TV()||AppUtils.isAmv()){
                    updateApk4SmartTV(file);
                }else if (AppUtils.isSVT()){
                    updateApk4SVT(file);
                }else if (AppUtils.isPhilips()){
                    updateApk4Philips(file);
                }else{
                    updateApk4SmartTV(file);
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
        String tempTargetPath = ConstantValues.APK_INSTALLED_PATH_GIEC + "tempSavormedia.apk";
        String targetPath = ConstantValues.APK_INSTALLED_PATH_GIEC + "savormedia.apk";
        try {
            proc = Runtime.getRuntime().exec("su");
            try {
                DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
                dos.writeBytes("mount -o remount,rw /system\n");
                dos.flush();

                String catCommand = "cat " + file.getPath() + " > " + tempTargetPath + "\n";
                dos.writeBytes(catCommand);
                dos.flush();
                Thread.sleep(3000);

                dos.writeBytes("chmod 755 " + tempTargetPath + "\n");
                dos.flush();
                Thread.sleep(3000);

                File tempTargetFile = new File(tempTargetPath);
                if (file.length()==tempTargetFile.length()){
                    dos.writeBytes("mv " + tempTargetPath +" "+ targetPath + "\n");
                    dos.flush();
                    Thread.sleep(3000);
                    isflag = true;
                }
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
    //王牌4K
    public static boolean updateApk4Trump4k(File file) {
        if (GlobalValues.isUpdateApk){
            return false;
        }
        if (file.length() <= 0) {
            file.delete();
            LogFileUtil.writeException(new Throwable("apk update fatal, updateapksamples.apk length is 0"));
            return false;
        }
        boolean isflag = false;
        String apkPath = file.getAbsolutePath();
//        String[] args = {"pm","install", "-r",apkPath};
        String[] args = {"pm","install","-r",apkPath,"am","start","-n","com.savor.ads/.activity.MainActivity"};
        // 创建一个操作系统进程并执行命令行操作
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        try {
            process = processBuilder.start();
            successResult = new BufferedReader (new InputStreamReader(process.getInputStream ()));
            errorResult = new BufferedReader (new InputStreamReader(process.getErrorStream ()));
            String s ;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
            if (process.waitFor() == 0 || successMsg.toString().contains("Success")){
                isflag = true;
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            try {
                if (successResult != null){
                    successResult.close();
                }
                if (errorResult != null){
                    errorResult.close();
                }
            } catch ( IOException e){
                e.printStackTrace();
            }
            if (process != null){
                process. destroy();
            }
        }
        if (isflag) {
            GlobalValues.isUpdateApk = true;
            handler.post(()->ShowMessage.showToast(mContext,"新版本更新完成"));
//            mContext.sendBroadcast(new Intent(ConstantValues.UPDATE_APK_ACTION));
            try {
                Runtime.getRuntime().exec("reboot");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isflag;
    }
    //多视彩，智慧云
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void updateApk4SmartTV(File file) {
        installPackage(file.getAbsoluteFile());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void installPackage(File apkFilePath) {

        Log.w(TAG, "installPackage pkg: " + apkFilePath);
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        PackageInstaller.Session session = null;
        // 创建一个Session
        try {
            int sessionId = getPi().createSession(params);
            // 建立和PackageManager的socket通道，Android中的通信不仅仅有Binder还有很多其它的
            session = getPi().openSession(sessionId);
        } catch (IOException e) {
            e.printStackTrace();
//            return PackageManager.INSTALL_FAILED_INVALID_APK;
        }
        addApkToInstallSession(apkFilePath, session);
        // Create an install status receiver.
//        Context context = InstallApkSessionApi.this;
        Intent intent = new Intent(mContext, UpdateUtil.class);
        intent.setAction(PACKAGE_INSTALLED_ACTION);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        IntentSender statusReceiver = pendingIntent.getIntentSender();
        // Commit the session (this will start the installation workflow).
        session.commit(statusReceiver);
//        final LocalIntentReceiver localReceiver = new LocalIntentReceiver();
//        session.commit(localReceiver.getIntentSender());
//        final Intent result = localReceiver.getResult();
//        synchronized (localReceiver) {
//            final int status = result.getIntExtra(PackageInstaller.EXTRA_STATUS,PackageInstaller.STATUS_FAILURE);
//            if (session != null) {
//                session.close();
//            }
//            if (status != PackageInstaller.STATUS_SUCCESS) {
//                Log.e(TAG, "Installation should have succeeded, but got code "+ status);
//                return status;
//            } else {
//                Log.e(TAG, "Installation  have succeeded");
//                return status;
//            }
//        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static boolean addApkToInstallSession(File apkFilePath, PackageInstaller.Session session) {
        InputStream in = null;
        OutputStream out = null;
        boolean success = false;
        try {
            out = session.openWrite("updateapksamples.apk", 0, apkFilePath.length());
            in = new FileInputStream(apkFilePath);
            int total = 0, c;
            byte[] buffer = new byte[1024 * 1024];
            while ((c = in.read(buffer)) != -1) {
                total += c;
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            Log.d(TAG, "streamed " + total + " bytes");
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != session) {
                session.close();
            }
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

//    private static class LocalIntentReceiver {
//        private final SynchronousQueue<Intent> mResult = new SynchronousQueue<>();
//
//        private IIntentSender.Stub mLocalSender = new IIntentSender.Stub() {
//            @Override
//            public int send(int code, Intent intent, String resolvedType, IIntentReceiver iIntentReceiver, String s1) {
//                try {
//                    mResult.offer(intent, 5, TimeUnit.SECONDS);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                return code;
//            }
//        };
//
//        public IntentSender getIntentSender() {
//            return new IntentSender((IIntentSender) mLocalSender);
//        }
//
//        public Intent getResult() {
//            try {
//                return mResult.take();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

    private static PackageManager getPm() {
        return mContext.getPackageManager();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static PackageInstaller getPi() {
        return getPm().getPackageInstaller();
    }

    public static boolean updateApk4SVT(File file) {
        if (file.length() <= 0) {
            file.delete();
            LogFileUtil.writeException(new Throwable("apk update fatal, updateapksamples.apk length is 0"));
            return false;
        }

        boolean isflag = false;
        Process proc = null;
        String targetPath = ConstantValues.APK_INSTALLED_PATH_APP + "savormedia.apk";
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

    public void updateApk4Philips(File file){
        Intent intent = new Intent();
        intent.setAction("cms.intent.action.UPDATE_APK");
        intent.putExtra("filePath", file.getAbsolutePath());
        intent.putExtra("keep", true);
        intent.putExtra("packageName", "com.savor.ads");
        intent.putExtra("activityName", "com.savor.ads.activity.MainActivity");
        mContext.sendBroadcast(intent);
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

package com.savor.ads;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.jar.savor.box.ServiceUtil;
import com.jar.savor.box.services.RemoteService;
import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.ResponseErrorMessage;
import com.savor.ads.core.Session;
import com.savor.ads.service.UMessageIntentService;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.FileUtils;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.GoodsCountdownQrCodeWindowManager;
import com.savor.ads.utils.KeyCode;
import com.savor.ads.utils.KeyCodeConstant;
import com.savor.ads.utils.KeyCodeConstantGiec;
import com.savor.ads.utils.KeyCodeConstantSVT;
import com.savor.ads.utils.KeyCodeConstantY55C;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.MiniProgramQrCodeWindowManager;
import com.savor.ads.utils.QrCodeWindowManager;
import com.savor.ads.utils.GoodsQrCodeWindowManager;
import com.savor.ads.utils.ShowMessage;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import java.io.DataOutputStream;
import java.io.File;

/**
 * Created by Administrator on 2016/12/9.
 */

public class SavorApplication extends MultiDexApplication implements ApiRequestListener {

    private QrCodeWindowManager mQrCodeWindowManager;
    private MiniProgramQrCodeWindowManager miniProgramQrCodeWindowManager;
    private GoodsQrCodeWindowManager goodsQrCodeWindowManager;
    private GoodsCountdownQrCodeWindowManager goodsCountdownQrCodeWindowManager;
    private ServiceConnection mConnection;
    private Context context;
    private Session session;
    private Handler mHandler = new Handler();
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        // 设置异常捕获处理类
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        // 初始化文件记录类
        LogFileUtil.init();
        // 映射真实健值
        mappingKeyCode();
        session = Session.get(context);
        session.setHeartbeatMiniNetty(false);
        mQrCodeWindowManager = new QrCodeWindowManager();
        miniProgramQrCodeWindowManager = MiniProgramQrCodeWindowManager.get(this);
        goodsQrCodeWindowManager = GoodsQrCodeWindowManager.get(this);
        goodsCountdownQrCodeWindowManager = new GoodsCountdownQrCodeWindowManager(this);
        // 启动投屏类操作处理的Service
//        startScreenProjectionService();
        registerActivityLifecycle();
        // 检测播放时间
        AppUtils.checkPlayTime(SavorApplication.this);
        initPush();
    }

    private void initPush() {
        if (AppUtils.isMstar()||AppUtils.isSVT()) {
            initMStarPush();
        } else if (AppUtils.isGiec()){
            initGiecPush();
        }else if (AppUtils.isLeTV()){
            initLetvPush();
        }
    }

    private void initMStarPush() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                boolean isCopySuccess;
                if (getPackageName().equals(AppUtils.getProcessName(SavorApplication.this)) &&
                        !GlobalValues.IS_UPUSH_SO_COPY_SUCCESS) {
                    File innerLibDir = new File("/sdcard/inner_so/");
                    if (!innerLibDir.exists()) {
                        innerLibDir.mkdirs();
                    }
                    if (innerLibDir.exists()) {
                        FileUtils.copyFilesFromAssets(SavorApplication.this, "inner_so/", innerLibDir.getPath());
                    }

                    Process proc = null;
                    try {
                        proc = Runtime.getRuntime().exec("su");

                        DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
                        dos.writeBytes("mount -o remount,rw /system\n");
                        dos.flush();

                        dos.writeBytes("busybox cp /sdcard/inner_so/* /data/data/com.savor.ads/lib/\n");
                        dos.flush();
                        Thread.sleep(2000);

                        dos.writeBytes("chmod -R 755 /data/data/com.savor.ads/lib/\n");
                        dos.flush();
                        Thread.sleep(200);

                        dos.close();

                        GlobalValues.IS_UPUSH_SO_COPY_SUCCESS = true;
                        isCopySuccess = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        GlobalValues.IS_UPUSH_SO_COPY_SUCCESS = false;
                        isCopySuccess = false;
                    } finally {
                        if (proc != null) {
                            try {
                                proc.destroy();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    isCopySuccess = true;
                }

                LogFileUtil.write("Copy so file success? " + isCopySuccess);
                if (isCopySuccess) {
                    LogUtils.d("copy so success");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            PushAgent pushAgent = PushAgent.getInstance(SavorApplication.this);
                            pushAgent.setPushIntentServiceClass(UMessageIntentService.class);
                            //注册推送服务，每次调用register方法都会回调该接口
                            pushAgent.register(new IUmengRegisterCallback() {

                                @Override
                                public void onSuccess(String deviceToken) {
                                    //注册成功会返回device token
                                    Log.e("register", "UPush register success, deviceToken is " + deviceToken);
                                    LogFileUtil.write("UPush register success, deviceToken is " + deviceToken);

                                    GlobalValues.IS_UPUSH_REGISTER_SUCCESS = true;
                                    reportDeviceToken(deviceToken);

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            PushAgent.getInstance(SavorApplication.this).onAppStart();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(String s, String s1) {
                                    Log.e("register", "UPush register failed, s is " + s + ", s1 is " + s1);
                                    LogFileUtil.write("UPush register failed, s is " + s + ", s1 is " + s1);
                                    GlobalValues.IS_UPUSH_REGISTER_SUCCESS = false;
                                }
                            });
                        }
                    });
                }
            }
        }).start();
    }

    private void initGiecPush() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                boolean isCopySuccess = false;
                if (getPackageName().equals(AppUtils.getProcessName(SavorApplication.this)) &&
                        !GlobalValues.IS_UPUSH_SO_COPY_SUCCESS) {
                    File innerLibDir = new File("/sdcard/inner_so/"/*ConstantValues.APK_INSTALLED_PATH + "lib/arm/"*/);
                    if (!innerLibDir.exists()) {
                        innerLibDir.mkdirs();
                    }
                    if (innerLibDir.exists()) {
                        FileUtils.copyFilesFromAssets(SavorApplication.this, "inner_so/", innerLibDir.getPath());
                    }

                    Process proc = null;
                    try {
                        proc = Runtime.getRuntime().exec("su");

                        DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
                        dos.writeBytes("mount -o remount,rw /system\n");
                        dos.flush();

                        dos.writeBytes("mkdir /system/priv-app/savormedia/lib/\n");
                        dos.flush();
                        Thread.sleep(100);

                        dos.writeBytes("mkdir /system/priv-app/savormedia/lib/arm/\n");
                        dos.flush();
                        Thread.sleep(100);

                        dos.writeBytes("cp /sdcard/inner_so/* /system/priv-app/savormedia/lib/arm/\n");
                        dos.flush();
                        Thread.sleep(2000);

                        dos.writeBytes("chmod -R 755 /system/priv-app/savormedia/lib/\n");
                        dos.flush();
                        Thread.sleep(200);

                        //                        dos.writeBytes("cp /sdcard/outer_so/ /system/lib/\n");
                        //                        dos.flush();
                        //                        Thread.sleep(2000);

                        dos.close();

                        GlobalValues.IS_UPUSH_SO_COPY_SUCCESS = true;
                        isCopySuccess = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        GlobalValues.IS_UPUSH_SO_COPY_SUCCESS = false;
                        isCopySuccess = false;
                    } finally {
                        if (proc != null) {
                            try {
                                proc.destroy();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    isCopySuccess = true;
                }

                LogFileUtil.write("Copy so file success? " + isCopySuccess);
                if (isCopySuccess) {
                    LogUtils.d("copy so success");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            PushAgent pushAgent = PushAgent.getInstance(SavorApplication.this);
                            pushAgent.setPushIntentServiceClass(UMessageIntentService.class);
                            //注册推送服务，每次调用register方法都会回调该接口
                            pushAgent.register(new IUmengRegisterCallback() {

                                @Override
                                public void onSuccess(String deviceToken) {
                                    //注册成功会返回device token
                                    Log.e("register", "UPush register success, deviceToken is " + deviceToken);
                                    LogFileUtil.write("UPush register success, deviceToken is " + deviceToken);

                                    GlobalValues.IS_UPUSH_REGISTER_SUCCESS = true;
                                    reportDeviceToken(deviceToken);

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            PushAgent.getInstance(SavorApplication.this).onAppStart();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(String s, String s1) {
                                    Log.e("register", "UPush register failed, s is " + s + ", s1 is " + s1);
                                    LogFileUtil.write("UPush register failed, s is " + s + ", s1 is " + s1);
                                    GlobalValues.IS_UPUSH_REGISTER_SUCCESS = false;
                                }
                            });
                        }
                    });
                }
            }
        }).start();
    }

    private void initLetvPush(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                PushAgent pushAgent = PushAgent.getInstance(SavorApplication.this);
                pushAgent.setPushIntentServiceClass(UMessageIntentService.class);
                //注册推送服务，每次调用register方法都会回调该接口
                pushAgent.register(new IUmengRegisterCallback() {

                    @Override
                    public void onSuccess(String deviceToken) {
                        //注册成功会返回device token
                        Log.e("register", "UPush register success, deviceToken is " + deviceToken);
                        LogFileUtil.write("UPush register success, deviceToken is " + deviceToken);

                        GlobalValues.IS_UPUSH_REGISTER_SUCCESS = true;
                        reportDeviceToken(deviceToken);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                PushAgent.getInstance(SavorApplication.this).onAppStart();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String s, String s1) {
                        Log.e("register", "UPush register failed, s is " + s + ", s1 is " + s1);
                        LogFileUtil.write("UPush register failed, s is " + s + ", s1 is " + s1);
                        GlobalValues.IS_UPUSH_REGISTER_SUCCESS = false;
                    }
                });
            }
        });
    }

    private void reportDeviceToken(String deviceToken) {
        AppApi.reportDeviceToken(this, this, deviceToken);
    }

    /**
     * 显示二维码
     */
    public void showQrCodeWindow(String code) {
        if (TextUtils.isEmpty(code)) {
            code = Session.get(this).getAuthCode();
        }
        mQrCodeWindowManager.showQrCode(this, code);
    }

    /**
     * 显示小程序二维码
     */
    public void showMiniProgramQrCodeWindow(int QRCodeType) {
        String box_mac = Session.get(this).getEthernetMac();
        String path=null;
        if (QRCodeType==8){
            path = AppUtils.getFilePath( AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_NAME;
        }else if (QRCodeType==12){
            path = AppUtils.getFilePath( AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_BIG_NAME;
        }else if (QRCodeType==13){
            path = AppUtils.getFilePath( AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_CALL_NAME;
        }else if (QRCodeType==15){
            path = AppUtils.getFilePath( AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_NEW_NAME;
        }else if (QRCodeType==16){
            path = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_NAME;
        }else if (QRCodeType==19){
            path = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_NEW_NAME;
        }else if (QRCodeType==20){
            path = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_BIG_NAME;
        }else if (QRCodeType==21){
            path = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_CALL_NAME;
        }
        String url;
        if (QRCodeType==16
                ||QRCodeType==19
                ||QRCodeType==20
                ||QRCodeType==21){
            url = AppApi.API_URLS.get(AppApi.Action.CP_SIMPLE_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+QRCodeType;
        }else{
            url = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+QRCodeType;
        }
        LogUtils.i("showMiniProgramQrCodeWindow.................."+url);
        LogFileUtil.write("showMiniProgramQrCodeWindow.................."+url);
        if (!TextUtils.isEmpty(url)){
            miniProgramQrCodeWindowManager.showQrCode(this,url,path,QRCodeType);
        }
    }

    public void hideMiniProgramQrCodeWindow() {
        LogUtils.i("closeMiniProgramQrCodeWindow..................");
        miniProgramQrCodeWindowManager.hideQrCode();
    }

    /**
     * 展示活动商品码
     */
    public void showGoodsQrCodeWindow(String goodsQrcodeUrl,String goodsQrcodePath){
        goodsQrCodeWindowManager.showQrCode(this,goodsQrcodeUrl,goodsQrcodePath);
    }

    public void hideGoodsQrCodeWindow(){
        goodsQrCodeWindowManager.hideQrCode();
    }

    public void showGoodsCountdownQrCodeWindow(String goodsQrcodeUrl,String goodsQrcodePath,long countdownTime){
        goodsCountdownQrCodeWindowManager.showQrCode(this,goodsQrcodeUrl,goodsQrcodePath,countdownTime);
    }

    public void hideGoodsCountdownQrCodeWindow(){
        goodsCountdownQrCodeWindowManager.hideQrCode();
    }

    public void stopScreenProjectionService() {
        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    /**
     * 映射真实健值
     */
    private void mappingKeyCode() {
        if (AppUtils.isMstar()) {
            KeyCode.KEY_CODE_BACK = KeyCodeConstant.KEY_CODE_BACK;
            KeyCode.KEY_CODE_CHANGE_MODE = KeyCodeConstant.KEY_CODE_CHANGE_MODE;
//            KeyCode.KEY_CODE_CHANGE_RESOLUTION = KeyCodeConstant.KEY_CODE_CHANGE_RESOLUTION;
            KeyCode.KEY_CODE_CHANGE_SIGNAL = KeyCodeConstant.KEY_CODE_CHANGE_SIGNAL;
            KeyCode.KEY_CODE_CHANNEL_LIST = KeyCodeConstant.KEY_CODE_CHANNEL_LIST;
            KeyCode.KEY_CODE_DOWN = KeyCodeConstant.KEY_CODE_DOWN;
            KeyCode.KEY_CODE_LEFT = KeyCodeConstant.KEY_CODE_LEFT;
            KeyCode.KEY_CODE_MANUAL_HEARTBEAT = KeyCodeConstant.KEY_CODE_MANUAL_HEARTBEAT;
            KeyCode.KEY_CODE_NEXT_ADS = KeyCodeConstant.KEY_CODE_NEXT_ADS;
            KeyCode.KEY_CODE_PLAY_PAUSE = KeyCodeConstant.KEY_CODE_PLAY_PAUSE;
            KeyCode.KEY_CODE_PREVIOUS_ADS = KeyCodeConstant.KEY_CODE_PREVIOUS_ADS;
            KeyCode.KEY_CODE_RIGHT = KeyCodeConstant.KEY_CODE_RIGHT;
            KeyCode.KEY_CODE_SETTING = KeyCodeConstant.KEY_CODE_SETTING;
            KeyCode.KEY_CODE_SHOW_APP_INSTALLED = KeyCodeConstant.KEY_CODE_SHOW_APP_INSTALLED;
            KeyCode.KEY_CODE_SHOW_INFO = KeyCodeConstant.KEY_CODE_SHOW_INFO;
            KeyCode.KEY_CODE_SHOW_QRCODE = KeyCodeConstant.KEY_CODE_SHOW_QRCODE;
            KeyCode.KEY_CODE_SYSTEM_SETTING = KeyCodeConstant.KEY_CODE_SYSTEM_SETTING;
            KeyCode.KEY_CODE_UP = KeyCodeConstant.KEY_CODE_UP;
            KeyCode.KEY_CODE_UPLOAD_CHANNEL_INFO = KeyCodeConstant.KEY_CODE_UPLOAD_CHANNEL_INFO;
            KeyCode.KEY_CODE_UDISK_UPDATE = KeyCodeConstant.KEY_CODE_UDISK_UPDATE;
            KeyCode.KEY_CODE_UDISK_COPY = KeyCodeConstant.KEY_CODE_UDISK_COPY;
            KeyCode.KEY_CODE_SHOW_PLAYLIST = KeyCodeConstant.KEY_CODE_SHOW_PLAYLIST;
        } else if (AppUtils.isLeTV()){
            KeyCode.KEY_CODE_BACK = KeyCodeConstantY55C.KEY_CODE_BACK;
            KeyCode.KEY_CODE_CHANGE_MODE = KeyCodeConstantY55C.KEY_CODE_CHANGE_MODE;
            KeyCode.KEY_CODE_CHANGE_SIGNAL = KeyCodeConstantY55C.KEY_CODE_CHANGE_SIGNAL;
            KeyCode.KEY_CODE_CHANNEL_LIST = KeyCodeConstantY55C.KEY_CODE_CHANNEL_LIST;
            KeyCode.KEY_CODE_DOWN = KeyCodeConstantY55C.KEY_CODE_DOWN;
            KeyCode.KEY_CODE_LEFT = KeyCodeConstantY55C.KEY_CODE_LEFT;
            KeyCode.KEY_CODE_MANUAL_HEARTBEAT = KeyCodeConstantY55C.KEY_CODE_MANUAL_HEARTBEAT;
            KeyCode.KEY_CODE_NEXT_ADS = KeyCodeConstantY55C.KEY_CODE_NEXT_ADS;
            KeyCode.KEY_CODE_PLAY_PAUSE = KeyCodeConstantY55C.KEY_CODE_PLAY_PAUSE;
            KeyCode.KEY_CODE_PREVIOUS_ADS = KeyCodeConstantY55C.KEY_CODE_PREVIOUS_ADS;
            KeyCode.KEY_CODE_RIGHT = KeyCodeConstantY55C.KEY_CODE_RIGHT;
            KeyCode.KEY_CODE_SETTING = KeyCodeConstantY55C.KEY_CODE_SETTING;
            KeyCode.KEY_CODE_SHOW_APP_INSTALLED = KeyCodeConstantY55C.KEY_CODE_SHOW_APP_INSTALLED;
            KeyCode.KEY_CODE_SHOW_INFO = KeyCodeConstantY55C.KEY_CODE_SHOW_INFO;
            KeyCode.KEY_CODE_SHOW_QRCODE = KeyCodeConstantY55C.KEY_CODE_SHOW_QRCODE;
            KeyCode.KEY_CODE_SYSTEM_SETTING = KeyCodeConstantY55C.KEY_CODE_SYSTEM_SETTING;
            KeyCode.KEY_CODE_UP = KeyCodeConstantY55C.KEY_CODE_UP;
            KeyCode.KEY_CODE_UPLOAD_CHANNEL_INFO = KeyCodeConstantY55C.KEY_CODE_UPLOAD_CHANNEL_INFO;
            KeyCode.KEY_CODE_UDISK_UPDATE = KeyCodeConstantY55C.KEY_CODE_UDISK_UPDATE;
            KeyCode.KEY_CODE_UDISK_COPY = KeyCodeConstantY55C.KEY_CODE_UDISK_COPY;
            KeyCode.KEY_CODE_SHOW_PLAYLIST = KeyCodeConstantY55C.KEY_CODE_SHOW_PLAYLIST;
        } else if (AppUtils.isSVT()){
            KeyCode.KEY_CODE_BACK = KeyCodeConstantSVT.KEY_CODE_BACK;
            KeyCode.KEY_CODE_CHANGE_MODE = KeyCodeConstantSVT.KEY_CODE_CHANGE_MODE;
            KeyCode.KEY_CODE_CHANGE_RESOLUTION = KeyCodeConstantSVT.KEY_CODE_CHANGE_RESOLUTION;
            KeyCode.KEY_CODE_CHANGE_SIGNAL = KeyCodeConstantSVT.KEY_CODE_CHANGE_SIGNAL;
            KeyCode.KEY_CODE_CHANNEL_LIST = KeyCodeConstantSVT.KEY_CODE_CHANNEL_LIST;
            KeyCode.KEY_CODE_DOWN = KeyCodeConstantSVT.KEY_CODE_DOWN;
            KeyCode.KEY_CODE_LEFT = KeyCodeConstantSVT.KEY_CODE_LEFT;
            KeyCode.KEY_CODE_MANUAL_HEARTBEAT = KeyCodeConstantSVT.KEY_CODE_MANUAL_HEARTBEAT;
            KeyCode.KEY_CODE_NEXT_ADS = KeyCodeConstantSVT.KEY_CODE_NEXT_ADS;
            KeyCode.KEY_CODE_PLAY_PAUSE = KeyCodeConstantSVT.KEY_CODE_PLAY_PAUSE;
            KeyCode.KEY_CODE_PREVIOUS_ADS = KeyCodeConstantSVT.KEY_CODE_PREVIOUS_ADS;
            KeyCode.KEY_CODE_RIGHT = KeyCodeConstantSVT.KEY_CODE_RIGHT;
            KeyCode.KEY_CODE_SETTING = KeyCodeConstantSVT.KEY_CODE_SETTING;
            KeyCode.KEY_CODE_SHOW_APP_INSTALLED = KeyCodeConstantSVT.KEY_CODE_SHOW_APP_INSTALLED;
            KeyCode.KEY_CODE_SHOW_INFO = KeyCodeConstantSVT.KEY_CODE_SHOW_INFO;
            KeyCode.KEY_CODE_SHOW_QRCODE = KeyCodeConstantSVT.KEY_CODE_SHOW_QRCODE;
            KeyCode.KEY_CODE_SYSTEM_SETTING = KeyCodeConstantSVT.KEY_CODE_SYSTEM_SETTING;
            KeyCode.KEY_CODE_UP = KeyCodeConstantSVT.KEY_CODE_UP;
            KeyCode.KEY_CODE_UPLOAD_CHANNEL_INFO = KeyCodeConstantSVT.KEY_CODE_UPLOAD_CHANNEL_INFO;
            KeyCode.KEY_CODE_UDISK_UPDATE = KeyCodeConstantSVT.KEY_CODE_UDISK_UPDATE;
            KeyCode.KEY_CODE_UDISK_COPY = KeyCodeConstantSVT.KEY_CODE_UDISK_COPY;
            KeyCode.KEY_CODE_SHOW_PLAYLIST = KeyCodeConstantSVT.KEY_CODE_SHOW_PLAYLIST;
        }else {
            KeyCode.KEY_CODE_BACK = KeyCodeConstantGiec.KEY_CODE_BACK;
            KeyCode.KEY_CODE_CHANGE_MODE = KeyCodeConstantGiec.KEY_CODE_CHANGE_MODE;
            KeyCode.KEY_CODE_CHANGE_RESOLUTION = KeyCodeConstantGiec.KEY_CODE_CHANGE_RESOLUTION;
            KeyCode.KEY_CODE_CHANGE_SIGNAL = KeyCodeConstantGiec.KEY_CODE_CHANGE_SIGNAL;
            KeyCode.KEY_CODE_CHANNEL_LIST = KeyCodeConstantGiec.KEY_CODE_CHANNEL_LIST;
            KeyCode.KEY_CODE_DOWN = KeyCodeConstantGiec.KEY_CODE_DOWN;
            KeyCode.KEY_CODE_LEFT = KeyCodeConstantGiec.KEY_CODE_LEFT;
            KeyCode.KEY_CODE_MANUAL_HEARTBEAT = KeyCodeConstantGiec.KEY_CODE_MANUAL_HEARTBEAT;
            KeyCode.KEY_CODE_NEXT_ADS = KeyCodeConstantGiec.KEY_CODE_NEXT_ADS;
            KeyCode.KEY_CODE_PLAY_PAUSE = KeyCodeConstantGiec.KEY_CODE_PLAY_PAUSE;
            KeyCode.KEY_CODE_PREVIOUS_ADS = KeyCodeConstantGiec.KEY_CODE_PREVIOUS_ADS;
            KeyCode.KEY_CODE_RIGHT = KeyCodeConstantGiec.KEY_CODE_RIGHT;
            KeyCode.KEY_CODE_SETTING = KeyCodeConstantGiec.KEY_CODE_SETTING;
            KeyCode.KEY_CODE_SHOW_APP_INSTALLED = KeyCodeConstantGiec.KEY_CODE_SHOW_APP_INSTALLED;
            KeyCode.KEY_CODE_SHOW_INFO = KeyCodeConstantGiec.KEY_CODE_SHOW_INFO;
            KeyCode.KEY_CODE_SHOW_QRCODE = KeyCodeConstantGiec.KEY_CODE_SHOW_QRCODE;
            KeyCode.KEY_CODE_SYSTEM_SETTING = KeyCodeConstantGiec.KEY_CODE_SYSTEM_SETTING;
            KeyCode.KEY_CODE_UP = KeyCodeConstantGiec.KEY_CODE_UP;
            KeyCode.KEY_CODE_UPLOAD_CHANNEL_INFO = KeyCodeConstantGiec.KEY_CODE_UPLOAD_CHANNEL_INFO;
            KeyCode.KEY_CODE_UDISK_UPDATE = KeyCodeConstantGiec.KEY_CODE_UDISK_UPDATE;
            KeyCode.KEY_CODE_UDISK_COPY = KeyCodeConstantGiec.KEY_CODE_UDISK_COPY;
            KeyCode.KEY_CODE_SHOW_PLAYLIST = KeyCodeConstantGiec.KEY_CODE_SHOW_PLAYLIST;
        }
    }

    @Override
    public void onSuccess(AppApi.Action method, Object obj) {
        switch (method){
            case CP_POST_DEVICE_TOKEN_JSON:
                LogUtils.d("Report DeviceToken onSuccess!");
                LogFileUtil.write("Report DeviceToken onSuccess!");
                break;
        }

    }

    @Override
    public void onError(AppApi.Action method, Object obj) {
        if (AppApi.Action.CP_POST_DEVICE_TOKEN_JSON.equals(method)) {
            String msg = "";
            if (obj instanceof ResponseErrorMessage) {
                msg = ((ResponseErrorMessage) obj).getMessage();
            }
            LogUtils.d("Report DeviceToken onError! msg is " + msg);
            LogFileUtil.write("Report DeviceToken onError! msg is " + msg);
        }
    }

    @Override
    public void onNetworkFailed(AppApi.Action method) {
        if (AppApi.Action.CP_POST_DEVICE_TOKEN_JSON.equals(method)) {
            LogUtils.d("Report DeviceToken onNetworkFailed!");
            LogFileUtil.write("Report DeviceToken onNetworkFailed!");
        }
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Log.i("SavorApplication123", "APP遁入后台");
            if (AppUtils.isLeTV()||AppUtils.isSVT()){
//                mHandler.postDelayed(mBackToAdsPlayerRunnable, 60 * 1000);
                int switchTime = session.getSwitchTime();
                if (switchTime > 0 && switchTime != 999){
                    // 添加延时切换到广告播放的Runnable, 999被定义为不切换
                    mHandler.postDelayed(mBackToAdsPlayerRunnable, 60 * 1000 * switchTime);
                }
            }
        }
    }

    private Runnable mBackToAdsPlayerRunnable = ()->gotoAdsPlayer();
    private void gotoAdsPlayer() {
        if (GlobalValues.getInstance().PLAY_LIST == null || GlobalValues.getInstance().PLAY_LIST.isEmpty()) {
            // 尝试填充播放列表
            fillPlayList();
        }

        if (GlobalValues.getInstance().PLAY_LIST != null && !GlobalValues.getInstance().PLAY_LIST.isEmpty()) {
            AppUtils.appToFront(context);
        } else {
            ShowMessage.showToast(context, "未发现可播放轮播内容，无法跳转");
        }
    }

    public void fillPlayList() {
        LogUtils.d("开始fillPlayList");
        if (!TextUtils.isEmpty(AppUtils.getMainMediaPath())) {
            AppUtils.fillPlaylist(this, null, 1);
        } else {
            LogFileUtil.writeKeyLogInfo("跳转轮播，未找到SD卡！");
            ShowMessage.showToast(context, "未发现SD卡");
        }
    }

    private void registerActivityLifecycle(){
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mHandler.removeCallbacks(mBackToAdsPlayerRunnable);
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
}

package com.savor.ads.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import androidx.core.app.ActivityCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.savor.ads.BuildConfig;
import com.savor.ads.R;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.ServerInfo;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.database.DBHelper;
import com.savor.ads.log.LogProduceService;
import com.savor.ads.log.LogUploadService;
import com.savor.ads.service.HandleMediaDataService;
import com.savor.ads.service.HeartbeatService;
import com.savor.ads.service.ProjectionService;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.FileUtils;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.KeyCode;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.TimeCalibrateHelper;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by Administrator on 2016/12/6.
 */

public class MainActivity extends BaseActivity {
    Handler mHandler = new Handler();
    private ImageView main_imgIv;
    private static final String KEY_BOOT_VIDEO_FINISH = "boot.video.finish";
    private static final String KEY_BOOT_ANIMATION_FINISH = "boot.animation.finish";
    private static final String KEY_BOOT_STR_AD_FINISH = "sys.play.resumead";
    private static final String VALUE_BOOT_VIDEO_FINISHED = "1";
    private static final String VALUE_BOOT_ANIMATION_FINISHED = "1";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDisplay();

        mSession.setStartTime(AppUtils.getCurTime());

        // 清楚Glide图片缓存
        GlideImageLoader.clearCache(mContext, true, true);
        if (AppUtils.isMstar()){
            mHandler.postDelayed(()->gotoAdsActivity(), 1000*30);
        }else if (AppUtils.isGiec()){
            mHandler.postDelayed(()->gotoAdsActivity(), 1000*5);
        }else if (AppUtils.isLeTV()){
            LogUtils.d("MainActivity++进入乐视电视");
            while(true){
                if (isBootVideoFinished()){
                    LogUtils.d("MainActivity++进入已经播放完开机动画");
                    mHandler.postDelayed(()->gotoAdsActivity(), 1000*5);
                    break;
                }
            }
        }else if (AppUtils.isSVT()){
            verifyStoragePermissions(mContext);
            mHandler.postDelayed(()->gotoAdsActivity(), 1000*5);
            mHandler.postDelayed(()->initMediaVideo(),1000*60);

        }
    }


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        }
    }


    public static boolean isBootVideoFinished() {
        String videoFinished = SystemProperties.get(KEY_BOOT_VIDEO_FINISH);
        String bootAnifinished = SystemProperties.get(KEY_BOOT_ANIMATION_FINISH);
        String strAdFinished = SystemProperties.get(KEY_BOOT_STR_AD_FINISH);
        String isScreenOff = SystemProperties.get("soundbar.le.speaker");
        if (!TextUtils.isEmpty(isScreenOff) && "1".equals(isScreenOff.trim())) {
            if (!TextUtils.isEmpty(strAdFinished) && "3".equals(strAdFinished)) {
                return true;
            }
        }
        if (TextUtils.isEmpty(videoFinished) || VALUE_BOOT_VIDEO_FINISHED.equals(videoFinished.trim())) {
            if (VALUE_BOOT_ANIMATION_FINISHED.equals(bootAnifinished)) {
                return true;
            }
        }
        return false;
    }

    private void gotoAdsActivity() {
        ArrayList<MediaLibBean> tempList = DBHelper.get(this).getTempProList();
        if (tempList != null && tempList.size() > 30) {
            String selection = DBHelper.MediaDBInfo.FieldName.MEDIATYPE + "=? AND " +
                    DBHelper.MediaDBInfo.FieldName.PERIOD + "!=? AND " +
                    DBHelper.MediaDBInfo.FieldName.PERIOD + "!=?";
            String[] args = new String[]{ConstantValues.PRO, mSession.getProPeriod(), mSession.getProDownloadPeriod()};
            DBHelper.get(this).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWPLAYLIST, selection, args);
        }

        fillPlayList();
        LogFileUtil.write("MainActivity:删除旧的视频逻辑");
        AppUtils.deleteOldMedia(mContext,false);
        AppUtils.deleteProjectionData(this);
        AppUtils.deleteIdleDirectory();
        Intent intent = new Intent(mContext, AdsPlayerActivity.class);
        startActivity(intent);
    }

    private void initMediaVideo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String mediaPath = AppUtils.getFilePath(AppUtils.StorageFile.media);
                File fileDirectory = new File(mediaPath);
                if (fileDirectory.exists()&&fileDirectory.listFiles().length==0){
                    File initFile = new File(mediaPath,ConstantValues.INIT_VIDEO_NAME);
                    FileUtils.copyFileFormAssets(mContext,ConstantValues.ASSETS_VIDEO_NAME,initFile.getAbsolutePath());
                }
            }
        }).start();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (AppUtils.isMstar()) {
            //获取小平台地址
            mHandler.postDelayed(()->getSmallServerInfo(),1000*10);
            //启动心跳服务
            mHandler.postDelayed(() -> startHeartbeatService(),1000*50);
            //启动下载服务
            mHandler.postDelayed(() -> startDownloadMediaDataService(),1000*60*2);
            //启动生产日志服务
            mHandler.postDelayed(() -> startProduceLogService(),1000*40);
            //启动上传日志服务
            mHandler.postDelayed(() -> startUploadLogService(),1000*60);
            //启动投屏服务
            mHandler.postDelayed(() -> startProjectionService(),1000*10);
        }else{
            //获取小平台地址
            mHandler.postDelayed(()->getSmallServerInfo(),1000*5);
            //启动心跳服务
            mHandler.postDelayed(() -> startHeartbeatService(),1000*10);
            //启动下载服务
            mHandler.postDelayed(() -> startDownloadMediaDataService(),1000*15);
            //启动生产日志服务
            mHandler.postDelayed(() -> startProduceLogService(),1000*20);
            //启动上传日志服务
            mHandler.postDelayed(() -> startUploadLogService(),1000*25);
            //启动投屏服务
            mHandler.postDelayed(() -> startProjectionService(),1000*10);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d("MainActivity++进入已经播放完开机动画");
    }

    /**
     * 获取小平台地址信息
     */
    private void getSmallServerInfo(){
        if (mSession.getServerInfo() != null) {
            /**特殊需求，因为虚拟小平台地址切换 20190613**/
            if (mSession.isUseVirtualSp()){
                mSession.setServerInfo(new ServerInfo(BuildConfig.VIRTUAL_SP_HOST, 3));
            }
            AppApi.resetSmallPlatformInterface(mContext);

            // source=3表示是在设置界面手动设置的
            if (mSession.getServerInfo().getSource() != 3) {
                // 去云平台获取小平台地址
                getSpIpFromServer();
            }
        } else {
            // 去云平台获取小平台地址
            getSpIpFromServer();
        }
    }
    /**
     * 去云平台获取小平台地址
     */
    private void getSpIpFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!AppUtils.isNetworkAvailable(mContext)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                LogUtils.w("将发HTTP请求去发现小平台信息");
                LogFileUtil.write("MainActivity 将发HTTP请求去发现小平台信息");
                AppApi.getSpIp(mContext, new ApiRequestListener() {
                    @Override
                    public void onSuccess(AppApi.Action method, Object obj) {
                        if (obj instanceof ServerInfo) {
                            LogUtils.w("HTTP接口发现小平台信息");
                            LogFileUtil.write("MainActivity HTTP接口发现小平台信息");
                            handleServerIp((ServerInfo) obj);
                        }
                    }

                    @Override
                    public void onError(AppApi.Action method, Object obj) {
                        LogUtils.w("HTTP接口发现小平台信息失败");
                        LogFileUtil.write("MainActivity HTTP接口发现小平台信息失败");
                    }

                    @Override
                    public void onNetworkFailed(AppApi.Action method) {
                        LogUtils.w("HTTP接口发现小平台信息失败");
                        LogFileUtil.write("MainActivity HTTP接口发现小平台信息失败");
                    }
                });
            }
        }).start();
    }

    private void handleServerIp(ServerInfo serverInfo) {
        if (serverInfo != null && !TextUtils.isEmpty(serverInfo.getServerIp()) && serverInfo.getNettyPort() > 0 && serverInfo.getCommandPort() > 0 && serverInfo.getDownloadPort() > 0 &&
                (mSession.getServerInfo() == null || mSession.getServerInfo().getSource() != 1)) {
            LogUtils.w("将使用HTTP拿到的信息重置小平台信息");
            LogFileUtil.write("MainActivity 将使用HTTP拿到的信息重置小平台信息");
            serverInfo.setSource(2);
            if (serverInfo.getServerIp().contains("*")) {
                serverInfo.setServerIp(serverInfo.getServerIp().split("\\*")[0]);
            }
            mSession.setServerInfo(serverInfo);
            AppApi.resetSmallPlatformInterface(this);

            // 重设NettyClient ip、端口号
            // NettyClient.get() != null意味着在MainActivity已经初始化Netty并开始连接
//            if (NettyClient.get() != null) {
//                NettyClient.get().setServer(serverInfo.getNettyPort(), serverInfo.getServerIp());
//            } else {
//                Intent intent = new Intent(this, MessageService.class);
//                startService(intent);
//            }
        }
    }

    /**
     * 启动心跳服务
     */
    private void startHeartbeatService() {
        LogFileUtil.write("MainActivity will startHeartbeatService");
        Intent intent = new Intent(this, HeartbeatService.class);
        startService(intent);
    }



    /**
     * 启动下载媒体文件服务
     */
    private void startDownloadMediaDataService() {
        LogUtils.v("========start download media service======");
        LogFileUtil.write("MainActivity will startDownloadMediaDataService");
        Intent intent = new Intent(this, HandleMediaDataService.class);
        startService(intent);
    }

    /**
     * 启动生产日志服务
     */
    private void startProduceLogService() {
        LogFileUtil.write("MainActivity will start LogProduceService");
        LogProduceService logProduceService = new LogProduceService(mContext);
        logProduceService.run();
    }

    /**
     * 启动上传log服务
     */
    private void startUploadLogService() {
        LogFileUtil.write("MainActivity will start LogUploadService");
        LogUploadService logUploadService = new LogUploadService(mContext);
        logUploadService.start();
    }

    private void startProjectionService(){
        LogFileUtil.write("MainActivity will start ProjectionService");
        Intent intent = new Intent(mContext, ProjectionService.class);
        startService(intent);
    }

    void initDisplay() {
        try{
            main_imgIv = findViewById(R.id.main_img);
            if (new File(mSession.getSplashPath()).exists()){
                Bitmap bitmap = BitmapFactory.decodeFile(mSession.getSplashPath());
                if (bitmap != null) {
                    main_imgIv.setImageBitmap(bitmap);
                } else {
                    main_imgIv.setImageResource(R.mipmap.bg_splash);
                }
            }else{
                main_imgIv.setImageResource(R.mipmap.bg_splash);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 30, 0);
        }
        if (!AppUtils.isLeTV()&&!AppUtils.isSVT()){
            TimeCalibrateHelper timeCalibrateHelper = new TimeCalibrateHelper();
            timeCalibrateHelper.startCalibrateTime();
        }


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyCode.KEY_CODE_SHOW_INFO) {
            showBoxInfo();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

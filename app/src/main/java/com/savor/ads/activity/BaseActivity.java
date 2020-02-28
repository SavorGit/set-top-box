package com.savor.ads.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.mstar.tv.service.skin.AudioSkin;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.ResponseErrorMessage;
import com.savor.ads.core.Session;
import com.savor.ads.dialog.BoxInfoDialog;
import com.savor.ads.dialog.FileCopyDialog;
import com.savor.ads.dialog.InputBoiteIdDialog;
import com.savor.ads.dialog.UsbUpdateDialog;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.KeyCode;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;
import com.savor.ads.utils.TechnicalLogReporter;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by zhanghq on 2016/6/23.
 */
public abstract class BaseActivity extends Activity implements InputBoiteIdDialog.Callback {

    protected Activity mContext;
    protected Session mSession;
    public AudioManager mAudioManager = null;

    private BoxInfoDialog mBoxInfoDialog;
    private UsbUpdateDialog mUsbUpdateDialog;
    private InputBoiteIdDialog mInputBoiteIdDialog;
    private FileCopyDialog mFileCopyDialog;

    private Handler mHandler = new Handler();

    protected boolean mIsGoneToSystemSetting;
    private AudioSkin mAudioSkin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        ActivitiesManager.getInstance().pushActivity(this);
        mSession = Session.get(mContext);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        LogFileUtil.write("BaseActivity onCreate this is " + this.toString());

        if (AppUtils.isMstar()||AppUtils.isSVT()) {
            mAudioSkin = new AudioSkin(this);
            mAudioSkin.connect(null);
        }
        if (GlobalValues.IS_UPUSH_REGISTER_SUCCESS) {
            LogUtils.d("onAppStart " + this.getClass().getSimpleName());
//            LogFileUtil.write("onAppStart " + this.getClass().getSimpleName());
            PushAgent.getInstance(this).onAppStart();
        }
        //1是打开，0是关闭
//        writeCecOption("hdmi_control_enabled",false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void writeCecOption(String key, boolean value) {
        Settings.Global.putInt(getContentResolver(), key, value ? 1 : 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        registerListener();

    }

    /**
     * 注册广播，判断sd卡以及U盘挂载状态
     */
    public void registerListener() {
        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
//        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
//        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
//        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
//        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);

        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        intentFilter.addDataScheme("file");

        registerReceiver(recevierListener, intentFilter);
    }

    private BroadcastReceiver recevierListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d("BroadcastReceiver: " + intent.getAction());
            if (intent.getAction() == null) {
                return;
            }

            String path = intent.getData().getPath();
            switch (intent.getAction()) {
                case Intent.ACTION_MEDIA_MOUNTED:
                    if (AppUtils.isMstar()) {
                        if (path.contains("/mnt/extsd")) {
                            handleExtsdMounted();
                        } else if (path.contains("usb")) {
                            mSession.setUsbPath(path + File.separator);
                            handleUdiskMounted(path);
                        }
                    } else {
                        if (path.contains("storage/") && path.contains("-")) {
                            mSession.setUsbPath(path + File.separator);
                            handleUdiskMounted(path);
                        }
                    }
                    break;
                case Intent.ACTION_MEDIA_UNMOUNTED:
                case Intent.ACTION_MEDIA_REMOVED:
                case Intent.ACTION_MEDIA_BAD_REMOVAL:
                    if (AppUtils.isMstar()) {
                        if (path.contains("/mnt/extsd")) {//sd 卡拔除
                            handleExtsdRemoved();
                        } else if (path.contains("usb")) {
                            //U盘拔出
                            mSession.setUsbPath(null);
                            handleUdiskRemoved(path);
                        }
                    } else {
                        if (path.contains("storage/") && path.contains("-")) {
                            mSession.setUsbPath(null);
                            handleUdiskRemoved(path);
                        }
                    }
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    //获取联网状态的NetworkInfo对象
                    NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (info!=null){
                        //如果当前的网络连接成功并且网络连接可用
                        if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
//                                AppApi.getScreenInitConfig(mContext,apiRequestListener);
                            }
                        }
                    }
                    break;
            }
        }
    };


    private void handleExtsdMounted() {
        TechnicalLogReporter.sdcardMounted(this);
        checkAndClearCache();

//        fillPlayList();

//        if (GlobalValues.PLAY_LIST != null && !GlobalValues.PLAY_LIST.isEmpty()) {
//            Intent intent = new Intent(this, AdsPlayerActivity.class);
//            startActivity(intent);
//        }
    }

    private void checkAndClearCache() {
        AppUtils.clearAllCache(this);

        String lastStartStr = mSession.getLastStartTime();
        String curTimeStr = AppUtils.getCurTime(AppUtils.DATEFORMAT_YYMMDD);
        String dateStr = null;
        if (!TextUtils.isEmpty(lastStartStr) && lastStartStr.contains(" ")) {
            dateStr = lastStartStr.split(" ")[0];
        }
    }

    public void fillPlayList() {
        LogUtils.d("开始fillPlayList");
        if (!TextUtils.isEmpty(AppUtils.getMainMediaPath())) {
            AppUtils.fillPlaylist(this, null, 1);
        } else {
            LogFileUtil.writeKeyLogInfo("跳转轮播，未找到SD卡！");
            ShowMessage.showToast(mContext, "未发现SD卡");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        if (recevierListener != null) {
            unregisterReceiver(recevierListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivitiesManager.getInstance().popActivity(this);

        if (mBoxInfoDialog != null && mBoxInfoDialog.isShowing()) {
            mBoxInfoDialog.dismiss();
        }
        if (mAudioSkin != null) {
            mAudioSkin.disconnect();
            mAudioSkin = null;
        }
    }


    private Runnable mHideInfoRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBoxInfoDialog != null && mBoxInfoDialog.isShowing()) {
                mBoxInfoDialog.dismiss();
            }
        }
    };

    /**
     * 显示盒子信息
     */
    protected void showBoxInfo() {
        if (mBoxInfoDialog == null) {
            mBoxInfoDialog = new BoxInfoDialog(this);
            mBoxInfoDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mHandler.removeCallbacks(mHideInfoRunnable);
                }
            });
        }
        if (!mBoxInfoDialog.isShowing()) {
            mBoxInfoDialog.show();
        }
        mHandler.postDelayed(mHideInfoRunnable, 10 * 1000);
    }

    /**
     * 插入USB以后，读取USB中的内容,目前仅限图片
     *
     * @param pathString
     */
    private void handleUdiskMounted(String pathString) {
        ShowMessage.showToast(mContext, "U盘已插入，正在读取...");
        String imagesPath = pathString + File.separator + ConstantValues.USB_FILE_PATH;
        File file = new File(imagesPath);
        if (!file.exists()) {
//            ShowMessage.showToast(mContext, "未在U盘中检测到【redian】文件夹，请检查后重试！");
            return;
        }
        File[] files = file.listFiles();
        //空文件夹的情况
        if (files.length == 0) {
//            ShowMessage.showToast(mContext, "检测到【redian】文件夹是空的，请检查后重试！");
            return;
        }
        ArrayList<String> usbImgPathList = getImagePath(files);//获取所有图片路径
        if (usbImgPathList.size() <= 0) {
//            ShowMessage.showToast(mContext, "检测到【redian】文件夹中没有图片文件，请检查后重试！");
            return;
        }

        if (!(this instanceof MainActivity)) {
            if (this instanceof UsbImageViewerActivity) {
                ((UsbImageViewerActivity) this).resetImages(usbImgPathList, imagesPath);
            } else {
                Intent intent = new Intent(this, UsbImageViewerActivity.class);
                intent.putExtra(UsbImageViewerActivity.EXTRA_URLS, usbImgPathList);
                intent.putExtra(UsbImageViewerActivity.EXTRA_USB_PATH, imagesPath);
                startActivity(intent);
            }
        }
    }

    //获取所有图片路径
    private ArrayList<String> getImagePath(File[] files) {

        ArrayList<String> usbImgPathList = new ArrayList<>();
        for (File file : files) {
            if (AppUtils.checkIsImageFile(file.getPath())) {
                usbImgPathList.add(file.getPath());
            }
        }
        return usbImgPathList;
    }

    /**
     * USB拔出
     */
    private void handleUdiskRemoved(String pathString) {
        ShowMessage.showToast(mContext, "U盘已拔出");
        String imagesPath = pathString + File.separator + ConstantValues.USB_FILE_PATH;
        if (this instanceof UsbImageViewerActivity && imagesPath.equals(((UsbImageViewerActivity) this).getCurrentUsbPath())) {
            finish();
        }
    }


    private void handleExtsdRemoved() {
        AppUtils.EXTERNAL_SDCARD_PATH = null;
        TechnicalLogReporter.sdcardRemoved(this);
        //SD移除时跳到TV页
        if (this instanceof AdsPlayerActivity) {
            if (AppUtils.isMstar()) {
                Intent intent = new Intent(this, TvPlayerActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, TvPlayerGiecActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if (keyCode == KeyCode.KEY_CODE_SYSTEM_SETTING) {
//            LogFileUtil.write("will gotoSystemSetting");
            gotoSystemSetting();
            handled = true;

        } else if (keyCode == KeyCode.KEY_CODE_SETTING) {
            gotoSetting();
            handled = true;

        } else if (keyCode == KeyCode.KEY_CODE_MANUAL_HEARTBEAT) {
            manualHeartbeat();
            handled = true;

        } else if (keyCode == KeyCode.KEY_CODE_SHOW_APP_INSTALLED) {
            gotoAppBrowser();
            handled = true;

        } else if (keyCode == KeyCode.KEY_CODE_UDISK_UPDATE) {
            handleUsbUpdate();
            handled = true;
        } else if (keyCode == KeyCode.KEY_CODE_UDISK_COPY) {
            handleUsbCopy();
            handled = true;
        }
        return handled || super.onKeyDown(keyCode, event);
    }

    private void gotoSystemSetting() {
        mIsGoneToSystemSetting = true;
        Intent intent = new Intent("android.settings.SETTINGS");
        startActivity(intent);
    }

    private void gotoSetting() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void gotoAppBrowser() {
        Intent intent = new Intent(this, AppBrowserActivity.class);
        startActivity(intent);
    }

    private void handleUsbUpdate() {
        if (checkAndSetUsbPath()) {
            if (!TextUtils.isEmpty(mSession.getBoiteId())) {
                if (mUsbUpdateDialog == null) {
                    mUsbUpdateDialog = new UsbUpdateDialog(this);
                }
                if (!mUsbUpdateDialog.isShowing()) {
                    mUsbUpdateDialog.show();
                }
            } else {
                if (mInputBoiteIdDialog == null) {
                    mInputBoiteIdDialog = new InputBoiteIdDialog(this, this);
                }

                if (!mInputBoiteIdDialog.isShowing()) {
                    mInputBoiteIdDialog.show();
                }
            }
        } else {
            ShowMessage.showToast(this, "未发现可执行U盘目录");
        }
    }

    private void handleUsbCopy() {
        if (checkAndSetUsbPath()) {
            File mediaFile = new File(mSession.getUsbPath() + ConstantValues.USB_FILE_HOTEL_MEDIA_PATH);
            if (!mediaFile.exists()) {
                ShowMessage.showToast(this, "未发现可执行U盘目录");
            } else {
                if (mFileCopyDialog == null) {
                    mFileCopyDialog = new FileCopyDialog(this);
                }
                if (!mFileCopyDialog.isShowing()) {
                    mFileCopyDialog.show();
                }
            }
        } else {
            ShowMessage.showToast(this, "未发现可执行U盘目录");
        }
    }

    private boolean checkAndSetUsbPath() {
        boolean hasEligibleUdisk = false;
        if (AppUtils.isMstar()||AppUtils.isSVT()) {
            for (File file : new File("/mnt/usb/").listFiles()) {
                if (new File(file, ConstantValues.USB_FILE_HOTEL_PATH).exists() ||
                        new File(file, ConstantValues.USB_FILE_HOTEL_MEDIA_PATH).exists()) {
                    mSession.setUsbPath(file.getPath() + File.separator);
                    hasEligibleUdisk = true;
                    break;
                }
            }
        } else {
            String[] possiblePaths = new String[]{"/storage/udisk0/", "/storage/udisk1/", "/storage/udisk2/"};
            for (String path : possiblePaths) {
                if (new File(path + ConstantValues.USB_FILE_HOTEL_PATH).exists() ||
                        new File(path + ConstantValues.USB_FILE_HOTEL_MEDIA_PATH).exists()) {
                    mSession.setUsbPath(path);
                    hasEligibleUdisk = true;
                    break;
                }
            }
        }
        return hasEligibleUdisk;
    }

    private void manualHeartbeat() {
        ShowMessage.showToast(this, "开始上报心跳");
        LogFileUtil.write("开始手动上报心跳");
        AppApi.heartbeat(this, new ApiRequestListener() {
            @Override
            public void onSuccess(AppApi.Action method, Object obj) {
                ShowMessage.showToast(mContext, "上报心跳成功");
                LogFileUtil.write("手动上报心跳成功。 " + obj);
            }

            @Override
            public void onError(AppApi.Action method, Object obj) {
                String msg = "";
                if (obj instanceof ResponseErrorMessage) {
                    ResponseErrorMessage errorMessage = (ResponseErrorMessage) obj;
                    msg = errorMessage.getMessage();
                }
                ShowMessage.showToast(mContext, "上报心跳失败 " + msg);
                LogFileUtil.write("手动上报心跳失败 " + msg);
            }

            @Override
            public void onNetworkFailed(AppApi.Action method) {
                ShowMessage.showToast(mContext, "上报心跳失败，网络异常");
                LogFileUtil.write("手动上报心跳失败，网络异常");
            }
        });
    }

    protected void setVolume(int volume) {
        if (AppUtils.isMstar()||AppUtils.isSVT()) {
            if (mAudioSkin != null) {
                if (volume > 100)
                    volume = 100;
                else if (volume < 0)
                    volume = 0;
                mAudioSkin.setVolume(volume);
            }
        } else {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                LogUtils.d("System volume:" + audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
                if (volume > 100)
                    volume = 100;
                else if (volume < 0)
                    volume = 0;
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume * maxVolume / 100, 0);
            }
        }
    }

    @Override
    public void onBoiteIdCheckPass() {
        handleUsbUpdate();
    }
}

package com.savor.ads.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.savor.ads.BuildConfig;
import com.savor.ads.R;
import com.savor.ads.SavorApplication;
import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.core.Session;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.StringUtils;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by zhanghq on 2016/12/12.
 */

public class BoxInfoDialog extends Dialog {
    private Context mContext;
    private Session session;
    private TextView mHotelNameTv;
    private TextView tvDownloadStateTv;
    private TextView mRomVersionTv;
    private TextView mAppVersionTv;
    private TextView mSystemTimeTv;
    private TextView mRoomNameTv;
    private TextView mBoxNameTv;
    private TextView mSignalSourceTv;
    private TextView mTvSwitchTimeTv;
    private TextView mEthernetMacTv;
    private TextView mWlanMacTv;
    private TextView mWlanMacLabelTv;
    private TextView mEthernetIpTv;
    private TextView mWlanIpLabelTv;
    private TextView mWlanIpTv;
    private TextView mAdsPeriodTv;
    private TextView mBirthdayPeriodTv;
    private TextView mProPeriodTv;
    private TextView mAdvPeriodTv;
    private TextView mAdsDownloadPeriodTv;
    private TextView mBirthdayDownloadPeriodTv;
    private TextView mProDownloadPeriodTv;
    private TextView mAdvDownloadPeriodTv;
    private TextView mLogoPeriodTv;
    private TextView mLoadingPeriodTv;
    private TextView mServerIpTv;
    private TextView mLastPowerOnTimeTv;
    //轮播音量
    private TextView mVolumeTv;
    //电视节目音量
    private TextView mTvVolumeTv;
    //用户内容点播音量
    private TextView contentDemandVolumeTV;
    //轮播内容点播音量
    private TextView proDemandVolumeTV;
    private TextView imgProjectionVolumeTV;
    private TextView videoProjectionVolumeTV;


    public BoxInfoDialog(Context context) {
        super(context, R.style.box_info_dialog_theme);
        mContext = context;
        session = Session.get(mContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_box_info);
        setDialogAttributes();
        mHotelNameTv = findViewById(R.id.tv_hotel_name);
        tvDownloadStateTv = findViewById(R.id.tv_download_state);
        mRomVersionTv = findViewById(R.id.tv_rom_version);
        mAppVersionTv = findViewById(R.id.tv_app_version);
        mSystemTimeTv = findViewById(R.id.tv_sys_time);
        mTvSwitchTimeTv = findViewById(R.id.tv_switch_tv_time);
        mRoomNameTv = findViewById(R.id.tv_room_name);
        mEthernetIpTv = findViewById(R.id.tv_eth_ip);
        mWlanIpTv = findViewById(R.id.tv_wlan_ip);
        mWlanIpLabelTv = findViewById(R.id.tv_wlan_ip_label);
        mSignalSourceTv = findViewById(R.id.tv_signal_source);
        mEthernetMacTv = findViewById(R.id.tv_ethernet_mac);
        mWlanMacTv = findViewById(R.id.tv_wlan_mac);
        mWlanMacLabelTv = findViewById(R.id.tv_wlan_mac_label);
        mAdsPeriodTv = findViewById(R.id.tv_ads_period);
        mBirthdayPeriodTv = findViewById(R.id.tv_birthday_period);
        mProPeriodTv = findViewById(R.id.tv_pro_period);
        mAdvPeriodTv = findViewById(R.id.tv_adv_period);
        mLogoPeriodTv = findViewById(R.id.tv_logo_version);
        mLoadingPeriodTv = findViewById(R.id.tv_loading_version);
        mServerIpTv = findViewById(R.id.tv_server_ip);
        mLastPowerOnTimeTv = findViewById(R.id.tv_last_power_on_time);
        mVolumeTv = findViewById(R.id.tv_volume);
        mTvVolumeTv = findViewById(R.id.tv_tv_volume);
        contentDemandVolumeTV = findViewById(R.id.tv_content_demand_volume);
        proDemandVolumeTV = findViewById(R.id.tv_pro_demand_volume);
        imgProjectionVolumeTV = findViewById(R.id.tv_img_projection_volume);
        videoProjectionVolumeTV = findViewById(R.id.tv_video_projection_volume);
        //保存字段
        mBoxNameTv = findViewById(R.id.tv_box_name);
        mAdsDownloadPeriodTv = findViewById(R.id.tv_ads_download_period);
        mAdvDownloadPeriodTv = findViewById(R.id.tv_adv_download_period);
        mProDownloadPeriodTv = findViewById(R.id.tv_pro_download_period);
        mBirthdayDownloadPeriodTv = findViewById(R.id.tv_birthday_download_period);
    }


    private void setDialogAttributes() {
        Window window = getWindow(); // 得到对话框
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = WindowManager.LayoutParams.MATCH_PARENT;
        wl.height = WindowManager.LayoutParams.MATCH_PARENT;
        wl.gravity = Gravity.CENTER;
        window.setAttributes(wl);
    }

    @Override
    public void show() {
        super.show();
        try {
            ((SavorApplication) mContext.getApplicationContext()).hideMiniProgramQrCodeWindow();
            mHotelNameTv.setText(session.getBoiteName());
            tvDownloadStateTv.setText("未下载");
            if (session.getType()==2){
                tvDownloadStateTv.setTextColor(Color.parseColor("#FF0000"));
            }else{
                tvDownloadStateTv.setTextColor(Color.parseColor("#FFFFFFFF"));
            }
            mRomVersionTv.setText(session.getRomVersion());
            mAppVersionTv.setText(session.getVersionName() + "_" + session.getVersionCode());
            mSystemTimeTv.setText(AppUtils.getCurTime());
            if (AppUtils.isSVT()){
                int svtTvInputKey = AppUtils.queryCurInputSrc(mContext);
                mSignalSourceTv.setText(getSvtTvInputSource(svtTvInputKey));
            }else {
                mSignalSourceTv.setText(AppUtils.getInputType(session.getTvInputSource()));
            }
            mTvSwitchTimeTv.setText(String.valueOf(session.getSwitchTime()));
            mRoomNameTv.setText(session.getRoomName());
            mBoxNameTv.setText(session.getBoxName());
            mEthernetIpTv.setText(AppUtils.getEthernetIP());
            mEthernetMacTv.setText(session.getEthernetMac());

            mWlanIpLabelTv.setText("无线IP地址");
            mWlanIpTv.setText(AppUtils.getWlanIP());
            mWlanMacLabelTv.setText("无线MAC地址");
            mWlanMacTv.setText(session.getWlanMac());

            mAdsPeriodTv.setText(session.getAdsPeriod());
            mBirthdayPeriodTv.setText(session.getBirthdayOndemandPeriod());
            mAdvPeriodTv.setText(session.getAdvPeriod());
            mProPeriodTv.setText(session.getProPeriod());
            mLogoPeriodTv.setText(session.getSplashVersion());
            mLoadingPeriodTv.setText(session.getLoadingVersion());
            mAdsDownloadPeriodTv.setText(session.getAdsDownloadPeriod());
            mAdvDownloadPeriodTv.setText(session.getAdvDownloadPeriod());
            mProDownloadPeriodTv.setText(session.getProDownloadPeriod());
            mBirthdayDownloadPeriodTv.setText(session.getBirthdayOndemandDownloadPeriod());

            if (!TextUtils.isEmpty(session.getProNextMediaPubTime())) {
                if (!TextUtils.isEmpty(session.getAdsNextPeriod()) && session.getAdsNextPeriod().equals(session.getAdsDownloadPeriod())) {
                    mAdsDownloadPeriodTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_check, 0);
                } else {
                    mAdsDownloadPeriodTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                if (!TextUtils.isEmpty(session.getAdvNextPeriod()) && session.getAdvNextPeriod().equals(session.getAdvDownloadPeriod())) {
                    mAdvDownloadPeriodTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_check, 0);
                } else {
                    mAdvDownloadPeriodTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                if (!TextUtils.isEmpty(session.getProNextPeriod()) && session.getProNextPeriod().equals(session.getProDownloadPeriod())) {
                    mProDownloadPeriodTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_check, 0);
                } else {
                    mProDownloadPeriodTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            } else {
                mAdsDownloadPeriodTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mAdvDownloadPeriodTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mProDownloadPeriodTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            if (session.getServerInfo() != null) {
                if (session.isConnectedToSP()) {
                    mServerIpTv.setText(session.getServerInfo().getServerIp());
                } else {
                    if (session.isUseVirtualSp()){
                        mServerIpTv.setText(BuildConfig.VIRTUAL_SP_HOST);
                    }else{
                        mServerIpTv.setText(session.getServerInfo().getServerIp());
                    }

//                mServerIpTv.setText(Html.fromHtml("<font color=#E61A6B>"
//                        + session.getServerInfo().getServerIp() + "</font> "));
                }
                if (session.getServerInfo().getSource() == 3) {
                    mServerIpTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_manual, 0);
                } else {
                    mServerIpTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_auto, 0);
                }
            } else {
                mServerIpTv.setText("");
                mServerIpTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            mLastPowerOnTimeTv.setText(TextUtils.isEmpty(session.getLastStartTime()) ? "初次开机" : session.getLastStartTime());
            if (AppUtils.isSVT()||AppUtils.isPhilips()){
                mVolumeTv.setText(String.valueOf(session.getTvCarouselVolume()));
                contentDemandVolumeTV.setText(String.valueOf(session.getTvContentDemandVolume()));
                proDemandVolumeTV.setText(String.valueOf(session.getTvProDemandVolume()));
                imgProjectionVolumeTV.setText(String.valueOf(session.getTvImgFroscreenVolume()));
                videoProjectionVolumeTV.setText(String.valueOf(session.getTvVideoFroscreenVolume()));
            }else {
                mVolumeTv.setText(String.valueOf(session.getBoxCarouselVolume()));
                mTvVolumeTv.setText(String.valueOf(session.getBoxTvVolume()));
                contentDemandVolumeTV.setText(String.valueOf(session.getBoxContentDemandVolume()));
                proDemandVolumeTV.setText(String.valueOf(session.getBoxProDemandVolume()));
                imgProjectionVolumeTV.setText(String.valueOf(session.getBoxImgFroscreenVolume()));
                videoProjectionVolumeTV.setText(String.valueOf(session.getBoxVideoFroscreenVolume()));
            }

        }catch (Exception e){
            e.printStackTrace();
            CrashReport.postCatchedException(e);
        }
    }

    private String getSvtTvInputSource(int svtTvInputKey){
        String inputSource = "暂无";
        switch (svtTvInputKey){
            case ConstantValues.SVT_INPUT_SOURCE_ATV:
                inputSource = "模拟电视";
                break;
            case ConstantValues.SVT_INPUT_SOURCE_DTV:
                inputSource = "数字电视";
                break;
            case ConstantValues.SVT_INPUT_SOURCE_CVBS:
                inputSource = "视频";
                break;
            case ConstantValues.SVT_INPUT_SOURCE_YPBPR:
                inputSource = "分量";
                break;
            case ConstantValues.SVT_INPUT_SOURCE_HDMI1:
                inputSource = "HDMI1";
                break;
            case ConstantValues.SVT_INPUT_SOURCE_HDMI2:
                inputSource = "HDMI2";
                break;
            case ConstantValues.SVT_INPUT_SOURCE_HDMI3:
                inputSource = "HDMI3";
                break;
        }
        return inputSource;
    }

    public void setTvDownloadState(){
        if (GlobalValues.isDownload||GlobalValues.isWLANDownload){
            tvDownloadStateTv.setText("下载中---"+GlobalValues.currentDownlaodFileName+"|当前网速---"+session.getNetSpeed());
        }else{
            tvDownloadStateTv.setText("未下载");
        }
    }


    public void moveFocus(int changeType){
        switch (changeType){
            case 1:
                moveFocusUp();
                break;
            case 2:
                moveFocusDown();
                break;
            case 3:
                moveFocusLeft();
                break;
            case 4:
                moveFocusRight();
                break;
        }
    }

    public void moveFocusDown(){
        new Thread(()->{
            Instrumentation instrumentation = new Instrumentation();
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        }).start();
    }
    public void moveFocusUp(){
        new Thread(()->{
            Instrumentation instrumentation = new Instrumentation();
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
        }).start();
    }
    public void moveFocusLeft(){
        new Thread(()->{
            Instrumentation instrumentation = new Instrumentation();
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
        }).start();
    }
    public void moveFocusRight(){
        new Thread(()->{
            Instrumentation instrumentation = new Instrumentation();
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
            if (activity instanceof AdsPlayerActivity){
                AdsPlayerActivity adsPlayerActivity = (AdsPlayerActivity) activity;
                adsPlayerActivity.toCheckMediaIsShowMiniProgramIcon();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}

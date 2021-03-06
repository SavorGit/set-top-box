package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.savor.ads.core.Session;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.StringUtils;

/**
 * Created by zhanghq on 2016/12/12.
 */

public class BoxInfoDialog extends Dialog {
    private Context mContext;
    private TextView mHotelNameTv;
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
    private TextView mVolumeTv;

    private TextView mProjectVolumeTv;
    private TextView mVodVolumeTv;
    private TextView mTvVolumeTv;

    public BoxInfoDialog(Context context) {
        super(context, R.style.box_info_dialog_theme);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_box_info);
        setDialogAttributes();
        mHotelNameTv = findViewById(R.id.tv_hotel_name);
        mRomVersionTv = findViewById(R.id.tv_rom_version);
        mAppVersionTv = findViewById(R.id.tv_app_version);
        mSystemTimeTv = findViewById(R.id.tv_sys_time);
        mVolumeTv = findViewById(R.id.tv_volume);
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
        mProjectVolumeTv = findViewById(R.id.tv_project_volume);
        mVodVolumeTv = findViewById(R.id.tv_vod_volume);
        mTvVolumeTv = findViewById(R.id.tv_tv_volume);
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
            Session session = Session.get(getContext());
            mHotelNameTv.setText(session.getBoiteName());
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
            if (session.isStandalone()) {
                mWlanIpLabelTv.setText("U盘更新时间");
                mWlanIpTv.setText(session.getLastUDiskUpdateTime());
                mWlanMacLabelTv.setText("是否单机版");
                mWlanMacTv.setText("是");
            } else {
                mWlanIpLabelTv.setText("无线IP地址");
                mWlanIpTv.setText(AppUtils.getWlanIP());
                mWlanMacLabelTv.setText("无线MAC地址");
                mWlanMacTv.setText(session.getWlanMac());
            }
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
            mVolumeTv.setText(String.valueOf(session.getVolume()));
            mProjectVolumeTv.setText(String.valueOf(session.getProjectVolume()));
            mVodVolumeTv.setText(String.valueOf(session.getVodVolume()));
            mTvVolumeTv.setText(String.valueOf(session.getTvVolume()));
        }catch (Exception e){
            e.printStackTrace();
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
}

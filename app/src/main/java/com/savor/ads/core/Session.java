package com.savor.ads.core;

/*
 * Copyright (C) 2010 mAPPn.Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import androidx.core.util.Pair;
import android.text.TextUtils;
import android.util.Base64;

import com.savor.ads.bean.ProjectionGuideImg;
import com.savor.ads.bean.PushRTBItem;
import com.savor.ads.bean.SellWineActivityBean;
import com.savor.ads.bean.ServerInfo;
import com.savor.ads.bean.VersionInfo;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.MacAddressUtils;
import com.savor.ads.utils.SaveFileData;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Administrator
 */
@SuppressLint("WorldReadableFiles")
public class Session {
    private final static String TAG = "Session";
    private Context mContext;
    private SaveFileData mPreference;
    private static Session mInstance;


    private final int osVersion;
    private final String buildVersion;
    private final String model;
    private final String brand;
    private final String romVersion;
    private String versionName;
    private int versionCode;
    private String debugType;
    private boolean isDebug;
    private String appName;
    private String macAddress;
    private String token;

    private ServerInfo serverInfo;
    /** 机顶盒轮播音量 */
    private int boxCarouselVolume;
    /** 机顶盒内容点播音量 */
    private int boxContentDemandVolume;
    /** 机顶盒节目点播音量 */
    private int boxProDemandVolume;
    /** 机顶盒图片投屏音量 */
    private int boxImgFroscreenVolume;
    /** 机顶盒视频投屏音量 */
    private int boxVideoFroscreenVolume;
    /** 机顶盒电视节目音量 */
    private int boxTvVolume;
    /** 电视轮播音量 */
    private int tvCarouselVolume;
    /** 电视内容点播音量 */
    private int tvContentDemandVolume;
    /** 电视节目点播音量 */
    private int tvProDemandVolume;
    /** 电视图片投屏音量 */
    private int tvImgFroscreenVolume;
    /** 电视视频投屏音量 */
    private int tvVideoFroscreenVolume;
    //酒楼名称
    private String boiteName;
    //酒楼ID
    private String boiteId;
    //包间名称
    private String roomName;
    //包间ID
    private String roomId;
    private String boxId;
    /** 盒子名*/
    private String boxName;
    /** 包间类型*/
    private String roomType;

    /**下一期要播放的广告时间*/
    private String proNextMediaPubTime;

    /**节目单期号（含节目）*/
    private String proPeriod;
    /**宣传片期号*/
    private String advPeriod;
    /**广告期号*/
    private String adsPeriod;
    /**RTB广告期号*/
    private String rtbadsPeriod;
    /**百度聚屏广告期号*/
    private String polyAdsPeriod;
    /**下载中的节目单期号（含节目）*/
    private String proDownloadPeriod;
    /**下载中的宣传片期号*/
    private String advDownloadPeriod;
    /**下载中的广告期号*/
    private String adsDownloadPeriod;
    private String polyAdsDownloadPeriod;
    /**下一期节目单期号（含节目）*/
    private String proNextPeriod;
    /**下一期广告期号*/
    private String adsNextPeriod;
    /**下一期宣传片期号*/
    private String advNextPeriod;

    /**生日媒体视频期号**/
    private String birthdayOndemandDownloadPeriod;
    private String birthdayOndemandPeriod;
    /**互动投屏广告期号*/
    private String interactionAdsPeriod;
    /**活动商品广告期号*/
    private String activityAdsPeriod;
    /**商城商品广告期号*/
    private String shopGoodsAdsPeriod;
    /**用户精选内容期号*/
    private String selectContentPeriod;
    /**热播内容期号*/
    private String hotContentPeriod;
    /**欢迎词内容期号*/
    private String welcomeResourcePeriod;
    /**酒水平台广告期号*/
    private String storeSaleAdsPeriod;

    //开机时间
    private String startTime;
    private String lastStartTime;
    //如果当前播放的是电视节目，多长时间切换到广告
    private int switchTime;

    /**
     * 电视当前频道
     */
    private int mTvCurrentChannelNumber;
    /**
     * 电视默认频道
     */
    private int mTvDefaultChannelNumber;
    /**
     * 电视当前输入源
     */
    private int mTvInputSource;
    /**
     * 以太网卡MAC地址
     */
    private String mEthernetMac;
    /**
     * 以太网卡MAC地址
     */
    private String mEthernetMacWithColon;
    /**
     * 无线网卡MAC地址
     */
    private String mWlanMac;

    /**
     * oss区域ID
     */
    private String ossAreaId;

    private boolean mIsConnectedToSP;
    /** 呼玛验证码*/
    private String mAuthCode;
    /** 启动图路径*/
    private String mSplashPath;

    /** 小平台中的所有版本号期号等信息*/
    private ArrayList<VersionInfo> mSPVersionInfo;
    /**电视机尺寸**/
    private int tvSize;
    /** 启动图版本*/
    private String mSplashVersion;
    /** 加载图版本*/
    private String mLoadingVersion;

    /** 是否使用虚拟小平台*/
    private boolean mUseVirtualSp;
    /**单机版U盘目录*/
    private String usbPath;
	/**上次U盘更新时间*/
    private String lastUDiskUpdateTime;

    /**记录下载速度**/
	private String netSpeed;
	/**后台配置时候是显示小程序码的屏**/
    private boolean isShowMiniProgramIcon;
    /**后台配置是否显示极简小程序码的屏**/
    private boolean isShowSimpleMiniProgramIcon;
    /**后台配置是否是支持互动投屏前后加广告的屏**/
    private boolean isOpenInteractscreenad;
    /**投屏互动广告展示间隔次数**/
    private int systemSappForscreenNums;
    /**小程序NETTY服务是否存活**/
    private boolean isHeartbeatMiniNetty;
    private String nettyUrl;
    private int nettyPort;
    /**小程序码类型:1.展示小程序码，2,展示二维码**/
    private int qrcodeType;
    /**
     * 活动广告播放类型 1替换 2队列
     */
    private int activityPlayType;
    /**
     * 极简版投屏上传文件大小分界
     */
    private long simple_upload_size;
    /**文件投屏插播广告间隔次数*/
    private int scenceadv_show_num;
    /**二维码展示时长s*/
    private int qrcode_showtime;
    /**二维码间隔时长s*/
    private int qrcode_takttime;
    /**是否是安装4G卡的机顶盒**/
	private boolean whether4gNetwork;

	/**底部二维码背景*/
	private String qrcodeGifBgPath;
    /**是否展示引導嗎*/
	private ProjectionGuideImg guideImg;

    //是展示动画二维码还是展示普通二维码
	private boolean isShowAnimQRcode;
    /**是否是售卖酒水的酒楼，0:不是,1:是 */
    private boolean isSaleHotel;
    /**酒水售卖酒楼二维码tip轮转显示*/
    private List<String> qrcodeTipList=new ArrayList<>();

    public boolean isFirstWelcomeImg() {
        return isFirstWelcomeImg;
    }

    public void setFirstWelcomeImg(boolean firstWelcomeImg) {
        isFirstWelcomeImg = firstWelcomeImg;
    }

    //每次開機只有第一個掃碼的人展示歡迎圖片，重啟復位
    private boolean isFirstWelcomeImg;

    private boolean isWifiHotel;

    private int normalUseWechat;
    /**下载来源类型|-1:走之前下载逻辑 1:云端下载 2:局域网下载*/
    private int type;
    private String lan_ip;
    private String lan_mac;
    //售酒现金奖励活动
    private SellWineActivityBean sellWineBean;


    private Session(Context context) {

        mContext = context;
        mPreference = new SaveFileData(context, "savor");
        osVersion = Build.VERSION.SDK_INT;
        buildVersion = Build.VERSION.RELEASE;
        model = Build.MODEL;
        brand = Build.BRAND;
        romVersion = Build.VERSION.INCREMENTAL;
        try {
//            AppUtils.clearExpiredFile(context, false);
//            AppUtils.clearExpiredCacheFile(context);
            readSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Session get(Context context) {

        if (mInstance == null) {
            mInstance = new Session(context);
        }
        return mInstance;
    }

    private void readSettings() {

        getApplicationInfo();
        boxCarouselVolume = mPreference.loadIntKey(P_APP_BOX_CAROUSEL_VOLUME, ConstantValues.BOX_CAROUSEL_VOLUME);
        boxContentDemandVolume = mPreference.loadIntKey(P_APP_BOX_CONTENT_DEMAND_VOLUME, ConstantValues.BOX_CONTENT_DEMAND_VOLUME);
        boxProDemandVolume = mPreference.loadIntKey(P_APP_BOX_PRO_DEMAND_VOLUME, ConstantValues.BOX_PRO_DEMAND_VOLUME);
        boxImgFroscreenVolume = mPreference.loadIntKey(P_APP_BOX_IMG_FORSCREEN_VOLUME, ConstantValues.BOX_IMG_FORSCREEN_VOLUME);
        boxVideoFroscreenVolume = mPreference.loadIntKey(P_APP_BOX_VIDEO_FORSCREEN_VOLUME, ConstantValues.BOX_VIDEO_FORSCREEN_VOLUME);
        boxTvVolume = mPreference.loadIntKey(P_APP_BOX_TV_VOLUME, ConstantValues.BOX_TV_VOLUME);

        tvCarouselVolume = mPreference.loadIntKey(P_APP_TV_CAROUSEL_VOLUME, ConstantValues.TV_CAROUSEL_VOLUME);
        tvContentDemandVolume = mPreference.loadIntKey(P_APP_TV_CONTENT_DEMAND_VOLUME, ConstantValues.TV_CONTENT_DEMAND_VOLUME);
        tvProDemandVolume = mPreference.loadIntKey(P_APP_TV_PRO_DEMAND_VOLUME, ConstantValues.TV_PRO_DEMAND_VOLUME);
        tvImgFroscreenVolume = mPreference.loadIntKey(P_APP_TV_IMG_FORSCREEN_VOLUME, ConstantValues.TV_IMG_FORSCREEN_VOLUME);
        tvVideoFroscreenVolume = mPreference.loadIntKey(P_APP_TV_VIDEO_FORSCREEN_VOLUME, ConstantValues.TV_VIDEO_FORSCREEN_VOLUME);

        boiteName = mPreference.loadStringKey(P_APP_BOITENAME, null);
        boiteId = mPreference.loadStringKey(P_APP_BOITEID, null);
        roomName = mPreference.loadStringKey(P_APP_ROOMNAME, null);
        roomId = mPreference.loadStringKey(P_APP_ROOMID, null);
        boxName = mPreference.loadStringKey(P_APP_BOXNAME, null);
        boxId = mPreference.loadStringKey(P_APP_BOXID, null);
        roomType = mPreference.loadStringKey(P_APP_ROOM_TYPE, null);

        proPeriod = mPreference.loadStringKey(P_APP_PRO_MEDIA_PERIOD,"");
        proDownloadPeriod = mPreference.loadStringKey(P_APP_PRO_DOWNLOAD_MEDIA_PERIOD,"");
        proNextPeriod = mPreference.loadStringKey(P_APP_PRO_NEXT_MEDIA_PERIOD,"");
        proNextMediaPubTime = mPreference.loadStringKey(P_APP_PRO_NEXT_MEDIA_PUBTIME,"");
        advPeriod = mPreference.loadStringKey(P_APP_ADV_MEDIA_PERIOD,"");
        advDownloadPeriod = mPreference.loadStringKey(P_APP_ADV_DOWNLOAD_MEDIA_PERIOD,"");
        advNextPeriod = mPreference.loadStringKey(P_APP_ADV_NEXT_MEDIA_PERIOD,"");
        adsPeriod = mPreference.loadStringKey(P_APP_ADS_MEIDA_PERIOD,"");
        adsDownloadPeriod = mPreference.loadStringKey(P_APP_ADS_DOWNLOAD_MEIDA_PERIOD, "");
        rtbadsPeriod = mPreference.loadStringKey(P_APP_RTBADS_MEIDA_PERIOD,"");
        polyAdsPeriod = mPreference.loadStringKey(P_APP_POLYADS_MEDIA_PERIOD,"");
        polyAdsDownloadPeriod = mPreference.loadStringKey(P_APP_POLYADS_DOWNLOAD_MEDIA_PERIOD,"");

        birthdayOndemandDownloadPeriod = mPreference.loadStringKey(P_APP_BIRTHDAY_ONDEMAND_DOWNLOAD_PERIOD,"");
        birthdayOndemandPeriod = mPreference.loadStringKey(P_APP_BIRTHDAY_ONDEMAND_PERIOD,"");
        interactionAdsPeriod = mPreference.loadStringKey(P_APP_FORSCREEN_ADS_PERIOD,"");
        activityAdsPeriod = mPreference.loadStringKey(P_APP_ACTIVITY_ADS_PERIOD,"");
        shopGoodsAdsPeriod = mPreference.loadStringKey(P_APP_SHOP_GOODS_ADS_PERIOD,"");
        selectContentPeriod = mPreference.loadStringKey(P_APP_SELECT_CONTENT_PERIOD,"");
        hotContentPeriod = mPreference.loadStringKey(P_APP_HOT_CONTENT_PERIOD,"");
        welcomeResourcePeriod = mPreference.loadStringKey(P_APP_WELCOME_RESOURCE_PERIOD,"");
        storeSaleAdsPeriod = mPreference.loadStringKey(P_APP_STORE_SALE_ADS_PERIOD,"");
        startTime = mPreference.loadStringKey(P_APP_STARTTIME, null);
        lastStartTime = mPreference.loadStringKey(P_APP_LASTSTARTTIME, null);
        switchTime = mPreference.loadIntKey(P_APP_SWITCHTIME, ConstantValues.DEFAULT_SWITCH_TIME);

        mTvInputSource = mPreference.loadIntKey(P_APP_TV_CURRENT_INPUT, 0);
        mTvCurrentChannelNumber = mTvDefaultChannelNumber = mPreference.loadIntKey(P_APP_TV_DEFAULT_CHANNEL, 0);
        serverInfo = (ServerInfo) StringToObject(mPreference.loadStringKey(P_APP_SERVER_INFO, null));
        mEthernetMac = mPreference.loadStringKey(P_APP_ETHERNET_MAC, null);
        mWlanMac = mPreference.loadStringKey(P_APP_WLAN_MAC, null);
        ossAreaId = mPreference.loadStringKey(P_APP_OSS_AREA_ID,null);
        mAuthCode = mPreference.loadStringKey(P_APP_AUTH_CODE,null);
        mSplashPath = mPreference.loadStringKey(P_APP_SPLASH_PATH, "/Pictures/logo.jpg");
        mSplashVersion = mPreference.loadStringKey(P_APP_SPLASH_VERSION, "");
        mLoadingVersion = mPreference.loadStringKey(P_APP_LOADING_VERSION, "");
        mSPVersionInfo = (ArrayList<VersionInfo>) StringToObject(mPreference.loadStringKey(P_APP_SP_VERSION_INFO, ""));
        tvSize = mPreference.loadIntKey(P_APP_TV_SIZE,0);

        mUseVirtualSp = mPreference.loadBooleanKey(P_APP_USE_VIRTUAL_SP, false);
        lastUDiskUpdateTime = mPreference.loadStringKey(P_APP_LAST_UDISK_UPDATE_TIME, "");
        netSpeed = mPreference.loadStringKey(P_APP_DOWNLOAD_NET_SPEED,"");
        isShowMiniProgramIcon = mPreference.loadBooleanKey(P_APP_SHOW_MIMIPROGRAM,false);
        isShowSimpleMiniProgramIcon = mPreference.loadBooleanKey(P_APP_SHOW_SIMPLE_MIMIPROGRAM,false);
        isOpenInteractscreenad = mPreference.loadBooleanKey(P_APP_OPEN_INTERACTSCREENAD,false);
        systemSappForscreenNums = mPreference.loadIntKey(P_APP_SYSTEM_FORSCREEN_NUMS,0);
        simple_upload_size = mPreference.loadLongKey(P_APP_SIMPLE_UPLOAD_SIZE,0);
        scenceadv_show_num = mPreference.loadIntKey(P_APP_SCENCEADV_SHOW_NUM,0);
        qrcode_showtime = mPreference.loadIntKey(P_APP_QRCODE_SHOW_TIME,0);
        qrcode_takttime = mPreference.loadIntKey(P_APP_QRCODE_TAKT_TIME,0);
        nettyUrl = mPreference.loadStringKey(P_APP_NETTY_URL,null);
        nettyPort = mPreference.loadIntKey(P_APP_NETTY_PORT,0);
        whether4gNetwork = mPreference.loadBooleanKey(P_APP_4G_NETWORK,false);
        qrcodeGifBgPath = mPreference.loadStringKey(P_APP_QRCODE_GIFBG,"");
        qrcodeType = mPreference.loadIntKey(P_APP_BOX_QRCODETYPE,1);
        guideImg = (ProjectionGuideImg) getObj(P_APP_PROJECTION_GUIDE);
        isShowAnimQRcode = mPreference.loadBooleanKey(P_APP_SHOW_ANIM,false);
        isWifiHotel = mPreference.loadBooleanKey(P_APP_WIFI_HOTEL,false);
    }

    /*
     * 读取App配置信息
     */
    private void getApplicationInfo() {

        final PackageManager pm = mContext.getPackageManager();
        try {
            final PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),
                    0);
            versionName = pi.versionName;
            versionCode = pi.versionCode;

            final ApplicationInfo ai = pm.getApplicationInfo(
                    mContext.getPackageName(), PackageManager.GET_META_DATA);
            debugType = "1";// ai.metaData.get("app_debug").toString();

            if ("1".equals(debugType)) {
                // developer mode
                isDebug = true;
            } else if ("0".equals(debugType)) {
                // release mode
                isDebug = false;
            }
            LogUtils.allow = isDebug;

            appName = String.valueOf(ai.loadLabel(pm));
            LogUtils.appTagPrefix = appName;

        } catch (NameNotFoundException e) {
            LogUtils.d("met some error when get application info");
        }
    }


    private void writePreference(Pair<String, Object> updateItem) {
        //
        // // the preference key
        final String key = updateItem.first;

        //根据不同的key确定不同的存储方式。
        if (P_APP_BOITENAME.equals(key)
                || P_APP_BOITEID.equals(key)
                || P_APP_ROOMNAME.equals(key)
                || P_APP_ROOMID.equals(key)
                || P_APP_BOXNAME.equals(key)
                || P_APP_BOXID.equals(key)
                || P_APP_ROOM_TYPE.equals(key)
                || P_APP_PRO_MEDIA_PERIOD.equals(key)
                || P_APP_PRO_DOWNLOAD_MEDIA_PERIOD.equals(key)
                || P_APP_PRO_NEXT_MEDIA_PERIOD.equals(key)
                || P_APP_PRO_NEXT_MEDIA_PUBTIME.equals(key)
                || P_APP_ADV_MEDIA_PERIOD.equals(key)
                || P_APP_ADV_DOWNLOAD_MEDIA_PERIOD.equals(key)
                || P_APP_ADV_NEXT_MEDIA_PERIOD.equals(key)
                || P_APP_ADS_MEIDA_PERIOD.equals(key)
                || P_APP_RTBADS_MEIDA_PERIOD.equals(key)
                || P_APP_RTBADS_DOWNLOAD_MEIDA_PERIOD.equals(key)
                || P_APP_ADS_DOWNLOAD_MEIDA_PERIOD.equals(key)
                || P_APP_STARTTIME.equals(key)
                || P_APP_LASTSTARTTIME.equals(key)
                || P_APP_ETHERNET_MAC.equals(key)
                || P_APP_WLAN_MAC.equals(key)
                || P_APP_OSS_AREA_ID.equals(key)
                || P_APP_AUTH_CODE.equals(key)
                || P_APP_SPLASH_PATH.equals(key)
                || P_APP_SPLASH_VERSION.equals(key)
                || P_APP_LOADING_VERSION.equals(key)
                || P_APP_LAST_UDISK_UPDATE_TIME.equals(key)
                || P_APP_LOADING_VERSION.equals(key)
                || P_APP_DOWNLOAD_NET_SPEED.equals(key)
                || P_APP_NETTY_URL.equals(key)
                || P_APP_BIRTHDAY_ONDEMAND_DOWNLOAD_PERIOD.equals(key)
                || P_APP_BIRTHDAY_ONDEMAND_PERIOD.equals(key)
                || P_APP_FORSCREEN_ADS_PERIOD.equals(key)
                || P_APP_QRCODE_GIFBG.equals(key)
                || P_APP_ACTIVITY_ADS_PERIOD.equals(key)
                || P_APP_SELECT_CONTENT_PERIOD.equals(key)
                || P_APP_HOT_CONTENT_PERIOD.equals(key)
                || P_APP_WELCOME_RESOURCE_PERIOD.equals(key)
                || P_APP_SHOP_GOODS_ADS_PERIOD.equals(key)
                || P_APP_STORE_SALE_ADS_PERIOD.equals(key)) {

            mPreference.saveStringKey(key, (String) updateItem.second);
        } else if (P_APP_BOX_CAROUSEL_VOLUME.equals(key) ||
                P_APP_BOX_CONTENT_DEMAND_VOLUME.equals(key) ||
                P_APP_BOX_PRO_DEMAND_VOLUME.equals(key) ||
                P_APP_BOX_IMG_FORSCREEN_VOLUME.equals(key) ||
                P_APP_BOX_VIDEO_FORSCREEN_VOLUME.equals(key) ||
                P_APP_BOX_TV_VOLUME.equals(key) ||
                P_APP_TV_CAROUSEL_VOLUME.equals(key) ||
                P_APP_TV_CONTENT_DEMAND_VOLUME.equals(key) ||
                P_APP_TV_PRO_DEMAND_VOLUME.equals(key) ||
                P_APP_TV_IMG_FORSCREEN_VOLUME.equals(key) ||
                P_APP_TV_VIDEO_FORSCREEN_VOLUME.equals(key) ||
                P_APP_TV_DEFAULT_CHANNEL.equals(key) ||
                P_APP_TV_CURRENT_INPUT.equals(key) ||
                P_APP_SWITCHTIME.equals(key) ||
                P_APP_TV_SIZE.equals(key)||
                P_APP_NETTY_PORT.equals(key)||
                P_APP_SYSTEM_FORSCREEN_NUMS.equals(key)||
                P_APP_QRCODE_SHOW_TIME.equals(key)||
                P_APP_QRCODE_TAKT_TIME.equals(key)||
                P_APP_BOX_QRCODETYPE.equals(key)||
                P_APP_SCENCEADV_SHOW_NUM.equals(key)) {
            mPreference.saveIntKey(key, (int) updateItem.second);
        } else if (P_APP_SIMPLE_UPLOAD_SIZE.equals(key)){
            mPreference.saveLongKey(key, (long) updateItem.second);
        } else if (P_APP_USE_VIRTUAL_SP.equals(key)||
                P_APP_4G_NETWORK.equals(key)||
                P_APP_SHOW_MIMIPROGRAM.equals(key)||
                P_APP_SHOW_SIMPLE_MIMIPROGRAM.equals(key)||
                P_APP_OPEN_INTERACTSCREENAD.equals(key)||
                P_APP_SHOW_ANIM.equals(key)||
                P_APP_WIFI_HOTEL.equals(key)) {
            mPreference.saveBooleanKey(key, (boolean) updateItem.second);
        } else {
            String string = ObjectToString(updateItem.second);
            mPreference.saveStringKey(key, string);
        }
    }

    private Object getObj(String key) {
        String string = mPreference.loadStringKey(key, "");
        Object object = null;
        if (!TextUtils.isEmpty(string)) {
            try {
                object = StringToObject(string);
            } catch (Exception ex) {
                LogUtils.e("wang" + "异常" + ex.toString());
            }
        }
        return object;
    }

    private void setObj(String key, Object obj) {
        try {
            writePreference(new Pair<String, Object>(key, obj));
        } catch (Exception ex) {
            LogUtils.e("wang" + ex.toString());
        }
    }

    private String ObjectToString(Object obj) {
        String productBase64 = null;
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            productBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            LogUtils.e("错误" + "保存错误" + e.toString());
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return productBase64;
    }

    private Object StringToObject(String str) {
        Object obj = null;
        byte[] base64Bytes;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            String productBase64 = str;
            if (null == productBase64
                    || TextUtils.isEmpty(productBase64.trim())) {
                return null;
            }

            base64Bytes = Base64.decode(productBase64, Base64.DEFAULT);
            bais = new ByteArrayInputStream(base64Bytes);
            ois = new ObjectInputStream(bais);
            obj = ois.readObject();
        } catch (Exception e) {
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ois!=null){
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }


    public String getVersionName() {
        if (TextUtils.isEmpty(versionName)) {
            getApplicationInfo();
        }
        return versionName == null ? "" : versionName;
    }

    public int getVersionCode() {
        if (versionCode <= 0) {
            getApplicationInfo();
        }
        return versionCode;
    }


    public String getMacAddr() {
        if (TextUtils.isEmpty(macAddress)) {
            try {
                WifiManager wifi = (WifiManager) mContext
                        .getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifi.getConnectionInfo();
                macAddress = info.getMacAddress();
            } catch (Exception ex) {
                LogUtils.e(ex.toString());
            }
        }
        return macAddress;
    }

    /**
     * 返回设备相关信息
     */
    public String getDeviceInfo() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("versionname=");
        buffer.append(versionName);
        buffer.append(";versioncode=");
        buffer.append(versionCode);
        buffer.append(";macaddress=");
        buffer.append(getMacAddr());
        buffer.append(";buildversion=");
        buffer.append(buildVersion);

        TimeZone timeZone = TimeZone.getDefault();
        buffer.append(";systemtimezone=");
        buffer.append(timeZone.getID());

        return buffer.toString();
    }

    // 获取应用名字
    public String getAppName() {
        return appName;
    }

    public int getOsVersion() {
        return osVersion;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public String getModel() {
        return model;
    }

    public String getBrand() {
        return brand;
    }

    public String getRomVersion() {
        return romVersion;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getDebugType() {
        return debugType;
    }

    public void setDebugType(String debugType) {
        this.debugType = debugType;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        synchronized (Session.class) {
            this.serverInfo = serverInfo;
            writePreference(new Pair<>(P_APP_SERVER_INFO, serverInfo));
        }
    }

    public ArrayList<VersionInfo> getSPVersionInfo() {
        return mSPVersionInfo;
    }

    public void setSPVersionInfo(ArrayList<VersionInfo> SPVersionInfo) {
        mSPVersionInfo = SPVersionInfo;
        writePreference(new Pair<>(P_APP_SP_VERSION_INFO, mSPVersionInfo));
    }

    public int getTvSize() {
        return tvSize;
    }

    public void setTvSize(int tvSize) {
        this.tvSize = tvSize;
        writePreference(new Pair<>(P_APP_TV_SIZE, tvSize));
    }

    public int getBoxCarouselVolume() {
        return boxCarouselVolume;
    }

    public void setBoxCarouselVolume(int boxCarouselVolume) {
        this.boxCarouselVolume = boxCarouselVolume;
        writePreference(new Pair<>(P_APP_BOX_CAROUSEL_VOLUME,boxCarouselVolume));
    }

    public int getBoxContentDemandVolume() {
        return boxContentDemandVolume;
    }

    public void setBoxContentDemandVolume(int boxContentDemandVolume) {
        this.boxContentDemandVolume = boxContentDemandVolume;
        writePreference(new Pair<>(P_APP_BOX_CONTENT_DEMAND_VOLUME,boxContentDemandVolume));
    }

    public int getBoxProDemandVolume() {
        return boxProDemandVolume;
    }

    public void setBoxProDemandVolume(int boxProDemandVolume) {
        this.boxProDemandVolume = boxProDemandVolume;
        writePreference(new Pair<>(P_APP_BOX_PRO_DEMAND_VOLUME,boxProDemandVolume));
    }

    public int getBoxImgFroscreenVolume() {
        return boxImgFroscreenVolume;
    }

    public void setBoxImgFroscreenVolume(int boxImgFroscreenVolume) {
        this.boxImgFroscreenVolume = boxImgFroscreenVolume;
        writePreference(new Pair<>(P_APP_BOX_IMG_FORSCREEN_VOLUME,boxImgFroscreenVolume));
    }

    public int getBoxVideoFroscreenVolume() {
        return boxVideoFroscreenVolume;
    }

    public void setBoxVideoFroscreenVolume(int boxVideoFroscreenVolume) {
        this.boxVideoFroscreenVolume = boxVideoFroscreenVolume;
        writePreference(new Pair<>(P_APP_BOX_VIDEO_FORSCREEN_VOLUME,boxVideoFroscreenVolume));
    }

    public int getBoxTvVolume() {
        return boxTvVolume;
    }

    public void setBoxTvVolume(int boxTvVolume) {
        this.boxTvVolume = boxTvVolume;
        writePreference(new Pair<>(P_APP_BOX_TV_VOLUME,boxTvVolume));
    }

    public int getTvCarouselVolume() {
        return tvCarouselVolume;
    }

    public void setTvCarouselVolume(int tvCarouselVolume) {
        this.tvCarouselVolume = tvCarouselVolume;
        writePreference(new Pair<>(P_APP_TV_CAROUSEL_VOLUME,tvCarouselVolume));
    }

    public int getTvContentDemandVolume() {
        return tvContentDemandVolume;
    }

    public void setTvContentDemandVolume(int tvContentDemandVolume) {
        this.tvContentDemandVolume = tvContentDemandVolume;
        writePreference(new Pair<>(P_APP_TV_CONTENT_DEMAND_VOLUME,tvContentDemandVolume));
    }

    public int getTvProDemandVolume() {
        return tvProDemandVolume;
    }

    public void setTvProDemandVolume(int tvProDemandVolume) {
        this.tvProDemandVolume = tvProDemandVolume;
        writePreference(new Pair<>(P_APP_TV_PRO_DEMAND_VOLUME,tvProDemandVolume));
    }

    public int getTvImgFroscreenVolume() {
        return tvImgFroscreenVolume;
    }

    public void setTvImgFroscreenVolume(int tvImgFroscreenVolume) {
        this.tvImgFroscreenVolume = tvImgFroscreenVolume;
        writePreference(new Pair<>(P_APP_TV_IMG_FORSCREEN_VOLUME,tvImgFroscreenVolume));
    }

    public int getTvVideoFroscreenVolume() {
        return tvVideoFroscreenVolume;
    }

    public void setTvVideoFroscreenVolume(int tvVideoFroscreenVolume) {
        this.tvVideoFroscreenVolume = tvVideoFroscreenVolume;
        writePreference(new Pair<>(P_APP_TV_VIDEO_FORSCREEN_VOLUME,tvVideoFroscreenVolume));
    }

    public String getBoiteName() {
        return boiteName;
    }

    public void setBoiteName(String boiteName) {
        this.boiteName = boiteName;
        writePreference(new Pair<>(P_APP_BOITENAME, boiteName));
    }

    public String getBoiteId() {
        return boiteId == null ? "" : boiteId;
    }

    public void setBoiteId(String boiteId) {
        this.boiteId = boiteId;
        writePreference(new Pair<>(P_APP_BOITEID, boiteId));
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
        writePreference(new Pair<>(P_APP_ROOMNAME, roomName));
    }

    public String getRoomId() {
        return roomId == null ? "" : roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
        writePreference(new Pair<>(P_APP_ROOMID, roomId));
    }

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
        writePreference(new Pair<>(P_APP_BOXID, this.boxId));
    }

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
        writePreference(new Pair<>(P_APP_BOXNAME, this.boxName));
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
        writePreference(new Pair<>(P_APP_ROOM_TYPE, this.roomType));
    }

    public String getProNextMediaPubTime() {
        return proNextMediaPubTime;
    }

    public void setProNextMediaPubTime(String proNextMediaPubTime) {
        this.proNextMediaPubTime = proNextMediaPubTime;
        writePreference(new Pair<>(P_APP_PRO_NEXT_MEDIA_PUBTIME,proNextMediaPubTime));
    }

    public void setProPeriod(String proPeriod) {
        this.proPeriod = proPeriod;
        writePreference(new Pair<>(P_APP_PRO_MEDIA_PERIOD,proPeriod));
    }

    public void setAdvPeriod(String advPeriod) {
        this.advPeriod = advPeriod;
        writePreference(new Pair<>(P_APP_ADV_MEDIA_PERIOD,advPeriod));
    }

    public void setAdsPeriod(String adsPeriod) {
        this.adsPeriod = adsPeriod;
        writePreference(new Pair<>(P_APP_ADS_MEIDA_PERIOD,adsPeriod));
    }

    public void setRtbAdsPeriod(String adsPeriod) {
        this.rtbadsPeriod = adsPeriod;
        writePreference(new Pair<>(P_APP_RTBADS_MEIDA_PERIOD,adsPeriod));
    }

    public String getPolyAdsPeriod() {
        return polyAdsPeriod;
    }

    public void setPolyAdsPeriod(String polyAdsPeriod) {
        this.polyAdsPeriod = polyAdsPeriod;
        writePreference(new Pair<>(P_APP_POLYADS_MEDIA_PERIOD,polyAdsPeriod));
    }

    public String getPolyAdsDownloadPeriod() {
        return polyAdsDownloadPeriod;
    }

    public void setPolyAdsDownloadPeriod(String polyAdsDownloadPeriod) {
        this.polyAdsDownloadPeriod = polyAdsDownloadPeriod;
        writePreference(new Pair<>(P_APP_POLYADS_DOWNLOAD_MEDIA_PERIOD,polyAdsDownloadPeriod));
    }

    public void setProDownloadPeriod(String proDownloadPeriod) {
        this.proDownloadPeriod = proDownloadPeriod;
        writePreference(new Pair<>(P_APP_PRO_DOWNLOAD_MEDIA_PERIOD,proDownloadPeriod));
    }

    public void setAdvDownloadPeriod(String advDownloadPeriod) {
        this.advDownloadPeriod = advDownloadPeriod;
        writePreference(new Pair<>(P_APP_ADV_DOWNLOAD_MEDIA_PERIOD,advDownloadPeriod));
    }

    public void setAdsDownloadPeriod(String adsDownloadPeriod) {
        this.adsDownloadPeriod = adsDownloadPeriod;
        writePreference(new Pair<>(P_APP_ADS_DOWNLOAD_MEIDA_PERIOD, adsDownloadPeriod));
    }

    public void setProNextPeriod(String proNextPeriod) {
        this.proNextPeriod = proNextPeriod;
        writePreference(new Pair<>(P_APP_PRO_NEXT_MEDIA_PERIOD,proNextPeriod));
    }

    public void setAdsNextPeriod(String adsNextPeriod) {
        this.adsNextPeriod = adsNextPeriod;
    }

    public void setAdvNextPeriod(String advNextPeriod) {
        this.advNextPeriod = advNextPeriod;
        writePreference(new Pair<>(P_APP_ADV_NEXT_MEDIA_PERIOD, advNextPeriod));
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        setLastStartTime(this.startTime);
        this.startTime = startTime;
        writePreference(new Pair<>(P_APP_STARTTIME, startTime));
        LogFileUtil.writeBootInfo(startTime);
    }

    public String getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(String lastStartTime) {
        this.lastStartTime = lastStartTime;
        writePreference(new Pair<>(P_APP_LASTSTARTTIME, lastStartTime));
    }

    public int getSwitchTime() {
        return switchTime;
    }

    public void setSwitchTime(int switchTime) {
        this.switchTime = switchTime;
        writePreference(new Pair<>(P_APP_SWITCHTIME, switchTime));
    }

    public int getTvCurrentChannelNumber() {
        return mTvCurrentChannelNumber;
    }

    public void setTvCurrentChannelNumber(int tvCurrentChannelNumber) {
        mTvCurrentChannelNumber = tvCurrentChannelNumber;
    }

    public int getTvDefaultChannelNumber() {
        return mTvDefaultChannelNumber;
    }

    public void setTvDefaultChannelNumber(int tvDefaultChannelNumber) {
        mTvCurrentChannelNumber = mTvDefaultChannelNumber = tvDefaultChannelNumber;
        writePreference(new Pair<>(P_APP_TV_DEFAULT_CHANNEL, tvDefaultChannelNumber));
    }

    public int getTvInputSource() {
        return mTvInputSource;
    }

    public void setTvInputSource(int tvInputSource) {
        mTvInputSource = tvInputSource;
        writePreference(new Pair<>(P_APP_TV_CURRENT_INPUT, tvInputSource));
    }

    public String getEthernetMac() {
        if (TextUtils.isEmpty(mEthernetMac)) {
            String mac = AppUtils.getEthernetMacAddr();
            if (!TextUtils.isEmpty(mac)) {
                mEthernetMac = mac.replaceAll(":", "");
                writePreference(new Pair<>(P_APP_ETHERNET_MAC, mEthernetMac));
            }
        }
        return mEthernetMac == null ? "" : mEthernetMac;
    }

    public String getEthernetMacWithColon() {
        if (TextUtils.isEmpty(mEthernetMacWithColon)) {
            mEthernetMacWithColon = AppUtils.getEthernetMacAddr();
            writePreference(new Pair<>(P_APP_ETHERNET_MAC_WITH_COLON, mEthernetMacWithColon));
        }
        return mEthernetMacWithColon == null ? "" : mEthernetMacWithColon;
    }

    public String getWlanMac() {
        if (TextUtils.isEmpty(mWlanMac)) {
            mWlanMac = MacAddressUtils.getWifiMac();
            writePreference(new Pair<>(P_APP_WLAN_MAC, mWlanMac));
        }
        return mWlanMac;
    }


    public String getOssAreaId() {
        return ossAreaId;
    }

    public void setOssAreaId(String ossAreaId) {
        this.ossAreaId = ossAreaId;
        writePreference(new Pair<>(P_APP_OSS_AREA_ID, ossAreaId));
    }

    public boolean isConnectedToSP() {
        return mIsConnectedToSP;
    }

    public void setConnectedToSP(boolean connectedToSP) {
        mIsConnectedToSP = connectedToSP;
    }

    public String getAuthCode() {
        return mAuthCode;
    }

    public void setAuthCode(String authCode) {
        if (!TextUtils.isEmpty(authCode) && !authCode.equals(mAuthCode)) {
            writePreference(new Pair<>(P_APP_AUTH_CODE, authCode));
        }
        mAuthCode = authCode;
    }

    public String getSplashPath() {
        return mSplashPath;
    }

    public void setSplashPath(String splashPath) {
        mSplashPath = splashPath;
        writePreference(new Pair<>(P_APP_SPLASH_PATH, splashPath));
    }

    public String getSplashVersion() {
        return mSplashVersion == null ? "" : mSplashVersion;
    }

    public void setSplashVersion(String splashVersion) {
        if (TextUtils.isEmpty(mSplashVersion) || !mSplashVersion.equals(splashVersion)) {
            mSplashVersion = splashVersion;
            writePreference(new Pair<>(P_APP_SPLASH_VERSION, splashVersion));
        }
    }

    public String getLoadingVersion() {
        return mLoadingVersion == null ? "" : mLoadingVersion;
    }

    public void setLoadingVersion(String loadingVersion) {
        if (TextUtils.isEmpty(mLoadingVersion) || !mLoadingVersion.equals(loadingVersion)) {
            mLoadingVersion = loadingVersion;
            writePreference(new Pair<>(P_APP_LOADING_VERSION, loadingVersion));
        }
    }


    public String getUsbPath() {
        return usbPath;
    }

    public void setUsbPath(String usbPath) {
        this.usbPath = usbPath;
    }

    public String getBirthdayOndemandDownloadPeriod() {
        return birthdayOndemandDownloadPeriod;
    }

    public void setBirthdayOndemandDownloadPeriod(String birthdayOndemandDownloadPeriod) {
        this.birthdayOndemandDownloadPeriod = birthdayOndemandDownloadPeriod;
        writePreference(new Pair<>(P_APP_BIRTHDAY_ONDEMAND_DOWNLOAD_PERIOD,birthdayOndemandDownloadPeriod));

    }

    public String getBirthdayOndemandPeriod() {
        return birthdayOndemandPeriod;
    }

    public void setBirthdayOndemandPeriod(String birthdayOndemandPeriod) {
        this.birthdayOndemandPeriod = birthdayOndemandPeriod;
        writePreference(new Pair<>(P_APP_BIRTHDAY_ONDEMAND_PERIOD,birthdayOndemandPeriod));
    }

    public String getInteractionAdsPeriod() {
        return interactionAdsPeriod;
    }

    public void setInteractionAdsPeriod(String interactionAdsPeriod) {
        this.interactionAdsPeriod = interactionAdsPeriod;
        writePreference(new Pair<>(P_APP_FORSCREEN_ADS_PERIOD,interactionAdsPeriod));
    }

    public String getActivityAdsPeriod() {
        return activityAdsPeriod;
    }

    public void setActivityAdsPeriod(String activityAdsPeriod) {
        this.activityAdsPeriod = activityAdsPeriod;
        writePreference(new Pair<>(P_APP_ACTIVITY_ADS_PERIOD,activityAdsPeriod));
    }

    public String getShopGoodsAdsPeriod() {
        return shopGoodsAdsPeriod;
    }

    public void setShopGoodsAdsPeriod(String shopGoodsAdsPeriod) {
        this.shopGoodsAdsPeriod = shopGoodsAdsPeriod;
        writePreference(new Pair<>(P_APP_SHOP_GOODS_ADS_PERIOD,shopGoodsAdsPeriod));
    }

    public String getSelectContentPeriod() {
        return selectContentPeriod;
    }

    public void setSelectContentPeriod(String selectContentPeriod) {
        this.selectContentPeriod = selectContentPeriod;
        writePreference(new Pair<>(P_APP_SELECT_CONTENT_PERIOD,selectContentPeriod));
    }

    public String getHotContentPeriod() {
        return hotContentPeriod;
    }

    public void setHotContentPeriod(String hotContentPeriod) {
        this.hotContentPeriod = hotContentPeriod;
        writePreference(new Pair<>(P_APP_HOT_CONTENT_PERIOD,hotContentPeriod));
    }

    public String getWelcomeResourcePeriod() {
        return welcomeResourcePeriod;
    }

    public void setWelcomeResourcePeriod(String welcomeResourcePeriod) {
        this.welcomeResourcePeriod = welcomeResourcePeriod;
        writePreference(new Pair<>(P_APP_WELCOME_RESOURCE_PERIOD,welcomeResourcePeriod));
    }

    public String getStoreSaleAdsPeriod() {
        return storeSaleAdsPeriod;
    }

    public void setStoreSaleAdsPeriod(String storeSaleAdsPeriod) {
        this.storeSaleAdsPeriod = storeSaleAdsPeriod;
        writePreference(new Pair<>(P_APP_STORE_SALE_ADS_PERIOD,storeSaleAdsPeriod));
    }

    public String getNetSpeed() {
        return netSpeed;
    }

    public void setNetSpeed(String netSpeed) {
        this.netSpeed = netSpeed;
        writePreference(new Pair<>(P_APP_DOWNLOAD_NET_SPEED,netSpeed));
    }

    public boolean isShowMiniProgramIcon() {
        return isShowMiniProgramIcon;
    }

    public void setShowMiniProgramIcon(boolean showMiniProgramIcon) {
        isShowMiniProgramIcon = showMiniProgramIcon;
        writePreference(new Pair<>(P_APP_SHOW_MIMIPROGRAM,showMiniProgramIcon));
    }

    public boolean isShowSimpleMiniProgramIcon() {
        return isShowSimpleMiniProgramIcon;
    }

    public void setShowSimpleMiniProgramIcon(boolean showSimpleMiniProgramIcon) {
        isShowSimpleMiniProgramIcon = showSimpleMiniProgramIcon;
        writePreference(new Pair<>(P_APP_SHOW_SIMPLE_MIMIPROGRAM,showSimpleMiniProgramIcon));
    }

    public boolean isOpenInteractscreenad() {
        return isOpenInteractscreenad;
    }

    public void setOpenInteractscreenad(boolean openInteractscreenad) {
        isOpenInteractscreenad = openInteractscreenad;
        writePreference(new Pair<>(P_APP_OPEN_INTERACTSCREENAD,openInteractscreenad));
    }

    public int getSystemSappForscreenNums() {
        return systemSappForscreenNums;
    }

    public void setSystemSappForscreenNums(int systemSappForscreenNums) {
        this.systemSappForscreenNums = systemSappForscreenNums;
        writePreference(new Pair<>(P_APP_SYSTEM_FORSCREEN_NUMS,systemSappForscreenNums));
    }

    public long getSimple_upload_size() {
        return simple_upload_size;
    }

    public void setSimple_upload_size(long simple_upload_size) {
        this.simple_upload_size = simple_upload_size;
        writePreference(new Pair<>(P_APP_SIMPLE_UPLOAD_SIZE,simple_upload_size));
    }

    public int getScenceadv_show_num() {
        return scenceadv_show_num;
    }

    public void setScenceadv_show_num(int scenceadv_show_num) {
        this.scenceadv_show_num = scenceadv_show_num;
        writePreference(new Pair<>(P_APP_SCENCEADV_SHOW_NUM,scenceadv_show_num));
    }

    public int getQrcode_showtime() {
        return qrcode_showtime;
    }

    public void setQrcode_showtime(int qrcode_showtime) {
        this.qrcode_showtime = qrcode_showtime;
        writePreference(new Pair<>(P_APP_QRCODE_SHOW_TIME,qrcode_showtime));
    }

    public int getQrcode_takttime() {
        return qrcode_takttime;
    }

    public void setQrcode_takttime(int qrcode_takttime) {
        this.qrcode_takttime = qrcode_takttime;
        writePreference(new Pair<>(P_APP_QRCODE_TAKT_TIME,qrcode_takttime));
    }

    public boolean isHeartbeatMiniNetty() {
        return isHeartbeatMiniNetty;
    }

    public void setHeartbeatMiniNetty(boolean heartbeatMiniNetty) {
        isHeartbeatMiniNetty = heartbeatMiniNetty;
    }

    public String getNettyUrl() {
        return nettyUrl;
    }

    public void setNettyUrl(String nettyUrl) {
        this.nettyUrl = nettyUrl;
        writePreference(new Pair<>(P_APP_NETTY_URL,nettyUrl));
    }

    public int getNettyPort() {
        return nettyPort;
    }

    public void setNettyPort(int nettyPort) {
        this.nettyPort = nettyPort;
        writePreference(new Pair<>(P_APP_NETTY_PORT,nettyPort));
    }

    public int getActivityPlayType() {
        return activityPlayType;
    }

    public void setActivityPlayType(int activityPlayType) {
        this.activityPlayType = activityPlayType;
    }

    public boolean isWhether4gNetwork() {
        return whether4gNetwork;
    }

    public void setWhether4gNetwork(boolean whether4gNetwork) {
        this.whether4gNetwork = whether4gNetwork;
        writePreference(new Pair<>(P_APP_4G_NETWORK,whether4gNetwork));
    }

    public String getQrcodeGifBgPath() {
        return qrcodeGifBgPath;
    }

    public void setQrcodeGifBgPath(String qrcodeGifBgPath) {
        this.qrcodeGifBgPath = qrcodeGifBgPath;
        writePreference(new Pair<>(P_APP_QRCODE_GIFBG,qrcodeGifBgPath));
    }

    public int getQrcodeType() {
        return qrcodeType;
    }

    public void setQrcodeType(int qrcodeType) {
        this.qrcodeType = qrcodeType;
        writePreference(new Pair<>(P_APP_BOX_QRCODETYPE,qrcodeType));
    }

    public ProjectionGuideImg getGuideImg() {
        return guideImg;
    }

    public void setGuideImg(ProjectionGuideImg guideImg) {
        this.guideImg = guideImg;
        writePreference(new Pair<>(P_APP_PROJECTION_GUIDE,guideImg));
    }

    public boolean isShowAnimQRcode() {
        return isShowAnimQRcode;
    }

    public void setShowAnimQRcode(boolean showAnimQRcode) {
        isShowAnimQRcode = showAnimQRcode;
        writePreference(new Pair<>(P_APP_SHOW_ANIM,showAnimQRcode));
    }

    public boolean isWifiHotel() {
        return isWifiHotel;
    }

    public void setWifiHotel(boolean wifiHotel) {
        isWifiHotel = wifiHotel;
        writePreference(new Pair<>(P_APP_WIFI_HOTEL,wifiHotel));
    }

    public boolean isSaleHotel() {
        return isSaleHotel;
    }

    public void setSaleHotel(boolean saleHotel) {
        isSaleHotel = saleHotel;
    }

    public List<String> getQrcodeTipList() {
        return qrcodeTipList;
    }

    public void setQrcodeTipList(List<String> qrcodeTipList) {
        this.qrcodeTipList = qrcodeTipList;
    }

    public int getNormalUseWechat() {
        return normalUseWechat;
    }

    public void setNormalUseWechat(int normalUseWechat) {
        this.normalUseWechat = normalUseWechat;
    }

    public SellWineActivityBean getSellWineBean() {
        return sellWineBean;
    }

    public void setSellWineBean(SellWineActivityBean sellWineBean) {
        this.sellWineBean = sellWineBean;
    }

    //机顶盒轮播音量
    public static final String P_APP_BOX_CAROUSEL_VOLUME = "com.savor.box.carousel.volume";
    //机顶盒用户内容点播音量
    public static final String P_APP_BOX_CONTENT_DEMAND_VOLUME = "com.savor.box.content.demand.volume";
    //机顶盒公司节目点播音量
    public static final String P_APP_BOX_PRO_DEMAND_VOLUME = "com.savor.box.pro.demand.volume";
    //机顶盒图片投屏音量
    public static final String P_APP_BOX_IMG_FORSCREEN_VOLUME = "com.savor.box.img.froscreen.volume";
    //机顶盒视频投屏音量
    public static final String P_APP_BOX_VIDEO_FORSCREEN_VOLUME = "com.savor.box.video.froscreen.volume";
    //机顶盒电视音量
    public static final String P_APP_BOX_TV_VOLUME = "com.savor.box.tv_volume";
    //电视机轮播音量
    public static final String P_APP_TV_CAROUSEL_VOLUME = "com.savor.tv.carousel.volume";
    //机顶盒用户内容点播音量
    public static final String P_APP_TV_CONTENT_DEMAND_VOLUME = "com.savor.tv.content.demand.volume";
    //机顶盒公司节目点播音量
    public static final String P_APP_TV_PRO_DEMAND_VOLUME = "com.savor.tv.pro.demand.volume";
    //机顶盒图片投屏音量
    public static final String P_APP_TV_IMG_FORSCREEN_VOLUME = "com.savor.tv.img.froscreen.volume";
    //机顶盒视频投屏音量
    public static final String P_APP_TV_VIDEO_FORSCREEN_VOLUME = "com.savor.tv.video.froscreen.volume";
    //酒楼名称
    public static final String P_APP_BOITENAME = "com.savor.ads.boiteName";
    //酒楼ID
    public static final String P_APP_BOITEID = "com.savor.ads.boiteId";
    //包间名称
    public static final String P_APP_ROOMNAME = "com.savor.ads.roomName";
    //包间ID
    public static final String P_APP_ROOMID = "com.savor.ads.roomId";
    // 机顶盒名称
    public static final String P_APP_BOXNAME = "com.savor.ads.boxName";
    // 机顶盒ID
    public static final String P_APP_BOXID = "com.savor.ads.boxId";
    // 包间类型
    public static final String P_APP_ROOM_TYPE = "com.savor.ads.roomType";
    /**节目单期号(含节目内容)*/
    public static final String P_APP_PRO_MEDIA_PERIOD = "com.savor.pro.mediaPeriod";
    public static final String P_APP_PRO_DOWNLOAD_MEDIA_PERIOD = "com.savor.pro.downloadMediaPeriod";
    public static final String P_APP_PRO_NEXT_MEDIA_PERIOD= "com.savor.pro.nextMediaPeriod";
    public static final String P_APP_PRO_NEXT_MEDIA_PUBTIME = "com.savor.pro.nextPubTime";
    /**宣传片相关期号*/
    public static final String P_APP_ADV_MEDIA_PERIOD = "com.savor.adv.mediaPeriod";
    public static final String P_APP_ADV_DOWNLOAD_MEDIA_PERIOD = "com.savor.adv.downloadMediaPeriod";
    public static final String P_APP_ADV_NEXT_MEDIA_PERIOD = "com.savor.adv.nextMediaPeriod";
    /**广告相关期号*/
    public static final String P_APP_ADS_MEIDA_PERIOD = "com.savor.ads.mediaPeriod";
    public static final String P_APP_ADS_DOWNLOAD_MEIDA_PERIOD = "com.savor.ads.downloadMediaPeriod";
    public static final String P_APP_RTBADS_MEIDA_PERIOD = "com.savor.ads.rtbMediaPeriod";
    public static final String P_APP_RTBADS_DOWNLOAD_MEIDA_PERIOD = "com.savor.ads.rtbDownloadMediaPeriod";
    public static final String P_APP_POLYADS_MEDIA_PERIOD = "com.savor.ads.polyMediaPeriod";
    public static final String P_APP_POLYADS_DOWNLOAD_MEDIA_PERIOD = "com.saovr.ads.polyDownloadMediaPeriod";

    /**生日歌期号KEY**/
    public static final String P_APP_BIRTHDAY_ONDEMAND_DOWNLOAD_PERIOD = "com.savor.ads.birthday_ondemand_download_period";
    public static final String P_APP_BIRTHDAY_ONDEMAND_PERIOD = "com.savor.ads.birthday_ondemand_period";
    /**互动广告期号KEY*/
    public static final String P_APP_FORSCREEN_ADS_PERIOD = "com.savor.ads.forsrceen_ads_period";
    /**活动商品广告期号KEY*/
    public static final String P_APP_ACTIVITY_ADS_PERIOD = "com.savor.ads.activity_ads_period";
    /**商城商品广告期号KEY*/
    public static final String P_APP_SHOP_GOODS_ADS_PERIOD = "com.savor.ads.shop_goods_ads_period";
    /**用户精选内容期号KEY*/
    public static final String P_APP_SELECT_CONTENT_PERIOD = "com.savor.ads.select_content_period";
    /**用户热播内容期号KEY*/
    public static final String P_APP_HOT_CONTENT_PERIOD = "com.savor.ads.hot_content_period";
    /**欢迎词内容期号KEY*/
    public static final String P_APP_WELCOME_RESOURCE_PERIOD = "com.savor.ads.welcome_resource_period";
    /**酒水平台广告期号KEY*/
    public static final String P_APP_STORE_SALE_ADS_PERIOD = "com.savor.ads.store_sale_ads_period";
    //开机时间
    public static final String P_APP_STARTTIME = "com.savor.ads.startTime";
    public static final String P_APP_LASTSTARTTIME = "com.savor.ads.laststartTime";
    //切换时间
    public static final String P_APP_SWITCHTIME = "com.savor.ads.switchtime";
    // 电视默认频道KEY
    public static final String P_APP_TV_DEFAULT_CHANNEL = "com.savor.ads.tvDefaultChannel";
    // 电视当前信号源KEY
    public static final String P_APP_TV_CURRENT_INPUT = "com.savor.ads.tvCurrentInput";
    // 小平台信息KEY
    public static final String P_APP_SERVER_INFO = "com.savor.ads.serverInfo";
    // 以太网卡MAC地址KEY
    public static final String P_APP_ETHERNET_MAC = "com.savor.ads.ethernetMac";
    public static final String P_APP_ETHERNET_MAC_WITH_COLON = "com.savor.ads.ethernetMacWithColon";
    // 无线网卡MAC地址KEY
    public static final String P_APP_WLAN_MAC = "com.savor.ads.wlanMac";
    //oss区域ID
    public static final String P_APP_OSS_AREA_ID = "com.savor.ads.oss.areaId";
    //启动图版本key
    public static final String P_APP_SPLASH_VERSION = "com.savor.ads.splashVersion";
    //呼玛验证码key
    public static final String P_APP_AUTH_CODE = "com.savor.ads.authCode";
    //启动图路径key
    public static final String P_APP_SPLASH_PATH = "com.savor.ads.splashPath";
    //加载图版本key
    public static final String P_APP_LOADING_VERSION = "com.savor.ads.loadingVersion";
    //小平台中的各种版本信息key
    public static final String P_APP_SP_VERSION_INFO = "com.savor.ads.spVersionInfo";
    public static final String P_APP_TV_SIZE = "com.savor.ads.tvSize";
    // 是否使用虚拟小平台key
    public static final String P_APP_USE_VIRTUAL_SP = "com.savor.ads.use_virtual_sp";
    //上次U盘动作时间
    public static final String P_APP_LAST_UDISK_UPDATE_TIME = "com.savor.ads.last_udisk_update_time";

    public static final String P_APP_DOWNLOAD_NET_SPEED = "com.savor.download.net_speed";

    public static final String P_APP_SHOW_MIMIPROGRAM = "com.savor.ads.show.miniprogram";
    public static final String P_APP_SHOW_SIMPLE_MIMIPROGRAM = "com.savor.ads.show.simpleminiprogram";
    public static final String P_APP_OPEN_INTERACTSCREENAD = "com.savor.ads.open.interactscreenad";
    public static final String P_APP_SYSTEM_FORSCREEN_NUMS = "com.savor.ads.system.forscreen.nums";
    public static final String P_APP_NETTY_URL = "com.savor.ads.netty_url";
    public static final String P_APP_NETTY_PORT = "com.savor.ads.netty_port";

    public static final String P_APP_4G_NETWORK = "com.savor.ads.4g.network";

    public static final String P_APP_BOX_QRCODETYPE = "com.savor.ads.box.qrcodeType";
    public static final String P_APP_SIMPLE_UPLOAD_SIZE = "com.savor.ads.simple.uploadSize";
    public static final String P_APP_SCENCEADV_SHOW_NUM = "com.savor.ads.simple.scenceadv_show_num";
    public static final String P_APP_QRCODE_SHOW_TIME = "com.savor.ads.qrcode.showtime";
    public static final String P_APP_QRCODE_TAKT_TIME = "com.savor.ads.qrcode.takttime";
    public static final String P_APP_QRCODE_GIFBG = "com.savor.ads.qrcode.gifbg";
    public static final String P_APP_PROJECTION_GUIDE = "com.savor.ads.projection.guide";
    public static final String P_APP_SHOW_ANIM = "com.savor.ads.show.anim";
    public static final String P_APP_WIFI_HOTEL = "com.savor.ads.wifi.hotel";

    public String getAdsPeriod() {
        return adsPeriod == null ? "" : adsPeriod;
    }

    public String getRtbadsPeriod() {
        return rtbadsPeriod == null ? "" : rtbadsPeriod;
    }

    public String getAdvPeriod() {
        return advPeriod == null ? "" : advPeriod;
    }

    public String getProPeriod() {
        return proPeriod == null ? "" : proPeriod;
    }


    public String getAdsDownloadPeriod() {
        return adsDownloadPeriod == null ? "" : adsDownloadPeriod;
    }

    public String getAdvDownloadPeriod() {
        return advDownloadPeriod == null ? "" : advDownloadPeriod;
    }

    public String getProDownloadPeriod() {
        return proDownloadPeriod == null ? "" : proDownloadPeriod;
    }

    public String getAdsNextPeriod() {
        return adsNextPeriod == null ? "" : adsNextPeriod;
    }

    public String getAdvNextPeriod() {
        return advNextPeriod == null ? "" : advNextPeriod;
    }

    public String getProNextPeriod() {
        return proNextPeriod == null ? "" : proNextPeriod;
    }

    public boolean isUseVirtualSp() {
        return mUseVirtualSp;
    }

    public void setUseVirtualSp(boolean useVirtualSp) {
        if (useVirtualSp != mUseVirtualSp) {
            mUseVirtualSp = useVirtualSp;
            writePreference(new Pair<>(P_APP_USE_VIRTUAL_SP, mUseVirtualSp));
        }
    }

    public String getLastUDiskUpdateTime() {
        return lastUDiskUpdateTime;
    }

    public void setLastUDiskUpdateTime(String lastUDiskUpdateTime) {
        this.lastUDiskUpdateTime = lastUDiskUpdateTime;
        writePreference(new Pair<>(P_APP_LAST_UDISK_UPDATE_TIME, lastUDiskUpdateTime));
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLan_ip() {
        return lan_ip;
    }

    public void setLan_ip(String lan_ip) {
        this.lan_ip = lan_ip;
    }

    public String getLan_mac() {
        return lan_mac;
    }

    public void setLan_mac(String lan_mac) {
        this.lan_mac = lan_mac;
    }
}
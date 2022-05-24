package com.savor.ads.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.jar.savor.box.ServiceUtil;
import com.jar.savor.box.vo.VolumeResponseVo;
import com.savor.ads.BuildConfig;
import com.savor.ads.bean.SeckillGoodsBean;

import com.savor.ads.bean.SeckillGoodsResult;
import com.savor.ads.service.RemoteService;
import com.savor.ads.R;
import com.savor.ads.SavorApplication;
import com.savor.ads.bean.AdInfo;
import com.savor.ads.bean.AdMasterResult;
import com.savor.ads.bean.AdPayloadBean;
import com.savor.ads.bean.AdRequestInfo;
import com.savor.ads.bean.AdSlot;
import com.savor.ads.bean.AdTrack;
import com.savor.ads.bean.AdType;
import com.savor.ads.bean.AdsMeiSSPBean;
import com.savor.ads.bean.AdsMeiSSPResult;
import com.savor.ads.bean.BaiduAdLocalBean;
import com.savor.ads.bean.JDmomediaBean;
import com.savor.ads.bean.JDmomediaLocalBean;
import com.savor.ads.bean.JDmomediaMaterial;
import com.savor.ads.bean.JDmomediaResult;
import com.savor.ads.bean.JsonBean;
import com.savor.ads.bean.MediaDevice;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.MediaNetWork;
import com.savor.ads.bean.MediaUdId;
import com.savor.ads.bean.MeiAdLocalBean;
import com.savor.ads.bean.NetworkUtils;
import com.savor.ads.bean.OOHLinkAdLocalBean;
import com.savor.ads.bean.RtbRequest;
import com.savor.ads.bean.YishouAdLocalBean;
import com.savor.ads.bean.ZmengAdLocalBean;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.customview.SavorVideoView;
import com.savor.ads.customview.StrokeTextView;
import com.savor.ads.database.DBHelper;
import com.savor.ads.dialog.AtlasDialog;
import com.savor.ads.dialog.PlayListDialog;
import com.savor.ads.dialog.ScanRedEnvelopeQrCodeDialog;
import com.savor.ads.log.LogReportUtil;
import com.savor.ads.okhttp.coreProgress.download.FileDownloader;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.BaiduAdsResponseCode;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.DeviceUtils;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.KeyCode;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.MiniProgramQrCodeWindowManager;
import com.savor.ads.utils.QrCodeWindowManager;
import com.savor.ads.utils.ShellUtils;
import com.savor.ads.utils.ShowMessage;
import com.savor.ads.utils.TimeUtils;
import com.savor.ads.utils.ZmengAdsResponseCode;
import com.savor.tvlibrary.OutputResolution;
import com.savor.tvlibrary.TVOperatorFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import pl.droidsonroids.gif.GifImageView;
import tianshu.ui.api.TsUiApiV20171122;
import tianshu.ui.api.ZmtAPI;
import tianshu.ui.api.ZmtAdRequestUtil;

import static com.savor.ads.utils.ConstantValues.DSP_DOWNLOADING_FILES;

/**
 * 广告播放页面
 */
public class AdsPlayerActivity<T extends MediaLibBean> extends BaseActivity implements SavorVideoView.PlayStateCallback, ApiRequestListener, PlayListDialog.Callback {

    private static final String TAG = "AdsPlayerActivity";
    private SavorVideoView mSavorVideoView;
    private FrameLayout partakeDishLayout;
    private TextView pdCountdownTV;
    private StrokeTextView pdActivityNameTV;
    private FrameLayout prizeHeadLayout;
    private TextView lotteryTimeTV;
    private TextView lotteryNameTV;
    private ImageView imgView;
    private RelativeLayout priceLayout;
    private TextView goodsPriceTV;
    private RelativeLayout goodsTitleLayout;
    private TextView goodsTimeTV;
    private RelativeLayout storeSaleLayout;
    private LinearLayout wxProjectionTipLayout;
    private ImageView wxProjectionIconTipIV;
    private TextView wxProjectionTxtTipTV;
    //声音布局
    private RelativeLayout mVolumeRl;
    private TextView mVolumeTv;
    private ProgressBar mVolumePb;
    //跑马灯效果展示
    private RelativeLayout captionLayout;
    private TextView captionTipTV;
    //秒杀布局
    private RelativeLayout seckillFrontLayout;
    private TextView seckillCountdownFrontTV;
    private TextView hotelNameFrontTV;
    private ImageView seckillGifFrontIV;
    private ImageView seckillGoodsFrontIV;
    private TextView goodsJDPriceFrontTV;
    private TextView goodsSeckillPriceFrontTV;
    private RelativeLayout seckillBackLayout;
    private TextView seckillCountdownBackTV;
    private TextView hotelNameBackTV;
    private ImageView seckillGifBackIV;
    private ImageView seckillGoodsBackIV;
    private TextView goodsJDPriceBackTV;
    private TextView goodsSeckillPriceBackTV;

    //酒水售卖广告浮层
    private RelativeLayout haveWineBgLayout;
    private ImageView wineImgIV;
    private TextView winePriceTV;

    private int seckillTime=5400;
    //-1:秒杀倒计时结束,0:当前版位无秒杀，1：秒杀倒计时进行中
    private int seckillState = 0;
    //秒杀活动是一个集合，当前序号
    private int currentSeckillIndex=0;
    //秒杀活动集合
    private List<SeckillGoodsBean> seckillGoodsBeanList;
    //当前秒杀活动
    private SeckillGoodsBean seckillGoodsBean;

    private int mCurrentVolume = 0;
    private int delayTime;
    private ArrayList<T> mPlayList;
    private String mListPeriod;
    private boolean mNeedUpdatePlaylist;

    //每次开机赋值，请求广告类型
    private String dspRequestType = null;
    //0-开始播放
    private static final String START_TRACKING = "0";
    //1-结束播放
    private static final String END_TRACKING = "1";
    /**
     * 日志用的播放记录标识
     */
    private String mUUID;
    private long mActivityResumeTime;

    private static final int DELAY_TIME = 2;
    private AdMasterResult adMasterResult = null;

    private PlayListDialog mPlayListDialog;
    //是否已经返回video广告
    private boolean isResponseVideo;
    //是否已经返回image广告
    private boolean isResponseImage;

    private AdInfo mAdInfo;
    private DBHelper dbHelper;
    private long oohlinkStartTime;
    private long oohlinkEndTime;
    private ScanRedEnvelopeQrCodeDialog scanRedEnvelopeQrCodeDialog=null;
    private ServiceConnection mConnection;
    private List<String> polyAdsList = new ArrayList<>();
    /**抽奖活动开奖时间*/
    private String lotteryTime;

    private AnimatorSet mRightOutSet; // 右出动画
    private AnimatorSet mLeftInSet; // 左入动画
    private boolean mIsShowBack;

    private Handler mHandler = new Handler(msg -> {
        switch (msg.what){
            case 1:
                String qrcode_url = msg.getData().getString("qrcode_url");
                String qrcode_path = msg.getData().getString("qrcode_path");
                Long countdownTime = msg.getData().getLong("countdownTime");
                if (countdownTime!=0){
                    ((SavorApplication) getApplication()).showGoodsCountdownQrCodeWindow(qrcode_url,qrcode_path,countdownTime);
                }else {
                    ((SavorApplication) getApplication()).showGoodsQrCodeWindow(qrcode_url,qrcode_path);
                }
        }
        return true;
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_player);
        dbHelper = DBHelper.get(mContext);
        mSavorVideoView = findViewById(R.id.video_view);
        mSavorVideoView.setIfShowPauseBtn(false);
        mSavorVideoView.setIfShowLoading(false);
        mSavorVideoView.setIfHandlePrepareTimeout(true);
        mSavorVideoView.setPlayStateCallback(this);
        partakeDishLayout = findViewById(R.id.partake_dish_layout);
        pdCountdownTV = findViewById(R.id.pd_countdown);
        pdActivityNameTV = findViewById(R.id.pd_activity_name);
        prizeHeadLayout = findViewById(R.id.prize_head_layout);
        lotteryTimeTV = findViewById(R.id.lottery_time);
        lotteryNameTV = findViewById(R.id.lottery_name);

        imgView = findViewById(R.id.img_view);
        priceLayout = findViewById(R.id.price_layout);
        goodsPriceTV = findViewById(R.id.goods_price);
        goodsTitleLayout = findViewById(R.id.goods_title_layout);
        goodsTimeTV = findViewById(R.id.goods_time);
        storeSaleLayout = findViewById(R.id.store_sale_layout);

        wxProjectionTipLayout = findViewById(R.id.wx_projection_tip_layout);
        wxProjectionIconTipIV = findViewById(R.id.wx_projection_icon_tip);
        wxProjectionTxtTipTV = findViewById(R.id.wx_projection_nickname_tip);

        mVolumeRl = findViewById(R.id.rl_volume_view);
        mVolumeTv = findViewById(R.id.tv_volume);
        mVolumePb =  findViewById(R.id.pb_volume);

        captionLayout = findViewById(R.id.caption_layout);
        captionTipTV = findViewById(R.id.caption_tip);
        seckillFrontLayout = findViewById(R.id.seckill_front_layout);
        seckillCountdownFrontTV = findViewById(R.id.seckill_countdown_front);
        hotelNameFrontTV = findViewById(R.id.hotel_name_front);
        seckillGifFrontIV = findViewById(R.id.seckill_gif_front);
        seckillGoodsFrontIV = findViewById(R.id.seckill_goods_front);
        goodsJDPriceFrontTV = findViewById(R.id.goods_jd_price_front);
        goodsSeckillPriceFrontTV = findViewById(R.id.goods_seckill_price_front);
        seckillBackLayout = findViewById(R.id.seckill_back_layout);
        seckillGifBackIV = findViewById(R.id.seckill_gif_back);
        seckillCountdownBackTV = findViewById(R.id.seckill_countdown_back);
        hotelNameBackTV = findViewById(R.id.hotel_name_back);
        seckillGoodsBackIV = findViewById(R.id.seckill_goods_back);
        goodsJDPriceBackTV = findViewById(R.id.goods_jd_price_back);
        goodsSeckillPriceBackTV = findViewById(R.id.goods_seckill_price_back);

        haveWineBgLayout = findViewById(R.id.have_wine_bg_layout);
        wineImgIV = findViewById(R.id.wine_img);
        winePriceTV = findViewById(R.id.wine_price);

        registerDownloadReceiver();
        // 启动投屏类操作处理的Service

        startScreenProjectionService();

        AtlasDialog atlasDialog = new AtlasDialog(getApplicationContext());
        atlasDialog.show();
    }

    public VolumeResponseVo volume(int action) {
        VolumeResponseVo responseVo = new VolumeResponseVo();

        switch (action) {
            case 1:
                //TODO:静音
                break;
            case 2:
                //TODO:取消静音
                break;
            case 3:
                // 音量减
                mCurrentVolume -= 5;
                if (mCurrentVolume < 0) {
                    mCurrentVolume = 0;
                }
                setVolume(mCurrentVolume);
                mHandler.post(()->showVolume(mCurrentVolume));
                break;
            case 4:
                // 音量加
                mCurrentVolume += 5;
                if (mCurrentVolume > 100) {
                    mCurrentVolume = 100;
                }
                setVolume(mCurrentVolume);
                mHandler.post(()->showVolume(mCurrentVolume));
                break;
        }
        responseVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        responseVo.setVol(mCurrentVolume);
        return responseVo;
    }

    private void showVolume(int currentVolume) {
        mVolumePb.setProgress(currentVolume);
        mVolumeTv.setText(currentVolume + "");
        mVolumeRl.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(mHideVolumeViewRunnable);
        mHandler.postDelayed(mHideVolumeViewRunnable, 1000 * 5);
    }

    /**
     * 隐藏音量显示Runnable
     */
    private Runnable mHideVolumeViewRunnable = ()->hideVolumeView();

    private void hideVolumeView(){
        mVolumeRl.setVisibility(View.GONE);
    }

    /**霸王菜头部布局开关*/
    public void isClosePartakeDishHead(boolean close){
        if (close){
            partakeDishLayout.setVisibility(View.INVISIBLE);
        }else{
            partakeDishLayout.setVisibility(View.VISIBLE);
        }
    }
    /**设置霸王菜开奖倒计时*/
    int partakedishTime = 0;
    public void setPartakeDishCountdown(String activityName,int time){
        if (!TextUtils.isEmpty(activityName)){
            pdActivityNameTV.setText(activityName);
        }
        if (time>0){
            partakedishTime = time;
            mHandler.removeCallbacks(partakedishRunnable);
            partakeDishCountdown();
        }
    }

    private void partakeDishCountdown(){
        String minute = TimeUtils.formatSecondsToMin(partakedishTime);
        pdCountdownTV.setText(minute);
        partakedishTime = partakedishTime-1;
        if (partakedishTime<=0){
            mHandler.removeCallbacks(partakedishRunnable);
            isClosePartakeDishHead(true);
        }else{
            mHandler.postDelayed(partakedishRunnable,1000);
        }

    }

    private Runnable partakedishRunnable = ()->partakeDishCountdown();

    /**抽奖活动头部布局开关*/
    public void isClosePrizeHeadLayout(boolean close){
        if (close){
            prizeHeadLayout.setVisibility(View.INVISIBLE);
        }else{
            prizeHeadLayout.setVisibility(View.VISIBLE);
        }
    }
    /**设置抽奖活动开奖时间*/
    public void setLotteryStartTime(String lotteryName,String lotteryTime){
        if (!TextUtils.isEmpty(lotteryName)){
            lotteryNameTV.setText(lotteryName);
        }
        if (!TextUtils.isEmpty(lotteryTime)){
            this.lotteryTime = lotteryTime;
            lotteryTimeTV.setText(lotteryTime);
        }
    }


    public void setScanRedEnvelopeQrCodeDialogListener(ScanRedEnvelopeQrCodeDialog sreqcd){
        this.scanRedEnvelopeQrCodeDialog = sreqcd;
    }

    private void startScreenProjectionService() {
        mConnection = ServiceUtil.registerService(ProjectOperationListener.getInstance(this));
        bindService(new Intent(this, RemoteService.class), mConnection, Service.BIND_AUTO_CREATE);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        checkAndPlay(-1);
    }

    private void registerDownloadReceiver() {
        IntentFilter intentFilter = new IntentFilter(ConstantValues.UPDATE_PLAYLIST_ACTION);
        registerReceiver(mDownloadCompleteReceiver, intentFilter);
    }

    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConstantValues.UPDATE_PLAYLIST_ACTION.equals(intent.getAction())) {
                mNeedUpdatePlaylist = true;
            }
        }
    };

    private void checkAndPlay(int lastMediaOrder) {
        LogFileUtil.write("AdsPlayerActivity checkAndPlay GlobalValues.PLAY_LIST=" + GlobalValues.getInstance().PLAY_LIST + " AppUtils.getMainMediaPath()=" + AppUtils.getMainMediaPath());
        // 未发现SD卡时跳到TV
        if (GlobalValues.getInstance().PLAY_LIST == null || GlobalValues.getInstance().PLAY_LIST.isEmpty() || TextUtils.isEmpty(AppUtils.getMainMediaPath())) {
            if (AppUtils.isMstar()) {
                Intent intent = new Intent(this, TvPlayerActivity.class);
                startActivity(intent);
            } else if (AppUtils.isGiec()){
                Intent intent = new Intent(this, TvPlayerGiecActivity.class);
                startActivity(intent);
            }else if (AppUtils.isSVT()){
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                intent.setClassName(ConstantValues.SVT_SIGNAL_SOURCE_PAKAGE, ConstantValues.SVT_SIGNAL_SOURCE_CLASS);
                try{
                    startActivity(intent);
                }catch (ActivityNotFoundException e){
                    LogUtils.d("Can't find signalsourcemanager activity.", e);
                }catch (Exception e){
                    LogUtils.d("Error while switching to signalsourcemanager.", e);
                }
            }
            finish();
        } else {
            mPlayList = GlobalValues.getInstance().PLAY_LIST;
            mListPeriod = mSession.getAdsPeriod();
            doPlay(lastMediaOrder);
        }
    }

    private void doPlay(int lastMediaOrder) {
        LogFileUtil.write("AdsPlayerActivity doPlay");
        ArrayList<String> urls = new ArrayList<>();
        if (mPlayList != null && mPlayList.size() > 0) {
            int index = 0;
            for (int i = 0; i < mPlayList.size(); i++) {
                MediaLibBean bean = mPlayList.get(i);
                if (bean.getOrder() > lastMediaOrder) {
                    index = i;
                    break;
                }
            }
            for (int i = 0; i < mPlayList.size(); i++) {
                MediaLibBean bean = mPlayList.get(i);
                urls.add(bean.getMediaPath());
            }
            mSavorVideoView.setMediaFiles(urls, index, 0);
        }
    }

    /**
     * Play the current video while detecting the next video that needs to be played if the video is poly,
     * and if so,
     * call the interface to determine if there is a resource that can be played on a poly video.
     *
     * @param index
     */
    private void toCheckIfPolyAds(int index) {

        if (mPlayList != null) {
            int next = (index + 1) % mPlayList.size();
            if (next < mPlayList.size()) {
                MediaLibBean bean = mPlayList.get(next);
                // 当下一个位置是聚屏类、且未被填充媒体内容、且当前未被“未发现百度广告”阻止时，请求百度聚屏广告
                if (ConstantValues.POLY_ADS.equals(bean.getType())&&TextUtils.isEmpty(bean.getName())) {
                    String tpmedias = mSession.getTpMedias();
                    if (!TextUtils.isEmpty(tpmedias)){
                        String[] polyAds = tpmedias.split(",");
                        if (polyAds.length>0){
                            polyAdsList.clear();
                            for (int i =0;i<polyAds.length;i++){
                                polyAdsList.add(polyAds[i]);
                            }
                        }
                        if (!polyAdsList.isEmpty()&&polyAdsList.size()>0){
                            if (polyAdsList.size()<=GlobalValues.DSP_POLY_ORDER_POSITION){
                                GlobalValues.DSP_POLY_ORDER_POSITION = 0;
                            }
                            dspRequestType = polyAdsList.get(GlobalValues.DSP_POLY_ORDER_POSITION);
                            if (dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_OOHLINK)){
                                requestOOHLinkAds();
                            }else if (dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_BAIDU)){
                                requestBaiduAds();
                            }else if (dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_MEI)){
                                requestMeiDSPAds();
                            }else if (dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_ZMENG)){
                                requestZmengAds();
                            }else if (dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_JDMOMEDIA)){
                                requestJDmomedia();
                            }else if (dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_YISHOU)){
                                requestYishouAds();
                            }

                            GlobalValues.DSP_POLY_ORDER_POSITION++;
                        }
                    }

                }
            }
        }
    }

    /**
     * 请求百度聚屏广告
     */
    private void requestBaiduAds() {
        String release = mSession.getBuildVersion();
        String[] ver = release.split("[.]");
        int verMajor = 0, verMinor = 0, verMicro = 0;
        for (int i = 0; i < ver.length; i++) {
            int temp = 0;
            try {
                temp = Integer.parseInt(ver[i]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            switch (i) {
                case 0:
                    verMajor = temp;
                    break;
                case 1:
                    verMinor = temp;
                    break;
                case 2:
                    verMicro = temp;
                    break;
            }
        }

        Point point = DensityUtil.getScreenRealSize(this);

        ByteString uuid = null, appId = null, adslotId = null, mac = null, model = null, brand = null, ip = null;
        try {
            uuid = ByteString.copyFrom(UUID.randomUUID().toString().replace("-", ""), "utf-8");
            appId = ByteString.copyFrom(ConstantValues.BAIDU_ADS_APP_ID, "utf-8");
            if (mSession.getTvSize()>0&&mSession.getTvSize()<=40){
                adslotId = ByteString.copyFrom(ConstantValues.BAIDU_ADSLOT_ID1, "utf-8");
            }else if (mSession.getTvSize()>40&&mSession.getTvSize()<=45){
                adslotId = ByteString.copyFrom(ConstantValues.BAIDU_ADSLOT_ID2, "utf-8");
            }else if (mSession.getTvSize()>45&&mSession.getTvSize()<=50){
                adslotId = ByteString.copyFrom(ConstantValues.BAIDU_ADSLOT_ID3, "utf-8");
            }else if (mSession.getTvSize()>50&&mSession.getTvSize()<=55){
                adslotId = ByteString.copyFrom(ConstantValues.BAIDU_ADSLOT_ID4, "utf-8");
            }else if (mSession.getTvSize()>55){
                adslotId = ByteString.copyFrom(ConstantValues.BAIDU_ADSLOT_ID5, "utf-8");
            }
            mac = ByteString.copyFrom(mSession.getEthernetMacWithColon(), "utf-8");
            model = ByteString.copyFrom(mSession.getModel(), "utf-8");
            brand = ByteString.copyFrom(mSession.getBrand(), "utf-8");
            ip = ByteString.copyFrom(AppUtils.getLocalIPAddress(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (adslotId==null){
            return;
        }
        TsUiApiV20171122.TsApiRequest request = TsUiApiV20171122.TsApiRequest.newBuilder()
                .setRequestId(uuid)
                .setApiVersion(TsUiApiV20171122.Version.newBuilder()
                        .setMajor(6).setMinor(0).setMicro(0))
                .setAppId(appId)
                .setSlot(TsUiApiV20171122.SlotInfo.newBuilder()
                        .setAdslotId(adslotId))
                .setDevice(TsUiApiV20171122.Device.newBuilder()
                        .setUdid(TsUiApiV20171122.UdId.newBuilder()
                                .setIdType(TsUiApiV20171122.UdIdType.MAC)
                                .setId(mac))
                        .setModel(model)
                        .setOsType(TsUiApiV20171122.OsType.ANDROID)
                        .setOsVersion(TsUiApiV20171122.Version.newBuilder().setMajor(verMajor).setMinor(verMinor).setMicro(verMicro))
                        .setScreenSize(TsUiApiV20171122.Size.newBuilder().setWidth(point.x).setHeight(point.y))
                        .setVendor(brand))
                .setNetwork(TsUiApiV20171122.Network.newBuilder()
                        .setConnectionType(TsUiApiV20171122.Network.ConnectionType.ETHERNET)
                        .setOperatorType(TsUiApiV20171122.Network.OperatorType.ISP_UNKNOWN)
                        .setIpv4(ip))
                .build();
        AppApi.requestBaiduAds(this, this, request);
    }

    /**
     * 请求钛镁聚屏广告
     */
    private void requestMeiDSPAds(){
        requestMeiSSPImageAds();
        requestMeiSSPVideoAds();
    }
    private void requestMeiSSPVideoAds(){

        HashMap<String,Object> param = new HashMap<>();
        param.put("key",ConstantValues.MEI_SSP_VIDEO_KEY);
        param.put("appid","2005");
        param.put("userid","2005");
        param.put("count",1);
        param.put("ua","");
        param.put("ip",AppUtils.getEthernetIP());
        param.put("did",AppUtils.getDeviceId(this));
        param.put("didsha1","");
        param.put("didmd5","");
        param.put("dpid","");
        param.put("dpidsha1","");
        param.put("dpidmd5","");
        param.put("os",mSession.getOsVersion());
        param.put("osv",mSession.getBuildVersion());
        param.put("screen_w",DensityUtil.getScreenWidth(this));
        param.put("screen_h",DensityUtil.getScreenHeight(this));
        param.put("devicetype",0);
        param.put("carrier",0);
        param.put("connectiontype",0);
        param.put("lat","");
        param.put("lon","");

        AppApi.requestMeiVideoAds(this,this,param);
        isResponseVideo = false;

    }
    private void requestMeiSSPImageAds(){

        HashMap<String,Object> param = new HashMap<>();

        param.put("key",ConstantValues.MEI_SSP_IMAGE_KEY);
        param.put("appid","2005");
        param.put("userid","2005");
        param.put("count",1);
        param.put("ua","");
        param.put("ip",AppUtils.getEthernetIP());
        param.put("did",AppUtils.getDeviceId(this));
        param.put("didsha1","");
        param.put("didmd5","");
        param.put("dpid","");
        param.put("dpidsha1","");
        param.put("dpidmd5","");
        param.put("os",mSession.getOsVersion());
        param.put("osv",mSession.getBuildVersion());
        param.put("screen_w",DensityUtil.getScreenWidth(this));
        param.put("screen_h",DensityUtil.getScreenHeight(this));
        param.put("devicetype",0);
        param.put("carrier",0);
        param.put("connectiontype",0);
        param.put("lat","");
        param.put("lon","");

        AppApi.requestMeiImageAds(this,this,param);
        isResponseImage = false;

    }

    /**奥凌广告平台**/
    private void requestOOHLinkAds(){
        AdRequestInfo adRequestInfo = new AdRequestInfo();
        adRequestInfo.setDuration(15);  //时长  如果你请求的时候传入的是15s，后台会返回小于或者等于15s的广告。后续会限制，如果没有传入广告时长的广告后台会返回没有广告的。
        adRequestInfo.setMatHeight(720);  //请求广告分辨率的高
        adRequestInfo.setMatWidth(1280);  //请求广告分辨率的宽
        adRequestInfo.setType(AdType.UNKNOWN);

        final RtbRequest rtbRequest = getRtbRequest(ConstantValues.MCHANNEL_ID, ConstantValues.MTOKEN, adRequestInfo, mSession.getEthernetMac());

        AppApi.requestOOHLinkAds(mContext,this,rtbRequest);

    }

    private RtbRequest getRtbRequest(String channelId, String token, AdRequestInfo adRequestInfo, String playCode){
        String requestId = DeviceUtils.getRequestId(channelId, playCode);  //接入方自定义，发请求时填写，需要确保唯一性（要求32位字符串）可以用播控器编码加时间戳
        MediaDevice mediaDevice = new MediaDevice();
        mediaDevice.setDeviceType(MediaDevice.DeviceType.OUTDOOR_SCREEN.getValue());
        mediaDevice.setOsType(MediaDevice.OsType.ANDROID.getValue());
        mediaDevice.setOsVersion(Build.VERSION.RELEASE);
        mediaDevice.setModel(Build.MODEL);
        mediaDevice.setVendor(Build.BRAND);
        mediaDevice.setScreenHeight(DeviceUtils.getScreenHeight(this));
        mediaDevice.setScreenWidth(DeviceUtils.getScreenWidth(this));

        MediaUdId mediaUdId = new MediaUdId();
        mediaUdId.setAndroidId(DeviceUtils.getAndroidID(this));
        mediaUdId.setImei(DeviceUtils.getAndroidID(this));
        mediaUdId.setMac(DeviceUtils.getMacAddress(this));

        MediaNetWork mediaNetWork = new MediaNetWork();
        mediaNetWork.setIpv4(NetworkUtils.getIPAddress(true));
        mediaNetWork.setOperatorType(
                NetworkUtils.getCarrierOperator(this).getValue());
        mediaNetWork.setConnectionType(NetworkUtils.getNetworkType(this).getValue());

        AdSlot adSlot = new AdSlot();
        adSlot.setType(adRequestInfo.getType().getValue());
        adSlot.setDuration(adRequestInfo.getDuration());
        adSlot.setAdslotHeight(adRequestInfo.getMatHeight());
        adSlot.setAdslotWidth(adRequestInfo.getMatWidth());

        RtbRequest rtbRequest = new RtbRequest();
        rtbRequest.setRequestId(requestId);
        rtbRequest.setChannelId(channelId);
        rtbRequest.setPlayCode(playCode);
        rtbRequest.setToken(token);
        rtbRequest.setDevice(mediaDevice);
        rtbRequest.setUdid(mediaUdId);
        rtbRequest.setNetwork(mediaNetWork);
        rtbRequest.setAdSlot(adSlot);
        return rtbRequest;
    }

    /**众盟广告平台**/
    private void requestZmengAds(){
        try{
            ZmtAdRequestUtil zmtAdRequestUtil = new ZmtAdRequestUtil();
            ZmtAPI.ZmAdRequest zmAdRequest = zmtAdRequestUtil.buildAdRequst(this);
            AppApi.requestZmengAds(mContext,this,zmAdRequest);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    /**京东钼媒广告平台**/
    private void requestJDmomedia(){
        try{

            JSONObject jsonDevice = new JSONObject();
            JSONObject jsonUdid = new JSONObject();
            //udid
            jsonUdid.accumulate("android_id",DeviceUtils.getAndroidID(mContext));
            jsonUdid.accumulate("mac",AppUtils.getEthernetMacAddr());
            jsonUdid.accumulate("imei",DeviceUtils.getAndroidID(mContext));
            jsonDevice.accumulate("udid",jsonUdid);
            //screen_size
            JSONObject jsonScreenSize = new JSONObject();
            jsonScreenSize.accumulate("width",1280);
            jsonScreenSize.accumulate("height",720);
            jsonDevice.accumulate("screen_size",jsonScreenSize);
            //network
            JSONObject jsonNetwork = new JSONObject();
            jsonNetwork.accumulate("connection_type","WIFI");
            jsonNetwork.accumulate("operator_type","MOBILE");

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("appid",ConstantValues.JDMOMEDIA_APPID);
            jsonObject.accumulate("request_id",Long.toString(System.currentTimeMillis())
                    + RandomStringUtils.randomAlphanumeric(19));
            jsonObject.accumulate("device",jsonDevice);
            jsonObject.accumulate("network",jsonNetwork);
            jsonObject.accumulate("times",15);
            jsonObject.accumulate("type",0);
            AppApi.requestJDmomediaAds(mContext,this,jsonObject);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    /**易售广告平台**/
    private void requestYishouAds(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("device-uuid",mSession.getEthernetMac());
            if (GlobalValues.YISHOU_REQUEST_TYPE.equals(ConstantValues.YISHOU_IMG)){
                jsonObject.accumulate("slot-id",ConstantValues.YISHOU_IMG_ID);
                jsonObject.accumulate("type",ConstantValues.YISHOU_IMG);
            }else{
                jsonObject.accumulate("slot-id",ConstantValues.YISHOU_VDO_ID);
                jsonObject.accumulate("type",ConstantValues.YISHOU_VDO);
            }
            jsonObject.accumulate("quantity",1);
            jsonObject.accumulate("ip",AppUtils.getEthernetIP());
            HashMap<String,String> params = new HashMap<>();
            params.put("payload",jsonObject.toString());
            AppApi.requestYishouAds(this,this,params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void reportOOHLinkAdsLog(){

        final HashMap<String,String> params = new HashMap<>();
        params.put("playCode", mSession.getEthernetMac());
        params.put("requestId", mAdInfo.getRequestId());  //获取广告的时候requestId
        params.put("channelId", String.valueOf(mAdInfo.getChannelId()));  //此处的channelId，是获取广告之后channelId
        params.put("beginTime", AppUtils.timeStamp2Date(oohlinkStartTime,""));  //此处beginTime是广告开始播放的时间，需要我们在广告开始播放的时候记录一下。  格式为 yyyy-MM-dd HH:mm:ss
        params.put("endTime", AppUtils.timeStamp2Date(oohlinkEndTime,""));  //广告播放结束的时间。 格式为 yyyy-MM-dd HH:mm:ss
        params.put("type", String.valueOf(mAdInfo.getMatType()));  //文件类型(1:图片;2:视频), 根据getAd返回的参数进行对应
        params.put("fileName", mAdInfo.getFileName());  //文件名称可能为空，可以以下载后的文件名作为文件的name
        params.put("fileUrl", mAdInfo.getMatUrl());    //文件素材的url
        params.put("duration", String.valueOf(mAdInfo.getDuration()));  //广告时长
        params.put("planId", String.valueOf(mAdInfo.getPlanId()));  //广告计划标识
        AppApi.reportOOHLinkAdsLog(mContext,this,params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppUtils.isGiec()){
            new Handler().postDelayed(()->ShellUtils.setAmvecmPcMode(), 3000);
        }
        LogFileUtil.write("AdsPlayerActivity onResume " + this.hashCode());
        Log.d(TAG, "::onResume");
        judegeCurrentLotteryState();
        mActivityResumeTime = System.currentTimeMillis();
        if (AppUtils.isSVT()||AppUtils.isPhilips()){
            setVolume(mSession.getTvCarouselVolume());
            if (mCurrentVolume==0){
                mCurrentVolume = mSession.getTvCarouselVolume();
            }
        }else if(AppUtils.isGiec()){
            setVolume(mSession.getBoxCarouselVolume());
            if (mCurrentVolume==0) {
                mCurrentVolume = mSession.getBoxCarouselVolume();
            }
        }
        if (GlobalValues.mIsGoneToTv) {
            GlobalValues.IS_BOX_BUSY = true;
            mSavorVideoView.onResume();
            GlobalValues.mIsGoneToTv = false;
            GlobalValues.IS_BOX_BUSY = false;
            mSavorVideoView.postDelayed(currentVideoRunnable, 1000 * DELAY_TIME);
        }

    }

    private void judegeCurrentLotteryState(){
        if (!TextUtils.isEmpty(lotteryTime)){
            long currentHour = AppUtils.getHour(new Date());
            long currentMinute = AppUtils.getMinute(new Date());
            String[] prizeTime = lotteryTime.split(":");
            int prizeHour =Integer.valueOf(prizeTime[0]);
            int prizeMinute = Integer.valueOf(prizeTime[1]);
            if (currentHour>prizeHour){
                GlobalValues.isPrize= false;
                this.lotteryTime = null;
                isClosePrizeHeadLayout(true);
            }else if (currentHour==prizeHour){
                if (currentMinute>=prizeMinute){
                    GlobalValues.isPrize = false;
                    this.lotteryTime = null;
                    isClosePrizeHeadLayout(true);
                }
            }
        }
    }

    //秒杀倒计时处理显示
    private void secKillCountdown(){
        String timeformat = TimeUtils.formatSecondsToHour(seckillTime);
        seckillCountdownFrontTV.setText(timeformat);
        seckillCountdownBackTV.setText(timeformat);
        seckillTime = seckillTime-60;
        if (seckillTime<=0){
            mHandler.removeCallbacks(seckillCountdownRunnable);
            mHandler.removeCallbacks(flipCardRunnable);
            seckillFrontLayout.setVisibility(View.GONE);
            seckillBackLayout.setVisibility(View.GONE);
            if (seckillGoodsBeanList!=null&&seckillGoodsBeanList.size()>0){
                seckillGoodsBeanList.remove(seckillGoodsBean);
                mHandler.removeCallbacks(switchSeckillRunnable);
                mHandler.post(switchSeckillRunnable);
            }
        }else{
            mHandler.postDelayed(seckillCountdownRunnable,1000*60);
        }

    }

    private Runnable seckillCountdownRunnable = ()->secKillCountdown();

    Runnable currentVideoRunnable = new Runnable() {
        @Override
        public void run() {
            Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
            if (activity instanceof AdsPlayerActivity){
                mSavorVideoView.playCurrentVideo();
            }
        }
    };

    public void toCheckMediaIsShowMiniProgramIcon(){
        try{
            if (mPlayList != null && !TextUtils.isEmpty(mPlayList.get(mCurrentPlayingIndex).getVid())) {
                MediaLibBean libBean = mPlayList.get(mCurrentPlayingIndex);
                isShowMiniProgramIcon(libBean);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 设置翻转卡片动画
    @SuppressWarnings("ResourceType")
    private void setAnimators() {
        mRightOutSet = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.anim.anim_out);
        mLeftInSet = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.anim.anim_in);
    }

    // 改变视角距离, 贴近屏幕
    private void setCameraDistance() {
        int distance = 16000;
        float scale = mContext.getResources().getDisplayMetrics().density * distance;
        seckillFrontLayout.setCameraDistance(scale);
        seckillBackLayout.setCameraDistance(scale);
    }

    // 翻转卡片
    public void flipCard() {
        // 正面朝上
        if (!mIsShowBack) {
            mRightOutSet.setTarget(seckillFrontLayout);
            mLeftInSet.setTarget(seckillBackLayout);
            mRightOutSet.start();
            mLeftInSet.start();
            mIsShowBack = true;
        } else {
            // 背面朝上
            mRightOutSet.setTarget(seckillBackLayout);
            mLeftInSet.setTarget(seckillFrontLayout);
            mRightOutSet.start();
            mLeftInSet.start();
            mIsShowBack = false;
        }
        mHandler.postDelayed(flipCardRunnable,1000*30);
    }

    private Runnable flipCardRunnable = ()->flipCard();

    @Override
    protected void onStart() {
        LogFileUtil.write("AdsPlayerActivity onStart " + this.hashCode());
        LogUtils.d(TAG + "::onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        LogFileUtil.write("AdsPlayerActivity onRestart " + this.hashCode());
        LogUtils.d(TAG + "::onRestart");
        super.onRestart();

    }

    @Override
    protected void onStop() {
        LogFileUtil.write("AdsPlayerActivity onStop " + this.hashCode());
        LogUtils.d(TAG + "::onStop");
        mSavorVideoView.onStop();
        super.onStop();
    }

    @Override
    protected void onPause() {
        LogFileUtil.write("AdsPlayerActivity onPause " + this.hashCode());
        Log.d(TAG, "::onPause");
        mSavorVideoView.onPause();
        mSavorVideoView.removeCallbacks(currentVideoRunnable);
        GlobalValues.mIsGoneToTv = true;
        priceLayout.setVisibility(View.GONE);
        goodsTitleLayout.setVisibility(View.GONE);
        storeSaleLayout.setVisibility(View.GONE);
        mHandler.removeCallbacks(mCountDownRunnable);
        wxProjectionTipLayout.setVisibility(View.GONE);
        ((SavorApplication) getApplication()).hideMiniProgramQrCodeWindow();
        ((SavorApplication) getApplication()).hideGoodsQrCodeWindow();
        ((SavorApplication) getApplication()).hideGoodsCountdownQrCodeWindow();
        super.onPause();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 禁止进入页面后马上操作
        if (System.currentTimeMillis() - mActivityResumeTime < ConstantValues.KEY_DOWN_LAG + DELAY_TIME * 1000)
            return true;

        boolean handled = false;
        if (keyCode == KeyEvent.KEYCODE_BACK) {//                handleBack();
            handled = true;

            // 切换到电视模式
        } else if (keyCode == KeyCode.KEY_CODE_CHANGE_MODE) {
            switchToTvPlayer();
            handled = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((SavorApplication) getApplication()).hideMiniProgramQrCodeWindow();
                    ((SavorApplication) getApplication()).hideGoodsQrCodeWindow();
                    ((SavorApplication) getApplication()).hideGoodsCountdownQrCodeWindow();
                }
            },1000*3);
            // 呼出二维码
        } else if (keyCode == KeyCode.KEY_CODE_PLAY_PAUSE) {
            mSavorVideoView.togglePlay();
            handled = true;

            // 上一条
        } else if (keyCode == KeyCode.KEY_CODE_PREVIOUS_ADS) {
            mSavorVideoView.playPrevious();
            handled = true;

            // 下一条
        } else if (keyCode == KeyCode.KEY_CODE_NEXT_ADS) {
            Log.d(TAG,"onKeydown");
            mSavorVideoView.playNext();
            handled = true;

            // 机顶盒信息
        } else if (keyCode == KeyCode.KEY_CODE_SHOW_INFO) {// 对话框弹出后会获得焦点，所以这里不需要处理重复点击重复显示的问题
            showBoxInfo();
            handled = true;

        } else if (keyCode == KeyCode.KEY_CODE_CHANGE_RESOLUTION) {
            if (!AppUtils.isMstar()) {
                changeResolution();
                handled = true;
            }

        } else if (keyCode == KeyCode.KEY_CODE_SHOW_PLAYLIST) {
            showPlaylist();
            handled = true;

        }
        return handled || super.onKeyDown(keyCode, event);
    }

    public void changeMedia(int value){
        if (mSavorVideoView!=null){
            switch (value){
                case 1:
                    mSavorVideoView.playPrevious();
                    break;
                case 2:
                    Log.d(TAG,"changeMedia");
                    mSavorVideoView.playNext();
                    break;
            }
        }
    }

    public void moveFocus(int changeType){
        if (mPlayListDialog!=null&&mPlayListDialog.isShowing()){
            mPlayListDialog.moveFocus(changeType);
        }else if (mBoxInfoDialog!=null&&mBoxInfoDialog.isShowing()){
            mBoxInfoDialog.moveFocus(changeType);
        }
    }

    public void showPlaylist() {
        if (mPlayListDialog == null) {
            mPlayListDialog = new PlayListDialog(this, this);
        }
//        if (mPlayList != null) {
        if (!mPlayListDialog.isShowing()) {
            mPlayListDialog.showPlaylist(mPlayList);
        }
//        } else {
//            ShowMessage.showToast(mContext, "播放列表为空");
//        }
    }

    private Runnable mHidePlayListWindow = () -> {
        if (mPlayListDialog!=null&&mPlayListDialog.isShowing()){
            mPlayListDialog.dismiss();
        }
    };

    public void hideInfo(){
        mHandler.post(mHideInfoRunnable);
        mHandler.post(mHidePlayListWindow);

    }

    int resolutionIndex = 0;

    private void changeResolution() {
        OutputResolution resolution = OutputResolution.values()[(resolutionIndex++) % OutputResolution.values().length];
        TVOperatorFactory.getTVOperator(this, TVOperatorFactory.TVType.GIEC)
                .switchResolution(resolution);
        String msg = "1080P";
        switch (resolution) {
            case RESOLUTION_1080p:
                msg = "1080P";
                break;
            case RESOLUTION_720p:
                msg = "720P";
                break;
        }
        ShowMessage.showToast(getApplicationContext(), msg);
    }

    /**
     * 切换到电视模式
     */
    private void switchToTvPlayer() {
        String vid = "";
        if (mPlayList != null && mCurrentPlayingIndex >= 0 && mCurrentPlayingIndex < mPlayList.size()) {
            vid = mPlayList.get(mCurrentPlayingIndex).getVid();
        }
        if (AppUtils.isMstar()) {
            Intent intent = new Intent(this, TvPlayerActivity.class);
            intent.putExtra(TvPlayerActivity.EXTRA_LAST_VID, vid);
            startActivity(intent);
        } else if(AppUtils.isGiec()){
            Intent intent = new Intent(this, TvPlayerGiecActivity.class);
            intent.putExtra(TvPlayerActivity.EXTRA_LAST_VID, vid);
            startActivity(intent);
        }else if (AppUtils.isSVT()){
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.setClassName(ConstantValues.SVT_SIGNAL_SOURCE_PAKAGE, ConstantValues.SVT_SIGNAL_SOURCE_CLASS);
            try{
                startActivity(intent);
            }catch (ActivityNotFoundException e){
                LogUtils.d("Can't find signalsourcemanager activity.", e);
            }catch (Exception e){
                LogUtils.d("Error while switching to signalsourcemanager.", e);
            }
        }
    }

    @Override
    public boolean onMediaComplete(int index, boolean isLast) {
        // 这里只是为了防止到这里的时候mUUID没值，正常mUUID肯定会在onMediaPrepared()中赋值
        if (TextUtils.isEmpty(mUUID)) {
            mUUID = String.valueOf(System.currentTimeMillis());
        }
        if (mPlayList != null) {
            MediaLibBean item = mPlayList.get(index);
            if (!TextUtils.isEmpty(item.getVid())) {
                LogReportUtil.get(this).sendAdsLog(mUUID, mSession.getBoiteId(), mSession.getRoomId(),
                        String.valueOf(System.currentTimeMillis()), "end", item.getType(), item.getVid(),
                        "", mSession.getVersionName(), mListPeriod, mSession.getBirthdayOndemandPeriod(),
                        "");
            }
            if (ConstantValues.POLY_ADS.equals(item.getType())
                    ||ConstantValues.POLY_ADS_ONLINE.equals(item.getType())){
                mPlayList.get(index).setType(ConstantValues.POLY_ADS);
                if (item instanceof BaiduAdLocalBean) {
                    noticeAdsMonitor(item);
                    postPolyPlayRecord(item.getVid(),item.getTp_md5(),item.getName(),item.getChinese_name(),ConstantValues.DSP_MEDIA_TYPE_BAIDU);
                }else if (item instanceof MeiAdLocalBean){
                    noticeAdsMonitor(item);
                    GlobalValues.POLY_MEI_ADS_PLAY_LIST.remove(item);
                    postPolyPlayRecord(item.getVid(),item.getTp_md5(),item.getName(),item.getChinese_name(),ConstantValues.DSP_MEDIA_TYPE_MEI);
                }else if (item instanceof OOHLinkAdLocalBean){
                    oohlinkEndTime = System.currentTimeMillis();
                    GlobalValues.DSP_OOHLINK_ADS_PLAY_LIST.remove(item);
                    reportOOHLinkAdsLog();
                    postPolyPlayRecord("0",item.getTp_md5(),item.getName(),item.getChinese_name(),ConstantValues.DSP_MEDIA_TYPE_OOHLINK);
                }else if (item instanceof ZmengAdLocalBean){
                    ZmengAdLocalBean bean = (ZmengAdLocalBean)item;
                    GlobalValues.DSP_ZMENG_ADS_PLAY_LIST.remove(item);
                    //回调结束播放监播url
                    zmengNoticeTrackingUrlByEvent(bean.getAdTrackingList(),END_TRACKING);
                    //计费调用曝光地址
                    noticeAdsMonitor(item);
                    postPolyPlayRecord(item.getVid(),item.getTp_md5(),item.getName(),item.getChinese_name(),ConstantValues.DSP_MEDIA_TYPE_ZMENG);
                }else if (item instanceof JDmomediaLocalBean){
                    JDmomediaLocalBean bean = (JDmomediaLocalBean)item;
                    JDmomediaNoticeTrackingUrl(bean.getAd_tracking(),END_TRACKING);
                    postPolyPlayRecord(item.getVid(),item.getTp_md5(),item.getName(),item.getChinese_name(),ConstantValues.DSP_MEDIA_TYPE_JDMOMEDIA);
                }else if (item instanceof YishouAdLocalBean){
                    YishouAdLocalBean bean = (YishouAdLocalBean)item;
                    noticeAdsMonitor(bean);
                    postPolyPlayRecord(item.getVid(),item.getTp_md5(),item.getName(),item.getChinese_name(),ConstantValues.DSP_MEDIA_TYPE_YISHOU);
                }
                mPlayList.get(index).setName(null);
                mPlayList.get(index).setMediaPath(null);
                mPlayList.get(index).setChinese_name("已过期");
            }
            if (ConstantValues.LOCAL_LIFE.equals(item.getType())){
                AppApi.postForscreenAdsLog(this,this,item.getAds_id(),mSession.getEthernetMac());
            }
        }

        if (mNeedUpdatePlaylist) {
            // 重新获取播放列表开始播放
            mNeedUpdatePlaylist = false;
            if (GlobalValues.getInstance().PLAY_LIST != null && !GlobalValues.getInstance().PLAY_LIST.equals(mPlayList)) {
                int currentOrder = mPlayList.get(index).getOrder();
                mSavorVideoView.stop();
                checkAndPlay(currentOrder);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void postPolyPlayRecord(String mediaId,String media_md5,String media_name,String chinese_name,String tpmedia_id){
        AppApi.postPolyPlayRecord(mContext,this,mediaId,media_md5,media_name,chinese_name,tpmedia_id);
    }

    @Override
    public boolean onMediaPrepared(int index) {
        if (mPlayList != null && (!TextUtils.isEmpty(mPlayList.get(index).getVid()))) {
            MediaLibBean libBean = mPlayList.get(index);
            GlobalValues.currentVid = libBean.getVid();
            if (libBean.getType().equals(ConstantValues.ADS)
                    ||libBean.getType().equals(ConstantValues.POLY_ADS)
                    ||libBean.getType().equals(ConstantValues.POLY_ADS_ONLINE)
                    ||libBean.getType().equals(ConstantValues.ACTGOODS_OPTI)
                    ||libBean.getType().equals(ConstantValues.ACTGOODS_ACTIVITY)
                    ||libBean.getType().equals(ConstantValues.ACTGOODS_COUNTDOWN)){
                if (scanRedEnvelopeQrCodeDialog!=null&&scanRedEnvelopeQrCodeDialog.isShowing()){
                    scanRedEnvelopeQrCodeDialog.dismiss();
                }
            }
            isShowMiniProgramIcon(libBean);
            if (!TextUtils.isEmpty(libBean.getEnd_date())) {
                // 检测截止时间是否已到，到达的话跳到下一个
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date endDate = null;
                try {
                    endDate = format.parse(libBean.getEnd_date());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date now = new Date();
                if (endDate != null && endDate.before(now)) {
                    Log.d(TAG,"onMediaPrepared-date");
                    mSavorVideoView.playNext();
                    return true;
                }
            }
            if (!TextUtils.isEmpty(libBean.getStart_date())) {
                // 检测截止时间是否已到，到达的话跳到下一个
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = null;
                try {
                    startDate = format.parse(libBean.getStart_date());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date now = new Date();
                if (startDate != null && startDate.after(now)) {
                    Log.d(TAG,"onMediaPrepared-date");
                    mSavorVideoView.playNext();
                    return true;
                }
            }
            String action = "";
            if (mCurrentPlayingIndex != index) {
                // 准备播放新视频时产生一个新的UUID作为日志标识
                mUUID = String.valueOf(System.currentTimeMillis());
                mCurrentPlayingIndex = index;
                action = "start";
            } else {
                // 这里只是为了防止到这里的时候mUUID没值，正常mUUID肯定会在onMediaPrepared()中赋值
                if (TextUtils.isEmpty(mUUID)) {
                    mUUID = String.valueOf(System.currentTimeMillis());
                }
                action = "resume";
            }
            getSeckillGoodsInfo();
            LogReportUtil.get(this).sendAdsLog(mUUID, mSession.getBoiteId(), mSession.getRoomId(),
                    String.valueOf(System.currentTimeMillis()), action, libBean.getType(), libBean.getVid(),
                    "", mSession.getVersionName(), mListPeriod, mSession.getBirthdayOndemandPeriod(), "");

            //回调开始播放监听
            if (!TextUtils.isEmpty(libBean.getDuration())){
                mSavorVideoView.setAdsDuration(Integer.valueOf(libBean.getDuration()));
            }
            if (libBean.getType().equals(ConstantValues.ACTGOODS_OPTI)
                    ||libBean.getType().equals(ConstantValues.ACTGOODS_ACTIVITY)
                    ||libBean.getType().equals(ConstantValues.ACTGOODS_COUNTDOWN)){
                if ((ConstantValues.ACTGOODS_COUNTDOWN).equals(libBean.getType())){
                    ((SavorApplication) getApplication()).hideGoodsQrCodeWindow();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            long countdownTime = getGoodsCountdown(libBean.getGoods_id());
                            Message message = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putString("qrcode_url",libBean.getQrcode_url());
                            bundle.putString("qrcode_path",libBean.getQrcode_path());
                            bundle.putLong("countdownTime",countdownTime);
                            message.setData(bundle);
                            message.what = 1;
                            mHandler.sendMessage(message);
                        }
                    }).start();
                }else{
                    ((SavorApplication) getApplication()).hideGoodsCountdownQrCodeWindow();
                    ((SavorApplication) getApplication()).showGoodsQrCodeWindow(libBean.getQrcode_url(),libBean.getQrcode_path());
                }

                if (!TextUtils.isEmpty(libBean.getPrice())&&!"0".equals(libBean.getPrice())){
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        priceLayout.setVisibility(View.VISIBLE);
                        goodsPriceTV.setText(libBean.getPrice());
                    }else{
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                priceLayout.setVisibility(View.VISIBLE);
                                goodsPriceTV.setText(libBean.getPrice());
                            }
                        });
                    }

                }else{
                    priceLayout.setVisibility(View.GONE);
                }
                if (!TextUtils.isEmpty(libBean.getDuration())
                        &&!"0".equals(libBean.getDuration())
                        &&!ConstantValues.ACTGOODS_COUNTDOWN.equals(libBean.getType())){
                    delayTime = Integer.valueOf(libBean.getDuration());
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        goodsTitleLayout.setVisibility(View.VISIBLE);
                        goodsTimeTV.setVisibility(View.VISIBLE);
                        goodsTimeTV.setText(delayTime+"s");
                    }else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                goodsTitleLayout.setVisibility(View.VISIBLE);
                                goodsTimeTV.setVisibility(View.VISIBLE);
                                goodsTimeTV.setText(delayTime+"s");
                            }
                        });
                    }

                    mHandler.removeCallbacks(mCountDownRunnable);
                    mHandler.postDelayed(mCountDownRunnable,1000);
                }else{
                    mHandler.removeCallbacks(mCountDownRunnable);
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        goodsTitleLayout.setVisibility(View.GONE);
                    }else {
                        mHandler.post(()->goodsTitleLayout.setVisibility(View.GONE));
                    }
                }
                if (libBean.getIs_storebuy()==1){
                    storeSaleLayout.setVisibility(View.VISIBLE);
                }else{
                    storeSaleLayout.setVisibility(View.GONE);
                }
            }else{
                ((SavorApplication) getApplication()).hideGoodsQrCodeWindow();
                ((SavorApplication) getApplication()).hideGoodsCountdownQrCodeWindow();
                priceLayout.setVisibility(View.GONE);
                goodsTitleLayout.setVisibility(View.GONE);
                storeSaleLayout.setVisibility(View.GONE);
                mHandler.removeCallbacks(mCountDownRunnable);
            }

            if (libBean.getType().equals(ConstantValues.SHOP_GOODS_ADS)
                    ||libBean.getType().equals(ConstantValues.LOCAL_LIFE)){
                String qrcode_url=libBean.getQrcode_url();
                String qrcode_path=libBean.getQrcode_path();
                if (libBean.getType().equals(ConstantValues.LOCAL_LIFE)){
                        ((SavorApplication) getApplication()).showGoodsQrCodeWindow(qrcode_url,qrcode_path,ConstantValues.LOCAL_LIFE);
                }else {
                    ((SavorApplication) getApplication()).showGoodsQrCodeWindow(qrcode_url,qrcode_path,ConstantValues.SHOP_GOODS_ADS);
                }

            }else{
                ((SavorApplication) getApplication()).hideGoodsQrCodeWindow();
            }

            if (ConstantValues.STORE_SALE.equals(libBean.getType())){
                haveWineBgLayout.setVisibility(View.VISIBLE);
                String imagePath = libBean.getImage_path();
                String imageUrl = libBean.getImage_url();
                String price = libBean.getPrice();
                if (!TextUtils.isEmpty(imagePath)){
                    GlideImageLoader.loadLocalImage(mContext,new File(imagePath),wineImgIV);
                }else if (!TextUtils.isEmpty(imageUrl)){
                    GlideImageLoader.loadImage(mContext,imageUrl,wineImgIV);
                }
                winePriceTV.setText(price);
            }else{
                haveWineBgLayout.setVisibility(View.GONE);
            }

            if (ConstantValues.POLY_ADS.equals(libBean.getType())
                    ||ConstantValues.POLY_ADS_ONLINE.equals(libBean.getType())){
                if (libBean instanceof OOHLinkAdLocalBean){
                    noticeAdsMonitor(libBean);
                    oohlinkStartTime= System.currentTimeMillis();
                }else if (libBean instanceof ZmengAdLocalBean){
                    ZmengAdLocalBean bean = (ZmengAdLocalBean)libBean;
                    //回调开始播放监播url
                    zmengNoticeTrackingUrlByEvent(bean.getAdTrackingList(),START_TRACKING);
                }else if (libBean instanceof JDmomediaLocalBean){
                    JDmomediaLocalBean bean = (JDmomediaLocalBean)libBean;
                    JDmomediaNoticeTrackingUrl(bean.getAd_tracking(),START_TRACKING);
                }
            }

        }
        toCheckIfPolyAds(index);
        return false;
    }
    /**
     *是否展示二维码，展示什么类型的二维码
     */
    private void isShowMiniProgramIcon(MediaLibBean libBean){
        if (mSession.isShowAnimQRcode()){
            MiniProgramQrCodeWindowManager.get(this).setCurrentPlayMediaId(libBean.getVid());
        }else{
            QrCodeWindowManager.get(this).setCurrentPlayMediaId(libBean.getVid());
        }
        if (libBean.getIs_sapp_qrcode() == 1
                && !GlobalValues.isOpenRedEnvelopeWin
                && !libBean.getType().equals(ConstantValues.POLY_ADS)
                && !libBean.getType().equals(ConstantValues.POLY_ADS_ONLINE)
                && !libBean.getType().equals(ConstantValues.ACTGOODS_OPTI)
                && !libBean.getType().equals(ConstantValues.ACTGOODS_ACTIVITY)
                && !libBean.getType().equals(ConstantValues.ACTGOODS_COUNTDOWN)
                && !libBean.getType().equals(ConstantValues.SHOP_GOODS_ADS)) {
            if (mSession.isShowMiniProgramIcon()&& mSession.isShowSimpleMiniProgramIcon()){
                if (mSession.isWifiHotel()){
                    ((SavorApplication) getApplication()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_QRCODE_NETWORK_TYPE);
                }else {
                    if (mSession.isHeartbeatMiniNetty()) {
                        ((SavorApplication) getApplication()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_QRCODE_SMALL_TYPE);
                    }else{
                        ((SavorApplication) getApplication()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE);
                    }
                }
            }else if (!mSession.isShowMiniProgramIcon()&& mSession.isShowSimpleMiniProgramIcon()){
                if (mSession.isWifiHotel()){
                    ((SavorApplication) getApplication()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_QRCODE_NETWORK_TYPE);
                }else{
                    ((SavorApplication) getApplication()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE);
                }
            }else if (mSession.isShowMiniProgramIcon()&& !mSession.isShowSimpleMiniProgramIcon()){
                if (mSession.isHeartbeatMiniNetty()) {
                    ((SavorApplication) getApplication()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_QRCODE_SMALL_TYPE);
                }else {
                    ((SavorApplication) getApplication()).hideMiniProgramQrCodeWindow();
                }
            }
        }else{
            ((SavorApplication) getApplication()).hideMiniProgramQrCodeWindow();
        }
    }

    /**
     * 获取秒杀相关商品信息
     */
    private void getSeckillGoodsInfo(){
        try{
            String boxMac = mSession.getEthernetMac();
            AppApi.getSeckillGoodsFromCloudfrom(mContext,this,boxMac);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private long getGoodsCountdown(int goods_id){
        long countdownTime=0;
        try {
            JsonBean jsonBean = AppApi.getGoodsCountdownTime(mContext,this,goods_id);
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")==AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                countdownTime = jsonObject.getJSONObject("result").getLong("remain_time");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return countdownTime;
    }
    //倒计时线程
    private Runnable mCountDownRunnable = ()->goodsShowCountDown();

    private void goodsShowCountDown(){
        try{
            if (delayTime>0){
                delayTime = delayTime-1;
                goodsTimeTV.setText(delayTime+"s");
                mHandler.postDelayed(mCountDownRunnable,1000);
            }else{
                mHandler.removeCallbacks(mCountDownRunnable);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String zmengNoticeTrackingUrlByEvent(List<ZmtAPI.AdTracking> adTrackings, String event) {
        if (adTrackings == null || adTrackings.isEmpty()) {
            return null;
        }
        for (ZmtAPI.AdTracking adTracking : adTrackings) {
            if (event.equals(adTracking.getTrackingEvent())) {
                for (String trackingUrl:adTracking.getTrackingUrlList()){
                    AppApi.getNoticeAdsMonitor(mContext,this,trackingUrl);
                }
            }
        }
        return null;
    }

    /**
     * 京东钼媒回调地址
     * @param adTrackings 回调地址集合
     * @param position -1播放前前回调，1播放后回调
     */
    private void JDmomediaNoticeTrackingUrl(List<List<String>> adTrackings,String position){
        try {
            if (adTrackings!=null&&adTrackings.size()>0){
                List<String> list ;
                switch (position){
                    case "0":
                        list = adTrackings.get(0);
                        if (!list.isEmpty()){
                            for (String adTrack:list){
                                AppApi.getNoticeAdsMonitor(mContext,this,adTrack);
                            }
                        }
                        break;
                    case "1":
                        list = adTrackings.get(1);
                        if (!list.isEmpty()){
                            for (String adTrack:list){
                                AppApi.getNoticeAdsMonitor(mContext,this,adTrack);
                            }
                        }
                        list = adTrackings.get(2);
                        if (!list.isEmpty()){
                            for (String adTrack:list){
                                AppApi.getNoticeAdsMonitor(mContext,this,adTrack);
                            }
                        }
                        break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void showDanmaku(String url,String barrage){
        mSavorVideoView.addItems(url,barrage);
    }


    @Override
    public boolean onMediaError(int index, boolean isLast) {
        if (mNeedUpdatePlaylist) {
            LogUtils.v(TAG+" 视频播放异常更新播放列表后继续播放");
            // 重新获取播放列表开始播放
            mNeedUpdatePlaylist = false;
            if (GlobalValues.getInstance().PLAY_LIST != null && !GlobalValues.getInstance().PLAY_LIST.equals(mPlayList)) {
                int currentOrder = mPlayList.get(index).getOrder();
                mSavorVideoView.stop();
                checkAndPlay(currentOrder);
//                AppUtils.deleteOldMedia(mContext);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private int mCurrentPlayingIndex = -1;

    @Override
    public void onMediaPause(int index) {
        // 这里只是为了防止到这里的时候mUUID没值，正常mUUID肯定会在onMediaPrepared()中赋值
        if (TextUtils.isEmpty(mUUID)) {
            mUUID = String.valueOf(System.currentTimeMillis());
        }
        try {
            if (mPlayList != null && !TextUtils.isEmpty(mPlayList.get(index).getVid())) {
                LogReportUtil.get(this).sendAdsLog(mUUID, mSession.getBoiteId(), mSession.getRoomId(),
                        String.valueOf(System.currentTimeMillis()), "pause", mPlayList.get(index).getType(), mPlayList.get(index).getVid(),
                        "", mSession.getVersionName(), mListPeriod, mSession.getBirthdayOndemandPeriod(),
                        "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMediaResume(int index) {
        // 这里只是为了防止到这里的时候mUUID没值，正常mUUID肯定会在onMediaPrepared()中赋值
        if (TextUtils.isEmpty(mUUID)) {
            mUUID = String.valueOf(System.currentTimeMillis());
        }
        if (mPlayList != null && !TextUtils.isEmpty(mPlayList.get(index).getVid())) {
            LogReportUtil.get(this).sendAdsLog(mUUID, mSession.getBoiteId(), mSession.getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "resume", mPlayList.get(index).getType(), mPlayList.get(index).getVid(),
                    "", mSession.getVersionName(), mListPeriod, mSession.getBirthdayOndemandPeriod(),
                    "");
        }
    }

    /**
     * 回调计费曝光链接
     * @param obj
     */
    private void noticeAdsMonitor(Object obj){
        if (obj instanceof MeiAdLocalBean){
            MeiAdLocalBean bean = (MeiAdLocalBean)obj;
            if (!TextUtils.isEmpty(bean.getImpression())){
                ConstantValues.MEI_SSP_ADS_MONITOR_URL = bean.getImpression();
                AppApi.getNoticeAdsMonitor(this,this,bean.getImpression());
            }else{
                ConstantValues.MEI_SSP_ADS_MONITOR_URL = null;
            }
        }else if (obj instanceof OOHLinkAdLocalBean){
            OOHLinkAdLocalBean bean = (OOHLinkAdLocalBean)obj;
            if (bean.getWinNoticeUrlList()!=null&&bean.getWinNoticeUrlList().size()>0){
                for (String winNoticeUrl:bean.getWinNoticeUrlList()){
                    AppApi.getNoticeAdsMonitor(mContext,this,winNoticeUrl);
                }
            }
            if (bean.getAdTrackList()!=null&&bean.getAdTrackList().size()>0){
                for (AdTrack adTrack:bean.getAdTrackList()){
                    for (String trackUrl:adTrack.getTrackList()){
                        AppApi.getNoticeAdsMonitor(mContext,this,trackUrl);
                    }
                }
            }
        }else if (obj instanceof ZmengAdLocalBean){
            ZmengAdLocalBean bean = (ZmengAdLocalBean)obj;
            if (bean.getWinNoticeUrlList()!=null&&bean.getWinNoticeUrlList().size()>0){
                for (String winNoticeUrl:bean.getWinNoticeUrlList()){
                    AppApi.getNoticeAdsMonitor(mContext,this,winNoticeUrl);
                }
            }
        }else if (obj instanceof YishouAdLocalBean){
            YishouAdLocalBean bean = (YishouAdLocalBean)obj;
            if (!TextUtils.isEmpty(bean.getAd_tracking())){
                AppApi.getNoticeAdsMonitor(mContext,this,bean.getAd_tracking());
            }
        }

    }

    @Override
    public void onSuccess(AppApi.Action method, Object obj) {
        switch (method) {
            case AD_BAIDU_ADS:
                if (obj instanceof TsUiApiV20171122.TsApiResponse) {
                    TsUiApiV20171122.TsApiResponse tsApiResponse = (TsUiApiV20171122.TsApiResponse) obj;
                    if (BaiduAdsResponseCode.SUCCESS == tsApiResponse.getErrorCode()) {
                        if (TextUtils.isEmpty(dspRequestType)){
                            dspRequestType = ConstantValues.DSP_MEDIA_TYPE_BAIDU;
                        }
                        handleBaiduAdData(tsApiResponse);
                    } else {
                        LogUtils.e("百度聚屏请求错误，错误码为：" + tsApiResponse.getErrorCode());
                        LogFileUtil.write("百度聚屏请求错误，错误码为：" + tsApiResponse.getErrorCode());
                        try{
                            if (dspRequestType!=null&&dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_BAIDU)){
                                dspRequestType = null;
                            }
                            handleBaiduAdErrorData(tsApiResponse);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
                break;
            case AD_MEI_VIDEO_ADS_JSON:
                if (obj instanceof List<?>){
                    List<AdsMeiSSPResult> adsMeiSSPResultList = (List<AdsMeiSSPResult>)obj;
                    handleMeiSSPVideoAdsData(adsMeiSSPResultList);
                }
                break;
            case AD_MEI_IMAGE_ADS_JSON:
                if (obj instanceof List<?>){
                    List<AdsMeiSSPResult> adsMeiSSPResultList = (List<AdsMeiSSPResult>)obj;
                    handleMeiSSPImageAdsData(adsMeiSSPResultList);
                }
                break;
            case AD_POST_OOHLINK_ADS_JSON:
                if (obj instanceof AdInfo){
                    AdInfo adInfo = (AdInfo)obj;
                    if (adInfo!=null){
                        mAdInfo = adInfo;
                        handleOOHLinkAdsData(mAdInfo);
                    }
                }
                break;
            case AD_ZMENG_ADS:
                if (obj instanceof ZmtAPI.ZmAdResponse){
                    ZmtAPI.ZmAdResponse zmAdResponse = (ZmtAPI.ZmAdResponse)obj;
                    if (ZmengAdsResponseCode.SUCCESS==zmAdResponse.getErrorCode()){
                        handleZmengAdData(zmAdResponse);
                    }else{
                        if (dspRequestType!=null&&dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_ZMENG)){
                            dspRequestType = null;
                        }
                    }
                }
                break;
            case AD_POST_JDMOMEDIA_ADS_PLAIN:
                if (obj instanceof JDmomediaResult){
                    JDmomediaResult result = (JDmomediaResult)obj;
                    if (result!=null&&result.getCode()==AppApi.HTTP_RESPONSE_ADS_SUCCESS){
                        handleJDmomediaAdsData(result);
                    }else{
                        if (dspRequestType!=null&&dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_JDMOMEDIA)){
                            dspRequestType = null;
                        }
                    }
                }
                break;
            case AD_POST_YISHOU_JSON:
                if (obj instanceof AdPayloadBean){
                    AdPayloadBean payload= (AdPayloadBean)obj;
                    handleYishouAdsData(payload);
                }
                break;
            case CP_GET_SECKILL_GOODS_FROM_JSON:
                if (obj instanceof SeckillGoodsResult){
                    SeckillGoodsResult goodsResult = (SeckillGoodsResult)obj;
                    handleSeckillGoodsInfo(goodsResult);
                }
                break;
        }
    }

    private void handleBaiduAdErrorData(TsUiApiV20171122.TsApiResponse tsApiResponse){
        if (tsApiResponse!=null&&tsApiResponse.getAdsList()!=null
                &&!tsApiResponse.getAdsList().isEmpty()){
            for (TsUiApiV20171122.Ad ad:tsApiResponse.getAdsList()){
                if (ad.getMaterialMetasList()!=null){
                    for (TsUiApiV20171122.MaterialMeta materialMeta:ad.getMaterialMetasList()){
                        switch (materialMeta.getMaterialType()){
                            case VIDEO:
                                String md5 = materialMeta.getMaterialMd5().toStringUtf8();
                                LogUtils.d("百度聚屏请求，获取到物料md5：" + md5);
//                                LogFileUtil.write("百度聚屏请求，获取到物料md5：" + md5);
                                if (!TextUtils.isEmpty(md5)) {
                                    String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
                                    String[] selectionArgs = new String[]{md5};
                                    List<MediaLibBean> list = DBHelper.get(this).findRtbadsMediaLibByWhere(selection, selectionArgs);
                                    if (list != null && !list.isEmpty()) {
                                        DBHelper.get(this).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.RTB_ADS,selection, selectionArgs);
                                        if (GlobalValues.POLY_BAIDU_ADS_PLAY_LIST!=null&&GlobalValues.POLY_BAIDU_ADS_PLAY_LIST.size()>0){
                                            BaiduAdLocalBean localBean = null;
                                            for (BaiduAdLocalBean bean:GlobalValues.POLY_BAIDU_ADS_PLAY_LIST){
                                                if (bean.getTp_md5().equals(md5)){
                                                    localBean = bean;
                                                    break;
                                                }
                                            }
                                            if (localBean!=null){
                                                GlobalValues.POLY_BAIDU_ADS_PLAY_LIST.remove(localBean);
                                            }
                                        }
                                    }
                                }

                                break;
                            case IMAGE:

                                break;
                        }
                    }
                }
            }
            GlobalValues.CURRENT_MEDIA_ORDER = mPlayList.get(mCurrentPlayingIndex).getOrder();
            if (AppUtils.fillPlaylist(this, null, 1)) {
                mNeedUpdatePlaylist = true;
            }
        }
    }

    private void handleBaiduAdData(TsUiApiV20171122.TsApiResponse tsApiResponse) {
        if (tsApiResponse != null && tsApiResponse.getAdsList() != null && !tsApiResponse.getAdsList().isEmpty()) {
            ArrayList<BaiduAdLocalBean> baiduAdList = new ArrayList<>();
            for (TsUiApiV20171122.Ad ad :
                    tsApiResponse.getAdsList()) {
                if (ad.getMaterialMetasList() != null) {
                    for (TsUiApiV20171122.MaterialMeta material :
                            ad.getMaterialMetasList()) {
                        switch (material.getMaterialType()) {
                            case VIDEO:
                                String md5 = material.getMaterialMd5().toStringUtf8();
                                LogUtils.v("百度聚屏请求，获取到物料md5：" + md5);
//                                LogFileUtil.write("百度聚屏请求，获取到物料md5：" + md5);
                                if (!TextUtils.isEmpty(md5)) {
                                    String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
                                    String[] selectionArgs = new String[]{md5};
                                    List<MediaLibBean> list = DBHelper.get(this).findRtbadsMediaLibByWhere(selection, selectionArgs);
                                    if (list != null && !list.isEmpty()) {
                                        BaiduAdLocalBean bean = new BaiduAdLocalBean(list.get(0));
                                        bean.setMediaRemotePath(material.getVideoUrl());
                                        bean.setWinNoticeUrlList(ad.getWinNoticeUrlList());
                                        if (ad.getThirdMonitorUrlList()!=null&&ad.getThirdMonitorUrlList().size()>0){
                                            bean.setThirdMonitorUrlList(ad.getThirdMonitorUrlList());
                                        }
                                        bean.setExpireTime(tsApiResponse.getExpirationTime());

                                        baiduAdList.add(bean);

                                        if (GlobalValues.CURRENT_ADS_REPEAT_PAIR != null) {
                                            if (md5.equals(GlobalValues.CURRENT_ADS_REPEAT_PAIR.first)) {
                                                GlobalValues.CURRENT_ADS_REPEAT_PAIR = new Pair<>(md5, GlobalValues.CURRENT_ADS_REPEAT_PAIR.second + 1);
                                            } else {
                                                GlobalValues.CURRENT_ADS_REPEAT_PAIR = new Pair<>(md5, 1);
                                            }
                                        } else {
                                            GlobalValues.CURRENT_ADS_REPEAT_PAIR = new Pair<>(md5, 1);
                                        }
                                    } else {
                                        // 返回的物料在本地没找到，记录下来物料md5，下载完之后恢复请求
                                        GlobalValues.NOT_FOUND_BAIDU_ADS_KEY = md5;
                                        LogUtils.v("百度聚屏请求，物料未在本地发现：" + md5);
//                                        LogFileUtil.write("百度聚屏请求，物料未在本地发现：" + md5);
                                    }
                                }

                                break;
                            case IMAGE:
                                // TODO: 处理图片类广告
                                break;
                        }
                    }
                }
            }

            if (!baiduAdList.isEmpty() && mPlayList != null) {
                GlobalValues.POLY_BAIDU_ADS_PLAY_LIST = baiduAdList;
                GlobalValues.CURRENT_MEDIA_ORDER = mPlayList.get(mCurrentPlayingIndex).getOrder();
                if (AppUtils.fillPlaylist(this, null, 1)) {
                    mNeedUpdatePlaylist = true;
                }
            }
        }
    }
    private void handleMeiSSPVideoAdsData(List<AdsMeiSSPResult> results){
        ArrayList<MeiAdLocalBean> adsMeiSSPBeanList = new ArrayList<>();
        if (results!=null){
            for (AdsMeiSSPResult result:results){
                if (result.getVideo()!=null&&!TextUtils.isEmpty(result.getVideo().getUrl())){
                    AdsMeiSSPBean bean = result.getVideo();
                    String url = bean.getUrl();
                    String[] names = url.split("\\/");
                    String fileName = names[names.length-1];
                    String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
                    String[] selectionArgs = new String[]{fileName};
                    List<MediaLibBean> list = DBHelper.get(this).findRtbadsMediaLibByWhere(selection, selectionArgs);
                    if (list!=null){
                        MeiAdLocalBean meiAdLocalBean = new MeiAdLocalBean(list.get(0));
                        meiAdLocalBean.setImpression(result.getImpression()[0]);
                        adsMeiSSPBeanList.add(meiAdLocalBean);
                    }
                }
            }

        }
        isResponseVideo = true;
        LogUtils.d("MEISSP--VideoAds下载完成");
        updateMeiAdsToPlaylist(adsMeiSSPBeanList);
    }
    private void handleMeiSSPImageAdsData(List<AdsMeiSSPResult> results){
        ArrayList<MeiAdLocalBean> adsMeiSSPBeanList = new ArrayList<>();
        if (results!=null){
            for (AdsMeiSSPResult result:results){
                if (result.getImage()!=null&&!TextUtils.isEmpty(result.getImage().getUrl())){
                    AdsMeiSSPBean bean = result.getImage();
                    String url = bean.getUrl();
                    String[] names = url.split("\\/");
                    String fileName = names[names.length-1];
                    String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
                    String[] selectionArgs = new String[]{fileName};
                    List<MediaLibBean> list = DBHelper.get(this).findRtbadsMediaLibByWhere(selection, selectionArgs);
                    if (list!=null){
                        MeiAdLocalBean meiAdLocalBean = new MeiAdLocalBean(list.get(0));
                        meiAdLocalBean.setImpression(result.getImpression()[0]);
                        adsMeiSSPBeanList.add(meiAdLocalBean);
                    }
                }
            }

        }
        isResponseImage = true;
        LogUtils.d("MEISSP--ImageAds下载完成");
        updateMeiAdsToPlaylist(adsMeiSSPBeanList);
    }

    private void updateMeiAdsToPlaylist(ArrayList<MeiAdLocalBean> adsMeiSSPBeanList){
        if (TextUtils.isEmpty(dspRequestType)){
            dspRequestType = ConstantValues.DSP_MEDIA_TYPE_MEI;
        }
        if (GlobalValues.POLY_MEI_ADS_PLAY_LIST == null){
            GlobalValues.POLY_MEI_ADS_PLAY_LIST = new ArrayList<>();
        }
        GlobalValues.POLY_MEI_ADS_PLAY_LIST.addAll(adsMeiSSPBeanList);
        LogUtils.d("MEISSP--GlobalValues.POLY_ADS_PLAY_LIST大小="+GlobalValues.POLY_MEI_ADS_PLAY_LIST.size());
        if (!GlobalValues.POLY_MEI_ADS_PLAY_LIST.isEmpty() && mPlayList != null) {
            if (isResponseVideo&&isResponseImage){
                if (GlobalValues.POLY_MEI_ADS_PLAY_LIST.size()==0){
                    if (dspRequestType!=null&&dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_MEI)){
                        dspRequestType = null;
                    }
                }
                LogUtils.d("MEISSP--VideoAds&ImageAds下载完成");
                GlobalValues.CURRENT_MEDIA_ORDER = mPlayList.get(mCurrentPlayingIndex).getOrder();
                if (AppUtils.fillPlaylist(this, null, 1)) {
                    mNeedUpdatePlaylist = true;
                }
            }

        }
    }
    private void handleOOHLinkAdsData(final AdInfo adInfo){
        try{
            if (adInfo==null||adInfo.getErrorCode()!=0){
                if (dspRequestType!=null&&dspRequestType.equals(ConstantValues.DSP_MEDIA_TYPE_OOHLINK)){
                    dspRequestType = null;
                }
                return;
            }
            if (TextUtils.isEmpty(dspRequestType)){
                dspRequestType = ConstantValues.DSP_MEDIA_TYPE_OOHLINK;
            }
            ArrayList<OOHLinkAdLocalBean> adsOOHLinkBeanList = new ArrayList<>();
            if (adInfo!=null){
                String matMd5 = adInfo.getMatMd5();
                String name = adInfo.getFileName();
                int matType = adInfo.getMatType();
                if (matType==1){
                    name = matMd5+".jpg";
                }else if (matType==2){
                    name = matMd5+".mp4";
                }

                final String fileName = name;
                final String basePath = AppUtils.getFilePath(AppUtils.StorageFile.poly_ads_online);
                final String path = AppUtils.getFilePath(AppUtils.StorageFile.poly_ads_online) + fileName;
                final String url = adInfo.getMatUrl();
                if (!TextUtils.isEmpty(matMd5)){
                    String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
                    String[] selectionArgs = new String[]{matMd5};
                    List<MediaLibBean> list = DBHelper.get(this).findRtbadsMediaLibByWhere(selection, selectionArgs);
                    if (list!=null&&list.size()>0&&new File(path).exists()){
                        OOHLinkAdLocalBean bean = new OOHLinkAdLocalBean(list.get(0));
                        bean.setWinNoticeUrlList(adInfo.getWinNoticeUrlList());
                        if (adInfo.getAdTrackList()!=null&&adInfo.getAdTrackList().size()>0){
                            bean.setAdTrackList(adInfo.getAdTrackList());
                        }
                        bean.setExpireTime(adInfo.getExpTime());
                        adsOOHLinkBeanList.add(bean);
                        dbHelper.insertOrUpdateRTBAdsList(bean, true);
                    }else{
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
                                String[] selectionArgs = new String[]{adInfo.getMatMd5()};
                                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.RTB_ADS,selection,selectionArgs);
                                if (DSP_DOWNLOADING_FILES.contains(fileName)){
                                    return;
                                }

                                try {
                                    DSP_DOWNLOADING_FILES.add(fileName);
                                    boolean isDownloaded =new FileDownloader(mContext,url, basePath,fileName,true).downloadByRange();
                                    boolean isCompleted = AppUtils.isDownloadCompleted(path,adInfo.getMatMd5());
                                    if (isDownloaded&&isCompleted){
                                        DSP_DOWNLOADING_FILES.remove(fileName);
                                        MediaLibBean bean = new MediaLibBean();
                                        bean.setVid(adInfo.getMatMd5());
                                        bean.setName(fileName);
                                        bean.setChinese_name(adInfo.getFileName());
                                        bean.setMediaPath(path);
                                        bean.setDuration(adInfo.getDuration()+"");
                                        bean.setTp_md5(adInfo.getMatMd5());
                                        bean.setTpmedia_id(ConstantValues.DSP_MEDIA_TYPE_OOHLINK);
                                        bean.setType(ConstantValues.POLY_ADS_ONLINE);
                                        dbHelper.insertOrUpdateRTBAdsList(bean, false);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    }
                }

                if (!adsOOHLinkBeanList.isEmpty() && mPlayList != null) {
                    GlobalValues.DSP_OOHLINK_ADS_PLAY_LIST = adsOOHLinkBeanList;
                    GlobalValues.CURRENT_MEDIA_ORDER = mPlayList.get(mCurrentPlayingIndex).getOrder();
                    if (AppUtils.fillPlaylist(this, null, 1)) {
                        mNeedUpdatePlaylist = true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void handleZmengAdData(ZmtAPI.ZmAdResponse zmAdResponse){
        if (TextUtils.isEmpty(dspRequestType)){
            dspRequestType = ConstantValues.DSP_MEDIA_TYPE_ZMENG;
        }
        ArrayList<ZmengAdLocalBean> zmengAdList = new ArrayList<>();
        String url = AppUtils.bytesToString(zmAdResponse.getMaterialSrc().getBytes(),"UTF-8");
        String fileMd5 = zmAdResponse.getFileMd5();
        String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
        String[] selectionArgs = new String[]{fileMd5.toLowerCase()};
        List<MediaLibBean> list = DBHelper.get(this).findRtbadsMediaLibByWhere(selection, selectionArgs);
        if (list!=null&&list.size()>0){
            ZmengAdLocalBean zmengAdLocalBean = new ZmengAdLocalBean(list.get(0));
            zmengAdLocalBean.setWinNoticeUrlList(zmAdResponse.getWinNoticeUrlList());
            zmengAdLocalBean.setAdTrackingList(zmAdResponse.getAdTrackingList());
            zmengAdLocalBean.setExpireTime(zmAdResponse.getExpirationTime());
            zmengAdLocalBean.setDuration(zmAdResponse.getDuration()+"");
            zmengAdList.add(zmengAdLocalBean);
        }
        if (!zmengAdList.isEmpty() && mPlayList != null) {
            GlobalValues.DSP_ZMENG_ADS_PLAY_LIST = zmengAdList;
            GlobalValues.CURRENT_MEDIA_ORDER = mPlayList.get(mCurrentPlayingIndex).getOrder();
            if (AppUtils.fillPlaylist(this, null, 1)) {
                mNeedUpdatePlaylist = true;
            }
        }
    }

    private void handleJDmomediaAdsData(JDmomediaResult result){
        try{

            if (TextUtils.isEmpty(dspRequestType)){
                dspRequestType = ConstantValues.DSP_MEDIA_TYPE_JDMOMEDIA;
            }
            ArrayList<JDmomediaLocalBean> JDmomediaList = new ArrayList<>();
            JDmomediaBean jDmomediaBean = result.getData();
            if(jDmomediaBean.getMaterial()==null){
                return;
            }
            JDmomediaMaterial material = jDmomediaBean.getMaterial();
            String md5 = material.getMd5();
            String name = material.getTitle();
            String type = material.getType();
            if (type.equals("image")){
                name = md5+".jpg";
            }else if (type.equals("video")){
                name = md5+".mp4";
            }

            final String fileName = name;
            final String basePath =AppUtils.getFilePath(AppUtils.StorageFile.poly_ads_online);
            final String path = AppUtils.getFilePath(AppUtils.StorageFile.poly_ads_online) + fileName;
            final String url = material.getUrl();
            if (!TextUtils.isEmpty(md5)){
                String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
                String[] selectionArgs = new String[]{md5};
                List<MediaLibBean> list = DBHelper.get(this).findRtbadsMediaLibByWhere(selection, selectionArgs);
                if (list!=null&&list.size()>0&&new File(path).exists()){
                    JDmomediaLocalBean bean = new JDmomediaLocalBean(list.get(0));
                    bean.setAd_tracking(jDmomediaBean.getAd_tracking());

                    JDmomediaList.add(bean);

                }else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
                            String[] selectionArgs = new String[]{md5};
                            dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.RTB_ADS,selection,selectionArgs);
                            if (DSP_DOWNLOADING_FILES.contains(fileName)){
                                return;
                            }

                            try {
                                DSP_DOWNLOADING_FILES.add(fileName);
                                boolean isDownloaded =new FileDownloader(mContext,url, basePath,fileName,true).downloadByRange();
                                boolean isCompleted = AppUtils.isDownloadCompleted(path,md5.toUpperCase());
                                if (isDownloaded&&isCompleted){
                                    DSP_DOWNLOADING_FILES.remove(fileName);
                                    MediaLibBean bean = new MediaLibBean();
                                    bean.setVid(md5);
                                    bean.setName(fileName);
                                    bean.setChinese_name(material.getTitle());
                                    bean.setMediaPath(path);
                                    bean.setDuration(material.getShow_time()+"");
                                    bean.setTpmedia_id(ConstantValues.DSP_MEDIA_TYPE_JDMOMEDIA);
                                    bean.setTp_md5(md5);
                                    bean.setType(ConstantValues.POLY_ADS_ONLINE);

                                    dbHelper.insertOrUpdateRTBAdsList(bean, false);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            }

            if (!JDmomediaList.isEmpty() && mPlayList != null) {
                GlobalValues.DSP_JDMOMEDIA_ADS_PLAY_LIST = JDmomediaList;
                GlobalValues.CURRENT_MEDIA_ORDER = mPlayList.get(mCurrentPlayingIndex).getOrder();
                if (AppUtils.fillPlaylist(this, null, 1)) {
                    mNeedUpdatePlaylist = true;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleYishouAdsData(AdPayloadBean bean){
        if (bean==null){
            return;
        }
        try{
            ArrayList<YishouAdLocalBean> yishouAdList = new ArrayList<>();
            String sign = bean.getSign();
            String url = bean.getUrl();
            String suffix = url.substring(url.lastIndexOf(".")+1,url.length());
            String name = sign+"."+suffix;
            String fileName =name;
            final String basePath =AppUtils.getFilePath(AppUtils.StorageFile.poly_ads_online);
            final String path = basePath + fileName;
            if (TextUtils.isEmpty(sign)){
                return;
            }
            String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
            String[] selectionArgs = new String[]{sign};
            List<MediaLibBean> list = DBHelper.get(this).findRtbadsMediaLibByWhere(selection, selectionArgs);
            if (list!=null&&list.size()>0&&new File(path).exists()){
                YishouAdLocalBean yishouAdLocalBean = new YishouAdLocalBean(list.get(0));
                yishouAdLocalBean.setAd_tracking(bean.getTrack_url());
                yishouAdList.add(yishouAdLocalBean);
            }else{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
                        String[] selectionArgs = new String[]{sign};
                        dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.RTB_ADS,selection,selectionArgs);
                        if (DSP_DOWNLOADING_FILES.contains(fileName)){
                            return;
                        }
                        try {
                            DSP_DOWNLOADING_FILES.add(fileName);
                            boolean isDownloaded =new FileDownloader(mContext,url, basePath,fileName,true).downloadByRange();
                            boolean isCompleted = AppUtils.isDownloadCompleted(path,sign.toUpperCase());
                            if (isDownloaded&&isCompleted){
                                DSP_DOWNLOADING_FILES.remove(fileName);
                                MediaLibBean bean = new MediaLibBean();
                                bean.setVid(sign);
                                bean.setName(fileName);
                                bean.setChinese_name(fileName);
                                bean.setMediaPath(path);
                                bean.setSuffix(suffix);
                                bean.setDuration(bean.getDuration());
                                bean.setTpmedia_id(ConstantValues.DSP_MEDIA_TYPE_YISHOU);
                                bean.setTp_md5(sign);
                                bean.setType(ConstantValues.POLY_ADS_ONLINE);

                                dbHelper.insertOrUpdateRTBAdsList(bean, false);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
            if (!yishouAdList.isEmpty() && mPlayList != null) {
                GlobalValues.DSP_YISHOU_ADS_PLAY_LIST = yishouAdList;
                GlobalValues.CURRENT_MEDIA_ORDER = mPlayList.get(mCurrentPlayingIndex).getOrder();
                if (AppUtils.fillPlaylist(this, null, 1)) {
                    mNeedUpdatePlaylist = true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleSeckillGoodsInfo(SeckillGoodsResult goodsResult){
        try {
            if (goodsResult!=null){
                MediaLibBean libBean = mPlayList.get(mCurrentPlayingIndex);
                String mediaType = libBean.getType();
                String chineseName = libBean.getChinese_name();
                int leftPopWind = goodsResult.getLeft_pop_wind();
                if (leftPopWind==1&&mediaType.equals(ConstantValues.ADV)){
                    //秒杀商品业务
                    if (goodsResult.getDatalist()!=null&&goodsResult.getDatalist().size()>0&&seckillState==0){
                        seckillGoodsBeanList = goodsResult.getDatalist();
                        //展示秒杀商品
                        showCurrentSeckillGoods();
                        //展示15分钟后，在关闭15分钟，然后重新开启(该逻辑废除20220523)
//                        mHandler.postDelayed(()->goodsCloseCountdown(),1000*60*15);
                        //每隔5分钟切换一次秒杀商品
                        mHandler.postDelayed(switchSeckillRunnable,1000*60*5);
                    }
                }else{
                    lanternCloseWin();
                }

                //处理跑马灯相关逻辑
                int marquee = goodsResult.getMarquee();
                if (marquee==1
                        &&(mediaType.equals(ConstantValues.PRO)||mediaType.equals(ConstantValues.ADV))
                        &&!chineseName.startsWith(ConstantValues.DINNER_TOPIC)
                        &&goodsResult.getRoll_content()!=null
                        &&goodsResult.getRoll_content().length>0){
                    List<String> rollContent = Arrays.asList(goodsResult.getRoll_content());
                    handleCaptionTip(rollContent);
                }else{
                    captionLayout.setVisibility(View.GONE);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showCurrentSeckillGoods(){
        if (seckillGoodsBeanList==null||seckillGoodsBeanList.size()==0){
            currentSeckillIndex = 0;
            seckillState = -1;
            return;
        }
        currentSeckillIndex = currentSeckillIndex%seckillGoodsBeanList.size();
        seckillGoodsBean = seckillGoodsBeanList.get(currentSeckillIndex);
        seckillState =1;
        seckillTime = seckillGoodsBean.getRemain_time();
        String goodsImgUrl = BuildConfig.OSS_ENDPOINT+seckillGoodsBean.getImage();
        String jdPrice = seckillGoodsBean.getLine_price();
        String price = seckillGoodsBean.getPrice();
        String hotelName = seckillGoodsBean.getHotel_name();
        if (TextUtils.isEmpty(hotelName)){
            hotelNameFrontTV.setVisibility(View.GONE);
            hotelNameBackTV.setVisibility(View.GONE);
            LinearLayout.LayoutParams paramf = (LinearLayout.LayoutParams) seckillGifFrontIV.getLayoutParams();
            paramf.setMargins(0,20,0,0);
            seckillGifFrontIV.setLayoutParams(paramf);
            LinearLayout.LayoutParams paramb = (LinearLayout.LayoutParams) seckillGifBackIV.getLayoutParams();
            paramb.setMargins(0,20,0,0);
            seckillGifBackIV.setLayoutParams(paramb);

        }else{
            hotelNameFrontTV.setText(hotelName);
            hotelNameBackTV.setText(hotelName);
            hotelNameFrontTV.setVisibility(View.VISIBLE);
            hotelNameBackTV.setVisibility(View.VISIBLE);
        }
        GlideImageLoader.loadImage(mContext,goodsImgUrl,seckillGoodsFrontIV);
        GlideImageLoader.loadImage(mContext,goodsImgUrl,seckillGoodsBackIV);
        goodsJDPriceFrontTV.setText("京东价"+jdPrice+"/瓶");
        goodsJDPriceBackTV.setText("京东价"+jdPrice+"/瓶");
        goodsSeckillPriceFrontTV.setText(price+"/瓶");
        goodsSeckillPriceBackTV.setText(price+"/瓶");
        setAnimators(); // 设置动画
        setCameraDistance(); // 设置镜头距离
        //翻转秒杀背景动画
        mHandler.removeCallbacks(flipCardRunnable);
        mHandler.postDelayed(flipCardRunnable,1000*30);
        seckillFrontLayout.setVisibility(View.VISIBLE);
        seckillBackLayout.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(seckillCountdownRunnable);
        mHandler.post(seckillCountdownRunnable);
    }

    private void handleCaptionTip(List<String> rollContent){
        captionLayout.setVisibility(View.VISIBLE);
        String captionText = "";
        for (String content:rollContent){
            captionText = captionText+" "+content;
        }
        captionTipTV.setText(captionText);
        setTextMarquee(captionTipTV);

    }

    public static void setTextMarquee(TextView textView) {
        if (textView != null) {
            textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            textView.setSingleLine(true);
            textView.setSelected(true);
            textView.setFocusable(true);
            textView.setFocusableInTouchMode(true);
            textView.setHorizontallyScrolling(true);
            textView.setMarqueeRepeatLimit(-1);
        }
    }

    //关闭灯笼窗口
    private void lanternCloseWin(){
        seckillState = 0;
        if (seckillFrontLayout.getVisibility()==View.VISIBLE){
            seckillFrontLayout.setVisibility(View.GONE);
            seckillBackLayout.setVisibility(View.GONE);
            mHandler.removeCallbacks(seckillCountdownRunnable);
            mHandler.removeCallbacks(flipCardRunnable);
            mHandler.removeCallbacks(switchSeckillRunnable);
        }
    }
    //秒杀商品显示15分钟，关闭15分钟
//    private void goodsShowCountdown(){
//        seckillState=0;
//    }

    private void switchSeckillGoods(){
        currentSeckillIndex ++;
        showCurrentSeckillGoods();
        if (seckillGoodsBeanList!=null&&seckillGoodsBeanList.size()>0){
            mHandler.postDelayed(switchSeckillRunnable,1000*60*5);
        }
    }

    private Runnable switchSeckillRunnable = ()->switchSeckillGoods();

    @Override
    public void onError(AppApi.Action method, Object obj) {
        switch (method) {
            case AD_BAIDU_ADS:
                LogFileUtil.write("百度聚屏请求失败");
                break;
            case AD_MEI_VIDEO_ADS_JSON:
                isResponseVideo = true;
                break;
            case AD_MEI_IMAGE_ADS_JSON:
                isResponseImage = true;
                break;
            case AD_POST_OOHLINK_ADS_JSON:
                LogUtils.d("请求奥凌广告失败");
                break;
            case AD_POST_YISHOU_JSON:
                if (GlobalValues.YISHOU_REQUEST_TYPE.equals(ConstantValues.YISHOU_IMG)){
                    GlobalValues.YISHOU_REQUEST_TYPE = ConstantValues.YISHOU_VDO;
                }else{
                    GlobalValues.YISHOU_REQUEST_TYPE = ConstantValues.YISHOU_IMG;
                }
                break;
        }
    }

    @Override
    public void onNetworkFailed(AppApi.Action method) {
        switch (method) {
            case AD_BAIDU_ADS:
                LogFileUtil.write("百度聚屏请求失败，网络异常");
                break;
        }
    }

    @Override
    public void onMediaItemSelect(int index) {
        LogUtils.d("onMediaItemSelect index is " + index);
        if (mPlayList != null && index < mPlayList.size()) {
            if (mSavorVideoView != null) {
                ArrayList<String> urls = new ArrayList<>();
                for (int i = 0; i < mPlayList.size(); i++) {
                    MediaLibBean bean = mPlayList.get(i);
                    urls.add(bean.getMediaPath());
                }

                mSavorVideoView.setMediaFiles(urls, index, 0);
            }
        }
    }


    @Override
    protected void onDestroy() {
        LogUtils.d(TAG+"::onDestroy");
        LogFileUtil.write("AdsPlayerActivity onDestroy");
        super.onDestroy();
        GlobalValues.mIsGoneToTv = false;
        unregisterReceiver(mDownloadCompleteReceiver);
        unbindService(mConnection);

    }
}

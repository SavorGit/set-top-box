package com.savor.ads.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
import com.savor.ads.utils.IPAddressUtils;
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
//import com.tencent.bugly.crashreport.CrashReport;

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

import tianshu.ui.api.TsUiApiV20171122;
import tianshu.ui.api.ZmtAPI;
import tianshu.ui.api.ZmtAdRequestUtil;

import static com.savor.ads.utils.ConstantValues.DSP_DOWNLOADING_FILES;

import androidx.annotation.Nullable;

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

    WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
    private int layerWidth;
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
    Handler handler=new Handler(Looper.getMainLooper());
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
        IntentFilter intentApkFilter = new IntentFilter(ConstantValues.UPDATE_APK_ACTION);
        registerReceiver(mDownloadApkReceiver, intentApkFilter);
    }

    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConstantValues.UPDATE_PLAYLIST_ACTION.equals(intent.getAction())) {
                mNeedUpdatePlaylist = true;
            }
        }
    };

    private BroadcastReceiver mDownloadApkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConstantValues.UPDATE_APK_ACTION.equals(intent.getAction())) {
                onStop();
                ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

                manager.killBackgroundProcesses("com.savor.ads");
                // 获取默认启动activity
                final Intent intentLauncher = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentLauncher);
                //杀掉以前进程
                android.os.Process.killProcess(android.os.Process.myPid());

//                finish();
//                Intent mStartActivity = new Intent(context, MainActivity.class);
//                int mPendingIntentId = 123456;
//                PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//                AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 200, mPendingIntent);
//                System.exit(0);
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
        if (mCurrentVolume==0) {
            if(AppUtils.isGiec()){
                mCurrentVolume = mSession.getBoxCarouselVolume();
            }else{
                mCurrentVolume = mSession.getTvCarouselVolume();
            }
        }
        setVolume(mCurrentVolume);
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
        handler.removeCallbacks(layerToLeftRunnable);
        handler.removeCallbacks(layerToRightRunnable);
        haveWineBgLayout.clearAnimation();
        haveWineBgLayout.setVisibility(View.INVISIBLE);
        if (mPlayList != null) {
            MediaLibBean item = mPlayList.get(index);
            if (!TextUtils.isEmpty(item.getVid())) {
                LogReportUtil.get(this).sendAdsLog(mUUID, mSession.getBoiteId(), mSession.getRoomId(),
                        String.valueOf(System.currentTimeMillis()), "end", item.getType(), item.getVid(),
                        "", mSession.getVersionName(), mListPeriod, mSession.getBirthdayOndemandPeriod(),
                        "");
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

    @Override
    public boolean onMediaPrepared(int index) {
        if (haveWineBgLayout.VISIBLE==View.VISIBLE){
            haveWineBgLayout.clearAnimation();
            haveWineBgLayout.setVisibility(View.INVISIBLE);
        }
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
                handler.removeCallbacks(layerToLeftRunnable);
                handler.removeCallbacks(layerToRightRunnable);
                layerWidth = haveWineBgLayout.getWidth();
                String imagePath = libBean.getImage_path();
                String imageUrl = libBean.getImage_url();
                if (!TextUtils.isEmpty(imagePath)){
                    GlideImageLoader.loadLocalImage(mContext,new File(imagePath),wineImgIV);
                }else if (!TextUtils.isEmpty(imageUrl)){
                    GlideImageLoader.loadImage(mContext,imageUrl,wineImgIV);
                }
                String price = libBean.getPrice();
                int is_price = libBean.getIs_price();
                if (is_price==1&&!TextUtils.isEmpty(price)){
                    winePriceTV.setText(price);
                    handler.postDelayed(layerToLeftRunnable,15*1000);
                }
            }else{
                haveWineBgLayout.setVisibility(View.INVISIBLE);
            }

        }
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

    private Runnable layerToLeftRunnable = ()->setLayerRightToLeftAminator();
    /**设置展开动画*/
    private void setLayerRightToLeftAminator(){
        haveWineBgLayout.setVisibility(View.VISIBLE);
        Animation translateAnimation = new TranslateAnimation(wmParams.width+layerWidth, wmParams.width, 0, 0);//设置平移的起点和终点
        translateAnimation.setDuration(3000);//动画持续的时间为10s
        translateAnimation.setFillEnabled(true);//使其可以填充效果从而不回到原地
        translateAnimation.setFillAfter(true);//不回到起始位置
        //如果不添加setFillEnabled和setFillAfter则动画执行结束后会自动回到远点
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                handler.postDelayed(layerToRightRunnable,32*1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        haveWineBgLayout.setAnimation(translateAnimation);//给imageView添加的动画效果
        translateAnimation.startNow();//动画开始执行 放在最后即可
    }

    private Runnable layerToRightRunnable = ()->setLayerLeftToRightAminator();
    /**设置收起动画*/
    private void setLayerLeftToRightAminator(){
        haveWineBgLayout.clearAnimation();
        Animation translateAnimation = new TranslateAnimation(wmParams.width, wmParams.width+layerWidth, 0, 0);//设置平移的起点和终点
        translateAnimation.setDuration(3000);//动画持续的时间为10s
        translateAnimation.setFillEnabled(true);//使其可以填充效果从而不回到原地
        translateAnimation.setFillAfter(true);//不回到起始位置
        //如果不添加setFillEnabled和setFillAfter则动画执行结束后会自动回到远点
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                haveWineBgLayout.clearAnimation();
                haveWineBgLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        haveWineBgLayout.setAnimation(translateAnimation);//给imageView添加的动画效果
        translateAnimation.startNow();//动画开始执行 放在最后即可

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

    @Override
    public void onSuccess(AppApi.Action method, Object obj) {
        switch (method) {
            case CP_GET_SECKILL_GOODS_FROM_JSON:
                if (obj instanceof SeckillGoodsResult){
                    SeckillGoodsResult goodsResult = (SeckillGoodsResult)obj;
                    handleSeckillGoodsInfo(goodsResult);
                }
                break;
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
    int retryFrontCount = 0;
    int retryBackCount = 0;
    private void showCurrentSeckillGoods(){
        if (seckillGoodsBeanList==null||seckillGoodsBeanList.size()==0){
            currentSeckillIndex = 0;
            seckillState = -1;
            return;
        }
        retryFrontCount = 0;
        retryBackCount = 0;
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
        //加入重试机制，如果图片加载失败，则重试加载三次，如果还是失败，则关闭灯笼窗口20220608
        retryFrontLoadImg(goodsImgUrl,seckillGoodsFrontIV);
        retryBackLoadImg(goodsImgUrl,seckillGoodsBackIV);
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

    private void retryFrontLoadImg(String imageUrl,ImageView imageView){
        final Runnable runnable = ()->retryFrontLoadImg(imageUrl,imageView);
        GlideImageLoader.loadImageWithoutCache(mContext, imageUrl, imageView, new RequestListener() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
//                CrashReport.postCatchedException(e);
                if (retryFrontCount<3){
                    mHandler.postDelayed(runnable,100);
                    retryFrontCount++;
                }else{
                    mHandler.removeCallbacks(seckillCountdownRunnable);
                    mHandler.removeCallbacks(flipCardRunnable);
                    mHandler.removeCallbacks(switchSeckillRunnable);
                    seckillFrontLayout.setVisibility(View.GONE);
                    seckillBackLayout.setVisibility(View.GONE);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        });

    }
    private void retryBackLoadImg(String imageUrl,ImageView imageView){
        final Runnable runnable = ()->retryBackLoadImg(imageUrl,imageView);
        GlideImageLoader.loadImageWithoutCache(mContext, imageUrl, imageView, new RequestListener() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
//                CrashReport.postCatchedException(e);
                if (retryBackCount<3){
                    mHandler.postDelayed(runnable,100);
                    retryBackCount++;
                }else{
                    mHandler.removeCallbacks(seckillCountdownRunnable);
                    mHandler.removeCallbacks(flipCardRunnable);
                    mHandler.removeCallbacks(switchSeckillRunnable);
                    seckillFrontLayout.setVisibility(View.GONE);
                    seckillBackLayout.setVisibility(View.GONE);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        });

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
        }
    }

    @Override
    public void onNetworkFailed(AppApi.Action method) {
        switch (method) {

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
        unregisterReceiver(mDownloadApkReceiver);
        unbindService(mConnection);

    }
}

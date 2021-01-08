package com.savor.ads.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.savor.ads.service.RemoteService;
import com.jar.savor.box.vo.PlayResponseVo;
import com.jar.savor.box.vo.QueryPosBySessionIdResponseVo;
import com.jar.savor.box.vo.RotateResponseVo;
import com.jar.savor.box.vo.SeekResponseVo;
import com.jar.savor.box.vo.VolumeResponseVo;
import com.savor.ads.R;
import com.savor.ads.SavorApplication;
import com.savor.ads.bean.MediaFileBean;
import com.savor.ads.bean.MiniProgramProjection;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.customview.CircleProgressBar;
import com.savor.ads.customview.MyImageView;
import com.savor.ads.customview.ProjectVideoView;
import com.savor.ads.log.LogParamValues;
import com.savor.ads.log.LogReportUtil;
import com.savor.ads.projection.action.ProjectionActionBase;
import com.savor.ads.projection.action.StopAction;
import com.savor.ads.service.MiniProgramNettyService;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.KeyCode;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import pl.droidsonroids.gif.GifImageView;

public class ScreenProjectionActivity extends BaseActivity{
    private static final String TAG = "BLACK_SCREEN";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_PATH = "extra_path";
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_WAITER_ICON_URL = "extra_waiter_icon_url";
    public static final String EXTRA_WAITER_NAME = "extra_waiter_name";
    public static final String EXTRA_IMAGE_PATH = "extra_image_path";
    public static final String EXTRA_MUSIC_PATH = "extra_music_path";
    public static final String EXTRA_WORD_SIZE = "extra_word_size";
    public static final String EXTRA_WORD_COLOR = "extra_word_color";
    public static final String EXTRA_WORD_FONT_PATH = "extra_word_font_path";
    public static final String EXTRA_AVATAR_URL = "extra_avatar_url";
    public static final String EXTRA_NICKNAME = "extra_nickname";
    public static final String EXTRA_FORSCREEN_ID = "extra_forscreen_id";
    public static final String EXTRA_PRICE_ID = "extra_price_id";
    public static final String EXTRA_STORE_SALE_ID = "extra_store_sale_id";
    public static final String EXTRA_DELAY_TIME_ID = "extra_delay_time_id";
    public static final String EXTRA_ACTION_ID = "extra_action_id";
    public static final String EXTRA_FROM_SERVICE_ID = "extra_from_service";
    public static final String EXTRA_PROJECTION_WORDS = "extra_projection_words";
    public static final String EXTRA_PROJECTION_TIME = "extra_projection_time";
    public static final String EXTRA_MEDIA_ID = "extra_vid";
    public static final String EXTRA_IMAGE_ROTATION = "extra_image_rotation";
    public static final String EXTRA_IS_THUMBNAIL = "extra_is_thumbnail";
    public static final String EXTRA_IMAGE_TYPE = "extra_image_type";
    public static final String EXTRA_IS_FROM_WEB = "extra_is_from_web";
    public static final String EXTRA_PROJECT_ACTION = "extra_project_action";
    public static final String EXTRA_IS_NEW_DEVICE = "extra_is_new_device";

    /**
     * 投屏静止状态持续时间，超时自动退出投屏
     */
    //标准版投屏轮播时间
    private static final int PROJECT_DURATION = 1000 * 10+2000;
    //餐厅版投屏轮播时间
    private static final int REST_PROJECT_DURATION = 1000 * 60;
    private static final int WELCOME_PROJECT_DURATION = 1000 * 20;
    private static final int WELCOME_EVALUATE_DURATION = 1000 * 60;
    /**
     * 文件投屏持续时间
     */
    private static final int PROJECT_DURATION_FILE = 1000 * 60 * 5 + 1000;
    /**
     * 投屏类型
     */
    private String mProjectType;
    /**
     * 生日歌相关视频连续播3次
     */
    private int videoBirthdayCount=0;
    /**
     * 媒体文件位置
     */
    private String mMediaPath;
    private String mMediaUrl;
    /**
     * 视频ID（只有点播会传进来）
     */
    private String mMediaId;
    /**
     * 是否是缩略图（只有投图会传进来）
     */
    private boolean mIsThumbnail;
    /**
     * 图片类型
     * 1：普通图片；
     * 2：文件图片；
     * 3：幻灯片图片；
     * 4:小程序餐厅端投图片
     * 5:前置后置广告图片
     * 6:活动商品广告图片
     * 7:小程序销售端投欢迎词
     * 8:评价完服务人员显示的欢迎词
     */
    private int mImageType;
    private String mImagePath;
    private String mMusicPath;
    private String preForscreenId;
    private String mForscreenId;
    private String goodsPrice;
    /**是否店内有货 1有货 0无货*/
    private int storeSale;
    private int currentAction;
    private String waiterIconUrl;
    private String waiterName;
    /**投屏文字*/
    private String mProjectionWords;
    /**投屏文字-字号*/
    private String mProjectionWordsSize;
    /**投屏文字-颜色*/
    private String mProjectionWordsColor;
    /**投屏文字-字体*/
    private String mFontPath;
    /**小程序餐厅端投屏时长，接受的参数为毫秒*/
    private int projectionTime;
    private boolean mIsNewDevice;
    private String avatar_url;
    private String nickname;
    /**小程序投屏前后端广告投屏时长,作为广告倒计时使用，接受的参数为秒,需做转换*/
    private String delayTime;
    /**来自于哪个service的投屏请求，1： 标准版小程序，2：jetty服务(目前为极简版)*/
    private int from_service;
    private Handler mHandler = new Handler();
    /**
     * 旋转图片Runnable
     */
    private Runnable mRotateImageRunnable = ()->rotatePicture();
    /**
     * 定时退出投屏Runnable
     */
    private Runnable mExitProjectionRunnable = ()->handleProjectionEndResult();

    /**
     * 隐藏音量显示Runnable
     */
    private Runnable mHideVolumeViewRunnable = ()->hideVolumeView();

    private void hideVolumeView(){
        mVolumeRl.setVisibility(View.GONE);
    }
    /**
     * 隐藏静音显示Runnable
     */
    private Runnable mHideMuteViewRunnable = ()->hideMuteView();

    private void hideMuteView(){
        mMuteIv.setVisibility(View.GONE);
    }

    private ProjectVideoView mSavorVideoView;
    private RelativeLayout mImageArea;
    private ImageView mImageView;
    private MyImageView welcomeView;
    private TextView mProjectionWordsTV;
    private TextView mWelcomeWordsTV;
    private MediaPlayer mMusicPlayer;
    private GifImageView imageGifView;
    /**评价结束显示服务员信息*/
    private LinearLayout waiterLayout;
    private ImageView waiterIconTipIV;
    private TextView waiterNameTipTV;
    /**投欢迎词时显示服务员信息*/
    private LinearLayout waiterWelcomeLayout;
    private ImageView waiterIconWelcomeTipIV;
    private TextView waiterNameWelcomeTipTV;
    private RelativeLayout mImageLoadingTip;
    private CircleProgressBar mImageLoadingPb;
    private TextView mImageLoadingTv;
    private TextView mProjectTipTv;
    private ImageView mMuteIv;
    private RelativeLayout mVolumeRl;
    private TextView mVolumeTv;
    private ProgressBar mVolumePb;
    private RelativeLayout priceLayout;
    private TextView goodsPriceTV;
    private RelativeLayout storeSaleLayout;

    private LinearLayout wxProjectionTipLayout;
    private ImageView wxProjectionIconTipIV;
    private TextView wxProjectionTxtTipTV;
    private TextView projectCountdowTipTV;
    /**
     * 图片旋转角度
     */
    private int mImageRotationDegree;
    /**
     * 视频初始位置
     */
//    private int mVideoInitPosition;
    /**
     * 日志用的投屏动作记录标识
     */
    private String mUUID;

    private boolean mIsFirstResume = true;
    private boolean mHasInitializedVolume;

    private int mCurrentVolume = 60;
    private String mType;
    private String mInnerType;

    private boolean mIsFromWeb = false;
    private StopAction mStopAction;
    private ProjectionActionBase mProjectAction;

    private boolean isNewProjection = false;


    private ServiceConnection mNettyConnection;
    private ServiceConnection mJettyConnection;
    private MiniProgramNettyService miniProgramNettyService;
    private MiniProgramNettyService.AdsBinder adsBinder;
    private RemoteService remoteJettyService;
    private RemoteService.OperationBinder remoteBinder;

    private long startTime=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("onCreate " + this.hashCode());
        setContentView(R.layout.activity_screen_projection);

        findView();
        setView();
        isNewProjection = true;
        handleIntent();
        bindMiniprogramNettyService();
        bindMiniprogramJettyService();
    }

    /**
     * 绑定netty服务
     */
    private void bindMiniprogramNettyService(){
        mNettyConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                adsBinder = (MiniProgramNettyService.AdsBinder) binder;
                if (adsBinder!=null){
                    miniProgramNettyService = adsBinder.getService();
                    LogUtils.d(miniProgramNettyService+"123");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(mContext, MiniProgramNettyService.class);
        bindService(intent,mNettyConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 绑定netty服务
     */
    private void bindMiniprogramJettyService(){
        mJettyConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                remoteBinder = (RemoteService.OperationBinder) binder;
                if (remoteBinder!=null){
                    remoteJettyService = remoteBinder.getController();
                    LogUtils.d(remoteJettyService+"456");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(mContext, RemoteService.class);
        bindService(intent,mJettyConnection, Context.BIND_AUTO_CREATE);
    }
    private void findView() {
        mSavorVideoView = findViewById(R.id.video_view);
        mImageArea = findViewById(R.id.rl_image);
        mImageView =  findViewById(R.id.image_view);
        welcomeView = findViewById(R.id.welcome_view);
        imageGifView = findViewById(R.id.image_gif_view);
        waiterLayout = findViewById(R.id.waiter_layout);
        waiterIconTipIV = findViewById(R.id.waiter_icon_tip);
        waiterNameTipTV = findViewById(R.id.waiter_name_tip);
        waiterWelcomeLayout = findViewById(R.id.waiter_welcome_layout);
        waiterIconWelcomeTipIV = findViewById(R.id.waiter_icon_welcome_tip);
        waiterNameWelcomeTipTV = findViewById(R.id.waiter_name_welcome_tip);
        mProjectionWordsTV = findViewById(R.id.project_words);
        mWelcomeWordsTV = findViewById(R.id.welcome_words);
        mImageLoadingTip = findViewById(R.id.rl_loading_tip);
        mImageLoadingPb = findViewById(R.id.pb_image);
        mImageLoadingTv = findViewById(R.id.tv_loading_tip);
        mProjectTipTv = findViewById(R.id.tv_project_tip);
        mMuteIv = findViewById(R.id.iv_mute);
        mVolumeRl = findViewById(R.id.rl_volume_view);
        mVolumeTv = findViewById(R.id.tv_volume);
        mVolumePb =  findViewById(R.id.pb_volume);

        wxProjectionTipLayout = findViewById(R.id.wx_projection_tip_layout);
        wxProjectionIconTipIV = findViewById(R.id.wx_projection_icon_tip);
        wxProjectionTxtTipTV = findViewById(R.id.wx_projection_txt_tip);

        projectCountdowTipTV = findViewById(R.id.tv_project_countdow_tip);
        priceLayout = findViewById(R.id.price_layout);
        goodsPriceTV = findViewById(R.id.goods_price);
        storeSaleLayout = findViewById(R.id.store_sale_layout);
    }

    private void setView() {
        mSavorVideoView.setIfShowPauseBtn(true);
        mSavorVideoView.setIfShowLoading(true);
        mSavorVideoView.setLooping(false);
        mSavorVideoView.setIfHandlePrepareTimeout(true);
        mSavorVideoView.setPlayStateCallback(mPlayStateCallback);

    }

    private void initSounds(){
        try{
            if (!TextUtils.isEmpty(mMusicPath)){
                mMusicPlayer = new MediaPlayer();
                mMusicPlayer.setDataSource(mMusicPath);
                mMusicPlayer.prepare();
                mMusicPlayer.start();
                mMusicPlayer.setLooping(true);
            }
            initVolume();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void initVolume() {
        if (!mHasInitializedVolume) {
            if (AppUtils.isSVT()) {
                mCurrentVolume = mSession.getXxProjectionVolume();
            } else {
                mCurrentVolume = mSession.getProjectVolume();
            }
            setVolume(mCurrentVolume);
            mHasInitializedVolume = true;
        }
    }
    /**
     * 处理自己本身的投屏
     * */
    private void handleIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            LogUtils.w("handleIntent will exitProjection " + this.hashCode());
            exitProjection();
        } else {
            handleBundleData(bundle);

            mappingLogType();

            handleProjectRequest();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mIsNewDevice&&!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_DEVICE_NAME)) {
//            projectTipAnimateIn();
            mUUID = null;
        } else {
            mProjectTipTv.setVisibility(View.GONE);
        }
    }

    private void handleBundleData(Bundle bundle) {
        mIsThumbnail = bundle.getBoolean(EXTRA_IS_THUMBNAIL, true);
        mProjectType = bundle.getString(EXTRA_TYPE);
        mMediaPath = bundle.getString(EXTRA_PATH);
        mMediaUrl = bundle.getString(EXTRA_URL);
        avatar_url = bundle.getString(EXTRA_AVATAR_URL);
        nickname = bundle.getString(EXTRA_NICKNAME);
        waiterIconUrl = bundle.getString(EXTRA_WAITER_ICON_URL);
        waiterName = bundle.getString(EXTRA_WAITER_NAME);
        delayTime = bundle.getString(EXTRA_DELAY_TIME_ID);
        if (mIsThumbnail) {
            mMediaId = bundle.getString(EXTRA_MEDIA_ID, "");
        }
        from_service = bundle.getInt(EXTRA_FROM_SERVICE_ID);
        mImageRotationDegree = bundle.getInt(EXTRA_IMAGE_ROTATION);
        mImageType = bundle.getInt(EXTRA_IMAGE_TYPE);
        mImagePath = bundle.getString(EXTRA_IMAGE_PATH);
        mMusicPath = bundle.getString(EXTRA_MUSIC_PATH);
        preForscreenId = mForscreenId;
        mForscreenId = bundle.getString(EXTRA_FORSCREEN_ID);
        goodsPrice = bundle.getString(EXTRA_PRICE_ID);
        storeSale = bundle.getInt(EXTRA_STORE_SALE_ID,0);

        currentAction = bundle.getInt(EXTRA_ACTION_ID,0);
        mProjectionWords = bundle.getString(EXTRA_PROJECTION_WORDS);
        mProjectionWordsSize = bundle.getString(EXTRA_WORD_SIZE);
        mProjectionWordsColor = bundle.getString(EXTRA_WORD_COLOR);
        mFontPath = bundle.getString(EXTRA_WORD_FONT_PATH);
        projectionTime = bundle.getInt(EXTRA_PROJECTION_TIME);

        mIsFromWeb = bundle.getBoolean(EXTRA_IS_FROM_WEB);
        mIsNewDevice = bundle.getBoolean(EXTRA_IS_NEW_DEVICE);


//        Typeface typeface = Typeface.createFromFile(AppUtils.getSDCardPath()+"typeface/wxz.ttf");
        if (!TextUtils.isEmpty(mFontPath)){
            Typeface typeface = Typeface.createFromFile(mFontPath);
            mWelcomeWordsTV.setTypeface(typeface);
        }else{
            mWelcomeWordsTV.setTypeface(null);
        }
        mProjectAction = (ProjectionActionBase) bundle.getSerializable(EXTRA_PROJECT_ACTION);
        if (mProjectAction != null) {
            mProjectAction.onActionEnd();
        }
    }

    public void setTextViewStyles(TextView text){
        int color1 = Color.parseColor("#0066FF");
        int color2 = Color.parseColor("#FF3333");
        LinearGradient mLinearGradient =new LinearGradient(0, 0, 0, text.getPaint().getTextSize(), color1, color2, Shader.TileMode.CLAMP);

    text.getPaint().setShader(mLinearGradient);

    text.invalidate();

    }

    /**
     * 处理投屏
     * 根据不同的投屏类型
     */
    private void handleProjectRequest() {
        startTime = System.currentTimeMillis();
        if (!ConstantValues.PROJECT_TYPE_REST_PICTURE.equals(mProjectType)||
                (ConstantValues.PROJECT_TYPE_REST_PICTURE.equals(mProjectType)&&mIsThumbnail)){

            ((SavorApplication) getApplication()).hideMiniProgramQrCodeWindow();
        }
        ((SavorApplication) getApplication()).hideGoodsCountdownQrCodeWindow();
        if (currentAction!=40){
            ((SavorApplication) getApplication()).hideGoodsQrCodeWindow();
        }
        if (from_service!=GlobalValues.FROM_SERVICE_MINIPROGRAM){
            GlobalValues.PROJECT_IMAGES.clear();
            LogUtils.d("projectionIdMap>3>GlobalValues.PROJECT_IMAGES=null");
        }else if (from_service!=GlobalValues.FROM_SERVICE_REMOTE){
            GlobalValues.PROJECT_STREAM_IMAGE.clear();
        }
        mSavorVideoView.setLooping(false);
        projectCountdowTipTV.setVisibility(View.GONE);
        mWelcomeWordsTV.setVisibility(View.GONE);
        mProjectionWordsTV.setVisibility(View.GONE);
        priceLayout.setVisibility(View.GONE);
        storeSaleLayout.setVisibility(View.GONE);
        waiterLayout.setVisibility(View.GONE);
        waiterWelcomeLayout.setVisibility(View.GONE);
        //有新的投屏进来，如果有正在轮播的欢迎词，就打断播放,并且停止背景音乐 20191212
        GlobalValues.mpprojection = null;
        if (mMusicPlayer!=null&&mMusicPlayer.isPlaying()){
            mMusicPlayer.setLooping(false);
            mMusicPlayer.stop();
            mMusicPlayer.release();
            mMusicPlayer = null;
        }
        if (!TextUtils.isEmpty(nickname)
                &&!TextUtils.isEmpty(avatar_url)
                &&!ConstantValues.PROJECT_TYPE_REST_PICTURE.equals(mProjectType)
                &&!ConstantValues.PROJECT_TYPE_VIDEO_REST.equals(mProjectType)){
            wxProjectionTxtTipTV.setText(nickname);
            GlideImageLoader.loadRoundImage(mContext,avatar_url,wxProjectionIconTipIV,R.mipmap.wxavatar);
            wxProjectionTxtTipTV.setVisibility(View.VISIBLE);
            wxProjectionIconTipIV.setVisibility(View.VISIBLE);
        }else{
            wxProjectionIconTipIV.setVisibility(View.GONE);
            wxProjectionTxtTipTV.setVisibility(View.GONE);
        }
        if (ConstantValues.PROJECT_TYPE_VIDEO_VOD.equals(mProjectType)) {
            // 点播
            mSavorVideoView.setVisibility(View.VISIBLE);
            mImageArea.setVisibility(View.GONE);
            wxProjectionTipLayout.setVisibility(View.GONE);
            mHandler.removeCallbacks(mExitProjectionRunnable);
            mHandler.removeCallbacks(mShowMiniProgramQrCodeRunnable);
            mHandler.removeCallbacks(mCountDownRunnable);
            ArrayList<MediaFileBean> list = new ArrayList<>();
            MediaFileBean bean = new MediaFileBean();
            if (!TextUtils.isEmpty(mMediaUrl)){
                bean.setUrl(mMediaUrl);
            }
            if (!TextUtils.isEmpty(mMediaPath)){
                bean.setCacheFile(new File(mMediaPath));
            }
            list.add(bean);

            mSavorVideoView.setMediaFiles(list);
        }else if (ConstantValues.PROJECT_TYPE_VIDEO_BIRTHDAY.equals(mProjectType)){
            // 生日点播
            mSavorVideoView.setVisibility(View.VISIBLE);
            mImageArea.setVisibility(View.GONE);
            mSavorVideoView.setLooping(true);
            videoBirthdayCount = 0;
            wxProjectionTipLayout.setVisibility(View.GONE);
            mHandler.removeCallbacks(mExitProjectionRunnable);
            mHandler.removeCallbacks(mShowMiniProgramQrCodeRunnable);
            mHandler.removeCallbacks(mCountDownRunnable);
            ArrayList<MediaFileBean> list = new ArrayList<>();
            MediaFileBean bean = new MediaFileBean();
            bean.setUrl(mMediaUrl);
            if (!TextUtils.isEmpty(mMediaPath)){
                bean.setCacheFile(new File(mMediaPath));
            }
            list.add(bean);

            mSavorVideoView.setMediaFiles(list);
        }else if (ConstantValues.PROJECT_TYPE_VIDEO.equals(mProjectType)) {

            if (currentAction==40){
                if (!TextUtils.isEmpty(goodsPrice)){
                    priceLayout.setVisibility(View.VISIBLE);
                    goodsPriceTV.setText(goodsPrice);
                }else{
                    priceLayout.setVisibility(View.GONE);
                }
                if (storeSale>0){
                   storeSaleLayout.setVisibility(View.VISIBLE);
                }else {
                   storeSaleLayout.setVisibility(View.GONE);
                }
            }
            // 视频投屏
            mSavorVideoView.setVisibility(View.VISIBLE);
            mImageArea.setVisibility(View.GONE);
            mHandler.removeCallbacks(mExitProjectionRunnable);
            mHandler.removeCallbacks(mShowMiniProgramQrCodeRunnable);
            mHandler.removeCallbacks(mCountDownRunnable);
            ArrayList<MediaFileBean> list = new ArrayList<>();
            MediaFileBean bean = new MediaFileBean();
            if (!TextUtils.isEmpty(mMediaUrl)){
                bean.setUrl(mMediaUrl);
            }
            if (!TextUtils.isEmpty(mMediaPath)){
                bean.setCacheFile(new File(mMediaPath));
            }
            list.add(bean);
            mSavorVideoView.setMediaFiles(list);
            mHandler.removeCallbacks(mCountDownRunnable);
            if (!TextUtils.isEmpty(delayTime)&&!"0".equals(delayTime)){
                projectCountdowTipTV.setText(delayTime);
                projectCountdowTipTV.setVisibility(View.VISIBLE);
                mHandler.post(mCountDownRunnable);
            }

        }else if (ConstantValues.PROJECT_TYPE_VIDEO_REST.equals(mProjectType)) {
            // 小程序餐厅端视频投屏
            mSavorVideoView.setVisibility(View.VISIBLE);
            mSavorVideoView.setLooping(true);
            mImageArea.setVisibility(View.GONE);

            mHandler.removeCallbacks(mExitProjectionRunnable);
            mHandler.removeCallbacks(mShowMiniProgramQrCodeRunnable);
            mHandler.removeCallbacks(mCountDownRunnable);
            ArrayList<MediaFileBean> list = new ArrayList<>();
            MediaFileBean bean = new MediaFileBean();
            bean.setUrl(mMediaUrl);
            if (!TextUtils.isEmpty(mMediaPath)){
                bean.setCacheFile(new File(mMediaPath));
            }
            list.add(bean);

            mSavorVideoView.setMediaFiles(list);
            if (projectionTime==0){
                rescheduleToExit(false);
            }else{
                rescheduleToExit(true);
            }
            //如果餐厅端通过小程序投视频超过2分钟，那么就展示小程序码,add at time:20190325
            mHandler.postDelayed(mShowMiniProgramQrCodeRunnable,1000*60*2);

        } else if (ConstantValues.PROJECT_TYPE_PICTURE.equals(mProjectType)) {
            if (currentAction==4
                    &&(TextUtils.isEmpty(preForscreenId)||(!TextUtils.isEmpty(preForscreenId)&&!preForscreenId.equals(mForscreenId)))){
                //投图片开始展示第一张时上报一次
                showImgLog();
            }
            downloadLog(true);
            if (currentAction==40){
                if (!TextUtils.isEmpty(goodsPrice)){
                    priceLayout.setVisibility(View.VISIBLE);
                    goodsPriceTV.setText(goodsPrice);
                }else {
                    priceLayout.setVisibility(View.GONE);
                }
                if (storeSale>0){
                    storeSaleLayout.setVisibility(View.VISIBLE);
                }else {
                    storeSaleLayout.setVisibility(View.GONE);
                }
            }else {
                priceLayout.setVisibility(View.GONE);
                storeSaleLayout.setVisibility(View.GONE);
            }
            // 图片投屏
            mSavorVideoView.setVisibility(View.GONE);
            mSavorVideoView.release();
            mImageArea.setVisibility(View.VISIBLE);
            welcomeView.setVisibility(View.GONE);
            mHandler.removeCallbacks(mShowMiniProgramQrCodeRunnable);
            mHandler.removeCallbacks(mCountDownRunnable);
            // 展示图片
            if (GlobalValues.CURRENT_PROJECT_BITMAP != null) {
                if (mImageView.getDrawable() != null) {
                    if (mImageView.getDrawable() instanceof BitmapDrawable) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) mImageView.getDrawable();
                        if (bitmapDrawable.getBitmap() != null && bitmapDrawable.getBitmap() != GlobalValues.CURRENT_PROJECT_BITMAP) {
                            bitmapDrawable.getBitmap().recycle();
                        }
                    }
                }

                // 图片分辨率过高的话ImageView加载会黑屏，这里缩小后再加载
                if (GlobalValues.CURRENT_PROJECT_BITMAP.getWidth() > DensityUtil.getScreenRealSize(this).x) {
                    GlobalValues.CURRENT_PROJECT_BITMAP = Bitmap.createScaledBitmap(GlobalValues.CURRENT_PROJECT_BITMAP,
                            DensityUtil.getScreenRealSize(this).x,
                            GlobalValues.CURRENT_PROJECT_BITMAP.getHeight() * DensityUtil.getScreenRealSize(this).x / GlobalValues.CURRENT_PROJECT_BITMAP.getWidth(), true);
                }
                mImageView.setImageBitmap(GlobalValues.CURRENT_PROJECT_BITMAP);
            }else if (!TextUtils.isEmpty(mImagePath)){

                if (mImagePath.endsWith("gif")){

                    imageGifView.setImageURI(Uri.fromFile(new File(mImagePath)));
                    imageGifView.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.GONE);
                }else{
                    imageGifView.setVisibility(View.GONE);
                    mImageView.setVisibility(View.VISIBLE);
                    //最后一个参数传Drawable是为了解决图片切换闪烁问题
                    if (mImageView.getDrawable()!=null){
                        GlideImageLoader.loadImageWithDrawable(mContext,mImagePath,mImageView,mImageView.getDrawable());
                    }else{
                        File file = new File(mImagePath);
                        GlideImageLoader.loadLocalImage(mContext,file,mImageView);
                    }
                }
            }
            if (!TextUtils.isEmpty(mProjectionWords)){
                mProjectionWordsTV.setText(mProjectionWords);
                mProjectionWordsTV.setVisibility(View.VISIBLE);
            }else{
                mProjectionWordsTV.setVisibility(View.GONE);
            }
            if (mIsThumbnail) {
                // 只有当传过来是缩略图时才去重置ImageView状态
                mImageView.setRotation(0);
                mImageView.setScaleX(1);
                mImageView.setScaleY(1);
                rotatePicture();
            }
            rescheduleToExit(true);
            mHandler.removeCallbacks(mCountDownRunnable);
            if (!TextUtils.isEmpty(delayTime)&&!"0".equals(delayTime)){
                projectCountdowTipTV.setText(delayTime);
                projectCountdowTipTV.setVisibility(View.VISIBLE);
                mHandler.post(mCountDownRunnable);
            }
        } else if (mProjectType.equals(ConstantValues.PROJECT_TYPE_REST_PICTURE)){
            downloadLog(true);
            //小程序餐厅端投图片
            mSavorVideoView.setVisibility(View.GONE);
            mSavorVideoView.release();
            mImageArea.setVisibility(View.VISIBLE);
            welcomeView.setVisibility(View.GONE);
            mHandler.removeCallbacks(mShowMiniProgramQrCodeRunnable);
            mHandler.removeCallbacks(mCountDownRunnable);

            if (mImagePath.endsWith("gif")){

                imageGifView.setImageURI(Uri.fromFile(new File(mImagePath)));
                imageGifView.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.GONE);
            }else{
                imageGifView.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                //最后一个参数传Drawable是为了解决图片切换闪烁问题
                if (mImageView.getDrawable()!=null){
                    GlideImageLoader.loadImageWithDrawable(mContext,mImagePath,mImageView,mImageView.getDrawable());
                }else{
                    GlideImageLoader.loadImage(mContext,mImagePath,mImageView);
                }

            }
            if (!TextUtils.isEmpty(mProjectionWords)){
                mProjectionWordsTV.setText(mProjectionWords);
                mProjectionWordsTV.setVisibility(View.VISIBLE);
            }else{
                mProjectionWordsTV.setVisibility(View.GONE);
            }
            mImageView.setRotation(0);
            mImageView.setScaleX(1);
            mImageView.setScaleY(1);
            rotatePicture();
            if (mIsThumbnail) {
                // 只有当传过来是缩略图时才去重置ImageView状态
                rescheduleToExit(true);
                //如果餐厅端通过小程序投视频超过2分钟，那么就展示小程序码,add at time:20190325

                mHandler.postDelayed(mShowMiniProgramQrCodeRunnable,1000*60*2);

            }else{
                if (projectionTime==0){
                    rescheduleToExit(true);
                }
            }

        }else if (mProjectType.equals(ConstantValues.PROJECT_TYPE_WELCOME_PICTURE)){
            // 小程序餐厅销售端欢迎词投屏
            mSavorVideoView.setVisibility(View.GONE);
            mSavorVideoView.release();
            mImageArea.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            mHandler.removeCallbacks(mShowMiniProgramQrCodeRunnable);
            mHandler.removeCallbacks(mCountDownRunnable);
            if (!TextUtils.isEmpty(mImagePath)){
                if (mImagePath.endsWith("gif")){

                    imageGifView.setImageURI(Uri.fromFile(new File(mImagePath)));
                    imageGifView.setVisibility(View.VISIBLE);
                    welcomeView.setVisibility(View.GONE);
                }else{
                    imageGifView.setVisibility(View.GONE);
                    welcomeView.setVisibility(View.VISIBLE);
                    //最后一个参数传Drawable是为了解决图片切换闪烁问题
                    try{
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (mImageType==8){
                        GlideImageLoader.loadLocalImage(mContext,Integer.valueOf(mImagePath),welcomeView);
                    }else{
                        if (welcomeView.getDrawable()!=null){
                            GlideImageLoader.loadImageWithDrawable(mContext,mImagePath,welcomeView,welcomeView.getDrawable());
                        }else{
                            GlideImageLoader.loadImage(mContext,mImagePath,welcomeView);
                        }
                    }

                }
            }

            if (!TextUtils.isEmpty(mProjectionWords)){
                mWelcomeWordsTV.setText(mProjectionWords);
                mWelcomeWordsTV.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(mProjectionWordsColor)){
                    mWelcomeWordsTV.setTextColor(Color.parseColor(mProjectionWordsColor));
                }
                if (!TextUtils.isEmpty(mProjectionWordsSize)){
                    mWelcomeWordsTV.setTextSize(Float.parseFloat(mProjectionWordsSize));
                }
            }else{
                mWelcomeWordsTV.setVisibility(View.GONE);
            }
            if (mImageType==7||mImageType==8){
                if (!TextUtils.isEmpty(waiterName)){
                    waiterWelcomeLayout.setVisibility(View.VISIBLE);
                    waiterNameWelcomeTipTV.setText(waiterName+"为您服务");
                    if (!TextUtils.isEmpty(waiterIconUrl)){
                        GlideImageLoader.loadRoundImage(mContext,waiterIconUrl,waiterIconWelcomeTipIV,R.mipmap.wxavatar);
                    }
                }else{
                    waiterWelcomeLayout.setVisibility(View.GONE);
                }
            }

            mHandler.postDelayed(()->initSounds(),500);
            welcomeView.setRotation(0);
            welcomeView.setScaleX(1);
            welcomeView.setScaleY(1);
            welcomeRotatePicture();
            rescheduleToExit(true);
            //如果餐厅端通过小程序投欢迎词超过5分钟，那么就展示小程序码,add at time:20191212
            mHandler.postDelayed(mShowMiniProgramQrCodeRunnable,1000*60*5);
            if (mImageType==7){
                MiniProgramProjection mpp = new MiniProgramProjection();
                mpp.setImg_path(mImagePath);
                mpp.setMusic_path(mMusicPath);
                mpp.setForscreen_char(mProjectionWords);
                mpp.setWordsize(mProjectionWordsSize);
                mpp.setColor(mProjectionWordsColor);
                mpp.setFont_path(mFontPath);
                mpp.setPlay_times(projectionTime);
                mpp.setWaiterIconUrl(waiterIconUrl);
                mpp.setWaiterName(waiterName);

                GlobalValues.mpprojection = mpp;
            }


        }else if (mProjectType.equals(ConstantValues.PROJECT_TYPE_BUSINESS_WELCOME)){
            // 小程序用戶端售端欢迎词投屏
            mSavorVideoView.setVisibility(View.GONE);
            mSavorVideoView.release();
            mImageArea.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            mHandler.removeCallbacks(mShowMiniProgramQrCodeRunnable);
            mHandler.removeCallbacks(mCountDownRunnable);
            if (!TextUtils.isEmpty(mImagePath)){
                if (mImagePath.endsWith("gif")){

                    imageGifView.setImageURI(Uri.fromFile(new File(mImagePath)));
                    imageGifView.setVisibility(View.VISIBLE);
                    welcomeView.setVisibility(View.GONE);
                }else{
                    imageGifView.setVisibility(View.GONE);
                    welcomeView.setVisibility(View.VISIBLE);
                    //最后一个参数传Drawable是为了解决图片切换闪烁问题
                    if (welcomeView.getDrawable()!=null){
                        GlideImageLoader.loadImageWithDrawable(mContext,mImagePath,welcomeView,welcomeView.getDrawable());
                    }else{
                        GlideImageLoader.loadImage(mContext,mImagePath,welcomeView);
                    }
                }
            }

            if (!TextUtils.isEmpty(mProjectionWords)){
                mWelcomeWordsTV.setText(mProjectionWords);
                mWelcomeWordsTV.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(mProjectionWordsColor)){
                    mWelcomeWordsTV.setTextColor(Color.parseColor(mProjectionWordsColor));
                }
                if (!TextUtils.isEmpty(mProjectionWordsSize)){
                    mWelcomeWordsTV.setTextSize(Float.parseFloat(mProjectionWordsSize));
                }
            }else{
                mWelcomeWordsTV.setVisibility(View.GONE);
            }

            mHandler.postDelayed(()->initSounds(),500);
            welcomeView.setRotation(0);
            welcomeView.setScaleX(1);
            welcomeView.setScaleY(1);
            if (mIsThumbnail){
                rescheduleToExit(true);
            }
        }
        if (!mProjectType.startsWith("rstr_")) {
            if (!ConstantValues.PROJECT_TYPE_PICTURE.equals(mProjectType) || mIsThumbnail) {
                LogReportUtil.get(mContext).sendAdsLog(mUUID, mSession.getBoiteId(), mSession.getRoomId(),
                        String.valueOf(System.currentTimeMillis()), "start", mType, mMediaId,
                        GlobalValues.CURRENT_PROJECT_DEVICE_ID, mSession.getVersionName(), mSession.getAdsPeriod(), mSession.getBirthdayOndemandPeriod(),
                        mInnerType);
            }
        }

    }

    //倒计时线程
    private Runnable mShowMiniProgramQrCodeRunnable = ()->showMiniProgramQrCode();

    private void showMiniProgramQrCode(){

        if (mSession.getQrcodeType()==2){
            if (AppUtils.isNetworkAvailable(mContext) && mSession.isHeartbeatMiniNetty()) {
                ((SavorApplication) getApplication()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_QRCODE_OFFICIAL_TYPE);
                LogUtils.v("MiniProgramNettyService showMiniProgramQrCodeWindow");
            } else {
                ((SavorApplication) getApplication()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    //倒计时线程
    private Runnable mCountDownRunnable = ()->interactionCountDown();

    private void interactionCountDown(){
        try{
            if (!TextUtils.isEmpty(delayTime)){
                delayTime = (Integer.valueOf(delayTime)-1)+"";
                projectCountdowTipTV.setText(delayTime);
                if (0!=Integer.valueOf(delayTime)){
                    mHandler.postDelayed(mCountDownRunnable,1000);
                }else{
                    projectCountdowTipTV.setVisibility(View.GONE);

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 设置新投放源
     *
     * @param bundle
     */
    public void setNewProjection(Bundle bundle) {
        isNewProjection = false;
        handleNewProjection(bundle);
    }

    private void mappingLogType() {
        // 每次点播都生成新的UUID，投屏生成子UUID当做mediaId作为后台数据统计标识
        if (TextUtils.isEmpty(mUUID) || ConstantValues.PROJECT_TYPE_VIDEO_VOD.equals(mProjectType)) {
            mUUID = String.valueOf(System.currentTimeMillis());
        }
        if (ConstantValues.PROJECT_TYPE_PICTURE.equals(mProjectType)) {
            mType = "projection";
            switch (mImageType) {
                case 1:
                    mInnerType = "pic";
                    // 只有单张投屏才生成mediaId，幻灯片和文件投屏会由手机端传来一个id标识同一系列图片
                    if (mIsThumbnail) {
                        mMediaId = String.valueOf(System.currentTimeMillis());
                    }
                    break;
                case 2:
                    mInnerType = "file";
                    break;
                case 3:
                    mInnerType = "ppt";
                    break;
            }
        } else if (ConstantValues.PROJECT_TYPE_VIDEO.equals(mProjectType)) {
            mType = "projection";
            mInnerType = "video";
            mMediaId = String.valueOf(System.currentTimeMillis());
        } else if (ConstantValues.PROJECT_TYPE_VIDEO_VOD.equals(mProjectType)) {
            if (mIsFromWeb) {
                mType = "h5vod";
            } else {
                mType = "vod";
            }
            mInnerType = "video";
        }
    }


    private void handleNewProjection(Bundle bundle){
        String lastMediaId = mMediaId;
        String lastProjectType = mProjectType;
        handleBundleData(bundle);

        if (!mProjectType.startsWith("rstr_")) {
            // 新的投屏来时，给上一次投屏记一次end（由于投图片存在大小图的问题，这里作区分只有小图来时才记end）
            if (!ConstantValues.PROJECT_TYPE_PICTURE.equals(mProjectType) || mIsThumbnail) {
                LogReportUtil.get(mContext).sendAdsLog(mUUID, mSession.getBoiteId(), mSession.getRoomId(),
                        String.valueOf(System.currentTimeMillis()), "end", mType, lastMediaId,
                        GlobalValues.CURRENT_PROJECT_DEVICE_ID, mSession.getVersionName(), mSession.getAdsPeriod(),
                        mSession.getBirthdayOndemandPeriod(), mInnerType);
            }
        }

        if (mIsNewDevice) {
//            projectTipAnimateIn();
            mUUID = null;
        }

        // 如果上一次是点播，把UUID清空以便后面生成新的
        if (ConstantValues.PROJECT_TYPE_VIDEO_VOD.equals(lastProjectType)) {
            mUUID = null;
        }

        mappingLogType();

        mHandler.post(()->handleProjectRequest());
    }

    /**
     * 更改进度
     *
     * @param position
     * @return
     */
    public SeekResponseVo seekTo(int position) {
        SeekResponseVo responseVo = new SeekResponseVo();
        if (!ConstantValues.PROJECT_TYPE_VIDEO_VOD.equals(mProjectType) &&
                !ConstantValues.PROJECT_TYPE_VIDEO.equals(mProjectType)) {
            responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            responseVo.setMsg("失败");
        } else {
            if (mSavorVideoView.isInPlaybackState()) {
                mSavorVideoView.seekTo(position * 1000);
                responseVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                responseVo.setMsg("成功");
            } else {
                responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                responseVo.setMsg("失败");
            }
        }
        return responseVo;
    }

    /**
     * 播放、暂停
     *
     * @param action
     * @return
     */
    public PlayResponseVo togglePlay(int action) {
        PlayResponseVo responseVo = new PlayResponseVo();
        responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
        responseVo.setMsg("操作失败");
        if (!ConstantValues.PROJECT_TYPE_VIDEO_VOD.equals(mProjectType) &&
                !ConstantValues.PROJECT_TYPE_VIDEO.equals(mProjectType)) {
            responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            responseVo.setMsg("失败");
        } else {

        }
        return responseVo;
    }

    private boolean mIsBeenStopped;

    public boolean isBeenStopped() {
        return mIsBeenStopped;
    }

    /**
     * 停止投屏
     *
     * @return
     */
    public void stop(boolean resetFlag, StopAction stopAction) {
        LogUtils.e("StopResponseVo will exitProjection " + this.hashCode());
        mStopAction = stopAction;
        mIsBeenStopped = true;
        GlobalValues.mpprojection = null;
        GlobalValues.WELCOME_ID = 0;
        mHandler.post(()->exitProjection());

        if (resetFlag) {
            resetGlobalFlag();
        }
    }

    /**
     * 旋转投屏图片
     *
     * @param rotateDegree
     * @return
     */
    public RotateResponseVo rotate(int rotateDegree) {
        RotateResponseVo responseVo = new RotateResponseVo();
        if (ConstantValues.PROJECT_TYPE_PICTURE.equals(mProjectType)
                ||ConstantValues.PROJECT_TYPE_REST_PICTURE.equals(mProjectType)) {
            mImageRotationDegree = (mImageRotationDegree + rotateDegree) % 360;

            mHandler.post(mRotateImageRunnable);

            rescheduleToExit(true);

            responseVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
            responseVo.setMsg("成功");
            responseVo.setRotateValue(rotateDegree);
        } else {
            responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            responseVo.setMsg("失败");
        }
        return responseVo;
    }

    /**
     * 查询播放进度
     *
     * @return
     */
    public Object query() {
        QueryPosBySessionIdResponseVo queryResponse = new QueryPosBySessionIdResponseVo();
        if (!ConstantValues.PROJECT_TYPE_VIDEO_VOD.equals(mProjectType) &&
                !ConstantValues.PROJECT_TYPE_VIDEO.equals(mProjectType)) {
            queryResponse.setResult(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
        } else {
            queryResponse.setResult(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
            int mCurrPos = mSavorVideoView.getCurrentPosition();

            queryResponse.setPos(mCurrPos);
        }

        return queryResponse;
    }

    public VolumeResponseVo volume(int action) {
        VolumeResponseVo responseVo = new VolumeResponseVo();
        switch (action) {
            case 1:
                // 静音
                setVolume(0);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showMuteView();
                        showVolume(0);
                    }
                });
                break;
            case 2:
                // 取消静音
                setVolume(mCurrentVolume);

                mHandler.removeCallbacks(mHideMuteViewRunnable);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMuteIv.setVisibility(View.GONE);
                        showVolume(mCurrentVolume);
                    }
                });
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
                if (mCurrentVolume > 200) {
                    mCurrentVolume = 200;
                }
                setVolume(mCurrentVolume);

                mHandler.post(()->showVolume(mCurrentVolume));
                break;
        }
        responseVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        responseVo.setVol(mCurrentVolume);
        return responseVo;
    }


    private void showMuteView() {
        mMuteIv.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(mHideMuteViewRunnable);
        mHandler.postDelayed(mHideMuteViewRunnable, 1000 * 3);
    }

    private void showVolume(int currentVolume) {
        mVolumePb.setProgress(currentVolume);
        mVolumeTv.setText(currentVolume + "");
        mVolumeRl.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(mHideVolumeViewRunnable);
        mHandler.postDelayed(mHideVolumeViewRunnable, 1000 * 5);
    }

    /**
     * 重置定期退出页面计划
     *
     * @param scheduleNewOne 是否重置
     */
    private void rescheduleToExit(boolean scheduleNewOne) {
        LogUtils.e("rescheduleToExit scheduleNewOne=" + scheduleNewOne + " " + this.hashCode());

        mHandler.removeCallbacks(mExitProjectionRunnable);

        if (scheduleNewOne) {
            int duration = PROJECT_DURATION;
            if (1==mImageType||5==mImageType||6==mImageType){
                if (!TextUtils.isEmpty(delayTime)){
                    duration = Integer.valueOf(delayTime)*1000;
                }
            }else if (2 == mImageType) {
                duration = PROJECT_DURATION_FILE;
            }else if (4==mImageType||9==mImageType||ConstantValues.PROJECT_TYPE_VIDEO_REST.equals(mProjectType)){
                if (projectionTime!=0){
                    duration = projectionTime;
                }else{
                    duration = REST_PROJECT_DURATION;
                }
            }else if (7==mImageType){
                if (projectionTime!=0){
                    duration = projectionTime*1000;
                }else{
                    duration = WELCOME_PROJECT_DURATION;
                }
            }else if (8==mImageType){
                if (projectionTime!=0){
                    duration = projectionTime*1000;
                }else{
                    duration = WELCOME_EVALUATE_DURATION;
                }
            }

            mHandler.postDelayed(mExitProjectionRunnable, duration);
        }
    }



    private void handleProjectionEndResult(){
        downloadLog(true);
        LogUtils.d(TAG+"handleProjectionEndResult " + ScreenProjectionActivity.this.hashCode());
        if (miniProgramNettyService!=null&&from_service == GlobalValues.FROM_SERVICE_MINIPROGRAM){
            LogUtils.d("handleImgAndVideo=000>>>currentAction="+currentAction+"&mForscreenId="+mForscreenId);
            miniProgramNettyService.startProjection(currentAction,mForscreenId);

        }else if (remoteJettyService!=null&&from_service==GlobalValues.FROM_SERVICE_REMOTE){
            if (remoteJettyService.controllHandler!=null){
                remoteJettyService.controllHandler.startRemoteProjecion(currentAction,mForscreenId);
            }
        }

        mMediaPath = null;
        mMediaUrl = null;
        AppApi.notifyStop(mContext, apiRequestListener, 2, "");
        resetGlobalFlag();
        exitProjection();

    }
    /**
     * 重置全局变量
     */
    private void resetGlobalFlag() {
        /**如果销售端投图片，是通过固定时间来显示轮播图片的，时间到了以后，需要终止轮播*/
        if (4==mImageType||9==mImageType){
            GlobalValues.PROJECT_IMAGES.clear();
        }
        GlobalValues.LAST_PROJECT_DEVICE_ID = GlobalValues.CURRENT_PROJECT_DEVICE_ID;
        GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        GlobalValues.CURRENT_PROJECT_DEVICE_ID = null;
        GlobalValues.CURRENT_PROJECT_DEVICE_IP = null;
        GlobalValues.CURRENT_PROJECT_DEVICE_NAME = null;
        GlobalValues.CURRENT_PROJECT_IMAGE_ID = null;
        GlobalValues.CURRENT_PROJECT_ID = null;
    }
    private void exitProjection() {
        LogUtils.e(TAG+"will exitProjection " + this.hashCode());
        mSavorVideoView.setLooping(false);
        if (mMusicPlayer!=null){
            mMusicPlayer.setLooping(false);
        }

        mIsBeenStopped = true;
        finish();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MiniProgramNettyService.projectionIdMap.clear();
            }
        },1000*60*10);
//        MiniProgramNettyService.projectionIdMap.clear();
        LogUtils.w("finish done " + this.hashCode());
    }

    /**
     * 旋转图片
     */
    private void rotatePicture() {
        mImageView.setRotation(mImageRotationDegree);
        if (mImageView.getDrawable() != null) {
            if (mImageRotationDegree == 90 || mImageRotationDegree == 270) {
                int viewWidth = mImageView.getDrawable().getIntrinsicWidth();// mImageView.getWidth();
                int viewHeight = mImageView.getDrawable().getIntrinsicHeight();//mImageView.getHeight();
                mImageView.setScaleX(viewHeight / (float) viewWidth);
                mImageView.setScaleY(viewHeight / (float) viewWidth);
            } else {
                mImageView.setScaleX(1);
                mImageView.setScaleY(1);
            }
        }
    }

    private void welcomeRotatePicture() {
        welcomeView.setRotation(mImageRotationDegree);
        if (welcomeView.getDrawable() != null) {
            if (mImageRotationDegree == 90 || mImageRotationDegree == 270) {
                int viewWidth = welcomeView.getDrawable().getIntrinsicWidth();// welcomeView.getWidth();
                int viewHeight = welcomeView.getDrawable().getIntrinsicHeight();//welcomeView.getHeight();
                welcomeView.setScaleX(viewHeight / (float) viewWidth);
                welcomeView.setScaleY(viewHeight / (float) viewWidth);
            } else {
                welcomeView.setScaleX(1);
                welcomeView.setScaleY(1);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AppApi.notifyStop(this, apiRequestListener, 2, "");
            resetGlobalFlag();
            exitProjection();
            handled = true;

        } else if (keyCode == KeyCode.KEY_CODE_PLAY_PAUSE) {
            handled = true;
        }
        return handled || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSavorVideoView.onStop();
    }

    @Override
    protected void onDestroy() {
        LogUtils.e(TAG+"onDestroy " + this.hashCode());
        // 清空消息队列
        mHandler.removeCallbacksAndMessages(null);

        // 记录业务日志
        if (!mProjectType.startsWith("rstr_")) {
            LogReportUtil.get(mContext).sendAdsLog(mUUID, mSession.getBoiteId(), mSession.getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "end", mType, mMediaId, GlobalValues.LAST_PROJECT_DEVICE_ID,
                    mSession.getVersionName(), mSession.getAdsPeriod(), mSession.getBirthdayOndemandPeriod(), mInnerType);
        }
        // 释放资源
        mSavorVideoView.release();
        if (mMusicPlayer!=null&&mMusicPlayer.isPlaying()){
            mMusicPlayer.stop();
            mMusicPlayer.release();
        }
        if (mNettyConnection!=null){
            unbindService(mNettyConnection);
        }
        if (mJettyConnection!=null){
            unbindService(mJettyConnection);
        }
        if (mStopAction != null) {
            mStopAction.onActionEnd();
        }
        GlobalValues.INTERACTION_ADS_PLAY = 0;
        super.onDestroy();
    }

    private ProjectVideoView.PlayStateCallback mPlayStateCallback = new ProjectVideoView.PlayStateCallback() {
        @Override
        public boolean onMediaComplete(int index, boolean isLast) {
            LogUtils.w(TAG+"activity onMediaComplete ,hashCode=" + this.hashCode());
            LogUtils.w(TAG+"activity onMediaComplete ,isLash=" + isLast);
            if (isLast){
                if (ConstantValues.PROJECT_TYPE_VIDEO.equals(mProjectType)){
                    handleProjectionEndResult();
                }else if (ConstantValues.PROJECT_TYPE_VIDEO_VOD.equals(mProjectType)){
                    handleProjectionEndResult();
                }else if (ConstantValues.PROJECT_TYPE_VIDEO_REST.equals(mProjectType)&&projectionTime==0){
                    handleProjectionEndResult();
                }else if (ConstantValues.PROJECT_TYPE_VIDEO_BIRTHDAY.equals(mProjectType)){
                    if (videoBirthdayCount<2){
                        videoBirthdayCount++;
                    }else{
                        handleProjectionEndResult();
                    }
                }
            }

            return false;
        }

        @Override
        public boolean onMediaError(int index, boolean isLast) {
            LogUtils.w("activity onMediaError " + this.hashCode());
            downloadLog(false);
            if (!AppUtils.isSVT()){
                ShowMessage.showToast(mContext, "视频播放失败");
            }
            LogFileUtil.write("视频播放失败:" + mMediaPath);
            if (isLast) {
                AppApi.notifyStop(mContext, apiRequestListener, 2, "");
                resetGlobalFlag();
                exitProjection();
            }
            return false;
        }

        @Override
        public boolean onMediaPrepared(int index) {
            initVolume();
            if (projectionTime==0){
                mHandler.removeCallbacks(mExitProjectionRunnable);
            }
            return false;
        }

        @Override
        public void onMediaPause(int index) {
        }

        @Override
        public void onMediaResume(int index) {
        }

        @Override
        public void onMediaBufferPercent(){
            MiniProgramProjection mpp = MiniProgramNettyService.miniProgramProjection;
            if (mpp!=null){
                HashMap<String,Object> params = new HashMap<>();
                params.put("req_id",mpp.getReq_id());
                params.put("forscreen_id",mpp.getForscreen_id());
                params.put("resource_id",mpp.getVideo_id());
                params.put("box_mac",mSession.getEthernetMac());
                params.put("openid",mpp.getOpenid());
                params.put("is_download",1);
                postProjectionResourceLog(params);
            }
            if (from_service==GlobalValues.FROM_SERVICE_MINIPROGRAM){
                String mUuid = String.valueOf(System.currentTimeMillis());
                String useTime = String.valueOf(System.currentTimeMillis()-startTime);
                String resourceSize=null;
                if (!TextUtils.isEmpty(mMediaUrl)){
                    String [] names = mMediaUrl.split("/");
                    if (names!=null&&names.length>0){
                        String name = names[names.length-1];
                        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                        File file = new File(basePath+name);
                        if (file.exists()){
                            resourceSize = String.valueOf(file.length());
                        }
                    }
                }
                LogReportUtil.get(mContext).downloadLog(mUuid, LogParamValues.download,LogParamValues.standard_duration,useTime);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LogReportUtil.get(mContext).downloadLog(mUuid, LogParamValues.download,LogParamValues.standard_size,resourceSize);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mpp!=null&&!TextUtils.isEmpty(mpp.getSerial_number())){
                    LogReportUtil.get(mContext).downloadLog(mUuid, LogParamValues.download,LogParamValues.standard_serial,mpp.getSerial_number());
                }
            }
        }
    };

    private void showImgLog(){
        MiniProgramProjection mpp = MiniProgramNettyService.miniProgramProjection;
        if (mpp!=null){
            HashMap<String,Object> params = new HashMap<>();
            params.put("req_id",mpp.getReq_id());
            params.put("forscreen_id",mpp.getForscreen_id());
            params.put("resource_id",mpp.getVideo_id());
            params.put("box_mac",mSession.getEthernetMac());
            params.put("openid",mpp.getOpenid());
            params.put("is_play",1);
            postProjectionResourceLog(params);
        }
    }

    private void downloadLog(boolean success){
        String mUuid = String.valueOf(System.currentTimeMillis());
        if (from_service==GlobalValues.FROM_SERVICE_MINIPROGRAM){
            if (success){
                LogReportUtil.get(mContext).downloadLog(mUuid, LogParamValues.launch,LogParamValues.standard_success);
            }else{
                LogReportUtil.get(mContext).downloadLog(mUuid, LogParamValues.launch,LogParamValues.standard_fail);
            }
        }else{
            if (success){
                LogReportUtil.get(mContext).downloadLog(mUuid, LogParamValues.launch,LogParamValues.speed_success);
            }else{
                LogReportUtil.get(mContext).downloadLog(mUuid, LogParamValues.launch,LogParamValues.speed_fail);
            }
        }
    }

    /**
     * 小程序投屏日志统计接口，不在区分资源类型
     * @param params
     */
    private void postProjectionResourceLog(HashMap<String,Object> params) {
        AppApi.postProjectionResourceParam(mContext, apiRequestListener, params);
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
}

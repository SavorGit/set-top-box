package com.savor.ads.activity;

import static com.savor.ads.utils.ConstantValues.MINI_PROGRAM_QRCODE_SMALL_TYPE;
import static com.savor.ads.utils.ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.jar.savor.box.vo.VolumeResponseVo;
import com.savor.ads.R;
import com.savor.ads.SavorApplication;
import com.savor.ads.adapter.StringPagerAdapter;
import com.savor.ads.bean.MediaFileBean;
import com.savor.ads.bean.MeetingLoopPlayBean;
import com.savor.ads.bean.MeetingResourceBean;
import com.savor.ads.bean.MeetingWelcomeBean;
import com.savor.ads.bean.ProjectionImg;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.customview.ProjectVideoView;
import com.savor.ads.database.DBHelper;
import com.savor.ads.log.LogParamValues;
import com.savor.ads.log.LogReportUtil;
import com.savor.ads.projection.action.StopAction;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import pl.droidsonroids.gif.GifImageView;

public class LoopPlayActivity extends BaseActivity{
    private static final String TAG = "LoopPlayActivity";
    private ProjectVideoView mSavorVideoView;
    private RelativeLayout mImageArea;
    private TextView mWelcomeWordsTV;
    private MediaPlayer mMusicPlayer;
    private GifImageView imageGifView;
    private RelativeLayout mVolumeRl;
    private TextView mVolumeTv;
    private ProgressBar mVolumePb;
    private LinearLayout proQrcodeLayout;
    private ImageView proQrcodeIV;
    private ImageView aloneImageIV;
    private ViewPager imageViewPager;

    private boolean mHasInitializedVolume;
    private int currentFileIndex;
    private int mCurrentVolume = 0;
    /**来自于哪个service的投屏请求，1： 标准版小程序，2：jetty服务(目前为极简版)*/
    private int from_service;
    private MeetingLoopPlayBean loopPlayBean;
    private List<String> listMediaPaths;
    private MeetingWelcomeBean meetingWelcomeBean;
    private ArrayList<Object> resourcelist;
    private Set linkedHashSet;
    private String mMusicPath;
    private String mFontPath;
    private DBHelper dbHelper;
    private StringPagerAdapter imageAdapter;
    ArrayList<String> mDataImg = new ArrayList<>();
    private int mCurrentIndex;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String time = meetingWelcomeBean.getFinish_time();
            if (isExpireResource(time)){
                stop();
            }else{
                mCurrentIndex = (mCurrentIndex + 1) % mDataImg.size();
                imageViewPager.setCurrentItem(mCurrentIndex, true);
                mHandler.sendEmptyMessageDelayed(0, 10 * 1000);
            }

            return true;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("onCreate " + this.hashCode());
        setContentView(R.layout.activity_loop_play);
        dbHelper = DBHelper.get(mContext);
        findView();
        setView();
        handleIntent();
    }

    private void findView() {
        mSavorVideoView = findViewById(R.id.video_view);
        mImageArea = findViewById(R.id.rl_image);
        imageGifView = findViewById(R.id.image_gif_view);
        aloneImageIV = findViewById(R.id.alone_view);
        imageViewPager = findViewById(R.id.vp_images);
        mVolumeRl = findViewById(R.id.rl_volume_view);
        mVolumeTv = findViewById(R.id.tv_volume);
        mVolumePb =  findViewById(R.id.pb_volume);
        mWelcomeWordsTV = findViewById(R.id.welcome_words);
        proQrcodeLayout = findViewById(R.id.projection_qrcode_layout);
        proQrcodeIV = findViewById(R.id.projection_qrcode);
    }

    private void setView() {
        mSavorVideoView.setIfShowPauseBtn(true);
        mSavorVideoView.setIfShowLoading(true);
        mSavorVideoView.setLooping(false);
        mSavorVideoView.setIfHandlePrepareTimeout(true);
        mSavorVideoView.setPlayStateCallback(mPlayStateCallback);
        imageAdapter = new StringPagerAdapter(mContext, mDataImg);
        imageViewPager.setAdapter(imageAdapter);
    }

    private void initTypeface(String forscreen_char,String wordsize,String color){
        if (!TextUtils.isEmpty(mFontPath)){
            Typeface typeface = Typeface.createFromFile(mFontPath);
            mWelcomeWordsTV.setTypeface(typeface);
        }else{
            mWelcomeWordsTV.setTypeface(null);
        }
        mWelcomeWordsTV.setText(forscreen_char);
        if (!TextUtils.isEmpty(color)){
            mWelcomeWordsTV.setTextColor(Color.parseColor(color));
        }
        if (!TextUtils.isEmpty(wordsize)){
            mWelcomeWordsTV.setTextSize(Float.parseFloat(wordsize));
        }
        mWelcomeWordsTV.setVisibility(View.VISIBLE);
    }

    private void initSounds(){
        try{
            if (!TextUtils.isEmpty(mMusicPath)){
                if (mMusicPlayer==null){
                    mMusicPlayer = new MediaPlayer();
                }
                if (!mMusicPlayer.isPlaying()){
                    mMusicPlayer.setDataSource(mMusicPath);
                    mMusicPlayer.prepare();
                    mMusicPlayer.start();
                    mMusicPlayer.setLooping(true);
                }
            }
            initVolume();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void initVolume() {
        if (!mHasInitializedVolume) {
            if (AppUtils.isSVT()||AppUtils.isPhilips()) {
                mCurrentVolume = mSession.getTvVideoFroscreenVolume();
            } else {
                mCurrentVolume = mSession.getBoxVideoFroscreenVolume();
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

            handleProjectRequest();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private void handleBundleData(Bundle bundle) {
        loopPlayBean = (MeetingLoopPlayBean) bundle.getSerializable("loopBean");
        if (loopPlayBean!=null){
            listMediaPaths = loopPlayBean.getVideoPaths();
            meetingWelcomeBean = loopPlayBean.getWelcome();
        }
    }

    /**
     * 处理投屏
     * 根据不同的投屏类型
     */
    private void handleProjectRequest() {

        ((SavorApplication) getApplication()).hideMiniProgramQrCodeWindow();
        ((SavorApplication) getApplication()).hideGoodsCountdownQrCodeWindow();
        ((SavorApplication) getApplication()).hideGoodsQrCodeWindow();
        mSavorVideoView.setLooping(true);
        mWelcomeWordsTV.setVisibility(View.GONE);
        resourcelist = new ArrayList<>();
        linkedHashSet = new LinkedHashSet();
        mDataImg = new ArrayList<>();
        if (listMediaPaths!=null&&listMediaPaths.size()>0){
            for (String mediaPath:listMediaPaths){
                MediaFileBean bean = new MediaFileBean();
                File file = new File(mediaPath);
                bean.setFilename(file.getName());
                bean.setCacheFile(file);
                resourcelist.add(bean);
            }
        }
        if (meetingWelcomeBean!=null&&meetingWelcomeBean.getImg_list()!=null){
            resourcelist.add(meetingWelcomeBean);
        }
        if (resourcelist.size()==1&&meetingWelcomeBean!=null){
            for (ProjectionImg img: meetingWelcomeBean.getImg_list()){
                if (!TextUtils.isEmpty(img.getFilePath())){
                    mDataImg.add(img.getFilePath());
                }
            }
            if (mDataImg.size()>0){
                if (mDataImg.size()==1){
                    aloneImageIV.setVisibility(View.VISIBLE);
                    imageViewPager.setVisibility(View.GONE);
                    GlideImageLoader.loadImageWithoutCache(mContext, mDataImg.get(0), aloneImageIV, 0, 0);
                    mHandler.post(()->imgShowCountDown());
                }else{
                    aloneImageIV.setVisibility(View.GONE);
                    imageViewPager.setVisibility(View.VISIBLE);
                    imageAdapter.setDataSource(mDataImg);
                    mHandler.sendEmptyMessageDelayed(0,10*1000);
                }
                handleMeetingWelcome();
            }else{
                //没有播放视频资源，只有欢迎词并且欢迎词没下载成功，只能退出
                stop();
            }
        }else{
            mSavorVideoView.setMediaFiles(resourcelist);
            showQrCodeWhenProjection();
        }

    }

    //倒计时线程
    private Runnable mCountDownRunnable = () -> imgShowCountDown();

    private void imgShowCountDown(){
        String time = meetingWelcomeBean.getFinish_time();
        if (isExpireResource(time)){
            stop();
        }else{
            mHandler.postDelayed(mCountDownRunnable,10*1000);
        }

    }

    private void handleMeetingWelcome(){
        mMusicPath = meetingWelcomeBean.getMusicPath();
        initSounds();
        String forscreen_char = meetingWelcomeBean.getForscreen_char();
        if (!TextUtils.isEmpty(forscreen_char)){
            mFontPath = meetingWelcomeBean.getFontPath();
            String wordsize = meetingWelcomeBean.getWordsize();
            String color = meetingWelcomeBean.getColor();
            initTypeface(forscreen_char,wordsize,color);
        }
    }

    private void stopMusicPlayer(){
        if (mMusicPlayer!=null&&mMusicPlayer.isPlaying()){
            mMusicPlayer.setLooping(false);
            mMusicPlayer.stop();
            mMusicPlayer.release();
            mMusicPlayer = null;
        }
    }

    private void showQrCodeWhenProjection(){
        ((SavorApplication) getApplication()).hideMiniProgramQrCodeWindow();
        String box_mac = Session.get(this).getEthernetMac();
        String path = null;
        String url = null;
        proQrcodeLayout.setVisibility(View.VISIBLE);
        if (mSession.isShowMiniProgramIcon()&& mSession.isShowSimpleMiniProgramIcon()){
            if (mSession.isHeartbeatMiniNetty()) {
                path = AppUtils.getFilePath( AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_NAME;
                url = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+MINI_PROGRAM_QRCODE_SMALL_TYPE;
            }else{
                path = AppUtils.getFilePath( AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_NAME;
                url = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+MINI_PROGRAM_SQRCODE_SMALL_TYPE;
            }
        }else if (!mSession.isShowMiniProgramIcon()&& mSession.isShowSimpleMiniProgramIcon()){
            path = AppUtils.getFilePath( AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_SQRCODE_NAME;
            url = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+MINI_PROGRAM_SQRCODE_SMALL_TYPE;
        }else if (mSession.isShowMiniProgramIcon()&& !mSession.isShowSimpleMiniProgramIcon()){
            if (mSession.isHeartbeatMiniNetty()) {
                path = AppUtils.getFilePath( AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_NAME;
                url = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ box_mac+"&type="+MINI_PROGRAM_QRCODE_SMALL_TYPE;
            }
        }
        if (!TextUtils.isEmpty(path)&&!TextUtils.isEmpty(url)){
            ViewGroup.LayoutParams layoutParams = proQrcodeLayout.getLayoutParams();
            layoutParams.width =DensityUtil.dip2px(mContext, 108);
            layoutParams.height =DensityUtil.dip2px(mContext, 108*1.2f);
            proQrcodeLayout.setLayoutParams(layoutParams);
            File file = new File(path);
            if (file.exists()){
                GlideImageLoader.loadLocalImage(mContext,file,proQrcodeIV);
            }else{
                GlideImageLoader.loadImageWithoutCache(mContext,path,proQrcodeIV,0,0);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
    public void stop() {
        LogUtils.e("StopResponseVo will exitProjection " + this.hashCode());
        mIsBeenStopped = true;
        showProjectionPlayState(1);
        mHandler.post(()->exitProjection());
    }

    public VolumeResponseVo volume(int action) {
        VolumeResponseVo responseVo = new VolumeResponseVo();
        switch (action) {
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

    private void handleProjectionEndResult(){
        downloadLog(true);
        showProjectionPlayState(1);
        AppApi.notifyStop(mContext, apiRequestListener, 2, "");
        exitProjection();

    }

    private void exitProjection() {
        LogUtils.e(TAG+"will exitProjection " + this.hashCode());
        mSavorVideoView.setLooping(false);
        if (mMusicPlayer!=null){
            mMusicPlayer.setLooping(false);
        }

        mIsBeenStopped = true;
        finish();
        LogUtils.w("finish done " + this.hashCode());
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AppApi.notifyStop(this, apiRequestListener, 2, "");
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
        // 释放资源
        mSavorVideoView.release();
        if (mMusicPlayer!=null&&mMusicPlayer.isPlaying()){
            mMusicPlayer.stop();
            mMusicPlayer.release();
        }
        super.onDestroy();
    }

    private ProjectVideoView.PlayStateCallback mPlayStateCallback = new ProjectVideoView.PlayStateCallback() {
        @Override
        public boolean onMediaComplete(int index, boolean isLast) {
            LogUtils.w(TAG+"activity onMediaComplete ,hashCode=" + this.hashCode());
            LogUtils.w(TAG+"activity onMediaComplete ,isLash=" + isLast);
//            if (isLast){
//                handleProjectionEndResult();
//            }

            return false;
        }

        @Override
        public boolean onMediaError(int index, boolean isLast) {
            LogUtils.w("activity onMediaError " + this.hashCode());
            downloadLog(false);
            showProjectionPlayState(1);
            if (!AppUtils.isSVT()){
                ShowMessage.showToast(mContext, "视频播放失败");
            }
            LogFileUtil.write("视频播放失败:" );
            if (isLast) {
                AppApi.notifyStop(mContext, apiRequestListener, 2, "");
                exitProjection();
            }
            return false;
        }

        @Override
        public boolean onMediaPrepared(int index) {
            if (resourcelist!=null){
                Object objResource = resourcelist.get(index);
                if (objResource instanceof MeetingWelcomeBean&&meetingWelcomeBean!=null){
                    String time = meetingWelcomeBean.getFinish_time();
                    if (isExpireResource(time)){
                        if (!allPlaybackFinish(index)){
                            mSavorVideoView.playNext();
                            return true;
                        }
                    }
                    handleMeetingWelcome();
                }else{
                    stopMusicPlayer();
                    mWelcomeWordsTV.setVisibility(View.GONE);
                    MediaFileBean fileBean = (MediaFileBean) objResource;
                    String fileName = fileBean.getFilename();
                    String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
                    String[] selectionArgs = new String[]{String.valueOf(fileName)};
                    List<MeetingResourceBean> resourceBeans = dbHelper.findMeetingResourceList(selection,selectionArgs);
                    if (resourceBeans!=null){
                        MeetingResourceBean bean = resourceBeans.get(0);
                        if (isExpireResource(bean.getEnd_date())){
                            if (!allPlaybackFinish(index)){
                                mSavorVideoView.playNext();
                                return true;
                            }
                        }
                    }else{
                        if (!allPlaybackFinish(index)){
                            mSavorVideoView.playNext();
                            return true;
                        }
                    }

                }
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
        }
    };

    /**
     * 查看当前播放的资源是否已经过期
     * @param time
     * @return
     */
    private boolean isExpireResource(String time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date endDate = null;
        try {
            endDate = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date now = new Date();
        if (endDate != null && endDate.before(now)) {
            Log.d(TAG,"onMediaPrepared-isExpire");
            return true;
        }else{
            return false;
        }

    }

    /**
     * 查看当前临时播放列表是否已经播放完毕
     * 播放完毕就退出播放
     * @param index
     */
    private boolean allPlaybackFinish(int index){
        linkedHashSet.add(index);
        if (linkedHashSet.size()==resourcelist.size()){
            handleProjectionEndResult();
            return true;
        }
        return false;
    }

    /**
     * @param playState 0:开始，1:结束
     */
    private void showProjectionPlayState(int playState){

//        HashMap<String,Object> params = new HashMap<>();
//        params.put("req_id",proProjection.getReq_id());
//        params.put("forscreen_id",proProjection.getForscreen_id());
//        params.put("resource_id",proProjection.getFilename());
//        params.put("box_mac",mSession.getEthernetMac());
//        params.put("openid",proProjection.getOpenid());
//        String time = System.currentTimeMillis()+"";
//        if (playState==0){
//            params.put("box_playstime",time);
//        }else{
//            params.put("box_playetime",time);
//        }
//        if (proProjection.getType()==GlobalValues.FROM_SERVICE_MINIPROGRAM){
//            postProjectionResourceLog(params);
//        }else{
//            postSimpleProjectionResourceLog(params);
//        }
    }

    /**
     * 小程序投屏日志统计接口，不在区分资源类型
     * @param params
     */
    private void postProjectionResourceLog(HashMap<String,Object> params) {
        AppApi.postProjectionResourceParam(mContext, apiRequestListener, params);
    }

    private void postSimpleProjectionResourceLog(HashMap<String,Object> params){
        AppApi.postSimpleProjectionResourceParam(mContext, apiRequestListener, params);
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

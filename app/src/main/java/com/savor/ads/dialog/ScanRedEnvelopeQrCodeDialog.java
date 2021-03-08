package com.savor.ads.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mstar.tv.service.skin.AudioSkin;
import com.savor.ads.R;
import com.savor.ads.SavorApplication;
import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.bean.ProjectionGuideImg;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.core.Session;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.MiniProgramQrCodeWindowManager;
import com.savor.ads.utils.QrCodeWindowManager;
import com.savor.ads.utils.ShowMessage;

import java.io.File;

import static com.savor.ads.utils.GlobalValues.FROM_SERVICE_MINIPROGRAM;

/**
 * Created by zhanghq on 2016/12/10.
 */

public class ScanRedEnvelopeQrCodeDialog extends Dialog{

    private Handler mHandler = new Handler();
    private Context mContext;
    private ImageView scanAvatarIconIv;
    private TextView scanNickNameTv;
    private ImageView scanQrCodeIv;
    private TextView scanWxCountDownTv;
    private TextView redEnvelopeTipTv;
    WindowManager mWindowManager;
    private LinearLayout mFloatLayout;

    private boolean mIsAdded;
    private boolean mIsHandling;
    private int delayTime=60*1000;
    private int height =0;
    private int mBrokenSoundId;
    private SoundPool mSoundPool;
    private int soundNum;
    private AudioSkin mAudioSkin;
    public ScanRedEnvelopeQrCodeDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_scan_red_envelope_view);
        setDialogAttributes();
        initViews();
        loadSound();
    }

    private void setDialogAttributes() {
        height = DensityUtil.getScreenHeight(mContext)/4*3;
        Window window = getWindow(); // 得到对话框
//        window.getDecorView().setPadding(0, 20, 20, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = height+50;
        wl.height = height;
        wl.gravity = Gravity.CENTER;
        wl.format = PixelFormat.RGBA_8888;
        //设置window type
        wl.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wl.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        window.setDimAmount(0f);
        window.setAttributes(wl);
    }
    private void initViews(){
        scanAvatarIconIv = findViewById(R.id.iv_scan_wx_avatar_icon);
        scanNickNameTv = findViewById(R.id.tv_scan_wx_nickname);
        scanWxCountDownTv = findViewById(R.id.tv_scan_wx_countdown);
        scanQrCodeIv = findViewById(R.id.iv_scan_qrcode);
        redEnvelopeTipTv = findViewById(R.id.red_envelope_tip);
    }

    private void loadSound() {
        soundNum = 0;
        mSoundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 0);
        mBrokenSoundId = mSoundPool.load(mContext, R.raw.red, 1);
        if (AppUtils.isMstar()) {
            mAudioSkin = new AudioSkin(mContext);
            mAudioSkin.connect(null);
        }

    }

    public void setRedEnvelopeInfo(String wxAvatarUrl,String wxNickname,String redEnvelopeUrl,String content){
        if (!TextUtils.isEmpty(wxAvatarUrl)){
            GlideImageLoader.loadRoundImage(mContext,wxAvatarUrl,scanAvatarIconIv,R.mipmap.wxavatar);
        }
        if (!TextUtils.isEmpty(wxNickname)){
            scanNickNameTv.setText(wxNickname);
        }
        if (!TextUtils.isEmpty(content)){
            redEnvelopeTipTv.setText(content);
            redEnvelopeTipTv.setVisibility(View.VISIBLE);
        }else{
            redEnvelopeTipTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(redEnvelopeUrl)){
            GlideImageLoader.loadImageWithoutCache(mContext, redEnvelopeUrl, scanQrCodeIv, new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ShowMessage.showToast(mContext, "加载二维码失败");
                        }
                    });
                    dismiss();
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    mHandler.removeCallbacks(mExitRunnable);
                    mHandler.postDelayed(mExitRunnable,delayTime);
                    mHandler.removeCallbacks(mCountDownRunnable);
                    scanWxCountDownTv.setText(delayTime/1000+"");
                    mHandler.postDelayed(mCountDownRunnable,1000);
                    mHandler.post(redEnvelopeSoundRunnable);
                    ((SavorApplication) mContext.getApplicationContext()).hideMiniProgramQrCodeWindow();
                    GlobalValues.isOpenRedEnvelopeWin = true;
                    return false;
                }
            });
        }
    }
    //倒计时线程
    private Runnable mCountDownRunnable = () -> wxScanCountDown();

    private void wxScanCountDown(){
        delayTime = delayTime-1000;
        scanWxCountDownTv.setText(delayTime/1000+"");
        if (delayTime!=0){
            mHandler.postDelayed(mCountDownRunnable,1000);
        }
    }

    //退出线程
    private Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
            GlobalValues.isOpenRedEnvelopeWin = false;
//            if (!TextUtils.isEmpty(GlobalValues.currentVid)){
//                if (Session.get(mContext).isShowAnimQRcode()){
//                    MiniProgramQrCodeWindowManager.get(mContext).setCurrentPlayMediaId(GlobalValues.currentVid);
//                }else{
//                    QrCodeWindowManager.get(mContext).setCurrentPlayMediaId(GlobalValues.currentVid);
//                }
//                if (ConstantValues.QRCODE_CALL_VIDEO_ID.equals(GlobalValues.currentVid)){
//                    ((SavorApplication) mContext.getApplicationContext()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_QRCODE_BIG_TYPE);
//                }else {
//                    ((SavorApplication) mContext.getApplicationContext()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_QRCODE_OFFICIAL_TYPE);
//                }
//            }
            ProjectionGuideImg guideImg = Session.get(mContext).getGuideImg();
            if (guideImg!=null){
                String fileName = guideImg.getBonus_forscreen_filename();
                String filePath = AppUtils.getFilePath(AppUtils.StorageFile.cache)+fileName;
                if (new File(filePath).exists())
                ProjectOperationListener.getInstance(mContext).showImage(1,filePath,true,String.valueOf(45),null,null,-1, FROM_SERVICE_MINIPROGRAM);
            }
        }
    };


    private void playRedEnvelopeSound() {

        if (mSoundPool != null && mBrokenSoundId > 0) {
            mSoundPool.play(mBrokenSoundId, 1, 1, 5, 0, 1);
        }
        mHandler.postDelayed(redEnvelopeSoundRunnable,1000*3);
    }

    private Runnable redEnvelopeSoundRunnable = new Runnable() {
        @Override
        public void run() {
            soundNum ++;
            if (soundNum>=4){
                return;
            }
              playRedEnvelopeSound();
        }
    };


//    protected void setVolume(int volume) {
//        if (AppUtils.isMstar()) {
//            if (mAudioSkin != null) {
//                if (volume > 100)
//                    volume = 100;
//                else if (volume < 0)
//                    volume = 0;
//                mAudioSkin.setVolume(volume);
//            }
//        } else {
//            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//            if (audioManager != null) {
//                LogUtils.d("System volume:" + audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
//                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
//                if (volume > 100)
//                    volume = 100;
//                else if (volume < 0)
//                    volume = 0;
//                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume * maxVolume / 100, 0);
//            }
//        }
//    }



    @Override
    public void dismiss() {
        if (mSoundPool != null) {
            mSoundPool.release();
        }
        super.dismiss();
    }

    @Deprecated
    public void showSendRedEnvelopeWindow(final Context context, final String url) {
        LogUtils.d("showSendEnvelopeWindow");
        if (TextUtils.isEmpty(url)) {
            LogUtils.e("URL is empty, will not show code window!!");
            return;
        }


        mHandler.removeCallbacks(mHideRunnable);
        mHandler.postDelayed(mHideRunnable, delayTime);

        mIsHandling = true;

        final WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        //获取WindowManager
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.CENTER;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        int width = DensityUtil.getScreenWidth(context);
        int height = DensityUtil.getScreenHeight(context);
        wmParams.x = width/2;
        wmParams.y = height/2;
        int viewHeight = height/3*2;
        int viewWidth = AppUtils.getInt(viewHeight*1.2);
        //设置悬浮窗口长宽数据
        wmParams.width = viewHeight;
        wmParams.height = viewWidth;

        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_scan_red_envelope_view_bak, null);
        final ImageView avatarIconIv = mFloatLayout.findViewById(R.id.iv_wx_avatar_icon);
        final TextView nickNameTv = mFloatLayout.findViewById(R.id.tv_wx_nickname);

        final ImageView qrCodeIv = mFloatLayout.findViewById(R.id.iv_qrcode);

        LogUtils.v("ScanRedEnvelopeQrCodeDialog_bak 开始addView");
        LogFileUtil.write("ScanRedEnvelopeQrCodeDialog_bak 开始addView");
        if (Looper.myLooper() == Looper.getMainLooper()) {
            addToWindow(context, wmParams,url,qrCodeIv);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addToWindow(context, wmParams,url,qrCodeIv);
                }
            });
        }
    }
    @Deprecated
    private void addToWindow(final Context context,final WindowManager.LayoutParams wmParams, String url,ImageView qrCodeIv) {
        try {
            GlideImageLoader.loadImageWithoutCache(context, url, qrCodeIv, new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    mIsHandling = false;
                    mIsAdded = false;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ShowMessage.showToast(context, "加载二维码失败");
                        }
                    });
                    hideQrCode();
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    if (mFloatLayout.getParent() == null) {
                        mWindowManager.addView(mFloatLayout, wmParams);
                    }
                    return false;
                }
            });
        }catch (Exception e){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ShowMessage.showToast(context, "加载二维码失败");
                }
            });
        }

        mIsHandling = false;
        mIsAdded = true;
    }
    @Deprecated
    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mIsHandling = false;
            if (mIsAdded) {
                mIsAdded = false;
                hideQrCode();
            }
        }
    };
    @Deprecated
    public void hideQrCode() {
        if (mFloatLayout.getParent() != null) {
            //移除悬浮窗口
            mWindowManager.removeViewImmediate(mFloatLayout);
        }
    }
}

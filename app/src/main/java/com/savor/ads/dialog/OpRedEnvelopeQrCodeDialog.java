package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mstar.tv.service.skin.AudioSkin;
import com.savor.ads.R;
import com.savor.ads.SavorApplication;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.ShowMessage;

/**
 * Created by zhanghq on 2016/12/10.
 */

public class OpRedEnvelopeQrCodeDialog extends Dialog{

    private Handler mHandler = new Handler();
    private Context mContext;
    private ImageView scanAvatarIconIv;
    private TextView scanNickNameTv;
    private ImageView scanQrCodeIv;
    private TextView scanWxCountDownTv;
    WindowManager mWindowManager;
    private LinearLayout mFloatLayout;

    private boolean mIsAdded;
    private boolean mIsHandling;
    private int delayTime=60*1000;
    private int height =0;
    private int width =0;
    private int mBrokenSoundId;
    private SoundPool mSoundPool;
    private int soundNum;
    private AudioSkin mAudioSkin;
    public OpRedEnvelopeQrCodeDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_op_red_envelope_view);
        setDialogAttributes();
        initViews();
        loadSound();
    }

    private void setDialogAttributes() {
        width = DensityUtil.getScreenHeight(mContext);
        height = DensityUtil.getScreenHeight(mContext)/4*3;
        Window window = getWindow(); // 得到对话框
//        window.getDecorView().setPadding(0, 20, 20, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = width;
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
        scanAvatarIconIv = findViewById(R.id.hotel_logo_iv);
        scanNickNameTv = findViewById(R.id.hotel_name_tv);
        scanWxCountDownTv = findViewById(R.id.tv_scan_wx_countdown);
        scanQrCodeIv = findViewById(R.id.iv_scan_qrcode);
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

    public void setRedEnvelopeInfo(String wxAvatarUrl,String wxNickname,String redEnvelopeUrl){
        if (!TextUtils.isEmpty(wxAvatarUrl)){
            GlideImageLoader.loadRoundImage(mContext,wxAvatarUrl,scanAvatarIconIv,R.mipmap.wxavatar);
        }
        if (!TextUtils.isEmpty(wxNickname)){
            scanNickNameTv.setText(wxNickname);
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

    @Override
    public void dismiss() {
        if (mSoundPool != null) {
            mSoundPool.release();
        }
        super.dismiss();
    }
}

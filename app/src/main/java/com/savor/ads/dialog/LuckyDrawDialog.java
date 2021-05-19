package com.savor.ads.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
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
import com.savor.ads.R;
import com.savor.ads.SavorApplication;
import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.customview.StrokeTextView;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;

import java.io.File;

/**
 * Created by zhanghq on 2021/05/17.
 */

public class LuckyDrawDialog extends Dialog{

    private Handler mHandler = new Handler();
    private Context mContext;
    private ImageView luckDrawQrcodeIV;
    private ImageView hotelIconIV;
    private StrokeTextView hotelNameTV;
    private TextView countDownTv;

    private boolean mIsAdded;
    private boolean mIsHandling;
    private int delayTime=60;
    private int width =0;
    private int height =0;
    public LuckyDrawDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_lucky_draw_view);
        setDialogAttributes();
        initViews();
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
        hotelIconIV = findViewById(R.id.hotel_icon_iv);
        hotelNameTV = findViewById(R.id.hotel_name_tv);
        luckDrawQrcodeIV = findViewById(R.id.lucky_draw_qrcode_iv);
        countDownTv = findViewById(R.id.lucky_draw_countdown_tv);
    }

    //倒计时线程
    private Runnable mCountDownRunnable = () -> wxPayCountDown();

    private void wxPayCountDown(){
        delayTime = delayTime-1;
        countDownTv.setText(delayTime+"");
        if (delayTime<=0){
            mHandler.post(mExitRunnable);
            mHandler.removeCallbacks(mCountDownRunnable);
        }else {
            mHandler.postDelayed(mCountDownRunnable,1000);
        }
    }

    @Override
    public void dismiss() {
        mHandler.removeCallbacks(mExitRunnable);
        mHandler.removeCallbacks(mCountDownRunnable);
        GlobalValues.isOpenRedEnvelopeWin = false;
        super.dismiss();
    }

    //退出线程
    private Runnable mExitRunnable = ()->dismiss();

    public void showLuckDrawWindow(String hotelAvatarUrl,String hotelName,int countDown,String luckDrawUrl) {
        LogUtils.d("showSendEnvelopeWindow");
        if (TextUtils.isEmpty(hotelAvatarUrl)) {
            LogUtils.e("URL is empty, will not show code window!!");
            return;
        }
        if (!TextUtils.isEmpty(hotelAvatarUrl)){
            GlideImageLoader.loadRoundImage(mContext,hotelAvatarUrl,hotelIconIV,R.mipmap.wxavatar);
        }
        if (!TextUtils.isEmpty(hotelName)){
            hotelNameTV.setText(hotelName);
        }
        if (countDown!=0){
            this.delayTime = countDown;
        }
        if (!TextUtils.isEmpty(luckDrawUrl)){
            GlideImageLoader.loadImageWithoutCache(mContext, luckDrawUrl, luckDrawQrcodeIV, new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    mHandler.post(()->ShowMessage.showToast(mContext, "加载二维码失败"));
                    dismiss();
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    mHandler.removeCallbacks(mExitRunnable);
                    mHandler.removeCallbacks(mCountDownRunnable);
                    countDownTv.setText(delayTime+"");
                    mHandler.postDelayed(mCountDownRunnable,1000);
                    ((SavorApplication) mContext.getApplicationContext()).hideMiniProgramQrCodeWindow();
                    GlobalValues.isOpenRedEnvelopeWin = true;
                    return false;
                }
            });
        }
    }

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


    public void hideQrCode() {
        dismiss();
    }
}

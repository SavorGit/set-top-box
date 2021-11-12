package com.savor.ads.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.savor.ads.R;
import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;

public class PrizeQrcodeDialog extends Dialog {
    private Context mContext;
    private LinearLayout wineLayout;
    private ImageView qrcodeIV;
    private TextView countdownTV;
    private String lotteryTime;
    private int countdownTime;
    private Handler mHandler = new Handler();

    public PrizeQrcodeDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_prize_qrcode);
        setDialogAttributes();
        initViews();
    }
    private void setDialogAttributes() {
        int width = DensityUtil.getScreenWidth(mContext);
        int height = DensityUtil.getScreenHeight(mContext);
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
    private void initViews() {
        wineLayout = findViewById(R.id.wine_layout);
        qrcodeIV = findViewById(R.id.qrcode_img);
        countdownTV = findViewById(R.id.countdown_tip);
        int height = DensityUtil.getScreenHeight(mContext);
        int width = DensityUtil.getScreenWidth(mContext);
        ViewGroup.LayoutParams layoutParams = wineLayout.getLayoutParams();
        layoutParams.width = width/7*4;
        layoutParams.height = height/7*4;
        wineLayout.setLayoutParams(layoutParams);
    }


    public void setDatas(String wineBgPath,String qrcodeImgUrl,int countdownTime,String lotteryTime) {
        this.lotteryTime = lotteryTime;
        wineLayout.setBackground(Drawable.createFromPath(wineBgPath));
        if (!TextUtils.isEmpty(qrcodeImgUrl)){
            GlideImageLoader.loadImage(mContext,qrcodeImgUrl,qrcodeIV);
        }
        this.countdownTime = countdownTime;
        mHandler.removeCallbacks(mCountDownRunnable);
        showCountDown();
    }

    @Override
    public void dismiss() {
        mHandler.removeCallbacks(mCountDownRunnable);
        mHandler.post(mExitRunnable);
        super.dismiss();
    }

    //倒计时线程
    private Runnable mCountDownRunnable = ()->showCountDown();

    private void showCountDown(){
        try{
            if (countdownTime!=0){
                countdownTime = countdownTime-1;
                countdownTV.setText(countdownTime+"s");
                if (countdownTime!=0){
                    mHandler.postDelayed(mCountDownRunnable,1000);
                }else{
                    countdownTV.setVisibility(View.GONE);
                    dismiss();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //退出线程
    private Runnable mExitRunnable = ()-> {
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof AdsPlayerActivity&&!TextUtils.isEmpty(lotteryTime)){
            AdsPlayerActivity adsPlayerActivity = (AdsPlayerActivity) activity;
            adsPlayerActivity.isClosePrizeHeadLayout(false);
            adsPlayerActivity.toCheckMediaIsShowMiniProgramIcon();
        }
    };
}

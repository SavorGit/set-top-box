package com.savor.ads.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.savor.ads.R;
import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.customview.StrokeTextView;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;

import java.io.File;

/**
 * Created by zhanghq on 2020/08/26.
 */

public class PartakeDishDialog extends Dialog{

    private Handler mHandler = new Handler();
    private Context mContext;
    private LinearLayout partakeActivityLayout;
    private StrokeTextView activityNameTV;
    private ImageView partakeDishQrcodeIV;
    private ImageView partakeDishImgIV;
    private StrokeTextView partakeDishNameTV;
    private TextView lotteryTimeTV;
    private TextView partakeDishCountDownTv;


    private boolean mIsAdded;
    private boolean mIsHandling;
    private int delayTime=30;
    private int width =0;
    private int height =0;
    public PartakeDishDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_partake_dish_view);
        setDialogAttributes();
        initViews();
    }

    private void setDialogAttributes() {
        width = DensityUtil.getScreenWidthOrHeight(mContext,0);
        height = DensityUtil.getScreenWidthOrHeight(mContext,1);
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
        partakeActivityLayout = findViewById(R.id.partake_activity_layout);
        ViewGroup.LayoutParams layoutParams = partakeActivityLayout.getLayoutParams();
        layoutParams.width = this.width/100*75;
        layoutParams.height = this.height/100*63;
        partakeActivityLayout.setLayoutParams(layoutParams);
        lotteryTimeTV = findViewById(R.id.lottery_time);
        partakeDishImgIV = findViewById(R.id.partake_dish_img);
        partakeDishNameTV = findViewById(R.id.partake_dish_name);
        activityNameTV = findViewById(R.id.activity_name);
        partakeDishQrcodeIV = findViewById(R.id.partake_dish_qrcode);
        ViewGroup.LayoutParams layoutParamsView = partakeDishQrcodeIV.getLayoutParams();
        layoutParamsView.height = this.height/15*4;
        layoutParamsView.width = this.height/15*4;
        partakeDishQrcodeIV.setLayoutParams(layoutParamsView);
        partakeDishCountDownTv = findViewById(R.id.partake_dish_countdown);
    }

    //倒计时线程
    private Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            wxPayCountDown();
        }
    };

    private void wxPayCountDown(){
        delayTime = delayTime-1;
        partakeDishCountDownTv.setText(delayTime+"s");
        if (delayTime<=0){
            mHandler.post(mExitRunnable);
            mHandler.removeCallbacks(mCountDownRunnable);
        }else {
            mHandler.postDelayed(mCountDownRunnable,1000);
        }
    }

    //退出线程
    private Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
            if (activity instanceof AdsPlayerActivity){
                AdsPlayerActivity adsPlayerActivity = (AdsPlayerActivity) activity;
                adsPlayerActivity.isClosePartakeDishHead(false);
            }
            dismiss();
        }
    };

    public void setPartakeDishBg(String filePath){
        File file = new File(filePath);
        if (file.exists()){
            Bitmap bit = BitmapFactory.decodeFile(filePath);
            partakeDishImgIV.setImageBitmap(bit);
        }
    }

    public void showPartakeDishWindow(final Context context, final String url,int delayTime,String lotter_time,String partake_name,String activity_name) {
        LogUtils.d("showSendEnvelopeWindow");
        if (TextUtils.isEmpty(url)) {
            LogUtils.e("URL is empty, will not show code window!!");
            return;
        }
        if (delayTime!=0){
            this.delayTime = delayTime;
        }
        if (!TextUtils.isEmpty(lotter_time)){
            lotteryTimeTV.setText("今日"+lotter_time);
        }
        if (!TextUtils.isEmpty(partake_name)){
            partakeDishNameTV.setText("奖品:"+partake_name);
        }
        if (!TextUtils.isEmpty(activity_name)){
            activityNameTV.setText(activity_name);
        }
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.removeCallbacks(mCountDownRunnable);
        wxPayCountDown();
        mIsHandling = true;

        LogUtils.v("ScanRedEnvelopeQrCodeDialog_bak 开始addView");
        LogFileUtil.write("ScanRedEnvelopeQrCodeDialog_bak 开始addView");
        if (Looper.myLooper() == Looper.getMainLooper()) {
            addToWindow(context,url);
        } else {
            mHandler.post(()->addToWindow(context,url));
        }
    }
    private void addToWindow(final Context context, String url) {
        try {
            GlideImageLoader.loadImageWithoutCache(context, url, partakeDishQrcodeIV, new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    mIsHandling = false;
                    mIsAdded = false;
                    mHandler.post(()->ShowMessage.showToast(context, "加载二维码失败"));
                    hideQrCode();
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {

                    return false;
                }
            });
        }catch (Exception e){
            mHandler.post(()->ShowMessage.showToast(context, "加载二维码失败"));
        }

        mIsHandling = false;
        mIsAdded = true;
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

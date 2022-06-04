package com.savor.ads.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
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

/**
 * Created by zhanghq on 2020/08/26.
 * 酒水运营活动
 */

public class OperationalActivityDialog extends Dialog{

    private Handler mHandler = new Handler();
    private Context mContext;
    private LinearLayout activityLayout;
    private ImageView activityQrcodeIV;
    private TextView activityCountDownTv;
    private LinearLayout prizeInfoLayout;
    private ImageView prizeImgIV;
    private StrokeTextView prizeNameTV;

    private boolean mIsAdded;
    private boolean mIsHandling;
    private int delayTime=30;
    private int width =0;
    private int height =0;
    public OperationalActivityDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_operational_activity_view);
        setDialogAttributes();
        initViews();
    }

    private void setDialogAttributes() {
        width = DensityUtil.getScreenWidth(mContext);
        height = DensityUtil.getScreenHeight(mContext);
        Window window = getWindow(); // 得到对话框
//        window.getDecorView().setPadding(0, 20, 20, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = width;
        wl.height =height;
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
        activityLayout = findViewById(R.id.activity_layout);
        ViewGroup.LayoutParams layoutParams = activityLayout.getLayoutParams();
        layoutParams.width = this.height;
        layoutParams.height = this.height/4*3;
        activityLayout.setLayoutParams(layoutParams);
        activityQrcodeIV = findViewById(R.id.activity_qrcode);
        prizeInfoLayout = findViewById(R.id.prize_info_layout);
        prizeImgIV = findViewById(R.id.prize_img);
        prizeNameTV = findViewById(R.id.prize_name);
        activityCountDownTv = findViewById(R.id.activity_countdown);
    }

    //倒计时线程
    private Runnable mCountDownRunnable = () -> mCountDown();

    private void mCountDown(){
        delayTime = delayTime-1;
        activityCountDownTv.setText(delayTime+"s");
        if (delayTime<=0){
            mHandler.post(mExitRunnable);
        }else {
            mHandler.postDelayed(mCountDownRunnable,1000);
        }
    }

    public void dismiss() {
        mHandler.post(mExitRunnable);

    }

    //退出线程
    private Runnable mExitRunnable = ()->hideQrCode();

    public void showActivityWindow(final Context context, String prizeImg,String prizeName,final String url,int delayTime) {
        LogUtils.d("OperationalActivityDialog");
        if (TextUtils.isEmpty(url)) {
            LogUtils.e("URL is empty, will not show code window!!");
            return;
        }
        if (delayTime!=0){
            this.delayTime = delayTime;
        }
        if (!TextUtils.isEmpty(prizeName)){
            prizeNameTV.setText("奖品:"+prizeName);
        }
        if (!TextUtils.isEmpty(prizeImg)){
            GlideImageLoader.loadImage(mContext,prizeImg,prizeImgIV);
            prizeInfoLayout.setVisibility(View.VISIBLE);
        }else{
            prizeInfoLayout.setVisibility(View.GONE);
        }

        mIsHandling = true;

        LogUtils.v("OperationalActivityDialog 开始addView");
        if (Looper.myLooper() == Looper.getMainLooper()) {
            addToWindow(context,url);
        } else {
            mHandler.post(()->addToWindow(context,url));
        }
    }
    private void addToWindow(final Context context, String url) {
        try {
            GlideImageLoader.loadImageWithoutCache(context, url, activityQrcodeIV, new RequestListener() {
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
                    mHandler.removeCallbacks(mHideRunnable);
                    mHandler.removeCallbacks(mCountDownRunnable);
                    mCountDown();
                    mIsAdded = true;
                    ((SavorApplication) mContext.getApplicationContext()).hideMiniProgramQrCodeWindow();
                    GlobalValues.isOpenRedEnvelopeWin = true;
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
        GlobalValues.isOpenRedEnvelopeWin = false;
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof AdsPlayerActivity) {
            ((AdsPlayerActivity<?>) activity).toCheckMediaIsShowMiniProgramIcon();
        }
        super.dismiss();
    }
}

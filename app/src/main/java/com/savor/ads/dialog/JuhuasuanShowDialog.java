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
import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.customview.StrokeTextView;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;

/**
 * Created by zhanghq on 2020/08/26.
 */

public class JuhuasuanShowDialog extends Dialog{

    private Handler mHandler = new Handler();
    private Context mContext;
    private LinearLayout juhuasuanLayout;
    private ImageView juhuasuanQrcodeIV;
    private ImageView hotelIconIV;
    private StrokeTextView hotelNameTV;
    private TextView juhuasuanCountDownTv;


    private boolean mIsAdded;
    private boolean mIsHandling;
    private int delayTime=30;
    private int width =0;
    private int height =0;
    public JuhuasuanShowDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_juhuasuan_show_view);
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
        hotelIconIV = findViewById(R.id.hotel_icon_iv);
        hotelNameTV = findViewById(R.id.hotel_name_tv);
        juhuasuanLayout = findViewById(R.id.juhuasuan_layout);
//        ViewGroup.LayoutParams layoutParams = juhuasuanLayout.getLayoutParams();
//        layoutParams.width = this.height/4;
//        layoutParams.height = this.height/4;
//        juhuasuanLayout.setLayoutParams(layoutParams);
        juhuasuanQrcodeIV = findViewById(R.id.juhuasuan_qrcode);
//        ViewGroup.LayoutParams layoutParamsView = partakeDishQrcodeIV.getLayoutParams();
//        layoutParamsView.height = this.height/15*3;
//        layoutParamsView.width = this.height/15*3;
//        partakeDishQrcodeIV.setLayoutParams(layoutParamsView);
        juhuasuanCountDownTv = findViewById(R.id.juhuasuan_show_countdown);
    }

    //倒计时线程
    private Runnable mCountDownRunnable = () -> wxPayCountDown();

    private void wxPayCountDown(){
        delayTime = delayTime-1;
        juhuasuanCountDownTv.setText(delayTime+"s");
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
        super.dismiss();
    }

    //退出线程
    private Runnable mExitRunnable = ()->dismiss();

    public void showJuhuasuanWindow(final Context context,String hotelAvatarUrl,String hotelName, final String codeurl,int delayTime) {
        LogUtils.d("showSendEnvelopeWindow");
        if (TextUtils.isEmpty(codeurl)) {
            LogUtils.e("URL is empty, will not show code window!!");
            return;
        }
        if (delayTime!=0){
            this.delayTime = delayTime;
        }else{
            this.delayTime = 30;
        }
        if (!TextUtils.isEmpty(hotelAvatarUrl)){
            GlideImageLoader.loadRoundImage(mContext,hotelAvatarUrl,hotelIconIV,R.mipmap.wxavatar);
        }
        if (!TextUtils.isEmpty(hotelName)){
            hotelNameTV.setText(hotelName);
        }
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.removeCallbacks(mCountDownRunnable);
        wxPayCountDown();
        mIsHandling = true;

        LogUtils.v("ScanRedEnvelopeQrCodeDialog_bak 开始addView");
        LogFileUtil.write("ScanRedEnvelopeQrCodeDialog_bak 开始addView");
        if (Looper.myLooper() == Looper.getMainLooper()) {
            addToWindow(context,codeurl);
        } else {
            mHandler.post(()->addToWindow(context,codeurl));
        }
    }
    private void addToWindow(final Context context, String url) {
        try {
            GlideImageLoader.loadImageWithoutCache(context, url, juhuasuanQrcodeIV, new RequestListener() {
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

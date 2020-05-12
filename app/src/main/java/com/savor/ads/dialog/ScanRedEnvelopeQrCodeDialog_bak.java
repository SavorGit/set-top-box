package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.savor.ads.R;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.ShowMessage;

/**
 * Created by zhanghq on 2016/12/10.
 */

public class ScanRedEnvelopeQrCodeDialog_bak extends Dialog{

    private Handler mHandler = new Handler();
    private Context mContext;

    private ImageView avatarIconIv;
    private TextView nickNameTv;
    private ImageView qrCodeIv;
    private TextView scanCountdownTv;
    WindowManager mWindowManager;
    private LinearLayout mFloatLayout;

    private boolean mIsAdded;
    private boolean mIsHandling;
    private int delayTime=60*1000;
    private int height =0;
    public ScanRedEnvelopeQrCodeDialog_bak(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_scan_red_envelope_view_bak);
        setDialogAttributes();
        initViews();
    }

    private void setDialogAttributes() {
        height = DensityUtil.getScreenWidthOrHeight(mContext,1)/3;
        Window window = getWindow(); // 得到对话框
//        window.getDecorView().setPadding(0, 20, 20, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = height;
//        wl.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wl.height = WindowManager.LayoutParams.MATCH_PARENT;
        wl.gravity = Gravity.LEFT;
        wl.format = PixelFormat.RGBA_8888;
        //设置window type
        wl.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wl.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        window.setDimAmount(0f);
        window.setAttributes(wl);
    }
    private void initViews(){
        avatarIconIv = findViewById(R.id.iv_wx_avatar_icon);
        nickNameTv = findViewById(R.id.tv_wx_nickname);
        qrCodeIv = findViewById(R.id.iv_qrcode);
        scanCountdownTv = findViewById(R.id.tv_scan_countdown);
    }

    public void setRedEnvelopeInfo(String wxAvatarUrl,String wxNickname,String redEnvelopeUrl){
        if (!TextUtils.isEmpty(wxAvatarUrl)){
            GlideImageLoader.loadRoundImage(mContext,wxAvatarUrl,avatarIconIv,R.mipmap.wxavatar);
        }
        if (!TextUtils.isEmpty(wxNickname)){
            nickNameTv.setText(wxNickname);
        }
        if (!TextUtils.isEmpty(redEnvelopeUrl)){
            GlideImageLoader.loadImageWithoutCache(mContext, redEnvelopeUrl, qrCodeIv, new RequestListener() {
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
                    scanCountdownTv.setText(delayTime/1000+"");
                    mHandler.removeCallbacks(mCountDownRunnable);
                    mHandler.postDelayed(mCountDownRunnable,1000);
                    return false;
                }
            });
        }
    }

    //倒计时线程
    private Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            wxPayCountDown();
        }
    };

    private void wxPayCountDown(){
        delayTime = delayTime-1000;
        scanCountdownTv.setText(delayTime/1000+"");
        if (delayTime!=0){
            mHandler.postDelayed(mCountDownRunnable,1000);
        }
    }

    private Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };
}

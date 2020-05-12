package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;

/**
 * Created by zhanghq on 2016/12/10.
 */

public class PayRedEnvelopeQrCodeDialog extends Dialog{

    private Handler mHandler = new Handler();
    private Context mContext;

    private ImageView payAvatarIconIv;
    private TextView payNickNameTv;
    private ImageView payQrCodeIv;
    private TextView payWxCountDownTv;
    WindowManager mWindowManager;
    private LinearLayout mFloatLayout;

    private boolean mIsAdded;
    private boolean mIsHandling;
    private int delayTime=60*1000;
    private int height =0;
    public PayRedEnvelopeQrCodeDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pay_red_envelope_view);
        setDialogAttributes();
        initViews();
    }

    private void setDialogAttributes() {
        height = DensityUtil.getScreenWidthOrHeight(mContext,1)/4*3;
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
        payAvatarIconIv = findViewById(R.id.iv_pay_wx_avatar_icon);
        payNickNameTv = findViewById(R.id.tv_pay_wx_nickname);
        payWxCountDownTv = findViewById(R.id.tv_pay_wx_countdown);
        payQrCodeIv = findViewById(R.id.iv_pay_qrcode);
    }

    public void setRedEnvelopeInfo(String wxAvatarUrl,String wxNickname,String redEnvelopeUrl){
        if (!TextUtils.isEmpty(wxAvatarUrl)){
            GlideImageLoader.loadRoundImage(mContext,wxAvatarUrl,payAvatarIconIv,R.mipmap.wxavatar);
        }
        if (!TextUtils.isEmpty(wxNickname)){
            payNickNameTv.setText(wxNickname);
        }
        if (!TextUtils.isEmpty(redEnvelopeUrl)){
            GlideImageLoader.loadImageWithoutCache(mContext, redEnvelopeUrl, payQrCodeIv, new RequestListener() {
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
                    payWxCountDownTv.setText(delayTime/1000+"");
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
        payWxCountDownTv.setText(delayTime/1000+"");
        if (delayTime!=0){
            mHandler.postDelayed(mCountDownRunnable,1000);
        }
    }

    //退出线程
    private Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };

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
        int width = DensityUtil.getScreenWidthOrHeight(context,0);
        int height = DensityUtil.getScreenWidthOrHeight(context,1);
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

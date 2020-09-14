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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.savor.ads.R;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;

import java.io.File;

/**
 * Created by zhanghq on 2016/12/10.
 */

public class ExtensionQrCodeDialog extends Dialog{

    private Handler mHandler = new Handler();
    private Context mContext;
    private RelativeLayout extensionLayout;
    private ImageView extensionQrcodeIV;
    private LinearLayout forscreenNumsLayout;
    private TextView forscreenNumTV;
    private TextView payWxCountDownTv;
//    WindowManager mWindowManager;
//    private RelativeLayout mFloatLayout;

    private boolean mIsAdded;
    private boolean mIsHandling;
    private int delayTime=30;
    private int width =0;
    private int height =0;
    public ExtensionQrCodeDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_extension_qrcode_view);
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
        extensionLayout = findViewById(R.id.extension_layout);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) extensionLayout.getLayoutParams();
        layoutParams.height = this.height/100*80;
        layoutParams.width = this.width/100*55;
        extensionLayout.setLayoutParams(layoutParams);
        extensionQrcodeIV = findViewById(R.id.extension_qrcode);
        ViewGroup.LayoutParams layoutParamsView = extensionQrcodeIV.getLayoutParams();
        layoutParamsView.height = this.height/11*4;
        layoutParamsView.width = this.height/11*4;
        extensionQrcodeIV.setLayoutParams(layoutParamsView);
        forscreenNumsLayout = findViewById(R.id.forscreen_nums_layout);
        forscreenNumTV = findViewById(R.id.forscreen_num);
        payWxCountDownTv = findViewById(R.id.tv_extension_countdown);
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
        payWxCountDownTv.setText(delayTime+"");
        if (delayTime!=0){
            mHandler.postDelayed(mCountDownRunnable,1000);
        }else {
            mHandler.post(mExitRunnable);
        }
    }

    //退出线程
    private Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };

    public void showExtensionWindow(final Context context, final String url,int delayTime,String forscreen_num) {
        LogUtils.d("showSendEnvelopeWindow");
        if (TextUtils.isEmpty(url)) {
            LogUtils.e("URL is empty, will not show code window!!");
            return;
        }
        if (delayTime!=0){
            this.delayTime = delayTime;
        }
        if (!TextUtils.isEmpty(forscreen_num)){
            forscreenNumsLayout.setVisibility(View.VISIBLE);
            forscreenNumTV.setText(forscreen_num+"人");
        }else{
            forscreenNumsLayout.setVisibility(View.GONE);
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
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addToWindow(context,url);
                }
            });
        }
    }
    private void addToWindow(final Context context, String url) {
        try {
            String path = AppUtils.getFilePath(AppUtils.StorageFile.cache) + ConstantValues.MINI_PROGRAM_QRCODE_EXTENSIOM_NAME;
            File localFile = new File(path);
            if (localFile.exists()){
                GlideImageLoader.loadLocalImage(context,localFile,extensionQrcodeIV);
            }else {
                GlideImageLoader.loadImageWithoutCache(context, url, extensionQrcodeIV, new RequestListener() {
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

                        return false;
                    }
                });
            }
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
//        if (mFloatLayout.getParent() != null) {
//            //移除悬浮窗口
//            mWindowManager.removeViewImmediate(mFloatLayout);
            dismiss();
//        }
    }
}

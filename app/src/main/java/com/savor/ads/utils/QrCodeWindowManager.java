package com.savor.ads.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.savor.ads.R;
import com.savor.ads.core.Session;

import java.io.File;

/**
 * Created by zhanghq on 2018/7/9.
 */

public class QrCodeWindowManager {
    private Handler mHandler = new Handler();
    private Context context;
    private static QrCodeWindowManager mInstance;
    final WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
    WindowManager mWindowManager;
    private LinearLayout mFloatLayout;
    private ImageView qrCodeFrontIV;
    private TextView qrCodeFrontTipTV;
    public static boolean mIsAdded;
    private boolean mIsHandling;

    public QrCodeWindowManager(Context mContext){
        this.context = mContext;
        if (mIsHandling) {
            return;
        }
        mIsHandling = true;

        //获取WindowManager
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.BOTTOM;

        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = DensityUtil.dip2px(context, 30);
        wmParams.y = DensityUtil.dip2px(context, 25);

        //设置悬浮窗口长宽数据
        wmParams.width = DensityUtil.dip2px(context, 168);
        wmParams.height = DensityUtil.dip2px(context, 168*1.2f);
        //获取浮动窗口视图所在布局
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        mFloatLayout = (LinearLayout) layoutInflater.inflate(R.layout.view_miniprogram_qrcode_front, null);
        qrCodeFrontIV = mFloatLayout.findViewById(R.id.iv_mini_program_qrcode_front);
        qrCodeFrontTipTV = mFloatLayout.findViewById(R.id.qrcode_front_tip);
        qrCodeFrontTipTV.setText("扫码投屏");
        qrCodeFrontTipTV.setTextColor(Color.parseColor("#e61f18"));
    }

    public static QrCodeWindowManager get(Context context){
        if (mInstance==null){
            mInstance = new QrCodeWindowManager(context);
        }
        return mInstance;

    }

    /**
     *
     * @param context
     * @param url 小程序码外网地址
     * @param path 小程序码本地地址
     */
    public void showQrCode(final Context context, final String url,final String path) {
        LogUtils.d("showQrCode");
        if (TextUtils.isEmpty(url)) {
            LogUtils.e("Code is empty, will not show code window!!");
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            addToWindow(context, url,path, qrCodeFrontIV);
        } else {
            mHandler.post(()->addToWindow(context, url,path,qrCodeFrontIV));
        }
    }

    private void addToWindow(final Context context,String url,final String path,final ImageView qrCodeIv) {

        boolean isCompletePicture = FileUtils.isCompletePicture(path);
        File localFile = null;
        long hour = 0;
        if (isCompletePicture){
            localFile = new File(path);
            long modifyTime = localFile.lastModified();
            long nowTime = System.currentTimeMillis();
            hour = (nowTime-modifyTime)/1000/60/60;
        }

        if (isCompletePicture&&hour<2&&!GlobalValues.isActivity) {
            GlideImageLoader.loadLocalImage(context,localFile,qrCodeFrontIV);
            handleWindowLayout();
        }else{
            try {
                GlideImageLoader.loadImageWithoutCache(context, url, qrCodeIv, new RequestListener() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                        mIsHandling = false;
                        ShowMessage.showToast(context, "加载二维码失败");
                        hideQrCode();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                        handleWindowLayout();
                        return false;
                    }
                });
            }catch (Exception e){
                mHandler.post(()->ShowMessage.showToast(context, "加载二维码失败"));
                e.printStackTrace();
            }

        }


    }

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mIsAdded){
                    mWindowManager.removeViewImmediate(mFloatLayout);
                }
                mIsAdded = false;
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    };

    public void hideQrCode() {
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.post(mHideRunnable);



    }

    private void handleWindowLayout(){
        try {
            if (context!=null&&mFloatLayout!=null) {
                //移除悬浮窗口
                if (mIsAdded){
                    mWindowManager.removeViewImmediate(mFloatLayout);
                    mIsAdded = false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (mFloatLayout.getParent() == null) {
            //设置悬浮窗口长宽数据
            mWindowManager.addView(mFloatLayout, wmParams);
        }
        mIsAdded = true;
        mIsHandling = false;
    }

}
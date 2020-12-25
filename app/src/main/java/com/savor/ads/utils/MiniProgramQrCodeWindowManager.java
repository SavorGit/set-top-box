package com.savor.ads.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.savor.ads.R;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.log.LogReportUtil;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by zhanghq on 2018/7/9.
 */

public class MiniProgramQrCodeWindowManager {
    private String ACTION_SHOW_START="1";
    private String ACTION_SHOW_END = "2";
    private Session session;
    private Handler mHandler = new Handler();
    private Context context;
    private String mediaId;
    private String preMediaId;
    private String qrCodeUrl;
    private String qrCodePath;
    private QrCodeWindowManager mQrCodeWindowManager;
    private static MiniProgramQrCodeWindowManager mInstance;
    final WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
    final WindowManager.LayoutParams wmNewParams = new WindowManager.LayoutParams();
    final WindowManager.LayoutParams wmCallParams = new WindowManager.LayoutParams();
    WindowManager mWindowManager;
    private FrameLayout mFloatLayout;
    private RelativeLayout qrcodeAnimLayout;
    private GifImageView qrCodeGifView;
    private ImageView qrcodeLogoIV;
    private ImageView qrcodeIV;

    private LinearLayout mFloatNewLayout;
    private ImageView qrcodeNewLogoIV;
    private TextView qrcodeNewTipTV;
    private RelativeLayout mFloatCallLayout;
    public static boolean mIsAdded;
    public static boolean mNewIsAdded;
    public static boolean mCallIsAdded;
    private boolean mIsHandling;
    private String currentTime = null;
    private int QRCodeType=0;

    public MiniProgramQrCodeWindowManager(Context mContext){
        this.context = mContext;
        session = Session.get(context);
        if (mIsHandling) {
            return;
        }
        mIsHandling = true;
        mQrCodeWindowManager = QrCodeWindowManager.get(context);

        //获取WindowManager
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //设置window type
        wmNewParams.type = wmCallParams.type = wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        wmNewParams.format = wmCallParams.format = wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmNewParams.flags = wmCallParams.flags = wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        wmNewParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        wmCallParams.gravity = Gravity.LEFT|Gravity.CENTER_VERTICAL;

        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = DensityUtil.dip2px(context, 30);
//        wmParams.y = DensityUtil.dip2px(context, 1);

        wmNewParams.x = DensityUtil.dip2px(context,50);
        wmNewParams.y = DensityUtil.dip2px(context,50);

        wmCallParams.x = DensityUtil.dip2px(context, 185);
        wmCallParams.y = DensityUtil.dip2px(context, 30);
        //设置悬浮窗口长宽数据
        wmParams.width = DensityUtil.dip2px(context, 376);
        wmParams.height = DensityUtil.dip2px(context, 188*1.2f);

        wmNewParams.width = DensityUtil.dip2px(context, 350);
        wmNewParams.height = DensityUtil.dip2px(context, 350);

        wmCallParams.width = DensityUtil.dip2px(context, 270);
        wmCallParams.height = DensityUtil.dip2px(context,270);
        //获取浮动窗口视图所在布局
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        mFloatLayout = (FrameLayout) layoutInflater.inflate(R.layout.layout_miniprogram_qrcode, null);
        qrcodeAnimLayout = mFloatLayout.findViewById(R.id.qrcode_anim_layout);
        qrCodeGifView = mFloatLayout.findViewById(R.id.qrcode_gif_view);
        qrcodeLogoIV = mFloatLayout.findViewById(R.id.qrcode_redian_logo);
        qrcodeIV = mFloatLayout.findViewById(R.id.iv_mini_program_qrcode);

        mFloatNewLayout = (LinearLayout) layoutInflater.inflate(R.layout.layout_miniprogram_new_qrcode, null);
        qrcodeNewLogoIV = mFloatNewLayout.findViewById(R.id.qrcode_new_logo);
        qrcodeNewTipTV = mFloatNewLayout.findViewById(R.id.qrcode_new_tip);
        mFloatCallLayout = (RelativeLayout) layoutInflater.inflate(R.layout.layout_miniprogram_call_qrcode,null);
    }

    public static MiniProgramQrCodeWindowManager get(Context context){
        if (mInstance==null){
            mInstance = new MiniProgramQrCodeWindowManager(context);
        }
        return mInstance;

    }

    private Runnable getToRightRunnable = ()->setLeftToRightAminator();
    /**设置左侧滑出动画*/
    private void setLeftToRightAminator(){
        if (mIsAdded){
            mWindowManager.removeViewImmediate(mFloatLayout);
            mWindowManager.addView(mFloatLayout,wmParams);
        }
        Animation translateAnimation = new TranslateAnimation(-wmParams.width-wmParams.x, 0, 0, 0);//设置平移的起点和终点
        translateAnimation.setDuration(3000);//动画持续的时间为10s
        translateAnimation.setFillEnabled(true);//使其可以填充效果从而不回到原地
        translateAnimation.setFillAfter(true);//不回到起始位置
        //如果不添加setFillEnabled和setFillAfter则动画执行结束后会自动回到远点
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mQrCodeWindowManager.hideQrCode();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHandler.postDelayed(getToLeftRunnable,session.getQrcode_showtime()*1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        qrcodeAnimLayout.setAnimation(translateAnimation);//给imageView添加的动画效果
        translateAnimation.startNow();//动画开始执行 放在最后即可

    }

    private Runnable getToLeftRunnable = ()->setRightToLeftAminator();
    /**设置滑入动画*/
    private void setRightToLeftAminator(){
        if (mIsAdded){
            mWindowManager.removeViewImmediate(mFloatLayout);
            mWindowManager.addView(mFloatLayout,wmParams);
        }
        Animation translateAnimation = new TranslateAnimation(0, -wmParams.width-wmParams.x, 0, 0);//设置平移的起点和终点
        translateAnimation.setDuration(3000);//动画持续的时间为10s
        translateAnimation.setFillEnabled(true);//使其可以填充效果从而不回到原地
        translateAnimation.setFillAfter(true);//不回到起始位置
        //如果不添加setFillEnabled和setFillAfter则动画执行结束后会自动回到远点
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHandler.postDelayed(getToRightRunnable,session.getQrcode_takttime()*1000);
                mQrCodeWindowManager.showQrCode(context,qrCodeUrl,qrCodePath);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        qrcodeAnimLayout.setAnimation(translateAnimation);//给imageView添加的动画效果
        translateAnimation.startNow();//动画开始执行 放在最后即可
    }



    /**
     * // 设置发展卡片动画
     *     @SuppressWarnings("ResourceType")
     *     private void setAnimators() {
     *         mRightOutSet = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.anim.anim_out);
     *         mLeftInSet = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.anim.anim_in);
     *     }
     *
     *     // 改变视角距离, 贴近屏幕
     *     private void setCameraDistance() {
     *         int distance = 16000;
     *         float scale = context.getResources().getDisplayMetrics().density * distance;
     *         mFloatFrontLayout.setCameraDistance(scale);
     *         mFloatBackLayout.setCameraDistance(scale);
     *     }
     *
     *     // 翻转卡片
     *     public void flipCard() {
     *         // 正面朝上
     *         if (!mIsShowBack) {
     *             mRightOutSet.setTarget(mFloatFrontLayout);
     *             mLeftInSet.setTarget(mFloatBackLayout);
     *             mRightOutSet.start();
     *             mLeftInSet.start();
     *             mIsShowBack = true;
     *         } else {
     *         // 背面朝上
     *             mRightOutSet.setTarget(mFloatBackLayout);
     *             mLeftInSet.setTarget(mFloatFrontLayout);
     *             mRightOutSet.start();
     *             mLeftInSet.start();
     *             mIsShowBack = false;
     *         }
     *     }
     */

    public void setCurrentPlayMediaId(String mediaid){
        this.mediaId = mediaid;
    }

    /**
     *
     * @param context
     * @param url 小程序码外网地址
     * @param path 小程序码本地地址
     * @param type 小程序码类型
     */
    public void showQrCode(final Context context, final String url,final String path,final int type) {
        LogUtils.d("showQrCode");
        if (TextUtils.isEmpty(url)) {
            LogUtils.e("Code is empty, will not show code window!!");
            return;
        }
        qrCodeUrl = url;
        qrCodePath = path;
        mQrCodeWindowManager.hideQrCode();
        String gifBgPath = session.getQrcodeGifBgPath();
        if (!TextUtils.isEmpty(gifBgPath)){
            qrCodeGifView.setVisibility(View.VISIBLE);
            File file = new File(gifBgPath);
            qrCodeGifView.setImageURI(Uri.fromFile(file));
        }else{
            qrCodeGifView.setVisibility(View.GONE);
        }
//        qrCodeGifView.setVisibility(View.VISIBLE);
//        qrCodeGifView.setImageResource(R.drawable.qrcode_bg);

        mHandler.removeCallbacks(getToLeftRunnable);
        mHandler.removeCallbacks(getToRightRunnable);
        QRCodeType = type;
        if (QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_SMALL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_OFFICIAL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_HELP_TYPE){
            LogUtils.v("QrCodeWindowManager 开始addView");

            if (Looper.myLooper() == Looper.getMainLooper()) {
                addToWindow(context, url,path, qrcodeIV);
            } else {
                mHandler.post(()->addToWindow(context, url,path,qrcodeIV));
            }
        }else if (QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_BIG_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_CALL_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_BIG_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_CALL_TYPE){
            final ImageView callQRCodeIv = mFloatCallLayout.findViewById(R.id.iv_mini_program_call_qrcode);
            if (Looper.myLooper() == Looper.getMainLooper()) {
                addToWindow(context, url,path,callQRCodeIv);
            } else {
                mHandler.post(()-> addToWindow(context, url,path,callQRCodeIv));
            }
        }else if (QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_NEW_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_NEW_TYPE){
            final ImageView newQRCodeIv = mFloatNewLayout.findViewById(R.id.iv_mini_program_new_qrcode);

            LogUtils.v("QrCodeWindowManager 开始addView");
            if (Looper.myLooper() == Looper.getMainLooper()) {
                addToWindow(context, url,path, newQRCodeIv);
            } else {
                mHandler.post(()->addToWindow(context, url,path,newQRCodeIv));
            }
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
        if (GlobalValues.isActivity){
            url = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ session.getEthernetMac()+"&type="+ ConstantValues.MINI_PROGRAM_QRCODE_PARTAKE_DISH_TYPE;
        }
        if ((QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_SMALL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_OFFICIAL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE)
                &&isCompletePicture&&hour<2&&!GlobalValues.isActivity) {
            GlideImageLoader.loadLocalImage(context,localFile,qrCodeIv);
            handleWindowLayout();
        }else if((QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_BIG_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_CALL_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_BIG_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_CALL_TYPE)
                &&isCompletePicture&&hour<2&&!GlobalValues.isActivity){
            ImageView qrCodeIV = mFloatCallLayout.findViewById(R.id.iv_mini_program_call_qrcode);
            GlideImageLoader.loadLocalImage(context,localFile,qrCodeIV);
            handleWindowLayout();
        }else if ((QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_NEW_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_NEW_TYPE)
                &&isCompletePicture&&hour<2&&!GlobalValues.isActivity){
            ImageView qrCodeIV = mFloatNewLayout.findViewById(R.id.iv_mini_program_new_qrcode);
            GlideImageLoader.loadLocalImage(context,localFile,qrCodeIV);
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
                if (TextUtils.isEmpty(preMediaId)){
                    mIsAdded = false;
                    return;
                }
                if (preMediaId.equals(ConstantValues.QRCODE_CALL_VIDEO_ID)){
                    if (mCallIsAdded){
                        mWindowManager.removeViewImmediate(mFloatCallLayout);
                    }
                }else if (preMediaId.equals(ConstantValues.QRCODE_PRO_VIDEO_ID)){
                    if (mNewIsAdded){
                        mWindowManager.removeViewImmediate(mFloatNewLayout);
                    }
                }else{
                    if (mIsAdded){
                        mWindowManager.removeViewImmediate(mFloatLayout);
                    }
                }
                mIsAdded = false;
                mNewIsAdded = false;
                mCallIsAdded = false;
                String id = currentTime;
                String box_mac = session.getEthernetMac();
                String media_id = preMediaId;
                String action = ACTION_SHOW_END;
                String log_time = String.valueOf(System.currentTimeMillis());;
//                sendMiniProgramIconShowLog(id,box_mac,media_id,log_time,action);
                Log.d("mpqcwm","sendMiniProgramIconShowLog(id="+id+"|box_mac="+box_mac+"|media_id="+media_id+"|log_time="+log_time+"|action="+action);
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    };

    public void hideQrCode() {
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.removeCallbacks(getToLeftRunnable);
        mHandler.removeCallbacks(getToRightRunnable);
        mHandler.post(mHideRunnable);
        mQrCodeWindowManager.hideQrCode();


    }

    private void handleWindowLayout(){
        try {
            if (context!=null&&mFloatLayout!=null) {
                //移除悬浮窗口
                if (!TextUtils.isEmpty(preMediaId)&&preMediaId.equals(ConstantValues.QRCODE_CALL_VIDEO_ID)){
                    if (mCallIsAdded){
                        mWindowManager.removeViewImmediate(mFloatCallLayout);
                        mCallIsAdded = false;
                    }
                }else if (!TextUtils.isEmpty(preMediaId)&&preMediaId.equals(ConstantValues.QRCODE_PRO_VIDEO_ID)){
                    if (mNewIsAdded){
                        mWindowManager.removeViewImmediate(mFloatNewLayout);
                        mNewIsAdded = false;
                    }
                }else{
                    if (mIsAdded){
                        mWindowManager.removeViewImmediate(mFloatLayout);
                        mIsAdded = false;
                    }
                }
                String id = currentTime;
                String box_mac = session.getEthernetMac();
                String media_id = preMediaId;
                String log_time = String.valueOf(System.currentTimeMillis());
                String action = ACTION_SHOW_END;
//                sendMiniProgramIconShowLog(id,box_mac,media_id,log_time,action);
                Log.d("mpqcwm","sendMiniProgramIconShowLog(id="+id+"|box_mac="+box_mac+"|media_id="+media_id+"|log_time="+log_time+"|action="+action);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (mFloatLayout.getParent() == null) {
            //设置悬浮窗口长宽数据
            if (mediaId.equals(ConstantValues.QRCODE_CALL_VIDEO_ID)){
                mWindowManager.addView(mFloatCallLayout, wmCallParams);
            }else if (mediaId.equals(ConstantValues.QRCODE_PRO_VIDEO_ID)){
                mWindowManager.addView(mFloatNewLayout, wmNewParams);
            }else {
                mWindowManager.addView(mFloatLayout, wmParams);
                try {
                    if (session.getQrcode_showtime()>0){
                        setLeftToRightAminator();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE){
                qrcodeLogoIV.setVisibility(View.VISIBLE);
            }else if (QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_NEW_TYPE){
                qrcodeNewLogoIV.setVisibility(View.VISIBLE);
                qrcodeNewTipTV.setTextColor(Color.parseColor("#231816"));
            }else{
                qrcodeNewTipTV.setTextColor(Color.parseColor("#e61f18"));
                qrcodeLogoIV.setVisibility(View.GONE);
                qrcodeNewLogoIV.setVisibility(View.GONE);
            }

            LogUtils.v("QrCodeWindowManager addView SUCCESS");
//                    LogFileUtil.write("QrCodeWindowManager addView SUCCESS");
            currentTime = String.valueOf(System.currentTimeMillis());
            String id = currentTime;
            String box_mac = session.getEthernetMac();
            String media_id = mediaId;
            String action = ACTION_SHOW_START;
            String log_time = currentTime;
//            sendMiniProgramIconShowLog(id,box_mac,media_id,log_time,action);
            preMediaId = mediaId;
            Log.d("mpqcwm","sendMiniProgramIconShowLog(id="+id+"|box_mac="+box_mac+"|media_id="+media_id+"|log_time="+log_time+"|action="+action);
        }
        //翻转小程序码
//        mHandler.removeCallbacks(flipCardRunnable);
//        mHandler.postDelayed(flipCardRunnable,1000*3);
        mIsHandling = false;
        if (QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_SMALL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_OFFICIAL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_HELP_TYPE){
            mIsAdded = true;
        }else if(QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_BIG_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_CALL_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_BIG_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_CALL_TYPE){
            mCallIsAdded = true;
        }else if (QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_NEW_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_NEW_TYPE){
            mNewIsAdded = true;
        }
    }

//    private Runnable flipCardRunnable = ()->flipCard();
}

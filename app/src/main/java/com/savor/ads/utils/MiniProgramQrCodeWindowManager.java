package com.savor.ads.utils;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.log.LogReportUtil;

import java.io.File;
import java.util.HashMap;

/**
 * Created by zhanghq on 2018/7/9.
 */

public class MiniProgramQrCodeWindowManager {
    private String ACTION_SHOW_START="1";
    private String ACTION_SHOW_END = "2";
    private Session session;
    private Handler mHandler = new Handler();
    private Context context;
    private LogReportUtil logReportUtil;
    private String mediaId;
    private String preMediaId;
    private static MiniProgramQrCodeWindowManager mInstance;
    final WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
    final WindowManager.LayoutParams wmNewParams = new WindowManager.LayoutParams();
    final WindowManager.LayoutParams wmCallParams = new WindowManager.LayoutParams();
    WindowManager mWindowManager;
    private FrameLayout mFloatLayout;
    private LinearLayout mFloatFrontLayout;
    private ImageView qrcodeFrontLogoIV;
    private TextView qrcodeFrontTipTV;
    private LinearLayout mFloatBackLayout;
    private ImageView qrcodeBackLogoIV;
    private TextView qrcodeBackTipTV;

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

    private AnimatorSet mRightOutSet; // 右出动画
    private AnimatorSet mLeftInSet; // 左入动画
    private boolean mIsShowBack;

    public MiniProgramQrCodeWindowManager(Context mContext){
        this.context = mContext;
        session = Session.get(context);
        logReportUtil = LogReportUtil.get(context);
        if (mIsHandling) {
            return;
        }
        mIsHandling = true;

        final String ssid = AppUtils.getShowingSSID(context);


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
        wmParams.y = DensityUtil.dip2px(context, 25);

        wmNewParams.x = DensityUtil.dip2px(context,50);
        wmNewParams.y = DensityUtil.dip2px(context,50);

        wmCallParams.x = DensityUtil.dip2px(context, 185);
        wmCallParams.y = DensityUtil.dip2px(context, 30);
        //设置悬浮窗口长宽数据
        wmParams.width = DensityUtil.dip2px(context, 168);
        wmParams.height = DensityUtil.dip2px(context, 168*1.2f);

        wmNewParams.width = DensityUtil.dip2px(context, 350);
        wmNewParams.height = DensityUtil.dip2px(context, 350);

        wmCallParams.width = DensityUtil.dip2px(context, 270);
        wmCallParams.height = DensityUtil.dip2px(context,270);
        //获取浮动窗口视图所在布局
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        mFloatLayout = (FrameLayout) layoutInflater.inflate(R.layout.layout_miniprogram_qrcode, null);
        mFloatFrontLayout = mFloatLayout.findViewById(R.id.layout_miniprogram_qrcode_front);
        qrcodeFrontLogoIV = mFloatLayout.findViewById(R.id.qrcode_front_logo);
        qrcodeFrontTipTV = mFloatFrontLayout.findViewById(R.id.qrcode_front_tip);
        mFloatBackLayout = mFloatLayout.findViewById(R.id.layout_miniprogram_qrcode_back);
        qrcodeBackLogoIV = mFloatLayout.findViewById(R.id.qrcode_back_logo);
        qrcodeBackTipTV = mFloatBackLayout.findViewById(R.id.qrcode_back_tip);
        mFloatNewLayout = (LinearLayout) layoutInflater.inflate(R.layout.layout_miniprogram_new_qrcode, null);
        qrcodeNewLogoIV = mFloatNewLayout.findViewById(R.id.qrcode_new_logo);
        qrcodeNewTipTV = mFloatNewLayout.findViewById(R.id.qrcode_new_tip);
        mFloatCallLayout = (RelativeLayout) layoutInflater.inflate(R.layout.layout_miniprogram_call_qrcode,null);
        setAnimators(); // 设置动画
        setCameraDistance(); // 设置镜头距离
    }

    public static MiniProgramQrCodeWindowManager get(Context context){
        if (mInstance==null){
            mInstance = new MiniProgramQrCodeWindowManager(context);
        }
        return mInstance;

    }

    // 设置动画
    @SuppressWarnings("ResourceType")
    private void setAnimators() {
        mRightOutSet = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.anim.anim_out);
        mLeftInSet = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.anim.anim_in);
    }

    // 改变视角距离, 贴近屏幕
    private void setCameraDistance() {
        int distance = 16000;
        float scale = context.getResources().getDisplayMetrics().density * distance;
        mFloatFrontLayout.setCameraDistance(scale);
        mFloatBackLayout.setCameraDistance(scale);
    }

    // 翻转卡片
    public void flipCard() {
        // 正面朝上
        if (!mIsShowBack) {
            mRightOutSet.setTarget(mFloatFrontLayout);
            mLeftInSet.setTarget(mFloatBackLayout);
            mRightOutSet.start();
            mLeftInSet.start();
            mIsShowBack = true;
        } else {
        // 背面朝上
            mRightOutSet.setTarget(mFloatBackLayout);
            mLeftInSet.setTarget(mFloatFrontLayout);
            mRightOutSet.start();
            mLeftInSet.start();
            mIsShowBack = false;
        }
    }

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

        QRCodeType = type;
        if (QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_SMALL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE){
            final ImageView qrCodeFrontIv = mFloatLayout.findViewById(R.id.iv_mini_program_qrcode_front);
            final ImageView qrCodeBackIv = mFloatLayout.findViewById(R.id.iv_mini_program_qrcode_back);

            LogUtils.v("QrCodeWindowManager 开始addView");

            if (Looper.myLooper() == Looper.getMainLooper()) {
                addToWindow(context, url,path, qrCodeFrontIv);
                addToWindow(context, url,path, qrCodeBackIv);
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        addToWindow(context, url,path,qrCodeFrontIv);
                        addToWindow(context, url,path,qrCodeFrontIv);
                    }
                });
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

    private void addToWindow(final Context context,final String url,final String path,final ImageView qrCodeIv) {

        boolean isCompletePicture = FileUtils.isCompletePicture(path);
        File localFile = new File(path);
        long modifyTime = localFile.lastModified();
        long nowTime = System.currentTimeMillis();
        long hour = (nowTime-modifyTime)/1000/60/60;
        if ((QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_SMALL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE)
                &&isCompletePicture&&hour<2) {
            ImageView qrCodeFrontIV = mFloatLayout.findViewById(R.id.iv_mini_program_qrcode_front);
            ImageView qrCodeBackIV = mFloatLayout.findViewById(R.id.iv_mini_program_qrcode_back);
            GlideImageLoader.loadLocalImage(context,localFile,qrCodeFrontIV);
            GlideImageLoader.loadLocalImage(context,localFile,qrCodeBackIV);
            handleWindowLayout();
        }else if((QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_BIG_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_CALL_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_BIG_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_CALL_TYPE)
                &&isCompletePicture&&hour<2){
            ImageView qrCodeIV = mFloatCallLayout.findViewById(R.id.iv_mini_program_call_qrcode);
            GlideImageLoader.loadLocalImage(context,localFile,qrCodeIV);
            handleWindowLayout();
        }else if ((QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_NEW_TYPE
                    ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_NEW_TYPE)
                &&isCompletePicture&&hour<2){
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
        mHandler.post(mHideRunnable);



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
            }
            if (QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE){
                qrcodeFrontLogoIV.setVisibility(View.VISIBLE);
                qrcodeBackLogoIV.setVisibility(View.VISIBLE);
                qrcodeFrontTipTV.setTextColor(Color.parseColor("#231816"));
                qrcodeBackTipTV.setTextColor(Color.parseColor("#231816"));
            }else if (QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_NEW_TYPE){
                qrcodeNewLogoIV.setVisibility(View.VISIBLE);
                qrcodeNewTipTV.setTextColor(Color.parseColor("#231816"));
            }else{
                qrcodeFrontTipTV.setTextColor(Color.parseColor("#e61f18"));
                qrcodeBackTipTV.setTextColor(Color.parseColor("#e61f18"));
                qrcodeNewTipTV.setTextColor(Color.parseColor("#e61f18"));
                qrcodeFrontLogoIV.setVisibility(View.GONE);
                qrcodeBackLogoIV.setVisibility(View.GONE);
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
        mHandler.removeCallbacks(flipCardRunnable);
        mHandler.postDelayed(flipCardRunnable,1000*3);
        mIsHandling = false;
        if (QRCodeType==ConstantValues.MINI_PROGRAM_QRCODE_SMALL_TYPE
                ||QRCodeType==ConstantValues.MINI_PROGRAM_SQRCODE_SMALL_TYPE){
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

    private Runnable flipCardRunnable = ()->flipCard();

    /**
     *
     * @param id 开始结束成对存在的流水号
     * @param box_mac 机顶盒mac
     * @param media_id 当前播放视频id
     * @param log_time 二维码动作时间
     * @param action 二维码动作是开始还是结束
     */
    private void sendMiniProgramIconShowLog(String id,String box_mac,String media_id,String log_time,String action){
        HashMap<String,Object> params = new HashMap<>();
        params.put("id",id);
        params.put("box_mac",box_mac);
        params.put("media_id",media_id);
        params.put("log_time",log_time);
        params.put("action",action);
        AppApi.postMiniProgramIconShowLog(context,requestListener,params);

    }

    ApiRequestListener requestListener = new ApiRequestListener() {
        @Override
        public void onSuccess(AppApi.Action method, Object obj) {

        }

        @Override
        public void onError(AppApi.Action method, Object obj) {

        }

        @Override
        public void onNetworkFailed(AppApi.Action method) {

        }
    };
}

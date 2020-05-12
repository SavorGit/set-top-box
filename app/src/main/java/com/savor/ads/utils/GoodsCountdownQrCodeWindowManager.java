package com.savor.ads.utils;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.savor.ads.R;
import com.savor.ads.core.Session;
import com.savor.ads.log.LogReportUtil;

import java.io.File;

/**
 * 秒杀专用
 * Created by bichao on 2019/7/18.
 */

public class GoodsCountdownQrCodeWindowManager {
    private String ACTION_SHOW_START="1";
    private String ACTION_SHOW_END = "2";
    private Session session;
    private Handler mHandler = new Handler();
    //秒杀商品倒计时(单位/秒)
    private long delayTime=0;
    private Context context;
    private LogReportUtil logReportUtil;
    private String mediaId;
    private String preMediaId;
    private static GoodsCountdownQrCodeWindowManager mInstance;
    final WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
    WindowManager mWindowManager;
    private LinearLayout mFloatFrontLayout;
    private LinearLayout qrcodeTipLayout;
    private ImageView qrcodeFrontLogoIV;
    private TextView qrcodeFrontTipTV;
    private RelativeLayout qrcodeCountdownLayout;
    private TextView qrcodeHourTipTV;
    private TextView qrcodeMinuteTipTV;
    private TextView qrcodeSecondTipTV;
    public static boolean mGoodIsAdded;
    private boolean mIsHandling;
    private String currentTime = null;


    public GoodsCountdownQrCodeWindowManager(Context mContext){
        this.context = mContext;
        session = Session.get(context);
        logReportUtil = LogReportUtil.get(context);
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
        wmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;

        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = DensityUtil.dip2px(context, 30);
        wmParams.y = DensityUtil.dip2px(context, 25);

        //设置悬浮窗口长宽数据
        wmParams.width = DensityUtil.dip2px(context, 168);
        wmParams.height = DensityUtil.dip2px(context, 168*1.4f);

        //获取浮动窗口视图所在布局
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        mFloatFrontLayout = (LinearLayout) layoutInflater.inflate(R.layout.view_good_countdown_miniprogram_qrcode,null);
        qrcodeTipLayout = mFloatFrontLayout.findViewById(R.id.qrcode_tip_layout);
        qrcodeFrontLogoIV = mFloatFrontLayout.findViewById(R.id.qrcode_front_logo);
        qrcodeFrontTipTV = mFloatFrontLayout.findViewById(R.id.qrcode_front_tip);
        qrcodeCountdownLayout = mFloatFrontLayout.findViewById(R.id.qrcode_countdown_layout);
        qrcodeHourTipTV = mFloatFrontLayout.findViewById(R.id.qrcode_hour_tip);
        qrcodeMinuteTipTV = mFloatFrontLayout.findViewById(R.id.qrcode_minute_tip);
        qrcodeSecondTipTV = mFloatFrontLayout.findViewById(R.id.qrcode_second_tip);
    }

    public static GoodsCountdownQrCodeWindowManager get(Context context){
        if (mInstance==null){
            mInstance = new GoodsCountdownQrCodeWindowManager(context);
        }
        return mInstance;

    }

    /**
     *
     * @param context
     * @param url 小程序码外网地址
     * @param path 小程序码本地地址
     */
    public void showQrCode(final Context context, final String url,final String path,long countdownTime) {
        LogUtils.d("showQrCode");
        if (TextUtils.isEmpty(url)) {
            LogUtils.e("Code is empty, will not show code window!!");
            return;
        }
        delayTime = countdownTime;
        if (delayTime>0){
            qrcodeTipLayout.setVisibility(View.GONE);
            qrcodeCountdownLayout.setVisibility(View.VISIBLE);
        }else{
            qrcodeTipLayout.setVisibility(View.VISIBLE);
            qrcodeCountdownLayout.setVisibility(View.GONE);
        }
        mHandler.removeCallbacks(mCountDownRunnable);
        goodsShowCountDown();

        final ImageView qrCodeFrontIv = mFloatFrontLayout.findViewById(R.id.mini_program_qrcode_front);

        LogUtils.v("QrCodeWindowManager 开始addView");

        if (Looper.myLooper() == Looper.getMainLooper()) {
            addToWindow(context, url,path, qrCodeFrontIv);
        } else {
            mHandler.post(()->addToWindow(context, url,path,qrCodeFrontIv));
        }

    }

    private void addToWindow(final Context context,final String url,final String path,final ImageView qrCodeIv) {

        boolean isCompletePicture = FileUtils.isCompletePicture(path);
        if (isCompletePicture) {
            File localFile = new File(path);
            GlideImageLoader.loadLocalImage(context,localFile,qrCodeIv);
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
                if (mGoodIsAdded){
                    mWindowManager.removeViewImmediate(mFloatFrontLayout);
                }
                mGoodIsAdded = false;
                String id = currentTime;
                String box_mac = session.getEthernetMac();
                String media_id = preMediaId;
                String action = ACTION_SHOW_END;
                String log_time = String.valueOf(System.currentTimeMillis());;
                Log.d("mpqcwm","sendMiniProgramIconShowLog(id="+id+"|box_mac="+box_mac+"|media_id="+media_id+"|log_time="+log_time+"|action="+action);
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    };

    public void hideQrCode() {
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.post(mHideRunnable);
        mHandler.removeCallbacks(mCountDownRunnable);


    }

    private void handleWindowLayout(){
        try {
            if (mGoodIsAdded&&context!=null&&mFloatFrontLayout!=null) {
                //移除悬浮窗口
                if (mGoodIsAdded){
                    mWindowManager.removeViewImmediate(mFloatFrontLayout);
                }

                mGoodIsAdded = false;
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
        if (mFloatFrontLayout.getParent() == null) {
            //设置悬浮窗口长宽数据
            mWindowManager.addView(mFloatFrontLayout, wmParams);

            LogUtils.v("QrCodeWindowManager addView SUCCESS");
//                    LogFileUtil.write("QrCodeWindowManager addView SUCCESS");
            currentTime = String.valueOf(System.currentTimeMillis());
            String id = currentTime;
            String box_mac = session.getEthernetMac();
            String media_id = mediaId;
            String action = ACTION_SHOW_START;
            String log_time = currentTime;
            preMediaId = mediaId;
            Log.d("mpqcwm","sendMiniProgramIconShowLog(id="+id+"|box_mac="+box_mac+"|media_id="+media_id+"|log_time="+log_time+"|action="+action);
        }
        mIsHandling = false;
        mGoodIsAdded = true;
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.postDelayed(mHideRunnable,1000*60*2);
    }

    //倒计时线程
    private Runnable mCountDownRunnable = ()->goodsShowCountDown();

    private void goodsShowCountDown(){
        try{
            if (delayTime>0){
                delayTime = delayTime-1;
                String timeStr = TimeUtils.formatSeconds(delayTime);
                String [] times = timeStr.split(":");
                if (times!=null&&times.length==3){
                    qrcodeHourTipTV.setText(times[0]);
                    qrcodeMinuteTipTV.setText(times[1]);
                    qrcodeSecondTipTV.setText(times[2]);
                }
                mHandler.postDelayed(mCountDownRunnable,1000);
            }else{
                mHandler.removeCallbacks(mCountDownRunnable);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

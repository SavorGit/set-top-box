package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
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
import com.savor.ads.customview.StrokeTextView;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by zhanghq on 2020/08/26.
 */

public class JuhuasuanResultDialog extends Dialog{

    private Handler mHandler = new Handler();
    private Context mContext;
    private LinearLayout juhuasuanResultLayout;
//    private ImageView hotelIconIV;
//    private StrokeTextView hotelNameTV;
    private TextView activityPriceTv;
    private TextView originalPriceTv;
    private TextView juhuasuanResultCountDownTv;


    private boolean mIsAdded;
    private boolean mIsHandling;
    private int delayTime=30;
    private int width =0;
    private int height =0;
    public JuhuasuanResultDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_juhuasuan_result_view);
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
//        hotelIconIV = findViewById(R.id.hotel_icon_iv);
//        hotelNameTV = findViewById(R.id.hotel_name_tv);
        juhuasuanResultLayout = findViewById(R.id.juhuasuan_result_layout);
//        ViewGroup.LayoutParams layoutParams = juhuasuanResultLayout.getLayoutParams();
//        layoutParams.width = this.height;
//        layoutParams.height = this.height/4*3;
//        juhuasuanResultLayout.setLayoutParams(layoutParams);
//        ViewGroup.LayoutParams layoutParamsView = partakeDishQrcodeIV.getLayoutParams();
//        layoutParamsView.height = this.height/15*3;
//        layoutParamsView.width = this.height/15*3;
//        partakeDishQrcodeIV.setLayoutParams(layoutParamsView);
        activityPriceTv = findViewById(R.id.activity_price_tv);
        originalPriceTv = findViewById(R.id.original_price_tv);
        juhuasuanResultCountDownTv = findViewById(R.id.juhuasuan_result_countdown);
    }

    //倒计时线程
    private Runnable mCountDownRunnable = () -> wxPayCountDown();

    private void wxPayCountDown(){
        delayTime = delayTime-1;
        juhuasuanResultCountDownTv.setText(delayTime+"s");
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

    public void juhuasuanResultWindow(String activityPrice, String jdPrice, List<String> listContent, int delayTime) {

        if (delayTime!=0){
            this.delayTime = delayTime;
        }else{
            this.delayTime = 30;
        }
        if (!TextUtils.isEmpty(activityPrice)){
            activityPriceTv.setText("活动价格："+activityPrice+"/瓶");
        }
        if (!TextUtils.isEmpty(jdPrice)){
        Spannable spanStrikethrough = new SpannableString("京东价格："+jdPrice+"/瓶");
        StrikethroughSpan stSpan = new StrikethroughSpan();  //设置删除线样式
        spanStrikethrough.setSpan(stSpan, 0, jdPrice.length()+7, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        originalPriceTv.setText(spanStrikethrough);
        }
        if (listContent!=null&&listContent.size()>0){
            juhuasuanResultLayout.removeAllViews();
            for (String content:listContent){
                View view = View.inflate(mContext, R.layout.item_text, null);
                TextView tv = view.findViewById(R.id.activity_name_tv);
                tv.setText(content);
                juhuasuanResultLayout.addView(view);
            }
        }
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.removeCallbacks(mCountDownRunnable);
        wxPayCountDown();

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

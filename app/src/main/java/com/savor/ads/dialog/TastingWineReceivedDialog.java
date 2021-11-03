package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
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

import com.savor.ads.R;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;

public class TastingWineReceivedDialog extends Dialog {
    private Context mContext;
    private LinearLayout judgeLayout;
    private ImageView avatarIV;
    private TextView nickNameTV;
    private TextView judgeTipTV;
    private ImageView judgeImgIV;
    private TextView countdowTV;

    private int countdownTime;
    private Handler mHandler = new Handler();

    public TastingWineReceivedDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_tasting_wine_received);
        setDialogAttributes();
        initViews();
    }
    private void setDialogAttributes() {
        int width = DensityUtil.getScreenWidth(mContext);
        int height = DensityUtil.getScreenHeight(mContext);
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
    private void initViews() {
        judgeLayout = findViewById(R.id.judge_layout);
        avatarIV = findViewById(R.id.lucky_avatar);
        nickNameTV = findViewById(R.id.lucky_nickname);
        judgeTipTV = findViewById(R.id.judge_tip);
        judgeImgIV = findViewById(R.id.judge_img);
        countdowTV = findViewById(R.id.countdown_tip);
        int height = DensityUtil.getScreenHeight(mContext);
        ViewGroup.LayoutParams layoutParams = judgeLayout.getLayoutParams();
        layoutParams.width = height/7*5;
        layoutParams.height = height/7*5;
        judgeLayout.setLayoutParams(layoutParams);
    }


    public void setDatas(String avatarUrl,String nickName,String judgeImgUrl,int countdownTime) {
        if (!TextUtils.isEmpty(avatarUrl)){
            GlideImageLoader.loadRoundImage(mContext,avatarUrl,avatarIV,R.mipmap.wxavatar);
        }
        if (!TextUtils.isEmpty(nickName)){
            nickNameTV.setText(nickName);
        }
        if (!TextUtils.isEmpty(judgeImgUrl)){
            GlideImageLoader.loadImage(mContext,judgeImgUrl,judgeImgIV);
        }
        this.countdownTime = countdownTime;
        mHandler.removeCallbacks(mCountDownRunnable);
        showCountDown();
    }

    @Override
    public void dismiss() {
        mHandler.removeCallbacks(mCountDownRunnable);
        super.dismiss();
    }

    //倒计时线程
    private Runnable mCountDownRunnable = ()->showCountDown();

    private void showCountDown(){
        try{
            if (countdownTime!=0){
                countdownTime = countdownTime-1;
                countdowTV.setText(countdownTime+"s");
                if (countdownTime!=0){
                    mHandler.postDelayed(mCountDownRunnable,1000);
                }else{
                    countdowTV.setVisibility(View.GONE);
                    dismiss();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

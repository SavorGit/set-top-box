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

public class BusinessCardDialog extends Dialog {
    private Context mContext;
    private LinearLayout cardLayout;
    private ImageView cardAvatarIV;
    private TextView nickNameTV;
    private TextView jobTitleTV;
    private TextView mobileTV;
    private TextView companyTV;
    private ImageView cardQrcodeIV;
    private TextView cardCountdowTipTV;

    private String cardAvatarUrl;
    private String nickName;
    private String jobTitle;
    private String mobile;
    private String company;
    private String cardQrcodeUrl;
    private int countdownTime;
    private Handler mHandler = new Handler();

    public BusinessCardDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_business_card);
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
        cardLayout = findViewById(R.id.card_layout);
        cardAvatarIV = findViewById(R.id.card_avatar);
        nickNameTV = findViewById(R.id.nick_name);
        jobTitleTV = findViewById(R.id.job_title);
        mobileTV = findViewById(R.id.mobile);
        companyTV = findViewById(R.id.company);
        cardQrcodeIV = findViewById(R.id.card_qrcode);
        cardCountdowTipTV = findViewById(R.id.card_countdow_tip);
        int width = DensityUtil.getScreenWidth(mContext);
        int height = DensityUtil.getScreenHeight(mContext);
        ViewGroup.LayoutParams layoutParams = cardLayout.getLayoutParams();
        layoutParams.width = width/7*3;
        layoutParams.height = height/7*3;
        cardLayout.setLayoutParams(layoutParams);
    }


    public void setDatas(String cardAvatarUrl,String nickName,String jobTitle,String mobile,String company,String cardQrcodeUrl,int countdownTime) {
        if(!TextUtils.isEmpty(cardAvatarUrl)){
            GlideImageLoader.loadRoundImage(mContext,cardAvatarUrl,cardAvatarIV,R.mipmap.wxavatar);
        }
        if (!TextUtils.isEmpty(nickName)){
            nickNameTV.setText(nickName);
        }
        if (!TextUtils.isEmpty(jobTitle)){
            jobTitleTV.setText(jobTitle);
        }
        if (!TextUtils.isEmpty(mobile)){
            mobileTV.setText("tel:"+mobile);
        }
        if (!TextUtils.isEmpty(company)){
            companyTV.setText(company);
        }
        if (!TextUtils.isEmpty(cardQrcodeUrl)){
            GlideImageLoader.loadImage(mContext,cardAvatarUrl,cardAvatarIV);
        }
        this.countdownTime = countdownTime;
        mHandler.removeCallbacks(mCountDownRunnable);
        showCountDown();
    }


    //倒计时线程
    private Runnable mCountDownRunnable = ()->showCountDown();

    private void showCountDown(){
        try{
            if (countdownTime!=0){
                countdownTime = countdownTime-1;
                cardCountdowTipTV.setText(countdownTime+"s");
                if (countdownTime!=0){
                    mHandler.postDelayed(mCountDownRunnable,1000);
                }else{
                    cardCountdowTipTV.setVisibility(View.GONE);
                    dismiss();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

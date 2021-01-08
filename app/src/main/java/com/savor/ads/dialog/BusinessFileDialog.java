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

public class BusinessFileDialog extends Dialog {
    private Context mContext;
    private LinearLayout fileLayout;
    private ImageView avatarIconIV;
    private TextView nickNameTV;
    private TextView fileNameTV;
    private ImageView fileQrcodeIV;
    private TextView countdowTipTV;

    private int countdownTime;
    private Handler mHandler = new Handler();

    public BusinessFileDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_business_file);
        setDialogAttributes();
        initViews();
    }
    private void setDialogAttributes() {
        int width = DensityUtil.getScreenWidth(mContext);
        int height = DensityUtil.getScreenHeight(mContext);
        Window window = getWindow(); // 得到对话框
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
        fileLayout = findViewById(R.id.file_layout);
        avatarIconIV = findViewById(R.id.card_avatar);
        nickNameTV = findViewById(R.id.nick_name);
        fileNameTV = findViewById(R.id.filename);
        fileQrcodeIV = findViewById(R.id.file_qrcode);
        countdowTipTV = findViewById(R.id.countdow_tip);
        int width = DensityUtil.getScreenWidth(mContext);
        int height = DensityUtil.getScreenHeight(mContext);
        ViewGroup.LayoutParams layoutParams = fileLayout.getLayoutParams();
        layoutParams.width = width/7*3;
        layoutParams.height = height/7*3;
        fileLayout.setLayoutParams(layoutParams);
    }


    public void setDatas(String cardAvatarUrl,String nickName,String fileName,String cardQrcodeUrl,int countdownTime) {
        if(!TextUtils.isEmpty(cardAvatarUrl)){
            GlideImageLoader.loadRoundImage(mContext,cardAvatarUrl,avatarIconIV,R.mipmap.wxavatar);
        }
        if (!TextUtils.isEmpty(nickName)){
            nickNameTV.setText(nickName);
        }
        if (!TextUtils.isEmpty(fileName)){
            fileNameTV.setText(fileName);
        }

        if (!TextUtils.isEmpty(cardQrcodeUrl)){
            GlideImageLoader.loadImage(mContext,cardAvatarUrl,fileQrcodeIV);
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
                countdowTipTV.setText(countdownTime+"s");
                if (countdownTime!=0){
                    mHandler.postDelayed(mCountDownRunnable,1000);
                }else{
                    countdowTipTV.setVisibility(View.GONE);
                    dismiss();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

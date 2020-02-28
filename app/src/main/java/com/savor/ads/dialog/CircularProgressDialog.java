package com.savor.ads.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.savor.ads.R;
import com.savor.ads.utils.DashboardView4;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.PercentCircle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghq on 2018/8/28.
 * 展示视频下载进度条
 */
public class CircularProgressDialog extends Dialog{
    private Context mContext;
    private RelativeLayout circular_layout;
    private PercentCircle percentCircle;
    private Handler mHandler = new Handler();
    private static final int PROJECT_TIP_DURATION = 1000 * 60;
    private boolean isShowTip;
    private boolean isAnimFinished;
    public CircularProgressDialog(@NonNull Context context) {
        super(context,R.style.miniProgramImagesDialog);
        mContext = context;
    }

    public CircularProgressDialog(Context context, int theme){
        super(context,theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_circular_progress);
        setDialogAttributes();

    }

    private void setDialogAttributes() {
        Window window = getWindow(); // 得到对话框
        window.getDecorView().setPadding(20, 20, 0, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = WindowManager.LayoutParams.MATCH_PARENT;
        wl.height = WindowManager.LayoutParams.MATCH_PARENT;
        wl.gravity = Gravity.LEFT;
        wl.format = PixelFormat.RGBA_8888;
        //设置window type
        wl.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wl.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        window.setDimAmount(0f);
        window.setAttributes(wl);



    }


    public void initContent(){
        circular_layout = (RelativeLayout) findViewById(R.id.circular_layout);
        percentCircle = (PercentCircle) findViewById(R.id.percentCircle);



    }

    public void initnum(int num){
        percentCircle.setTargetPercent(num);
    }


    /**
     * 投屏提示退出Runnable
     */
    private Runnable mProjectTipOutRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtils.w("mProjectTipOutRunnable " );
            projectTipAnimateOut();
        }
    };
    public void projectTipAnimateIn() {
        mHandler.removeCallbacks(mProjectTipOutRunnable);
        mHandler.postDelayed(mProjectTipOutRunnable, PROJECT_TIP_DURATION);
        if (isShowTip){
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doAnimationIn();
        } else {
            circular_layout.post(new Runnable() {
                @Override
                public void run() {
                    doAnimationIn();
                }
            });
        }
    }
    private void doAnimationIn() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        circular_layout.setVisibility(View.VISIBLE);
        circular_layout.startAnimation(animation);
        isShowTip = true;
    }

    public void projectTipAnimateOut() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, -1,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        circular_layout.startAnimation(animation);
        isShowTip = false;
    }
    @Override
    public void show() {
        super.show();
//        initContent();
    }

    UpdateDownloadedProgress downloadedProgress= new UpdateDownloadedProgress() {
        @Override
        public void upadteDownlaodedNum(int allNum, int currentNum) {

        }
    };

    public interface UpdateDownloadedProgress{

        void upadteDownlaodedNum(int allNum, int currentNum);
    }
}

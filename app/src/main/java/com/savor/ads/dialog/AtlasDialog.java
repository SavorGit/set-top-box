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

import com.savor.ads.R;
import com.savor.ads.utils.DashboardView4;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghq on 2018/8/7.
 * 展示图片
 */
public class AtlasDialog extends Dialog{
    private List<Integer> list = new ArrayList<>();
    private Context mContext;
    private LinearLayout atlas_all_layout;
//    private TextView downloadedTipTV;
    private DashboardView4 dashboard_view;
    private Handler mHandler = new Handler();
    private static final int PROJECT_TIP_DURATION = 1000 * 10;
    private boolean isShowTip;
    private boolean isAnimFinished;
    public AtlasDialog(@NonNull Context context) {
        super(context,R.style.miniProgramImagesDialog);
        mContext = context;
    }

    public AtlasDialog(Context context,int theme){
        super(context,theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_atlas);
        setDialogAttributes();
//        initContent(0,0);
    }

    private void setDialogAttributes() {
        int height = DensityUtil.getScreenWidthOrHeight(mContext,1)/9;
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

    public UpdateDownloadedProgress getUpdateDownloadedProgress(){
        return downloadedProgress;
    }


    public void initContent(){
//        list.add(R.mipmap.egg1);
//        list.add(R.mipmap.egg2);
//        list.add(R.mipmap.egg3);
//        list.add(R.mipmap.egg3);
//        list.add(R.mipmap.egg3);
//        list.add(R.mipmap.egg1);
//        list.add(R.mipmap.egg2);
//        list.add(R.mipmap.egg3);
//        list.add(R.mipmap.egg3);
//        atlas_all_layout = (LinearLayout) findViewById(R.id.atlas_all_layout);
//        int height = DensityUtil.getScreenWidth(mContext,1)/9;
//        ViewGroup.LayoutParams params = atlas_all_layout.getLayoutParams();
//        atlas_all_layout.setLayoutParams(params);
//        atlas_all_layout.removeAllViews();
//        atlas_all_layout.getTop();
//        LogUtils.d("AtlasDialog+++"+atlas_all_layout.getTop());
//        if (GlobalValues.PROJECT_IMAGES!=null&&GlobalValues.PROJECT_IMAGES.size()>0)
//        for(int i =0;i<list.size();i++){
//            ImageView imageView = new ImageView(mContext);
//            atlas_all_layout.addView(imageView);
//            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
//            layoutParams.width = height;
//            layoutParams.height = height;
//            imageView.setLayoutParams(layoutParams);
//            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            imageView.setImageURI(Uri.fromFile(new File(GlobalValues.PROJECT_IMAGES.get(i))));
//            imageView.setImageResource(list.get(i));
//        }
        atlas_all_layout = (LinearLayout) findViewById(R.id.atlas_all_layout);
//        downloadedTipTV = (TextView) findViewById(R.id.downloaded_tip);
        dashboard_view = (DashboardView4) findViewById(R.id.dashboard_view);



    }

    public void initnum(final int num){

        ObjectAnimator animator = ObjectAnimator.ofInt(dashboard_view, "mRealTimeValue",
                dashboard_view.getVelocity(), 10);
        animator.setDuration(1500).setInterpolator(new LinearInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimFinished = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimFinished = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimFinished = true;
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // int value = (int) animation.getAnimatedValue();
                int pos = num;
                int value = 0;
                switch (pos){
                    case 1:
                        value = 10;
                        break;
                    case 2:
                        value = 30;
                        break;
                    case 3:
                        value = 50;
                        break;
                    case 4:
                        value = 70;
                        break;
                    case 5:
                        value = 90;
                        break;
                    case 6:
                        value = 120;
                        break;
                    case 7:
                        value = 140;
                        break;
                    case 8:
                        value = 160;
                        break;
                    case 9:
                        value = 180;
                        break;
                     default:
                         break;
                }

                dashboard_view.setVelocity(value);
            }
        });
        animator.start();
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
            dashboard_view.post(new Runnable() {
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
        dashboard_view.setVisibility(View.VISIBLE);
        dashboard_view.startAnimation(animation);
        isShowTip = true;
    }

    public void projectTipAnimateOut() {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, -1,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        dashboard_view.startAnimation(animation);
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

        void upadteDownlaodedNum(int allNum,int currentNum);
    }
}

package com.savor.ads.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.savor.ads.R;
import com.savor.ads.adapter.SignInUserAdapter;
import com.savor.ads.bean.PartakeUser;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;

import java.util.ArrayList;
import java.util.List;

public class MeetingSignInActivity extends BaseActivity{
    public static final String TAG = MeetingSignInActivity.class.getSimpleName();
    private Context context;
    private RelativeLayout meetingSignInLayout;
    private TextView meetingCompanyNameTV;
    private GridView signInListGV;
    private TextView mCountDownTv;
    private TextView signInNumTV;
    private LinearLayout signInQrcodeLayout;
    private ImageView qrCodeSignInIV;
    private SignInUserAdapter signInUserAdapter;
    private ArrayList<String> signInList = new ArrayList();

    private String bgImgPath;
    private int delayTime;
    private String company_name;
    private String codeUrl;
    private List<PartakeUser> users;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_meeting_sign_in);
        initViews();
        getIntentData();
        initData();
    }

    private void initViews(){
        meetingSignInLayout = findViewById(R.id.meeting_sign_layout);
        meetingCompanyNameTV = findViewById(R.id.tv_meeting_company_name);
        signInListGV = findViewById(R.id.gv_sign_in_list);
        signInQrcodeLayout = findViewById(R.id.sign_in_qrcode_layout);
        qrCodeSignInIV = findViewById(R.id.iv_qrcode_sign_in);
        mCountDownTv = findViewById(R.id.tv_sign_in_countdown);
        signInNumTV = findViewById(R.id.tv_sign_in_num);
    }
    private void getIntentData(){
        Intent intent = getIntent();
        bgImgPath = intent.getStringExtra("bgImgPath");
        delayTime = intent.getIntExtra("countdown",10);
        company_name = intent.getStringExtra("company_name");
        codeUrl = intent.getStringExtra("codeUrl");
        users = (ArrayList<PartakeUser>) intent.getSerializableExtra("users");
    }
    private void initData() {
        if (!TextUtils.isEmpty(bgImgPath)){
            meetingSignInLayout.setBackground(Drawable.createFromPath(bgImgPath));
        }
        mCountDownTv.setText("剩余"+delayTime+"分钟");
        meetingCompanyNameTV.setText(company_name);
        mHandler.postDelayed(mCountDownRunnable,1000*60);
        if (!TextUtils.isEmpty(codeUrl)){
            ViewGroup.LayoutParams layoutParams = signInQrcodeLayout.getLayoutParams();
            layoutParams.width = DensityUtil.dip2px(mContext, 188);
            layoutParams.height =DensityUtil.dip2px(mContext, 188*1.2f);
            signInQrcodeLayout.setLayoutParams(layoutParams);
            GlideImageLoader.loadImage(mContext,codeUrl,qrCodeSignInIV);
        }
        signInUserAdapter = new SignInUserAdapter(context,signInList);
        signInListGV.setAdapter(signInUserAdapter);
        if (users!=null&&users.size()>0){
            signInUserAdapter.setSignInUser(users);
            signInNumTV.setText("已签到"+users.size()+"人");
            signInNumTV.setVisibility(View.VISIBLE);
        }else{
            signInNumTV.setVisibility(View.GONE);
        }
    }

    public void setSignInUsersData(List<PartakeUser> listUser){
        this.users = listUser;
        if (users!=null&&users.size()>0){
            signInUserAdapter.setSignInUser(users);
            signInNumTV.setText("已签到"+users.size()+"人");
            signInNumTV.setVisibility(View.VISIBLE);
        }else{
            signInNumTV.setVisibility(View.GONE);
        }
    }

    //倒计时线程
    private Runnable mCountDownRunnable = () -> wxPayCountDown();
    private void wxPayCountDown(){
        delayTime = delayTime-1;
        mCountDownTv.setText("剩余"+delayTime+"分钟");
        if (delayTime<=0){
            mHandler.removeCallbacks(mCountDownRunnable);
            finish();
        }else {
            mHandler.postDelayed(mCountDownRunnable,1000*60);
        }
    }

    public void stop(){
        mHandler.removeCallbacks(mCountDownRunnable);
        finish();
    }
}

package com.savor.ads.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.savor.ads.BuildConfig;
import com.savor.ads.R;
import com.savor.ads.adapter.PartakeDishDrawAdapter;
import com.savor.ads.bean.PartakeDishBean;
import com.savor.ads.bean.PartakeLottery;
import com.savor.ads.bean.PartakeUser;
import com.savor.ads.utils.Base64Utils;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.StrokeTextView;

import java.util.ArrayList;
import java.util.List;

public class PartakeDishDrawActivity extends BaseActivity{
    public static final String TAG = PartakeDishDrawActivity.class.getSimpleName();
    private Activity mActivity;
    private RecyclerView recyclerView;
    private RelativeLayout lotteryPersonLayout;
    private ImageView lotteryPersonImgIV;
    private TextView lotteryPersonnameTV;
    private ImageView crownIV;
    private StrokeTextView lotteryDescTV;
    private ImageView lotteryimgIV;
    private TextView lotteryNameTV;
    private PartakeDishBean partakeDishBean;
    private Handler handler = new Handler();
    private List<PartakeUser> partakeUsers;
    private PartakeLottery lottery;
    /**开奖时候，设定头像转头时间*/
    private boolean stopRotation=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_partake_dish_draw);
        initViews();
        getIntentData();
        initData();
        initRecyclerView(recyclerView, new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, true));
    }


    private void initViews(){
        recyclerView=findViewById(R.id.list_horizontal);
        lotteryPersonLayout = findViewById(R.id.lottery_person_layout);
        lotteryPersonImgIV = findViewById(R.id.lottery_person_img);
        lotteryPersonnameTV = findViewById(R.id.lottery_personname);
        crownIV = findViewById(R.id.crown);
        lotteryDescTV = findViewById(R.id.lottery_desc);
        lotteryimgIV = findViewById(R.id.lottery_img);
        lotteryNameTV = findViewById(R.id.lottery_name);

    }
    private void getIntentData(){
        partakeDishBean = (PartakeDishBean) getIntent().getSerializableExtra("pdb");
    }
    private void initData() {
        partakeUsers=new ArrayList<>();
        if (partakeDishBean!=null
                &&partakeDishBean.getPartake_user()!=null
                &&partakeDishBean.getPartake_user().size()>0){
            if (partakeDishBean.getPartake_user()!=null&&partakeDishBean.getPartake_user().size()>0){
                partakeUsers =partakeDishBean.getPartake_user();
            }
            lottery = partakeDishBean.getLottery();
            if (lottery!=null){
                if (!TextUtils.isEmpty(lottery.getDish_image())){
                    String dishImg = BuildConfig.OSS_ENDPOINT+lottery.getDish_image();
                    GlideImageLoader.loadImage(mContext,dishImg,lotteryimgIV);
                }
                if (!TextUtils.isEmpty(lottery.getDish_name())){
                    lotteryNameTV.setText(lottery.getDish_name());
                }
            }
        }
    }

    int itemCount = 0;
    public void initRecyclerView(final RecyclerView recyclerView, final CarouselLayoutManager layoutManager) {

        final PartakeDishDrawAdapter adapter= new PartakeDishDrawAdapter(mActivity,partakeUsers);
        itemCount = adapter.getItemCount();

        // enable zoom effect. this line can be customized
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        layoutManager.setMaxVisibleItems(8);

        recyclerView.setLayoutManager(layoutManager);
        // we expect only fixed sized item for now
        recyclerView.setHasFixedSize(true);
        // sample adapter with random data
        recyclerView.setAdapter(adapter);
        // enable center post scrolling
        recyclerView.addOnScrollListener(new CenterScrollListener());
        layoutManager.addOnItemSelectionListener(new CarouselLayoutManager.OnCenterItemSelectionListener() {

            @Override
            public void onCenterItemChanged(final int adapterPosition) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //5.0以下的手机会不会主动刷新到界面,需要调用此方法
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }, 50);
            }
        });

        adapter.setLayoutManager(layoutManager);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRotation = true;
            }
        },1000*15);
        autoRoll();
    }


    int currentItem=0;
    private Runnable autoRollRunnable = ()->autoRoll();

    private void autoRoll(){

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentItem==itemCount){
                    currentItem =0;
                }
                recyclerView.scrollToPosition(currentItem%itemCount);
//                recyclerView.scrollBy(20,0);
            }
        },1000);
        PartakeUser user = partakeUsers.get(currentItem);
        if (user!=null&&stopRotation&&user.getIs_lottery()==1){
            handler.removeCallbacks(autoRollRunnable);
            recyclerView.setVisibility(View.GONE);
            lotteryPersonLayout.setVisibility(View.VISIBLE);
            lotteryPersonnameTV.setText(user.getNickName());
            String avatarUrl = Base64Utils.getFromBase64(user.getAvatarUrl());
            GlideImageLoader.loadImage(mContext,avatarUrl,lotteryPersonImgIV,R.mipmap.avatar,R.mipmap.avatar);
            crownIV.setVisibility(View.VISIBLE);
            lotteryDescTV.setText("恭喜"+"\""+user.getNickName()+"\""+"获得本轮奖品~");
            lotteryDescTV.setVisibility(View.VISIBLE);
            handler.postDelayed(()->finish(),1000*60);
        }else{
            currentItem ++;
            handler.postDelayed(autoRollRunnable,500);
        }
    }
}

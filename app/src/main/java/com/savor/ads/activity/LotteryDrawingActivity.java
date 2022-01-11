package com.savor.ads.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.savor.ads.R;
import com.savor.ads.adapter.PartakeDishDrawAdapter;
import com.savor.ads.bean.LotteryResult;
import com.savor.ads.bean.PartakeDishBean;
import com.savor.ads.bean.PartakeLottery;
import com.savor.ads.bean.PartakeUser;
import com.savor.ads.customview.StrokeTextView;
import com.savor.ads.service.MiniProgramNettyService;
import com.savor.ads.utils.Base64Utils;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LotteryDrawingActivity extends BaseActivity{
    public static final String TAG = LotteryDrawingActivity.class.getSimpleName();
    private Activity mActivity;
    private LinearLayout luckyUserContentLayout;
    private LinearLayout luckyUserLayout;
    private RecyclerView recyclerView;
    private RelativeLayout lotteryPersonLayout;
    private ImageView lotteryPersonImgIV;
    private TextView lotteryPersonnameTV;
    private ImageView crownIV;
    private StrokeTextView lotteryDescTV;
    private StrokeTextView lotteryTipTV;
    private int action;
    private String content;
    private LotteryResult lotteryResult;
    private List<PartakeUser> lotteryUsers;
    private List<PartakeLottery> winPrizeUsers;
    private Map<String,Integer> winPrizeUsersMap;
    private Handler handler = new Handler();
    /**开奖时候，设定头像转头时间*/
    private boolean stopRotation=false;
    private MiniProgramNettyService miniProgramNettyService;
    private MiniProgramNettyService.AdsBinder adsBinder;
    private ServiceConnection mNettyConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_lottery_drawing);
        initViews();
        getIntentData();
        initData();
        if (action!=157){
            initRecyclerView(recyclerView, new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, true));
        }
        bindMiniprogramNettyService();
    }

    /**
     * 绑定netty服务
     */
    private void bindMiniprogramNettyService(){
        mNettyConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                adsBinder = (MiniProgramNettyService.AdsBinder) binder;
                if (adsBinder!=null){
                    miniProgramNettyService = adsBinder.getService();
                    LogUtils.d(miniProgramNettyService+"123");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(mContext, MiniProgramNettyService.class);
        bindService(intent,mNettyConnection, Context.BIND_AUTO_CREATE);
    }

    private void initViews(){
        luckyUserContentLayout = findViewById(R.id.lucky_user_content_layout);
        luckyUserLayout = findViewById(R.id.lucky_user_layout);
        recyclerView=findViewById(R.id.list_horizontal);
        lotteryPersonLayout = findViewById(R.id.lottery_person_layout);
        lotteryPersonImgIV = findViewById(R.id.lottery_person_img);
        lotteryPersonnameTV = findViewById(R.id.lottery_personname);
        crownIV = findViewById(R.id.crown);
        lotteryDescTV = findViewById(R.id.lottery_desc);
        lotteryTipTV = findViewById(R.id.lottery_tip);

    }
    private void getIntentData(){
        Intent intent = getIntent();
        action = intent.getIntExtra("action",0);
        content = intent.getStringExtra("content");
        if (action!=157){
            lotteryResult = (LotteryResult) intent.getSerializableExtra("lottery");
            if (lotteryResult!=null){
                if (lotteryResult.getPartake_user()!=null&&lotteryResult.getPartake_user().size()>0){
                    lotteryUsers = lotteryResult.getPartake_user();
                }
                if (lotteryResult.getLottery()!=null&&lotteryResult.getLottery().size()>0){
                    winPrizeUsers = lotteryResult.getLottery();
                }
            }
        }
    }
    private void initData() {
        GlobalValues.isPrize = false;
        luckyUserLayout.removeAllViews();
        if (winPrizeUsers!=null&&winPrizeUsers.size()>0){
            winPrizeUsersMap = new HashMap<>();
            for (PartakeLottery lottery:winPrizeUsers){
                winPrizeUsersMap.put(lottery.getOpenid(),lottery.getLevel());
            }
        }
        if (action==157&&!TextUtils.isEmpty(content)){
            luckyUserContentLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            lotteryPersonLayout.setVisibility(View.GONE);
            lotteryTipTV.setText(content);
            lotteryTipTV.setVisibility(View.VISIBLE);
            handler.postDelayed(()->finish(),1000*15);
        }
    }

    int itemCount = 0;
    public void initRecyclerView(final RecyclerView recyclerView, final CarouselLayoutManager layoutManager) {

        final PartakeDishDrawAdapter adapter= new PartakeDishDrawAdapter(mActivity,lotteryUsers);
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
        handler.postDelayed(()->stopRotation = true,1000*15);
        autoRoll();
    }

    int currentItem=0;
    private Runnable autoRollRunnable = ()->autoRoll();

    private void autoRoll(){
        if (currentItem==itemCount){
            currentItem =0;
        }
        handler.postDelayed(()->{
                recyclerView.scrollToPosition(currentItem%itemCount);
//                recyclerView.scrollBy(20,0);
        },1000);
        PartakeUser user = lotteryUsers.get(currentItem);
        if (user!=null&&stopRotation&&user.getIs_lottery()==1){
            handler.removeCallbacks(autoRollRunnable);
            recyclerView.setVisibility(View.GONE);
            lotteryPersonLayout.setVisibility(View.VISIBLE);
            lotteryPersonnameTV.setText(user.getNickName());
            String avatarUrl = Base64Utils.getFromBase64(user.getAvatarUrl());
            GlideImageLoader.loadImage(mContext,avatarUrl,lotteryPersonImgIV,R.mipmap.avatar,R.mipmap.avatar);
            crownIV.setVisibility(View.VISIBLE);
            String openid =user.getOpenid();
            int level = winPrizeUsersMap.get(openid);
            if (level>0){
                lotteryDescTV.setText("恭喜"+"\""+user.getNickName()+"\""+"获得"+level+"等奖~");
            }else{
                lotteryDescTV.setText("恭喜"+"\""+user.getNickName()+"\""+"获奖~");
            }
            lotteryDescTV.setVisibility(View.VISIBLE);
            displayLuckyUserAvatar(avatarUrl);
            lotteryUsers.get(currentItem).setIs_lottery(-1);
            if (luckyUserLayout.getChildCount()<winPrizeUsers.size()){
                handler.postDelayed(()->{
                    recyclerView.setVisibility(View.VISIBLE);
                    lotteryPersonLayout.setVisibility(View.GONE);
                    stopRotation = false;
                    lotteryDescTV.setVisibility(View.GONE);
                    currentItem = 0;
                    handler.postDelayed(()->stopRotation = true,1000*15);
                    autoRoll();
                },1000*5);
            }else{
                handler.postDelayed(()-> {
                    Intent intent = new Intent(mContext,LotteryDrawResultActivity.class);
                    intent.putExtra("lotteryUsers", (Serializable) lotteryUsers);
                    intent.putExtra("winPrizeUsers", (Serializable) winPrizeUsers);
                    startActivity(intent);
                    finish();
                },1000*10);
            }
        }else{
            currentItem ++;
            handler.postDelayed(autoRollRunnable,500);
        }
    }

    /**
     * 展示中奖者的头像
     * */
    private void displayLuckyUserAvatar(String avatarUrl){
        View itemView = View.inflate(mContext,R.layout.item_user_avatar,null);
        ImageView userAvatarIV = itemView.findViewById(R.id.user_avatar);
        GlideImageLoader.loadImage(mContext,avatarUrl,userAvatarIV,R.mipmap.avatar,R.mipmap.avatar);
        luckyUserLayout.addView(itemView);
    }

    @Override
    protected void onDestroy() {
        if (mNettyConnection!=null){
            unbindService(mNettyConnection);
        }
        super.onDestroy();
    }
}

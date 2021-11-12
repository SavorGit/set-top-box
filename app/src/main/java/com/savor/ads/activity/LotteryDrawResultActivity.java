package com.savor.ads.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.savor.ads.BuildConfig;
import com.savor.ads.R;
import com.savor.ads.bean.LotteryResult;
import com.savor.ads.bean.PartakeLottery;
import com.savor.ads.bean.PartakeUser;
import com.savor.ads.utils.Base64Utils;
import com.savor.ads.utils.GlideImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LotteryDrawResultActivity extends BaseActivity{
    public static final String TAG = LotteryDrawResultActivity.class.getSimpleName();
    private Context context;
    private LinearLayout allPrizeLayout;
    private List<PartakeUser> lotteryUsers;
    private List<PartakeLottery> winPrizeUsers;
    private Map<Integer,Object> winPrizeUserMap;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_lottery_draw_result);
        initViews();
        getIntentData();
        initData();
    }


    private void initViews(){
        allPrizeLayout = findViewById(R.id.all_prize_layout);

    }
    private void getIntentData(){
        Intent intent = getIntent();
        lotteryUsers = (List<PartakeUser>) intent.getSerializableExtra("lotteryUsers");
        winPrizeUsers = (List<PartakeLottery>) intent.getSerializableExtra("winPrizeUsers");
    }
    private void initData() {
        allPrizeLayout.removeAllViews();
        if (lotteryUsers!=null&&lotteryUsers.size()>0&&winPrizeUsers!=null&&winPrizeUsers.size()>0){
            for (PartakeLottery lottery:winPrizeUsers){
                String openid = lottery.getOpenid();
                for (PartakeUser user:lotteryUsers){
                    if (openid.equals(user.getOpenid())){
                        lottery.setAvatarUrl(user.getAvatarUrl());
                        lottery.setNickName(user.getNickName());
                    }
                }
            }
            winPrizeUserMap = new HashMap<>();
            List<PartakeLottery> prizeLevel1 = new ArrayList<>();
            List<PartakeLottery> prizeLevel2 = new ArrayList<>();
            List<PartakeLottery> prizeLevel3 = new ArrayList<>();
            for (PartakeLottery lottery:winPrizeUsers){
                switch (lottery.getLevel()){
                    case 1:
                        prizeLevel1.add(lottery);
                        break;
                    case 2:
                        prizeLevel2.add(lottery);
                        break;
                    case 3:
                        prizeLevel3.add(lottery);
                        break;
                }
            }
            winPrizeUserMap.put(1,prizeLevel1);
            winPrizeUserMap.put(2,prizeLevel2);
            winPrizeUserMap.put(3,prizeLevel3);
            displayPrizeInfo();
        }
    }


    private void displayPrizeInfo(){
        if (winPrizeUserMap!=null&&winPrizeUserMap.size()>0){
            List<PartakeLottery> prizeLevel1 = (List<PartakeLottery>) winPrizeUserMap.get(1);
            if (prizeLevel1!=null&&prizeLevel1.size()>0){
                displayLuckyUser(prizeLevel1);
            }
            List<PartakeLottery> prizeLevel2 = (List<PartakeLottery>) winPrizeUserMap.get(2);
            if (prizeLevel2!=null&&prizeLevel2.size()>0){
                displayLuckyUser(prizeLevel2);
            }
            List<PartakeLottery> prizeLevel3 = (List<PartakeLottery>) winPrizeUserMap.get(3);
            if (prizeLevel3!=null&&prizeLevel3.size()>0){
                displayLuckyUser(prizeLevel3);
            }
        }
        handler.postDelayed(()->finish(),1000*60);
    }

    private void displayLuckyUser(List<PartakeLottery> prizeLevel){
        PartakeLottery lottery = prizeLevel.get(0);
        View prizeView = View.inflate(mContext,R.layout.item_prize_layout,null);
        ImageView prizeImgIV = prizeView.findViewById(R.id.prize_img);
        TextView prizeLevelTV = prizeView.findViewById(R.id.prize_level);
        TextView prizeNameTV = prizeView.findViewById(R.id.prize_name);
        String prizeImgUrl = BuildConfig.OSS_ENDPOINT+lottery.getDish_image();
        GlideImageLoader.loadImage(mContext, prizeImgUrl,prizeImgIV,0,0);
        int level = lottery.getLevel();
        if (level==1){
            prizeLevelTV.setText("一等奖");
        }else if (level==2){
            prizeLevelTV.setText("二等奖");
        }else if (level==3){
            prizeLevelTV.setText("三等奖");
        }
        prizeNameTV.setText(lottery.getDish_name());
        for (PartakeLottery user:prizeLevel){
            LinearLayout luckyUserAllLayout = prizeView.findViewById(R.id.lucky_user_all_layout);
            View luckyUserView = View.inflate(mContext,R.layout.item_lucky_user_layout,null);
            ImageView userAvatarImgIV = luckyUserView.findViewById(R.id.user_avatar_img);
            TextView userNicknameTV = luckyUserView.findViewById(R.id.user_nickname);
            TextView roomNameTV = luckyUserView.findViewById(R.id.room_name);
            String avatarUrl = Base64Utils.getFromBase64(user.getAvatarUrl());
            GlideImageLoader.loadImage(mContext,avatarUrl,userAvatarImgIV,R.mipmap.avatar,R.mipmap.avatar);
            userNicknameTV.setText(user.getNickName());
            roomNameTV.setText("("+user.getRoom_name()+")");
            luckyUserAllLayout.addView(luckyUserView);
        }
        allPrizeLayout.addView(prizeView);
    }
}

package com.savor.ads.customview;

import android.content.Context;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.savor.ads.R;
import com.savor.ads.activity.MonkeyGameActivity.WinningListener;

/**
 * Created by jeanboy on 2017/4/20.
 */

public class LuckyMonkeyPanelView extends FrameLayout {


    private ImageView bg_1;
    private ImageView bg_2;
    private WinningListener winningListener;
    private PanelItemView itemView1, itemView2, itemView3,
            itemView4, itemView5, itemView6,
            itemView7, itemView8,itemView9;

    private ItemView[] itemViewArr = new ItemView[9];
    private int currentIndex = 0;
    private int currentTotal = 0;
    private int stayIndex = 0;

    private boolean isMarqueeRunning = false;
    private boolean isGameRunning = false;
    private boolean isTryToStop = false;

    private static final int DEFAULT_SPEED = 150;
    private static final int MIN_SPEED = 50;
    private int currentSpeed = DEFAULT_SPEED;

    public LuckyMonkeyPanelView(@NonNull Context context) {
        this(context, null);
    }

    public LuckyMonkeyPanelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LuckyMonkeyPanelView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_lucky_mokey_panel, this);
        setupView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startMarquee();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopMarquee();
        super.onDetachedFromWindow();
    }

    private void setupView() {
        bg_1 = (ImageView) findViewById(R.id.bg_1);
        bg_2 = (ImageView) findViewById(R.id.bg_2);
        itemView1 = (PanelItemView) findViewById(R.id.item1);
        itemView2 = (PanelItemView) findViewById(R.id.item2);
        itemView3 = (PanelItemView) findViewById(R.id.item3);
        itemView4 = (PanelItemView) findViewById(R.id.item4);
        itemView5 = (PanelItemView) findViewById(R.id.item5);
        itemView6 = (PanelItemView) findViewById(R.id.item6);
        itemView7 = (PanelItemView) findViewById(R.id.item7);
        itemView8 = (PanelItemView) findViewById(R.id.item8);
        itemView9 = (PanelItemView) findViewById(R.id.item9);

        itemViewArr[0] = itemView4;
        itemViewArr[1] = itemView1;
        itemViewArr[2] = itemView2;
        itemViewArr[3] = itemView3;
        itemViewArr[4] = itemView6;
        itemViewArr[5] = itemView9;
        itemViewArr[6] = itemView8;
        itemViewArr[7] = itemView7;
        itemViewArr[8] = itemView5;

    }

    private void stopMarquee() {
        isMarqueeRunning = false;
        isGameRunning = false;
        isTryToStop = false;
    }

    private void startMarquee() {
        isMarqueeRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isMarqueeRunning) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    post(new Runnable() {
                        @Override
                        public void run() {
                            if (bg_1 != null && bg_2 != null) {
                                if (VISIBLE == bg_1.getVisibility()) {
                                    bg_1.setVisibility(GONE);
                                    bg_2.setVisibility(VISIBLE);
                                } else {
                                    bg_1.setVisibility(VISIBLE);
                                    bg_2.setVisibility(GONE);
                                }
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private long getInterruptTime() {
        currentTotal++;
        if (isTryToStop) {
            currentSpeed += 10;
            if (currentSpeed > DEFAULT_SPEED) {
                currentSpeed = DEFAULT_SPEED;
            }
        } else {
            if (currentTotal / itemViewArr.length > 0) {
                currentSpeed -= 10;
            }
            if (currentSpeed < MIN_SPEED) {
                currentSpeed = MIN_SPEED;
            }
        }
        return currentSpeed;
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public void startGame() {
        isGameRunning = true;
        isTryToStop = false;
        currentSpeed = DEFAULT_SPEED;
        postDelayed(goRunnable,getInterruptTime());
    }


    private Runnable goRunnable = new Runnable() {
        @Override
        public void run() {
            int preIndex = currentIndex;
            currentIndex++;
            if (currentIndex >= itemViewArr.length) {
                currentIndex = 0;
            }

            itemViewArr[preIndex].setFocus(false);
            itemViewArr[currentIndex].setFocus(true);

            if (isTryToStop && currentSpeed == DEFAULT_SPEED && stayIndex == currentIndex) {
//                isGameRunning = false;
                Log.d("currentIndex","currentIndex==="+currentIndex);
                if (winningListener!=null){
                    winningListener.setWinningWeixinHead(itemViewArr[currentIndex].getImageViewDrawable());
                    Log.d("stayIndex","stayIndex==========="+stayIndex);
                }
            }else{
                postDelayed(goRunnable,getInterruptTime());
            }
        }
    };



    public void tryToStop(int position) {
        stayIndex = position;
        isTryToStop = true;

    }


    public void setWinningListener(WinningListener listener){
        this.winningListener = listener;
    }



    public void setImageViewSrc(int action,String uri){
        switch (action){
            case 1:
                itemView4.setImageViewSrc(uri);
                break;
            case 2:
                itemView1.setImageViewSrc(uri);
                break;
            case 3:
                itemView2.setImageViewSrc(uri);
                break;
            case 4:
                itemView3.setImageViewSrc(uri);
                break;
            case 5:
                itemView6.setImageViewSrc(uri);
                break;
            case 6:
                itemView9.setImageViewSrc(uri);
                break;
            case 7:
                itemView8.setImageViewSrc(uri);
                break;
            case 8:
                itemView7.setImageViewSrc(uri);
                break;
            case 9:
                itemView5.setImageViewSrc(uri);
                break;
        }
    }
}

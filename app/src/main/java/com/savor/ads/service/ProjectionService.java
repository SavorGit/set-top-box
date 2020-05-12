package com.savor.ads.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.bean.MiniProgramProjection;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.core.Session;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.GlobalValues;

public class ProjectionService extends Service {
    private Context context;
    private Session session;
    private Handler mHandler = new Handler();
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        session = Session.get(context);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loopProjection();
        return super.onStartCommand(intent, flags, startId);
    }

    private void loopProjection(){
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (GlobalValues.mpprojection!=null&&activity instanceof AdsPlayerActivity){
            long nowTime = System.currentTimeMillis();
            long diffTime =nowTime-GlobalValues.loopStartTime;
            float hour = diffTime/1000/60/60;
            if (hour>2){
                GlobalValues.mpprojection = null;
                GlobalValues.WELCOME_ID = 0;
                return;
            }
            MiniProgramProjection mpprojection = GlobalValues.mpprojection;
            String imgPath = mpprojection.getImg_path();
            String musicPath = mpprojection.getMusic_path();
            String forscreen_char = mpprojection.getForscreen_char();
            String wordsize = mpprojection.getWordsize();
            String wordcolor = mpprojection.getColor();
            String fontPath = mpprojection.getFont_path();
            int rotation = mpprojection.getRotation();
            String waiterIconUrl = mpprojection.getWaiterIconUrl();
            String waiterName = mpprojection.getWaiterName();
            int play_times = 0;
            ProjectOperationListener.getInstance(context).showRestImage(7,imgPath,rotation,musicPath,forscreen_char,wordsize,wordcolor,fontPath,waiterIconUrl,waiterName,play_times,GlobalValues.FROM_SERVICE_MINIPROGRAM);
        }

        mHandler.postDelayed(mProjectionRunnable,1000*60*5+1000 * 10);
    }

    private Runnable mProjectionRunnable = ()->loopProjection();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

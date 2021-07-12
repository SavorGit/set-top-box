package com.savor.ads.projection.action;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.savor.ads.activity.ScreenProjectionActivity;
import com.savor.ads.projection.ProjectPriority;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.LogUtils;

import java.io.Serializable;

/**
 * Created by zhang.haiqiang on 2017/5/22.
 */

public class VideoAction extends ProjectionActionBase implements Serializable {

    private transient Context mContext;
    private String videoPath;
    private String videoUrl;
    private boolean isNewDevice;
    private String forscreenId;
    private String price;
    private int storeSale;
    private String avatarUrl;
    private String nickname;
    private String delayTime;
    private int action;
    public VideoAction(Context context, String videoPath,String videoUrl,boolean isNewDevice,int currentAction,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.mContext = context;
        this.videoPath = videoPath;
        this.videoUrl = videoUrl;
        this.isNewDevice = isNewDevice;
        this.action = currentAction;
        this.fromService = fromService;
    }

    public VideoAction(Context context, String videoPath,String videoUrl, boolean isNewDevice,String forscreenId,String avatarUrl,String nickname,int action,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        mContext = context;
        this.videoPath = videoPath;
        this.videoUrl = videoUrl;
        this.isNewDevice = isNewDevice;
        this.forscreenId = forscreenId;
        this.avatarUrl = avatarUrl;
        this.nickname = nickname;
        this.action = action;
    }

    public VideoAction(Context context, String videoPath, boolean isNewDevice,String forscreenId,String avatarUrl,String nickname,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        mContext = context;
        this.videoPath = videoPath;
        this.videoUrl = videoUrl;
        this.isNewDevice = isNewDevice;
        this.forscreenId = forscreenId;
        this.avatarUrl = avatarUrl;
        this.nickname = nickname;
    }

    public VideoAction(Context context, String videoPath, boolean isNewDevice,String forscreenId,String avatarUrl,String nickname,String delayTime,int action,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        mContext = context;
        this.videoPath = videoPath;
        this.isNewDevice = isNewDevice;
        this.forscreenId = forscreenId;
        this.avatarUrl = avatarUrl;
        this.nickname = nickname;
        this.delayTime = delayTime;
        this.action = action;
    }

    public VideoAction(Context context, String videoPath, boolean isNewDevice,String price,int storeSale,String delayTime,int action,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        mContext = context;
        this.videoPath = videoPath;
        this.isNewDevice = isNewDevice;
        this.price = price;
        this.storeSale = storeSale;
        this.delayTime = delayTime;
        this.action = action;
    }

    @Override
    public void execute() {
        onActionBegin();

        // 跳转或将参数设置到ScreenProjectionActivity
        Bundle data = new Bundle();
        data.putString(ScreenProjectionActivity.EXTRA_PATH, videoPath);
        data.putString(ScreenProjectionActivity.EXTRA_URL, videoUrl);
        data.putString(ScreenProjectionActivity.EXTRA_TYPE, ConstantValues.PROJECT_TYPE_VIDEO);
        data.putBoolean(ScreenProjectionActivity.EXTRA_IS_NEW_DEVICE, isNewDevice);
        data.putString(ScreenProjectionActivity.EXTRA_FORSCREEN_ID, forscreenId);
        data.putString(ScreenProjectionActivity.EXTRA_PRICE_ID, price);
        data.putInt(ScreenProjectionActivity.EXTRA_STORE_SALE_ID, storeSale);
        data.putString(ScreenProjectionActivity.EXTRA_DELAY_TIME_ID, delayTime);
        data.putInt(ScreenProjectionActivity.EXTRA_ACTION_ID, action);
        data.putInt(ScreenProjectionActivity.EXTRA_FROM_SERVICE_ID, fromService);
        data.putString(ScreenProjectionActivity.EXTRA_AVATAR_URL,avatarUrl);
        data.putString(ScreenProjectionActivity.EXTRA_NICKNAME,nickname);
        data.putSerializable(ScreenProjectionActivity.EXTRA_PROJECT_ACTION, this);
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof ScreenProjectionActivity && !((ScreenProjectionActivity) activity).isBeenStopped()) {
            LogUtils.d("Listener will setNewProjection");
            ((ScreenProjectionActivity) activity).setNewProjection(data);
        } else {
            if (activity == null) {
                LogUtils.d("Listener will startActivity in new task");
                Intent intent = new Intent(mContext, ScreenProjectionActivity.class);
                intent.putExtras(data);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {
                LogUtils.d("Listener will startActivity in " + activity);
                Intent intent = new Intent(activity, ScreenProjectionActivity.class);
                intent.putExtras(data);
                activity.startActivity(intent);
            }
        }
    }

    @Override
    public String toString() {
        return "VideoAction{" +
                "videoPath='" + videoPath + '\'' +
                '}';
    }
}

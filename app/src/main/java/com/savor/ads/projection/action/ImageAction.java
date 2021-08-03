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

public class ImageAction extends ProjectionActionBase implements Serializable {
    private transient Context mContext;
    private int imageType;
    private String imagePath;
    private String projectionWords;
    private int rotation;
    private boolean isThumbnail;
    private String seriesId;
    private String price;
    private int storeSale;
    private boolean isNewDevice;
    private String forscreenId;
    private String avatarUrl;
    private String nickname;
    /**倒计时时长*/
    private String delayTime;
    private String musicPath;
    private int action;


    public ImageAction(Context context, int imageType, String imagePath, boolean isThumbnail,String forscreenId,String projectionTime,String avatarUrl,String nickname,int action,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.mContext = context;
        this.imageType = imageType;
        this.imagePath = imagePath;
        this.isThumbnail = isThumbnail;
        this.forscreenId = forscreenId;
        this.delayTime = projectionTime;
        this.avatarUrl = avatarUrl;
        this.nickname = nickname;
        this.action = action;
        this.fromService = fromService;
    }


    public ImageAction(Context context, int imageType, String imagePath,boolean isThumbnail,String forscreenId,String words, String avatarUrl,String nickname,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        this.mContext = context;
        this.imageType = imageType;
        this.imagePath = imagePath;
        this.isThumbnail = isThumbnail;
        this.forscreenId = forscreenId;
        this.projectionWords = words;
        this.avatarUrl = avatarUrl;
        this.nickname = nickname;
    }

    public ImageAction(Context context, int imageType, String imagePath,boolean isThumbnail,String forscreenId,String words, String avatarUrl,String nickname,String delayTime,String musicPath,int action,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        mContext = context;
        this.imageType = imageType;
        this.imagePath = imagePath;
        this.isThumbnail = isThumbnail;
        this.forscreenId = forscreenId;
        this.projectionWords = words;
        this.avatarUrl = avatarUrl;
        this.nickname = nickname;
        this.delayTime = delayTime;
        this.musicPath = musicPath;
        this.action = action;

    }

    public ImageAction(Context context, int imageType, String imagePath,boolean isThumbnail,String price,int storeSale,String delayTime,int action,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        mContext = context;
        this.imageType = imageType;
        this.imagePath = imagePath;
        this.isThumbnail = isThumbnail;
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
        data.putString(ScreenProjectionActivity.EXTRA_TYPE, ConstantValues.PROJECT_TYPE_PICTURE);
        data.putString(ScreenProjectionActivity.EXTRA_IMAGE_PATH,imagePath);
        data.putInt(ScreenProjectionActivity.EXTRA_IMAGE_ROTATION, rotation);
        data.putBoolean(ScreenProjectionActivity.EXTRA_IS_THUMBNAIL, isThumbnail);
        data.putInt(ScreenProjectionActivity.EXTRA_IMAGE_TYPE, imageType);
        data.putString(ScreenProjectionActivity.EXTRA_MEDIA_ID, seriesId);
        data.putString(ScreenProjectionActivity.EXTRA_FORSCREEN_ID,forscreenId);
        data.putString(ScreenProjectionActivity.EXTRA_PRICE_ID,price);
        data.putInt(ScreenProjectionActivity.EXTRA_STORE_SALE_ID, storeSale);
        if (imageType==2){
            data.putInt(ScreenProjectionActivity.EXTRA_PROJECTION_TIME,Integer.valueOf(delayTime));
        }else {
            data.putString(ScreenProjectionActivity.EXTRA_DELAY_TIME_ID,delayTime);
        }
        data.putString(ScreenProjectionActivity.EXTRA_MUSIC_PATH,musicPath);
        data.putInt(ScreenProjectionActivity.EXTRA_ACTION_ID,action);
        data.putInt(ScreenProjectionActivity.EXTRA_FROM_SERVICE_ID, fromService);
        data.putString(ScreenProjectionActivity.EXTRA_PROJECTION_WORDS,projectionWords);
        data.putString(ScreenProjectionActivity.EXTRA_AVATAR_URL, avatarUrl);
        data.putString(ScreenProjectionActivity.EXTRA_NICKNAME, nickname);
        data.putBoolean(ScreenProjectionActivity.EXTRA_IS_NEW_DEVICE, isNewDevice);
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
        return "ImageAction{" +
                "imageType=" + imageType +
                ", rotation=" + rotation +
                ", isThumbnail=" + isThumbnail +
                ", seriesId='" + seriesId + '\'' +
                '}';
    }
}

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

public class ImageRestaurantAction extends ProjectionActionBase implements Serializable {
    private transient Context mContext;
    private int imageType;
    private String imagePath;
    private int rotation;
    private String projectionWords;
    private boolean isThumbnail;
    private String avatarUrl;
    private String nickname;
    private int projectionTime;

    public ImageRestaurantAction(Context context, int imageType, String imagePath,int rotation, boolean isThumbnail, String words, String avatarUrl, String nickname,int projectionTime,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        mContext = context;
        this.imageType = imageType;
        this.imagePath = imagePath;
        this.rotation = rotation;
        this.isThumbnail = isThumbnail;
        this.projectionWords = words;
        this.avatarUrl = avatarUrl;
        this.nickname = nickname;
        this.projectionTime = projectionTime;
    }

    @Override
    public void execute() {
        onActionBegin();

        // 跳转或将参数设置到ScreenProjectionActivity
        Bundle data = new Bundle();
        data.putString(ScreenProjectionActivity.EXTRA_TYPE, ConstantValues.PROJECT_TYPE_REST_PICTURE);
        data.putString(ScreenProjectionActivity.EXTRA_IMAGE_PATH,imagePath);
        data.putInt(ScreenProjectionActivity.EXTRA_IMAGE_ROTATION,rotation);
        data.putString(ScreenProjectionActivity.EXTRA_PROJECTION_WORDS,projectionWords);
        data.putInt(ScreenProjectionActivity.EXTRA_PROJECTION_TIME,projectionTime);
        data.putBoolean(ScreenProjectionActivity.EXTRA_IS_THUMBNAIL, isThumbnail);
        data.putInt(ScreenProjectionActivity.EXTRA_IMAGE_TYPE, imageType);
        data.putString(ScreenProjectionActivity.EXTRA_AVATAR_URL, avatarUrl);
        data.putString(ScreenProjectionActivity.EXTRA_NICKNAME, nickname);
        data.putInt(ScreenProjectionActivity.EXTRA_FROM_SERVICE_ID, fromService);
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
}

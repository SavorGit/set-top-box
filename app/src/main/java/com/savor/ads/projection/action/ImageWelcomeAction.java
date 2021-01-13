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

public class ImageWelcomeAction extends ProjectionActionBase implements Serializable {
    private transient Context mContext;
    private int imageType;
    private String imagePath;
    private boolean isThumbnail;
    private int rotation;
    private String musicPath;
    private String projectionWords;
    private String wordsize;
    private String color;
    private String fontPath;
    private String waiterIconUrl;
    private String waiterName;
    private int projectionTime;

    public ImageWelcomeAction(Context context, int imageType, boolean isThumbnail, String imagePath, String words, String wordsize, String color, String fontPath, int projectionTime, int fromService) {
        this(context, imageType,isThumbnail,imagePath, 0, "", words, wordsize, color, fontPath, projectionTime, fromService);
    }

    public ImageWelcomeAction(Context context, int imageType, boolean isThumbnail, String imagePath, int rotation, String musicPath, String words, String wordsize, String color, String fontPath, int projectionTime, int fromService) {
        super();
        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        this.mContext = context;
        this.imageType = imageType;
        this.isThumbnail = isThumbnail;
        this.imagePath = imagePath;
        this.rotation = rotation;
        this.musicPath = musicPath;
        this.projectionWords = words;
        this.wordsize = wordsize;
        this.color = color;
        this.fontPath = fontPath;
        this.projectionTime = projectionTime;
    }


    public ImageWelcomeAction(Context context, int imageType,String imagePath, int rotation, String musicPath, String words, String wordsize, String color, String fontPath, int projectionTime, int fromService) {
        super();
        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        this.mContext = context;
        this.imageType = imageType;
        this.imagePath = imagePath;
        this.rotation = rotation;
        this.musicPath = musicPath;
        this.projectionWords = words;
        this.wordsize = wordsize;
        this.color = color;
        this.fontPath = fontPath;
        this.projectionTime = projectionTime;
    }

    public ImageWelcomeAction(Context context, int imageType, String imagePath,int rotation, String musicPath, String words, String wordsize, String color, String fontPath, String waiterIconUrl, String waiterName,int projectionTime, int fromService) {
        super();
        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        this.mContext = context;
        this.imageType = imageType;
        this.imagePath = imagePath;
        this.rotation = rotation;
        this.musicPath = musicPath;
        this.projectionWords = words;
        this.wordsize = wordsize;
        this.color = color;
        this.fontPath = fontPath;
        this.waiterIconUrl = waiterIconUrl;
        this.waiterName = waiterName;
        this.projectionTime = projectionTime;
    }

    @Override
    public void execute() {
        onActionBegin();

        // 跳转或将参数设置到ScreenProjectionActivity
        Bundle data = new Bundle();
        if (imageType==9){
            data.putString(ScreenProjectionActivity.EXTRA_TYPE, ConstantValues.PROJECT_TYPE_BUSINESS_WELCOME);
        }else {
            data.putString(ScreenProjectionActivity.EXTRA_TYPE, ConstantValues.PROJECT_TYPE_WELCOME_PICTURE);
        }
        data.putInt(ScreenProjectionActivity.EXTRA_IMAGE_TYPE, imageType);
        data.putBoolean(ScreenProjectionActivity.EXTRA_IS_THUMBNAIL, isThumbnail);
        data.putString(ScreenProjectionActivity.EXTRA_IMAGE_PATH,imagePath);
        data.putString(ScreenProjectionActivity.EXTRA_MUSIC_PATH,musicPath);
        data.putInt(ScreenProjectionActivity.EXTRA_IMAGE_ROTATION,rotation);
        data.putString(ScreenProjectionActivity.EXTRA_WORD_SIZE,wordsize);
        data.putString(ScreenProjectionActivity.EXTRA_WORD_COLOR,color);
        data.putString(ScreenProjectionActivity.EXTRA_WORD_FONT_PATH,fontPath);
        data.putString(ScreenProjectionActivity.EXTRA_PROJECTION_WORDS,projectionWords);
        data.putString(ScreenProjectionActivity.EXTRA_WAITER_ICON_URL,waiterIconUrl);
        data.putString(ScreenProjectionActivity.EXTRA_WAITER_NAME,waiterName);
        data.putInt(ScreenProjectionActivity.EXTRA_PROJECTION_TIME,projectionTime);
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

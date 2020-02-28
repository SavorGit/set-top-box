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

public class VideoBirthdayAction extends ProjectionActionBase implements Serializable {

    private transient Context mContext;
    private String videoPath;
    private int position;
    private boolean isNewDevice;
    private String forscreenId;
    private int action;

    public VideoBirthdayAction(Context context, String videoPath, int position, String forscreenId,int action,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        mContext = context;
        this.videoPath = videoPath;
        this.position = position;
        this.forscreenId = forscreenId;
        this.action = action;
        this.fromService = fromService;
    }

    @Override
    public void execute() {
        onActionBegin();

        // 跳转或将参数设置到ScreenProjectionActivity
        Bundle data = new Bundle();
        data.putString(ScreenProjectionActivity.EXTRA_TYPE, ConstantValues.PROJECT_TYPE_VIDEO_BIRTHDAY);
        data.putString(ScreenProjectionActivity.EXTRA_URL, videoPath);
        data.putInt(ScreenProjectionActivity.EXTRA_VIDEO_POSITION, position);
        data.putString(ScreenProjectionActivity.EXTRA_FORSCREEN_ID, forscreenId);
        data.putInt(ScreenProjectionActivity.EXTRA_ACTION_ID, action);
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

    @Override
    public String toString() {
        return "VideoAction{" +
                "videoPath='" + videoPath + '\'' +
                ", position=" + position +
                '}';
    }
}

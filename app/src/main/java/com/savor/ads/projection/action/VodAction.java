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
public class VodAction extends ProjectionActionBase implements Serializable {

    private transient Context mContext;

    private String vid;
    private String url;
    private boolean isFromWeb;
    private boolean isNewDevice;
    private int action;

    public VodAction(Context context, String vid, String url,boolean isFromWeb, boolean isNewDevice,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        mContext = context;
        this.vid = vid;
        this.url = url;
        this.isFromWeb = isFromWeb;
        this.isNewDevice = isNewDevice;
        this.action = action;
    }

    public VodAction(Context context, String vid, String url,boolean isFromWeb, boolean isNewDevice,int action,int fromService) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.fromService = fromService;
        mContext = context;
        this.vid = vid;
        this.url = url;
        this.isFromWeb = isFromWeb;
        this.isNewDevice = isNewDevice;
        this.action = action;
    }

    @Override
    public void execute() {
        onActionBegin();

        // 跳转或将参数设置到ScreenProjectionActivity
        Bundle data = new Bundle();
        data.putString(ScreenProjectionActivity.EXTRA_PATH, url);
        data.putString(ScreenProjectionActivity.EXTRA_TYPE, ConstantValues.PROJECT_TYPE_VIDEO_VOD);
        data.putString(ScreenProjectionActivity.EXTRA_MEDIA_ID, vid);
        data.putInt(ScreenProjectionActivity.EXTRA_ACTION_ID, action);
        data.putInt(ScreenProjectionActivity.EXTRA_FROM_SERVICE_ID, fromService);
        data.putBoolean(ScreenProjectionActivity.EXTRA_IS_FROM_WEB, isFromWeb);
        data.putBoolean(ScreenProjectionActivity.EXTRA_IS_NEW_DEVICE, isNewDevice);
        data.putSerializable(ScreenProjectionActivity.EXTRA_PROJECT_ACTION, this);

        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof ScreenProjectionActivity && !((ScreenProjectionActivity) activity).isBeenStopped()) {
            LogUtils.e("Listener will setNewProjection");
            ((ScreenProjectionActivity) activity).setNewProjection(data);
        } else {
            LogUtils.e("Listener will startActivity in new task");
            Intent intent = new Intent(mContext, ScreenProjectionActivity.class);
            intent.putExtras(data);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    @Override
    public String toString() {
        return "VodAction{" +
                "vid='" + vid + '\'' +
                ", url='" + url + '\'' +
                ", isFromWeb=" + isFromWeb +
                '}';
    }
}

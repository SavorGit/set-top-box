package com.savor.ads.projection.action;

import android.app.Activity;

import com.savor.ads.activity.ScreenProjectionActivity;
import com.savor.ads.projection.ProjectPriority;
import com.savor.ads.utils.ActivitiesManager;

/**
 * Created by zhang.haiqiang on 2017/5/22.
 */

public class SeekAction extends ProjectionActionBase {

    private int position;

    public SeekAction(int position) {
        super();

        mPriority = ProjectPriority.NORMAL;
        this.position = position;
    }

    @Override
    public void execute() {
        onActionBegin();

        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof ScreenProjectionActivity) {
            ((ScreenProjectionActivity) activity).seekTo(position);
        }

        onActionEnd();
    }

    @Override
    public String toString() {
        return "SeekAction{" +
                "position=" + position +
                '}';
    }
}

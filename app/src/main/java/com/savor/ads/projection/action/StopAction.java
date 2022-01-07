package com.savor.ads.projection.action;

import android.app.Activity;

import com.savor.ads.activity.LoopPlayActivity;
import com.savor.ads.activity.MeetingSignInActivity;
import com.savor.ads.activity.ScreenProjectionActivity;
import com.savor.ads.projection.ProjectPriority;
import com.savor.ads.utils.ActivitiesManager;

/**
 * Created by zhang.haiqiang on 2017/5/22.
 */

public class StopAction extends ProjectionActionBase {

    private String projectId;
    private boolean isRstr;

    public StopAction(String projectId, boolean isRstr) {
        super();

        mPriority = ProjectPriority.HIGH;
        this.projectId = projectId;
        this.isRstr = isRstr;
    }

    @Override
    public void execute() {
        onActionBegin();
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof ScreenProjectionActivity) {
            ((ScreenProjectionActivity) activity).stop(true, this);
        }else if (activity instanceof LoopPlayActivity){
            ((LoopPlayActivity) activity).stop();
        }else if (activity instanceof MeetingSignInActivity){
            ((MeetingSignInActivity) activity).stop();
        }

    }
}

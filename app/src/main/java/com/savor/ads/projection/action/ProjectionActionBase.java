package com.savor.ads.projection.action;

import com.savor.ads.projection.ProjectPriority;
import com.savor.ads.projection.ProjectionListener;
import com.savor.ads.projection.ProjectionManager;

import java.util.ArrayList;

/**
 * Created by zhang.haiqiang on 2017/5/22.
 */

public abstract class ProjectionActionBase {

    /**动作优先级，值越大优先级越高*/
    protected ProjectPriority mPriority;
    /**来自于哪个投屏服务的请求*/
    protected int fromService;
    /**动作是否执行完毕*/
    protected boolean mIsActionEnd;

    private ArrayList<ProjectionListener> mListenerList;

    ProjectionActionBase() {
        mIsActionEnd = false;
        mListenerList = new ArrayList<>();
    }

    public void onActionBegin() {
        ProjectionManager.log(this.getClass().getSimpleName() +" onActionBegin");
        mIsActionEnd = false;
        if (mListenerList != null) {
            for (ProjectionListener listener :
                    mListenerList) {
                listener.onStart(this);
            }
        }
    }

    public void onActionEnd() {
        ProjectionManager.log(this.getClass().getSimpleName() +" onActionEnd");
        mIsActionEnd = true;
        if (mListenerList != null) {
            for (ProjectionListener listener :
                    mListenerList) {
                listener.onEnd(this);
            }
        }
    }

    public boolean isActionEnd() {
        return mIsActionEnd;
    }

    public void addActionListener(ProjectionListener projectionListener) {
        if (mListenerList == null) {
            mListenerList = new ArrayList<>();
        }
        mListenerList.add(projectionListener);
    }

    public ProjectPriority getPriority() {
        return mPriority;
    }

    public int getFromService() {
        return fromService;
    }


    public abstract void execute();
}

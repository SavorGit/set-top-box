package com.savor.ads.projection;

import com.savor.ads.projection.action.ProjectionActionBase;

/**
 * Created by zhang.haiqiang on 2017/5/22.
 */

public interface ProjectionListener {
    void onStart(ProjectionActionBase projection);
    void onEnd(ProjectionActionBase projection);
}

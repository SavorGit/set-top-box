package com.savor.tvlibrary;

import android.media.tv.TvView;
import android.view.SurfaceHolder;

import java.util.ArrayList;

/**
 * Created by zhang.haiqiang on 2017/7/19.
 */

public interface ITVOperator {
    void setDisplay(SurfaceHolder holder);
    void switchATVChannel(TvView tvView, int channelId);
    void autoTuning(AutoTurningCallback turningCallback);
    void interruptTuning();
    ArrayList<AtvChannel> getAtvChannels();
    ArrayList<AtvChannel> getSysChannels();
    void setAtvChannels(ArrayList<AtvChannel> channels);
    void setSignalSource(TvView tvView, TVSignal signal);
    void exitTv(TvView tvView);
    void switchResolution(OutputResolution resolution);
}

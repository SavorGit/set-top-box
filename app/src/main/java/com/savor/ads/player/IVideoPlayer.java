package com.savor.ads.player;

import java.io.File;
import java.util.ArrayList;

public interface IVideoPlayer {
    void resume();
    void pause();
    void togglePlay();
    void release();

    void seekTo(int position);

    void setPlayStateCallback(PlayStateCallback playStateCallback);

    void setIfHandlePrepareTimeout(boolean ifHandlePrepareTimeout);

    void setSource(String mediaPath, String mediaTag, int seekPosition);
    void setSource(String mediaPath, String mediaTag, int seekPosition,boolean changeState);
    void setSource(String mediaPath, String mediaTag, int seekPosition, boolean changeState, boolean cacheWithPlay, File cacheFile);

//    void onPause();
//    void onStop();
//    void onResume();
    int getCurrentPosition();
    int getDuration();
    boolean isPaused();
    boolean isInPlaybackState();
//    int getCurrentState();
}

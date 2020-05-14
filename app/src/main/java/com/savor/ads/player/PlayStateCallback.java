package com.savor.ads.player;


public interface PlayStateCallback {
    /**
     * 某个视频播放完毕
     *
     * @param mediaTag  当前视频标志
     */
    void onMediaComplete(String mediaTag);

    /**
     * 某个视频播放出错
     *
     * @param mediaTag  当前视频标志
     */
    void onMediaError(String mediaTag);

    /**
     * 视频准备完毕
     *
     * @return true: 播放被中止； false: otherwise
     */
    boolean onMediaPrepared(String mediaTag);

    void onMediaPause(String mediaTag);

    void onMediaResume(String mediaTag);

    void onMediaBufferUpdate(String mediaTag, int percent);

    void onMediaBufferTimeout(String mediaTag);
}

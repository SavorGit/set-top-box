package com.savor.ads.player;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.savor.ads.R;
import com.savor.ads.bean.MediaPlayerError;
import com.savor.ads.player.mediacodec.MediaCodecRenderView;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import androidx.annotation.RequiresApi;


/**
 * 公用的播放器类
 * Created by zhanghq on 2016/12/8.
 */
public class GGVideoPlayer extends StandardGSYVideoPlayer implements IVideoPlayer {
    private static final String TAG = "GGVideoPlayer";

    /**
     * 最大Prepare时间
     */
    private static final int MAX_PREPARE_TIME = 1000 * 20;

    private String mMediaPath;

    private String mMediaTag;
    /**
     * 指定的播放起始位置
     */
    private int mAssignedPlayPosition = -1;

    private boolean mIsPauseByOut;

    /**
     * 是否处理MediaPlayer的Prepare超时
     */
    private boolean mIfHandlePrepareTimeout;

    /**
     * 最大缓冲加载时间
     */
    private static final int MAX_BUFFER_TIME = 1000 * 10;
    private Runnable mBufferTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayStateCallback != null) {
                // 回调某个视频播放出错
                mPlayStateCallback.onMediaBufferTimeout(mMediaTag);
            }
        }
    };

    private PlayStateCallback mPlayStateCallback;
    private Handler mHandler = new Handler();

    public GGVideoPlayer(Context context) {
        this(context, null);
    }

    public GGVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_gg_video_view;
    }

    @Override
    protected void init(Context context) {
        super.init(context);

        mContext = context;

        initPlayer();

    }


    @Override
    public void onPrepared() {
        super.onPrepared();

        LogUtils.w(TAG + " onPrepared " +
                " mIsPauseByOut:" + mIsPauseByOut + " mAssignedPlayPosition:" + mAssignedPlayPosition
                + " " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " onPrepared " +
                " mIsPauseByOut:" + mIsPauseByOut + " mAssignedPlayPosition:" + mAssignedPlayPosition
                + " " + GGVideoPlayer.this.hashCode());

        if (mIfHandlePrepareTimeout) {
            // 准备开始播放移除Runnable
            removeCallbacks(mPrepareTimeoutRunnable);
        }

        boolean beenAborted = false;
        // 回调准备完毕
        if (mPlayStateCallback != null) {
            beenAborted = mPlayStateCallback.onMediaPrepared(mMediaTag);
        }

        if (!beenAborted) {

            // 如果Surface创建完毕且没被外部强行停止时，开始播放
            if (!mIsPauseByOut) {

                if (mAssignedPlayPosition > 0 && mAssignedPlayPosition < super.getDuration()) {
                    seekTo(mAssignedPlayPosition);
                    mAssignedPlayPosition = -1;
                } else {
                    super.startAfterPrepared();
                }
            }
        }
    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);

        LogUtils.e(TAG + " onError what = " + what + " extra = " + extra + " " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " onError what = " + what + " extra = " + extra + " " + GGVideoPlayer.this.hashCode());

        int position = getCurrentPosition();

        // 根据出错类型，判断是否可继续尝试播放此视频源
        if (extra == MediaPlayerError.ERROR_NOT_CONNECTED) {
            // TODO: 这个播放器会不会进这个错误，进这个错误时的现象什么样？
            // 网络连接错误的时候会进到ERROR_NOT_CONNECTED这个错误类型，这时reset MediaPlayer并记录播放进度
            LogFileUtil.write(TAG + " will resetAndPreparePlayer at method onErrorNet" + " " + GGVideoPlayer.this.hashCode());
            resetAndPreparePlayer(position);

        } else {
            if (extra == -192) {
                // 缓冲超时
                post(mBufferTimeoutRunnable);

            } else {

                if (mPlayStateCallback != null) {
                    // 回调某个视频播放出错
                    mPlayStateCallback.onMediaError(mMediaTag);
                }
            }
        }

    }

    @Override
    public void onSeekComplete() {
        super.onSeekComplete();

        super.startAfterPrepared();

//        if (super.getCurrentState() == GSYVideoView.CURRENT_STATE_NORMAL) {
//            super.startAfterPrepared();
//        } else if (super.getCurrentState() == GSYVideoView.CURRENT_STATE_PAUSE) {
//            super.onVideoResume();
//        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        super.onBufferingUpdate(percent);

        if (mPlayStateCallback != null) {
            mPlayStateCallback.onMediaBufferUpdate(mMediaTag, percent);
        }
    }

    @Override
    public void onCompletion() {
        super.onCompletion();

        LogUtils.w(TAG + " onCompletion" + " " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " onCompletion" + " " + GGVideoPlayer.this.hashCode());
    }

    @Override
    public void onAutoCompletion() {
        LogUtils.w(TAG + " onAutoCompletion" + " " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " onAutoCompletion" + " " + GGVideoPlayer.this.hashCode());

        super.onAutoCompletion();

        extractCompletion();

    }

    private void initPlayer() {
        LogUtils.w(TAG + " initPlayer " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " initPlayer " + GGVideoPlayer.this.hashCode());

        ((GSYVideoManager) getGSYVideoManager()).setTimeOut(MAX_BUFFER_TIME, true);
    }

    /**
     * 抽取播放完成方法
     */
    private void extractCompletion() {
        if (mPlayStateCallback != null) {
            // 回调某个视频播放完毕
            mPlayStateCallback.onMediaComplete(mMediaTag);
        }
    }

    /**
     * 设置播放数据源
     */
    private boolean setMediaPlayerSource() {
        LogUtils.w(TAG + " setMediaPlayerSource " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " setMediaPlayerSource " + GGVideoPlayer.this.hashCode());
        Map<String, String> mapHeadData =null;
        LogUtils.w("开始播放：" + mMediaPath + " " + GGVideoPlayer.this.hashCode());

        mHandler.removeCallbacksAndMessages(null);

        super.setStartAfterPrepared(false);
        super.setUp(mMediaPath, false, null, mapHeadData, "");

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void addTextureView() {
        mTextureView = new MediaCodecRenderView();
        mTextureView.addView(getContext(), mTextureViewContainer, mRotate, this, this, mEffectFilter, mMatrixGL, mRenderer, mMode);
    }

    /**
     * 准备播放
     */
    private void prepareMediaPlayer() {
        super.prepareVideo();

        if (mIfHandlePrepareTimeout) {
            removeCallbacks(mPrepareTimeoutRunnable);
            postDelayed(mPrepareTimeoutRunnable, MAX_PREPARE_TIME);
        }
    }

    /**
     * 准备超时
     */
    private Runnable mPrepareTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            // 回调某个视频播放出错
            mPlayStateCallback.onMediaError(mMediaTag);
        }
    };

    /**
     * 重置播放器状态，并准备下次播放
     *
     * @param assignedPlayPosition
     */
    private void resetAndPreparePlayer(int assignedPlayPosition) {
        LogUtils.w(TAG + " resetAndPreparePlayer assignedPlayPosition = " + assignedPlayPosition + " " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " resetAndPreparePlayer assignedPlayPosition = " + assignedPlayPosition + " " + GGVideoPlayer.this.hashCode());

        mAssignedPlayPosition = assignedPlayPosition;

        setAndPrepare();
    }

    private void setAndPrepare() {
        setMediaPlayerSource();
        prepareMediaPlayer();

//        if (setMediaPlayerSource()) {
//
//            prepareMediaPlayer();
//
//        } else {
//            if (mForcePlayFromStart) {
//                // 强制从头播放
//                mForcePlayFromStart = false;
//                mCurrentFileIndex = 0;
//                mAssignedPlayPosition = 0;
//            } else {
//                // 播放下一个
//                mCurrentFileIndex = (mCurrentFileIndex + 1) % mMediaFiles.size();
//                mAssignedPlayPosition = 0;
//            }
//
//            if (mIsLooping) {
//                // 重置播放器状态，以备下次播放
//                LogFileUtil.write(TAG + " will resetAndPreparePlayer at method setAndPrepare" + " " + GGVideoPlayer.this.hashCode());
//                resetAndPreparePlayer();
//            }
//        }
    }

    public void resume() {
        LogUtils.w(TAG + " resume " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " resume " + GGVideoPlayer.this.hashCode());
//        if (MediaPlayerState.PAUSED == playsta) {
        mIsPauseByOut = false;
//            playInner();
        super.onVideoResume();


        // 回调某个视频播放恢复播放
        if (mPlayStateCallback != null) {
            mPlayStateCallback.onMediaResume(mMediaTag);
        }
    }


    /**
     * 暂停播放
     */
    public void pause() {
        LogUtils.w(TAG + " pause " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " pause " + GGVideoPlayer.this.hashCode());

        mIsPauseByOut = true;
        super.onVideoPause();

        // 回调某个视频暂停
        if (mPlayStateCallback != null) {
            mPlayStateCallback.onMediaPause(mMediaTag);
        }
    }

    /**
     * 停止播放并释放MediaPlayer
     */
    public void release() {
        LogUtils.w(TAG + " release mPlayState: " + GGVideoPlayer.this.hashCode());
        super.release();
    }

    /**
     * 继续、暂停 播放
     */
    public void togglePlay() {
        LogUtils.w(TAG + "togglePlay " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " togglePlay  " + GGVideoPlayer.this.hashCode());
        clickStartIcon();
    }

    /**
     * 调整播放进度
     *
     * @param position
     */
    public void seekTo(int position) {
        LogUtils.w(TAG + " seek " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " seek " + GGVideoPlayer.this.hashCode());
        if (isInPlayingState()) {
            super.seekTo(position);
        } else {
            mAssignedPlayPosition = position;
        }
    }

    @Override
    public void onInfo(int what, int extra) {
        super.onInfo(what, extra);
        LogUtils.w(TAG + " onInfo " + GGVideoPlayer.this.hashCode()+ " what:"+what+" extra:"+extra);
    }

    /**
     * 获取播放进度，非播放状态调用时返回-1
     *
     * @return
     */
    public int getCurrentPosition() {
        if (isInPlayingState())
            return getCurrentPositionWhenPlaying();
        else
            return -1;
    }

    @Override
    public boolean isPaused() {
        return getCurrentState() == CURRENT_STATE_PAUSE;
    }

    @Override
    public boolean isInPlaybackState() {
        return super.isInPlayingState();
    }

    /**
     * 设置播放状态监听事件回调
     *
     * @param playStateCallback
     */
    public void setPlayStateCallback(PlayStateCallback playStateCallback) {
        mPlayStateCallback = playStateCallback;
    }

    /**
     * 设置是否处理Prepare超时回调，目前只有投屏会处理
     *
     * @param ifHandlePrepareTimeout
     */
    public void setIfHandlePrepareTimeout(boolean ifHandlePrepareTimeout) {
        mIfHandlePrepareTimeout = ifHandlePrepareTimeout;
    }

    @Override
    public void setSource(String mediaPath, String mediaTag) {
        setSource(mediaPath, mediaTag, 0);
    }

    @Override
    public void setSource(String mediaPath, String mediaTag, int seekPosition) {
        LogUtils.w(TAG + " setSource " + GGVideoPlayer.this.hashCode());
        LogFileUtil.write(TAG + " setSource " + GGVideoPlayer.this.hashCode());
        mIsPauseByOut = false;
        if (!TextUtils.isEmpty(mediaPath)) {
            mAssignedPlayPosition = seekPosition;

            mMediaPath = mediaPath;
            mMediaTag = mediaTag;

            if (!hasKnownPrefix(mMediaPath)) {
                mMediaPath = "file://" + mMediaPath;
            }

            initPlayer();

            setAndPrepare();
            changeRotate();
        }
    }

    private boolean hasKnownPrefix(String path) {
        boolean ret = false;
        if (!TextUtils.isEmpty(path)) {
            ret = path.startsWith("http://") || path.startsWith("https://") ||
                    path.startsWith("file://") || path.startsWith("content://");
        }

        return ret;
    }

    private void changeRotate(){
//        float rotate =super.mTextureView.getRotation();
//        mTextureView.setRotation(rotate);
//        mTextureView.requestLayout();
    }

}
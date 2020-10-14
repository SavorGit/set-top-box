package com.savor.ads.customview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mstar.android.media.MstMediaMetadataRetriever;
import com.savor.ads.R;
import com.savor.ads.bean.MediaFileBean;
import com.savor.ads.bean.MediaPlayerError;
import com.savor.ads.bean.MediaPlayerState;
import com.savor.ads.player.GGVideoPlayer;
import com.savor.ads.player.IVideoPlayer;
import com.savor.ads.player.PlayStateCallback;
import com.savor.ads.player.PlayerType;
import com.savor.ads.player.SavorPlayerFactory;
import com.savor.ads.utils.AcFunDanmakuParser;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.ui.widget.DanmakuView;


/**
 * 公用的播放器类
 * Created by zhanghq on 2016/12/8.
 */
public class ProjectVideoView extends RelativeLayout implements PlayStateCallback {
    private static final String TAG = "ProjectVideoView";
    private static final String TIP = "BLACK_SCREEN";
    /**
     * MediaPlayer最大Prepare时间
     */
    private static final int MAX_PREPARE_TIME = 1000 * 20;
    private Context mContext;
    private IVideoPlayer mVideoPlayer;
    OrientationUtils orientationUtils;
    private ImageView mImgView;
    private ImageView mLoadingIv;
    private CircleProgressBar mProgressBar;
    private ImageView mPlayVideoIv;

    //    private MediaPlayer mMediaPlayer;
//    private MediaPlayerState mPlayState;
    private ArrayList<MediaFileBean> mMediaFiles;

    /**
     * 当前应该播放的源序号
     */
    private int mCurrentFileIndex;
    /**
     * 指定的播放起始位置
     */
    private int mAssignedPlayPosition = -1;
    /**
     * 强制从头播放标志位
     */
    private boolean mForcePlayFromStart;
    private boolean mIsPauseByOut;

    /**
     * 暂停时是否显示“播放”图片
     */
    private boolean mIfShowPauseBtn;
    /**
     * 是否显示加载loading
     */
    private boolean mIfShowLoading;
    /**
     * 是否自动播放下一个
     */
    private boolean mIsLooping = true;
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
                boolean isLast = false;
                if (mMediaFiles != null && mMediaFiles.size() > 0) {
                    isLast = mCurrentFileIndex == mMediaFiles.size() - 1;
                }
                LogUtils.d(TIP+"视频播放超时");
                // 回调某个视频播放出错
                mPlayStateCallback.onMediaError(mCurrentFileIndex, isLast);
            }
        }
    };

    private PlayStateCallback mPlayStateCallback;
    /**
     * 当前播放的是图片还是视频:视频true,图片false
     **/
    private int duration = 0;
    private int mSurfaceViewWidth;
    private int mSurfaceViewHeight;
    private Handler mHandler = new Handler();
    public ProjectVideoView(Context context) {
        this(context, null);
    }

    public ProjectVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
        initMediaPlayer();
    }

    private void initView() {
        View rootView = View.inflate(getContext(), R.layout.layout_savor_video_view, this);
        ViewGroup vp = rootView.findViewById(R.id.player_container);
        mVideoPlayer = SavorPlayerFactory.getPlayer(PlayerType.GGPlayer, vp);
        mImgView = rootView.findViewById(R.id.img_view);
        mLoadingIv = findViewById(R.id.iv_loading);
        mProgressBar = rootView.findViewById(R.id.progress_bar);
        mPlayVideoIv = findViewById(R.id.iv_video_play);

        if (mVideoPlayer == null) {
            LogUtils.e(TAG + " Player init error!");
        }

    }

    private void initMediaPlayer() {
        LogUtils.w(TAG + "initMediaPlayer " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " initMediaPlayer " + ProjectVideoView.this.hashCode());
        mVideoPlayer.setPlayStateCallback(this);

    }

    /**
     * 抽取播放完成方法
     */
    private void extractCompletion() {

        boolean beenResetSource = false;
        if (mPlayStateCallback != null) {
            boolean isLast = false;
            if (mMediaFiles != null && mMediaFiles.size() > 0) {
                isLast = mCurrentFileIndex == mMediaFiles.size() - 1;
            }
            LogUtils.d(TIP+"视频播放完成");
            // 回调某个视频播放完毕
            beenResetSource = mPlayStateCallback.onMediaComplete(mCurrentFileIndex, isLast);
        }

        if (!beenResetSource) {
            if (mForcePlayFromStart) {
                // 强制从头播放
                mForcePlayFromStart = false;
                mCurrentFileIndex = 0;
                mAssignedPlayPosition = 0;
            } else {
                // 播放下一个
                mCurrentFileIndex = (mCurrentFileIndex + 1) % mMediaFiles.size();
                mAssignedPlayPosition = 0;
            }

            if (mIsLooping) {
                // 重置播放器状态，以备下次播放
                LogFileUtil.write(TAG + " will resetAndPreparePlayer at method extractCompletion" + " " + ProjectVideoView.this.hashCode());
                resetAndPreparePlayer();
            }
        }
    }

    /**
     * 设置播放数据源
     */
    private boolean setMediaPlayerSource() {
        LogUtils.w(TAG + " setMediaPlayerSource " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " setMediaPlayerSource " + ProjectVideoView.this.hashCode());
        if (mMediaFiles == null || mMediaFiles.isEmpty() || mCurrentFileIndex >= mMediaFiles.size()) {
            LogUtils.e(TAG + " setMediaPlayerSource in garbled source, mCurrentFileIndex =  " + mCurrentFileIndex + " " + ProjectVideoView.this.hashCode());
            LogFileUtil.write(TAG + " setMediaPlayerSource in garbled source, mCurrentFileIndex =  " + mCurrentFileIndex + " " + ProjectVideoView.this.hashCode());
            return false;
        }

        LogUtils.w("开始播放：" + mMediaFiles.get(mCurrentFileIndex) + " " + ProjectVideoView.this.hashCode());

        MediaFileBean bean = mMediaFiles.get(mCurrentFileIndex);
        String url = bean.getUrl();
        File cacheFile  = bean.getCacheFile();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mImgView.setVisibility(View.GONE);
        } else {
            post(()->mImgView.setVisibility(View.GONE));
        }
        if (!TextUtils.isEmpty(url)){
            mVideoPlayer.setCoverImage(url);
        }
        if (!TextUtils.isEmpty(url)&&cacheFile!=null){
            mVideoPlayer.setSource(url, String.valueOf(mCurrentFileIndex),0,false,true,cacheFile);
        }else if (cacheFile!=null){
            mVideoPlayer.setSource(cacheFile.getPath(), String.valueOf(mCurrentFileIndex),0,false);
        }else if (!TextUtils.isEmpty(url)){
            mVideoPlayer.setSource(url, String.valueOf(mCurrentFileIndex),0,false);
        }

        orientationUtils = new OrientationUtils((Activity) mContext,(GGVideoPlayer)mVideoPlayer);

        return true;
    }

    /**
     * 准备播放
     */
    private void prepareMediaPlayer() {
        LogUtils.w(TAG + " prepareMediaPlayer " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " prepareMediaPlayer " + ProjectVideoView.this.hashCode());
        if (mIfShowLoading) {
            mProgressBar.setVisibility(VISIBLE);
        }
        if (mIfHandlePrepareTimeout) {
            LogUtils.w(TAG + " postDelayed(mPrepareTimeoutRunnable, MAX_PREPARE_TIME) " + ProjectVideoView.this.hashCode());
            mHandler.removeCallbacks(mPrepareTimeoutRunnable);
            mHandler.postDelayed(mPrepareTimeoutRunnable, MAX_PREPARE_TIME);
        }
    }

    /**
     * 准备超时，播放下一个
     */
    private Runnable mPrepareTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsLooping) {
                playNext();
            } else {
                if (mPlayStateCallback != null) {
                    boolean isLast = false;
                    if (mMediaFiles != null && mMediaFiles.size() > 0) {
                        isLast = mCurrentFileIndex == mMediaFiles.size() - 1;
                    }
                    // 回调某个视频播放出错
//                    mPlayStateCallback.onMediaError(mCurrentFileIndex, isLast);
                }
            }
        }
    };

    /**
     * 重置播放器状态，并准备下次播放
     */
    private void resetAndPreparePlayer() {
        resetAndPreparePlayer(0);
    }

    /**
     * 重置播放器状态，并准备下次播放
     *
     * @param assignedPlayPosition
     */
    private void resetAndPreparePlayer(int assignedPlayPosition) {
        LogUtils.w(TAG + " resetAndPreparePlayer assignedPlayPosition = " + assignedPlayPosition + " " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " resetAndPreparePlayer assignedPlayPosition = " + assignedPlayPosition + " " + ProjectVideoView.this.hashCode());
//        if (mMediaPlayer != null) {
//            mMediaPlayer.reset();
//        }
//        mPlayState = MediaPlayerState.IDLE;

        mAssignedPlayPosition = assignedPlayPosition;

        if (mMediaFiles != null && mMediaFiles.size() > 0) {
            setAndPrepare();
        }
    }

    private void setAndPrepare() {
        Log.d("StackTrack", "ProjectVideoView::setAndPrepare");
        if (setMediaPlayerSource()) {
            prepareMediaPlayer();
        } else {
            if (mForcePlayFromStart) {
                // 强制从头播放
                mForcePlayFromStart = false;
                mCurrentFileIndex = 0;
                mAssignedPlayPosition = 0;
            } else {
                // 播放下一个
                mCurrentFileIndex = (mCurrentFileIndex + 1) % mMediaFiles.size();
                mAssignedPlayPosition = 0;
            }

            if (mIsLooping) {
                // 重置播放器状态，以备下次播放
                LogFileUtil.write(TAG + " will resetAndPreparePlayer at method setAndPrepare" + " " + ProjectVideoView.this.hashCode());
                resetAndPreparePlayer();
            }
        }
    }
    /**
     * 停止播放
     */
    public void stop() {
        LogUtils.w(TAG + " stop " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " stop " + ProjectVideoView.this.hashCode());
        stopInner();

        mIsPauseByOut = true;
    }

    /**
     * 停止播放
     */
    private void stopInner() {
        LogUtils.w(TAG + " stopInner " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " stopInner " + ProjectVideoView.this.hashCode());
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
    }

    /**
     * 所在页面onPause时请调用此方法处理
     */
//    public void onPause() {
//        if (mVideoPlayer != null) {
//            try {
//                // 记录播放进度
//                mAssignedPlayPosition = mVideoPlayer.getCurrentPosition();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            release();
//        }
//    }

    public void onStop() {
        LogUtils.w(TAG + " onPause " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " onPause " + ProjectVideoView.this.hashCode());

        mHandler.removeCallbacks(mPrepareTimeoutRunnable);

        if (mPlayStateCallback != null) {
            mPlayStateCallback.onMediaPause(mCurrentFileIndex);
        }
    }

    /**
     * 所在页面onResume时请调用此方法处理
     */
//    public void onResume() {
//        LogUtils.w(TAG + " onResume " + ProjectVideoView.this.hashCode());
//        LogFileUtil.write(TAG + " onResume " + ProjectVideoView.this.hashCode());
//
//        mIsPauseByOut = false;
//        if (mMediaFiles != null && mMediaFiles.size() > 0) {
//            setAndPrepare();
//        }
//    }


    /**
     * 停止播放并释放MediaPlayer
     */
    public void release() {
        LogUtils.w(TAG + " release " + ProjectVideoView.this.hashCode());
        Log.d("StackTrack", "ProjectVideoView::release");
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
        }

        if (mIfShowPauseBtn) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                mPlayVideoIv.setVisibility(GONE);
            } else {
                post(()->mPlayVideoIv.setVisibility(GONE));
            }
        }
    }

    /**
     * 设置播放源
     *
     * @param mediaFiles       文件路径集合
     */
    public void setMediaFiles(ArrayList<MediaFileBean> mediaFiles) {
        LogUtils.w(TAG + "setMediaFiles " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " setMediaFiles " + ProjectVideoView.this.hashCode());
        mIsPauseByOut = false;
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            mMediaFiles = mediaFiles;
            mPlayVideoIv.setVisibility(GONE);

            setAndPrepare();
        }
    }

    /**
     * 播放下一条
     */
    public void playNext() {
        LogUtils.w(TAG + " playNext " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " playNext " + ProjectVideoView.this.hashCode());
        stopInner();

        mIsPauseByOut = false;
        mAssignedPlayPosition = 0;
        mCurrentFileIndex = (mCurrentFileIndex + 1) % mMediaFiles.size();
        LogUtils.w(TAG + " mCurrentFileIndex:" + mCurrentFileIndex + " size = " + mMediaFiles.size() + " " + ProjectVideoView.this.hashCode());
        resetAndPreparePlayer();
    }

    /**
     * 播放上一条
     */
    public void playPrevious() {
        LogUtils.w(TAG + " playPrevious " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " playPrevious " + ProjectVideoView.this.hashCode());
        stopInner();

        mIsPauseByOut = false;
        mAssignedPlayPosition = 0;
        mCurrentFileIndex = (mCurrentFileIndex - 1 + mMediaFiles.size()) % mMediaFiles.size();
        LogUtils.w(TAG + " mCurrentFileIndex:" + mCurrentFileIndex + " size = " + mMediaFiles.size() + " " + ProjectVideoView.this.hashCode());
        resetAndPreparePlayer();
    }

    /**
     * 调整播放进度
     *
     * @param position
     */
    public void seekTo(int position) {
        LogUtils.w(TAG + " seek " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " seek " + ProjectVideoView.this.hashCode());
        mVideoPlayer.seekTo(position);
    }

    /**
     * 播放器是否处于可播放的状态
     *
     * @return
     */
    public boolean isInPlaybackState() {
        return mVideoPlayer != null && mVideoPlayer.isInPlaybackState();
    }

    /**
     * 是否显示开始、暂停按钮，默认不显示
     *
     * @param ifShowPauseBtn
     */
    public void setIfShowPauseBtn(boolean ifShowPauseBtn) {
        mIfShowPauseBtn = ifShowPauseBtn;
    }

    /**
     * 获取播放进度，非播放状态调用时返回-1
     *
     * @return
     */
    public int getCurrentPosition() {
        LogUtils.w(TAG + " getCurrentPosition " + ProjectVideoView.this.hashCode());
        LogFileUtil.write(TAG + " getCurrentPosition " + ProjectVideoView.this.hashCode());
        if (isInPlaybackState())
            return mVideoPlayer.getCurrentPosition();
        else
            return -1;
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
        mVideoPlayer.setIfHandlePrepareTimeout(mIfHandlePrepareTimeout);
    }

    /**
     * 是否自动播放下一个
     * 默认是自动播放
     *
     * @param looping
     */
    public void setLooping(boolean looping) {
        mIsLooping = looping;
    }

    public void setIfShowLoading(boolean ifShowLoading) {
        mIfShowLoading = ifShowLoading;
    }

    @Override
    public void onMediaComplete(String mediaTag) {
        extractCompletion();
    }

    @Override
    public void onMediaError(String mediaTag) {
        boolean beenResetSource = false;
        if (mPlayStateCallback != null) {
            boolean isLast = false;
            if (mMediaFiles != null && mMediaFiles.size() > 0) {
                isLast = mCurrentFileIndex == mMediaFiles.size() - 1;
            }
            LogUtils.d(TIP+"视频播放出错");
            // 回调某个视频播放出错
            beenResetSource = mPlayStateCallback.onMediaError(mCurrentFileIndex, isLast);
        }

        if (!beenResetSource) {
            if (mForcePlayFromStart) {
                // 强制从头播放
                mForcePlayFromStart = false;
                mCurrentFileIndex = 0;
                mAssignedPlayPosition = 0;
            } else /*if (position == mMediaPlayer.getDuration()) */ {
                // 播放下一个
                if (mMediaFiles != null && mMediaFiles.size() > 0) {
                    mCurrentFileIndex = (mCurrentFileIndex + 1) % mMediaFiles.size();
                    mAssignedPlayPosition = 0;
                }
            }
            if (mIsLooping) {
                LogFileUtil.write(TAG + " will resetAndPreparePlayer at method onError" + " " + ProjectVideoView.this.hashCode());
                resetAndPreparePlayer();
            }
        }
    }

    @Override
    public boolean onMediaPrepared(String mediaTag) {
        if (mIfHandlePrepareTimeout) {
            // 准备开始播放移除Runnable
            LogUtils.w(TAG + " removeCallbacks(mPrepareTimeoutRunnable) " + ProjectVideoView.this.hashCode());
            mHandler.removeCallbacks(mPrepareTimeoutRunnable);
        }

        boolean beenAborted = false;
        // 回调准备完毕
        if (mPlayStateCallback != null) {
            beenAborted = mPlayStateCallback.onMediaPrepared(mCurrentFileIndex);
        }

        if (!beenAborted) {
            if (mIfShowLoading) {
                mLoadingIv.setVisibility(GONE);
                mProgressBar.setVisibility(GONE);
            }
        }
        return beenAborted;
    }

    @Override
    public void onMediaPause(String mediaTag) {

    }

    @Override
    public void onMediaResume(String mediaTag) {

    }

    @Override
    public void onMediaBufferUpdate(String mediaTag, int percent) {
        if (mVideoPlayer.getDuration()==0){
            return;
        }
        if (percent==100){
            mPlayStateCallback.onMediaBufferPercent();
        }
//        int currentPercent = mVideoPlayer.getCurrentPosition() * 100 / mVideoPlayer.getDuration();
        LogUtils.v(TAG + "onBufferingUpdate currentPercent = " + percent + " position = " +
                mVideoPlayer.getCurrentPosition() + " duration = " + mVideoPlayer.getDuration() + " "
                + this.hashCode());
        LogFileUtil.write(TAG + " onBufferingUpdate currentPercent = " + percent +
                " position = " + mVideoPlayer.getCurrentPosition() + " duration = " +
                mVideoPlayer.getDuration() + " " + this.hashCode());
//                    if (mp.getCurrentPosition() + 400 < mp.getDuration()) {
//        if (percent < 99 && currentPercent >= percent - 1) {
//            // 缓冲部分不足时，暂停播放并显示进度圈
//            if (mIfShowLoading) {
//                mProgressBar.setVisibility(VISIBLE);
//            }
//            if (!mVideoPlayer.isPaused()) {
//                mVideoPlayer.pause();
//            }
//        } else {
//            // 缓冲好时，继续播放并隐藏进度圈
//            if (mIfShowLoading) {
//                mProgressBar.setVisibility(GONE);
//            }
//            if (mVideoPlayer.isPaused() && !mIsPauseByOut) {
//                mVideoPlayer.resume();
//            }
//        }
    }

    @Override
    public void onMediaBufferTimeout(String mediaTag) {
        mHandler.post(mBufferTimeoutRunnable);
    }


    public interface PlayStateCallback {
        /**
         * 某个视频播放完毕
         *
         * @param index  当前视频序号
         * @param isLast 是否是最后一个
         * @return true: 播放源被设置新的； false: otherwise
         */
        boolean onMediaComplete(int index, boolean isLast);

        /**
         * 某个视频播放出错
         *
         * @param index  当前视频序号
         * @param isLast 是否是最后一个
         * @return true: 播放源被设置新的； false: otherwise
         */
        boolean onMediaError(int index, boolean isLast);

        /**
         * 视频准备完毕
         *
         * @param index 当前视频序号
         * @return true: 播放被中止； false: otherwise
         */
        boolean onMediaPrepared(int index);

        void onMediaPause(int index);

        void onMediaResume(int index);

        void onMediaBufferPercent();
    }
}

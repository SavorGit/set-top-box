package com.savor.ads.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.savor.ads.R;
import com.savor.ads.adapter.StringPagerAdapter;
import com.savor.ads.bean.MediaPlayerError;
import com.savor.ads.bean.MediaPlayerState;
import com.savor.ads.core.Session;
import com.savor.ads.utils.AcFunDanmakuParser;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.Danmaku;
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
public class SavorVideoView extends RelativeLayout {
    private static final String TAG = "SavorVideoView";

    /**
     * MediaPlayer最大Prepare时间
     */
    private static final int MAX_PREPARE_TIME = 1000 * 20;
    private Context mContext;
    private SurfaceView mSurfaceTv;
    private SurfaceHolder mSurfaceHolder;
    private RelativeLayout mRootRl;
    /******************************/
    private DanmakuView mDanmakuView;
    private DanmakuContext context;
    private AcFunDanmakuParser mParser;
    /******************************/
//    private ImageView mImgView;
    private ViewPager atlasViewPager;
    private ImageView mLoadingIv;
    private CircleProgressBar mProgressBar;
    private ImageView mPlayVideoIv;

    private MediaPlayer mMediaPlayer;
    private MediaPlayerState mPlayState;
    private ArrayList<String> mMediaFiles;
    private StringPagerAdapter imageAdapter;
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
     * Surface是否创建好了
     */
    private boolean mIsSurfaceCreated;

    /**最大缓冲加载时间*/
    private static final int MAX_BUFFER_TIME = 1000 * 10;
    private Runnable mBufferTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayStateCallback != null) {
                boolean isLast = false;
                if (mMediaFiles != null && mMediaFiles.size() > 0) {
                    isLast = mCurrentFileIndex == mMediaFiles.size() - 1;
                }
                // 回调某个视频播放出错
                mPlayStateCallback.onMediaError(mCurrentFileIndex, isLast);
            }
        }
    };

    private PlayStateCallback mPlayStateCallback;
    private int mCurrentIndex;
    ArrayList<String> mDataSource= new ArrayList<>();
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mCurrentIndex ++;
            atlasViewPager.setCurrentItem(mCurrentIndex, true);
            if (mCurrentIndex+1==mDataSource.size()){
                removeCallbacks(mPlayCompletionRunnable);
                postDelayed(mPlayCompletionRunnable ,duration>0?duration*1000:5*1000);
            }else {
                mHandler.sendEmptyMessageDelayed(0, duration*1000);
            }
//            mCurrentIndex = (mCurrentIndex + 1) % mDataSource.size();
//            atlasViewPager.setCurrentItem(mCurrentIndex, true);
//            mHandler.sendEmptyMessageDelayed(0, duration*1000);
            return true;
        }
    });
    /**当前播放的是图片还是视频:视频true,图片false**/
    private boolean isVideoAds=true;
    private int duration=0;
    public SavorVideoView(Context context) {
        this(context, null);
    }

    public SavorVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
        initMediaPlayer();
    }

    private void initView() {
        View rootView = View.inflate(getContext(), R.layout.layout_savor_video_view, this);
        mRootRl = (RelativeLayout)  rootView;
        mSurfaceTv = rootView.findViewById(R.id.texture_view);
//        mImgView = rootView.findViewById(R.id.img_view);
        atlasViewPager = rootView.findViewById(R.id.atlas);
        mLoadingIv = findViewById(R.id.iv_loading);
        mProgressBar = rootView.findViewById(R.id.progress_bar);
        mPlayVideoIv = findViewById(R.id.iv_video_play);

        ViewPagerScroller pagerScroller = new ViewPagerScroller(mContext);
        pagerScroller.setScrollDuration(2000);
        pagerScroller.initViewPagerScroll(atlasViewPager);

        mSurfaceHolder = mSurfaceTv.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogUtils.w(TAG + "surfaceCreated mPlayState:" + mPlayState + " mMediaPlayer == null?" +(mMediaPlayer == null) + "  " + SavorVideoView.this.hashCode());
                LogFileUtil.write(TAG + " surfaceCreated mPlayState:" + mPlayState + " mMediaPlayer == null?" +(mMediaPlayer == null) + "  " + SavorVideoView.this.hashCode());
                mIsSurfaceCreated = true;
                if (mMediaPlayer != null /*&&
                        (mPlayState != MediaPlayerState.ERROR &&
                                mPlayState != MediaPlayerState.IDLE &&
                                mPlayState != MediaPlayerState.END)*/) {

                    try {
                        LogUtils.w("Will setDisplay Current state:" + mPlayState + " " + SavorVideoView.this.hashCode());
                        LogFileUtil.write("Will setDisplay Current state:" + mPlayState + " " + SavorVideoView.this.hashCode());
                        mMediaPlayer.setDisplay(mSurfaceHolder);
                        mMediaPlayer.setScreenOnWhilePlaying(true);
                    } catch (Exception e) {
                        LogUtils.e("setDisplay Exception, Current state:" + mPlayState + " " + SavorVideoView.this.hashCode());
                        LogFileUtil.write("setDisplay Exception, Current state:" + mPlayState + " " + SavorVideoView.this.hashCode());
                        e.printStackTrace();
                    }
                    if (mPlayState == MediaPlayerState.INITIALIZED) {
                        prepareMediaPlayer();
                    } else if (mPlayState == MediaPlayerState.PREPARED) {
                        playInner();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LogUtils.w(TAG + "surfaceChanged width = " + width + " height = " + height + " " + SavorVideoView.this.hashCode());
                LogFileUtil.write(TAG + " surfaceChanged width = " + width + " height = " + height + " " + SavorVideoView.this.hashCode());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LogUtils.w(TAG + "surfaceDestroyed" + " " + SavorVideoView.this.hashCode());
                LogFileUtil.write(TAG + " surfaceDestroyed" + " " + SavorVideoView.this.hashCode());
                mIsSurfaceCreated = false;

                release();
            }
        });

        context = DanmakuContext.create();
        mParser = new AcFunDanmakuParser();
        initDanmakuView(rootView);
    }

    /**弹幕相关开始---------------**/
    private void initDanmakuView(View rootView) {
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 10); // 滚动弹幕最大显示10行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        mDanmakuView = rootView.findViewById(R.id.view_danmaku);

        context.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3)
                .setDuplicateMergingEnabled(false)//是否合并重复弹幕
                .setScrollSpeedFactor(1.2f)//弹幕滚动速度
                .setScaleTextSize(1.2f)//文字大小
                .setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer
                // .setCacheStuffer(new BackgroundCacheStuffer()) //
                // 绘制背景使用BackgroundCacheStuffer
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);

        if (mDanmakuView != null) {
            mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {

                }

                @Override
                public void prepared() {
                    mDanmakuView.start();
                }
            });

            mDanmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {

                @Override
                public boolean onDanmakuClick(IDanmakus danmakus) {
                    BaseDanmaku latest = danmakus.last();
                    if (null != latest) {
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onViewClick(IDanmakuView view) {
                    return false;
                }
            });

            mDanmakuView.prepare(mParser, context);
            // mDanmakuView.showFPS(true);
            mDanmakuView.enableDanmakuDrawingCache(true);
        }
    }

    public void addItems(final String url,final String text) {
        Glide.with(mContext).load(url).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {

                try {
                    Bitmap bitmap = AppUtils.drawable2Bitmap(resource);
                    addDanmaKuShowTextAndImage(bitmap, text, Color.WHITE, Color.RED, false);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    private void addDanmaKuShowTextAndImage(Bitmap bitmap, String msg, int textColor, int bgColor, boolean islive) {
        BaseDanmaku danmaku = context.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null) {
            Log.e(TAG, "BaseDanmaku空");
        }

        //最里面的图像
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        drawable.setCircular(true);
        drawable.setAntiAlias(true);
        drawable.setCornerRadius(Math.max(bitmap.getWidth()/2, bitmap.getHeight()/2));
        drawable.setBounds(0, 0, bitmap.getWidth()/2, bitmap.getHeight()/2);

        SpannableStringBuilder spannable = createSpannable(drawable, msg, bgColor);
        danmaku.text = spannable;
        danmaku.padding = 10;
        danmaku.priority = 1; // 一定会显示, 一般用于本机发送的弹幕
        danmaku.isLive = islive;
        danmaku.paintHeight=50f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 50f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = textColor;
        danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
        //danmaku.underlineColor = Color.GREEN;

        mDanmakuView.addDanmaku(danmaku);
    }

    private SpannableStringBuilder createSpannable(Drawable drawable, String msg, int color) {
        String text = "image";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        spannableStringBuilder.append(msg);

        ImageSpan span = new ImageSpan(drawable);// ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableStringBuilder.setSpan(new BackgroundColorSpan(color), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
    }

    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {
        @Override
        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {

        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
            // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable

        }
    };
    /**弹幕相关结束---------------**/

    private void initMediaPlayer() {
        LogUtils.w(TAG + "initMediaPlayer mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " initMediaPlayer mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mPlayState = MediaPlayerState.IDLE;
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    LogUtils.w(TAG + "MediaPlayer onCompletion" + " " + SavorVideoView.this.hashCode());
                    LogFileUtil.write(TAG + " MediaPlayer onCompletion" + " " + SavorVideoView.this.hashCode());
                    extractCompletion();
                }
            });
            mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    LogUtils.v(TAG + "onBufferingUpdate percent = " + percent + " " + SavorVideoView.this.hashCode());
                    LogFileUtil.write(TAG + " onBufferingUpdate percent = " + percent + " " + SavorVideoView.this.hashCode());

                    // 播放器状态不对时getDuration报错，也没必要处理播放、暂停了，这里直接return
                    if (mPlayState == MediaPlayerState.IDLE || mPlayState == MediaPlayerState.INITIALIZED ||
                            mPlayState == MediaPlayerState.PREPARING || mPlayState == MediaPlayerState.ERROR)
                        return;

                    int currentPercent = mp.getCurrentPosition() * 100 / mp.getDuration();
                    LogUtils.v(TAG + "onBufferingUpdate currentPercent = " + currentPercent + " position = " + mp.getCurrentPosition() + " duration = " + mp.getDuration() + " " + SavorVideoView.this.hashCode());
                    LogFileUtil.write(TAG + " onBufferingUpdate currentPercent = " + currentPercent + " position = " + mp.getCurrentPosition() + " duration = " + mp.getDuration() + " " + SavorVideoView.this.hashCode());
//                    if (mp.getCurrentPosition() + 400 < mp.getDuration()) {
                    if (percent < 99 && currentPercent >= percent - 1) {
                        // 缓冲部分不足时，暂停播放并显示进度圈
                        if (mIfShowLoading) {
                            mProgressBar.setVisibility(VISIBLE);
                        }
                        if (mPlayState == MediaPlayerState.STARTED) {
                            pauseInner();
                        }
                    } else {
                        // 缓冲好时，继续播放并隐藏进度圈
                        if (mIfShowLoading) {
                            mProgressBar.setVisibility(GONE);
                        }
                        if (mPlayState == MediaPlayerState.PAUSED && !mIsPauseByOut) {
                            playInner();
                        }
                    }
//                    } else {
//                        // 缓冲好时，继续播放并隐藏进度圈
//                        if (mIfShowLoading) {
//                            mProgressBar.setVisibility(GONE);
//                        }
//                        if (mPlayState == MediaPlayerState.PAUSED && !mIsPauseByOut) {
//                            playInner();
//                        }
//                    }
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    LogUtils.w(TAG + "MediaPlayer onMediaPrepared " +  " mIsSurfaceCreated:" + mIsSurfaceCreated +
                            " mIsPauseByOut:" + mIsPauseByOut +" mAssignedPlayPosition:" + mAssignedPlayPosition
                            + " " + SavorVideoView.this.hashCode());
                    LogFileUtil.write(TAG + " MediaPlayer onMediaPrepared " +  " mIsSurfaceCreated:" + mIsSurfaceCreated +
                            " mIsPauseByOut:" + mIsPauseByOut +" mAssignedPlayPosition:" + mAssignedPlayPosition
                            + " " + SavorVideoView.this.hashCode());
                    mPlayState = MediaPlayerState.PREPARED;

                    if (mIfHandlePrepareTimeout) {
                        // 准备开始播放移除Runnable
                        removeCallbacks(mPrepareTimeoutRunnable);
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

                        // 如果Surface创建完毕且没被外部强行停止时，开始播放
                        if (mIsSurfaceCreated && !mIsPauseByOut) {
                            LogUtils.d("Will setDisplay in onPrepared() when surface is created");
                            mp.setDisplay(mSurfaceHolder);

                            if (mAssignedPlayPosition > 0 && mAssignedPlayPosition < mp.getDuration()) {
                                mp.seekTo(mAssignedPlayPosition);
                                mAssignedPlayPosition = -1;
                            } else {
                                playInner();
                            }
                        }
                    }
                }
            });
            mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    ViewGroup.LayoutParams layoutParams = mSurfaceTv.getLayoutParams();
                    int viewWidth = getWidth();
                    int viewHeight = getHeight();
                    LogUtils.w(TAG + "MediaPlayer onVideoSizeChanged width = " + width + " height = " + height +
                            " viewWidth = " + viewWidth + " viewHeight = " + viewHeight + " " + SavorVideoView.this.hashCode());
                    LogFileUtil.write(TAG + " MediaPlayer onVideoSizeChanged width = " + width + " height = " + height +
                            " viewWidth = " + viewWidth + " viewHeight = " + viewHeight + " " + SavorVideoView.this.hashCode());
                    if (((double) viewWidth / width) * height > viewHeight) {
                        layoutParams.width = (int) (((double) viewHeight / height) * width);
                        layoutParams.height = viewHeight;
                    } else {
                        layoutParams.width = viewWidth;
                        layoutParams.height = (int) (((double) viewWidth / width) * height);
                    }
//                    adjustAspectRatio(width, height);
                }
            });
            mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    LogUtils.w(TAG + "MediaPlayer onSeekComplete" + " " + SavorVideoView.this.hashCode());
                    LogFileUtil.write(TAG + " MediaPlayer onSeekComplete" + " " + SavorVideoView.this.hashCode());
                    if (mMediaPlayer != null && mPlayState == MediaPlayerState.PREPARED) {
                        playInner();
                    }
                }
            });
            mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    LogUtils.w(TAG + "MediaPlayer setOnInfoListener" + " what=" + what + " extra=" + extra + " " + SavorVideoView.this.hashCode());
                    LogFileUtil.write(TAG + " MediaPlayer setOnInfoListener" + " what=" + what + " extra=" + extra + " " + SavorVideoView.this.hashCode());
                    if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
                        mRootRl.removeCallbacks(mBufferTimeoutRunnable);
                        mRootRl.postDelayed(mBufferTimeoutRunnable, MAX_BUFFER_TIME);
                    } else if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
                        mRootRl.removeCallbacks(mBufferTimeoutRunnable);
                    }
                    return false;
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    LogUtils.e(TAG + "MediaPlayer onError what = " + what + " extra = " + extra + " " + SavorVideoView.this.hashCode());
                    LogFileUtil.write(TAG + " MediaPlayer onError what = " + what + " extra = " + extra + " " + SavorVideoView.this.hashCode());
                    if (!mp.equals(mMediaPlayer)) {
                        return true;
                    }
                    mPlayState = MediaPlayerState.ERROR;

                    int position = mp.getCurrentPosition();

                    // 根据出错类型，判断是否可继续尝试播放此视频源
                    if (extra == MediaPlayerError.ERROR_NOT_CONNECTED) {
                        // 网络连接错误的时候会进到ERROR_NOT_CONNECTED这个错误类型，这时reset MediaPlayer并记录播放进度
                        LogFileUtil.write(TAG + " will resetAndPreparePlayer at method onErrorNet" + " " + SavorVideoView.this.hashCode());
                        resetAndPreparePlayer(position);
                    } else {
                        boolean beenResetSource = false;
                        if (mPlayStateCallback != null) {
                            boolean isLast = false;
                            if (mMediaFiles != null && mMediaFiles.size() > 0) {
                                isLast = mCurrentFileIndex == mMediaFiles.size() - 1;
                            }
                            // 回调某个视频播放出错
                            beenResetSource = mPlayStateCallback.onMediaError(mCurrentFileIndex, isLast);
                        }

                        if (!beenResetSource) {
                            if (mForcePlayFromStart) {
                                // 强制从头播放
                                mForcePlayFromStart = false;
                                mCurrentFileIndex = 0;
                                mAssignedPlayPosition = 0;
                            } else /*if (position == mMediaPlayer.getDuration()) */{
                                // 播放下一个
                                if (mMediaFiles != null && mMediaFiles.size() > 0){
                                    mCurrentFileIndex = (mCurrentFileIndex + 1) % mMediaFiles.size();
                                    mAssignedPlayPosition = 0;
                                }
                            }
                            if (mIsLooping) {
                                LogFileUtil.write(TAG + " will resetAndPreparePlayer at method onError" + " " + SavorVideoView.this.hashCode());
                                resetAndPreparePlayer();
                            }
                        }
                    }
                    return true;
                }
            });

            try {
                if (mIsSurfaceCreated) {
                    LogUtils.d("Will setDisplay in initMediaPlayer() when surface is created");
                    LogFileUtil.write("Will setDisplay in initMediaPlayer() when surface is created");
                    mMediaPlayer.setDisplay(mSurfaceHolder);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *抽取播放完成方法
     */
    private void extractCompletion(){
        mPlayState = MediaPlayerState.COMPLETED;

        boolean beenResetSource = false;
        if (mPlayStateCallback != null) {
            boolean isLast = false;
            if (mMediaFiles != null && mMediaFiles.size() > 0) {
                isLast = mCurrentFileIndex == mMediaFiles.size() - 1;
            }
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
                LogFileUtil.write(TAG + " will resetAndPreparePlayer at method extractCompletion" + " " + SavorVideoView.this.hashCode());
                resetAndPreparePlayer();
            }
        }
    }

    /**
     * 设置播放数据源
     */
    private boolean setMediaPlayerSource() {
        LogUtils.w(TAG + " setMediaPlayerSource mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " setMediaPlayerSource mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (mMediaPlayer == null) {
            LogUtils.e(TAG + " setMediaPlayerSource mMediaPlayer == null " + " " + SavorVideoView.this.hashCode());
            LogFileUtil.write(TAG + " setMediaPlayerSource mMediaPlayer == null " + " " + SavorVideoView.this.hashCode());
            return false;
        }
        if (mPlayState != MediaPlayerState.IDLE) {
            LogUtils.e(TAG + " setMediaPlayerSource in illegal state: " + mPlayState + " " + SavorVideoView.this.hashCode());
            LogFileUtil.write(TAG + " setMediaPlayerSource in illegal state: " + mPlayState + " " + SavorVideoView.this.hashCode());
            return false;
        }
        if (mMediaFiles == null || mMediaFiles.isEmpty() || mCurrentFileIndex >= mMediaFiles.size() || TextUtils.isEmpty(mMediaFiles.get(mCurrentFileIndex))) {
            LogUtils.e(TAG + " setMediaPlayerSource in garbled source, mCurrentFileIndex =  " + mCurrentFileIndex + " " + SavorVideoView.this.hashCode());
            LogFileUtil.write(TAG + " setMediaPlayerSource in garbled source, mCurrentFileIndex =  " + mCurrentFileIndex + " " + SavorVideoView.this.hashCode());
            return false;
        }
        try {
            LogUtils.w("开始播放：" + mMediaFiles.get(mCurrentFileIndex) + " " + SavorVideoView.this.hashCode());
//            mMediaPlayer.setDataSource(mMediaFiles.get(mCurrentFileIndex));
            String url = mMediaFiles.get(mCurrentFileIndex);
            if (url.endsWith("mp4")||url.endsWith("MP4")){
                isVideoAds = true;
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    mHandler.removeCallbacksAndMessages(null);
                    atlasViewPager.setVisibility(GONE);
                } else {
                    mHandler.removeCallbacksAndMessages(null);
                    post(()->atlasViewPager.setVisibility(View.GONE));
                }
                mMediaPlayer.setDataSource(url);
                mPlayState = MediaPlayerState.INITIALIZED;
            }else {
                isVideoAds = false;
                if (mPlayStateCallback!=null){
                    mPlayStateCallback.onMediaPrepared(mCurrentFileIndex);
                }
                mCurrentIndex = 0;
                mDataSource= new ArrayList<>();
                if (url.contains(",")){
                    String[] urlArray = url.split(",");
                    for (String u:urlArray){
                        mDataSource.add(u);
                    }
                }else{
                    mDataSource.add(url);
                }
                atlasViewPager.setVisibility(VISIBLE);
                imageAdapter = new StringPagerAdapter(mContext,mDataSource);
                atlasViewPager.setAdapter(imageAdapter);
                if (mDataSource != null && mDataSource.size() == 1) {
                    removeCallbacks(mPlayCompletionRunnable);
                    postDelayed(mPlayCompletionRunnable ,duration>0?duration*1000:5*1000);
                }else if (mDataSource != null && mDataSource.size() > 1){
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.sendEmptyMessageDelayed(0, duration*1000);
                }
//                File file = new File(url);
//                if (Looper.myLooper() == Looper.getMainLooper()) {
//                    GlideImageLoader.loadLocalImage(mContext,file,mImgView);
//                }else {
//                    post(()->GlideImageLoader.loadLocalImage(mContext,file,mImgView));
//                }
//
//                projectTipAnimateIn();
//                removeCallbacks(mPlayCompletionRunnable);
//                postDelayed(mPlayCompletionRunnable ,duration>0?duration*1000:15*1000);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Runnable mPlayCompletionRunnable = new Runnable() {
        @Override
        public void run() {
//            projectTipAnimateOut();
//            mImgView.setVisibility(View.GONE);
            atlasViewPager.setVisibility(GONE);
            LogFileUtil.write(TAG + " setMediaPlayerSource-extractCompletion-mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
            extractCompletion();
        }
    };

//    public void projectTipAnimateIn() {
//
//        if (Looper.myLooper() == Looper.getMainLooper()) {
//            doAnimationIn();
//        } else {
//            mImgView.post(new Runnable() {
//                @Override
//                public void run() {
//                    doAnimationIn();
//                }
//            });
//        }
//    }

//    private void doAnimationIn() {
//        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1, Animation.RELATIVE_TO_SELF, 0,
//                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
//        animation.setDuration(1000);
//        animation.setFillAfter(true);
//        mImgView.setVisibility(View.VISIBLE);
//        mImgView.startAnimation(animation);
//    }

//    private void projectTipAnimateOut() {
//        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 1,
//                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
//        animation.setDuration(1000);
//        animation.setFillAfter(true);
//        mImgView.startAnimation(animation);
//    }

    /**
     * 准备播放
     */
    private void prepareMediaPlayer() {
        LogUtils.w(TAG + " prepareMediaPlayer mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " prepareMediaPlayer mPlayState:" + mPlayState + " mIsSurfaceCreated:" +
                mIsSurfaceCreated + " " + SavorVideoView.this.hashCode());
        if (mIsSurfaceCreated) {
            if (mPlayState != MediaPlayerState.INITIALIZED) {
                LogUtils.e(TAG + " prepareMediaPlayer in illegal state: " + mPlayState + " " + SavorVideoView.this.hashCode());
                LogFileUtil.write(TAG + " prepareMediaPlayer in illegal state: " + mPlayState + " " + SavorVideoView.this.hashCode());
                return;
            }
            mMediaPlayer.prepareAsync();
            mPlayState = MediaPlayerState.PREPARING;

            if (mIfShowLoading) {
//                GlideImageLoader.loadImage(getContext(), Environment.getExternalStorageDirectory().getAbsolutePath() +
//                        Session.get(getContext()).getLoadingPath(), mLoadingIv, 0, R.mipmap.ads);
//                mLoadingIv.setVisibility(VISIBLE);
                mProgressBar.setVisibility(VISIBLE);
            }

            if (mIfHandlePrepareTimeout) {
                removeCallbacks(mPrepareTimeoutRunnable);
                postDelayed(mPrepareTimeoutRunnable, MAX_PREPARE_TIME);
            }
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
                    mPlayStateCallback.onMediaError(mCurrentFileIndex, isLast);
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
        LogUtils.w(TAG + " resetAndPreparePlayer assignedPlayPosition = " + assignedPlayPosition + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " resetAndPreparePlayer assignedPlayPosition = " + assignedPlayPosition + " " + SavorVideoView.this.hashCode());
        if (mMediaPlayer!=null){
            mMediaPlayer.reset();
        }
        mPlayState = MediaPlayerState.IDLE;

        mAssignedPlayPosition = assignedPlayPosition;

        if (mMediaFiles != null && mMediaFiles.size() > 0) {
            setAndPrepare();
        }
    }

    private void setAndPrepare() {
        if (setMediaPlayerSource()) {
            if (isVideoAds){
                prepareMediaPlayer();
            }
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
                LogFileUtil.write(TAG + " will resetAndPreparePlayer at method setAndPrepare" + " " + SavorVideoView.this.hashCode());
                resetAndPreparePlayer();
            }
        }
    }

    public boolean tryPlay() {
        LogUtils.w(TAG + " tryPlay mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " tryPlay mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (MediaPlayerState.PAUSED == mPlayState) {
            mIsPauseByOut = false;
            playInner();

            if (mIfShowPauseBtn) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    mPlayVideoIv.setVisibility(GONE);
                } else {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mPlayVideoIv.setVisibility(GONE);
                        }
                    });
                }
            }

            // 回调某个视频播放恢复播放
            if (mPlayStateCallback != null) {
                mPlayStateCallback.onMediaResume(mCurrentFileIndex);
            }

            return true;
        } else if (MediaPlayerState.STARTED == mPlayState)  {
            return true;
        } else {
            return false;
        }
    }

    private void playInner() {
        LogUtils.w(TAG + " playInner mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " playInner mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        try {
            mMediaPlayer.start();
            mPlayState = MediaPlayerState.STARTED;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            LogUtils.e(TAG + " Exception when playInner");
            LogFileUtil.write(TAG + " Exception when playInner");
        }
    }

    /**
     * 暂停播放
     */
    public boolean tryPause() {
        LogUtils.w(TAG + " tryPause mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " tryPause mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (MediaPlayerState.STARTED == mPlayState) {
            mIsPauseByOut = true;
            pauseInner();

            if (mIfShowPauseBtn) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    mPlayVideoIv.setVisibility(VISIBLE);
                } else {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mPlayVideoIv.setVisibility(VISIBLE);
                        }
                    });
                }
            }

            // 回调某个视频暂停
            if (mPlayStateCallback != null) {
                mPlayStateCallback.onMediaPause(mCurrentFileIndex);
            }
            return true;
        } else if (MediaPlayerState.PAUSED == mPlayState)  {
            return true;
        } else {
            return false;
        }
    }

    private void pauseInner() {
        LogUtils.w(TAG + " pauseInner mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " pauseInner mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        mMediaPlayer.pause();
        mPlayState = MediaPlayerState.PAUSED;
    }

    /**
     * 停止播放
     */
    public void stop() {
        LogUtils.w(TAG + " stop mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " stop mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        stopInner();

        mIsPauseByOut = true;
    }

    /**
     * 停止播放
     */
    private void stopInner() {
        LogUtils.w(TAG + " stopInner mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " stopInner mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (isInPlaybackState() && mMediaPlayer != null) {
            mMediaPlayer.stop();
            mPlayState = MediaPlayerState.STOPPED;
        }
    }

    /**
     * 所在页面onPause时请调用此方法处理，类似的还有{@link #onResume()}
     */
    public void onPause() {
        LogFileUtil.write(TAG + " onPause mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (mMediaPlayer != null) {
            try {
                // 记录播放进度
                mAssignedPlayPosition = mMediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mHandler.removeCallbacksAndMessages(null);
            removeCallbacks(mPlayCompletionRunnable);
            release();
        }
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    public void onStop() {
        LogUtils.w(TAG + " onPause mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " onPause mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());

        removeCallbacks(mPrepareTimeoutRunnable);

        if (mPlayStateCallback != null) {
            mPlayStateCallback.onMediaPause(mCurrentFileIndex);
        }
//        mMediaPlayer.reset();
//        mPlayState = MediaPlayerState.IDLE;
    }

    /**
     * 所在页面onResume时请调用此方法处理，类似的还有{@link #onPause()}
     */
    public void onResume() {
        LogUtils.w(TAG + " onResume mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " onResume mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (mMediaPlayer == null) {
            initMediaPlayer();
        }

        mIsPauseByOut = false;
        if (mPlayState == MediaPlayerState.IDLE && mMediaFiles != null && mMediaFiles.size() > 0) {
            setAndPrepare();
        }

        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }


    /**
     * 停止播放并释放MediaPlayer
     */
    public void release() {
        LogUtils.w(TAG + " release mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (mMediaPlayer != null) {
            stopInner();

            if (mMediaPlayer != null) {
                mMediaPlayer.release();
            }
            mPlayState = MediaPlayerState.END;
            mMediaPlayer = null;
        }

        if (mIfShowPauseBtn) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                mPlayVideoIv.setVisibility(GONE);
            } else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayVideoIv.setVisibility(GONE);
                    }
                });
            }
        }
    }

    /**
     * 设置播放源
     *
     * @param mediaFiles
     */
    public void setMediaFiles(ArrayList<String> mediaFiles) {
        setMediaFiles(mediaFiles, 0, 0);
    }

    /**
     * 设置播放源
     *
     * @param mediaFiles       文件路径集合
     * @param currentFileIndex 要播放的文件序号
     * @param playPosition     文件播放进度
     */
    public void setMediaFiles(ArrayList<String> mediaFiles, int currentFileIndex, int playPosition) {
        LogUtils.w(TAG + "setMediaFiles mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " setMediaFiles mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        mIsPauseByOut = false;
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            mCurrentFileIndex = currentFileIndex;
            mAssignedPlayPosition = playPosition;
            mMediaFiles = mediaFiles;

//            if (mPlayState == MediaPlayerState.STARTED || mPlayState == MediaPlayerState.PAUSED) {
//                // 重设播放源集合后，将 mForcePlayFromStart置true强制从头播放
//                mForcePlayFromStart = true;
//            }

            if (mMediaPlayer == null) {
                initMediaPlayer();
                if (mIsSurfaceCreated) {
                    LogUtils.d("Will setDisplay in setMediaFiles() when surface is created");
                    mMediaPlayer.setDisplay(mSurfaceHolder);
                }
            }

            // 非IDLE状态时先重置再设置
            if (mPlayState != MediaPlayerState.IDLE) {
                mMediaPlayer.reset();
                mPlayState = MediaPlayerState.IDLE;
            }
            mPlayVideoIv.setVisibility(GONE);

            setAndPrepare();
        }
    }

    /**
     * 继续、暂停 播放
     */
    public void togglePlay() {
        LogUtils.w(TAG + "togglePlay mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " togglePlay mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (MediaPlayerState.PAUSED == mPlayState) {
            tryPlay();
        } else if (MediaPlayerState.STARTED == mPlayState) {
            tryPause();
        }
    }

    /**
     * 播放下一条
     */
    public void playNext() {
        LogUtils.w(TAG + " playNext mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " playNext mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        stopInner();

        mIsPauseByOut = false;
        mAssignedPlayPosition = 0;
        mCurrentFileIndex = (mCurrentFileIndex + 1) % mMediaFiles.size();
        LogUtils.w(TAG + " mCurrentFileIndex:" + mCurrentFileIndex + " size = " + mMediaFiles.size() + " " + SavorVideoView.this.hashCode());
        resetAndPreparePlayer();
    }

    /**
     * 播放上一条
     */
    public void playPrevious() {
        LogUtils.w(TAG + " playPrevious mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " playPrevious mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        stopInner();

        mIsPauseByOut = false;
        mAssignedPlayPosition = 0;
        mCurrentFileIndex =(mCurrentFileIndex - 1 + mMediaFiles.size()) % mMediaFiles.size();
        LogUtils.w(TAG + " mCurrentFileIndex:" + mCurrentFileIndex + " size = " + mMediaFiles.size() + " " + SavorVideoView.this.hashCode());
        resetAndPreparePlayer();
    }

    /**
     * 调整播放进度
     *
     * @param position
     */
    public void seekTo(int position) {
        LogUtils.w(TAG + " seek mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " seek mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(position);
        } else {
            mAssignedPlayPosition = position;
        }
    }

    /**
     * 播放器是否处于可播放的状态
     *
     * @return
     */
    public boolean isInPlaybackState() {
        return mPlayState == MediaPlayerState.PAUSED ||
                mPlayState == MediaPlayerState.STARTED ||
                mPlayState == MediaPlayerState.COMPLETED ||
                mPlayState == MediaPlayerState.PREPARED;
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
        LogUtils.w(TAG + " getCurrentPosition mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        LogFileUtil.write(TAG + " getCurrentPosition mPlayState:" + mPlayState + " " + SavorVideoView.this.hashCode());
        if (isInPlaybackState())
            return mMediaPlayer.getCurrentPosition();
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
    }

    /**
     * 是否自动播放下一个
     * 默认是自动播放
     * @param looping
     */
    public void setLooping(boolean looping) {
        mIsLooping = looping;
    }

    public void setIfShowLoading(boolean ifShowLoading) {
        mIfShowLoading = ifShowLoading;
    }

    public void setAdsDuration(int duration){
        this.duration = duration;
    }


    public interface PlayStateCallback {
        /**
         * 某个视频播放完毕
         * @param index 当前视频序号
         * @param isLast 是否是最后一个
         * @return true: 播放源被设置新的； false: otherwise
         */
        boolean onMediaComplete(int index, boolean isLast);
        /**
         * 某个视频播放出错
         * @param index 当前视频序号
         * @param isLast 是否是最后一个
         * @return true: 播放源被设置新的； false: otherwise
         */
        boolean onMediaError(int index, boolean isLast);
        /**
         * 视频准备完毕
         * @param index 当前视频序号
         * @return true: 播放被中止； false: otherwise
         */
        boolean onMediaPrepared(int index);

        void onMediaPause(int index);

        void onMediaResume(int index);
    }
}

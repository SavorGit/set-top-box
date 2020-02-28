package com.savor.ads.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.savor.ads.R;
import com.savor.ads.SavorApplication;
import com.savor.ads.bean.AtvProgramInfo;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.dialog.TvChannelListDialog;
import com.savor.ads.dialog.TvChannelSearchingDialog;
import com.savor.ads.log.LogReportUtil;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.KeyCode;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;
import com.savor.ads.utils.tv.TvOperate;
import com.tvos.common.TvManager;
import com.tvos.common.TvPlayer;
import com.tvos.common.exception.TvCommonException;
import com.tvos.common.vo.TvOsType;

import java.util.ArrayList;

public class TvPlayerActivity extends BaseActivity {

    /**
     * 是否自动搜索节目
     */
    public static final String EXTRA_IS_AUTO_SEARCHING = "extra_is_auto_searching";
    /**
     * 跳转前的广告视频ID
     */
    public static final String EXTRA_LAST_VID = "extra_last_vid";

    private SurfaceView mPreviewSv;
    private SurfaceHolder mSurfaceHolder;
    private boolean mIsSurfaceCreated;
    private RelativeLayout mChannelTipRl;
    private TextView mChannelNumberTv;
    private TextView mChannelNameTv;

    private TvOperate mTvOperate;

    private TvChannelListDialog mChannelListDialog;
    private TvChannelSearchingDialog mChannelSearchingDialog;

    private Handler mHandler = new Handler();

    private Runnable mBackToAdsPlayerRunnable = new Runnable() {
        @Override
        public void run() {
            gotoAdsPlayer();
        }
    };
    private Runnable mHideChannelSearchingRunnable = new Runnable() {
        @Override
        public void run() {
            if (mChannelSearchingDialog != null && mChannelSearchingDialog.isShowing()) {
                mChannelSearchingDialog.dismiss();
            }
        }
    };
    private Runnable mHideChannelTipRunnable = new Runnable() {
        @Override
        public void run() {
            if (mChannelTipRl != null) {
                mChannelTipRl.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 输入信号源集合
     */
    private final TvOsType.EnumInputSource[] mInputSource = new TvOsType.EnumInputSource[]
            {
                    TvOsType.EnumInputSource.E_INPUT_SOURCE_ATV,
                    TvOsType.EnumInputSource.E_INPUT_SOURCE_HDMI,
                    TvOsType.EnumInputSource.E_INPUT_SOURCE_CVBS
            };
    /**
     * 频道列表
     */
    private ArrayList<AtvProgramInfo> mChannelList;
    /**
     * 是否以自动搜台方式进入，目前设置页会这样进入
     */
    private boolean mIsAutoTurning;
    /**
     * 日志用的播放记录标识
     */
    private String mUUID;

//    private boolean mIsSurfaceDestroyed;

    private int mCurrentProgramIndex;

    private long mActivityResumeTime;

    private String mLastAdsVid;

    private static final int DELAY_TIME = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_player);

        handleIntent();
        findView();
        setView();
    }

    private void handleIntent() {
        if (getIntent() != null) {
            mIsAutoTurning = getIntent().getBooleanExtra(EXTRA_IS_AUTO_SEARCHING, false);
            mLastAdsVid = getIntent().getStringExtra(EXTRA_LAST_VID);
        }
    }

    private void findView() {
        mPreviewSv = (SurfaceView) findViewById(R.id.surface_view);
        mChannelTipRl = (RelativeLayout) findViewById(R.id.rl_channel_tip);
        mChannelNameTv = (TextView) findViewById(R.id.tv_channel_name);
        mChannelNumberTv = (TextView) findViewById(R.id.tv_channel_number);
    }

    private void setView() {
        mSurfaceHolder = mPreviewSv.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                TvPlayer tvPlayer = TvManager.getPlayerManager();
                try {
//                    if (!mIsSurfaceDestroyed) {
                    tvPlayer.setDisplay(holder);
                    // 产生一个新的UUID作为日志标识
                    mUUID = String.valueOf(System.currentTimeMillis());
                    LogReportUtil.get(mContext).sendAdsLog(mUUID, mSession.getBoiteId(), mSession.getRoomId(),
                            String.valueOf(System.currentTimeMillis()), "start", "tv", mLastAdsVid,
                            "", mSession.getVersionName(), mSession.getAdsPeriod(), mSession.getBirthdayOndemandPeriod(),
                            "");
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mIsSurfaceCreated = true;
//                mIsSurfaceDestroyed = false;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // 这里只是为了防止到这里的时候mUUID没值，正常mUUID肯定会在surfaceCreated()中赋值
                if (TextUtils.isEmpty(mUUID)) {
                    mUUID = String.valueOf(System.currentTimeMillis());
                }
                LogReportUtil.get(mContext).sendAdsLog(mUUID, mSession.getBoiteId(), mSession.getRoomId(),
                        String.valueOf(System.currentTimeMillis()), "end", "tv", mLastAdsVid,
                        "", mSession.getVersionName(), mSession.getAdsPeriod(), mSession.getBirthdayOndemandPeriod(),
                        "");

//                mIsSurfaceDestroyed = true;
                mIsSurfaceCreated = false;
            }
        });
    }

    private void init() {
        mTvOperate = new TvOperate();

        TvOsType.EnumInputSource inputSource = mInputSource[mSession.getTvInputSource()];
        try {
            TvManager.setInputSource(inputSource);
        } catch (TvCommonException e) {
            e.printStackTrace();
        }

        // 填充节目列表到mChannelList
        fillChannelList();

        // 输入源为ANT模拟电视信号时，显示当前频道
        if (inputSource == TvOsType.EnumInputSource.E_INPUT_SOURCE_ATV) {
            if (mChannelList == null || mChannelList.size() == 0) {
                mChannelTipRl.setVisibility(View.GONE);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ShowMessage.showToast(mContext, "未获取到频道信息");
                    }
                });
            } else {
                initCurrentProgram();
            }
        } else {
            mChannelTipRl.setVisibility(View.GONE);
        }

        if (mIsAutoTurning) {
            mIsAutoTurning = false;
            autoTurning();
        } else {
            if (mSession.getTvInputSource() >= mInputSource.length)
                mSession.setTvInputSource(0);
        }
        setVolume(mSession.getTvVolume());

        int switchTime = mSession.getSwitchTime();
        if (switchTime > 0 && switchTime != 999) {
            // 添加延时切换到广告播放的Runnable, 999被定义为不切换
            mHandler.postDelayed(mBackToAdsPlayerRunnable, 60 * 1000 * switchTime);
        }
    }

    private void initCurrentProgram() {
        boolean foundProgram = false;
        for (int i = 0, mChannelListSize = mChannelList.size(); i < mChannelListSize; i++) {
            AtvProgramInfo program = mChannelList.get(i);
            if (program.getChennalNum() == mSession.getTvCurrentChannelNumber()) {
                mCurrentProgramIndex = i;
                foundProgram = true;
                break;
            }
        }
        if (!foundProgram) {
            mSession.setTvCurrentChannelNumber(mChannelList.get(0).getChennalNum());
            mCurrentProgramIndex = 0;
        }
        mTvOperate.switchATVChannel(mSession.getTvCurrentChannelNumber());

        showChannelTips();
    }

    public void autoTurning() {
        /** 搜完台之后节目列表获取不到，搜台功能先注掉 */
        // 自动搜台时直接把输入源切为ANT
        mSession.setTvInputSource(0);

        mTvOperate.autoTuning(mTurningCallback);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showAutoTurningDialog();
            }
        });
    }

    private TvOperate.AutoTurningCallback mTurningCallback = new TvOperate.AutoTurningCallback() {
        @Override
        public void onProgressUpdate(int percent) {
            if (mChannelSearchingDialog != null && mChannelSearchingDialog.isShowing()) {
                mChannelSearchingDialog.updateProgress(percent);
            }
        }

        @Override
        public void onComplete() {
            if (mChannelSearchingDialog != null && mChannelSearchingDialog.isShowing()) {
                mChannelSearchingDialog.dismiss();
            }

            fillChannelList();

            if (mChannelList == null || mChannelList.size() == 0) {
                mChannelTipRl.setVisibility(View.GONE);
                ShowMessage.showToastLong(mContext, "未获取到频道信息");
            } else {
                initCurrentProgram();
            }

            GlobalValues.IS_BOX_BUSY = false;
        }
    };

    private void showChannelListDialog() {

        if (mChannelListDialog == null) {
            mChannelListDialog = new TvChannelListDialog(this, mChannelList, mChannelSelectCallback);
        } else {
            mChannelListDialog.setChannels(mChannelList);
        }
        if (!mChannelListDialog.isShowing()) {
            mChannelListDialog.show();
        }
    }

    private TvChannelListDialog.ChannelSelectCallback mChannelSelectCallback = new TvChannelListDialog.ChannelSelectCallback() {
        @Override
        public void onChannelSelect(int index) {
            mCurrentProgramIndex = index;

            changeChannel(mChannelList.get(index));
        }
    };

    /**
     * 改变频道
     *
     * @param program
     */
    private void changeChannel(AtvProgramInfo program) {
        if (program.getChennalNum() == mSession.getTvCurrentChannelNumber())
            return;
        mTvOperate.switchATVChannel(program.getChennalNum());
        mSession.setTvCurrentChannelNumber(program.getChennalNum());
        showChannelTips();
    }

    private void showAutoTurningDialog() {
        if (mChannelSearchingDialog == null) {
            mChannelSearchingDialog = new TvChannelSearchingDialog(this);
            mChannelSearchingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    LogUtils.d("mChannelSearchingDialog onDismiss");
                    GlobalValues.IS_BOX_BUSY = false;
                    mTvOperate.interruptTuning();
                }
            });
        }
        if (!mChannelSearchingDialog.isShowing()) {
            mChannelSearchingDialog.show();
        }
    }

    private void showChannelTips() {
        mHandler.removeCallbacks(mHideChannelTipRunnable);
        String name = "";
        for (AtvProgramInfo pro : mChannelList) {
            if (pro.getChennalNum() == mSession.getTvCurrentChannelNumber()) {
                name = pro.getChannelName();
                LogUtils.d("name:" + pro.getChannelName() + " freq:" + pro.getFreq());
                break;
            }
        }
        final String finalName = name;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChannelTipRl.setVisibility(View.VISIBLE);
                mChannelNumberTv.setText(String.format("%03d", (mSession.getTvCurrentChannelNumber())));
                mChannelNameTv.setText(finalName);
            }
        });

        mHandler.postDelayed(mHideChannelTipRunnable, 5 * 1000);
    }

    /**
     * TvOperate获取节目列表
     */
    private void fillChannelList() {

        AtvProgramInfo[] programList = mTvOperate.getAllProgramInfo();
        mChannelList = new ArrayList<>();
        if (programList != null && programList.length > 0) {
            for (AtvProgramInfo program : programList) {
                program.setChennalNum(program.getChennalNum() + 1);
                mChannelList.add(program);
            }
        }
//        for (int i = 0; i < programList.length; i++) {
//
////            int chennalNum = programList[i].chennalNum;
////            int freq = programList[i].freq;
////            int audioStandard = programList[i].audioStandard;
////            int videoStandard = programList[i].videoStandard;
//            String stationName = programList[i].getChannelName();
//
//            mChannelList.add(stationName);
//
//        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 禁止进入页面后马上操作
        if (System.currentTimeMillis() - mActivityResumeTime < ConstantValues.KEY_DOWN_LAG + DELAY_TIME * 1000)
            return true;

        boolean handled = false;
        if (keyCode == KeyCode.KEY_CODE_SHOW_QRCODE) {
            ((SavorApplication) getApplication()).showQrCodeWindow(null);
            handled = true;

        } else if (keyCode == KeyCode.KEY_CODE_SHOW_INFO) {// 对话框弹出后会获得焦点，所以这里不需要处理重复点击重复显示的问题
            showBoxInfo();
            handled = true;

            // 切换信号源
        } else if (keyCode == KeyCode.KEY_CODE_CHANGE_SIGNAL) {
            switchInputSource();
            handled = true;

            // 切换到广告模式
        } else if (keyCode == KeyCode.KEY_CODE_CHANGE_MODE) {
            gotoAdsPlayer();
            handled = true;

            //切换到电视TV输入源
        } else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            switchInputSource(0);
            showChannelTips();
            handled = true;

            //切换到HDMI输入源
        } else if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            switchInputSource(1);
            handled = true;

            //切换到AV输入源
        } else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            switchInputSource(2);
            handled = true;

            // 上传频道列表
        } else if (keyCode == KeyCode.KEY_CODE_UPLOAD_CHANNEL_INFO) {
            if (mSession.getTvInputSource() == 0 && mChannelList != null && mChannelList.size() > 0) {
                uploadProgram();
                handled = true;
            }

            // 上一台
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mSession.getTvInputSource() == 0 && mChannelList != null && mChannelList.size() > 0) {
                mCurrentProgramIndex = (mCurrentProgramIndex - 1 + mChannelList.size()) % mChannelList.size();
                changeChannel(mChannelList.get(mCurrentProgramIndex));
                handled = true;
            }

            // 下一台
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mSession.getTvInputSource() == 0 && mChannelList != null && mChannelList.size() > 0) {
                mCurrentProgramIndex = (mCurrentProgramIndex + 1) % mChannelList.size();
                changeChannel(mChannelList.get(mCurrentProgramIndex));
                handled = true;
            }

            // 频道列表
        } else if (keyCode == KeyCode.KEY_CODE_CHANNEL_LIST) {
            if (mSession.getTvInputSource() == 0 && mChannelList != null && mChannelList.size() > 0) {
                showChannelListDialog();
                handled = true;
            }

        } else if (keyCode == KeyEvent.KEYCODE_BACK) {// 屏蔽后退
            handled = true;

        }
        return handled || super.onKeyDown(keyCode, event);
    }

    private void gotoAdsPlayer() {
        if (GlobalValues.getInstance().PLAY_LIST == null || GlobalValues.getInstance().PLAY_LIST.isEmpty()) {
            // 尝试填充播放列表
            fillPlayList();
        }

        if (GlobalValues.getInstance().PLAY_LIST != null && !GlobalValues.getInstance().PLAY_LIST.isEmpty()) {
            Intent intent = new Intent(this, AdsPlayerActivity.class);
            startActivity(intent);
        } else {
            ShowMessage.showToast(this, "未发现可播放轮播内容，无法跳转");
        }
    }

    private void uploadProgram() {

        AtvProgramInfo[] programs = mTvOperate.getAllProgramInfo();
        // 服务器改成返回ChennalNum从1开始，这里统一加1后再上传
        if (programs != null && programs.length > 0) {
            for (AtvProgramInfo program : programs) {
                program.setChennalNum(program.getChennalNum() + 1);
            }
        }

        AppApi.uploadProgram(this, new ApiRequestListener() {
            @Override
            public void onSuccess(AppApi.Action method, Object obj) {
                ShowMessage.showToastLong(mContext, "上传成功");
            }

            @Override
            public void onError(AppApi.Action method, Object obj) {
                ShowMessage.showToastLong(mContext, "上传失败");
            }

            @Override
            public void onNetworkFailed(AppApi.Action method) {
                ShowMessage.showToastLong(mContext, "上传失败");
            }
        }, programs);
    }

    private void switchInputSource() {

        int index = (mSession.getTvInputSource() + 1) % 3;
//        ++index;
//        if (index >= 3) {
//            // 切换回广告模式
//            mSession.setTvInputSource(0);
//
//            gotoAdsPlayer();
//        } else {
        switchInputSource(index);
//        }
    }

    private void switchInputSource(int index) {
        // 保存当前输入源
        mSession.setTvInputSource(index);

        LogReportUtil.get(mContext).sendAdsLog(String.valueOf(System.currentTimeMillis()), mSession.getBoiteId(), mSession.getRoomId(),
                String.valueOf(System.currentTimeMillis()), "Signal", "system", "",
                "", mSession.getVersionName(), "", "",
                AppUtils.getInputType(mSession.getTvInputSource()));

        // 吐司提示输入源改变
        String type = AppUtils.getInputType(index);
        ShowMessage.showToastLong(mContext, type);

        // 设置输入源
        try {
            TvManager.setInputSource(mInputSource[mSession.getTvInputSource()]);
        } catch (TvCommonException e) {
            e.printStackTrace();
        }

        if (mInputSource[mSession.getTvInputSource()] == TvOsType.EnumInputSource.E_INPUT_SOURCE_ATV) {
            for (int i = 0, mChannelListSize = mChannelList.size(); i < mChannelListSize; i++) {
                AtvProgramInfo program = mChannelList.get(i);
                if (program.getChennalNum() == mSession.getTvCurrentChannelNumber()) {
                    mCurrentProgramIndex = i;
                    break;
                }
            }
            // 如果切换到电视模式的话，恢复频道为保存的值
            mTvOperate.switchATVChannel(mSession.getTvCurrentChannelNumber());
        } else {
            // 如果切换到非电视模式的话，隐藏频道提示
            if (mChannelTipRl != null && mChannelTipRl.getVisibility() == View.VISIBLE) {
                mChannelTipRl.setVisibility(View.GONE);
                mHandler.removeCallbacks(mHideChannelTipRunnable);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mActivityResumeTime = System.currentTimeMillis();

        boolean doInit = true;
        if (mIsGoneToSystemSetting) {
            mIsGoneToSystemSetting = false;
            if (GlobalValues.getInstance().PLAY_LIST != null && !GlobalValues.getInstance().PLAY_LIST.isEmpty()) {
                doInit = false;
                gotoAdsPlayer();
            }
        }

        if (doInit) {
            GlobalValues.IS_BOX_BUSY = true;
            ShowMessage.showToast(mContext, "电视节目准备中，即将开始播放");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000 * DELAY_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    boolean delayReleaseBusy = mIsAutoTurning;
                    init();
                    if (!delayReleaseBusy) {
                        GlobalValues.IS_BOX_BUSY = false;
                    }
                }
            }).start();
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    init();
//                }
//            }, 4000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHandler.removeCallbacks(mBackToAdsPlayerRunnable);
        try {
            TvManager.setInputSource(TvOsType.EnumInputSource.E_INPUT_SOURCE_STORAGE);
        } catch (TvCommonException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            TvManager.setInputSource(TvOsType.EnumInputSource.E_INPUT_SOURCE_STORAGE);
        } catch (TvCommonException e) {
            e.printStackTrace();
        }
        mHandler.removeCallbacksAndMessages(null);
        if (mChannelListDialog != null) {
            mChannelListDialog.onDestroy();
        }
        if (mChannelSearchingDialog != null && mChannelSearchingDialog.isShowing()) {
            mChannelSearchingDialog.dismiss();
        }
    }
}

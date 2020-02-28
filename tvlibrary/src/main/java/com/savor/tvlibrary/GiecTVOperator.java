package com.savor.tvlibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.tv.TvContract;
import android.media.tv.TvView;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.app.tv.TVChannelParams;
import com.droidlogic.app.tv.TvControlManager;
import com.droidlogic.app.tv.TvStoreManager;

import java.util.ArrayList;

/**
 * Created by zhang.haiqiang on 2017/9/26.
 */

public class GiecTVOperator implements ITVOperator {

    private Context mContext;
    private static final String AUTO_ATSC_C_PATH = "/sys/module/aml_fe/parameters/auto_search_std";
    private static final String AUTO_ATSC_C_MODE_ENABLE = "1";
    private static final String AUTO_ATSC_C_MODE_DISABLE = "0";
    private static final String AUTO_ATSC_C_ATV_PATH = "/sys/module/aml_fe/parameters/slow_mode";
    GiecTVOperator(Context context) {
        mContext = context;
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
//        TvControlManager.getInstance().StartTv();
    }

    @SuppressLint("NewApi")
    @Override
    public void switchATVChannel(TvView tvView, int channelId) {
//        tvView.reset();
        tvView.setStreamVolume(0);
        tvView.tune(ConstantValues.INPUT_ID_ATV, TvContract.buildChannelUri(channelId));
    }

    @SuppressLint("NewApi")
    @Override
    public void autoTuning(final AutoTurningCallback turningCallback) {
        final String TAG = "autoTurning";

        final TvStoreManager storeManager = new TvStoreManager(mContext, ConstantValues.INPUT_ID_ATV, 1) {
            @Override
            public void onScanEnd() {
                Log.d(TAG, "onScanEnd");
                TvControlManager.getInstance().DtvStopScan();
                TvControlManager.getInstance().StopTv();
            }
        };
        TvControlManager.StorDBEventListener storeListener = new TvControlManager.StorDBEventListener() {

            @Override
            public void StorDBonEvent(TvControlManager.ScannerEvent arg0) {
                storeManager.onStoreEvent(arg0);
            }

        };
        //必须设置,是存储搜索得到的数据
        TvControlManager.getInstance().setStorDBListener(storeListener);

        TvControlManager.getInstance().setScannerListener(new TvControlManager.ScannerEventListener() {
            @Override
            public void onEvent(TvControlManager.ScannerEvent event) {

                switch (event.type) {
                    case TvControlManager.EVENT_SCAN_PROGRESS:
                        Log.d(TAG, "onEvent:" + event.precent + "%\tfreq[" + event.freq + "] mode[" + event.mode + "] lock[" + event.lock + "] strength[" + event.strength + "] quality[" + event.quality + "]");

                        int isNewProgram = 0;
                        if ((event.mode == TVChannelParams.MODE_ANALOG) && (event.lock == 0x11)) { //trick here
                            isNewProgram = 1;
//                            Log.d(TAG, "Resume Scanning");
//                            if ((TvControlManager.getInstance().AtvDtvGetScanStatus() & TvControlManager.ATV_DTV_SCAN_STATUS_PAUSED)
//                                    == TvControlManager.ATV_DTV_SCAN_STATUS_PAUSED)
//                                resumeSearch();
                        } else if ((event.mode != TVChannelParams.MODE_ANALOG) && (event.programName.length() != 0)) {
//                            try {
//                                name = TVMultilingualText.getText(event.programName);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            isNewProgram = 2;
                        }

                        if (isNewProgram == 1) {
//                            channelNumber++;
                            Log.d(TAG, "New ATV Program");
                        } /*else if (isNewProgram == 2) {
                            if (event.srvType == 1)
                                channelNumber++;
                            else if (event.srvType == 2)
                                radioNumber++;
                            Log.d(TAG, "New DTV Program : [" + name + "] type[" + event.srvType + "]");
                        }*/

                        turningCallback.onProgressUpdate(event.precent);
                        break;

                    case TvControlManager.EVENT_ATV_PROG_DATA:
                        Log.d(TAG, "onEvent: EVENT_ATV_PROG_DATA");
                        break;

                    case TvControlManager.EVENT_STORE_BEGIN:
                        Log.d(TAG, "onEvent:Store begin");
                        break;

                    case TvControlManager.EVENT_STORE_END:
                        Log.d(TAG, "onEvent:Store end");
                        break;

                    case TvControlManager.EVENT_SCAN_END:
                        Log.d(TAG, "onEvent:Scan end");
                        break;

                    case TvControlManager.EVENT_SCAN_EXIT:
                        Log.d(TAG, "onEvent:Scan exit. percent=" + event.precent);
                        // 搜台结束，从系统数据表中映射出到自己的数据表中
                        TvDBHelper.getInstance(mContext).mappingChannelFromSysDb();
                        turningCallback.onComplete();
                        break;
                    default:
                        break;
                }
            }
        });
        /**20180509添加:电视搜台补全问题补充**/
        SystemControlManager scm = new SystemControlManager(mContext);
        scm.writeSysFs(AUTO_ATSC_C_PATH, AUTO_ATSC_C_MODE_DISABLE);
        scm.writeSysFs(AUTO_ATSC_C_ATV_PATH, AUTO_ATSC_C_MODE_DISABLE);
        /********************************/
        TvControlManager.getInstance().StartTv();
        TvControlManager.TvMode mode = new TvControlManager.TvMode(TvContract.Channels.TYPE_DTMB);
        int[] freqPair = new int[2];
        TvControlManager.getInstance().ATVGetMinMaxFreq(freqPair);
        /**20180509添加:电视搜台补全问题补充**/
        TvControlManager.getInstance().setAmAudioPreMute(TvControlManager.AUDIO_MUTE_FOR_TV);
        /********************************/
        TvControlManager.getInstance().DtvSetTextCoding("GB2312");
        TvControlManager.FEParas fe = new TvControlManager.FEParas();
        fe.setMode(mode);
        fe.setVideoStd(TvControlManager.ATV_VIDEO_STD_AUTO);
        fe.setAudioStd(TvControlManager.ATV_AUDIO_STD_AUTO);
        TvControlManager.ScanParas scan = new TvControlManager.ScanParas();
        scan.setMode(TvControlManager.ScanParas.MODE_DTV_ATV);
        scan.setAtvMode(TvControlManager.ScanType.SCAN_ATV_AUTO);
        scan.setDtvMode(TvControlManager.ScanType.SCAN_DTV_NONE);
//        scan.setDtvMode(TvControlManager.ScanType.SCAN_DTV_ALLBAND);

        scan.setAtvFrequency1(freqPair[0]);
        scan.setAtvFrequency2(freqPair[1]);
        scan.setDtvFrequency1(0);
        scan.setDtvFrequency2(0);
        /**20180509添加:电视搜台补全问题补充**/
        scan.setAtvModifier("mode",mode.getMode());
        /********************************/
        TvControlManager.getInstance().OpenDevForScan(DroidLogicTvUtils.OPEN_DEV_FOR_SCAN_DTV);
        /**
         * 特别说明播放电视节目就是根据搜台后保存在系统数据库的数据来播放，地址是Channels.CONTENT_URI
         * 在做搜索可以设置Channels.COLUMN_SERVICE_ID用来排序
         * 这个值好像对节目播放没有影响，其实节目编辑就是对数据进行操作， 可参考之前发的demo是如何进行数据库操作
         */
        mContext.getContentResolver().delete(TvContract.Channels.CONTENT_URI, null, null);
        TvDBHelper.getInstance(mContext).cleanChannelDb();

        TvControlManager.getInstance().TvScan(fe, scan);
    }

    @Override
    public void interruptTuning() {
//        TvControlManager.getInstance().OpenDevForScan(DroidLogicTvUtils.CLOSE_DEV_FOR_SCAN);
        TvControlManager.getInstance().DtvStopScan();
        TvControlManager.getInstance().StopTv();
    }

    @Override
    public ArrayList<AtvChannel> getAtvChannels() {
        TvDBHelper dbHelper = TvDBHelper.getInstance(mContext);
        ArrayList<AtvChannel> channels = dbHelper.getAtvChannels();
        return channels;
    }

    @Override
    public ArrayList<AtvChannel> getSysChannels() {
        TvDBHelper dbHelper = TvDBHelper.getInstance(mContext);
        ArrayList<AtvChannel> channels = dbHelper.getSysChannels();
        return channels;
    }

    @Override
    public void setAtvChannels(ArrayList<AtvChannel> channels) {
        TvDBHelper dbHelper = TvDBHelper.getInstance(mContext);
        dbHelper.updateSysDb(channels);
        dbHelper.mappingChannelFromSysDb();
    }

    @SuppressLint("NewApi")
    @Override
    public void setSignalSource(TvView tvView, TVSignal signal) {
        switch (signal) {
//            case ATV:
//                tvView.tune(ConstantValues.INPUT_ID_ATV, TvContract.buildChannelUriForPassthroughInput(ConstantValues.INPUT_ID_AV));
//                break;
            case AVI:
                tvView.tune(ConstantValues.INPUT_ID_AV, TvContract.buildChannelUriForPassthroughInput(ConstantValues.INPUT_ID_AV));
                break;
            case HDMI:
                tvView.tune(ConstantValues.INPUT_ID_HDMI, TvContract.buildChannelUriForPassthroughInput(ConstantValues.INPUT_ID_HDMI));
                break;
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void exitTv(TvView tvView) {
        TvControlManager.getInstance().StopTv();
//        TvControlManager.getInstance().release();
        tvView.reset();
    }

    @Override
    public void switchResolution(OutputResolution resolution) {
        try {
            OutputModeManager outputModeManager = new OutputModeManager(mContext);
            outputModeManager.setBestMode(resolution.strValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

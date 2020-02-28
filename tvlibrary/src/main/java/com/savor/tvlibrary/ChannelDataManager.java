package com.savor.tvlibrary;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.media.tv.TvContract.Channels;
import android.util.Log;

import com.droidlogic.app.tv.ChannelInfo;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.app.tv.TVMultilingualText;
import com.droidlogic.app.tv.TvControlManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChannelDataManager {

    private final static String TAG = ChannelDataManager.class.getSimpleName();

    //从ScannerEvent获取节目信息
    private ChannelInfo createAtvChannelInfo(TvControlManager.ScannerEvent event, int service_id) {
        String ATVName = event.programName;
        String type = Channels.TYPE_PAL;
        switch (event.videoStd) {
            case TvControlManager.ATV_VIDEO_STD_PAL:
                type = Channels.TYPE_PAL;
                break;
            case TvControlManager.ATV_VIDEO_STD_NTSC:
                type = Channels.TYPE_NTSC;
                break;
            case TvControlManager.ATV_VIDEO_STD_SECAM:
                type = Channels.TYPE_SECAM;
                break;
            default:
                type = Channels.TYPE_PAL;
                break;
        }

        TvControlManager.FEParas fep = new TvControlManager.FEParas(event.paras);

        if (ATVName.length() == 0)
            ATVName = "xxxATV Program";
        return new ChannelInfo.Builder()
                .setInputId(ConstantValues.INPUT_ID_ATV)
                .setType(type)
                .setServiceType(Channels.SERVICE_TYPE_AUDIO_VIDEO)//default is SERVICE_TYPE_AUDIO_VIDEO
                .setServiceId(service_id)
                .setDisplayNumber(Integer.toString(0))
                .setDisplayName(TVMultilingualText.getText(ATVName))
                .setLogoUrl(null)
                .setOriginalNetworkId(0)
                .setTransportStreamId(0)
                .setVideoPid(0)
                .setVideoStd(event.videoStd)
                .setVfmt(0)
                .setVideoWidth(0)
                .setVideoHeight(0)
                .setAudioPids(null)
                .setAudioFormats(null)
                .setAudioLangs(null)
                .setAudioExts(null)
                .setAudioStd(event.audioStd)
                .setIsAutoStd(event.isAutoStd)
                .setAudioTrackIndex(0)
                .setAudioCompensation(0)
                .setPcrPid(0)
                .setFrequency(event.freq)
                .setBandwidth(0)
                .setSymbolRate(0)
                .setModulation(0)
                .setFEParas(fep.toString())
                .setFineTune(0)
                .setBrowsable(true)
                .setIsFavourite(false)
                .setPassthrough(false)
                .setLocked(false)
                .setSubtitleTypes(event.stypes)
                .setSubtitlePids(event.sids)
                .setSubtitleStypes(event.sstypes)
                .setSubtitleId1s(event.sid1s)
                .setSubtitleId2s(event.sid2s)
                .setSubtitleLangs(event.slangs)
                .setDisplayNameMulti(ATVName)
                .setMajorChannelNumber(event.majorChannelNumber)
                .setMinorChannelNumber(event.minorChannelNumber)
                .setSourceId(event.sourceId)
                .setAccessControled(event.accessControlled)
                .setHidden(event.hidden)
                .setHideGuide(event.hideGuide)
                .build();
    }

    //插入数据，即就是保存节目信息
    public void insertAtvChannel(ChannelInfo channel, ContentResolver mContentResolver) {
        mContentResolver.insert(Channels.CONTENT_URI, buildAtvChannelData(channel));
        Log.d(TAG, "Insert ATV CH: [freq:" + channel.getFrequency()
                + "][name:" + channel.getDisplayName()
                + "][num:" + channel.getDisplayNumber()
                + "]");
    }

    //保存播放节目的必要信息，
    static ContentValues buildAtvChannelData(ChannelInfo channel) {
        ContentValues values = new ContentValues();
        values.put(Channels.COLUMN_INPUT_ID, channel.getInputId());
        values.put(Channels.COLUMN_DISPLAY_NUMBER, channel.getDisplayNumber());
        values.put(Channels.COLUMN_DISPLAY_NAME, channel.getDisplayName());
        values.put(Channels.COLUMN_TYPE, channel.getType());
//        values.put(Channels.COLUMN_BROWSABLE, channel.isBrowsable() ? 1 : 0);
        values.put("browsable", channel.isBrowsable() ? 1 : 0);
        values.put(Channels.COLUMN_SERVICE_TYPE, channel.getServiceType());
        values.put(Channels.COLUMN_SERVICE_ID, channel.getServiceId());//设置这个用来排序
        String output = DroidLogicTvUtils.mapToJson(buildAtvChannelMap(channel));
        values.put(Channels.COLUMN_INTERNAL_PROVIDER_DATA, output);
        return values;
    }

    private static Map<String, String> buildAtvChannelMap(ChannelInfo channel) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(ChannelInfo.KEY_VFMT, String.valueOf(channel.getVfmt()));
        map.put(ChannelInfo.KEY_FREQUENCY, String.valueOf(channel.getFrequency()));
        map.put(ChannelInfo.KEY_VIDEO_STD, String.valueOf(channel.getVideoStd()));
        map.put(ChannelInfo.KEY_AUDIO_STD, String.valueOf(channel.getAudioStd()));
        map.put(ChannelInfo.KEY_IS_AUTO_STD, String.valueOf(channel.getIsAutoStd()));
        map.put(ChannelInfo.KEY_FINE_TUNE, String.valueOf(channel.getFineTune()));
        map.put(ChannelInfo.KEY_AUDIO_COMPENSATION, String.valueOf(channel.getAudioCompensation()));
        map.put(ChannelInfo.KEY_IS_FAVOURITE, String.valueOf(channel.isFavourite() ? 1 : 0));
        map.put(ChannelInfo.KEY_MULTI_NAME, DroidLogicTvUtils.TvString.toString(channel.getDisplayNameMulti()));
        map.put(ChannelInfo.KEY_FE_PARAS, channel.getFEParas());
        map.put(ChannelInfo.KEY_MAJOR_NUM, String.valueOf(channel.getMajorChannelNumber()));
        map.put(ChannelInfo.KEY_MINOR_NUM, String.valueOf(channel.getMinorChannelNumber()));
        map.put(ChannelInfo.KEY_SOURCE_ID, String.valueOf(channel.getSourceId()));
        map.put(ChannelInfo.KEY_ACCESS_CONTROL, String.valueOf(channel.getAccessControled()));
        map.put(ChannelInfo.KEY_HIDDEN, String.valueOf(channel.getHidden()));
        map.put(ChannelInfo.KEY_HIDE_GUIDE, String.valueOf(channel.getHideGuide()));
        map.put(ChannelInfo.KEY_AUDIO_PIDS, Arrays.toString(channel.getAudioPids()));
        map.put(ChannelInfo.KEY_AUDIO_FORMATS, Arrays.toString(channel.getAudioFormats()));
        map.put(ChannelInfo.KEY_AUDIO_EXTS, Arrays.toString(channel.getAudioExts()));
        map.put(ChannelInfo.KEY_AUDIO_LANGS, DroidLogicTvUtils.TvString.toString(channel.getAudioLangs()));
        map.put(ChannelInfo.KEY_AUDIO_TRACK_INDEX, String.valueOf(channel.getAudioTrackIndex()));
        map.put(ChannelInfo.KEY_AUDIO_CHANNEL, String.valueOf(channel.getAudioChannel()));
        map.put(ChannelInfo.KEY_SUBT_TYPES, Arrays.toString(channel.getSubtitleTypes()));
        map.put(ChannelInfo.KEY_SUBT_PIDS, Arrays.toString(channel.getSubtitlePids()));
        map.put(ChannelInfo.KEY_SUBT_STYPES, Arrays.toString(channel.getSubtitleStypes()));
        map.put(ChannelInfo.KEY_SUBT_ID1S, Arrays.toString(channel.getSubtitleId1s()));
        map.put(ChannelInfo.KEY_SUBT_ID2S, Arrays.toString(channel.getSubtitleId2s()));
        map.put(ChannelInfo.KEY_SUBT_LANGS, DroidLogicTvUtils.TvString.toString(channel.getSubtitleLangs()));
        map.put(ChannelInfo.KEY_SUBT_TRACK_INDEX, String.valueOf(channel.getSubtitleTrackIndex()));
        return map;
    }
}

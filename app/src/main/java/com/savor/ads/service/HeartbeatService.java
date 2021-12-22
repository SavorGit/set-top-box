package com.savor.ads.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.savor.ads.BuildConfig;
import com.savor.ads.activity.BaseActivity;
import com.savor.ads.bean.DownloadDetailRequestBean;
import com.savor.ads.bean.MediaDownloadBean;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.MediaPlaylistBean;
import com.savor.ads.bean.NettyBalancingResult;
import com.savor.ads.bean.PlaylistDetailRequestBean;
import com.savor.ads.bean.ProgramBean;
import com.savor.ads.bean.ProgramBeanResult;
import com.savor.ads.bean.ProjectionGuideImg;
import com.savor.ads.bean.SelectContentBean;
import com.savor.ads.bean.ServerInfo;
import com.savor.ads.bean.SetBoxTopResult;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.database.DBHelper;
import com.savor.ads.log.LogUploadService;
import com.savor.ads.okhttp.coreProgress.download.FileDownloader;
import com.savor.ads.oss.OSSUtils;
import com.savor.ads.oss.OSSValues;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.FileUtils;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 */
public class HeartbeatService extends IntentService implements ApiRequestListener {
    private Context context;
    /**获取netty的IP和端口的接口异常，重试3次,间隔时间30s*/
//    private int retryCount;
    /**
     * 心跳周期，5分钟
     */
    private static final int HEARTBEAT_DURATION = 1000 * 60 * 5;
    /**
     * 小平台信息检测周期，1分钟
     */
    private static final int SERVER_INFO_CHECK_DURATION = 1000 * 60 * 1;
    /**
     * 单次循环等待时长。
     * 由于要在关键时间点上做检测，这里须>30sec <1min
     */
    private static final int ONE_CYCLE_TIME = 1000 * 30;

    /**
     * 上一个心跳过去的时长
     */
    private int mHeartbeatElapsedTime = 0;
    /**
     * 上一个小平台信息监测过去的时长
     */
    private int mServerInfoCheckElapsedTime = 0;

    private long total_data = TrafficStats.getTotalRxBytes();

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what ==1){
                int real_data = (int)msg.arg1;
                if(real_data>1024){
//                    listSpeed.add(real_data/1024+"kb/s");
                    session.setNetSpeed(real_data/1024+"kb/s");
                    Log.d("speed",real_data/1024+"kb/s");
                }else{
                    Log.d("speed",real_data+"b/s");
                    session.setNetSpeed(real_data+"b/s");
//                    listSpeed.add(real_data+"b/s");
                }
            }
        }
    };
    /**几秒刷新一次**/
    private final int count = 5;
    //心跳序列号
    private int serial_no;
    private Session session;
    private String gifBgPath;
    private DBHelper dbHelper;
    //上报热播内容相关的ID集合
    ArrayList<String> contentIds = new ArrayList<>();
    public HeartbeatService() {
        super("HeartbeatService");
        context = this;
        dbHelper = DBHelper.get(context);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // 循环检查网络情况直到可用
        do {
            LogFileUtil.write("HandleMediaDataService will check server info and network");
            if (AppUtils.isNetworkAvailable(this)) {
                break;
            }

            try {
                Thread.sleep(1000 * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
        session = Session.get(this);
        //  启动时立即心跳一次
        serial_no = 0;
        session.setNormalUseWechat(1);
        LogFileUtil.write("开机立刻上报心跳和调用是否显示小程序码接口一次");
        doHeartbeat();
        doInitConfig();
        getUploadLogFileType();
        monitorDownloadSpeed();

        if (!Session.get(this).isUseVirtualSp()) {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(mNetworkDetectionRunnable, 1, 5, TimeUnit.MINUTES);
        }

        while (true) {

            // 小平台信息监测周期到达
            if (mServerInfoCheckElapsedTime >= SERVER_INFO_CHECK_DURATION) {
                mServerInfoCheckElapsedTime = 0;
                if (session.getServerInfo() == null||session.getServerInfo().getSource() != 3){
                    httpGetIp();
                }
            }
            // 心跳周期到达，向云平台发送心跳
            if (mHeartbeatElapsedTime >= HEARTBEAT_DURATION) {
                mHeartbeatElapsedTime = 0;
                doInitConfig();
                testWechat(GlobalValues.testWechatUrl);
                doHeartbeat();
                getUploadLogFileType();
                try {
                    reportMediaDetail();
                    if (!session.isJDmomediaReport()){
                        AppApi.JDmomediaHeartbeat(context,this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String time = AppUtils.getCurTime("HH:mm");
            // 检测时间是否到达凌晨2点整
            if ("02:00".equals(time)) {
                // 去删除存本地的投屏文件
                AppUtils.clearAllCache(this);

                // 刷新播放列表
                Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
                if (activity instanceof BaseActivity) {
                    BaseActivity baseActivity = (BaseActivity) activity;
                    baseActivity.fillPlayList();
                    sendBroadcast(new Intent(ConstantValues.UPDATE_PLAYLIST_ACTION));
                }
            }

            try {
                Thread.sleep(ONE_CYCLE_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mHeartbeatElapsedTime += ONE_CYCLE_TIME;
            mServerInfoCheckElapsedTime += ONE_CYCLE_TIME;
        }
    }

    private void doHeartbeat() {
        LogFileUtil.write("开始自动上报心跳");
        serial_no +=1;
//        LogUtils.d("上报心跳====="+serial_no);
        AppApi.heartbeat(this, this,serial_no);
    }

    private void doInitConfig(){
        LogFileUtil.write("开机立刻调用一次展示小程序接口：当前小程序码状态："+Session.get(this).isShowMiniProgramIcon());

        AppApi.getScreenInitConfig(this,this);


    }

    private void getUploadLogFileType(){
        AppApi.getUploadLogFileType(this,this);
    }

    private void getNettyBalancingInfo(){
        HashMap<String,String> params = new HashMap<>();
        params.put("box_mac",session.getEthernetMac());
        params.put("req_id",System.currentTimeMillis()+"");
        AppApi.getNettyBalancingInfo(this,this,params);
    }

    private void monitorDownloadSpeed(){
        mHandler.postDelayed(mRunnable,0);

    }



    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mRunnable,count*1000);
            Message msg = mHandler.obtainMessage();
            msg.what = 1;
            msg.arg1 = getNetSpeed();
            mHandler.sendMessage(msg);
        }
    };
    private int getNetSpeed(){
        long traffic_data = TrafficStats.getTotalRxBytes() - total_data;
        total_data = TrafficStats.getTotalRxBytes();
        return (int)traffic_data /count ;
    }


    private void reportMediaDetail() {
        reportCurrent();

        Session session = Session.get(this);
        if (!TextUtils.isEmpty(session.getAdvDownloadPeriod()) && !session.getAdvDownloadPeriod().equals(session.getAdvPeriod())) {
            reportAdvDownload();
        }
        if (!TextUtils.isEmpty(session.getAdsDownloadPeriod()) && !session.getAdsDownloadPeriod().equals(session.getAdsPeriod())) {
            reportAdsDownload();
        }
        if (!TextUtils.isEmpty(session.getProDownloadPeriod()) && !session.getProDownloadPeriod().equals(session.getProPeriod())) {
            reportProDownload();
        }
    }

    private void reportProDownload() {
        reportDownloadDataByType(ConstantValues.PRO_DATA_PATH);
    }

    private void reportAdvDownload() {
        reportDownloadDataByType(ConstantValues.ADV_DATA_PATH);
    }

    private void reportAdsDownload() {
        reportDownloadDataByType(ConstantValues.ADS_DATA_PATH);
    }

    private void reportDownloadDataByType(String filePath) {
        ArrayList<MediaDownloadBean> medias = new ArrayList<>();
        DownloadDetailRequestBean requestBean = new DownloadDetailRequestBean();
        String jsonData = FileUtils.read(filePath);
        if (!TextUtils.isEmpty(jsonData)) {
            ProgramBean programBean = null;
            try {
                if (ConstantValues.ADS_DATA_PATH.equals(filePath) || ConstantValues.ADV_DATA_PATH.equals(filePath)) {
                    // 宣传片和广告
                    ProgramBeanResult programBeanResult = new Gson().fromJson(jsonData, new TypeToken<ProgramBeanResult>() {
                    }.getType());
                    if (programBeanResult.getCode() == AppApi.HTTP_RESPONSE_STATE_SUCCESS && programBeanResult.getResult() != null) {
                        programBean = programBeanResult.getResult();
                    }
                } else {
                    // 节目单
                    SetBoxTopResult setBoxTopResult = new Gson().fromJson(jsonData, new TypeToken<SetBoxTopResult>() {
                    }.getType());
                    if (setBoxTopResult.getCode() == AppApi.HTTP_RESPONSE_STATE_SUCCESS) {
                        if (setBoxTopResult.getResult() != null && setBoxTopResult.getResult().getPlaybill_list() != null) {
                            //该集合包含三部分数据，1:真实节目，2：宣传片占位符.3:广告占位符
                            for (ProgramBean item : setBoxTopResult.getResult().getPlaybill_list()) {
                                if (ConstantValues.PRO.equals(item.getVersion().getType())) {
                                    programBean = item;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (programBean != null && programBean.getVersion() != null) {
                if (programBean.getMedia_lib() != null && programBean.getMedia_lib().size() > 0) {
                    for (MediaLibBean bean : programBean.getMedia_lib()) {
                        MediaDownloadBean mediaDownloadBean = new MediaDownloadBean();
                        mediaDownloadBean.setMedia_id(bean.getVid());

                        String selection = null;
                        String[] selectionArgs = null;
                        if (ConstantValues.ADS_DATA_PATH.equals(filePath) || ConstantValues.ADV_DATA_PATH.equals(filePath)) {
                            mediaDownloadBean.setOrder(bean.getLocation_id());
                            selection = DBHelper.MediaDBInfo.FieldName.VID
                                    + "=? and "
                                    + DBHelper.MediaDBInfo.FieldName.LOCATION_ID
                                    + "=?";
                            selectionArgs = new String[]{bean.getVid(), bean.getLocation_id() + ""};
                        } else {
                            mediaDownloadBean.setOrder(bean.getOrder() + "");
                            selection = DBHelper.MediaDBInfo.FieldName.VID
                                    + "=? and "
                                    + DBHelper.MediaDBInfo.FieldName.ADS_ORDER
                                    + "=?";
                            selectionArgs = new String[]{bean.getVid(), bean.getOrder() + ""};
                        }

                        List<MediaLibBean> list = null;
                        if (ConstantValues.ADS_DATA_PATH.equals(filePath)) {
                            list = DBHelper.get(this).findNewAdsByWhere(selection, selectionArgs);
                        } else {
                            list = DBHelper.get(this).findNewPlayListByWhere(selection, selectionArgs);
                        }
                        if (list != null && list.size() >= 1) {
                            mediaDownloadBean.setState(1);
                        } else {
                            mediaDownloadBean.setState(0);
                        }
                        medias.add(mediaDownloadBean);
                    }
                }
                requestBean.setList(medias);

                requestBean.setPeriod(programBean.getVersion().getVersion());
                int type = 0;
                switch (programBean.getVersion().getType()) {
                    case ConstantValues.ADS:
                        type = 1;
                        break;
                    case ConstantValues.ADV:
                        type = 3;
                        break;
                    case ConstantValues.PRO:
                        type = 2;
                        break;
                }
                AppApi.reportDownloadList(this, this, type, requestBean);
            }
        }
    }

    private void reportCurrent() {
        ArrayList<MediaLibBean> list = new ArrayList<>();
        AppUtils.fillPlaylist(this, list, 2);
        if (!TextUtils.isEmpty(Session.get(this).getProPeriod())) {
            PlaylistDetailRequestBean playlistDetailRequestBean = new PlaylistDetailRequestBean();
            playlistDetailRequestBean.setMenu_num(Session.get(this).getProPeriod());
            ArrayList<MediaPlaylistBean> playlist = new ArrayList<>();
            for (MediaLibBean media : list) {
                if (!TextUtils.isEmpty(media.getVid())) {
                    MediaPlaylistBean bean = new MediaPlaylistBean();
                    bean.setMedia_id(media.getVid());
                    bean.setType(media.getType());
                    bean.setOrder(media.getOrder());
                    bean.setNewResource(media.getNewResource());
                    playlist.add(bean);
                }
            }
            playlistDetailRequestBean.setList(playlist);
            String selection = "";
            String[] selectionArgs = new String[]{};
            List<SelectContentBean> listLib = dbHelper.findHotPlayContentList(selection,selectionArgs);
            if (listLib!=null&&listLib.size()>0){
                for (SelectContentBean libBean:listLib){
                    contentIds.add(libBean.getId()+"");
                }
            }
            if (contentIds!=null&&contentIds.size()>0){
                playlistDetailRequestBean.setHotplay(contentIds);
            }
            AppApi.reportPlaylist(this, this, playlistDetailRequestBean);
        }
    }

    private void httpGetIp() {
        LogUtils.w("HeartbeatService 将发HTTP请求去发现小平台信息");
        LogFileUtil.write("HeartbeatService 将发HTTP请求去发现小平台信息");
        AppApi.getSpIp(this, this);
    }


    private void handleServerIp(ServerInfo serverInfo) {
        if (serverInfo != null && !TextUtils.isEmpty(serverInfo.getServerIp()) && serverInfo.getNettyPort() > 0 && serverInfo.getCommandPort() > 0 && serverInfo.getDownloadPort() > 0 &&
                (Session.get(this).getServerInfo() == null || Session.get(this).getServerInfo().getSource() != 1)) {
            LogUtils.w("HeartbeatService 将使用HTTP拿到的信息重置小平台信息");
            LogFileUtil.write("HeartbeatService 将使用HTTP拿到的信息重置小平台信息");
            serverInfo.setSource(2);
            if (serverInfo.getServerIp().contains("*")) {
                serverInfo.setServerIp(serverInfo.getServerIp().split("\\*")[0]);
            }
            Session.get(this).setServerInfo(serverInfo);
            AppApi.resetSmallPlatformInterface(this);
        }
    }

    private Runnable mNetworkDetectionRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtils.d("NetworkDetectionRunnable is run.");

            double internetLatency = getLatency("www.baidu.com");
            double intranetLatency = -1;
            if (Session.get(HeartbeatService.this).getServerInfo() != null) {
                intranetLatency = getLatency(Session.get(HeartbeatService.this).getServerInfo().getServerIp());
            }

            AppApi.postNetstat(HeartbeatService.this, HeartbeatService.this,
                    intranetLatency == -1 ? "" : "" + intranetLatency, internetLatency == -1 ? "" : "" + internetLatency);
        }

        private double getLatency(String address) {
            LogUtils.d("address is " + address);
            double latency = -1;

            Process process = null;
            InputStream is = null;
            BufferedReader reader = null;
            try {
                process = Runtime.getRuntime().exec("ping -c 10 -i 0.2 -s 56 " + address);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String str = null;
                while ((str = reader.readLine()) != null) {
                    if (str.contains("rtt ")) {
                        LogUtils.d(str);
                        String speedStr = str.split(" = ")[1].split(",")[0];
                        String unitStr = speedStr.split(" ")[1];
                        double min = Double.parseDouble(speedStr.split(" ")[0].split("/")[0]);
                        double avg = Double.parseDouble(speedStr.split(" ")[0].split("/")[1]);
                        double max = Double.parseDouble(speedStr.split(" ")[0].split("/")[2]);
                        double mdev = Double.parseDouble(speedStr.split(" ")[0].split("/")[3]);

                        if ("ms".equals(unitStr)) {
                            latency = avg;
                        } else if ("s".equals(unitStr)) {
                            latency = avg * 1000;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (process != null) {
                    try {
                        Thread.sleep(1000*10);
                        process.exitValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                        process.destroy();
                    }
                }
            }

            return latency;
        }
    };

    @Override
    public void onSuccess(AppApi.Action method, Object obj) {
        switch (method) {
            case CP_GET_HEARTBEAT_PLAIN:
                LogFileUtil.write("自动上报心跳成功。 " + obj);
                try {
                    JSONObject jsonObject = new JSONObject(obj.toString());
                    if (jsonObject.has("is_4g")&&1==jsonObject.getInt("is_4g")){
                        session.setWhether4gNetwork(true);
                    }else{
                        session.setWhether4gNetwork(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case SP_POST_NETSTAT_JSON:
                LogUtils.d("postNetstat success");
                break;
            case CP_GET_SP_IP_JSON:
                LogUtils.w("HeartbeatService HTTP接口发现小平台信息");
                LogFileUtil.write("HeartbeatService HTTP接口发现小平台信息");
                if (obj instanceof ServerInfo) {
                    handleServerIp((ServerInfo) obj);
                }
                break;
            case CP_POST_DOWNLOAD_LIST_JSON:
                LogUtils.d("上报下载列表成功");
                break;
            case CP_POST_PLAY_LIST_JSON:
                LogUtils.d("上报播放列表成功");
                break;
            case CP_POST_FORSCREEN_GETCONFIG_JSON:
                if (obj instanceof String){
                    try {
                        String info = (String)obj;
                        JSONObject jsonObject = new JSONObject(info);
                        //0:关闭,1:打开
                        int is_open_netty = 0;
                        if (jsonObject.has("is_open_netty")){
                            is_open_netty = jsonObject.getInt("is_open_netty");
                        }
                        //是否开启小程序码0:关闭，1:开启
                        int is_sapp_forscreen =jsonObject.getInt("is_sapp_forscreen");
                        //是否开启极简版小程序码|0:关闭，1:开启
                        int is_simple_sapp_forscreen =jsonObject.getInt("is_simple_sapp_forscreen");
                        //是否支持添加投屏互动广告
                        int is_open_interactscreenad = jsonObject.getInt("is_open_interactscreenad");
                        //播放投屏互动广告的间隔次数
                        int system_sapp_forscreen_nums = jsonObject.getInt("system_sapp_forscreen_nums");
                        //小程序码类型:1.展示小程序码，2,展示二维码
                        int qrcode_type = jsonObject.getInt("qrcode_type");
                        //是否开启版位签到码
                        int is_open_signin = jsonObject.getInt("is_open_signin");
                        //活动广告商品播放类型 1替换 2队列
                        int activity_adv_playtype = jsonObject.getInt("activity_adv_playtype");
                        //极简版投屏上传文件大小分界
                        int simple_upload_size = jsonObject.getInt("simple_upload_size");
                        //投文件每隔几次插播一条图片广告
                        int scenceadv_show_num = jsonObject.getInt("scenceadv_show_num");
                        if (is_open_netty==1 && is_sapp_forscreen==1){
                            LogFileUtil.write("开始立刻调用小程序码接口返回成功，启动小程序NETTY服务");
                            Log.d("HeartbeatService","showMiniProgramIcon(true)");
                            LogUtils.i("miniProgram--setShowMiniProgramIcon(true)");
                            session.setShowMiniProgramIcon(true);
                        }else{
                            LogUtils.i("miniProgram--setShowMiniProgramIcon(false)");
                            session.setShowMiniProgramIcon(false);
                        }
                        if (is_simple_sapp_forscreen==1){
                            session.setShowSimpleMiniProgramIcon(true);
                        }else{
                            session.setShowSimpleMiniProgramIcon(false);
                        }
                        if (jsonObject.has("isShowAnimQRcode")){
                            session.setShowAnimQRcode(jsonObject.getBoolean("isShowAnimQRcode"));
                        }
                        if (is_open_netty==1&&!session.isHeartbeatMiniNetty()){
                            Log.d("HeartbeatService","开始请求netty的ip地址和端口号");
//                            retryCount =0;
                            getNettyBalancingInfo();
                        }
                        if (is_open_interactscreenad==1){
                            session.setOpenInteractscreenad(true);
                            session.setSystemSappForscreenNums(system_sapp_forscreen_nums);
                        }else{
                            session.setOpenInteractscreenad(false);
                            session.setSystemSappForscreenNums(0);
                        }
                        session.setQrcodeType(qrcode_type);
                        session.setActivityPlayType(activity_adv_playtype);
                        session.setSimple_upload_size(simple_upload_size);
                        session.setScenceadv_show_num(scenceadv_show_num);
                        downloadQrcodeGifBg(jsonObject);
                        String guide = jsonObject.getString("forscreen_help_images");
                        handleProjectionGuideImg(guide);
                        if (jsonObject.has("forscreen_call_code")){
                            JSONObject json = jsonObject.getJSONObject("forscreen_call_code");
                            downloadCallQrcodeVideo(json);
                        }
                        if (jsonObject.has("is_wifi_hotel")&&jsonObject.getInt("is_wifi_hotel")==1){
                            session.setWifiHotel(true);
                        }else{
                            session.setWifiHotel(false);
                        }
                        if (jsonObject.has("test_wechat")){
                            String test_wechat = jsonObject.getString("test_wechat");
                            GlobalValues.testWechatUrl = test_wechat;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                break;
            case CP_GET_NETTY_BALANCING_FORM:
                if (obj instanceof NettyBalancingResult){
                    Log.d("HeartbeatService","返回netty的端口号和ip，准备启动netty");
                    try {
                        boolean start = false;
                        NettyBalancingResult netty = (NettyBalancingResult)obj;
                        if (!TextUtils.isEmpty(netty.getResult())){
                            String[] split = netty.getResult().split(":");
                            if (split!=null&&split.length==2){
                                String url = netty.getResult().split(":")[0];
                                String port = netty.getResult().split(":")[1];
                                if (TextUtils.isEmpty(session.getNettyUrl())){
                                    start=true;
                                }else if (!url.equals(session.getNettyUrl())){
                                    start = true;
                                }else if (!session.isHeartbeatMiniNetty()){
                                    start = true;
                                }
                                if (!TextUtils.isEmpty(url)){
                                    session.setNettyUrl(url);
                                }
                                if (!TextUtils.isEmpty(port)){
                                    session.setNettyPort(Integer.valueOf(port));
                                }
                            }
//                            retryCount =0;
                            if (start){
                                startMiniProgramNettyService();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            case CP_GET_UPLOAD_LOG_FILE_JSON:
                if (obj instanceof Integer){
                   int type = (Integer)obj;
                   handleUploadFileType(type);
                }
                break;
            case CP_GET_TEST_WECHAT_JSON:
                LogUtils.d("test_wechat_success");
                session.setNormalUseWechat(1);
                break;
        }
    }
    /**处理展示二维码的背景动态图*/
    private void downloadQrcodeGifBg(JSONObject jsonObject){
        try {
            if (jsonObject.has("qrcode_gif")){
                String qrcode_gif_filename=jsonObject.getString("qrcode_gif_filename");
                String qrcode_gif_url=jsonObject.getString("qrcode_gif");
                String qrcode_gif_md5 = jsonObject.getString("qrcode_gif_md5");
                String basePath = AppUtils.getSDCardPath()+AppUtils.PICTURES;
                gifBgPath = basePath+qrcode_gif_filename;
                boolean isExit = AppUtils.isDownloadCompleted(gifBgPath,qrcode_gif_md5.toUpperCase());
                if (isExit){
                    session.setQrcodeGifBgPath(gifBgPath);
                }else{
                    new Thread(()->{
                        boolean isDownloaded = new FileDownloader(context,qrcode_gif_url,basePath, qrcode_gif_filename,false).downloadByRange();
                            if (isDownloaded){
                            session.setQrcodeGifBgPath(gifBgPath);
                        }
                    }).start();
                }
                AppUtils.deleteOverdueGifPictures(qrcode_gif_filename);
            }
            //二维码展示时长
            int qrcode_showtime = jsonObject.getInt("qrcode_showtime");
            session.setQrcode_showtime(qrcode_showtime);
            //二维码间隔时长
            int qrcode_takttime = jsonObject.getInt("qrcode_takttime");
            session.setQrcode_takttime(qrcode_takttime);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**处理投屏过程中的引导图*/
    private void handleProjectionGuideImg(String guide){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        ProjectionGuideImg guideImg = gson.fromJson(guide, new TypeToken<ProjectionGuideImg>() {}.getType());
        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.cache);
        /******************************************/
        String image_url = guideImg.getImage_url();
        String imgName = guideImg.getImage_filename();
        if (!new File(basePath+imgName).exists()&&!TextUtils.isEmpty(image_url)){
            new Thread(() -> new FileDownloader(context,image_url,basePath, imgName).downloadByRange()).start();
        }
        String video_url = guideImg.getVideo_url();
        String videoName = guideImg.getVideo_filename();
        if (!new File(basePath+videoName).exists()&&!TextUtils.isEmpty(video_url)){
            new Thread(() -> new FileDownloader(context,video_url,basePath, videoName).downloadByRange()).start();
        }
        String file_url = guideImg.getFile_url();
        String fileName = guideImg.getFile_filename();
        if (!new File(basePath+fileName).exists()&&!TextUtils.isEmpty(file_url)){
            new Thread(() -> new FileDownloader(context,file_url,basePath, fileName).downloadByRange()).start();
        }
        String forscreen_url = guideImg.getForscreen_box_url();
        String forscreenName = guideImg.getForscreen_box_filename();
        if (!new File(basePath+forscreenName).exists()&&!TextUtils.isEmpty(forscreen_url)){
            new Thread(() -> new FileDownloader(context,forscreen_url,basePath, forscreenName).downloadByRange()).start();
        }
        String bonusUrl = guideImg.getBonus_forscreen_url();
        String bonusFilename = guideImg.getBonus_forscreen_filename();
        if (!new File(basePath+bonusFilename).exists()&&!TextUtils.isEmpty(bonusUrl)){
            new Thread(() -> new FileDownloader(context,bonusUrl,basePath, bonusFilename).downloadByRange()).start();
        }
        session.setGuideImg(guideImg);
    }

    private void downloadCallQrcodeVideo(JSONObject jsonObject){
        try{
            String basePath = AppUtils.getSDCardPath()+AppUtils.Download;
            String filename = jsonObject.getString("filename");
            String md5 = jsonObject.getString("md5");
            String url = jsonObject.getString("url");
            String filePath = basePath+filename;
            boolean isExit = AppUtils.isDownloadEasyCompleted(filePath,md5.toLowerCase());
            if (!isExit){
                new Thread(() -> new FileDownloader(context,url,basePath, filename,false).downloadByRange()).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void testWechat(String wechatUrl){
        if (TextUtils.isEmpty(wechatUrl)){
            return;
        }
        AppApi.getTestWechat(context,this,wechatUrl);
    }

    public void startMiniProgramNettyService(){
        LogFileUtil.write("测试netty启动 startMiniProgramNettyService");
        Intent intent = new Intent(context, MiniProgramNettyService.class);
        startService(intent);
    }

    private void handleUploadFileType(final int type){
        if (type==0){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String basePath = AppUtils.getMainMediaPath();

                String fileName = null;
                if (type==1){
                    fileName = LogFileUtil.LOG_FILE_NAME;
                }else if (type==2){
                    fileName = LogFileUtil.LOG_FILE_DOWNLOAD_NAME;
                }else if (type==3){
                    fileName = LogFileUtil.EXCEPTION_FILE_NAME;
                }else if (type==4){
                    fileName = LogFileUtil.KEY_LOG_FILE_NAME;
                }else if (type==5){
                    fileName = LogFileUtil.BOOT_DIR_NAME;
                }
                String sourceFilePath = basePath+File.separator+fileName;
                File sourceFile = new File(sourceFilePath);
                if (TextUtils.isEmpty(fileName)||!sourceFile.exists()){
                    return;
                }
                final String zipPath = sourceFile.getPath() + ".zip";
                final File zipFile = new File(zipPath);
                try {
                    AppUtils.zipFile(sourceFile,zipFile,zipFile.getName());
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (!zipFile.exists()){
                    return;
                }
                String ossFilePath = OSSValues.uploadDebugPath
                        + session.getOssAreaId()
                        + File.separator
                        + AppUtils.getCurTime("yyyyMMdd")
                        + File.separator
                        + session.getEthernetMac()
                        + File.separator
                        + fileName + ".zip";

                new OSSUtils(context,
                        BuildConfig.OSS_BUCKET_NAME,
                        ossFilePath,
                        zipPath,
                        new LogUploadService.UploadCallback() {
                            @Override
                            public void isSuccessOSSUpload(boolean flag) {

                                if (zipFile.exists()) {
                                    zipFile.delete();
                                }
                            }
                        }).asyncUploadFile();
            }
        }).start();
    }

    @Override
    public void onError(AppApi.Action method, Object obj) {
        switch (method) {
            case CP_GET_HEARTBEAT_PLAIN:
                LogFileUtil.write("自动上报心跳失败。 " + obj);
                break;
            case SP_POST_NETSTAT_JSON:
                LogUtils.d("postNetstat failed");
                break;
            case CP_GET_SP_IP_JSON:
                LogUtils.w("HeartbeatService HTTP接口发现小平台信息失败");
                LogFileUtil.write("HeartbeatService HTTP接口发现小平台信息失败");
                break;
            case CP_POST_DOWNLOAD_LIST_JSON:
                LogUtils.d("上报下载列表失败");
                break;
            case CP_POST_PLAY_LIST_JSON:
                LogUtils.d("上报播放列表失败");
                break;
            case CP_POST_FORSCREEN_GETCONFIG_JSON:
                LogUtils.d("HeartbeatService doInitConfig初始化接口异常，重新请求");

                break;
            case CP_GET_NETTY_BALANCING_FORM:
                mHandler.postDelayed(this::getNettyBalancingInfo,1000*30);
                LogUtils.d("HeartbeatService getNettyBalancingInfo获取netty地址接口异常，重新请求");
                break;
        }
    }

    @Override
    public void onNetworkFailed(AppApi.Action method) {
        switch (method) {
            case CP_GET_HEARTBEAT_PLAIN:
                LogFileUtil.write("自动上报心跳失败，网络异常");
                break;
            case SP_POST_NETSTAT_JSON:
                LogUtils.d("postNetstat failed");
                break;
            case CP_GET_SP_IP_JSON:
                LogUtils.w("HeartbeatService HTTP接口发现小平台信息失败");
                LogFileUtil.write("HeartbeatService HTTP接口发现小平台信息失败");
                break;
            case CP_POST_DOWNLOAD_LIST_JSON:
                LogUtils.d("上报下载列表失败，网络异常");
                break;
            case CP_POST_PLAY_LIST_JSON:
                LogUtils.d("上报播放列表失败，网络异常");
                break;
            case CP_POST_FORSCREEN_GETCONFIG_JSON:
                LogUtils.d("HeartbeatService doInitConfig初始化接口异常，重新请求");
                break;
            case CP_GET_NETTY_BALANCING_FORM:
                mHandler.postDelayed(() -> getNettyBalancingInfo(), 1000 * 30);
                LogUtils.d("HeartbeatService getNettyBalancingInfo获取netty地址网络异常，重新请求");
                break;
            case CP_GET_TEST_WECHAT_JSON:
                LogUtils.d("test_wechat_onNetworkFailed");
                session.setNormalUseWechat(0);
                break;
        }
    }
}

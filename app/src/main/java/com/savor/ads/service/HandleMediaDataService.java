package com.savor.ads.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.savor.ads.BuildConfig;
import com.savor.ads.activity.TvPlayerActivity;
import com.savor.ads.activity.TvPlayerGiecActivity;
import com.savor.ads.bean.ActivityGoodsBean;
import com.savor.ads.bean.ActivityGoodsResult;
import com.savor.ads.bean.BirthdayOndemandBean;
import com.savor.ads.bean.BirthdayOndemandResult;
import com.savor.ads.bean.BoxInitBean;
import com.savor.ads.bean.BoxInitResult;
import com.savor.ads.bean.InteractionAdsResult;
import com.savor.ads.bean.JsonBean;
import com.savor.ads.bean.MediaItemBean;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.ProgramBean;
import com.savor.ads.bean.ProgramBeanResult;
import com.savor.ads.bean.SelectContentBean;
import com.savor.ads.bean.SelectContentResult;
import com.savor.ads.bean.ServerInfo;
import com.savor.ads.bean.SetBoxTopResult;
import com.savor.ads.bean.SetTopBoxBean;
import com.savor.ads.bean.ShopGoodsBean;
import com.savor.ads.bean.ShopGoodsResult;
import com.savor.ads.bean.Television;
import com.savor.ads.bean.TvProgramGiecResponse;
import com.savor.ads.bean.TvProgramResponse;
import com.savor.ads.bean.UpgradeInfo;
import com.savor.ads.bean.VersionInfo;
import com.savor.ads.bean.WelcomeResourceBean;
import com.savor.ads.bean.WelcomeResourceResult;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.database.DBHelper;
import com.savor.ads.log.LogReportUtil;
import com.savor.ads.okhttp.coreProgress.download.ProgressDownloader;
import com.savor.ads.oss.OSSUtils;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.FileUtils;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShellUtils;
import com.savor.ads.utils.ShowMessage;
import com.savor.ads.utils.UpdateUtil;
import com.savor.ads.utils.tv.TvOperate;
import com.savor.tvlibrary.AtvChannel;
import com.savor.tvlibrary.ITVOperator;
import com.savor.tvlibrary.TVOperatorFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.savor.ads.database.DBHelper.MediaDBInfo.TableName.BIRTHDAY_ONDEMAND;

/**
 * 处理下载媒体文件逻辑的服务（下载内容分为两大块，第一是节目单，第二是点播）
 * 节目单分为三大块：既分三个接口返回数据
 * 1：节目接口，返回整个节目单，包含节目视频内容和宣传片以及广告的占位符
 * 2：宣传片接口，返回所有的宣传片，只有当节目和宣传片全部下载完成以后，才可以播放本期视频
 * 3: 广告接口，返回节目单中广告位上的广告
 * Created by bichao on 2016/12/10.
 */

public class HandleMediaDataService extends Service implements ApiRequestListener {

    private Context context;
    private Session session;
    private String logo_md5 = null;
    private String loading_img_md5 = null;
    /**
     * 平台返回的节目单数据
     */
    private SetTopBoxBean setTopBoxBean;
    /**
     * 广告集合
     */
    private ProgramBean adsProgramBean;
    /**
     * 宣传片集合
     */
    private ProgramBean advProgramBean;
    /**
     * poly广告集合
     */
    private ProgramBean polyAdsProgramBean;

    /**
     * 接口返回的盒子信息
     */
    private BoxInitBean boxInitBean;

    private DBHelper dbHelper;
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    UpdateUtil updateUtil = null;
    /**
     * 每次第一次开机运行的时候都检测一遍本地文件文件，如果损毁就重新下载
     */
    private boolean isFirstRun = true;
    /**
     * 聚屏广告首次下载
     * */
    private boolean isPolyFirstRun = true;
    /**
     * 广告下载
     */
    private boolean isAdsFirstRun = true;
    /**
     * 1.启动的时候写电视机播放音量和切换时间日志
     * 2.当音量和时间发生改变的时候写日志
     */
    private boolean isProduceLog = false;

    private boolean isProCompleted = false;  //节目是否下载完毕
    private String mProCompletedPeriod;

    private int poly_timeout_count = 0;
    private int pro_timeout_count = 0;
    private int ads_timeout_count = 0;
    private int adv_timeout_count = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        this.session = Session.get(this);
        dbHelper = DBHelper.get(context);
        updateUtil = new UpdateUtil(context);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d("==========into onStartCommand method=========");
        LogFileUtil.write("HandleMediaDataService onStartCommand");
        new Thread(new Runnable() {
            @Override
            public void run() {

                // 等30秒再开始下载
                try {
                    Thread.sleep(1000 * 30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                while (true) {

                    try {
                        // 循环检查网络、小平台信息的情况直到可用
                        do {
                            LogFileUtil.write("HandleMediaDataService will check server info and network");
                            if (AppUtils.isNetworkAvailable(context) &&
                                    session.getServerInfo() != null &&
                                    !TextUtils.isEmpty(AppUtils.getMainMediaPath())) {
                                break;
                            }

                            try {
                                Thread.sleep(1000 * 2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } while (true);

                        LogFileUtil.write("HandleMediaDataService will start UpdateUtil");
                        /**异步更新apk、rom,进入下载逻辑首先执行升级方法**/
                        UpgradeInfo upgradeInfo = updateUtil.getUpgradeInfoFromServer();
                        try {
                            if (upgradeInfo!=null&&!TextUtils.isEmpty(upgradeInfo.getNewestApkVersion())){
                                int localVersionCode = session.getVersionCode();
                                long remoteVersionCode = Long.valueOf(upgradeInfo.getNewestApkVersion());
                                if (localVersionCode<remoteVersionCode){
                                    Handler handler=new Handler(Looper.getMainLooper());
                                    handler.post(()->ShowMessage.showToast(context,"发现新版本，开始下载"));

                                    String basePath = AppUtils.getMainMediaPath()+File.separator;
                                    boolean isDownloaded;
                                    if (upgradeInfo.isVirtual()){
                                        String url = BuildConfig.OSS_ENDPOINT+upgradeInfo.getOss_path();
                                        isDownloaded = updateUtil.dowloadApkFile(url,basePath,ConstantValues.APK_DOWNLOAD_FILENAME);
                                    }else {
                                        String baseUrl = session.getServerInfo().getDownloadUrl();
                                        String url = baseUrl+upgradeInfo.getApkUrl();
                                        isDownloaded = updateUtil.dowloadApkFile(url,basePath,ConstantValues.APK_DOWNLOAD_FILENAME);
                                    }
                                    if (isDownloaded){
                                        File file = new File(basePath,ConstantValues.APK_DOWNLOAD_FILENAME);
                                        updateUtil.handleUpdateResult(file,upgradeInfo.getApkMd5());
                                    }
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        getBoxSupportPolyAdsTpmedias();
                        AppUtils.updateProjectionLog(context);
                        LogFileUtil.write("HandleMediaDataService will check space available");
                        // 检测剩余存储空间
                        if (AppUtils.getAvailableExtSize() < ConstantValues.EXTSD_LEAST_AVAILABLE_SPACE/2) {
                            // 存储空间不足
                            LogFileUtil.writeException(new Throwable("Low spaces in media partition"));

                            // 清理可清理的视频等文件
                            cleanMediaWhenSpaceLow();
                            // 提前播放的pro在上面这一步可能已经被删除，这里重新填充节目单并通知播放
                            notifyToPlay();
                            // 上报服务器 卡满异常
                            AppApi.reportSDCardState(context, HandleMediaDataService.this, 2);

                        } else {
                            // 空间充足，开始更新资源

//                            getPrizeInfo();

                            LogFileUtil.write("HandleMediaDataService will start getBoxInfo");
                            // 同步获取机顶盒基本信息，包括logo、loading图
                            getBoxInfo();

                            // 检测预约发布的播放时间是否已到达，启动时不检测因为已经在Application中检测过了
                            if (!isFirstRun && AppUtils.checkPlayTime(context)) {
                                notifyToPlay();
                            }

                            LogFileUtil.write("HandleMediaDataService will start getBirthdayOndemandFromCloudPlatform");
                            //同步获取生日歌相关视频数据
                            getBirthdayOndemandFromCloudPlatform();
                            LogFileUtil.write("HandleMediaDataService will start getInteractionAdsFromCloudPlatform");
                            //同步获取互动广告媒体数据
                            getInteractionAdsFromCloudPlatform();
                            //同步获取活动商品数据（主干版本是优选，销售端是活动商品）
                            getGoodsProgramListFromCloudPlatform();
                            //同步获取商城商品数据
                            getShopGoodsListFromCloudPlatform();
                            //同步获取用户精选内容上大屏数据
                            getSelectContentFromCloudPlatform();
                            //同步获取用户发现内容数据
                            getDiscoverContentFromCloudPlatform();
                            //同步获取欢迎词资源数据(含封面和mp3音乐)
                            getWelcomeResourceFromCloudPlatform();
                            // 同步获取轮播节目媒体数据
                            getProgramDataFromSmallPlatform();
                            LogFileUtil.write("HandleMediaDataService will start getAdvDataFromSmallPlatform");
                            //同步获取宣传片媒体数据
                            getAdvDataFromSmallPlatform();
                            LogFileUtil.write("HandleMediaDataService will start getPolyAdsFromSmallPlatform");
                            // 同步获取聚屏物料媒体数据
                            getPolyAdsFromSmallPlatform();
                            LogFileUtil.write("HandleMediaDataService will start getAdsDataFromSmallPlatform");
                            //同步获取广告片媒体数据
                            getAdsDataFromSmallPlatform();

                            LogFileUtil.write("HandleMediaDataService will start getTVMatchDataFromSmallPlatform");
                            // 异步获取电视节目信息
                            getTVMatchDataFromSmallPlatform();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogFileUtil.writeException(e);
                    }

                    // 睡眠10分钟
                    try {
                        Thread.sleep(1000 * 60 * 10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }




    private void cleanMediaWhenSpaceLow() {
        // 删除下载表中的当前、非下载中的节目单的内容
        String selection = DBHelper.MediaDBInfo.FieldName.PERIOD + "!=? AND " + DBHelper.MediaDBInfo.FieldName.PERIOD + "!=? AND " +
                DBHelper.MediaDBInfo.FieldName.PERIOD + "!=? AND " + DBHelper.MediaDBInfo.FieldName.PERIOD + "!=? ";
        String[] selectionArgs;
        selectionArgs = new String[]{session.getProPeriod(), session.getProDownloadPeriod(), session.getAdvPeriod(), session.getAdvDownloadPeriod()};
        dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWPLAYLIST, selection, selectionArgs);

        AppUtils.deleteOldMedia(this,true);
    }

    /**
     * 获取当前版位支持的poly广告平台
     */
    private void getBoxSupportPolyAdsTpmedias(){
        AppApi.getBoxSupportPolyAdsTpmedias(context,this);
    }

    private void getBoxInfo() {
        try {
            JsonBean jsonBean = AppApi.getBoxInitInfo(this, this, session.getEthernetMac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code") != AppApi.HTTP_RESPONSE_STATE_SUCCESS) {
                LogUtils.d("接口返回的状态不对,code=" + jsonObject.getInt("code"));
                return;
            }

            Object result = gson.fromJson(jsonBean.getConfigJson(), new TypeToken<BoxInitResult>() {
            }.getType());
            if (result instanceof BoxInitResult) {
                BoxInitBean boxInitBean = ((BoxInitResult) result).getResult();
                /*******************设置盒子基本信息开始************************/
                initBoxInfo(boxInitBean,jsonBean.getSmallType());
                /*******************设置盒子基本信息结束************************/
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {

        }
    }
    /**
     * 初始化盒子基本信息
     *
     * @param boiteBean
     */
    void initBoxInfo(BoxInitBean boiteBean,String smallType) {
        if (boiteBean == null) {
            return;
        }
        boxInitBean = boiteBean;

        // 应后端统计要求，只要某个音量改变，就产生4条音量的记录
        if (!isProduceLog || boiteBean.getAds_volume() != session.getVolume() ||
                boiteBean.getProject_volume() != session.getProjectVolume() ||
                boiteBean.getDemand_volume() != session.getVodVolume() ||
                boiteBean.getTv_volume() != session.getTvVolume()) {
            String volumeUUID = String.valueOf(System.currentTimeMillis());
            //生产电视播放音量日志
            LogReportUtil.get(context).sendAdsLog(volumeUUID,
                    session.getBoiteId(),
                    session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()),
                    "player_volume",
                    "system",
                    "",
                    "",
                    session.getVersionName(),
                    session.getAdsPeriod(),
                    session.getBirthdayOndemandPeriod(),
                    String.valueOf(boiteBean.getAds_volume()));
            LogReportUtil.get(context).sendAdsLog(volumeUUID,
                    session.getBoiteId(),
                    session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()),
                    "project_volume",
                    "system",
                    "",
                    "",
                    session.getVersionName(),
                    session.getAdsPeriod(),
                    session.getBirthdayOndemandPeriod(),
                    String.valueOf(boiteBean.getProject_volume()));
            LogReportUtil.get(context).sendAdsLog(volumeUUID,
                    session.getBoiteId(),
                    session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()),
                    "vod_volume",
                    "system",
                    "",
                    "",
                    session.getVersionName(),
                    session.getAdsPeriod(),
                    session.getBirthdayOndemandPeriod(),
                    String.valueOf(boiteBean.getDemand_volume()));
            LogReportUtil.get(context).sendAdsLog(volumeUUID,
                    session.getBoiteId(),
                    session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()),
                    "tv_volume",
                    "system",
                    "",
                    "",
                    session.getVersionName(),
                    session.getAdsPeriod(),
                    session.getBirthdayOndemandPeriod(),
                    String.valueOf(boiteBean.getTv_volume()));

            if (boiteBean.getAds_volume() > 0) {
                session.setVolume(boiteBean.getAds_volume());
            }
            if (boiteBean.getProject_volume() > 0) {
                session.setProjectVolume(boiteBean.getProject_volume());
            }
            if (boiteBean.getDemand_volume() > 0) {
                session.setVodVolume(boiteBean.getDemand_volume());
            }
            if (boiteBean.getTv_volume() > 0) {
                session.setTvVolume(boiteBean.getTv_volume());
            }
        }

        if (!isProduceLog || boiteBean.getSwitch_time() != session.getSwitchTime()) {
            //生产电视切换时间日志
            LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()),
                    session.getBoiteId(),
                    session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()),
                    "swich_time",
                    "system",
                    "",
                    "",
                    session.getVersionName(),
                    session.getAdsPeriod(),
                    session.getBirthdayOndemandPeriod(),
                    String.valueOf(boiteBean.getSwitch_time()));

            if (boiteBean.getSwitch_time() > 0) {
                session.setSwitchTime(boiteBean.getSwitch_time());
            }
        }

        isProduceLog = true;
//        if (!TextUtils.isEmpty(session.getBoiteId()) &&
//                !session.getBoiteId().equals(boiteBean.getHotel_id())){
//            deleteDataByChangeBoite();
//        }
        session.setBoxId(boiteBean.getBox_id());
        session.setBoiteId(boiteBean.getHotel_id());
        session.setBoiteName(boiteBean.getHotel_name());

        session.setRoomId(boiteBean.getRoom_id());
        session.setRoomName(boiteBean.getRoom_name());
        session.setRoomType(boiteBean.getRoom_type());
        session.setBoxName(boiteBean.getBox_name());
//        /**桶名称*/
//        if (!TextUtils.isEmpty(boiteBean.getOssBucketName())) {
//            session.setOss_bucket(boiteBean.getOssBucketName());
//        }
        /**桶地址*/
        if (!TextUtils.isEmpty(boiteBean.getArea_id())) {
            session.setOssAreaId(boiteBean.getArea_id());
        }

        // 组合小平台下发的各种版本信息
        ArrayList<VersionInfo> spVersionInfo = new ArrayList<>();
        if (boiteBean.getPlaybill_version_list() != null && !boiteBean.getPlaybill_version_list().isEmpty()) {
            spVersionInfo.addAll(boiteBean.getPlaybill_version_list());
        }
        if (boiteBean.getDemand_version_list() != null && !boiteBean.getDemand_version_list().isEmpty()) {
            spVersionInfo.addAll(boiteBean.getDemand_version_list());
        }
//        if (boiteBean.getLogo_version_list() != null && !boiteBean.getLogo_version_list().isEmpty()) {
//            spVersionInfo.addAll(boiteBean.getLogo_version_list());
//        }
//        if (boiteBean.getLoading_version_list() != null && !boiteBean.getLoading_version_list().isEmpty()) {
//            spVersionInfo.addAll(boiteBean.getLoading_version_list());
//        }
        if (boiteBean.getApk_version_list() != null && !boiteBean.getApk_version_list().isEmpty()) {
            spVersionInfo.addAll(boiteBean.getApk_version_list());
        }
        if (boiteBean.getSmall_web_version_list() != null && !boiteBean.getSmall_web_version_list().isEmpty()) {
            spVersionInfo.addAll(boiteBean.getSmall_web_version_list());
        }
        session.setSPVersionInfo(spVersionInfo);
        try{
            if (boiteBean.getTv_list()!=null&&boiteBean.getTv_list().size()>0){
                Television tv = boiteBean.getTv_list().get(0);
                session.setTvSize(Integer.valueOf(tv.getTv_size()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        /**下载启动图*/
        if (!TextUtils.isEmpty(boiteBean.getLogo_url()) && !TextUtils.isEmpty(boiteBean.getLogo_md5()) &&
                boxInitBean.getLogo_version_list() != null && !boxInitBean.getLogo_version_list().isEmpty()) {
            String logoBasePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/";
            String logoFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+session.getSplashPath();
            File logoFile = new File(logoFilePath);
            String md5 = null;
            try {
                if (logoFile.exists()) {
                    md5 = AppUtils.getMD5(org.apache.commons.io.FileUtils.readFileToByteArray(logoFile));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!boiteBean.getLogo_md5().equals(md5)) {
                logoFile.delete();
                String[] split = boiteBean.getLogo_url().split("/");
                String logo_name = split[split.length - 1];
                logo_md5 = boiteBean.getLogo_md5();

                String path = logoBasePath + logo_name;
                if (ConstantValues.VIRTUAL.equals(smallType)){
                    OSSUtils ossUtils = new OSSUtils(context,
                            BuildConfig.OSS_BUCKET_NAME,
                            boiteBean.getLogo_url(),
                            new File(path),false);
                    if (ossUtils.syncDownload()){
                        session.setSplashPath(path);
                        if (boxInitBean.getLogo_version_list() != null && !boxInitBean.getLogo_version_list().isEmpty()) {
                            session.setSplashVersion(boxInitBean.getLogo_version_list().get(0).getVersion());
                        }
                    }
                }else{
                    ServerInfo serverInfo = session.getServerInfo();
                    if (serverInfo != null) {
                        String baseUrl = serverInfo.getDownloadUrl();
                        String url = baseUrl + boiteBean.getLogo_url();
                        if (!TextUtils.isEmpty(boiteBean.getLogo_url())) {
                            File tarFile = new File(path);
                            if (tarFile.exists()) {
                                tarFile.delete();
                            }
                            AppApi.downloadLOGO(url, context, this, path);
                        }
                    }
                }
            } else {
                // 做容错，当md5比对一致时设置一次期号
                session.setSplashVersion(boxInitBean.getLogo_version_list().get(0).getVersion());
            }
        }

        /**下载开机视频*/
        if (AppUtils.isSVT()&&boiteBean.getBootvideo()!=null
                &&!TextUtils.isEmpty(boiteBean.getBootvideo().getUrl())
                &&!TextUtils.isEmpty(boiteBean.getBootvideo().getMd5())){
            String bvideoUrl = boiteBean.getBootvideo().getUrl();
            String bvideoMd5 = boiteBean.getBootvideo().getMd5();
            String basePath = AppUtils.getMainMediaPath()+File.separator;
            File file = new File(basePath,ConstantValues.BOOT_VIDEO_TEMP);
            boolean need=false;
            if (file.exists()){
                String localMd5 = AppUtils.getEasyMd5(file);
                if (!localMd5.equals(bvideoMd5)){
                    need = true;
                    file.delete();
                }
            }else{
                need = true;
            }
            if (need){
                String fileName = "temp_"+ConstantValues.BOOT_VIDEO_TEMP;
                boolean isDownloaded = new ProgressDownloader(context,bvideoUrl,basePath,fileName,true).downloadByRange();
                if (isDownloaded){
                    new File(basePath,fileName).renameTo(new File(basePath,ConstantValues.BOOT_VIDEO_TEMP));
                    ShellUtils.copyBootVideo(new File(basePath,ConstantValues.BOOT_VIDEO_TEMP));
                }
            }
        }


    }

    /**
     * 获取生日歌相关节目媒体文件
     */
    private void getBirthdayOndemandFromCloudPlatform(){
        try {
            JsonBean jsonBean = AppApi.getBirthdayOndemandFromCloudPlatform(context,this,session.getEthernetMac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                return;
            }
            BirthdayOndemandResult result = gson.fromJson(jsonObject.get("result").toString(), new TypeToken<BirthdayOndemandResult>() {
            }.getType());
            if (!isFirstRun&&result.getPeriod().equals(session.getBirthdayOndemandPeriod())){
                return;
            }
            handleBirthdayOndemandData(result);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleBirthdayOndemandData(BirthdayOndemandResult result){
        List<BirthdayOndemandBean> list = result.getDatalist();
        if (list!=null&&list.size()>0){
            List<String> fileNames = new ArrayList<>();
            dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.BIRTHDAY_ONDEMAND,null,null);
            session.setBirthdayOndemandDownloadPeriod(result.getPeriod());
            for (BirthdayOndemandBean bean:list){
                boolean isDownloaded;
                String basePath = AppUtils.getFilePath(AppUtils.StorageFile.birthday_ondemand);
                String path = AppUtils.getFilePath(AppUtils.StorageFile.birthday_ondemand) + bean.getMedia_name();
                String fileName =  bean.getMedia_name();
                if (AppUtils.isDownloadEasyCompleted(path, bean.getMd5())) {
                    isDownloaded = true;
                } else {
                    isDownloaded = new ProgressDownloader(context,bean.getOss_url(),basePath, fileName,true).downloadByRange();
                    if (isDownloaded && AppUtils.isDownloadEasyCompleted(path, bean.getMd5())) {
                        isDownloaded = true;
                    }
                }

                if (isDownloaded){
                    //下载成功以后将本地路径set到bean里，入库时使用
                    bean.setMedia_path(path);
                    String selection = DBHelper.MediaDBInfo.FieldName.MEDIAID + "=? ";
                    String[] selectionArgs = new String[]{bean.getMedia_id()};
                    List<BirthdayOndemandBean> birthdayOndemandBeanList = dbHelper.findBirthdayOndemandByWhere(selection,selectionArgs);
                    if (birthdayOndemandBeanList!=null&&birthdayOndemandBeanList.size()>0){
                        dbHelper.deleteDataByWhere(BIRTHDAY_ONDEMAND,selection,selectionArgs);
                    }
                    if (dbHelper.insertOrUpdateBirthdayOndemand(bean,false)){
                        fileNames.add(bean.getMedia_name());
                    }

                }
            }
            if (fileNames.size()==list.size()){
                //生日点播下载完成
                session.setBirthdayOndemandDownloadPeriod(result.getPeriod());
                session.setBirthdayOndemandPeriod(result.getPeriod());
                String birthday = AppUtils.getFilePath(AppUtils.StorageFile.birthday_ondemand);
                File[] birthdayFiles = new File(birthday).listFiles();
                for (File file : birthdayFiles) {
                    String fileName = file.getName();
                    if (!fileNames.contains(fileName)) {
                        file.delete();
                        LogUtils.d("删除文件===================" + file.getName());
                    }
                }

            }
        }
    }

    private void getInteractionAdsFromCloudPlatform(){
        try {
            JsonBean jsonBean = AppApi.postInteractionAdsFromCloudform(context,this,session.getEthernetMac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                return;
            }
            InteractionAdsResult interactionAdsResult = gson.fromJson(jsonObject.get("result").toString(), new TypeToken<InteractionAdsResult>() {
            }.getType());
            if (interactionAdsResult.getMedia_list()==null||interactionAdsResult.getMedia_list().size()==0){
                AppUtils.deleteInteractionAdsMedia(context);
            }
            if (!isFirstRun&&interactionAdsResult.getPeriod().equals(session.getInteractionAdsPeriod())){
                return;
            }
            handleInteractionAdsData(interactionAdsResult);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleInteractionAdsData(InteractionAdsResult interactionAdsResult){
        List<MediaLibBean> list = interactionAdsResult.getMedia_list();
        if (list!=null&&list.size()>0){
            List<String> fileNames = new ArrayList<>();
            dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.INTERACTION_ADS,null,null);
            for (MediaLibBean bean:list){
                boolean isDownloaded = false;
                String basePath = AppUtils.getFilePath(AppUtils.StorageFile.interaction_ads);
                String fileName = bean.getName();
                String path = AppUtils.getFilePath(AppUtils.StorageFile.interaction_ads) + bean.getName();
                if (bean.getMedia_type()==1){
                    isDownloaded = AppUtils.isDownloadEasyCompleted(path, bean.getMd5());
                }else if (bean.getMedia_type()==2){
                    isDownloaded = AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase());
                }
                if (!isDownloaded){
                    String url = BuildConfig.OSS_ENDPOINT+bean.getOss_path();
                    isDownloaded = new ProgressDownloader(context,url,basePath,fileName,true).downloadByRange();
                    if (isDownloaded
                            && (AppUtils.isDownloadEasyCompleted(path, bean.getMd5())
                            ||AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase()))) {
                        isDownloaded = true;
                    }
                }

                if (isDownloaded){
                    //下载成功以后将本地路径set到bean里，入库时使用
                    bean.setMediaPath(path);
                    bean.setPeriod(interactionAdsResult.getPeriod());
                    String selection = DBHelper.MediaDBInfo.FieldName.MEDIAID + "=? ";
                    String[] selectionArgs = new String[]{bean.getVid()};
                    List<MediaLibBean> listInteractionAds = dbHelper.findInteractionAdsByWhere(selection,selectionArgs);
                    if (listInteractionAds!=null&&listInteractionAds.size()>0){
                        dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.INTERACTION_ADS,selection,selectionArgs);
                    }
                    if (dbHelper.insertInteractionAds(bean)){
                        fileNames.add(bean.getName());
                    }

                }
            }
            if (fileNames.size()==list.size()){
                //互动广告下载完成
                session.setInteractionAdsPeriod(interactionAdsResult.getPeriod());

                String interaction = AppUtils.getFilePath(AppUtils.StorageFile.interaction_ads);
                File[] interactionFiles = new File(interaction).listFiles();
                for (File file : interactionFiles) {
                    String fileName = file.getName();
                    if (!fileNames.contains(fileName)) {
                        file.delete();
                        LogUtils.d("删除文件===================" + file.getName());
                    }
                }

            }
        }
    }

    /**
     * 获取活动商品相关广告数据
     */
    private void getGoodsProgramListFromCloudPlatform(){
        try{
            JsonBean jsonBean = AppApi.getGoodsProgramListFromCloudfrom(context,this,session.getEthernetMac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                return;
            }
            ActivityGoodsResult result = gson.fromJson(jsonObject.get("result").toString(), new TypeToken<ActivityGoodsResult>() {
            }.getType());
            if (!isFirstRun&&result.getPeriod().equals(session.getActivityAdsPeriod())){
                return;
            }
            if (result.getDatalist()==null||result.getDatalist().size()==0){
                AppUtils.deleteActivityAdsMedia(context);
                notifyToPlay();
                return;
            }
            handleGoodsProgramListData(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleGoodsProgramListData(ActivityGoodsResult result){
        List<ActivityGoodsBean> list = result.getDatalist();
        if (list!=null&&list.size()>0){
            List<String> fileNames = new ArrayList<>();
            dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.ACTIVITY_ADS,null,null);
            for (ActivityGoodsBean bean:list){
                boolean isDownloaded = false;
                String basePath = AppUtils.getFilePath(AppUtils.StorageFile.activity_ads);
                String fileName = bean.getName();
                String path = basePath + bean.getName();
                String qrcodeName = bean.getName()+"_qrcode.png";
                String qrcodePath = basePath + qrcodeName;
                if (bean.getMedia_type()==1){
                    isDownloaded = AppUtils.isDownloadEasyCompleted(path, bean.getMd5());
                }else if (bean.getMedia_type()==2){
                    isDownloaded = AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase());
                }
                if (!isDownloaded){
                    String url = BuildConfig.OSS_ENDPOINT+bean.getOss_path();
                    isDownloaded = new ProgressDownloader(context,url,basePath,fileName,true).downloadByRange();
                    if (isDownloaded
                            && (AppUtils.isDownloadEasyCompleted(path, bean.getMd5())
                            ||AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase()))) {
                        isDownloaded = true;
                    }else{
                        isDownloaded = false;
                    }
                }

                if (isDownloaded){
                    if (!TextUtils.isEmpty(bean.getQrcode_url())){
                        String qrcodeUrl = bean.getQrcode_url();
                        boolean downloadImg = new ProgressDownloader(context,qrcodeUrl,basePath,qrcodeName,true).downloadByRange();
                        if (downloadImg){
                            bean.setQrcode_path(qrcodePath);
                        }
                    }

                    //下载成功以后将本地路径set到bean里，入库时使用
                    bean.setMediaPath(path);
                    bean.setPeriod(result.getPeriod());
                    bean.setCreateTime(System.currentTimeMillis()+"");
                    String selection = DBHelper.MediaDBInfo.FieldName.GOODS_ID + "=? ";
                    String[] selectionArgs = new String[]{bean.getGoods_id()+""};
                    List<MediaLibBean> activityAdsList = dbHelper.findActivityAdsByWhere(selection,selectionArgs);
                    if (activityAdsList!=null&&activityAdsList.size()>0){
                        dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.ACTIVITY_ADS,selection,selectionArgs);
                    }
                    if (dbHelper.insertActivityAds(bean)){
                        fileNames.add(bean.getName());
                        fileNames.add(qrcodeName);
                    }

                }
            }
            if (fileNames.size()/2==list.size()){
                //互动广告下载完成
                session.setActivityAdsPeriod(result.getPeriod());

                String activityAds = AppUtils.getFilePath(AppUtils.StorageFile.activity_ads);
                File[] activityFiles = new File(activityAds).listFiles();
                for (File file : activityFiles) {
                    String fileName = file.getName();
                    if (!fileNames.contains(fileName)) {
                        file.delete();
                        LogUtils.d("删除文件===================" + file.getName());
                    }
                }
                notifyToPlay();
            }
        }
    }

    /**获取商城商品广告数据*/
    private void getShopGoodsListFromCloudPlatform(){
        try{
            JsonBean jsonBean = AppApi.getShopGoodsListFromCloudfrom(context,this,session.getEthernetMac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                return;
            }
            ShopGoodsResult result = gson.fromJson(jsonObject.get("result").toString(), new TypeToken<ShopGoodsResult>() {
            }.getType());
            if (!isFirstRun&&result.getPeriod().equals(session.getShopGoodsAdsPeriod())){
                return;
            }
            if (result.getDatalist()==null||result.getDatalist().size()==0){
                AppUtils.deleteShopGoodsAdsMedia(context);
                notifyToPlay();
                return;
            }
            handleShopGoodsListData(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void handleShopGoodsListData(ShopGoodsResult result){
        List<ShopGoodsBean> list = result.getDatalist();
        if (list!=null&&list.size()>0){
            List<String> fileNames = new ArrayList<>();
            dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SHOP_GOODS_ADS,null,null);
            String basePath = AppUtils.getFilePath(AppUtils.StorageFile.goods_ads);
            for (ShopGoodsBean bean:list){
                boolean isDownloaded = false;
                String fileName = bean.getName();
                String path = basePath + bean.getName();
                String qrcodeName = bean.getName()+"_qrcode.png";
                String qrcodePath = basePath + qrcodeName;
                if (bean.getMedia_type()==1){
                    isDownloaded = AppUtils.isDownloadEasyCompleted(path, bean.getMd5());
                }else if (bean.getMedia_type()==2){
                    isDownloaded = AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase());
                }
                if (!isDownloaded){
                    String url = BuildConfig.OSS_ENDPOINT+bean.getOss_path();
                    isDownloaded = new ProgressDownloader(context,url,basePath,fileName,true).downloadByRange();
                    if (isDownloaded
                            && (AppUtils.isDownloadEasyCompleted(path, bean.getMd5())
                            ||AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase()))) {
                        isDownloaded = true;
                    }else{
                        isDownloaded = false;
                    }
                }

                if (isDownloaded){
                    if (!TextUtils.isEmpty(bean.getQrcode_url())){
                        String qrcodeUrl = bean.getQrcode_url();
                        boolean downloadImg = new ProgressDownloader(context,qrcodeUrl,basePath,qrcodeName,true).downloadByRange();
                        if (downloadImg){
                            bean.setQrcode_path(qrcodePath);
                        }
                    }
                    //下载成功以后将本地路径set到bean里，入库时使用
                    bean.setMediaPath(path);
                    bean.setPeriod(result.getPeriod());
                    bean.setCreateTime(System.currentTimeMillis()+"");
                    if (dbHelper.insertShopGoodsAds(bean)){
                        fileNames.add(bean.getName());
                        fileNames.add(qrcodeName);
                    }

                }
            }
            if (fileNames.size()/2==list.size()){
                //互动广告下载完成
                session.setActivityAdsPeriod(result.getPeriod());

                String activityAds = AppUtils.getFilePath(AppUtils.StorageFile.goods_ads);
                File[] activityFiles = new File(activityAds).listFiles();
                for (File file : activityFiles) {
                    String fileName = file.getName();
                    if (!fileNames.contains(fileName)) {
                        file.delete();
                        LogUtils.d("删除文件===================" + file.getName());
                    }
                }
                notifyToPlay();
            }
        }
    }
    /**
     * 获取用户精选内容(热播内容)数据
     */
    private void getSelectContentFromCloudPlatform(){
        try{
            JsonBean jsonBean = AppApi.postSelectContentFromCloudfrom(context,this,session.getEthernetMac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                return;
            }
            SelectContentResult result = gson.fromJson(jsonObject.get("result").toString(), new TypeToken<SelectContentResult>() {
            }.getType());
            if (!isFirstRun&&result.getPeriod().equals(session.getSelectContentPeriod())){
                return;
            }
            if (result.getDatalist()==null||result.getDatalist().size()==0){
                String selection = DBHelper.MediaDBInfo.FieldName.TYPE + "=? ";
                String[] selectionArgs = new String[]{ConstantValues.SELECT_CONTENT_HOT};
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SELECT_CONTENT,selection,selectionArgs);
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.MEDIA_ITEM,selection,selectionArgs);
            }
            handleSelectContentData(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleSelectContentData(SelectContentResult result){
        List<SelectContentBean> list = result.getDatalist();
        if (list!=null&&list.size()>0){
            List<List<String>> fileSize = new ArrayList<>();
            List<String> fileNames = new ArrayList<>();
            String selection = DBHelper.MediaDBInfo.FieldName.TYPE + "=? ";
            String[] selectionArgs = new String[]{ConstantValues.SELECT_CONTENT_HOT};
            dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SELECT_CONTENT,selection,selectionArgs);
            for (SelectContentBean bean:list){
                if (bean.getSubdata()!=null&&bean.getSubdata().size()>0){
                    selection = DBHelper.MediaDBInfo.FieldName.ID + "=? ";
                    selectionArgs = new String[]{bean.getId()+""};
                    dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.MEDIA_ITEM,selection,selectionArgs);
                    List<String> subNames = new ArrayList<>();
                    for (MediaItemBean item:bean.getSubdata()){
                        boolean isDownloaded = false;
                        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.select_content);
                        String fileName = item.getName();
                        String path = basePath + item.getName();
                        if (bean.getMedia_type()==1){
                            isDownloaded = AppUtils.isDownloadEasyCompleted(path, item.getMd5());
                        }else if (bean.getMedia_type()==2||bean.getMedia_type()==21){
                            isDownloaded = AppUtils.isDownloadCompleted(path, item.getMd5().toUpperCase());
                        }
                        if (!isDownloaded){
                            String url = BuildConfig.OSS_ENDPOINT+item.getOss_path();
                            isDownloaded = new ProgressDownloader(context,url,basePath,fileName,true).downloadByRange();
                            if (isDownloaded
                                    && (AppUtils.isDownloadEasyCompleted(path, item.getMd5())
                                    ||AppUtils.isDownloadCompleted(path, item.getMd5().toUpperCase()))) {
                                isDownloaded = true;
                            }else{
                                isDownloaded = false;
                            }
                        }
                        if (isDownloaded){
                            //下载成功以后将本地路径set到bean里，入库时使用
                            item.setId(bean.getId());
                            item.setCreateTime(System.currentTimeMillis()+"");
                            item.setOss_path(path);
                            item.setMedia_type(bean.getMedia_type());
                            item.setType(result.getType());
                            if (dbHelper.insertMediaItem(item)){
                                subNames.add(item.getName());
                            }

                        }
                    }
                    if (subNames.size()==bean.getSubdata().size()){
                        bean.setCreateTime(System.currentTimeMillis()+"");
                        bean.setPeriod(result.getPeriod());
                        bean.setType(result.getType());
                        if (dbHelper.insertSelectContent(bean)){
                            fileSize.add(subNames);
                            fileNames.addAll(subNames);
                        }
                    }
                }
            }
            if (fileSize.size()==list.size()){
                //精选内容下载完成
                session.setSelectContentPeriod(result.getPeriod());
            }
        }
    }

    /**
     * 获取用户发现内容数据
     */
    private void getDiscoverContentFromCloudPlatform(){
        try{
            JsonBean jsonBean = AppApi.getDiscoverContentFromCloudfrom(context,this,session.getEthernetMac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                return;
            }
            SelectContentResult result = gson.fromJson(jsonObject.get("result").toString(), new TypeToken<SelectContentResult>() {
            }.getType());
            if (!isFirstRun&&result.getPeriod().equals(session.getDiscoverContentPeriod())){
                return;
            }
            if (result.getDatalist()==null||result.getDatalist().size()==0){
                String selection = DBHelper.MediaDBInfo.FieldName.TYPE + "=? ";
                String[] selectionArgs = new String[]{ConstantValues.SELECT_CONTENT_DISCOVER};
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SELECT_CONTENT,selection,selectionArgs);
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.MEDIA_ITEM,selection,selectionArgs);
                AppUtils.deleteSelectContentMedia(context);
                notifyToPlay();
            }
            handleDiscoverContentData(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleDiscoverContentData(SelectContentResult result){
        List<SelectContentBean> list = result.getDatalist();
        if (list!=null&&list.size()>0){
            List<List<String>> fileSize = new ArrayList<>();
            List<String> fileNames = new ArrayList<>();
            String selection = DBHelper.MediaDBInfo.FieldName.TYPE + "=? ";
            String[] selectionArgs = new String[]{ConstantValues.SELECT_CONTENT_DISCOVER};
            dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SELECT_CONTENT,selection,selectionArgs);
            for (SelectContentBean bean:list){
                if (bean.getSubdata()!=null&&bean.getSubdata().size()>0){
                    selection = DBHelper.MediaDBInfo.FieldName.ID + "=? ";
                    selectionArgs = new String[]{bean.getId()+""};
                    dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.MEDIA_ITEM,selection,selectionArgs);
                    List<String> subNames = new ArrayList<>();
                    for (MediaItemBean item:bean.getSubdata()){
                        boolean isDownloaded = false;
                        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.select_content);
                        String fileName = item.getName();
                        String path = basePath + item.getName();
                        if (bean.getMedia_type()==1){
                            isDownloaded = AppUtils.isDownloadEasyCompleted(path, item.getMd5());
                        }else if (bean.getMedia_type()==2||bean.getMedia_type()==21){
                            isDownloaded = AppUtils.isDownloadCompleted(path, item.getMd5().toUpperCase());
                        }
                        if (!isDownloaded){
                            String url = BuildConfig.OSS_ENDPOINT+item.getOss_path();
                            isDownloaded = new ProgressDownloader(context,url,basePath,fileName,true).downloadByRange();
                            if (isDownloaded
                                    && (AppUtils.isDownloadEasyCompleted(path, item.getMd5())
                                    ||AppUtils.isDownloadCompleted(path, item.getMd5().toUpperCase()))) {
                                isDownloaded = true;
                            }else{
                                isDownloaded = false;
                            }
                        }
                        if (isDownloaded){
                            //下载成功以后将本地路径set到bean里，入库时使用
                            item.setId(bean.getId());
                            item.setCreateTime(System.currentTimeMillis()+"");
                            item.setOss_path(path);
                            item.setMedia_type(bean.getMedia_type());
                            item.setType(result.getType());
                            if (dbHelper.insertMediaItem(item)){
                                subNames.add(item.getName());
                            }

                        }
                    }
                    if (subNames.size()==bean.getSubdata().size()){
                        bean.setCreateTime(System.currentTimeMillis()+"");
                        bean.setPeriod(result.getPeriod());
                        bean.setType(result.getType());
                        if (dbHelper.insertSelectContent(bean)){
                            fileSize.add(subNames);
                            fileNames.addAll(subNames);
                        }
                    }
                }
            }
            if (fileSize.size()==list.size()){
                //精选内容下载完成
                session.setDiscoverContentPeriod(result.getPeriod());

                AppUtils.deleteSelectContentMedia(context);
                notifyToPlay();
            }
        }
    }

    /**
     * 获取欢迎词资源数据(含封面和mp3音乐)
     */
    private void getWelcomeResourceFromCloudPlatform(){
        try{
            JsonBean jsonBean = AppApi.getWelcomeResourceFromCloudfrom(context,this,session.getEthernetMac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                return;
            }
            WelcomeResourceResult result = gson.fromJson(jsonObject.get("result").toString(), new TypeToken<WelcomeResourceResult>() {
            }.getType());
            if (!isFirstRun&&result.getPeriod().equals(session.getWelcomeResourcePeriod())){
                return;
            }
            if (result.getDatalist()==null||result.getDatalist().size()==0){
                String selection = null;
                String[] selectionArgs = null;
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.WELCOME_RESOURCE,selection,selectionArgs);
                AppUtils.deleteWelcomeResource(context);
            }
            handleWelcomeResourceData(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleWelcomeResourceData(WelcomeResourceResult result){
        List<WelcomeResourceBean> list = result.getDatalist();
        if (list!=null&&list.size()>0){
            List<String> fileNames = new ArrayList<>();
            String selection = null;
            String[] selectionArgs = null;
            dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.WELCOME_RESOURCE,selection,selectionArgs);
            for (WelcomeResourceBean bean:list){
                boolean isDownloaded = false;
                String basePath = AppUtils.getFilePath(AppUtils.StorageFile.welcome_resource);
                String fileName = bean.getName();
                String path = basePath + fileName;
                int media_type = bean.getMedia_type();
                if (media_type==1||media_type==3){
                    isDownloaded = AppUtils.isDownloadEasyCompleted(path, bean.getMd5());
                }else if (media_type==2||media_type==4||media_type==5){
                    isDownloaded = AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase());
                }
                if (!isDownloaded){
                    String url = BuildConfig.OSS_ENDPOINT+bean.getOss_path();
                    isDownloaded = new ProgressDownloader(context,url,basePath,fileName,true).downloadByRange();
                    if (isDownloaded
                            && (AppUtils.isDownloadEasyCompleted(path, bean.getMd5())
                            ||AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase()))) {
                        isDownloaded = true;
                    }else{
                        isDownloaded = false;
                    }
                }
                if (isDownloaded){
                    //下载成功以后将本地路径set到bean里，入库时使用
                    bean.setMedia_path(path);
                    bean.setCreateTime(System.currentTimeMillis()+"");
                    if (dbHelper.insertWelcomeResource(bean)){
                        fileNames.add(bean.getName());
                    }

                }
            }
            if (fileNames.size()==list.size()){
                //精选内容下载完成
                session.setWelcomeResourcePeriod(result.getPeriod());
                AppUtils.deleteWelcomeResource(context);
            }
        }
    }
    /**
     * 获取小平台节目单媒体文件
     * OSSsource true是从OSS下载，false是从实体小平台下载
     */
    private void getProgramDataFromSmallPlatform() {
        isProCompleted = false;
        String smallType=null;
        try {
            JsonBean jsonBean = AppApi.getProgramDataFromSmallPlatform(this, this, session.getEthernetMac());
            // 保存拿到的数据到本地
            FileUtils.write(ConstantValues.PRO_DATA_PATH, jsonBean.getConfigJson());

            SetBoxTopResult setBoxTopResult = gson.fromJson(jsonBean.getConfigJson(), new TypeToken<SetBoxTopResult>() {
            }.getType());
            if (setBoxTopResult.getCode() == AppApi.HTTP_RESPONSE_STATE_SUCCESS) {
                if (setBoxTopResult.getResult() != null) {
                    setTopBoxBean = setBoxTopResult.getResult();
                    smallType = jsonBean.getSmallType();
                    handleSmallPlatformProgramData(smallType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!TextUtils.isEmpty(e.getMessage())&&(e.getMessage().contains("failed to connect to")||e.getMessage().contains("No route to host"))){
                handleSmallPlatformProgramData(smallType);
            }
        }
    }

    /**
     * 处理小平台返回的节目单数据（包含内容数据和宣传片占位和广告占位）
     * OSSsource true是从OSS下载，false是从实体小平台下载
     */
    private void handleSmallPlatformProgramData(String smallType) {
        if (setTopBoxBean == null
                || setTopBoxBean.getPlaybill_list() == null
                || setTopBoxBean.getPlaybill_list().isEmpty()) {
            return;
        }
        LogFileUtil.write("轮播视频下载进入下载逻辑");
        //该集合包含三部分数据，1:真实节目，2：宣传片占位符.3:广告占位符
        ArrayList<ProgramBean> playbill_list = setTopBoxBean.getPlaybill_list();
        //当前最新节目期号
        String proPeriod = "";
        for (ProgramBean item : playbill_list) {
            if (item.getMedia_lib()==null||item.getMedia_lib().size()==0||item.getVersion()==null){
                continue;
            }
            String logUUID = String.valueOf(System.currentTimeMillis());
            if (ConstantValues.PRO.equals(item.getVersion().getType())) {
                proPeriod = item.getVersion().getVersion();

                //如果期数相同，则表示数据没有改变，不需要执行后续的下载动作（第一次循环即便期号相同，也做一次遍历作为文件校验）
                LogUtils.d("===============proMediaPeriod===========" + session.getProPeriod());
                if (!isFirstRun &&
                        (session.getProPeriod().equals(proPeriod) || session.getProNextPeriod().equals(proPeriod))) {
                    isProCompleted = true;
                    mProCompletedPeriod = proPeriod;
                    continue;
                }
                String selection = DBHelper.MediaDBInfo.FieldName.MEDIATYPE + "=? and " +
                        DBHelper.MediaDBInfo.FieldName.PERIOD + "=? ";
                String[] selectionArgs = new String[]{ConstantValues.PRO, proPeriod};
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWPLAYLIST,selection, selectionArgs);
                // 设置下载中期号
                session.setProDownloadPeriod(proPeriod);

                // 记录下载开始日志
                int count = item.getMedia_lib() == null ? 0 : item.getMedia_lib().size();
                LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                        String.valueOf(System.currentTimeMillis()), "start", "pro_down", proPeriod,
                        "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(count));
            }

            VersionInfo versionInfo = item.getVersion();
            if (versionInfo == null || TextUtils.isEmpty(versionInfo.getType())) {
                continue;
            }

            List<MediaLibBean> mediaLibList = item.getMedia_lib();
            int downloadedCount = 0;
            if (mediaLibList != null && mediaLibList.size() > 0) {

                ServerInfo serverInfo = session.getServerInfo();
                if (serverInfo == null) {
                    break;
                }

                String baseUrl = serverInfo.getDownloadUrl();
                if (!TextUtils.isEmpty(baseUrl) && baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                }

                LogUtils.d("---------轮播视频开始下载---------");
                for (MediaLibBean mediaItem : mediaLibList) {
                    try {
                        boolean isChecked = false;
                        boolean isNewDownload = false;
                        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.media);
                        String fileName = mediaItem.getName();
                        String path = basePath + fileName;
                        //判断当前数据是节目还是其他，如果是节目走下载逻辑,其他则直接入库
                        if (ConstantValues.PRO.equals(versionInfo.getType())) {

                            LogUtils.v("****开始下载pro视频:"+mediaItem.getChinese_name()+"****");
                            // 下载、校验
                            if (AppUtils.isDownloadEasyCompleted(path, mediaItem.getMd5())
                                    ||AppUtils.isDownloadCompleted(path, mediaItem.getMd5().toUpperCase())) {
                                isChecked = true;
                                LogUtils.v("****pro视频:"+mediaItem.getChinese_name()+"下载完成****");
                            } else {
                                LogFileUtil.write("轮播视频下载是从实体还是虚拟:"+smallType);
                                boolean isDownloaded ;
                                //虚拟小平台下载
                                if (ConstantValues.VIRTUAL.equals(smallType)){
                                    String url = BuildConfig.OSS_ENDPOINT + mediaItem.getOss_path();
                                    isDownloaded = new ProgressDownloader(context,url,basePath,fileName,true).downloadByRange();
                                }else {
                                    String url = baseUrl + mediaItem.getUrl();
                                    isDownloaded = new ProgressDownloader(context,url,basePath,fileName,false).downloadByRange();
                                }
                                if (mediaItem.getMedia_type()==1){
                                    isDownloaded =  AppUtils.isDownloadEasyCompleted(path, mediaItem.getMd5());
                                }else if (mediaItem.getMedia_type()==2){
                                    isDownloaded = AppUtils.isDownloadCompleted(path, mediaItem.getMd5().toUpperCase());
                                }
                                if (isDownloaded) {
                                    isChecked = true;
                                    isNewDownload = true;
                                    LogUtils.v("****pro视频:"+mediaItem.getChinese_name()+"下载完成****");
                                }
                            }
                        } else {
                            isChecked = true;
                        }
                        // 校验通过、插库
                        if (isChecked) {
                            if (!TextUtils.isEmpty(fileName)){
                                mediaItem.setMediaPath(path);
                            }
                            String selection = DBHelper.MediaDBInfo.FieldName.ADS_ORDER + "=? and " +
                                    DBHelper.MediaDBInfo.FieldName.PERIOD + "=? ";
                            String[] selectionArgs = new String[]{mediaItem.getOrder() + "", mediaItem.getPeriod()};
                            List<MediaLibBean> list = dbHelper.findNewPlayListByWhere(selection, selectionArgs);
                            if (list == null || list.isEmpty()) {
                                // 插库成功，downloadedCount加1
                                if (dbHelper.insertOrUpdateNewPlayListLib(mediaItem, -1)) {
                                    downloadedCount++;

                                    if (isNewDownload) {
                                        notifyToPlay();
                                    }
                                }
                            } else {
                                downloadedCount++;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (ConstantValues.PRO.equals(versionInfo.getType())) {
                // 下载完成（mediaLibList==null或size为0也认为是下载完成，认为新的节目单中没有该类型的数据）
                if (mediaLibList == null || downloadedCount == mediaLibList.size()) {
                    LogUtils.d("---------节目视频下载完成---------");
                    LogFileUtil.write("轮播视完成");
                    isProCompleted = true;
                    mProCompletedPeriod = proPeriod;
                    // 记录日志
                    // 记录下载完成日志
                    LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                            String.valueOf(System.currentTimeMillis()), "end", "pro_down", proPeriod,
                            "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(downloadedCount));
                    LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()),
                            Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                            String.valueOf(System.currentTimeMillis()), "update", versionInfo.getType(), "",
                            "", Session.get(context).getVersionName(), versionInfo.getVersion(),
                            Session.get(context).getBirthdayOndemandPeriod(), "");
                } else {
                    isProCompleted = false;
                    if (!isFirstRun) {
                        // 记录下载中止日志
                        LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                                String.valueOf(System.currentTimeMillis()), "suspend", "pro_down", proPeriod,
                                "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(downloadedCount));
                    }
                    if(pro_timeout_count>5){
                        pro_timeout_count = 0;
                    }else{
                        pro_timeout_count ++;
                        handleSmallPlatformProgramData(smallType);
                    }
                }
            }
        }
    }

    /**
     * 获取小平台宣传片媒体文件
     * OSSsource true从OSS下载，false从实体小平台下载
     */
    private void getAdvDataFromSmallPlatform() {
        String smallType=null;
        try {
            JsonBean jsonBean = AppApi.getAdvDataFromSmallPlatform(this, this, session.getEthernetMac());
            // 保存拿到的数据到本地
            FileUtils.write(ConstantValues.ADV_DATA_PATH, jsonBean.getConfigJson());

            Object result = gson.fromJson(jsonBean.getConfigJson(), new TypeToken<ProgramBeanResult>() {
            }.getType());
            ProgramBeanResult programBeanResult = (ProgramBeanResult) result;
            if (programBeanResult.getCode() == AppApi.HTTP_RESPONSE_STATE_SUCCESS) {
                if (programBeanResult.getResult() != null) {
                    advProgramBean = programBeanResult.getResult();
                    smallType = jsonBean.getSmallType();
                    handleSmallPlatformAdvData(smallType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!TextUtils.isEmpty(e.getMessage())&&(e.getMessage().contains("failed to connect to")||e.getMessage().contains("No route to host"))){
                handleSmallPlatformAdvData(smallType);
            }
        }
    }

    /**
     * 处理小平台返回的宣传片数据(下载完宣传片数据之后需要更新到节目单中，组合成可播放的节目单)
     */
    private void handleSmallPlatformAdvData(String smallType) {
        if (advProgramBean == null
                || advProgramBean.getVersion() == null
                || TextUtils.isEmpty(advProgramBean.getVersion().getVersion())) {
            return;
        }
        LogFileUtil.write("宣传片下载进入下载逻辑");
        String advPeriod = advProgramBean.getVersion().getVersion();
        if (!isFirstRun &&
                (session.getAdvPeriod().equals(advPeriod) || session.getAdvNextPeriod().equals(advPeriod))) {
            return;
        }

        ServerInfo serverInfo = session.getServerInfo();
        if (serverInfo == null) {
            return;
        }
        String baseUrl = serverInfo.getDownloadUrl();
        if (!TextUtils.isEmpty(baseUrl) && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String logUUID = String.valueOf(System.currentTimeMillis());
        // 记录下载开始日志
        int count = advProgramBean.getMedia_lib() == null ? 0 : advProgramBean.getMedia_lib().size();
        LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                String.valueOf(System.currentTimeMillis()), "start", "adv_down", advPeriod,
                "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(count));

        boolean isAdvCompleted;
        session.setAdvDownloadPeriod(advProgramBean.getVersion().getVersion());
        int advDownloadedCount = 0;
        if (advProgramBean.getMedia_lib() != null && advProgramBean.getMedia_lib().size() > 0) {
            for (MediaLibBean bean : advProgramBean.getMedia_lib()) {
                String basePath = AppUtils.getFilePath(AppUtils.StorageFile.media);
                String fileName = bean.getName();
                String path = basePath + bean.getName();

                boolean isChecked = false;
                try {
                    LogUtils.v("****开始下载adv视频:"+bean.getChinese_name()+"****");
                    // 下载、校验
                    if (AppUtils.isDownloadEasyCompleted(path, bean.getMd5())
                            ||AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase())) {
                        isChecked = true;
                    } else {
                        boolean isDownloaded;
                        //虚拟小平台
                        if (ConstantValues.VIRTUAL.equals(smallType)){
                            String url = BuildConfig.OSS_ENDPOINT+bean.getOss_path();
                            isDownloaded = new ProgressDownloader(context,url,basePath,fileName,true).downloadByRange();
                        }else{
                            String url = baseUrl + bean.getUrl();
                            isDownloaded = new ProgressDownloader(context,url,basePath,fileName,false).downloadByRange();
                        }
                        if (bean.getMedia_type()==1){
                            isDownloaded =  AppUtils.isDownloadEasyCompleted(path, bean.getMd5());
                        }else if (bean.getMedia_type()==2){
                            isDownloaded = AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase());
                        }
                        if (isDownloaded) {
                            isChecked = true;
                        }
                    }
                    LogUtils.v("****adv视频:"+bean.getChinese_name()+"下载完成****");
                    if (isChecked) {
                        // ADV是先在handleSmallPlatformProgramData()中插入，插入时的期号是节目单号，然后在这里更新实际数据
                        String selection = DBHelper.MediaDBInfo.FieldName.MEDIATYPE + "=? and " +
                                DBHelper.MediaDBInfo.FieldName.LOCATION_ID + "=? and " +
                                DBHelper.MediaDBInfo.FieldName.PERIOD + "=? ";

                        String[] selectionArgs = new String[]{bean.getType(), bean.getLocation_id(), advPeriod};
                        List<MediaLibBean> list = dbHelper.findNewPlayListByWhere(selection, selectionArgs);
                        String[] selectionArgs2 = new String[]{bean.getType(), bean.getLocation_id(), advProgramBean.getMenu_num()};

                        if (list!=null&&!list.isEmpty()){
                            if (list.size() > 1) {
                                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWPLAYLIST, selection, selectionArgs);
                            } else if (list.size() == 1) {
                                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWPLAYLIST, selection, selectionArgs2);
                                advDownloadedCount++;
                            }
                        }else{
                            list = dbHelper.findNewPlayListByWhere(selection, selectionArgs2);
                            long id = -1;
                            if (list != null) {
                                if (list.size() > 1) {
                                    dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWPLAYLIST, selection, selectionArgs2);
                                } else if (list.size() == 1) {
                                    id = list.get(0).getId();
                                }
                            }
                            if (id != -1) {
                                bean.setOrder(list.get(0).getOrder());
                                bean.setMediaPath(path);
                                // 插库成功，downloadedCount加1
                                if (dbHelper.insertOrUpdateNewPlayListLib(bean, id)) {
                                    advDownloadedCount++;
                                }
                            }
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (advDownloadedCount == advProgramBean.getMedia_lib().size()) {
                LogFileUtil.write("宣传片下载完成");
                isAdvCompleted = true;
            } else {
                isAdvCompleted = false;
            }
        } else {
            isAdvCompleted = true;
        }

        // 记录日志
        if (isAdvCompleted) {
            // 记录下载完成日志
            LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "end", "adv_down", advPeriod,
                    "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(advDownloadedCount));
        } else {
            // 记录下载中止日志
            LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "suspend", "adv_down", advPeriod,
                    "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(advDownloadedCount));

        }

        if (isAdvCompleted && isProCompleted && !TextUtils.isEmpty(advProgramBean.getMenu_num()) &&
                advProgramBean.getMenu_num().equals(mProCompletedPeriod)) {
            isFirstRun = false;
            // 记录日志
            LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()),
                    Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "update", advProgramBean.getVersion().getType(), "",
                    "", Session.get(context).getVersionName(), advProgramBean.getVersion().getVersion(),
                    Session.get(context).getBirthdayOndemandPeriod(), "");

            // 标识是立即播放还是预约发布
            boolean fillCurrentBill = true;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date pubDate = format.parse(setTopBoxBean.getPub_time());
                fillCurrentBill = pubDate.getTime() <= System.currentTimeMillis();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (fillCurrentBill) {
                session.setProPeriod(mProCompletedPeriod);
                session.setAdvPeriod(advPeriod);
            } else {
                session.setProNextPeriod(mProCompletedPeriod);
                session.setProNextMediaPubTime(setTopBoxBean.getPub_time());
                session.setAdvNextPeriod(advPeriod);
            }
            LogFileUtil.write("宣传片下载完成,进行非当前期删除动作");
            LogFileUtil.write("宣传片下载完成,进行非当前期删除动作,pro期号："+session.getProPeriod());
            LogFileUtil.write("宣传片下载完成,进行非当前期删除动作,next_pro期号："+session.getProNextPeriod());
            LogFileUtil.write("宣传片下载完成,进行非当前期删除动作,adv期号："+session.getAdvPeriod());
            LogFileUtil.write("宣传片下载完成,进行非当前期删除动作,next_adv期号："+session.getAdvNextPeriod());
            //2019-01-02 当屏幕卡死的时候出现视频adv pro全部被删除的情况，此处做非空判断,如果为空则不进行删除动作
            if (!TextUtils.isEmpty(session.getProPeriod())
                    &&!TextUtils.isEmpty(session.getAdvPeriod())){
                // 删除下载表中的当前、非预发布的节目单的内容
                String selection = DBHelper.MediaDBInfo.FieldName.PERIOD + "!=? AND " + DBHelper.MediaDBInfo.FieldName.PERIOD + "!=? AND " +
                        DBHelper.MediaDBInfo.FieldName.PERIOD + "!=? AND " + DBHelper.MediaDBInfo.FieldName.PERIOD + "!=? ";
                String[] selectionArgs;
                selectionArgs = new String[]{session.getProPeriod(), session.getProNextPeriod(), session.getAdvPeriod(), session.getAdvNextPeriod()};
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWPLAYLIST, selection, selectionArgs);
                // 将下载表中的内容拷贝到播放表
                LogFileUtil.write("宣传片下载完成,将新的一期完整节目从下载表拷贝到正式播放表");
                dbHelper.copyTableMethod(DBHelper.MediaDBInfo.TableName.NEWPLAYLIST, DBHelper.MediaDBInfo.TableName.PLAYLIST);
                AppUtils.deleteOldMedia(context,true);
                // 如果是填充当前期（立即播放）的话，通知ADSPlayer播放
                if (fillCurrentBill) {
                    notifyToPlay();
                }
            }

        }

        if (!isAdvCompleted){
            if (adv_timeout_count<5){
                adv_timeout_count ++;
                handleSmallPlatformAdvData(smallType);
            }else {
                adv_timeout_count = 0;
            }
        }


    }

    /**
     * 拉取小平台上广告媒体文件
     */
    private void getAdsDataFromSmallPlatform() {
        String smallType = null;
        try {
            JsonBean jsonBean = AppApi.getAdsDataFromSmallPlatform(this, this, session.getEthernetMac());
            // 保存拿到的数据到本地
            FileUtils.write(ConstantValues.ADS_DATA_PATH, jsonBean.getConfigJson());
            Object result = gson.fromJson(jsonBean.getConfigJson(), new TypeToken<ProgramBeanResult>() {
            }.getType());
            ProgramBeanResult programBeanResult = (ProgramBeanResult) result;
            if (programBeanResult.getCode() == AppApi.HTTP_RESPONSE_STATE_SUCCESS) {
                if (programBeanResult.getResult() != null) {
                    adsProgramBean = programBeanResult.getResult();
                    smallType = jsonBean.getSmallType();
                    handleSmallPlatformAdsData(smallType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!TextUtils.isEmpty(e.getMessage())&&(e.getMessage().contains("failed to connect to")||e.getMessage().contains("No route to host"))){
                handleSmallPlatformAdsData(smallType);
            }
        }
    }

    /**
     * 通过小平台获取广告数据
     */
    private void handleSmallPlatformAdsData(String smallType) {
        if (adsProgramBean == null
                || adsProgramBean.getVersion() == null
                || TextUtils.isEmpty(adsProgramBean.getVersion().getVersion())
                || adsProgramBean.getMedia_lib()==null
                || adsProgramBean.getMedia_lib().size()==0) {
            AppUtils.deleteAdsData(context);
            return;
        }
        String adsPeriod = adsProgramBean.getVersion().getVersion();
        if (!isAdsFirstRun && session.getAdsPeriod().equals(adsPeriod)) {
            return;
        }

        // 清空ads下载表
        dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWADSLIST, null, null);

        String logUUID = String.valueOf(System.currentTimeMillis());
        // 记录下载开始日志
        int count = adsProgramBean.getMedia_lib() == null ? 0 : adsProgramBean.getMedia_lib().size();
        LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                String.valueOf(System.currentTimeMillis()), "start", "ads_down", adsPeriod,
                "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(count));

        session.setAdsDownloadPeriod(adsPeriod);
        boolean isAdsCompleted ;
        int adsDownloadedCount = 0;
        if (adsProgramBean.getMedia_lib() != null && adsProgramBean.getMedia_lib().size() > 0) {
            ServerInfo serverInfo = session.getServerInfo();
            if (serverInfo == null) {
                return;
            }
            String baseUrl = serverInfo.getDownloadUrl();
            if (!TextUtils.isEmpty(baseUrl) && baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            for (MediaLibBean bean : adsProgramBean.getMedia_lib()) {
                String basePath = AppUtils.getFilePath(AppUtils.StorageFile.media);
                String fileName = bean.getName();
                String path = basePath +fileName;
                boolean isChecked = false;
                try {
                    // 下载、校验
                    LogUtils.v("****开始下载ads视频:"+bean.getChinese_name()+"****");
                    if (AppUtils.isDownloadEasyCompleted(path, bean.getMd5())
                            ||AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase())) {
                        isChecked = true;
                    } else {
                        boolean isDownloaded=false;
                        //虚拟小平台
                        String url;
                        boolean isVirtual;
                        if (ConstantValues.VIRTUAL.equals(smallType)){
                            isVirtual = true;
                            url = BuildConfig.OSS_ENDPOINT + bean.getOss_path();
                        }else{
                            isVirtual = false;
                            url = baseUrl + bean.getUrl();
                        }
                        new ProgressDownloader(context,url,basePath,fileName,isVirtual).downloadByRange();
                        if (bean.getMedia_type()==1){
                            isDownloaded =  AppUtils.isDownloadEasyCompleted(path, bean.getMd5());
                        }else if (bean.getMedia_type()==2){
                            isDownloaded = AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase());
                        }
                        if (isDownloaded) {
                            isChecked = true;
                        }
                    }
                    LogUtils.v("****ads视频:"+bean.getChinese_name()+"下载完成****");
                    if (isChecked) {
                        bean.setMediaPath(path);
                        // 插库成功，mDownloadedList中加入一条
                        if (dbHelper.insertOrUpdateNewAdsList(bean, -1)) {
                            adsDownloadedCount++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (adsDownloadedCount == adsProgramBean.getMedia_lib().size()) {
                isAdsCompleted = true;
            } else {
                isAdsCompleted = false;
            }
        } else {
            isAdsCompleted = true;
        }

        if (isAdsCompleted) {
            isAdsFirstRun = false;
            session.setAdsPeriod(adsPeriod);
            // 从ADS下载表拷贝到正式表
            dbHelper.copyTableMethod(DBHelper.MediaDBInfo.TableName.NEWADSLIST, DBHelper.MediaDBInfo.TableName.ADSLIST);
            // 记录日志
            // 记录下载完成日志
            LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "end", "ads_down", adsPeriod,
                    "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(adsDownloadedCount));
            LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()),
                    Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "update", adsProgramBean.getVersion().getType(), "",
                    "", Session.get(context).getVersionName(), adsProgramBean.getVersion().getVersion(),
                    Session.get(context).getBirthdayOndemandPeriod(), "");

            notifyToPlay();
        } else {
            // 记录下载中止日志
            LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "suspend", "ads_down", adsPeriod,
                    "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(adsDownloadedCount));
            if (ads_timeout_count <5){
                ads_timeout_count ++;
                handleSmallPlatformAdsData(smallType);
            }else{
                ads_timeout_count = 0;
            }

        }
    }

    /**
     * 获取百度聚屏广告
     * OSSsource true是从OSS下载，false是从实体小平台下载
     */
    private void getPolyAdsFromSmallPlatform(){
        try {
            JsonBean jsonBean = AppApi.getPolyAdsFromSmallPlatform(this,this,session.getEthernetMac());
            Object result = gson.fromJson(jsonBean.getConfigJson(), new TypeToken<ProgramBeanResult>() {
            }.getType());
            ProgramBeanResult programBeanResult = (ProgramBeanResult) result;
            if (programBeanResult.getCode() == AppApi.HTTP_RESPONSE_STATE_SUCCESS && programBeanResult.getResult() != null) {
                polyAdsProgramBean = programBeanResult.getResult();
                String smallType = jsonBean.getSmallType();
                handlePolyAdsFromSmallPlatform(smallType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 处理小平台返回的百度聚屏广告内容
     * 为了后期整合代码，故下载的聚屏广告并不单独创建表，统一放到rtb_ads下面
     * @param
     */
    private void handlePolyAdsFromSmallPlatform(String smallType){
        if (polyAdsProgramBean == null
                || polyAdsProgramBean.getVersion() == null
                || TextUtils.isEmpty(polyAdsProgramBean.getVersion().getVersion())) {
            return;
        }
        String adsPeriod = polyAdsProgramBean.getVersion().getVersion();
        if (!isPolyFirstRun&&session.getPolyAdsPeriod().equals(adsPeriod)) {
            return;
        }

        String logUUID = String.valueOf(System.currentTimeMillis());
        // 记录下载开始日志
        int count = polyAdsProgramBean.getMedia_lib() == null ? 0 : polyAdsProgramBean.getMedia_lib().size();
        LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                String.valueOf(System.currentTimeMillis()), "start", "poly_down", adsPeriod,
                "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(count));
        session.setPolyAdsDownloadPeriod(adsPeriod);

        boolean isAdsCompleted;
        int adsDownloadedCount = 0;
        ArrayList<String> fileNames = new ArrayList<>();    // 下载成功的文件名集合（后面删除老视频会用到）
        String selection = DBHelper.MediaDBInfo.FieldName.MEDIATYPE + "=? ";
        String[] selectionArgs = new String[]{ConstantValues.POLY_ADS};
        dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.RTB_ADS,selection, selectionArgs);
        if (polyAdsProgramBean.getMedia_lib() != null && polyAdsProgramBean.getMedia_lib().size() > 0) {
            ServerInfo serverInfo = session.getServerInfo();
            if (serverInfo == null) {
                return;
            }
            String baseUrl = serverInfo.getDownloadUrl();
            if (!TextUtils.isEmpty(baseUrl) && baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            LogUtils.d("---------poly广告视频开始下载---------");
            for (MediaLibBean bean : polyAdsProgramBean.getMedia_lib()) {
                String basePath = AppUtils.getFilePath(AppUtils.StorageFile.poly_ads);
                String fileName = bean.getName();
                String path = basePath + fileName;
                fileNames.add(bean.getName());
                boolean isChecked = false;
                try {
                    // 下载、校验
                    if (AppUtils.isDownloadEasyCompleted(path, bean.getMd5())
                            ||AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase())) {
                        isChecked = true;
                    } else {
                        boolean isDownloaded;
                        if (ConstantValues.VIRTUAL.equals(smallType)){
                            String url = BuildConfig.OSS_ENDPOINT + bean.getOss_path();
                            isDownloaded = new ProgressDownloader(context,url,basePath,fileName,true).downloadByRange();
                        }else {
                            String url = baseUrl + bean.getUrl();
                            isDownloaded = new ProgressDownloader(context,url,basePath,fileName,false).downloadByRange();
                        }

                        if (isDownloaded) {
                            if (bean.getMedia_type()==1&&AppUtils.isDownloadEasyCompleted(path, bean.getMd5())){
                                isChecked = true;
                            }else if (bean.getMedia_type()==2&&AppUtils.isDownloadCompleted(path, bean.getMd5().toUpperCase())){
                                isChecked = true;
                            }

                        }

                    }
                    if (isChecked) {
                        bean.setMediaPath(path);
                        // 入库
                        boolean isInsertSuccess = dbHelper.insertOrUpdateRTBAdsList(bean, false);
                        if (isInsertSuccess) {
                            adsDownloadedCount++;
                            if (!TextUtils.isEmpty(GlobalValues.NOT_FOUND_BAIDU_ADS_KEY) &&
                                    GlobalValues.NOT_FOUND_BAIDU_ADS_KEY.equals(bean.getTp_md5())) {
                                GlobalValues.NOT_FOUND_BAIDU_ADS_KEY = null;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (adsDownloadedCount == polyAdsProgramBean.getMedia_lib().size()) {
                isAdsCompleted = true;
            } else {
                isAdsCompleted = false;
            }
        } else {
            isAdsCompleted = true;
        }

        if (isAdsCompleted) {
            isPolyFirstRun = false;
            LogUtils.d("---------poly广告视频下载完成---------");
            session.setPolyAdsPeriod(adsPeriod);
            // 记录日志
            // 记录下载结束日志
            LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "end", "polyads_down", adsPeriod,
                    "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(adsDownloadedCount));
            LogReportUtil.get(context).sendAdsLog(String.valueOf(System.currentTimeMillis()),
                    Session.get(context).getBoiteId(), Session.get(context).getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "update", polyAdsProgramBean.getVersion().getType(), "",
                    "", Session.get(context).getVersionName(), polyAdsProgramBean.getVersion().getVersion(),
                    Session.get(context).getBirthdayOndemandPeriod(), "");


            deleteMediaFileNotInConfig(fileNames, AppUtils.StorageFile.poly_ads, DBHelper.MediaDBInfo.TableName.RTB_ADS);
            notifyToPlay();
        } else {
            // 记录下载中止日志
            LogReportUtil.get(this).sendAdsLog(logUUID, session.getBoiteId(), session.getRoomId(),
                    String.valueOf(System.currentTimeMillis()), "suspend", "polyads_down", adsPeriod,
                    "", session.getVersionName(), session.getAdsPeriod(), session.getBirthdayOndemandPeriod(), String.valueOf(adsDownloadedCount));
            if(poly_timeout_count<5){
                poly_timeout_count ++;
                handlePolyAdsFromSmallPlatform(smallType);
            }else {
                poly_timeout_count = 0;
            }

        }
    }

    private void getTVMatchDataFromSmallPlatform() {
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof TvPlayerGiecActivity ||activity instanceof TvPlayerActivity){
            return;
        }
        if (AppUtils.isMstar()) {
            AppApi.getTVMatchDataFromSmallPlatform(this, this);
        } else {
            AppApi.getGiecTVMatchDataFromSmallPlatform(this, this);
        }
    }

    private void notifyToPlay() {
        if (AppUtils.fillPlaylist(this, null, 1)) {
            LogUtils.d("发送通知更新播放列表广播");
            sendBroadcast(new Intent(ConstantValues.UPDATE_PLAYLIST_ACTION));
        }
    }

    /**
     * 删除没有在小平台配置文件内的点播文件和点播数据库记录
     *
     * @param arrayList
     * @param storage
     */
    private void deleteMediaFileNotInConfig(List<String> arrayList, AppUtils.StorageFile storage, String tableName) {
        File[] listFiles = new File(AppUtils.getFilePath(storage)).listFiles();
        if (arrayList==null||arrayList.size()==0){
            String selection ;
            String[] selectionArgs;
            List<MediaLibBean> list;
            switch (storage){
                case poly_ads:
                    selection = DBHelper.MediaDBInfo.FieldName.MEDIATYPE + "=? ";
                    selectionArgs = new String[]{ConstantValues.POLY_ADS};
                    dbHelper.deleteDataByWhere(tableName,selection,selectionArgs);
                    for (File file:listFiles){
                        if (file.isFile()){
                            file.delete();
                        }else {
                            FileUtils.deleteFile(file);
                        }
                    }
                    break;
                case rtb_ads:
                    selection = DBHelper.MediaDBInfo.FieldName.MEDIATYPE + "=? ";
                    selectionArgs = new String[]{"rtbads"};
                    list = dbHelper.findRtbadsMediaLibByWhere(selection,selectionArgs);
                    arrayList = new ArrayList<>();
                    if (list!=null&&list.size()>0){
                        for (MediaLibBean bean:list){
                            arrayList.add(bean.getName());
                        }
                    }
                    break;
            }

        }else{
            for (File file : listFiles) {
                if (file.isFile()) {
                    String oldName = file.getName();
                    if (!arrayList.contains(oldName)) {
                        if (file.delete()) {
                            String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
                            String[] selectionArgs = new String[]{oldName};
                            dbHelper.deleteDataByWhere(tableName, selection, selectionArgs);
                        }
                    }
                } else {
                    FileUtils.deleteFile(file);
                }
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSuccess(AppApi.Action method, Object obj) {
        switch (method) {
            case SP_GET_TV_MATCH_DATA_FROM_JSON:
                if (obj instanceof TvProgramResponse) {
                    TvProgramResponse response = (TvProgramResponse) obj;
                    TvOperate mtv = new TvOperate();
                    mtv.updateProgram(context, response);
                }
                break;
            case SP_GET_TV_MATCH_DATA_FROM_GIEC_JSON:
                if (obj instanceof TvProgramGiecResponse) {
                    TvProgramGiecResponse response = (TvProgramGiecResponse) obj;
                    ITVOperator tvOperate = TVOperatorFactory.getTVOperator(getApplicationContext(), TVOperatorFactory.TVType.GIEC);
                    for (AtvChannel atvChannel :
                            response.getTvChannelList()) {
                        atvChannel.setDisplayName(atvChannel.getChannelName());
//                        atvChannel.setDisplayNumber(atvChannel.getChannelNum() + "");
                    }
                    tvOperate.setAtvChannels(response.getTvChannelList());
                    session.setTvDefaultChannelNumber(response.getLockingChannelNum());
                }
                break;
            case SP_GET_LOGO_DOWN:
                if (obj instanceof File) {
                    File f = (File) obj;
                    byte[] fRead;
                    String md5Value = null;
                    try {
                        fRead = org.apache.commons.io.FileUtils.readFileToByteArray(f);
                        md5Value = AppUtils.getMD5(fRead);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //比较本地文件版本是否与服务器文件一致，如果一致则启动安装
                    if (md5Value != null && md5Value.equals(logo_md5)) {
                        try {
                            File file = new File(Environment.getExternalStorageDirectory(), session.getSplashPath());
                            if (file.exists()) {
                                file.delete();
                            }
                            String newPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/" + f.getName();
                            FileUtils.copyFile(f.getAbsolutePath(), newPath);
                            session.setSplashPath(newPath);
                            if (boxInitBean.getLogo_version_list() != null && !boxInitBean.getLogo_version_list().isEmpty()) {
                                session.setSplashVersion(boxInitBean.getLogo_version_list().get(0).getVersion());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case SP_GET_LOADING_IMG_DOWN:
                if (obj instanceof File) {
                    File f = (File) obj;
                    byte[] fRead = new byte[0];
                    String md5Value = null;
                    try {
                        fRead = org.apache.commons.io.FileUtils.readFileToByteArray(f);
                        md5Value = AppUtils.getMD5(fRead);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //比较本地文件版本是否与服务器文件一致，如果一致则启动安装
                    if (md5Value != null && md5Value.equals(loading_img_md5)) {
                        try {
                            File file = new File(Environment.getExternalStorageDirectory(), session.getLoadingPath());
                            if (file.exists()) {
                                file.delete();
                            }
                            String newPath = "/Pictures/" + f.getName();
                            FileUtils.copyFile(f.getAbsolutePath(), Environment.getExternalStorageDirectory().getAbsolutePath() + newPath);
                            session.setLoadingPath(newPath);
                            if (boxInitBean.getLoading_version_list() != null && !boxInitBean.getLoading_version_list().isEmpty()) {
                                session.setLoadingVersion(boxInitBean.getLoading_version_list().get(0).getVersion());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case CP_POST_SDCARD_STATE_JSON:
                LogUtils.d("上报SD卡状态成功");
                break;
        }
    }

    @Override
    public void onError(AppApi.Action method, Object obj) {
        switch (method) {
            case CP_POST_SDCARD_STATE_JSON:
                LogUtils.d("上报SD卡状态失败");
                break;
        }
    }

    @Override
    public void onNetworkFailed(AppApi.Action method) {
        switch (method) {
            case CP_POST_SDCARD_STATE_JSON:
                LogUtils.d("上报SD卡状态失败，网络异常");
                break;
        }
    }
}

package com.savor.ads.core;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.savor.ads.BuildConfig;
import com.savor.ads.bean.AtvProgramInfo;
import com.savor.ads.bean.JsonBean;
import com.savor.ads.bean.DownloadDetailRequestBean;
import com.savor.ads.bean.PlaylistDetailRequestBean;
import com.savor.ads.bean.RtbRequest;
import com.savor.ads.bean.ServerInfo;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.tvlibrary.AtvChannel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.helpers.ParserAdapter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tianshu.ui.api.TsUiApiV20171122;
import tianshu.ui.api.ZmtAPI;

public class AppApi {

    /**
     * 小平台地址（默认值是一个假数据，获取到真实的小平台地址后会被重置）
     */
    public static String SP_BASE_URL = "http://192.168.1.2/";

    private static String PHONE_BASE_URL = "http://192.168.0.1:8080/";

    public static String WLAN_BASE_URL = "";

    /**MeiSSP平台广告**/
    public static final String MEI_SSP_ADS_URL = "http://meiadx.meichuanmei.com/ps/std_json";
    /**奥凌广告平台**/
    public static final String OOHLING_GET_ADS_URL="http://www.oohlink.com/website/play/getAd";
    public static final String OOHLING_REPORT_LOG_URL="http://www.oohlink.com/website/play/reportDspPlayLog";
//    public static final String OOHLING_GET_ADS_URL="http://demo.oohlink.com/website/play/getAd";
//    public static final String OOHLING_REPORT_LOG_URL="http://demo.oohlink.com/website/play/reportDspPlayLog";
    /**
     * sdkconfig.xml配置文件服务器存放地址,如果为空的话，默认去加载本地assets目录
     */
    public static final String CONFIG_URL = BuildConfig.BASE_URL+"/Public/admaster/admaster_sdkconfig.xml";

    public static void resetSmallPlatformInterface(Context context) {
        ServerInfo serverInfo = Session.get(context).getServerInfo();
        if (serverInfo != null) {
            for(Action action:API_URLS.keySet()){
                if (action.name().startsWith("SP_")){
                    String url = API_URLS.get(action);
                    url = url.replace(SP_BASE_URL,serverInfo.getDownloadUrl());
                    API_URLS.put(action,url);
                }
            }
            SP_BASE_URL = serverInfo.getDownloadUrl();
        }
    }

    public static void resetPhoneInterface(String newIP) {
        for (Action action : API_URLS.keySet()) {
            if (action.name().startsWith("PH_")) {
                String url = API_URLS.get(action);
                url = url.replace(PHONE_BASE_URL, "http://"+newIP+":8080/");
                API_URLS.put(action, url);
            }
        }
        PHONE_BASE_URL = "http://"+newIP+":8080/";
    }

    public static void resetWLANBaseUrl(String baseIP){
        WLAN_BASE_URL = "http://"+baseIP+":8080/";
    }

    /**
     * Action-自定义行为 注意：自定义后缀必须为以下结束 _FORM:该请求是Form表单请求方式 _JSON:该请求是Json字符串
     * _XML:该请求是XML请求描述文件
     * CP_前缀标识云平台接口；SP_前缀标识小平台接口；PH_前缀标识移动端接口
     */
    public static enum Action {
        CP_GET_BIRTHDAY_ONDEMADN_FROM_JSON,
        CP_POST_FORSCREEN_ADSLIST_FROM_JSON,
        CP_GET_GOODS_PROGRAMLIST_FROM_JSON,
        CP_GET_SHOP_GOODS_LIST_FROM_JSON,
        CP_GET_HOT_CONTENT_FROM_JSON,
        CP_GET_WELCOMERESOURCE_FROM_JSON,
        SP_GET_PROGRAM_DATA_FROM_JSON,
        SP_GET_ADV_DATA_FROM_JSON,
        SP_GET_ADS_DATA_FROM_JSON,
        CP_GET_LIFE_ADS_DATA_FROM_JSON,
        SP_GET_TV_MATCH_DATA_FROM_JSON,
        SP_GET_TV_MATCH_DATA_FROM_GIEC_JSON,
        SP_GET_UPGRADE_INFO_JSON,
        SP_GET_UPGRADEDOWN,
        CP_GET_HEARTBEAT_PLAIN,
        SP_POST_UPLOAD_PROGRAM_JSON,
        SP_POST_UPLOAD_PROGRAM_GIEC_JSON,
        CP_GET_SP_IP_JSON,
        SP_GET_BOX_INIT_JSON,
        CP_GET_PRIZE_JSON,
        CP_REPORT_LOTTERY_JSON,
        PH_NOTIFY_STOP_JSON,
        CP_POST_DEVICE_TOKEN_JSON,
        SP_GET_POLY_ADS_JSON,
        CP_POST_POLY_PLAY_RECORD_JSON,
        SP_POST_NETSTAT_JSON,
        CP_POST_PLAY_LIST_JSON,
        CP_POST_DOWNLOAD_LIST_JSON,
        CP_POST_SDCARD_STATE_JSON,
        CP_POST_SHELL_COMMAND_RESULT_JSON,
        AD_BAIDU_ADS,
        AD_ZMENG_ADS,
        AD_MEI_VIDEO_ADS_JSON,
        AD_MEI_IMAGE_ADS_JSON,
        AD_POST_OOHLINK_ADS_JSON,
        AD_POST_OOHLINK_REPORT_LOG_JSON,
        AD_POST_JDMOMEDIA_ADS_PLAIN,
        AD_POST_JDMOMEDIA_HEARTBEAT_PLAIN,
        AD_POST_YISHOU_JSON,
        CP_GET_NOTICE_ADS_MONITOR_JSON,
        CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON,
        CP_SIMPLE_MINIPROGRAM_DOWNLOAD_QRCODE_JSON,
        CP_POST_FORSCREEN_GETCONFIG_JSON,
        CP_GET_UPLOAD_LOG_FILE_JSON,
        CP_GET_MINIPROGRAM_PROJECTION_RESOURCE_JSON,
        CP_GET_MINIPROGRAM_SIMPLE_PROJECTION_RESOURCE_JSON,
        CP_GET_MINIPROGRAM_PROJECTION_NETTYTIME_JSON,
        CP_POST_MINIPROGRAM_PROJECTION_GAME_JSON,
        CP_POST_MINIPROGRAM_ICON_SHOW_LOG_JSON,
        CP_POST_SIMPLE_MINIPROGRAM_FORSCREEN_LOG_JSON,
        CP_POST_UPDATE_SIMPLE_FORSCREEN_LOG_JSON,
        SP_GET_QR_SMALL_JSON,
        SP_GET_QR_BIG_JSON,
        SP_GET_QR_CALL_JSON,
        SP_GET_QR_SIMPLE_SMALL_JSON,
        SP_GET_QR_SIMPLE_BIG_JSON,
        SP_GET_QR_SIMPLE_CALL_JSON,
        SP_GET_QR_NETWORK_JSON,
        CP_GET_NETTY_BALANCING_FORM,
        CP_POST_LOGOUT_GAME_H5_JSON,
        CP_GET_BOX_TPMEDIAS_JSON,
        CP_GET_ADDPLAYLOG_JSON,
        CP_GET_GOODSCOUNTDOWN_JSON,
        CP_POST_FORSCREEN_ADSLOG_JSON,
        CP_POST_WELCOME_PLAYLOG_JSON,
        CP_GET_TEST_WECHAT_JSON,
        WLAN_GET_PROGRAM_GUIDES_JSON

    }


    /**
     * URL集合
     */
    public static HashMap<Action, String> API_URLS = new HashMap<Action, String>() {
        private static final long serialVersionUID = -8469661978245513712L;

        {
            put(Action.CP_GET_BIRTHDAY_ONDEMADN_FROM_JSON,BuildConfig.BASE_URL+"Smallappsimple/birthdaydemand/demandList");
            put(Action.CP_POST_FORSCREEN_ADSLIST_FROM_JSON,BuildConfig.BASE_URL+"Box/ForscreenAds/getAdsList");
            put(Action.CP_GET_GOODS_PROGRAMLIST_FROM_JSON,BuildConfig.BASE_URL+"box/program/getGoodsProgramList");
            put(Action.CP_GET_SHOP_GOODS_LIST_FROM_JSON,BuildConfig.BASE_URL+"box/program/getShopgoodsProgramList");
            put(Action.CP_GET_HOT_CONTENT_FROM_JSON,BuildConfig.BASE_URL+"box/program/getHotPlayProgramList");
            put(Action.CP_GET_WELCOMERESOURCE_FROM_JSON,BuildConfig.BASE_URL+"box/program/getWelcomeResource");
            put(Action.SP_GET_PROGRAM_DATA_FROM_JSON,SP_BASE_URL+"small/api/download/vod/config/v2");
            put(Action.SP_GET_ADV_DATA_FROM_JSON,SP_BASE_URL+"small/api/download/adv/config");
            put(Action.SP_GET_ADS_DATA_FROM_JSON,SP_BASE_URL+"small/api/download/ads/config");
            put(Action.CP_GET_LIFE_ADS_DATA_FROM_JSON,BuildConfig.BASE_URL+"box/lifeAds/getAdsList");
            put(Action.SP_GET_TV_MATCH_DATA_FROM_JSON,SP_BASE_URL+"small/tvList/api/stb/tv_getCommands");
            put(Action.SP_GET_TV_MATCH_DATA_FROM_GIEC_JSON,SP_BASE_URL+"small/tvListNew/api/stb/tv_getCommands");
            put(Action.SP_GET_UPGRADE_INFO_JSON,SP_BASE_URL+"small/api/download/apk/config");
            put(Action.CP_GET_HEARTBEAT_PLAIN, BuildConfig.BASE_URL + "Heartbeat/Report/index");
            put(Action.SP_POST_UPLOAD_PROGRAM_JSON, SP_BASE_URL + "small/tvList/api/stb/tv_commands");
            put(Action.SP_POST_UPLOAD_PROGRAM_GIEC_JSON, SP_BASE_URL + "small/tvListNew/api/stb/tv_commands");
            put(Action.CP_GET_SP_IP_JSON, BuildConfig.BASE_URL + "basedata/ipinfo/getIp");
            put(Action.SP_GET_BOX_INIT_JSON, SP_BASE_URL + "small/api/download/init");
            put(Action.CP_GET_PRIZE_JSON, BuildConfig.BASE_URL + "Award/Award/getAwardInfo");
            put(Action.CP_REPORT_LOTTERY_JSON, BuildConfig.BASE_URL + "Award/Award/recordAwardLog");
            put(Action.PH_NOTIFY_STOP_JSON, PHONE_BASE_URL + "stopProjection");
            put(Action.CP_POST_DEVICE_TOKEN_JSON, BuildConfig.BASE_URL + "Basedata/Box/reportDeviceToken");
            put(Action.SP_GET_POLY_ADS_JSON, SP_BASE_URL + "small/api/download/poly/config");
            put(Action.CP_POST_POLY_PLAY_RECORD_JSON,BuildConfig.BASE_URL +"Box/BaiduPoly/recordPlay");
            put(Action.SP_POST_NETSTAT_JSON, BuildConfig.BASE_URL + "Small/NetReport/reportInfo");
            put(Action.CP_POST_PLAY_LIST_JSON, BuildConfig.BASE_URL + "box/Program/reportPlayInfo");
            put(Action.CP_POST_DOWNLOAD_LIST_JSON, BuildConfig.BASE_URL + "box/Program/reportDownloadInfo");
            put(Action.CP_POST_SDCARD_STATE_JSON, BuildConfig.BASE_URL + "Opclient20/BoxMem/boxMemoryInfo");
            put(Action.CP_POST_SHELL_COMMAND_RESULT_JSON,BuildConfig.BASE_URL+"Box/ShellCallback/pushResult");
            put(Action.AD_BAIDU_ADS, BuildConfig.BAIDU_AD_BASE_URL);
            put(Action.AD_ZMENG_ADS, BuildConfig.ZMENG_AD_BASE_URL);
            put(Action.AD_MEI_VIDEO_ADS_JSON,MEI_SSP_ADS_URL);
            put(Action.AD_MEI_IMAGE_ADS_JSON,MEI_SSP_ADS_URL);
            put(Action.AD_POST_OOHLINK_ADS_JSON,OOHLING_GET_ADS_URL);
            put(Action.AD_POST_OOHLINK_REPORT_LOG_JSON,OOHLING_REPORT_LOG_URL);
            put(Action.AD_POST_JDMOMEDIA_ADS_PLAIN, BuildConfig.JDMOMEDIA_AD_BASE_URL+"ad/request?version=v1");
            put(Action.AD_POST_JDMOMEDIA_HEARTBEAT_PLAIN, BuildConfig.JDMOMEDIA_AD_BASE_URL+"screen/heartbeat");
            put(Action.CP_GET_NOTICE_ADS_MONITOR_JSON,"");
            put(Action.AD_POST_YISHOU_JSON,BuildConfig.YISHOU_AD_BASE_URL);
            put(Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON,BuildConfig.BASE_URL+"Smallapp21/index/getBoxQr");
            put(Action.CP_SIMPLE_MINIPROGRAM_DOWNLOAD_QRCODE_JSON,BuildConfig.BASE_URL+"Smallappsimple/index/getBoxQr");
            put(Action.CP_POST_FORSCREEN_GETCONFIG_JSON,BuildConfig.BASE_URL+"Box/Forscreen/getConfig");
            put(Action.CP_GET_UPLOAD_LOG_FILE_JSON,BuildConfig.BASE_URL+"box/BoxLog/isUploadLog");
            put(Action.CP_GET_MINIPROGRAM_PROJECTION_RESOURCE_JSON,BuildConfig.BASE_URL+"box/buriedPoint/boxNetLogs");
            put(Action.CP_GET_MINIPROGRAM_SIMPLE_PROJECTION_RESOURCE_JSON,BuildConfig.BASE_URL+"smallappsimple/forscreenLog/updateForscreenPlaytime");
            put(Action.CP_GET_MINIPROGRAM_PROJECTION_NETTYTIME_JSON,BuildConfig.BASE_URL+"box/buriedPoint/boxReceiveNetty");
            put(Action.CP_POST_MINIPROGRAM_PROJECTION_GAME_JSON,BuildConfig.BASE_URL+"Smallapp/BuriedPoint/activity");
            put(Action.CP_POST_MINIPROGRAM_ICON_SHOW_LOG_JSON,BuildConfig.BASE_URL+"Smallapp/BuriedPoint/sunCodeLog");
            put(Action.CP_POST_SIMPLE_MINIPROGRAM_FORSCREEN_LOG_JSON,BuildConfig.BASE_URL+"/Smallappsimple/ForscreenLog/recordForScreen");
            put(Action.CP_POST_UPDATE_SIMPLE_FORSCREEN_LOG_JSON,BuildConfig.BASE_URL+"Smallappsimple/ForscreenLog/updateForscreen");
            put(Action.CP_GET_NETTY_BALANCING_FORM,BuildConfig.BALANCING_NETTY_BASE+"/netty/balancing");
            put(Action.CP_POST_LOGOUT_GAME_H5_JSON,BuildConfig.BASE_URL+"Games/ClimbTree/logoutGameH5");
            put(Action.CP_GET_BOX_TPMEDIAS_JSON,BuildConfig.BASE_URL+"/Box/BaiduPoly/getBoxTpmedias");
            put(Action.CP_GET_ADDPLAYLOG_JSON,BuildConfig.BASE_URL+"box/forscreen/addPlaylog");
            put(Action.CP_GET_GOODSCOUNTDOWN_JSON,BuildConfig.BASE_URL+"box/program/getGoodsCountdown");
            put(Action.CP_POST_FORSCREEN_ADSLOG_JSON,BuildConfig.BASE_URL+"box/boxLog/adsPlaylog");
            put(Action.CP_POST_WELCOME_PLAYLOG_JSON,BuildConfig.BASE_URL+"box/boxLog/welcomePlaylog");
            put(Action.WLAN_GET_PROGRAM_GUIDES_JSON,WLAN_BASE_URL+"WLAN/getProAdvListData");

        }
    };

    /**
     * 获取盒子初始化信息
     * @param context
     * @param handler
     * @param boxMac
     */
    public static JsonBean getBoxInitInfo(Context context, ApiRequestListener handler, String boxMac) throws IOException {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("boxMac",boxMac);
        return new AppServiceOk(context, Action.SP_GET_BOX_INIT_JSON, handler, params).syncGet();
    }

    /**
     * 获取生日相关视频
     * @param context
     * @param handler
     * @param boxMac
     * @throws IOException
     */
    public static JsonBean getBirthdayOndemandFromCloudPlatform(Context context, ApiRequestListener handler, String boxMac) throws IOException{
        final HashMap<String, Object> params = new HashMap<>();
        params.put("boxMac",boxMac);
        return new AppServiceOk(context, Action.CP_GET_BIRTHDAY_ONDEMADN_FROM_JSON, handler, params).syncGet();
    }

    /**
     * 获取互动短广告
     * @param context
     * @param handler
     * @param boxMac
     * @return
     * @throws IOException
     */
    public static JsonBean postInteractionAdsFromCloudform(Context context, ApiRequestListener handler, String boxMac) throws IOException{
        final HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac",boxMac);
        return new AppServiceOk(context, Action.CP_POST_FORSCREEN_ADSLIST_FROM_JSON, handler, params).syncGet();
    }

    /**
     * 获取优选活动商品数据(优选，公司活动，店内活动)
     * @param context
     * @param handler
     * @param boxMac
     * @return
     * @throws IOException
     */
    public static JsonBean getGoodsProgramListFromCloudfrom(Context context,ApiRequestListener handler,String boxMac) throws IOException{
        final HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac",boxMac);
        return new AppServiceOk(context, Action.CP_GET_GOODS_PROGRAMLIST_FROM_JSON, handler, params).syncGet();
    }

    /**
     * GET
     * 获取商城商品节目单列表
     * @param context
     * @param handler
     * @param boxMac
     * @return
     * @throws IOException
     */
    public static JsonBean getShopGoodsListFromCloudfrom(Context context,ApiRequestListener handler,String boxMac) throws IOException{
        final HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac",boxMac);
        return new AppServiceOk(context, Action.CP_GET_SHOP_GOODS_LIST_FROM_JSON, handler, params).syncGet();
    }

    /**
     * 获取小程序互动首页热播内容
     * @param context
     * @param handler
     * @param boxMac
     * @return
     * @throws IOException
     */
    public static JsonBean getHotContentFromCloudfrom(Context context,ApiRequestListener handler,String boxMac) throws IOException{
        final HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac",boxMac);
        return new AppServiceOk(context,Action.CP_GET_HOT_CONTENT_FROM_JSON,handler,params).syncGet();
    }

    /**
     * 获取欢迎词相关资源
     * @param context
     * @param handler
     * @param boxMac
     * @return
     * @throws IOException
     */
    public static JsonBean getWelcomeResourceFromCloudfrom(Context context,ApiRequestListener handler,String boxMac) throws IOException{
        final HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac",boxMac);
        return new AppServiceOk(context,Action.CP_GET_WELCOMERESOURCE_FROM_JSON,handler,params).syncGet();
    }

    /**
     * 处理小平台返回的节目数据
     * @param context
     * @param handler
     * @param boxMac
     * @return
     * @throws IOException
     */
    public static JsonBean getProgramDataFromSmallPlatform(Context context, ApiRequestListener handler, String boxMac) throws IOException {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("boxMac",boxMac);
        return new AppServiceOk(context, Action.SP_GET_PROGRAM_DATA_FROM_JSON, handler, params).syncGet();

    }

    /**
     * 获取小平台宣传片文件
     * @param context
     * @param handler
     * @param boxMac
     * @return
     * @throws IOException
     */
    public static JsonBean getAdvDataFromSmallPlatform(Context context, ApiRequestListener handler,String boxMac) throws IOException {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("boxMac",boxMac);
        return new AppServiceOk(context, Action.SP_GET_ADV_DATA_FROM_JSON, handler, params).syncGet();

    }

    /**
     *获取小平台广告列表
     * @param context
     * @param handler
     * @param boxMac
     * @return
     * @throws IOException
     */
    public static JsonBean getAdsDataFromSmallPlatform(Context context, ApiRequestListener handler,String boxMac) throws IOException{
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("boxMac",boxMac);
        return new AppServiceOk(context, Action.SP_GET_ADS_DATA_FROM_JSON, handler, params).syncGet();
    }

    /**
     * 获取本地生活广告数据
     * @param context
     * @param handler
     * @param boxMac
     * @return
     * @throws IOException
     */
    public static JsonBean getLifeAdsListFromCloudfrom(Context context,ApiRequestListener handler,String boxMac) throws IOException{
        final HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac",boxMac);
        return new AppServiceOk(context,Action.CP_GET_LIFE_ADS_DATA_FROM_JSON,handler,params).syncGet();
    }

    /**
     * 获取百度聚屏广告资源
     * @param context
     * @param handler
     * @param boxMac
     * @return
     * @throws IOException
     */
    public static JsonBean getPolyAdsFromSmallPlatform(Context context, ApiRequestListener handler,String boxMac) throws IOException {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("boxMac",boxMac);
        return new AppServiceOk(context, Action.SP_GET_POLY_ADS_JSON, handler, params).syncGet();
    }

    /**
     * dsp广告平台数据统计
     * @param context
     * @param handler
     * @param media_id
     * @param media_md5
     * @param media_name
     * @param tpmedia_id
     */

    public static void postPolyPlayRecord(Context context, ApiRequestListener handler,String media_id,String media_md5,String media_name,String chinese_name,String tpmedia_id){
        final HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac",Session.get(context).getEthernetMac());
        params.put("media_id",media_id);
        params.put("media_md5",media_md5);
        params.put("media_name",media_name);
        params.put("chinese_name",chinese_name);
        params.put("tpmedia_id",tpmedia_id);

        new AppServiceOk(context, Action.CP_POST_POLY_PLAY_RECORD_JSON, handler, params).post();
    }

    /**
     * 获取小平台电视频道数据
     * @param context
     * @param handler
     */
    public static void getTVMatchDataFromSmallPlatform(Context context, ApiRequestListener handler) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        new AppServiceOk(context, Action.SP_GET_TV_MATCH_DATA_FROM_JSON, handler, params).get();
    }

    /**
     * 获取小平台电视频道数据
     * @param context
     * @param handler
     */
    public static void getGiecTVMatchDataFromSmallPlatform(Context context, ApiRequestListener handler) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        new AppServiceOk(context, Action.SP_GET_TV_MATCH_DATA_FROM_GIEC_JSON, handler, params).get();
    }

    /**
     *升级接口
     * @param context
     * @param handler
     */
    public static JsonBean upgradeInfo(Context context, ApiRequestListener handler,int versionCode) throws IOException{
        final HashMap<String, Object> params = new HashMap<>();
        params.put("versionCode",versionCode);
        return new AppServiceOk(context, Action.SP_GET_UPGRADE_INFO_JSON, handler, params).syncGet();
    }

    /**
     下载小程序码二维码小
     * @param url
     * @param context
     * @param handler
     * @param filePath
     */
    public static void downloadQRSmallImg(String url,Context context, ApiRequestListener handler,String filePath){
        final HashMap<String, Object> params = new HashMap<>();
        new AppServiceOk(context, Action.SP_GET_QR_SMALL_JSON, handler, params).downLoad(url, filePath);
    }

    /**
     * 下载小程序码二维码大
     * @param url
     * @param context
     * @param handler
     * @param filePath
     */
    public static void downloadQRBigImg(String url,Context context, ApiRequestListener handler,String filePath){
        final HashMap<String, Object> params = new HashMap<>();
        new AppServiceOk(context, Action.SP_GET_QR_BIG_JSON, handler, params).downLoad(url, filePath);
    }

    /**
     * 下载小程序码二维码call
     * @param url
     * @param context
     * @param handler
     * @param filePath
     */
    public static void downloadQRCallImg(String url,Context context, ApiRequestListener handler,String filePath){
        final HashMap<String, Object> params = new HashMap<>();
        new AppServiceOk(context, Action.SP_GET_QR_CALL_JSON, handler, params).downLoad(url, filePath);
    }

    /**
     * 下载极简小程序二维码小
     * @param url
     * @param context
     * @param handler
     * @param filePath
     */
    public static void downloadQRSimpleSmallImg(String url,Context context, ApiRequestListener handler,String filePath){
        final HashMap<String, Object> params = new HashMap<>();
        new AppServiceOk(context, Action.SP_GET_QR_SIMPLE_SMALL_JSON, handler, params).downLoad(url, filePath);
    }
    /**
     * 下载极简小程序二维码大
     * @param url
     * @param context
     * @param handler
     * @param filePath
     */
    public static void downloadQRSimpleBigImg(String url,Context context, ApiRequestListener handler,String filePath){
        final HashMap<String, Object> params = new HashMap<>();
        new AppServiceOk(context, Action.SP_GET_QR_SIMPLE_BIG_JSON, handler, params).downLoad(url, filePath);
    }
    /**
     * 下载极简小程序二维码call
     * @param url
     * @param context
     * @param handler
     * @param filePath
     */
    public static void downloadQRSimpleCallImg(String url,Context context, ApiRequestListener handler,String filePath){
        final HashMap<String, Object> params = new HashMap<>();
        new AppServiceOk(context, Action.SP_GET_QR_SIMPLE_CALL_JSON, handler, params).downLoad(url, filePath);
    }

    /**
     * 下载扫码上网二维码
     * @param url
     * @param context
     * @param handler
     * @param filePath
     */
    public static void downloadQRCodeNetworkImg(String url,Context context, ApiRequestListener handler,String filePath){
        final HashMap<String, Object> params = new HashMap<>();
        new AppServiceOk(context, Action.SP_GET_QR_NETWORK_JSON, handler, params).downLoad(url, filePath);
    }

    /**
     * 下载文件
     * @param type 1是ROM2是apk
     * @param context
     * @param handler
     */
    public static void downVersion(String url,Context context, ApiRequestListener handler,int type){
        try{
            String target= AppUtils.getMainMediaPath();//AppUtils.getSDCardPath();
            if (TextUtils.isEmpty(target)) {
                LogFileUtil.write("External SD is not exist, download canceled");
                return;
            }

            String targetApk;
            if (type==1){
                targetApk=target + File.separator + ConstantValues.ROM_DOWNLOAD_FILENAME;
            }else{
                targetApk=target + File.separator + ConstantValues.APK_DOWNLOAD_FILENAME;
            }

            File tarFile =new File(targetApk);
            if(tarFile.exists()){
                tarFile.delete();
            }
            final HashMap<String, Object> params = new HashMap<>();
            new AppServiceOk(context, Action.SP_GET_UPGRADEDOWN, handler, params).downLoad(url, targetApk);
        }catch(Exception ex){
//            UpdateUtil.APK_DOWNLOADING = false;
            LogUtils.d(ex.toString());
        }
    }

    /**
     * 心跳接口
     * @param context
     * @param handler
     */
    public static void heartbeat(Context context, ApiRequestListener handler,int serial_no) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("clientid", 2);
        params.put("mac", Session.get(context).getEthernetMac());
        params.put("pro_period", Session.get(context).getProPeriod());
        params.put("adv_period", Session.get(context).getAdvPeriod());
        params.put("period", Session.get(context).getAdsPeriod());
        params.put("pro_download_period", Session.get(context).getProDownloadPeriod());
        params.put("adv_download_period", Session.get(context).getAdvDownloadPeriod());
        params.put("ads_download_period", Session.get(context).getAdsDownloadPeriod());
        params.put("demand", Session.get(context).getBirthdayOndemandPeriod());
        params.put("vod_download_period", Session.get(context).getBirthdayOndemandDownloadPeriod());
        params.put("rtb_ads_period", Session.get(context).getRtbadsPeriod());
        params.put("apk", Session.get(context).getVersionName());
        params.put("apk_time", Session.get(context).getVersionCode());
        params.put("war", "");
        params.put("serial_no", serial_no);
        params.put("is_normaluse_wechat", Session.get(context).getNormalUseWechat());
        params.put("logo", Session.get(context).getSplashVersion());
        params.put("p_load_version", Session.get(context).getLoadingVersion());
        params.put("ip", AppUtils.getLocalIPAddress());
        params.put("hotelId", Session.get(context).getBoiteId());
        params.put("roomId", Session.get(context).getRoomId());
        params.put("signal", AppUtils.getInputType(Session.get(context).getTvInputSource()));
        params.put("net_speed",Session.get(context).getNetSpeed());
        new AppServiceOk(context, Action.CP_GET_HEARTBEAT_PLAIN, handler, params).get();
    }

    /**
     * 获取小平台IP
     * @param context
     * @param handler
     */
    public static void getSpIp(Context context, ApiRequestListener handler) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        new AppServiceOk(context, Action.CP_GET_SP_IP_JSON, handler, params).get();
    }

    public static void uploadProgram(Context context, ApiRequestListener handler, AtvProgramInfo[] programs) {
        if (programs == null || programs.length <= 0)
            return;
        List<AtvProgramInfo> programInfo = Arrays.asList(programs);
        final HashMap<String, Object> params = new HashMap<>();
        params.put("data", programInfo);
        new AppServiceOk(context, Action.SP_POST_UPLOAD_PROGRAM_JSON, handler, params).post();
    }

    public static void uploadProgram(Context context, ApiRequestListener handler, ArrayList<AtvChannel> programs) {
        if (programs == null || programs.size() <= 0)
            return;
        final HashMap<String, Object> params = new HashMap<>();
        params.put("data", programs);
        new AppServiceOk(context, Action.SP_POST_UPLOAD_PROGRAM_GIEC_JSON, handler, params).post();
    }

    /**
     * 获取奖项设置
     * @param context
     * @param handler
     */
    public static void getPrize(Context context, ApiRequestListener handler) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("mac", Session.get(context).getEthernetMac());
        new AppServiceOk(context, Action.CP_GET_PRIZE_JSON, handler, params).post();
    }

    /**
     * 上报抽奖信息
     * @param context
     * @param handler
     */
    public static void reportLottery(Context context, ApiRequestListener handler) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("mac", Session.get(context).getEthernetMac());
        params.put("date", AppUtils.getCurTime("yyyy-MM-dd"));
        new AppServiceOk(context, Action.CP_REPORT_LOTTERY_JSON, handler, params).post();
    }

    /**
     * 上报推送DeviceToken
     * @param context
     * @param handler
     */
    public static void reportDeviceToken(Context context, ApiRequestListener handler, String deviceToken) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("box_id", Session.get(context).getBoxId());
        params.put("box_mac", Session.get(context).getEthernetMac());
        params.put("device_token", deviceToken);
        new AppServiceOk(context, Action.CP_POST_DEVICE_TOKEN_JSON, handler, params).post();
    }

    /**
     * 通知手机投屏结束
     * @param context
     * @param handler
     */
    public static void notifyStop(Context context, ApiRequestListener handler, int type, String msg) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("type", type);
        params.put("tipMsg", msg);
        new AppServiceOk(context, Action.PH_NOTIFY_STOP_JSON, handler, params).get();
    }

    /**
     * 上传网络状况
     * @param context
     * @param handler
     * @param intranetLatency 内网延时时间ms
     * @param internetLatency 外网延时时间ms
     */
    public static void postNetstat(Context context, ApiRequestListener handler, String intranetLatency, String internetLatency){
        final HashMap<String, Object> params = new HashMap<>();
        params.put("hotel_id", Session.get(context).getBoiteId());
        params.put("box_id", Session.get(context).getBoxId());
        params.put("boxMac", Session.get(context).getEthernetMac());
        params.put("inn_delay", intranetLatency);
        params.put("out_delay", internetLatency);
        new AppServiceOk(context, Action.SP_POST_NETSTAT_JSON, handler, params).get();
    }

    /**
     * 上报当前播放列表
     * @param context
     * @param handler
     * @param detail    明细数据
     */
    public static void reportPlaylist(Context context, ApiRequestListener handler, PlaylistDetailRequestBean detail) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("box_mac", Session.get(context).getEthernetMac());
        params.put("resource_info", detail);
        new AppServiceOk(context, Action.CP_POST_PLAY_LIST_JSON, handler, params).post();
    }

    /**
     * 上报下载列表
     * @param context
     * @param handler
     * @param type      1广告；2节目；3宣传片
     * @param detail    明细数据
     */
    public static void reportDownloadList(Context context, ApiRequestListener handler, int type, DownloadDetailRequestBean detail) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("box_mac", Session.get(context).getEthernetMac());
        params.put("type", type);
        params.put("resource_info", detail);
        new AppServiceOk(context, Action.CP_POST_DOWNLOAD_LIST_JSON, handler, params).post();
    }

    /**
     * 上报SD卡异常
     * @param context
     * @param handler
     * @param type      1内存卡损坏；2内存卡已满
     */
    public static void reportSDCardState(Context context, ApiRequestListener handler, int type) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("box_id", Session.get(context).getBoxId());
        params.put("box_mac", Session.get(context).getEthernetMac());
        params.put("type", type);
        new AppServiceOk(context, Action.CP_POST_SDCARD_STATE_JSON, handler, params).post();
    }

    /**
     * 执行shell命令后返回的结果
     * @param context
     * @param handler
     * @param json 返回的json数据
     */
    public static void postShellCommandResult(Context context, ApiRequestListener handler, JSONArray json){
        final HashMap<String,Object> params = new HashMap<>();
        params.put("box_mac",Session.get(context).getEthernetMac());
        params.put("data",json);
        new AppServiceOk(context,Action.CP_POST_SHELL_COMMAND_RESULT_JSON,handler,params).post();

    }

    /**
     * 请求百度聚屏广告
     * @param context
     * @param handler
     * @param requestBean
     */
    public static void requestBaiduAds(Context context, ApiRequestListener handler, TsUiApiV20171122.TsApiRequest requestBean) {
//        new AppServiceOk(context, Action.AD_BAIDU_ADS, handler).postProto(requestBean);
    }

    /**
     * 请求taimei视频广告
     * @param context
     * @param handler
     * @param params
     */
    public static void requestMeiVideoAds(Context context,ApiRequestListener handler,HashMap<String,Object> params){

        new AppServiceOk(context,Action.AD_MEI_VIDEO_ADS_JSON,handler,params).post();
    }

    /**
     * 请求taimei图片广告
     * @param context
     * @param handler
     * @param params
     */
    public static void requestMeiImageAds(Context context,ApiRequestListener handler,HashMap<String,Object> params){

        new AppServiceOk(context,Action.AD_MEI_IMAGE_ADS_JSON,handler,params).post();
    }

    /**
     * 调用Mei平台聚屏广告曝光地址
     * @param context
     * @param handler
     */
    public static void getNoticeAdsMonitor(Context context,ApiRequestListener handler,String url){
        API_URLS.put(Action.CP_GET_NOTICE_ADS_MONITOR_JSON,url);
        new AppServiceOk(context,Action.CP_GET_NOTICE_ADS_MONITOR_JSON,handler).get();
    }
    /**
     * 请求初始化接口返回结果含:
     * 1.是否展示小程序码
     * 2.是否展示极简码
     * 3.是否支持互动投屏广告
     * 4.播放互动投屏头尾广告间隔次数
     * @param context
     * @param handler
     */
    public static void getScreenInitConfig(Context context, ApiRequestListener handler){
        final HashMap<String,Object> params = new HashMap<>();
        params.put("box_mac",Session.get(context).getEthernetMac());
        params.put("versionCode",Session.get(context).getVersionCode());
        new AppServiceOk(context,Action.CP_POST_FORSCREEN_GETCONFIG_JSON,handler,params).post();
    }

    /**
     * 上传机顶盒日志
     * @param context
     * @param handler
     */
    public static void getUploadLogFileType(Context context, ApiRequestListener handler){
        final HashMap<String,Object> params = new HashMap<>();
        params.put("box_mac",Session.get(context).getEthernetMac());
        new AppServiceOk(context,Action.CP_GET_UPLOAD_LOG_FILE_JSON,handler,params).get();
    }

    /**
     * 上报小程序投屏日志参数
     * @param context
     * @param handler
     * @param params
     */
    public static void postProjectionResourceParam(Context context,ApiRequestListener handler,HashMap<String,Object> params){
        new AppServiceOk(context,Action.CP_GET_MINIPROGRAM_PROJECTION_RESOURCE_JSON,handler,params).get();
    }

    /**
     * 上报小程序极简版投屏开始结束日志
     * @param context
     * @param handler
     * @param params
     */
    public static void postSimpleProjectionResourceParam(Context context,ApiRequestListener handler,HashMap<String,Object> params){
        new AppServiceOk(context,Action.CP_GET_MINIPROGRAM_SIMPLE_PROJECTION_RESOURCE_JSON,handler,params).get();
    }

    /**
     * 获取受到NETTY请求时间
     * @param context
     * @param handler
     * @return
     * @throws IOException
     */
    public static JsonBean getProjectionNettyTime(Context context,ApiRequestListener handler,String req_id,String box_downstime) throws IOException{
        HashMap<String,Object> params = new HashMap<>();
        params.put("req_id",req_id);
        params.put("box_downstime",box_downstime);
        return new AppServiceOk(context,Action.CP_GET_MINIPROGRAM_PROJECTION_NETTYTIME_JSON,handler,params).syncGet();
    }
    /**
     * 上报小程序互动游戏参数
     * @param context
     * @param handler
     * @param params
     */
    public static void postProjectionGamesParam(Context context,ApiRequestListener handler,HashMap<String,Object> params){
        new AppServiceOk(context,Action.CP_POST_MINIPROGRAM_PROJECTION_GAME_JSON,handler,params).post();
    }

    /**
     * 上报小程序码的显示隐藏日志
     * @param context
     * @param handler
     * @param params
     */
    public static void postMiniProgramIconShowLog(Context context,ApiRequestListener handler,HashMap<String,Object> params){
        new AppServiceOk(context,Action.CP_POST_MINIPROGRAM_ICON_SHOW_LOG_JSON,handler,params).post();
    }

    /**
     * 上传极简版投屏日志
     * @param context
     * @param handler
     * @param params
     */
    public static void postSimpleMiniProgramProjectionLog(Context context,ApiRequestListener handler,HashMap<String,Object> params,String forscreen_id){
        new AppServiceOk(context,Action.CP_POST_SIMPLE_MINIPROGRAM_FORSCREEN_LOG_JSON,handler,params,forscreen_id).post();
    }

    /**
     * 更新极简版投屏日志,上传投屏文件操作
     * @param context
     * @param handler
     * @param params
     * @param resource_id
     */
    public static void updateSimpleProjectionLog(Context context,ApiRequestListener handler,HashMap<String,Object> params,String resource_id){
        new AppServiceOk(context,Action.CP_POST_UPDATE_SIMPLE_FORSCREEN_LOG_JSON,handler,params,resource_id).post();

    }

    /**
     * 获取netty负载均衡ip和端口
     * @param context
     * @param hanler
     * @param param
     */
    public static void getNettyBalancingInfo(Context context,ApiRequestListener hanler,HashMap<String,String> param){
        try {
            String reqid = param.get("req_id");
            new AppServiceOk(context,Action.CP_GET_NETTY_BALANCING_FORM,hanler,param,reqid).postByAsynWithForm();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     *退出
     * @param context
     * @param handler
     */
    public static void postLogoutGameH5(Context context,ApiRequestListener handler){
        final HashMap<String,Object> params = new HashMap<>();
        params.put("box_mac",Session.get(context).getEthernetMac());
        new AppServiceOk(context,Action.CP_POST_LOGOUT_GAME_H5_JSON,handler,params).post();
    }

    /**
     * 获取奥凌广告
     * @param context
     * @param handler
     * @param rtbRequest
     */
    public static void requestOOHLinkAds(Context context,ApiRequestListener handler,RtbRequest rtbRequest){
        final Gson gson = new Gson();
        final HashMap<String,String> params = new HashMap<>();
        params.put("rtbRequest",gson.toJson(rtbRequest));
        new AppServiceOk(context,Action.AD_POST_OOHLINK_ADS_JSON,handler,params).requestPostByAsynWithForm(params);
    }

    /**
     * 上报奥凌广告日志
     * @param context
     * @param handler
     * @param params
     */
    public static void reportOOHLinkAdsLog(Context context,ApiRequestListener handler,HashMap<String,String> params){
        new AppServiceOk(context,Action.AD_POST_OOHLINK_REPORT_LOG_JSON,handler,params).requestPostByAsynWithForm(params);
    }

    /**
     * 请求众盟广告
     * @param context
     * @param handler
     * @param requestBean
     */
    public static void requestZmengAds(Context context, ApiRequestListener handler, ZmtAPI.ZmAdRequest requestBean){
        new AppServiceOk(context, Action.AD_ZMENG_ADS, handler).postProto(requestBean);
    }

    /**
     *请求京东钼媒广告
     * @param context
     * @param handler
     * @param params
     */
    public static void requestJDmomediaAds(Context context, ApiRequestListener handler, JSONObject params){
        new AppServiceOk(context,Action.AD_POST_JDMOMEDIA_ADS_PLAIN,handler,params).post();
    }

    /**
     * 京东钼媒心跳上报(一天一次)
     * @param context
     * @param handler
     */
    public static void JDmomediaHeartbeat(Context context,ApiRequestListener handler){
        try {
            String timestamp = String.valueOf(System.currentTimeMillis()/1000);

//            String timestamp = date.getTime()+"";
            String sign = AppUtils.getMD5(AppUtils.getMD5(ConstantValues.JDMOMEDIA_APPID+ConstantValues.JDMOMEDIA_APPKEY)+timestamp);
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("appid",ConstantValues.JDMOMEDIA_APPID);
            jsonObject.accumulate("timestamp",timestamp);
            jsonObject.accumulate("sign",sign);
            jsonObject.accumulate("udid",AppUtils.getEthernetMacAddr());
            jsonObject.accumulate("time",timestamp);
            new AppServiceOk(context,Action.AD_POST_JDMOMEDIA_HEARTBEAT_PLAIN,handler,jsonObject).post();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 请求易售广告
     * @param context
     * @param handler
     * @param param
     */
    public static void requestYishouAds(Context context,ApiRequestListener handler,HashMap<String,String> param){

        String requestUrl = BuildConfig.YISHOU_AD_BASE_URL;
        long timeStamp = System.currentTimeMillis();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("appid",ConstantValues.YISHOU_APPPID);
        hashMap.put("appkey",ConstantValues.YISHOU_APPPKEY);
        hashMap.put("sequence",timeStamp);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("version",ConstantValues.YISHOU_API);
        hashMap.put("sign",ApiRequestFactory.getYishouSign(param,timeStamp));
        requestUrl = ApiRequestFactory.getUrlRequest(requestUrl,Action.AD_POST_YISHOU_JSON,hashMap,Session.get(context));
        API_URLS.put(Action.AD_POST_YISHOU_JSON,requestUrl);
        new AppServiceOk(context,Action.AD_POST_YISHOU_JSON,handler,param).requestPostByAsynWithForm(param);
    }

    /**
     *获取机顶盒当前支持的第三方聚屏媒体
     * @param context
     * @param handler
     */
    public static void getBoxSupportPolyAdsTpmedias(Context context,ApiRequestListener handler){
        final HashMap<String,String> params = new HashMap<>();
        params.put("box_mac",Session.get(context).getEthernetMac());
        new AppServiceOk(context,Action.CP_GET_BOX_TPMEDIAS_JSON,handler,params).get();
    }

    /**
     * 上报精选内容播放日志
     * @param context
     * @param handler
     * @param vid
     */
    public static void reportSelectContentPlayLog(Context context,ApiRequestListener handler,String vid){
        final HashMap<String,String> params = new HashMap<>();
        params.put("vid",vid);
        params.put("type","4");
        new AppServiceOk(context,Action.CP_GET_ADDPLAYLOG_JSON,handler,params).get();
    }

    /**
     * 获取倒计时时间
     * @param context
     * @param handler
     * @param goods_id
     */
    public static JsonBean getGoodsCountdownTime(Context context,ApiRequestListener handler,int goods_id) throws IOException{
        final HashMap<String,String> params = new HashMap<>();
        params.put("goods_id",goods_id+"");
        return new AppServiceOk(context,Action.CP_GET_GOODSCOUNTDOWN_JSON,handler,params).syncGet();
    }

    /**
     * 投屏前后跟的广告添加播放日志
     * @param context
     * @param handler
     * @param ads_id
     * @param box_mac
     */
    public static void postForscreenAdsLog(Context context,ApiRequestListener handler,String ads_id,String box_mac){
        final HashMap<String,String> params = new HashMap<>();
        params.put("ads_id",ads_id);
        params.put("box_mac",box_mac);
        new AppServiceOk(context,Action.CP_POST_FORSCREEN_ADSLOG_JSON, handler,params).get();
    }

    /**
     * 欢迎词投屏记录
     * @param context
     * @param handler
     * @param welcome_id
     * @param box_mac
     */
    public static void postWelcomePlayAdsLog(Context context,ApiRequestListener handler,String welcome_id,String box_mac){
        final HashMap<String,String> params = new HashMap<>();
        params.put("welcome_id",welcome_id);
        params.put("box_mac",box_mac);
        new AppServiceOk(context,Action.CP_POST_WELCOME_PLAYLOG_JSON, handler,params).get();
    }

    /**
     * 测试微信接口接口是否正常返回
     * @param context
     * @param handler
     * @param wechatUrl
     */
    public static void getTestWechat(Context context,ApiRequestListener handler,String wechatUrl){
        new AppServiceOk(context,Action.CP_GET_TEST_WECHAT_JSON, handler).simpleGet(wechatUrl);
    }

    /**
     * 获取局域网内节目单数据
     * @param context
     * @param handler
     * @param boxMac
     */
    public static JsonBean getProgramGuidesData(Context context,ApiRequestListener handler,String boxMac) throws IOException{
        final HashMap<String, Object> params = new HashMap<>();
        params.put("boxMac",boxMac);
        return new AppServiceOk(context,Action.WLAN_GET_PROGRAM_GUIDES_JSON, handler,params).syncGet();
    }

    /**
     * 从这里定义业务的错误码
     */
    public static final int HTTP_RESPONSE_STATE_SUCCESS = 10000;
    /**
     * 奥凌广告平台返回正确状态码
     */
    public static final int HTTP_RESPONSE_ADS_SUCCESS = 0;

}

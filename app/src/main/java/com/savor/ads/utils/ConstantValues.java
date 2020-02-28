package com.savor.ads.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhanghq on 2016/12/8.
 */

public class ConstantValues {

    public static final int SERVER_REQUEST_PORT = 8080;
    /** 手机端操作响应码*/
    /** 失败*/
    public static final int SERVER_RESPONSE_CODE_FAILED = -1;
    /**box_mac非本机顶盒mac*/
    public static final int SERVER_RESPONSE_CODE_MAC_ERROR=1001;
    /** 视频播放完毕*/
    public static final int SERVER_RESPONSE_CODE_VIDEO_COMPLETE = 1;
    /** 大小图不匹配*/
    public static final int SERVER_RESPONSE_CODE_IMAGE_ID_CHECK_FAILED = 2;
    /** 投屏ID不匹配*/
    public static final int SERVER_RESPONSE_CODE_PROJECT_ID_CHECK_FAILED = 3;
    /** 他人正在投屏，供抢投方判断弹窗*/
    public static final int SERVER_RESPONSE_CODE_ANOTHER_PROJECT = 4;

    public static final String CACHE=".cache";
    /**
     * 显示二维码指令
     */
    public static final String NETTY_SHOW_QRCODE_COMMAND = "call-tdc";


    /**
     * 停止投屏
     */
    public static final String NETTY_STOP_PROJECTION_COMMAND = "call-stop-projection";

    /**
     * 小程序指令-下载
     */
    public static final String NETTY_MINI_PROGRAM_COMMAND = "call-mini-program";
    /**
     * 投屏类型:图片
     */
    public static final String PROJECT_TYPE_PICTURE = "pic";
    /**
     *投屏类型：小程序餐厅端图片
     */
    public static final String PROJECT_TYPE_REST_PICTURE = "pic_rest";
    /**
     *投屏类型：小程序餐厅销售销售端视频
     */
    public static final String PROJECT_TYPE_VIDEO_REST = "video_rest";
    /**
     *投屏类型：小程序餐厅销售端欢迎词
     */
    public static final String PROJECT_TYPE_WELCOME_PICTURE = "pic_welcome";

    /**
     * 投屏类型:小程序视频
     */
    public static final String PROJECT_TYPE_VIDEO = "video";
    /**
     * 投屏类型:小程序投生日歌
     */
    public static final String PROJECT_TYPE_VIDEO_BIRTHDAY = "video_birthday";
    /**
     * 投屏类型:小程序点播
     */
    public static final String PROJECT_TYPE_VIDEO_VOD = "vod";

    public static final String USB_FILE_PATH = "redian";

    /**U盘安装酒楼文件夹目录*/
    public static final String USB_FILE_HOTEL_PATH = "savor";
    public static final String USB_FILE_HOTEL_MEDIA_PATH = "media";
    /**U盘安装酒楼配置文件*/
    public static final String USB_FILE_HOTEL_UPDATE_CFG = "update.cfg";
    /**U盘安装酒楼配置文件-获取电视列表*/
    public static final String USB_FILE_HOTEL_GET_CHANNEL = "get_channel";
    /**U盘安装酒楼配置文件-上传电视列表*/
    public static final String USB_FILE_HOTEL_SET_CHANNEL = "set_channel";
    /**U盘安装酒楼配置文件-拉取日志文件*/
    public static final String USB_FILE_HOTEL_GET_LOG = "get_log";
    /**U盘安装酒楼配置文件-拉取备份日志文件*/
    public static final String USB_FILE_HOTEL_GET_LOGED = "get_loged";
    /**U盘安装酒楼配置文件-更新视频*/
    public static final String USB_FILE_HOTEL_UPDATE_MEIDA = "update_media";
    /**U盘安装酒楼配置文件-更新版本*/
    public static final String USB_FILE_HOTEL_UPDATE_APK = "update_apk";
    /**U盘安装酒楼配置文件-更新LOGO*/
    public static final String USB_FILE_HOTEL_UPDATE_LOGO = "update_logo";
    /**U盘安装酒楼配置文件-更新视频json文件*/
    public static final String USB_FILE_HOTEL_UPDATE_JSON = "play_list.json";
    /**U盘安装酒楼配置文件-更新宣传片目录*/
    public static final String USB_FILE_HOTEL_UPDATE_ADV= "adv";
    /**U盘安装酒楼配置文件-日志提取目录*/
    public static final String USB_FILE_LOG_PATH = "log";
    public static final String USB_FILE_LOGED_PATH = "loged";
    /**U盘安装-频道信息原数据*/
    public static final String USB_FILE_CHANNEL_RAW_DATA = "channel_raw";
    /**U盘安装-频道信息编辑数据*/
    public static final String USB_FILE_CHANNEL_EDIT_DATA = "channel.csv";
    /**U盘安装-酒楼列表文件*/
    public static final String USB_FILE_HOTEL_LIST_JSON = "hotel.json";
    /**U盘安装酒楼配置文件-单机日志标志*/
    public static final String STANDALONE="standalone";

    /** 更新播放列表Action*/
    public static final String UPDATE_PLAYLIST_ACTION = "com.savor.ads.action_update_playlist";


    public static final int KEY_DOWN_LAG = 2000;

    public static final String SSDP_CONTENT_TYPE = "box";

    /** 默认电视切换时间*/
    public static final int DEFAULT_SWITCH_TIME = 999;
    /** 默认轮播音量*/
    public static final int DEFAULT_ADS_VOLUME = 60;
    /** 默认投屏音量*/
    public static final int DEFAULT_PROJECT_VOLUME = 100;
    /** 默认点播音量*/
    public static final int DEFAULT_VOD_VOLUME = 90;
    /** 默认电视音量*/
    public static final int DEFAULT_TV_VOLUME = 100;
    /**节目单-节目*/
    public static final String PRO = "pro";
    /**节目单-宣传单*/
    public static final String ADV = "adv";
    /**节目单-广告*/
    public static final String ADS = "ads";
    /**节目单-RTB广告*/
    public static final String RTB_ADS = "rtbads";
    /**节目单-poly预下载广告*/
    public static final String POLY_ADS = "poly";
    public static final String POLY_ADS_ONLINE = "poly_online";
    /**节目单-activity活动广告*/
    public static final String ACTGOODS= "actgoods";
    /**活动商品-优选*/
    public static final String ACTGOODS_OPTI= "10";
    /**活动商品-我的活动*/
    public static final String ACTGOODS_ACTIVITY= "20";
    /**活动商品-秒杀*/
    public static final String ACTGOODS_COUNTDOWN= "40";
    /**节目单-精选内容*/
    public static final String SELECT_CONTENT= "selectcontent";
    /**百度广告类型**/
    public static final String DSP_MEDIA_TYPE_BAIDU = "1";
    /**钛镁广告类型**/
    public static final String DSP_MEDIA_TYPE_MEI = "2";
    /**奥凌广告类型**/
    public static final String DSP_MEDIA_TYPE_OOHLINK = "3";
    /**众盟广告类型**/
    public static final String DSP_MEDIA_TYPE_ZMENG = "4";
    /**京东钼媒广告类型**/
    public static final String DSP_MEDIA_TYPE_JDMOMEDIA = "5";
    /**易售广告类型**/
    public static final String DSP_MEDIA_TYPE_YISHOU = "6";

    public static final List<String> DSP_DOWNLOADING_FILES = new ArrayList<>();



    /**外置SD卡至少保留的可用空间*/
    public static final long EXTSD_LEAST_AVAILABLE_SPACE = 1024 * 1024 * 1024;


    public static final String APK_INSTALLED_PATH_GIEC = "/system/priv-app/savormedia/";
    public static final String APK_INSTALLED_PATH_SVT = "/system/app/savormedia/";

    /**节目数据文件位置*/
    public static final String PRO_DATA_PATH = "/sdcard/server_data/pro_data";
    /**宣传片数据文件位置*/
    public static final String ADV_DATA_PATH = "/sdcard/server_data/adv_data";
    /**广告数据文件位置*/
    public static final String ADS_DATA_PATH = "/sdcard/server_data/ads_data";

    /**百度聚屏APP ID*/
    public static final String BAIDU_ADS_APP_ID = "ce124b3c";
    /**百度聚屏代码位ID-小热点餐厅包间电视小于40寸*/
    public static final String BAIDU_ADSLOT_ID1 = "5821973";
    /**百度聚屏代码位ID-小热点餐厅包间电视41-45寸*/
    public static final String BAIDU_ADSLOT_ID2 = "5822033";
    /**百度聚屏代码位ID-小热点餐厅包间电视46-50寸*/
    public static final String BAIDU_ADSLOT_ID3 = "5822038";
    /**百度聚屏代码位ID-小热点餐厅包间电视51-55寸*/
    public static final String BAIDU_ADSLOT_ID4 = "5822040";
    /**百度聚屏代码位ID-小热点餐厅包间电视55寸以上*/
    public static final String BAIDU_ADSLOT_ID5 = "5822044";

    /**实体小平台**/
    public static final String ENTITY="entity";
    /**虚拟小平台**/
    public static final String VIRTUAL="virtual";

    public static final String APK_DOWNLOAD_FILENAME =  "updateapksamples.apk";
    public static final String ROM_DOWNLOAD_FILENAME =  "update_signed.zip";
    /**推送类型定义,1:RTB推送;2:移动网络4g投屏,3:执行shell命令,4:推送升级apk,5:推送关机**/
    public static final int PUSH_TYPE_RTB_ADS = 1;
    public static final int PUSH_TYPE_4G_PROJECTION = 2;
    public static final int PUSH_TYPE_SHELL_COMMAND = 3;
    public static final int PUSH_TYPE_UPDATE = 4;
    public static final int PUSH_TYPE_SHUTDOWN = 5;


    /**
     * (为了好识别所以下载展示二维码)
     * 8.标准版二维码
     * 12.大二维码
     * 13.小程序呼二维码
     * 15.大二维码（新节目）
     * 16.极简版二维码
     * 19.极简版大维码(新节目)
     * 20.极简版大二维码
     * 21.极简版呼二维码
     * **/

    public static final int MINI_PROGRAM_QRCODE_SMALL_TYPE = 8;
    public static final int MINI_PROGRAM_QRCODE_BIG_TYPE = 12;
    public static final int MINI_PROGRAM_QRCODE_CALL_TYPE = 13;
    public static final int MINI_PROGRAM_QRCODE_NEW_TYPE = 15;
    public static final int MINI_PROGRAM_SQRCODE_SMALL_TYPE = 16;
    public static final int MINI_PROGRAM_SQRCODE_NEW_TYPE = 19;
    public static final int MINI_PROGRAM_SQRCODE_BIG_TYPE = 20;
    public static final int MINI_PROGRAM_SQRCODE_CALL_TYPE = 21;

    /**存储到本地图片的名称**/
    public static final String MINI_PROGRAM_QRCODE_NAME = "getBoxQrcode.jpg";
    public static final String MINI_PROGRAM_QRCODE_TEMP_NAME = "getBoxQrcodeTemp.jpg";
    public static final String MINI_PROGRAM_QRCODE_BIG_NAME = "getBoxQrcodeBig.jpg";
    public static final String MINI_PROGRAM_QRCODE_BIG_TEMP_NAME = "getBoxQrcodeBigTemp.jpg";
    public static final String MINI_PROGRAM_QRCODE_NEW_NAME = "getBoxQrcodeNew.jpg";
    public static final String MINI_PROGRAM_QRCODE_NEW_TEMP_NAME = "getBoxQrcodeNewTemp.jpg";
    public static final String MINI_PROGRAM_QRCODE_CALL_NAME = "getBoxQrcodeCall.jpg";
    public static final String MINI_PROGRAM_QRCODE_CALL_TEMP_NAME = "getBoxQrcodeCallTemp.jpg";

    public static final String MINI_PROGRAM_SQRCODE_NAME = "getBoxQrcodeSimple.jpg";
    public static final String MINI_PROGRAM_SQRCODE_TEMP_NAME = "getBoxQrcodeSimpleTemp.jpg";
    public static final String MINI_PROGRAM_SQRCODE_BIG_NAME = "getBoxQrcodeSimpleBig.jpg";
    public static final String MINI_PROGRAM_SQRCODE_BIG_TEMP_NAME = "getBoxQrcodeSimpleBigTemp.jpg";
    public static final String MINI_PROGRAM_SQRCODE_NEW_NAME = "getBoxQrcodeSimpleNew.jpg";
    public static final String MINI_PROGRAM_SQRCODE_NEW_TEMP_NAME = "getBoxQrcodeSimpleNewTemp.jpg";
    public static final String MINI_PROGRAM_SQRCODE_CALL_NAME = "getBoxQrcodeSimpleCall.jpg";
    public static final String MINI_PROGRAM_SQRCODE_CALL_TEMP_NAME = "getBoxQrcodeSimpleCallTemp.jpg";

    public static String MEI_SSP_ADS_MONITOR_URL = "";

    public static final String PROJECTION_IMG_THUMBNAIL_PARAM = "?x-oss-process=image/resize,m_lfit,h_150,w_150";
    public static final String PROJECTION_VIDEO_THUMBNAIL_PARAM = "?x-oss-process=video/snapshot,t_7000,f_jpg,w_150,h_150,m_fast";

    /**小程序投屏-标准版**/
    public static final String SMALL_APP_ID_STANDARD = "1";
    /**小程序投屏-极简版**/
    public static final String SMALL_APP_ID_SIMPLE = "2";
    /**小程序投屏-餐厅版**/
    public static final String SMALL_APP_ID_REST = "4";

    /**轮播显示小程序码的视频,视频中显示用户投屏照片视频**/
    public static final String QRCODE_PRO_VIDEO_ID = "19533";
    /**手机呼出显示小程序码的视频**/
    public static final String QRCODE_CALL_VIDEO_ID = "17614";

    /*********钛镁广告平台账号开始*************/
    /**MeiSSP视频key**/
    public static final String MEI_SSP_VIDEO_KEY = "067e67da-6429-44d5-a2d8-bc5e682c8463";
    /**MeiSSP图片key**/
    public static final String MEI_SSP_IMAGE_KEY = "06910ca2-68ac-440c-b33d-17cc8031307b";
    /*********钛镁广告平台账号开始*************/

    /*********奥凌广告平台账号开始*************/
    public static final String MCHANNEL_ID="ReDian";
    public static final String MTOKEN="d0e11d1ff7b17918ee672e94751c980e";
    /*********奥凌广告平台账号结束*************/

    /*********众盟广告平台账号开始*************/
    public static final String ZMENG_CHANNEL_ID = "littlehotspot";
    //正式
    public static final String ZMENG_TOKEN = "8UQ8JSTzaE35M5TWsawnJYLIy9K_51B3sCmAeFVVyGFYw2nKwfKZSRLVhreVTfnO";
    //测试
//    public static final String ZMENG_TOKEN = "H9uFvW2rFJJQ1WulxOEww4LIy9K_51B3sCmAeFVVyGFYw2nKwfKZSRLVhreVTfnO";
    /*********众盟广告平台账号结束*************/

    /*********京东钼媒平台账号开始*************/
    //正式
    public static final String JDMOMEDIA_APPID = "9323";
    public static final String JDMOMEDIA_APPKEY = "733a43d9affa74a64ce3a6b3c41ffc69";
    //测试
//    public static final String JDMOMEDIA_APPID = "8721";
//    public static final String JDMOMEDIA_APPKEY = "392ed76a8eb418820eebbd6296db29b7";
    /*********京东钼媒平台账号结束*************/

    /*********易售广告平台账号开始*************/
    public static final String YISHOU_API = "1.3";
    //测试
//    public static final String YISHOU_APPPID = "pK9DWNA5633S8siJ";
//    public static final String YISHOU_APPPKEY = "wQyUslfRfxgCnFtLiyJax6tx8LREyBbQ";
    //正式
    public static final String YISHOU_APPPID = "cside7b9n120olls";
    public static final String YISHOU_APPPKEY = "86yzzacc5rjik4x90wkk0zee37g9qxv2";
    //图片
    public static final String YISHOU_IMG_ID = "25000170";
    public static final String YISHOU_IMG = "IMG";
    //视频
    public static final String YISHOU_VDO_ID = "25000171";
    public static final String YISHOU_VDO = "VDO";
    /*********易售广告平台账号结束*************/

    /**乐视信号源包名和类名*/
    public static final String LETV_SIGNAL_SOURCE_PAKAGE = "com.stv.signalsourcemanager";
    public static final String LETV_SIGNAL_SOURCE_CLASS = "com.stv.signalsourcemanager.MainActivity";

    /**视纬通信号源包名和类名*/
    public static final String SVT_SIGNAL_SOURCE_PAKAGE = "com.siviton.tvplayer";
    public static final String SVT_SIGNAL_SOURCE_CLASS = "com.siviton.tvplayer.ui.RootActivity";
    /**首次安装时，将assets目录下的初始化视频文件拷贝到EMC的media目录下*/
    public static final String ASSETS_VIDEO_NAME = "bootvideo.mp4";
    public static final String INIT_VIDEO_NAME = "init.mp4";
    public static final String BOOT_VIDEO_NAME = "bootvideo.ts";
    /**将启动视频临时存放到/sdcard/根目录下用于cp到/data/local/bootvideo.ts*/
    public static final String BOOT_VIDEO_TEMP = "bootvideo.mp4";
    public static final String BOOT_VIDEO_STORAGE = "/data/local/";

    /**模拟电视*/
    public static final int SVT_INPUT_SOURCE_ATV = 1;
    /**数字电视*/
    public static final int SVT_INPUT_SOURCE_DTV = 28;
    /**视频*/
    public static final int SVT_INPUT_SOURCE_CVBS = 2;
    /**分量*/
    public static final int SVT_INPUT_SOURCE_YPBPR = 16;
    /**HDMI1*/
    public static final int SVT_INPUT_SOURCE_HDMI1 = 23;
    /**HDMI2*/
    public static final int SVT_INPUT_SOURCE_HDMI2 = 24;
    /**HDMI3*/
    public static final int SVT_INPUT_SOURCE_HDMI3 = 25;

    /**用户精选-热播内容*/
    public static final String SELECT_CONTENT_HOT = "1";
    /**用户精选-发现内容*/
    public static final String SELECT_CONTENT_DISCOVER = "2";
}

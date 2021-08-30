package com.savor.ads.utils;

import android.graphics.Bitmap;
import android.util.Pair;

import com.savor.ads.bean.BaiduAdLocalBean;
import com.savor.ads.bean.BigImgBean;
import com.savor.ads.bean.JDmomediaLocalBean;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.MeiAdLocalBean;
import com.savor.ads.bean.MiniProgramProjection;
import com.savor.ads.bean.OOHLinkAdLocalBean;
import com.savor.ads.bean.ProjectionImg;
import com.savor.ads.bean.SimpleRequestBean;
import com.savor.ads.bean.YishouAdLocalBean;
import com.savor.ads.bean.ZmengAdLocalBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhanghq on 2017/3/20.
 */

public class GlobalValues <T extends MediaLibBean> {

    private static GlobalValues instance;

    public static GlobalValues getInstance() {
        if (instance == null) {
            instance = new GlobalValues();
        }
        return instance;
    }

    /** 二维码内容*/
    public static String QRCODE_CONTENT = "";
    /** 输入验证码*/
    public static String AUTH_CODE = "";

    /** 播放列表*/
    public ArrayList<T> PLAY_LIST;
    /**热播内容ID集合*/
    public ArrayList<String> HOT_CONTENT_LIST;
    /** 请求到的百度聚屏广告集合，填充节目单时会用到*/
    public static ArrayList<BaiduAdLocalBean> POLY_BAIDU_ADS_PLAY_LIST;
    public static ArrayList<MeiAdLocalBean> POLY_MEI_ADS_PLAY_LIST;
    public static ArrayList<OOHLinkAdLocalBean> DSP_OOHLINK_ADS_PLAY_LIST;
    public static ArrayList<ZmengAdLocalBean> DSP_ZMENG_ADS_PLAY_LIST;
    public static ArrayList<JDmomediaLocalBean> DSP_JDMOMEDIA_ADS_PLAY_LIST;
    public static ArrayList<YishouAdLocalBean> DSP_YISHOU_ADS_PLAY_LIST;
    /** 拿到聚屏广告后此刻的节目order，填充节目单时会用到*/
    public static int CURRENT_MEDIA_ORDER = 0;

    /** 当前投屏设备ID*/
    public volatile static String CURRENT_PROJECT_DEVICE_ID;
    /** 当前投屏设备IP*/
    public volatile static String CURRENT_PROJECT_DEVICE_IP;
    /** 上次投屏设备ID*/
    public volatile static String LAST_PROJECT_DEVICE_ID;
    /** 当前投屏设备名称*/
    public volatile static String CURRENT_PROJECT_DEVICE_NAME;
    /** 当前投屏图片*/
    public volatile static Bitmap CURRENT_PROJECT_BITMAP;
    /** 当前投屏图片ID*/
    public volatile static String CURRENT_PROJECT_IMAGE_ID;
    /** 当前投屏动作ID*/
    public volatile static String CURRENT_PROJECT_ID;
    /** 上次投屏ID*/
    public volatile static String LAST_PROJECT_ID;
    /** 是否是抽奖*/
    public volatile static boolean IS_LOTTERY;
    /** 是否是餐厅端投屏*/
    public volatile static boolean IS_RSTR_PROJECTION;

    /** 标识盒子是否正在忙碌中，，忙碌中则不处理投屏类请求*/
    public static volatile boolean IS_BOX_BUSY = false;

    /** 标识友盟推送所需SO拷贝是否成功*/
    public static boolean IS_UPUSH_SO_COPY_SUCCESS = false;
    /** 标识友盟推送注册是否成功*/
    public static boolean IS_UPUSH_REGISTER_SUCCESS = false;

    /** 未在本地找到的百度聚屏广告KEY（md5）*/
    public static String NOT_FOUND_BAIDU_ADS_KEY;
    /**
     * 百度聚屏广告连续重复次数
     * first: 广告md5
     * second: 连续次数
     */
    public static Pair<String, Integer> CURRENT_ADS_REPEAT_PAIR;
    /**
     * 当前跳过的聚屏广告请求次数
     */
    public static int CURRENT_ADS_BLOCKED_COUNT = 0;

    /**当前投屏人的微信ID**/
    public static String CURRENT_OPEN_ID;
    /**当前投屏操作的唯一标示ID**/
    public static String CURRENT_FORSCREEN_ID;
    /**本次投屏文字**/
//    public static String PROJECTION_WORDS;
    /**当前netty接收到的图片投屏人投的并且下载成功的照片集合**/
    public static ArrayList<String> PROJECT_IMAGES=new ArrayList<>();
    /**当前netty接收到的图片投屏人投的但是下载失败的照片集合**/
    public static ArrayList<String> PROJECT_FAIL_IMAGES=new ArrayList<>();
    /**当前netty接收到的图片投屏请求的集合**/
//    public static ArrayList<ProjectionImg> PROJECT_LIST = new ArrayList<>();
    /**当前netty接收到的视频投屏请求的集合**/
    public static String PROJECTION_VIDEO_PATH;
    /**极简版投图片下载成功集合**/
    public static ArrayList<String> PROJECT_STREAM_IMAGE = new ArrayList<>();
    /**极简版投图片下载失败集合**/
    public static ArrayList<String> PROJECT_STREAM_FAIL_IMAGE = new ArrayList<>();
    /**极简版切片投图片集合**/
    public static ArrayList<BigImgBean> PROJECT_THUMBNIAL_IMAGE = new ArrayList<>();
    /**极简版dialog显示下载列表个数使用**/
    public static ArrayList<ProjectionImg> PROJECT_STREAM_IMAGE_NUMS = new ArrayList<>();

    //间隔几次展示一次互动投屏片头片尾广告
    public static int INTERVAL_INTERACTION_ADS_NUM = 1;
    public static int CURRENT_INTERACTION_ADS_NUM = 0;
    /**当前是否正在播广告,主状态,1：前置广告;0:无广告;2:后置广告*/
    public static int INTERACTION_ADS_PLAY=0;
    /**当前投屏行为来自标准版小程序服务*/
    public static int FROM_SERVICE_MINIPROGRAM = 1;
    /**当前投屏行为来自jetty服务*/
    public static int FROM_SERVICE_REMOTE = 2;

    /**
     * 当前请求的聚屏广告的位置，从接口返回的聚屏的顺序依次请求,默认从0开始
     */
    public static int DSP_POLY_ORDER_POSITION=0;

    public static String YISHOU_REQUEST_TYPE=ConstantValues.YISHOU_IMG;

    /**循环播放欢迎词*/
    public static MiniProgramProjection mpprojection;
    /**欢迎词开始播放时间*/
    public static long loopStartTime;
    /**当前欢迎词投屏ID*/
    public static int WELCOME_ID;

    public static boolean mIsGoneToTv=false;
    /**当前是否处于霸王菜活动中*/
    public static boolean isActivity;
    /**----------------------------------**/
    /**当前机顶盒是否处于下载状态*/
    public static boolean isDownload;
    /**当前机顶盒下载文件*/
    public static String currentDownlaodFileName;

    public static long bytesNotWrite=0;
    /**
     * 解釋一下判斷邏輯
     * 如果投圖片三次及以上，就提示視頻引導
     * 如果投視頻三次及以上，就提示圖片引導
     * 如果投文件三次及以上，就提示視頻引導
     */
    public static ConcurrentHashMap<String,Integer> IMG_NUM= new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String,Integer> VIDEO_NUM= new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String,Integer> FILE_NUM= new ConcurrentHashMap<>();
    /**netty第一次注册*/
    public static boolean NETTY_FIRST_REGISTER;
    /**当前轮播视频的VID*/
    public static String currentVid;
    /**当前是否在展示红包弹出窗口*/
    public static boolean isOpenRedEnvelopeWin;
    public static String testWechatUrl;
}

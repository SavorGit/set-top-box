package com.savor.ads.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.savor.ads.BuildConfig;
import com.savor.ads.R;
import com.savor.ads.SavorApplication;
import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.bean.MediaFileBean;
import com.savor.ads.dialog.BusinessCardDialog;
import com.savor.ads.activity.MonkeyGameActivity;
import com.savor.ads.activity.PartakeDishDrawActivity;
import com.savor.ads.activity.ScreenProjectionActivity;
import com.savor.ads.activity.WebviewGameActivity;
import com.savor.ads.bean.BirthdayOndemandBean;
import com.savor.ads.bean.JsonBean;
import com.savor.ads.bean.PartakeDishBean;
import com.savor.ads.bean.ProjectionGuideImg;
import com.savor.ads.dialog.BusinessFileDialog;
import com.savor.ads.dialog.JuhuasuanResultDialog;
import com.savor.ads.dialog.JuhuasuanShowDialog;
import com.savor.ads.dialog.LuckyDrawDialog;
import com.savor.ads.dialog.OpRedEnvelopeQrCodeDialog;
import com.savor.ads.dialog.PartakeDishDialog;
import com.savor.ads.log.LogParamValues;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.MiniProgramProjection;
import com.savor.ads.bean.ProjectionImg;
import com.savor.ads.bean.UserBarrage;
import com.savor.ads.bean.WelcomeResourceBean;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.database.DBHelper;
import com.savor.ads.dialog.ProjectionImgListDialog;
import com.savor.ads.dialog.ScanRedEnvelopeQrCodeDialog;
import com.savor.ads.log.LogReportUtil;
import com.savor.ads.okhttp.coreProgress.download.ProjectionDownloader;
import com.savor.ads.oss.OSSUtils;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.Base64Utils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.MiniProgramQrCodeWindowManager;
import com.savor.ads.utils.QrCodeWindowManager;
import com.savor.ads.utils.ShowMessage;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.savor.small.netty.MiniProNettyClient;
import cn.savor.small.netty.MiniProNettyClient.MiniNettyMsgCallback;

import static com.savor.ads.utils.GlobalValues.FROM_SERVICE_MINIPROGRAM;

/**
 * 启动小程序Netty服务
 * Created by zhanghq on 2018/07/09.
 */
public class MiniProgramNettyService extends Service implements MiniNettyMsgCallback{
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    private Context context;
    private Session session;
    private DBHelper dbHelper;
    private String box_downstime;
    ProjectionImgListDialog pImgListDialog = null;
    //标准版轮播图片时长
    private int INTERVAL_TIME=1000*10;
    //销售端轮播图片时长
    private int REST_INTERVAL_TIME=1000*30;
    //如果网络比较慢，则等待，超2分钟退出
    private int DOWNLOAD_TIME = 0;
    private int downloadIndex;
    private int currentIndex;
    public static MiniProgramProjection mpProjection;
    private PartakeDishBean partakeDishBean;
    private String headPic;
    private String nickName;
    private String openid;
    private String lastOpenid;
    private int img_nums=0;
    private int playTimes=0;
    //投图片
    private int TYPE_IMG = 1;
    //投视频
    private int TYPE_VIDEO = 2;
    //上报投屏信息中，0:正常投屏，1：打断
    private static String PROJECTION_STATE_PLAY="0";
    private static String PROJECTION_STATE_BREAK="1";
    //是否进程在下载中
    private boolean isDownloadRunnable= false;
    //是否正在播放ppt
    private boolean isPPTRunnable = false;
    //当前投屏唯一标示ID
    private String forscreen_id;
    private String serial_number;
    private String words;
    private String wordSize;
    private String wordColor;
    private String fontPath;
    private String musicPath;
    Handler handler=new Handler(Looper.getMainLooper());
    public static ConcurrentHashMap<String,String> projectionIdMap = new ConcurrentHashMap<>();
    private int currentAction;
    /**霸王菜抽奖活动**/
    private PartakeDishDialog partakeDishDialog = null;
    /**发红包**/
    private ScanRedEnvelopeQrCodeDialog scanRedEnvelopeQrCodeDialog =null;
    /**系统发红包**/
    private OpRedEnvelopeQrCodeDialog opRedEnvelopeQrCodeDialog = null;
    /**系统抽奖**/
    private LuckyDrawDialog luckyDrawDialog = null;
    /**商務宴請-個人名片推送*/
    private BusinessCardDialog cardDialog = null;
    /**商务宴请-商业文件分享*/
    private BusinessFileDialog fileDialog = null;
    /**展示聚划算窗口*/
    private JuhuasuanShowDialog juhuasuanShowDialog = null;
    /**聚划算活动结果窗口*/
    private JuhuasuanResultDialog juhuasuanResultDialog = null;
    private AdsBinder adsBinder = new AdsBinder();
    /**增加投屏前置或者后置广告,前置：1，后置：2*/
    private MediaLibBean preOrNextAdsBean=null;
    public MiniProgramNettyService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        session = Session.get(context);
        dbHelper = DBHelper.get(context);
        pImgListDialog = new ProjectionImgListDialog(context);
        partakeDishDialog = new PartakeDishDialog(context);
        scanRedEnvelopeQrCodeDialog = new ScanRedEnvelopeQrCodeDialog(context);
        opRedEnvelopeQrCodeDialog = new OpRedEnvelopeQrCodeDialog(context);
        luckyDrawDialog = new LuckyDrawDialog(context);
        cardDialog = new BusinessCardDialog(context);
        fileDialog = new BusinessFileDialog(context);
        juhuasuanShowDialog = new JuhuasuanShowDialog(context);
        juhuasuanResultDialog = new JuhuasuanResultDialog(context);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogFileUtil.write("MiniProgramNettyService onHandleIntent");
        try {
            LogUtils.d("启动小程序NettyService");
            LogFileUtil.write("启动小程序NettyService");

            fetchMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return adsBinder;
    }

    public class AdsBinder extends Binder{
        public MiniProgramNettyService getService(){
            return MiniProgramNettyService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogFileUtil.write("MiniProgramNettyService onDestroy");
    }

    private void fetchMessage()  {
        try{
            LogUtils.d("测试netty启动 fetchMessage");
            if (!TextUtils.isEmpty(session.getNettyUrl())&&session.getNettyPort()!=0) {
                MiniProNettyClient.init(session.getNettyPort(), session.getNettyUrl(), this, getApplicationContext());
                MiniProNettyClient.get().start();
                String mUUID = String.valueOf(System.currentTimeMillis());
                LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.conn,LogParamValues.netty,"");
            }
        }catch (Exception e){
            Log.d("测试netty启动fetchMessage异常",e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveMiniServerMsg(String msg, String content) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        /***
         * action字段
         * 1:呼玛
         * 2:投屏视频
         * 3:退出投屏
         * 4:投屏多张图片（包括单张）
         * 5:点播机顶盒内存在的视频
         * 6:点播生日相关视频
         * 7:文件单图投屏
         * 8:图片旋转角度
         * 9:手机小程序呼出大码
         * 10:发现，喜欢，公开的多图投屏，通过一个完整json传递all data
         * 11:热播内容投图片
         * 12:热播内容投视频
         * 13:商城商品视频点播
         * 14:本地生活图片点播
         * 15:本地生活视频点播
         * 101:发起游戏
         * 102:开始游戏
         * 103:加入游戏
         * 104:退出游戏
         * 105:原班人马，在玩一次
         * 110:进入游戏（打开h5页面）
         * 111:退出游戏(退出h5页面)
         * 999:下载固定资源测试下载速度
         * 31:加减音量，change_type 1:减音量 2:加音量
         * 32:切换节目, change_type 1:上一个节目 2:下一个节目
         * 40:小程序点播商品广告
         * 42:小程序销售端视频投屏
         * 44:小程序销售端图片投屏(含单张)
         * 120:支付红包二维码(---废弃---)
         * 121:抢红包小程序码
         * 122:接收到弹幕
         * 130:销售端欢迎词推送
         * 131|132:退出欢迎词播放
         * 133:推广渠道投屏码
         * 134:投屏帮助视频
         * 135:霸王餐抽奖活动推送
         * 136:霸王餐开奖
         * 137:商务宴请-欢迎词(这个跟销售端欢迎词的区别是，销售端是单张图片，这个商务宴请的欢迎词是多张图片的)
         * 138:系统后台发起抽奖活动（含有发红包）
         * 140:对服务人员评价打赏后机顶盒展示效果
         * 141:推送用户审核通过的视频内容，走2的逻辑
         * 142:推送用户审核通过的图片内容,走10的逻辑
         * 150:小程序连接机顶盒成功后推送引导投屏图
         * 151:开始聚划算活动
         * 160:商务宴请-個人名片推送
         * 170:商务宴请-文件分享
         * 171:预下载投屏资源（含图片，视频，文件）
         * 000:活动广告
         */
        if (ConstantValues.NETTY_MINI_PROGRAM_COMMAND.equals(msg)){
            if (!TextUtils.isEmpty(content)){
                try {
                    JSONObject jsonObject = new JSONObject(content);
                    final int action = jsonObject.getInt("action");
                    boolean flag = checkIsUserProjection(action);
                    if (flag){
                        return;
                    }
                    currentAction = action;
                    if (jsonObject.has("req_id")){
                        String req_id = jsonObject.getString("req_id");
                        box_downstime = System.currentTimeMillis()+"";
                        JsonBean jsonBean = AppApi.getProjectionNettyTime(context,apiRequestListener,req_id,box_downstime);
                        JSONObject json = new JSONObject(jsonBean.getConfigJson());
                        if (json.getInt("code")==AppApi.HTTP_RESPONSE_STATE_SUCCESS){}
                    }
                    LogUtils.d("收到请求，action="+action);
                    LogUtils.d("收到请求，content="+content);
                    //如果是小程序投屏的话，就将app投屏状态情况
                    GlobalValues.CURRENT_PROJECT_BITMAP = null;
                    musicPath = null;
                    if (action==136){
                        PartakeDishBean partakeDishBean = gson.fromJson(content, new TypeToken<PartakeDishBean>() {}.getType());
                        this.partakeDishBean = partakeDishBean;
                    }else{
                        MiniProgramProjection mpProjection = gson.fromJson(content, new TypeToken<MiniProgramProjection>() {}.getType());
                        if (mpProjection!=null&&action!=3&&action!=133){
                            this.mpProjection = mpProjection;
                            headPic = Base64Utils.getFromBase64(mpProjection.getHeadPic());
                            nickName = mpProjection.getNickName();
                            lastOpenid = openid;
                            openid = mpProjection.getOpenid();
                            forscreen_id = mpProjection.getForscreen_id();
                            serial_number = mpProjection.getSerial_number();
                        }
                    }

                    /*******************************/
                    if (currentAction!=31&&currentAction!=32){
                        removeDialog();
                    }
                    /*******************************/
                    if (action != 101 && action != 102 && action != 103 && action != 105
                            && ActivitiesManager.getInstance().getCurrentActivity() instanceof MonkeyGameActivity) {
                        MonkeyGameActivity activity = (MonkeyGameActivity) ActivitiesManager.getInstance().getCurrentActivity();
                        activity.exitGame();
                    }
                    handler.removeCallbacks(mProjectShowImageRunnable);
                    handler.removeCallbacks(downloadFileRunnable);
                    handler.removeCallbacks(downloadFileNoDialogRunnable);
                    handler.post(mProjectExitDownloadRunnable);
                    if (action!=110){
                        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
                        if (activity instanceof WebviewGameActivity){
                            ((WebviewGameActivity) activity).exitWebview();
                        }

                    }

                    if (action!=2
                            &&action!=4
                            &&action!=42
                            &&action!=44
                            &&action!=31
                            &&action!=32
                            &&action!=121
                            &&action!=122
                            &&GlobalValues.INTERACTION_ADS_PLAY!=0){
                        return;
                    }
                    switch (action) {

                        case 1:
                            break;
                        case 2:
                        case 42:
                        case 12:
                        case 15:
                        case 141:
                            new Thread(()->projectionVideo(mpProjection)).start();
                            break;
                        case 3:
                            exitProjection();
                            break;
                        case 4:
                        case 44:
                            new Thread(()->projectionMoreImg(mpProjection,action)).start();
                            break;
                        case 5:
                            onDemandSetTopBoxVideo(mpProjection);
                            break;
                        case 6:
                            onDemandBirthdayVideo(mpProjection);
                            break;
                        case 7:
                            new Thread(()->projectionFileImg(mpProjection)).start();
                            break;
                        case 8:
                            projectionRotateImg();
                            break;
                        case 9:
                            callBigQrCodeVideo(mpProjection);
                            break;
                        case 10:
                        case 11:
                        case 14:
                        case 142:
                            projectionListImg(mpProjection);
                            break;
                        case 13:
                            onDemandGoodsVideo(mpProjection);
                            break;
                        case 31:
                            if (jsonObject.has("change_type")){
                                int adjust = jsonObject.getInt("change_type");
                                adjustVoice(adjust);
                            }
                            break;
                        case 32:
                            if (jsonObject.has("change_type")){
                                int value = jsonObject.getInt("change_type");
                                adjustVideo(value);
                            }
                            break;
                        case 40:
                            if (jsonObject.has("goods_id")){
                                int goods_id = jsonObject.getInt("goods_id");
                                String qrcode_url = jsonObject.getString("qrcode_url");
                                onDemandGoodsAds(goods_id,qrcode_url);
                            }
                            break;
                        case 101:
                            launchMonkeyGame(action,mpProjection);
                            break;
                        case 102:
                            startMonkeyGame(action,mpProjection);
                            break;
                        case 105:
                            repeatMonkeyGame(action,mpProjection);
                            break;
                        case 103:
                            addMonkeyGame(action,mpProjection);
                            break;
                        case 104:
                            exitMonkeyGame();
                            break;
                        case 110:
                            startWebviewGame(mpProjection.getUrl());
                            break;
                        case 111:
                            exitWebviewGame();
                            break;
                        case 121:
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
                                    String scanRedEnvelopeUrl = mpProjection.getCodeUrl();
                                    String content = mpProjection.getContent();
                                    int red_type = mpProjection.getRtype();

                                    if (red_type==2){
                                        if (activity instanceof AdsPlayerActivity){
                                            if (opRedEnvelopeQrCodeDialog !=null){
                                                opRedEnvelopeQrCodeDialog.dismiss();
                                                opRedEnvelopeQrCodeDialog = new OpRedEnvelopeQrCodeDialog(context);
                                                opRedEnvelopeQrCodeDialog.show();
                                                opRedEnvelopeQrCodeDialog.setRedEnvelopeInfo(headPic,nickName,scanRedEnvelopeUrl);
                                            }
                                        }
                                    }else{
                                        if (activity instanceof AdsPlayerActivity){
                                            ((AdsPlayerActivity) activity).setScanRedEnvelopeQrCodeDialogListener(scanRedEnvelopeQrCodeDialog);
                                            if (scanRedEnvelopeQrCodeDialog !=null){
                                                scanRedEnvelopeQrCodeDialog.dismiss();
                                                scanRedEnvelopeQrCodeDialog = new ScanRedEnvelopeQrCodeDialog(context);
                                                scanRedEnvelopeQrCodeDialog.show();
                                                scanRedEnvelopeQrCodeDialog.setRedEnvelopeInfo(headPic,nickName,scanRedEnvelopeUrl,content);

                                            }
                                        }
                                    }

                                }
                            });
                            break;
                        case 122:
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    List<UserBarrage> userBarrageList = mpProjection.getUserBarrages();
                                    Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
                                    if (userBarrageList!=null&&userBarrageList.size()>0&&activity instanceof AdsPlayerActivity){
                                        for (UserBarrage userBarrage:userBarrageList){
                                            if (!TextUtils.isEmpty(userBarrage.getBarrage())){
                                                if (!TextUtils.isEmpty(userBarrage.getHeadPic())){
                                                    String headPic = Base64Utils.getFromBase64(userBarrage.getHeadPic());
                                                    ((AdsPlayerActivity) activity).showDanmaku(headPic,userBarrage.getBarrage());
                                                }else{
                                                    ((AdsPlayerActivity) activity).showDanmaku(null,userBarrage.getBarrage());
                                                }

                                            }
                                        }
                                    }
                                }
                            });
                            break;
                        case 130:
                            projectionWelcome(mpProjection);
                            break;
                        case 131:
                        case 132:
                            exitProjectionWelcome(mpProjection);
                            break;
                        case 133:
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
//                                    if (activity instanceof AdsPlayerActivity&&scanRedEnvelopeQrCodeDialog!=null&&!scanRedEnvelopeQrCodeDialog.isShowing()){
//                                        //如果是电视机的话，因为无法将tv模块放到activity的管理栈中，所有加下面的if判断
//                                        if (AppUtils.isSVT()&&GlobalValues.mIsGoneToTv){
//                                           return;
//                                        }
//                                        String qrcodeurl = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ session.getEthernetMac()+"&type="+ ConstantValues.MINI_PROGRAM_QRCODE_EXTENSION_TYPE;
//                                        String forscreen_num = "";
//                                        int countdown = 0;
//                                        try {
//                                            if (jsonObject.has("forscreen_number")){
//                                                forscreen_num = jsonObject.getString("forscreen_number");
//                                            }
//                                            if (jsonObject.has("countdown")){
//                                                countdown = jsonObject.getInt("countdown");
//                                            }
//                                        }catch (Exception e){
//                                            e.printStackTrace();
//                                        }
//                                        if (extensionQrCodeDialog.isShowing()){
//                                            extensionQrCodeDialog.dismiss();
//                                        }
//                                        extensionQrCodeDialog.show();
//                                        extensionQrCodeDialog.showExtensionWindow(context,qrcodeurl,countdown,forscreen_num);
//                                    }
//                                }
//                            });
                            break;
                        case 134:
                            Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
                            if (activity instanceof AdsPlayerActivity){
//                                if (AppUtils.isSVT()&&GlobalValues.mIsGoneToTv){
//                                    return;
//                                }
                                onDemandExtensionVideo(mpProjection);
                            }
                            break;
                        case 135:
                           activity = ActivitiesManager.getInstance().getCurrentActivity();
                           if (activity instanceof AdsPlayerActivity||activity instanceof PartakeDishDrawActivity) {
                               if (AppUtils.isSVT() && GlobalValues.mIsGoneToTv) {
                                   return;
                               }
                               if (activity instanceof PartakeDishDrawActivity){
                                   activity.finish();
                               }
                               involvementPartakedish(mpProjection);

                           }
                           break;
                        case 136:
                            activity = ActivitiesManager.getInstance().getCurrentActivity();
                            if (activity instanceof AdsPlayerActivity) {
                                if (AppUtils.isSVT() && GlobalValues.mIsGoneToTv) {
                                    return;
                                }
                                partakedishResult(partakeDishBean);
                            }
                            break;
                        case 137:
                            businessWelcome();
                            break;
                        case 138:
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
                                    String luckQrcodeUrl = mpProjection.getCodeUrl();
                                    int countDown = mpProjection.getCountdown();
                                    if (activity instanceof AdsPlayerActivity){
                                        if (AppUtils.isSVT() && GlobalValues.mIsGoneToTv) {
                                            return;
                                        }
                                        if (luckyDrawDialog !=null){
                                            luckyDrawDialog.dismiss();
                                            luckyDrawDialog = new LuckyDrawDialog(context);
                                            luckyDrawDialog.show();
                                            luckyDrawDialog.showLuckDrawWindow(headPic,nickName,countDown,luckQrcodeUrl);
                                        }
                                    }
                                }
                            });
                            break;
                        case 140:
                            finishEvaluate(mpProjection);
                            break;
                        case 150:
                            activity = ActivitiesManager.getInstance().getCurrentActivity();
                            if (activity instanceof ScreenProjectionActivity){
                                return;
                            }
                            guideImage();
                            break;
                        case 151:
                            juhuasuanShow();
                            break;
                        case 152:
                            juhuasuanResult();
                            break;
                        case 160:
                            activity = ActivitiesManager.getInstance().getCurrentActivity();
                            if (activity instanceof ScreenProjectionActivity) {
                                exitProjection();
                            }
                            Thread.sleep(500);
                            if (AppUtils.isSVT() && GlobalValues.mIsGoneToTv) {
                                return;
                            }
                            showBusinessCard();
                            break;
                        case 170:
                            activity = ActivitiesManager.getInstance().getCurrentActivity();
                            if (activity instanceof ScreenProjectionActivity) {
                                exitProjection();
                            }
                            Thread.sleep(500);
                            if (AppUtils.isSVT() && GlobalValues.mIsGoneToTv) {
                                return;
                            }
                            showBusinessFile();
                            break;
                        case 171:
                            new Thread(this::preDownloadResource).start();
                            break;
                        case 999:
                            testNetDownload(mpProjection);
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
    /**一旦有netty请求进来，就关闭正在展示的dialog*/
    private void removeDialog(){
        if (cardDialog!=null&&cardDialog.isShowing()){
            cardDialog.dismiss();
        }
        if (fileDialog!=null&&fileDialog.isShowing()){
            fileDialog.dismiss();
        }
        if (partakeDishDialog!=null&&partakeDishDialog.isShowing()){
            partakeDishDialog.dismiss();
        }
        if (juhuasuanShowDialog!=null&&juhuasuanShowDialog.isShowing()){
            juhuasuanShowDialog.dismiss();
        }
        if (juhuasuanResultDialog!=null&&juhuasuanResultDialog.isShowing()){
            juhuasuanResultDialog.dismiss();
        }
    }

    /**如果当前用户正在投屏，则不允许销售端打断**/
    private boolean checkIsUserProjection(int action){
        boolean flag=false;
        if (action==42||action==44||action==130){
            Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
            if ((currentAction==2||currentAction==4||currentAction==6||currentAction==10)
                    &&activity instanceof ScreenProjectionActivity){
                flag = true;
            }
        }
        return flag;
    }

    @Override
    public void onMiniConnected() {
        //TODO:当建立NETTY连接以后请求接口获取小程序地址
        LogUtils.i("CurrentActivity.................." + ActivitiesManager.getInstance().getCurrentActivity());
        session.setHeartbeatMiniNetty(true);
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof AdsPlayerActivity){
            if (AppUtils.isSVT()&&GlobalValues.mIsGoneToTv){
                return;
            }
            handler.post(()->((AdsPlayerActivity) activity).toCheckMediaIsShowMiniProgramIcon());

        }
    }

    private void projectShowImage(int currentIndex,String words,String avatarUrl,String nickName){
        isPPTRunnable = true;
        if (GlobalValues.PROJECT_IMAGES!=null&&GlobalValues.PROJECT_IMAGES.size()>0){
            if (currentAction==4||currentAction==10||currentAction==11||currentAction==14||currentAction==142){
                boolean flag = true;
                LogUtils.d("PROJECT_IMAGES:flag=true|currentIndex="+currentIndex);
                if (GlobalValues.PROJECT_IMAGES.size()>currentIndex){
                    LogUtils.d("projectionIdMap>2>projectionIdMap="+projectionIdMap);
                    LogUtils.d("projectionIdMap>2>forscreen_id="+forscreen_id);
                    if (projectionIdMap!=null
                            &&!TextUtils.isEmpty(forscreen_id)
                            &&projectionIdMap.containsKey(forscreen_id)
                            &&PROJECTION_STATE_PLAY.equals(projectionIdMap.get(forscreen_id))){
                        String uri = GlobalValues.PROJECT_IMAGES.get(currentIndex);
                        //当前这个骚操作。。。是为了在投屏activity中获取当前展示的图片相关信息
                        if (mpProjection!=null){
                            ProjectionImg projectionImg = mpProjection.getImg_list().get(currentIndex);
                            if (projectionImg!=null){
                                mpProjection.setFilename(projectionImg.getFilename());
                            }
                        }
                        if (currentIndex==0&&mpProjection.getImg_slide()==0){
                            ProjectOperationListener.getInstance(context).showImage(1,uri,true,forscreen_id,words,avatarUrl,nickName,"",musicPath,currentAction, FROM_SERVICE_MINIPROGRAM);
                        }else {
                            ProjectOperationListener.getInstance(context).showImage(1,uri,false,forscreen_id,words,avatarUrl,nickName,"","",currentAction, FROM_SERVICE_MINIPROGRAM);
                        }

                        DOWNLOAD_TIME = 0;
                    }

                }else{
                    flag = false;
                }
                if (flag){
                    LogUtils.d("mProjectShowImageRunnable(forscreen_id):forscreen_id="+forscreen_id);
                    handler.postDelayed(mProjectShowImageRunnable,INTERVAL_TIME);
                }else {
                    if (GlobalValues.PROJECT_IMAGES!=null
                            &&GlobalValues.PROJECT_IMAGES.size()<img_nums
                            &&DOWNLOAD_TIME<1000*60*2){
                        this.currentIndex --;
                        DOWNLOAD_TIME = DOWNLOAD_TIME+INTERVAL_TIME;
                        LogUtils.d("PROJECT_IMAGES:flag=false|currentIndex="+currentIndex);
                        handler.postDelayed(mProjectShowImageRunnable,INTERVAL_TIME);
                    }else{
                        //进此逻辑证明下载完成或者下载过慢退出投屏状态
                        handler.post(mProjectExitDownloadRunnable);
                    }


                }

            }else if (currentAction==44||currentAction==137){
                LogUtils.d("playTimes=="+playTimes+"|投屏="+currentIndex);
                if (playTimes>0){
                    if (currentIndex>=GlobalValues.PROJECT_IMAGES.size()){
                        currentIndex =0;
                        this.currentIndex = currentIndex;
                    }
                }else{
                    if (currentIndex>=GlobalValues.PROJECT_IMAGES.size()){
                        playTimes = 0;
                        handler.removeCallbacks(mProjectShowImageRunnable);
                        //进此逻辑证明下载完成或者下载过慢退出投屏状态
                        handler.post(mProjectExitDownloadRunnable);
                        return;
                    }
                }

                if (projectionIdMap!=null
                        &&!TextUtils.isEmpty(forscreen_id)
                        &&projectionIdMap.containsKey(forscreen_id)
                        &&PROJECTION_STATE_PLAY.equals(projectionIdMap.get(forscreen_id))){
                    String uri = GlobalValues.PROJECT_IMAGES.get(currentIndex);
                    if (currentAction==44){
                        ProjectOperationListener.getInstance(context).showRestImage(4,uri,0,false,words,avatarUrl,nickName,playTimes, FROM_SERVICE_MINIPROGRAM);
                    }else if (currentAction==137){
                        ProjectOperationListener.getInstance(context).showBusinessImage(9,false,uri,words,wordSize,wordColor,fontPath,musicPath, playTimes,FROM_SERVICE_MINIPROGRAM);
                    }
                }

                if (img_nums>1){
                    if (currentAction==137){
                        handler.postDelayed(mProjectShowImageRunnable,INTERVAL_TIME);
                    }else {
                        handler.postDelayed(mProjectShowImageRunnable,REST_INTERVAL_TIME);
                    }
                }

            }
        }
    }

    private Runnable mProjectShowImageRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPPTRunnable){
                currentIndex ++;
                LogUtils.d("projectShowImage:forscreen_id="+forscreen_id);
                projectShowImage(currentIndex,words,headPic,nickName);
            }
        }
    };



    private Runnable mProjectExitDownloadRunnable = new Runnable() {
        @Override
        public void run() {
            if (pImgListDialog!=null&&pImgListDialog.isShowing()){
                pImgListDialog.clearContent();
                pImgListDialog.dismiss();
            }
        }
    };

    @Override
    public void onMiniCloseIcon() {
        ((SavorApplication) getApplication()).hideMiniProgramQrCodeWindow();
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity!=null && activity instanceof AdsPlayerActivity){
            if (AppUtils.isSVT()&&GlobalValues.mIsGoneToTv){
                return;
            }
            ((AdsPlayerActivity) activity).toCheckMediaIsShowMiniProgramIcon();
        }
    }


    ApiRequestListener apiRequestListener = new ApiRequestListener() {
        @Override
        public void onSuccess(AppApi.Action method, Object obj) {
            switch (method){
                case CP_GET_MINIPROGRAM_PROJECTION_RESOURCE_JSON:
                    LogUtils.d("54321=图片下载成功上报");
                    break;
            }
        }

        @Override
        public void onError(AppApi.Action method, Object obj) {

        }

        @Override
        public void onNetworkFailed(AppApi.Action method) {

        }
    };
    /**
     * 上传小程序投屏参数到云
     * */
    private void postMiniProgramGameParam(int action,MiniProgramProjection programProjection){
        if (programProjection==null){
            return;
        }
        HashMap<String,Object> params = new HashMap<>();
        switch (action){
            case 101:
                params.put("action","1");
                params.put("activity_id",programProjection.getActivity_id());
                params.put("order_time",System.currentTimeMillis());
                postProjectionGamesLog(params);
                break;
            case 102:
                params.put("action","3");
                params.put("activity_id",programProjection.getActivity_id());
                params.put("order_time",System.currentTimeMillis());
                postProjectionGamesLog(params);
                break;

            case 103:
                params.put("action","2");
                params.put("activity_id",programProjection.getActivity_id());
                params.put("openid",programProjection.getOpenid());
                params.put("order_time",System.currentTimeMillis());
                postProjectionGamesLog(params);
                break;
        }
    }



    /**
     * 小程序投屏日志统计接口，不在区分资源类型
     * @param params
     */
    private void postProjectionResourceLog(HashMap<String,Object> params){
        String box_downetime = System.currentTimeMillis()+"";
        params.put("box_downstime",box_downstime);
        params.put("box_downetime",box_downetime);
        AppApi.postProjectionResourceParam(context,apiRequestListener,params);
    }

    private void postWelcomePlayLog(String welcomeId){
        AppApi.postWelcomePlayAdsLog(context,apiRequestListener,welcomeId,session.getEthernetMac());
    }

    /**
     * 上报投屏前置后置广告日志
     * @param adsId
     */
    private void postForscreenAdsLog(String adsId){
        AppApi.postForscreenAdsLog(context,apiRequestListener,adsId,session.getEthernetMac());
    }

    /**
     * 互动游戏数据上传
     * */
    private void postProjectionGamesLog(HashMap<String,Object> params){
        AppApi.postProjectionGamesParam(context,apiRequestListener,params);
    }

    public interface DownloadProgressListener{
        void getDownloadProgress(long currentSize, long totalSize);
        void getDownloadProgress(String progress);
    }

    private void projectionFileImg(final MiniProgramProjection minipp){
        if (minipp == null||TextUtils.isEmpty(minipp.getUrl())) {
            return;
        }
        boolean isDownloaded = false;
        String forscreen_id = minipp.getForscreen_id();
        //更新投屏状态
        updateProjectionState(forscreen_id);

        String fileName = minipp.getFilename();
        String url = minipp.getUrl();
        String openid = minipp.getOpenid();
        String playTimes = minipp.getPlay_times()==0 ? "0":String.valueOf(minipp.getPlay_times());
        long startTime = System.currentTimeMillis();
        LogUtils.d("-|-|开始下载时间"+startTime);
        HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac", session.getEthernetMac());
        params.put("req_id",minipp.getReq_id());
        params.put("forscreen_id", forscreen_id);
        params.put("resource_id", minipp.getImg_id());
        params.put("openid", openid);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (pImgListDialog!=null){
                    if (!pImgListDialog.isShowing()){
                        pImgListDialog.show();
                        pImgListDialog.setProjectionPersonInfo(headPic,nickName);
                    }
                    pImgListDialog.clearContent();
                    ArrayList<ProjectionImg> list = new ArrayList<>();
                    ProjectionImg img = new ProjectionImg();
                    img.setImg_id(minipp.getImg_id());
                    img.setUrl(minipp.getUrl());
                    list.add(img);
                    pImgListDialog.setContent(list,TYPE_IMG);
                }
            }
        });
        String path=null;
        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
        int small_app_id = minipp.getSmall_app_id();
        if (small_app_id==2){
            if (fileName.contains("_")){
                String[] fileDirs = fileName.split("_");
                path = basePath +fileDirs[1]+"/"+ fileDirs[2];
            }
            if (!new File(path).exists()){
                path = basePath + fileName;
            }

        }else{
            path = basePath + fileName;
        }
        File file = new File(path);
        if (path!=null&&file.exists()) {
            isDownloaded = true;
            LogUtils.d("MiniProgramNettyService:投屏视频已存在");
            params.put("is_exist", 1);
            handler.post(()->pImgListDialog.setImgDownloadProgress(minipp.getVideo_id(),"100%"));

        } else {
            LogUtils.d("MiniProgramNettyService:需要下载视频的url=="+url);
            LogUtils.d("MiniProgramNettyService:file=="+file.getAbsolutePath());
            params.put("is_exist", 0);
            if (session.isWhether4gNetwork()){
                try {
                    String oss_url = BuildConfig.OSS_ENDPOINT+url;
                    isDownloaded = new ProjectionDownloader(context,oss_url, basePath,fileName,true,serial_number).downloadByRange();
                    if (isDownloaded){
                        handler.post(()->pImgListDialog.setImgDownloadProgress(minipp.getVideo_id(),"100%"));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{
                OSSUtils ossUtils = new OSSUtils(context,BuildConfig.OSS_BUCKET_NAME,url,file,true);
                ossUtils.setDownloadProgressListener(new DownloadProgressListener() {

                    @Override
                    public void getDownloadProgress(String progress) {

                    }

                    @Override
                    public void getDownloadProgress(final long currentSize, final long totalSize) {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                BigDecimal b = new BigDecimal(currentSize * 1.0 / totalSize);
                                Log.d("circularProgress", "原始除法得到的值" + currentSize * 1.0 / totalSize);
                                float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                                Log.d("circularProgress", "保留两位小数得到的值" + f1);
                                if (f1 >= 0.01f) {
                                    String value = String.valueOf(f1 * 100);
                                    int progress = Integer.valueOf(value.split("\\.")[0]);
                                    Log.d("circularProgress", "乘以100并且转成整数得到的值" + progress);
                                    pImgListDialog.setImgDownloadProgress(minipp.getVideo_id(),progress+"%");
                                }

                            }
                        });
                    }
                });
                LogUtils.d("12345+:开始下载投屏视频的url="+url);
                isDownloaded = ossUtils.syncNativeOSSDownload();
            }
        }
        if (isDownloaded) {
            LogUtils.d("12345+下载完成="+url);
            long endTime = System.currentTimeMillis();
            long downloadTime =  endTime- startTime;
            LogUtils.d("-|-|结束下载时间"+endTime);
            LogUtils.d("-|-|下载所用时间"+downloadTime);
            params.put("used_time", downloadTime);
            if (projectionIdMap!=null&&projectionIdMap.size()>0&&!TextUtils.isEmpty(forscreen_id)){
                params.put("is_break",projectionIdMap.get(forscreen_id));
            }
            postProjectionResourceLog(params);
            LogUtils.d("12345+:postProjectionResourceLog="+params);
            MobclickAgent.onEvent(context, "screenProjctionDownloadSuccess" + file.getName());
            if (GlobalValues.FILE_NUM.containsKey(openid)){
                if (GlobalValues.FILE_NUM.get(openid)!=-1){
                    GlobalValues.FILE_NUM.put(openid,GlobalValues.FILE_NUM.get(openid)+1);
                }
            }else{
                GlobalValues.FILE_NUM.put(openid,1);
            }
            GlobalValues.PROJECT_IMAGES.clear();
            GlobalValues.PROJECT_FAIL_IMAGES.clear();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (pImgListDialog!=null){
                        pImgListDialog.clearContent();
                        pImgListDialog.dismiss();
                    }
                }
            });
            ProjectOperationListener.getInstance(context).showImage(2, path, true,forscreen_id,playTimes, headPic, nickName,currentAction, FROM_SERVICE_MINIPROGRAM);
        } else {
            params.put("is_exist", 2);
            long endTime = System.currentTimeMillis();
            long downloadTime =  endTime- startTime;
            params.put("used_time", downloadTime);
            if (projectionIdMap!=null&&projectionIdMap.size()>0&&!TextUtils.isEmpty(forscreen_id)){
                params.put("is_break",projectionIdMap.get(forscreen_id));
            }
            postProjectionResourceLog(params);

            MobclickAgent.onEvent(context, "screenProjctionDownloadError" + file.getName());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (pImgListDialog!=null){
                        pImgListDialog.clearContent();
                        pImgListDialog.dismiss();
                    }
                }
            });
        }
    }

    private void projectionRotateImg(){
        LogUtils.d("旋转图片|openid:"+openid+"|lastOpenid="+lastOpenid);
        try {
            if (lastOpenid.equals(openid)){
                ProjectOperationListener.getInstance(context).rotate(90, GlobalValues.CURRENT_PROJECT_ID);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 点击单张图片或者投视频
     * @param minipp
     */
    private void projectionVideo(final MiniProgramProjection minipp){
        if (minipp == null||TextUtils.isEmpty(minipp.getUrl())) {
            return;
        }
        boolean isDownloaded = false;
        String forscreen_id = minipp.getForscreen_id();
        //更新投屏状态
        updateProjectionState(forscreen_id);

        int rotation = minipp.getRotation();
        LogUtils.d("12345+miniProgramProjection="+minipp);
        LogUtils.d("12345+downloadVideoIdMap="+projectionIdMap);
        String fileName = minipp.getFilename();
        long resourceSize = minipp.getResource_size();
        String url = minipp.getUrl();
        String openid = minipp.getOpenid();
        String req_id = minipp.getReq_id();
        long startTime = System.currentTimeMillis();
        LogUtils.d("-|-|开始下载时间"+startTime);
        HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac", session.getEthernetMac());
        params.put("req_id",req_id);
        params.put("forscreen_id", forscreen_id);
        params.put("resource_id", minipp.getVideo_id());
        params.put("openid", openid);
        String basePath;
        if (currentAction==12){
            basePath = AppUtils.getFilePath(AppUtils.StorageFile.hot_content);
        }else if (currentAction == 15){
            basePath = AppUtils.getFilePath(AppUtils.StorageFile.local_life);
        }else{
            basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
        }
        String path = basePath + fileName;
        File file = new File(path);
        String oss_url = BuildConfig.OSS_ENDPOINT+url;
        if (file.exists()) {
            isDownloaded = true;
            LogUtils.d("MiniProgramNettyService:投屏视频已存在");
            params.put("is_exist", 1);

        } else {
            LogUtils.d("MiniProgramNettyService:需要下载视频的url=="+url);
            LogUtils.d("MiniProgramNettyService:file=="+file.getAbsolutePath());
            params.put("is_exist", 0);

        }

        LogUtils.d("12345+下载完成="+url);
        if (projectionIdMap!=null&&projectionIdMap.size()>0&&!TextUtils.isEmpty(forscreen_id)){
            params.put("is_break",projectionIdMap.get(forscreen_id));
        }
        postProjectionResourceLog(params);
        LogUtils.d("12345+:postProjectionResourceLog="+params);

        MobclickAgent.onEvent(context, "screenProjctionDownloadSuccess" + file.getName());
        resetConstant();
        if (currentAction==42){
            if (isDownloaded){
                ProjectOperationListener.getInstance(context).showRestVideo(basePath,oss_url,true, headPic, nickName,minipp.getPlay_times());
            }else{
                ProjectOperationListener.getInstance(context).showRestVideo(basePath,oss_url,true, headPic, nickName,minipp.getPlay_times());
            }
        }else {
            //記錄投屏次數
            if (GlobalValues.VIDEO_NUM.containsKey(openid)){
                if (GlobalValues.VIDEO_NUM.get(openid)!=-1){
                    GlobalValues.VIDEO_NUM.put(openid,GlobalValues.VIDEO_NUM.get(openid)+1);
                }
            }else{
                GlobalValues.VIDEO_NUM.put(openid,1);
            }
            if (GlobalValues.INTERACTION_ADS_PLAY!=0){
                //处理抢投
                GlobalValues.PROJECTION_VIDEO_PATH = path;
            }else {
                //正常投屏
                preOrNextAdsBean = AppUtils.getInteractionAds(context);
                if (preOrNextAdsBean!=null){
                    if (preOrNextAdsBean.getPlay_position()==1){
                        GlobalValues.INTERACTION_ADS_PLAY = 1;
                        String adspath = preOrNextAdsBean.getMediaPath();
                        String duration = preOrNextAdsBean.getDuration();
                        if (preOrNextAdsBean.getMedia_type()==1){
                            ProjectOperationListener.getInstance(context).showVideo(adspath, true,forscreen_id, true,duration,currentAction, FROM_SERVICE_MINIPROGRAM);
                        }else{
                            ProjectOperationListener.getInstance(context).showImage(5, adspath, true,forscreen_id, words, headPic, nickName,duration,"",currentAction, FROM_SERVICE_MINIPROGRAM);
                        }
                        postForscreenAdsLog(preOrNextAdsBean.getVid());
                        preOrNextAdsBean = null;
                        GlobalValues.PROJECTION_VIDEO_PATH = path;
                    }else if (preOrNextAdsBean.getPlay_position()==2){
                        GlobalValues.PROJECTION_VIDEO_PATH = null;
                        ProjectOperationListener.getInstance(context).showVideo(basePath,oss_url,true,forscreen_id, headPic, nickName, currentAction,FROM_SERVICE_MINIPROGRAM);
                    }
                }else{
                    GlobalValues.INTERACTION_ADS_PLAY = 0;
                    preOrNextAdsBean = null;
                    GlobalValues.PROJECTION_VIDEO_PATH = null;
                    boolean isBreak = projectionIsBreak(forscreen_id);
                    if (!isBreak){
                        if (isDownloaded){
                            ProjectOperationListener.getInstance(context).showVideo(path,"",true,forscreen_id, headPic, nickName,currentAction, FROM_SERVICE_MINIPROGRAM);
                        }else{
                            ProjectOperationListener.getInstance(context).showVideo(basePath,oss_url, true,forscreen_id, headPic, nickName, currentAction,FROM_SERVICE_MINIPROGRAM);
                        }
                        if (!TextUtils.isEmpty(minipp.getQrcode_url())){
                            ((SavorApplication) getApplication()).showGoodsQrCodeWindow(minipp.getQrcode_url(),"");
                        }
                    }
                }
            }
        }
    }
    /**
     * 投多张图片
     * @param mpp
     */
    private void projectionMoreImg(final MiniProgramProjection mpp,int action){
        if (mpp == null || mpp.getImg_list()==null||mpp.getImg_list().size()==0) {
            return;
        }
        words = mpp.getForscreen_char();
        img_nums = mpp.getImg_list().size();
        final String openid = mpp.getOpenid();
        String forscreen_id = mpp.getForscreen_id();
        //更新投屏状态
        updateProjectionState(forscreen_id);
        if (img_nums == 0) {
            return;
        }

        resetConstant();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (pImgListDialog!=null&&pImgListDialog.isShowing()){
                    pImgListDialog.clearContent();
                    pImgListDialog.dismiss();
                }
            }
        });

        LogUtils.d("当前PROJECT_IMAGES=" + GlobalValues.PROJECT_IMAGES.size());

        //----------------------------------------------------//
        String musicFilePath = AppUtils.getFilePath(AppUtils.StorageFile.welcome_resource);
        String music_id = mpProjection.getMusic_id();
        getMusicPathMethod(musicFilePath,music_id);
        ArrayList<ProjectionImg> imgList = new ArrayList<>();
        imgList.addAll(mpp.getImg_list());
        ProjectionImg img = imgList.get(0);
        String url = BuildConfig.OSS_ENDPOINT+img.getUrl()+ConstantValues.PROJECTION_IMG_THUMBNAIL_PARAM;
        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
        String fileName = img.getFilename();
        String imgpath = basePath + fileName;
        if (action==44){
            playTimes =mpp.getPlay_times();
            LogUtils.d("playTimes=="+playTimes+"|缩略图");
            ProjectOperationListener.getInstance(context).showRestImage(4,url,0,true,words,headPic,nickName,playTimes, FROM_SERVICE_MINIPROGRAM);
        }else{
            if (GlobalValues.IMG_NUM.containsKey(openid)){
                if (GlobalValues.IMG_NUM.get(openid)!=-1){
                    GlobalValues.IMG_NUM.put(openid,GlobalValues.IMG_NUM.get(openid)+1);
                }
            }else{
                GlobalValues.IMG_NUM.put(openid,1);
            }
            if (!session.isOpenInteractscreenad()&&!new File(imgpath).exists()){
                ProjectOperationListener.getInstance(context).showImage(1, url, false,forscreen_id, words, headPic, nickName, FROM_SERVICE_MINIPROGRAM);
            }
        }

        if (img_nums>1||(img_nums==1&&!new File(imgpath).exists())){

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (pImgListDialog!=null){
                        if (!pImgListDialog.isShowing()){
                            pImgListDialog.show();
                            pImgListDialog.setProjectionPersonInfo(headPic,nickName);
                        }
                        pImgListDialog.setContent(imgList,TYPE_IMG);
                    }
                }
            });
        }

        LogUtils.d("1212:isDownloadRunnable="+isDownloadRunnable);

        if (GlobalValues.INTERACTION_ADS_PLAY==0&&currentAction==4){
            preOrNextAdsBean = AppUtils.getInteractionAds(context);
            if (preOrNextAdsBean!=null){
                if (preOrNextAdsBean.getPlay_position()==1){
                    GlobalValues.INTERACTION_ADS_PLAY = 1;
                    String adspath = preOrNextAdsBean.getMediaPath();
                    String duration = preOrNextAdsBean.getDuration();
                    if (preOrNextAdsBean.getMedia_type()==1){
                        ProjectOperationListener.getInstance(context).showVideo(adspath,  true,forscreen_id, true,duration,currentAction, FROM_SERVICE_MINIPROGRAM);
                    }else{
                        ProjectOperationListener.getInstance(context).showImage(5, adspath, true,forscreen_id, words, headPic, nickName,duration,"",currentAction, FROM_SERVICE_MINIPROGRAM);
                    }
                    postForscreenAdsLog(preOrNextAdsBean.getVid());
                    preOrNextAdsBean = null;
                }
            }
        }
        handler.removeCallbacks(downloadFileRunnable);
        downloadIndex =0;
        new Thread(()->downloadFile(downloadIndex)).start();
    }
    /**发现，热播，喜欢，公开的多图投屏*/
    private void projectionListImg(MiniProgramProjection program){
        if (program == null || program.getImg_list()==null||program.getImg_list().size()==0) {
            return;
        }
        String forscreen_id = program.getForscreen_id();
        //更新投屏状态
        updateProjectionState(forscreen_id);
        String req_id = program.getReq_id();
        words = program.getForscreen_char();
        img_nums = program.getImg_nums();
        String musicFilePath = AppUtils.getFilePath(AppUtils.StorageFile.welcome_resource);
        String music_id = program.getMusic_id();
        getMusicPathMethod(musicFilePath,music_id);
        resetConstant();
        if (GlobalValues.IMG_NUM.containsKey(openid)){
            if (GlobalValues.IMG_NUM.get(openid)!=-1){
                GlobalValues.IMG_NUM.put(openid,GlobalValues.IMG_NUM.get(openid)+1);
            }
        }else{
            GlobalValues.IMG_NUM.put(openid,1);
        }
        ArrayList<ProjectionImg> imgList = new ArrayList<>();
        imgList.addAll(program.getImg_list());
        ProjectionImg img = imgList.get(0);
        String basePath;
        if (currentAction==11){
            basePath = AppUtils.getFilePath(AppUtils.StorageFile.hot_content);
        }else if (currentAction==14){
            basePath = AppUtils.getFilePath(AppUtils.StorageFile.local_life);
        }else{
            basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
        }
        String fileName = img.getFilename();
        String imgpath = basePath + fileName;
        if (!new File(imgpath).exists()){
            String url = BuildConfig.OSS_ENDPOINT+img.getUrl()+ConstantValues.PROJECTION_IMG_THUMBNAIL_PARAM;
            ProjectOperationListener.getInstance(context).showImage(1,url,false,forscreen_id,words, headPic, nickName, FROM_SERVICE_MINIPROGRAM);
        }
        if (img_nums>1||(img_nums==1&&!new File(imgpath).exists())){
            if (currentAction!=142){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (pImgListDialog!=null){
                            if (!pImgListDialog.isShowing()){
                                pImgListDialog.show();
                                pImgListDialog.setProjectionPersonInfo(headPic,nickName);
                            }
                            pImgListDialog.setContent(imgList,TYPE_IMG);
                        }
                    }
                });
            }
        }
        downloadIndex =0;
        new Thread(()->downloadFile(downloadIndex)).start();
    }

    private void downloadFile(int downloadIndex){
        boolean isDownloaded=false;
        if (mpProjection==null){
            return;
        }
        try{
            MiniProgramProjection projection = this.mpProjection;
            if (projection.getImg_list()==null||projection.getImg_list().size()==0){
                return;
            }
            ProjectionImg img = projection.getImg_list().get(downloadIndex);
            final HashMap params = new HashMap<>();
            params.put("req_id",projection.getReq_id());
            params.put("box_mac", session.getEthernetMac());
            params.put("forscreen_id", projection.getForscreen_id());
            params.put("openid", projection.getOpenid());
            params.put("resource_id", img.getImg_id());
            String basePath;
            if (currentAction==11){
                basePath = AppUtils.getFilePath(AppUtils.StorageFile.hot_content);
            }else if (currentAction==14){
                basePath = AppUtils.getFilePath(AppUtils.StorageFile.local_life);
            }else{
                basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
            }
            String fileName = img.getFilename();
            long resourceSize = img.getResource_size();
            box_downstime = System.currentTimeMillis()+"";
            String path = basePath + fileName;
            File file = new File(path);
            if(currentAction==142){
                if (file.exists()) {
                    isDownloaded = true;
                } else {
                    String oss_url = BuildConfig.OSS_ENDPOINT+img.getUrl();
                    ProjectionDownloader downloader = new ProjectionDownloader(context,oss_url, basePath,fileName,resourceSize,true,serial_number);
                    isDownloaded = downloader.downloadByRange();
                }
            }else{
                if (file.exists()) {
                    isDownloaded = true;
                    handler.post(()->pImgListDialog.setImgDownloadProgress(img.getImg_id(), "100%"));
                } else {
                    try {
                        String oss_url = BuildConfig.OSS_ENDPOINT+img.getUrl();
                        ProjectionDownloader downloader = new ProjectionDownloader(context,oss_url, basePath,fileName,resourceSize,true,serial_number);
                        downloader.setDownloadProgressListener(new DownloadProgressListener() {
                            @Override
                            public void getDownloadProgress(long currentSize, long totalSize) {
                            }
                            @Override
                            public void getDownloadProgress(String progress) {
                                handler.post(()->pImgListDialog.setImgDownloadProgress(img.getImg_id(),progress));
                            }
                        });
                        isDownloaded = downloader.downloadByRange();
                        if (resourceSize==0&&isDownloaded){
                            handler.post(()->pImgListDialog.setImgDownloadProgress(img.getImg_id(), "100%"));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
            if (isDownloaded) {
                boolean isBreaks = projectionIsBreak(projection.getForscreen_id());
                if (!GlobalValues.PROJECT_IMAGES.contains(path)&&!isBreaks) {
                    GlobalValues.PROJECT_IMAGES.add(path);
                }
                //每下载一张图片就上报一次
                postProjectionResourceLog(params);

                LogUtils.d("img_nums:="+img_nums
                        +"|||PROJECT_IMAGES:="+GlobalValues.PROJECT_IMAGES
                        +"|||PROJECT_FAIL_IMAGES:="+GlobalValues.PROJECT_FAIL_IMAGES);
                if (!isPPTRunnable&&GlobalValues.INTERACTION_ADS_PLAY==0){
                    handler.removeCallbacks(mProjectShowImageRunnable);
                    currentIndex =0;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LogUtils.d("projectShowImage:第一张forscreen_id="+projection.getForscreen_id());
                            projectShowImage(currentIndex, words, headPic, nickName);
                        }
                    }).start();
                }
                if (projectionIdMap != null && projectionIdMap.containsKey(projection.getForscreen_id())) {
                    String isBreak = projectionIdMap.get(projection.getForscreen_id());
                    params.put("is_break", isBreak);
                    if (isBreak.equals(PROJECTION_STATE_BREAK)){
                        postProjectionResourceLog(params);
                    }
                }
            } else {
                boolean isBreaks = projectionIsBreak(projection.getForscreen_id());
                if (!GlobalValues.PROJECT_FAIL_IMAGES.contains(path)&&!isBreaks) {
                    GlobalValues.PROJECT_FAIL_IMAGES.add(path);
                }
                LogUtils.d("img_nums:="+img_nums
                        +"|||PROJECT_IMAGES:="+GlobalValues.PROJECT_IMAGES
                        +"|||PROJECT_FAIL_IMAGES:="+GlobalValues.PROJECT_FAIL_IMAGES);
                params.put("is_exist", 2);
                if (projectionIdMap != null && projectionIdMap.containsKey(projection.getForscreen_id())) {
                    String isBreak = projectionIdMap.get(projection.getForscreen_id());
                    params.put("is_break", isBreak);
                    if (isBreak.equals(PROJECTION_STATE_BREAK)){
                        postProjectionResourceLog(params);
                    }
                }
            }
            if (GlobalValues.PROJECT_IMAGES.size()+GlobalValues.PROJECT_FAIL_IMAGES.size()==img_nums){
                handler.removeCallbacks(downloadFileRunnable);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pImgListDialog!=null&&pImgListDialog.isShowing()){
                            pImgListDialog.clearContent();
                            pImgListDialog.dismiss();
                        }
                    }
                },1000);
            }else{
                handler.postDelayed(downloadFileRunnable,500);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 下载文件
     * @return
     */
    private Runnable downloadFileRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtils.d("下载测试----进入线程");
            downloadIndex ++;
            new Thread(()->downloadFile(downloadIndex)).start();
        }
    };
    /**
     * 退出投屏
     */
    private void exitProjection(){
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (pImgListDialog!=null&&pImgListDialog.isShowing()){
                    pImgListDialog.clearContent();
                    pImgListDialog.dismiss();
                }
            }
        });
        GlobalValues.PROJECT_STREAM_IMAGE.clear();
        GlobalValues.PROJECT_STREAM_FAIL_IMAGE.clear();
        GlobalValues.PROJECT_THUMBNIAL_IMAGE.clear();
        ProjectOperationListener.getInstance(context).stop(GlobalValues.CURRENT_PROJECT_ID);
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (this.mpProjection!=null&&!TextUtils.isEmpty(forscreen_id)){
            projectionIdMap.put(mpProjection.getForscreen_id(),PROJECTION_STATE_BREAK);
        }
        if (activity instanceof ScreenProjectionActivity&&this.mpProjection!=null){
            try {
                HashMap<String, Object> params = new HashMap<>();
                params.put("box_mac", session.getEthernetMac());
                params.put("req_id",mpProjection.getReq_id());
                params.put("forscreen_id", forscreen_id);
                params.put("openid", openid);
                if (!TextUtils.isEmpty(mpProjection.getVideo_id())){
                    params.put("resource_id", mpProjection.getVideo_id());
                }else if (mpProjection.getImg_list()!=null&&mpProjection.getImg_list().size()>0){
                    List<ProjectionImg> list = mpProjection.getImg_list();
                    ProjectionImg img = list.get(currentIndex);
                    params.put("resource_id", img.getImg_id());
                }
                params.put("is_exit", 1);
                postProjectionResourceLog(params);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 点击机顶盒内视频
     * @param miniProgramProjection
     */
    private void onDemandSetTopBoxVideo(MiniProgramProjection miniProgramProjection){
        String fileName = miniProgramProjection.getFilename();
        String url = miniProgramProjection.getUrl();
        if (TextUtils.isEmpty(fileName)||TextUtils.isEmpty(url)){
            return;
        }
        String forscreen_id = miniProgramProjection.getForscreen_id();
        updateProjectionState(forscreen_id);
        resetConstant();
        HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac", session.getEthernetMac());
        params.put("forscreen_id", forscreen_id);
        params.put("req_id",miniProgramProjection.getReq_id());
        params.put("openid",openid);
        params.put("resource_id",miniProgramProjection.getResource_id());
        params.put("is_break",projectionIdMap.get(forscreen_id));
        String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
        String[] selectionArgs = new String[]{fileName};
        List<MediaLibBean> listPlayList = dbHelper.findNewPlayListByWhere(selection, selectionArgs);
        if (listPlayList==null||listPlayList.size()==0){
            listPlayList =  dbHelper.findAdsByWhere(selection, selectionArgs);
        }
        List<MediaLibBean> listActivityAds = dbHelper.findActivityAdsByWhere(selection, selectionArgs);
        if (listPlayList != null && listPlayList.size() > 0) {
            String path = AppUtils.getFilePath(AppUtils.StorageFile.media) + fileName;
            File file = new File(path);
            if (file.exists()) {
                params.put("is_exist",1);
                ProjectOperationListener.getInstance(context).showVod(fileName, "", 0, false, true,currentAction,FROM_SERVICE_MINIPROGRAM);
            }
        }  else if (listActivityAds != null &&listActivityAds.size()>0){
            String path = AppUtils.getFilePath(AppUtils.StorageFile.activity_ads) + fileName;
            File file = new File(path);
            if (file.exists()) {
                params.put("is_exist",1);
                ProjectOperationListener.getInstance(context).showVideo(path,"", true,currentAction,FROM_SERVICE_MINIPROGRAM);
            }
        }else {
            params.put("is_exist",0);
            ProjectOperationListener.getInstance(context).showVideo("",url, true,currentAction,FROM_SERVICE_MINIPROGRAM);
        }
        postProjectionResourceLog(params);
    }


    private void onDemandExtensionVideo(MiniProgramProjection miniProgramProjection){
        String fileName = miniProgramProjection.getFilename();
        String url = miniProgramProjection.getUrl();
        if (TextUtils.isEmpty(fileName)&&TextUtils.isEmpty(url)){
            return;
        }
        String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
        String[] selectionArgs = new String[]{fileName};
        List<MediaLibBean> listPlayList = dbHelper.findNewPlayListByWhere(selection, selectionArgs);
        if (listPlayList!=null&&listPlayList.size()>=1){
            ProjectOperationListener.getInstance(context).showVod(fileName, "", 0, false, true,currentAction,FROM_SERVICE_MINIPROGRAM);
        }else{
//            if (!TextUtils.isEmpty(url)){
//                url = BuildConfig.OSS_ENDPOINT+url;
//            }
            ProjectOperationListener.getInstance(context).showVideo("",url, true,currentAction,FROM_SERVICE_MINIPROGRAM);
        }
        String names[] = fileName.split("\\.");
        if (names.length!=2){
            return;
        }
        String mediaId = names[0];
        if (session.isShowAnimQRcode()){
            MiniProgramQrCodeWindowManager.get(context).setCurrentPlayMediaId(mediaId);
        }else{
            QrCodeWindowManager.get(context).setCurrentPlayMediaId(mediaId);
        }
        ((SavorApplication) getApplication()).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_QRCODE_HELP_TYPE);
    }
    /**霸王餐参与抽奖窗口展示**/
    private void involvementPartakedish(MiniProgramProjection mpp){
        int countdown = mpp.getCountdown();
        int lottery_countdown = mpp.getLottery_countdown();
        String lottery_time = mpp.getLottery_time();
        String partake_img = mpp.getPartake_img();
        String oss_url = BuildConfig.OSS_ENDPOINT+partake_img;
        String partake_name = mpp.getPartake_name();
        String activity_name = mpp.getActivity_name();
        String partake_filename = mpp.getPartake_filename();
        String codeUrl = mpp.getCodeUrl();
        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.lottery);
        String filePath = basePath+partake_filename;
        File file = new File(filePath);
        boolean downloaded;
        if (file.exists()){
            downloaded = true;
        }else{
            ProjectionDownloader downloader = new ProjectionDownloader(context,oss_url, basePath,partake_filename,true);
            downloaded = downloader.downloadByRange();
        }
        if (downloaded){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (partakeDishDialog.isShowing()){
                        partakeDishDialog.dismiss();
                    }
                    partakeDishDialog = new PartakeDishDialog(context);
                    String qrcodeurl = codeUrl;
                    if (TextUtils.isEmpty(qrcodeurl)){
                        qrcodeurl = AppApi.API_URLS.get(AppApi.Action.CP_MINIPROGRAM_DOWNLOAD_QRCODE_JSON)+"?box_mac="+ session.getEthernetMac()+"&type="+ ConstantValues.MINI_PROGRAM_QRCODE_PARTAKE_DISH_TYPE;
                    }
                    partakeDishDialog.show();
                    partakeDishDialog.showPartakeDishWindow(context,qrcodeurl,countdown,lottery_time);

                    Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
                    if (activity instanceof AdsPlayerActivity&&lottery_countdown!=0){
                        GlobalValues.isActivity = true;
                        AdsPlayerActivity adsPlayerActivity = (AdsPlayerActivity) activity;
                        adsPlayerActivity.isClosePartakeDishHead(true);
                        adsPlayerActivity.setPartakeDishCountdown(activity_name,lottery_countdown);
                        adsPlayerActivity.toCheckMediaIsShowMiniProgramIcon();
                    }
                }
            });

        }
    }
    /**霸王餐开奖**/
    private void partakedishResult(PartakeDishBean pdb){
        GlobalValues.isActivity = false;
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof PartakeDishDrawActivity){
            activity.finish();
        }
        Intent intent =new Intent();
        intent.setClass(context, PartakeDishDrawActivity.class);
        intent.putExtra("pdb",pdb);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    /**商务宴请欢迎词*/
    private void businessWelcome(){
        if (mpProjection == null || mpProjection.getImg_list()==null||mpProjection.getImg_list().size()==0) {
            return;
        }
        List<ProjectionImg> imgList = mpProjection.getImg_list();
        img_nums = imgList.size();
        if (img_nums == 0) {
            return;
        }
        playTimes = mpProjection.getPlay_times()*1000;
        words = mpProjection.getForscreen_char();
        wordSize = mpProjection.getWordsize();
        wordColor = mpProjection.getColor();
        /**
         * 注意：由于用户端欢迎词没有返回forscreen_id
         * 故在使用过程中通过id来当做forscreen_id
         * 在下面的这行代码全局赋值给forscreen_id
         * */
        forscreen_id = mpProjection.getId()+"";
        mpProjection.setForscreen_id(forscreen_id);
        String font_id = mpProjection.getFont_id();
        String music_id = mpProjection.getMusic_id();
        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.welcome_resource);
        if (!TextUtils.isEmpty(font_id)){
            String selection = DBHelper.MediaDBInfo.FieldName.ID + "=? ";
            String[] selectionArgs = new String[]{font_id};
            List<WelcomeResourceBean> typefaceList = dbHelper.findWelcomeResourceList(selection,selectionArgs);
            if (typefaceList!=null&&typefaceList.size()>0){
                WelcomeResourceBean bean = typefaceList.get(0);
                File file = new File(basePath+bean.getName());
                if (file.exists()){
                    fontPath = basePath+bean.getName();
                }
            }
        }
       getMusicPathMethod(basePath,music_id);
        //更新投屏状态
        updateProjectionState(forscreen_id);
        resetConstant();

        ProjectionImg img = imgList.get(0);
        String url = BuildConfig.OSS_ENDPOINT+img.getUrl()+ConstantValues.PROJECTION_IMG_THUMBNAIL_PARAM;
        ProjectOperationListener.getInstance(context).showBusinessImage(9,true,url,words,wordSize,wordColor,fontPath,musicPath, playTimes,FROM_SERVICE_MINIPROGRAM);
        downloadIndex = 0;
        new Thread(()->downloadFileNoDialog(downloadIndex)).start();
    }


    private void getMusicPathMethod(String basePath,String music_id){
        if (!TextUtils.isEmpty(music_id)&&!"0".equals(music_id)){
            String selection = DBHelper.MediaDBInfo.FieldName.ID + "=? ";
            String[] selectionArgs = new String[]{music_id};
            List<WelcomeResourceBean> musicList = dbHelper.findWelcomeResourceList(selection,selectionArgs);
            if (musicList!=null&&musicList.size()>0){
                WelcomeResourceBean bean = musicList.get(0);
                musicPath = basePath+bean.getName();
                File file = new File(musicPath);
                if (!file.exists()){
                    musicPath = BuildConfig.OSS_ENDPOINT + mpProjection.getMusic_oss_addr();
                }
            }else{
                musicPath = BuildConfig.OSS_ENDPOINT + mpProjection.getMusic_oss_addr();
            }
        }
    }


    private void downloadFileNoDialog(int downloadIndex){
        boolean isDownloaded=false;
        if (mpProjection==null){
            return;
        }
        try{
            MiniProgramProjection projection = this.mpProjection;
            if (projection.getImg_list()==null||projection.getImg_list().size()==0){
                return;
            }
            ProjectionImg img = projection.getImg_list().get(downloadIndex);
            final HashMap params = new HashMap<>();
            params.put("req_id",projection.getReq_id());
            params.put("box_mac", session.getEthernetMac());
            params.put("forscreen_id", forscreen_id);
            params.put("openid", projection.getOpenid());
            params.put("resource_id", img.getImg_id());
            String basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
            String fileName = img.getFilename();
            String path = basePath + fileName;
            File file = new File(path);
            if (file.exists()) {
                isDownloaded = true;
            } else {
                try {
                    String oss_url = BuildConfig.OSS_ENDPOINT+img.getUrl();
                    ProjectionDownloader downloader = new ProjectionDownloader(context,oss_url, basePath,fileName,true,serial_number);
                    isDownloaded = downloader.downloadByRange();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (isDownloaded) {
                boolean isBreaks = projectionIsBreak(forscreen_id);
                if (!GlobalValues.PROJECT_IMAGES.contains(path)&&!isBreaks) {
                    GlobalValues.PROJECT_IMAGES.add(path);
                }
                if (GlobalValues.PROJECT_IMAGES.size()==1){
                    postProjectionResourceLog(params);
                }
                LogUtils.d("img_nums:="+img_nums
                        +"|||PROJECT_IMAGES:="+GlobalValues.PROJECT_IMAGES
                        +"|||PROJECT_FAIL_IMAGES:="+GlobalValues.PROJECT_FAIL_IMAGES);
                if (!isPPTRunnable){
                    handler.removeCallbacks(mProjectShowImageRunnable);
                    currentIndex =0;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LogUtils.d("projectShowImage:第一张forscreen_id="+projection.getForscreen_id());
                            projectShowImage(currentIndex, words, headPic, nickName);
                        }
                    }).start();
                }
                if (projectionIdMap != null && projectionIdMap.containsKey(forscreen_id)) {
                    String isBreak = projectionIdMap.get(forscreen_id);
                    params.put("is_break", isBreak);
                    if (isBreak.equals(PROJECTION_STATE_BREAK)){
                        postProjectionResourceLog(params);
                    }
                }
            } else {
                boolean isBreaks = projectionIsBreak(forscreen_id);
                if (!GlobalValues.PROJECT_FAIL_IMAGES.contains(path)&&!isBreaks) {
                    GlobalValues.PROJECT_FAIL_IMAGES.add(path);
                }
                LogUtils.d("img_nums:="+img_nums
                        +"|||PROJECT_IMAGES:="+GlobalValues.PROJECT_IMAGES
                        +"|||PROJECT_FAIL_IMAGES:="+GlobalValues.PROJECT_FAIL_IMAGES);
                params.put("is_exist", 2);
                if (projectionIdMap != null && projectionIdMap.containsKey(forscreen_id)) {
                    String isBreak = projectionIdMap.get(forscreen_id);
                    params.put("is_break", isBreak);
                    if (isBreak.equals(PROJECTION_STATE_BREAK)){
                        postProjectionResourceLog(params);
                    }
                }
            }
            if (GlobalValues.PROJECT_IMAGES.size()+GlobalValues.PROJECT_FAIL_IMAGES.size()==img_nums){
                handler.removeCallbacks(downloadFileNoDialogRunnable);
            }else{
                handler.postDelayed(downloadFileNoDialogRunnable,500);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 下载文件
     * @return
     */
    private Runnable downloadFileNoDialogRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtils.d("下载测试----进入线程");
            downloadIndex ++;
            new Thread(()->downloadFileNoDialog(downloadIndex)).start();
        }
    };

    /**
     * 点播生日歌相关
     * @param miniProgramProjection
     */
    private void onDemandBirthdayVideo(MiniProgramProjection miniProgramProjection){
        forscreen_id = miniProgramProjection.getForscreen_id();
        updateProjectionState(forscreen_id);
        resetConstant();
        long startTime = System.currentTimeMillis();
        HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac", session.getEthernetMac());
        params.put("forscreen_id", forscreen_id);
        params.put("req_id",miniProgramProjection.getReq_id());
        params.put("openid",openid);
        params.put("is_break",projectionIdMap.get(forscreen_id));
        String fileName = miniProgramProjection.getFilename();
        String url = miniProgramProjection.getUrl();
        String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
        String[] selectionArgs = new String[]{fileName};
        List<BirthdayOndemandBean> list = DBHelper.get(context).findBirthdayOndemandByWhere(selection,selectionArgs);
        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.birthday_ondemand);
        if (list!=null&&list.size()>0){
            String path = basePath + fileName;
            File file = new File(path);
            if (file.exists()){
                params.put("is_exist","1");
                ProjectOperationListener.getInstance(context).showVideoBirthday(null,path,forscreen_id, currentAction, FROM_SERVICE_MINIPROGRAM);
            }
        }else{
            params.put("is_exist","0");
            ProjectOperationListener.getInstance(context).showVideoBirthday(url,basePath,forscreen_id, currentAction, FROM_SERVICE_MINIPROGRAM);
        }
        postProjectionResourceLog(params);
    }


    private void onDemandGoodsVideo(MiniProgramProjection miniProgramProjection){
        forscreen_id = miniProgramProjection.getForscreen_id();
        updateProjectionState(forscreen_id);
        HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac", session.getEthernetMac());
        params.put("forscreen_id", forscreen_id);
        params.put("req_id",miniProgramProjection.getReq_id());
        params.put("openid",openid);
        params.put("is_break",projectionIdMap.get(forscreen_id));
        String goods_id = miniProgramProjection.getGoods_id();
        String url = BuildConfig.OSS_ENDPOINT+miniProgramProjection.getUrl();
        String qrcode_url = miniProgramProjection.getCodeUrl();
        String selection = DBHelper.MediaDBInfo.FieldName.GOODS_ID + "=? ";
        String[] selectionArgs = new String[]{goods_id};
        List<MediaLibBean> list = DBHelper.get(context).findShopGoodsAdsByWhere(selection,selectionArgs);
        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.goods_ads);
        boolean isExit = false;
        String mediaPath = null;
        if (list!=null&&list.size()>0){
            MediaLibBean bean = list.get(0);
            mediaPath = bean.getMediaPath();
            File file = new File(mediaPath);
            if (file.exists()){
                isExit = true;
            }
        }
        if (isExit){
            params.put("is_exist","1");
            ProjectOperationListener.getInstance(context).showVideo(mediaPath,"", true,currentAction,FROM_SERVICE_MINIPROGRAM);
        }else{
            params.put("is_exist","0");
            ProjectOperationListener.getInstance(context).showVideo("",url, true,currentAction,FROM_SERVICE_MINIPROGRAM);
        }
        ((SavorApplication) getApplication()).showGoodsQrCodeWindow(qrcode_url,"");
    }
    /**
     *通过小程序呼出展示大码的视频
     */
    private void callBigQrCodeVideo(MiniProgramProjection program){
        String forscreen_id = program.getForscreen_id();
        updateProjectionState(forscreen_id);
        String filename = program.getFilename();
        long startTime = System.currentTimeMillis();
        HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac", session.getEthernetMac());
        params.put("forscreen_id", forscreen_id);
        params.put("req_id",program.getReq_id());
        params.put("openid",openid);
        params.put("resource_id",ConstantValues.QRCODE_CALL_VIDEO_ID);
        params.put("is_exist","1");
        if (!TextUtils.isEmpty(forscreen_id)){
            params.put("is_break",projectionIdMap.get(forscreen_id));
        }
        String basePath = AppUtils.getSDCardPath()+AppUtils.Download;
        File file = new File(basePath,filename);
        if (file.exists()) {
            ProjectOperationListener.getInstance(context).showMiniProgramCode(filename,currentAction, FROM_SERVICE_MINIPROGRAM);
            long endTime = System.currentTimeMillis();
            long downloadTime =  endTime- startTime;
            params.put("used_time",downloadTime);
            postProjectionResourceLog(params);
        }

    }

    /**
     * 发起小游戏
     * @param action
     * @param miniProgramProjection
     */
    private void launchMonkeyGame(int action,MiniProgramProjection miniProgramProjection){
        GlobalValues.INTERACTION_ADS_PLAY = 0;
        Intent intent = new Intent(context, MonkeyGameActivity.class);
        intent.putExtra("miniProgramProjection", miniProgramProjection);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        postMiniProgramGameParam(action, miniProgramProjection);
    }

    /**
     * 开始互动小游戏
     * @param action
     * @param miniProgramProjection
     */
    private void startMonkeyGame(int action,MiniProgramProjection miniProgramProjection){
        if (ActivitiesManager.getInstance().getCurrentActivity() instanceof MonkeyGameActivity) {
            MonkeyGameActivity activity = (MonkeyGameActivity) ActivitiesManager.getInstance().getCurrentActivity();
            activity.startGame();
            if (action != 105) {
                postMiniProgramGameParam(action, miniProgramProjection);
            }
        }
    }
    /**
     * 再开一局
     * */
    private void repeatMonkeyGame(int action,MiniProgramProjection miniProgramProjection){
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof MonkeyGameActivity){
            MonkeyGameActivity monkeyGameActivity = (MonkeyGameActivity)activity;
            monkeyGameActivity.startGame();
        }else{
            GlobalValues.INTERACTION_ADS_PLAY = 0;
            Intent intent = new Intent(context, MonkeyGameActivity.class);
            intent.putExtra("miniProgramProjection", miniProgramProjection);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            postMiniProgramGameParam(action, miniProgramProjection);
        }
    }

    /**
     * 加入互动小游戏
     * @param action
     * @param miniProgramProjection
     */
    private void addMonkeyGame(int action,MiniProgramProjection miniProgramProjection){
        if (ActivitiesManager.getInstance().getCurrentActivity() instanceof MonkeyGameActivity) {
            MonkeyGameActivity activity = (MonkeyGameActivity) ActivitiesManager.getInstance().getCurrentActivity();
            activity.addWeixinAvatarToGame(miniProgramProjection);
            postMiniProgramGameParam(action, miniProgramProjection);
        }
    }

    /**
     * 退出互动小游戏
     */
    private void exitMonkeyGame(){
        if (ActivitiesManager.getInstance().getCurrentActivity() instanceof MonkeyGameActivity) {
            MonkeyGameActivity activity = (MonkeyGameActivity) ActivitiesManager.getInstance().getCurrentActivity();
            activity.exitGame();
        }
    }
    /**
     * 启动h5游戏页面
     */
    private void startWebviewGame(String url){
        if (!TextUtils.isEmpty(url)){
            GlobalValues.INTERACTION_ADS_PLAY = 0;
            ((SavorApplication) getApplication()).hideMiniProgramQrCodeWindow();
            ((SavorApplication) getApplication()).hideGoodsQrCodeWindow();
            ((SavorApplication) getApplication()).hideGoodsCountdownQrCodeWindow();
            Intent intent = new Intent(context, WebviewGameActivity.class);
            intent.putExtra("url", url);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    /**
     * 退出h5游戏页面
     */
    private void exitWebviewGame(){
        if (ActivitiesManager.getInstance().getCurrentActivity() instanceof WebviewGameActivity) {
            WebviewGameActivity activity = (WebviewGameActivity) ActivitiesManager.getInstance().getCurrentActivity();
            activity.exitWebview();
        }
    }

    /**
     * 测试网速专用
     * @param miniProgramProjection
     */
    private void testNetDownload(MiniProgramProjection miniProgramProjection){
        if (miniProgramProjection == null) {
            return;
        }
        HashMap params = new HashMap<>();
        params.put("box_mac", session.getEthernetMac());
        params.put("req_id",miniProgramProjection.getReq_id());
        params.put("forscreen_id", miniProgramProjection.getForscreen_id());
        params.put("resource_id", miniProgramProjection.getVideo_id());
        params.put("openid", miniProgramProjection.getOpenid());
        params.put("is_exist", "0");
        params.put("is_break","0");
        String fileName = miniProgramProjection.getFilename();
        String url = miniProgramProjection.getUrl();
        if (TextUtils.isEmpty(url)||TextUtils.isEmpty(fileName)) {
            return;
        }
        String path = AppUtils.getFilePath(AppUtils.StorageFile.projection) + fileName;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        long startTime = System.currentTimeMillis();
        OSSUtils ossUtils = new OSSUtils(context,
                BuildConfig.OSS_BUCKET_NAME,
                url,
                file,true);

        if (ossUtils.syncNativeOSSDownload()) {
            params.put("used_time", System.currentTimeMillis() - startTime);
            postProjectionResourceLog(params);
        }
    }

    /**
     *
     * @param value 1:减音量 2：加音量
     */
    private void adjustVoice(int value){
            switch (value){
            case 1:
                ProjectOperationListener.getInstance(context).volume(3,GlobalValues.CURRENT_PROJECT_ID);
                break;
            case 2:
                ProjectOperationListener.getInstance(context).volume(4,GlobalValues.CURRENT_PROJECT_ID);
            break;
        }

    }

    /**
     * 切换节目
     * @param value 1:上一个节目,2:下一个节目
     */
    private void adjustVideo(int value){
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if(activity instanceof ScreenProjectionActivity){
            activity.finish();
        }
        if (activity instanceof AdsPlayerActivity){
            handler.post(()-> ((AdsPlayerActivity) activity).changeMedia(value));
        }
    }


    private void onDemandGoodsAds(int goods_id,String qrcode_url){
        if (goods_id==0){
            return;
        }
        String selection = DBHelper.MediaDBInfo.FieldName.GOODS_ID + "=? ";
        String[] selectionArgs = new String[]{String.valueOf(goods_id)};
        List<MediaLibBean> list = DBHelper.get(context).findActivityAdsByWhere(selection,selectionArgs);
        if (list!=null&&list.size()>0){
            MediaLibBean bean = list.get(0);
            String path = AppUtils.getFilePath(AppUtils.StorageFile.activity_ads) + bean.getName();
            File file = new File(path);
            if (file.exists()){
                if (bean.getMedia_type()==1){
                    ProjectOperationListener.getInstance(context).showVideo(path, true,bean.getPrice(),bean.getIs_storebuy(),bean.getDuration(), currentAction, FROM_SERVICE_MINIPROGRAM);
                }else if (bean.getMedia_type()==2){
                    ProjectOperationListener.getInstance(context).showImage(6, path, true,bean.getPrice(),bean.getIs_storebuy(),bean.getDuration(),currentAction, FROM_SERVICE_MINIPROGRAM);
                }
                ((SavorApplication) getApplication()).showGoodsQrCodeWindow(qrcode_url,"");
            }else{
                handler.post(()->ShowMessage.showToast(context,"机顶盒内无此广告，要多开机哦"));
            }
        }
    }

    private void projectionWelcome(final MiniProgramProjection minipp){
        GlobalValues.loopStartTime = System.currentTimeMillis();
        LogUtils.d("12345+miniProgramProjection="+minipp);
        LogUtils.d("12345+downloadVideoIdMap="+projectionIdMap);
        String img_id = minipp.getImg_id();
        String music_id = minipp.getMusic_id();
        String forscreen_char = minipp.getForscreen_char();
        String wordsize = minipp.getWordsize();
        String wordcolor = minipp.getColor();
        String font_id = minipp.getFont_id();
        int play_times = minipp.getPlay_times();
        int rotation = minipp.getRotation();
        String filename = minipp.getFilename();
        String img_url = BuildConfig.OSS_ENDPOINT+minipp.getImg_oss_addr();
        int type = minipp.getType();
        String waiterName = minipp.getWaiterName();
        String waiterIconUrl = minipp.getWaiterIconUrl();
        long startTime = System.currentTimeMillis();
        LogUtils.d("-|-|开始下载时间"+startTime);
        HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac", session.getEthernetMac());
        params.put("forscreen_id", forscreen_id);
        params.put("img_id", img_id);
        params.put("music_id", music_id);
        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.welcome_resource);
        String imgPath;
        String fontPath = null;
        String selection = DBHelper.MediaDBInfo.FieldName.ID + "=? ";
        String[] selectionArgs = new String[]{img_id};
        List<WelcomeResourceBean> imgList = dbHelper.findWelcomeResourceList(selection,selectionArgs);
        if (imgList!=null&&imgList.size()>0){
            WelcomeResourceBean bean = imgList.get(0);
            imgPath = basePath+bean.getName();
            File file = new File(imgPath);
            if (!file.exists()){
                imgPath = checkProjectionExitFile(img_url,filename);
            }
        }else{
            imgPath = checkProjectionExitFile(img_url,filename);
        }
        getMusicPathMethod(basePath,music_id);
        if (!TextUtils.isEmpty(font_id)&&!"0".equals(font_id)){
            selectionArgs = new String[]{font_id};
            List<WelcomeResourceBean> typefaceList = dbHelper.findWelcomeResourceList(selection,selectionArgs);
            if (typefaceList!=null&&typefaceList.size()>0){
                WelcomeResourceBean bean = typefaceList.get(0);
                File file = new File(basePath+bean.getName());
                if (file.exists()){
                    fontPath = basePath+bean.getName();
                }
            }
        }
        postProjectionResourceLog(params);
        //记录当前欢迎词投屏ID
        GlobalValues.WELCOME_ID = minipp.getId();
        postWelcomePlayLog(minipp.getId()+"");
        if (type==1){
            ProjectOperationListener.getInstance(context).showRestImage(7,imgPath,rotation,musicPath,forscreen_char,wordsize,wordcolor,fontPath,play_times, FROM_SERVICE_MINIPROGRAM);
        }else{
            ProjectOperationListener.getInstance(context).showRestImage(7,imgPath,rotation,musicPath,forscreen_char,wordsize,wordcolor,fontPath,waiterIconUrl,waiterName,play_times, FROM_SERVICE_MINIPROGRAM);
        }

    }
    //如果是用户自主上传的欢迎词图片，接口是不存在，所以提前下载好，防止在网络加载过程中黑屏
    private String checkProjectionExitFile(String img_url,String filename){
        String imgPath;
        String projectionPath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
        String path = projectionPath+filename;
        if(new File(path).exists()){
            imgPath = path;
        }else{
            boolean isDownloaded = new ProjectionDownloader(context,img_url,projectionPath,filename,true,serial_number).downloadByRange();
            if (isDownloaded){
                imgPath = path;
            }else{
                imgPath = img_url;
            }
        }
        return imgPath;
    }

    private void exitProjectionWelcome(MiniProgramProjection minipp){
        int id = minipp.getId();
        if (id==GlobalValues.WELCOME_ID&&GlobalValues.mpprojection!=null){
            ProjectOperationListener.getInstance(context).stop(GlobalValues.CURRENT_PROJECT_ID);
            GlobalValues.mpprojection = null;
        }

    }
    /**对服务人员评价完成通知机顶盒*/
    private void finishEvaluate(final MiniProgramProjection minipp){
        String img_id = minipp.getImg_id();
        String music_id = minipp.getMusic_id();
//        String forscreen_char = minipp.getForscreen_char();
        String forscreen_char = "";
        String wordsize = minipp.getWordsize();
        String wordcolor = minipp.getColor();
        String font_id = minipp.getFont_id();
        //满意度级别
        int satisfaction = minipp.getSatisfaction();
        int play_times = minipp.getPlay_times();
        int rotation = minipp.getRotation();
        String filename = minipp.getFilename();
        String img_url = BuildConfig.OSS_ENDPOINT+minipp.getImg_oss_addr();
        String waiterName = minipp.getWaiterName();
        String waiterIconUrl = minipp.getWaiterIconUrl();

        long startTime = System.currentTimeMillis();
        LogUtils.d("-|-|开始下载时间"+startTime);
        HashMap<String, Object> params = new HashMap<>();
        params.put("box_mac", session.getEthernetMac());
        params.put("forscreen_id", forscreen_id);
        params.put("img_id", img_id);
        params.put("music_id", music_id);
        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.welcome_resource);
        String resourceId= null;
        if (satisfaction==1){
            resourceId= R.mipmap.evaluate_n+"";
        }else if (satisfaction==2){
            resourceId= R.mipmap.evaluate_m+"";
        }else if (satisfaction==3){
            resourceId= R.mipmap.evaluate_g+"";
        }

        String musicPath = null;
        String fontPath = null;
        String selection = DBHelper.MediaDBInfo.FieldName.ID + "=? ";
        String[] selectionArgs = new String[]{img_id};
        List<WelcomeResourceBean> imgList = dbHelper.findWelcomeResourceList(selection,selectionArgs);
//        if (imgList!=null&&imgList.size()>0){
//            WelcomeResourceBean bean = imgList.get(0);
//            imgPath = basePath+bean.getName();
//            File file = new File(imgPath);
//            if (!file.exists()){
//                imgPath = checkProjectionExitFile(img_url,filename);
//            }
//        }else{
//            imgPath = checkProjectionExitFile(img_url,filename);
//        }
        if (!TextUtils.isEmpty(music_id)&&!"0".equals(music_id)){
            selectionArgs = new String[]{music_id};
            List<WelcomeResourceBean> musicList = dbHelper.findWelcomeResourceList(selection,selectionArgs);
            if (musicList!=null&&musicList.size()>0){
                WelcomeResourceBean bean = musicList.get(0);
                musicPath = basePath+bean.getName();
                File file = new File(musicPath);
                if (!file.exists()){
                    musicPath = BuildConfig.OSS_ENDPOINT+minipp.getMusic_oss_addr();
                }
            }
        }
        if (!TextUtils.isEmpty(font_id)&&!"0".equals(font_id)){
            selectionArgs = new String[]{font_id};
            List<WelcomeResourceBean> typefaceList = dbHelper.findWelcomeResourceList(selection,selectionArgs);
            if (typefaceList!=null&&typefaceList.size()>0){
                WelcomeResourceBean bean = typefaceList.get(0);
                File file = new File(basePath+bean.getName());
                if (file.exists()){
                    fontPath = basePath+bean.getName();
                }
            }
        }
        //记录当前欢迎词投屏ID
        GlobalValues.WELCOME_ID = minipp.getId();

        ProjectOperationListener.getInstance(context).showRestImage(8,resourceId,rotation,musicPath,forscreen_char,wordsize,wordcolor,fontPath,waiterIconUrl,waiterName,play_times, FROM_SERVICE_MINIPROGRAM);
    }

    /**手機連接機頂盒以後引導圖*/
    private void guideImage(){
        ProjectionGuideImg guideImg = session.getGuideImg();
        if (guideImg!=null&&session.isFirstWelcomeImg()){
            String fileName = guideImg.getForscreen_box_filename();
            int delayTime = guideImg.getShow_time();
            String filePath = AppUtils.getFilePath(AppUtils.StorageFile.cache)+fileName;
            if (new File(filePath).exists()){
                session.setFirstWelcomeImg(false);
                String forsceenId = String.valueOf(System.currentTimeMillis());
                ProjectOperationListener.getInstance(context).showImage(1,filePath,true,forsceenId,String.valueOf(delayTime),headPic,nickName,currentAction, FROM_SERVICE_MINIPROGRAM);
            }
        }
    }

    private void juhuasuanShow(){
        if (juhuasuanShowDialog==null){
            juhuasuanShowDialog = new JuhuasuanShowDialog(context);
        }
        if (juhuasuanShowDialog.isShowing()){
            juhuasuanShowDialog.dismiss();
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                juhuasuanShowDialog.show();
                String codeUrl = mpProjection.getCodeUrl();
                int countdown = mpProjection.getCountdown();
                juhuasuanShowDialog.showJuhuasuanWindow(context,headPic,nickName,codeUrl,countdown);
            }
        });

    }

    private void juhuasuanResult(){
        if (juhuasuanResultDialog==null){
            juhuasuanResultDialog = new JuhuasuanResultDialog(context);
        }
        if (juhuasuanResultDialog.isShowing()){
            juhuasuanResultDialog.dismiss();
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                juhuasuanResultDialog.show();
                String activityPrice = mpProjection.getPrice();
                String jdPrice = mpProjection.getJd_price();
                int countdown = mpProjection.getCountdown();
                List<String> activityContents = mpProjection.getPrize_list();
                juhuasuanResultDialog.juhuasuanResultWindow(activityPrice,jdPrice,activityContents,countdown);
            }
        });
    }


    /**分享展示名片*/
    private void showBusinessCard(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (cardDialog.isShowing()){
                        cardDialog.dismiss();
                    }
                    cardDialog = new BusinessCardDialog(context);
                    cardDialog.show();
                    String jobTitle = mpProjection.getJob();
                    String mobile = mpProjection.getMobile();
                    String company = mpProjection.getCompany();
                    String cardQrcodeUrl = mpProjection.getCodeUrl();
                    int countdownTime = mpProjection.getCountdown();
                    cardDialog.setDatas(nickName,jobTitle,mobile,company,cardQrcodeUrl,countdownTime);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    /**分享展示商業文件*/
    private void showBusinessFile(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (fileDialog.isShowing()){
                        fileDialog.dismiss();
                    }
                    fileDialog = new BusinessFileDialog(context);
                    fileDialog.show();
                    String fileName = mpProjection.getFilename();
                    String cardQrcodeUrl = mpProjection.getCodeUrl();
                    int countdownTime = mpProjection.getCountdown();
                    fileDialog.setDatas(headPic,nickName,fileName,cardQrcodeUrl,countdownTime);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    /**预下载资源*/
    private void preDownloadResource(){
        if (mpProjection!=null&&mpProjection.getResource_list()!=null){
            List<MediaFileBean> list = mpProjection.getResource_list();
            for (MediaFileBean bean:list){
                String url = bean.getUrl();
                String filename = bean.getFilename();
                if (TextUtils.isEmpty(url)||TextUtils.isEmpty(filename)) {
                    return;
                }
                String basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection) + filename;
                File file = new File(path);
                if (file.exists()){
                    file.delete();
                }
                String oss_url = BuildConfig.OSS_ENDPOINT+url;
                ProjectionDownloader downloader = new ProjectionDownloader(context,oss_url, basePath,filename);
                downloader.downloadByRange();
            }
        }

    }
    /**每次投屏开始，设置一下当前投屏动作的状态*/
    private void updateProjectionState(String forscreen_id){
        //map的value值,如果为0正常投屏，如果为1则为打断的投屏
        if (projectionIdMap!=null&&projectionIdMap.size()>0){
            Iterator<Map.Entry<String, String>> iterator = projectionIdMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> entry = iterator.next();
                projectionIdMap.put(entry.getKey(),PROJECTION_STATE_BREAK);
                LogUtils.d("键 key ："+entry.getKey()+" 值value ："+entry.getValue());
            }
        }
        if (!TextUtils.isEmpty(forscreen_id)){
            projectionIdMap.put(forscreen_id,PROJECTION_STATE_PLAY);
        }
    }
    /**新的投屏动作进来，就重置上一次投屏动作的常量*/
    private void resetConstant(){
        GlobalValues.PROJECTION_VIDEO_PATH = null;
        GlobalValues.PROJECT_IMAGES.clear();
        GlobalValues.PROJECT_FAIL_IMAGES.clear();
        GlobalValues.PROJECT_THUMBNIAL_IMAGE.clear();
        GlobalValues.PROJECT_STREAM_IMAGE.clear();
        GlobalValues.PROJECT_STREAM_FAIL_IMAGE.clear();
        GlobalValues.CURRENT_OPEN_ID = openid;
        GlobalValues.CURRENT_FORSCREEN_ID = forscreen_id;
        isDownloadRunnable = false;
        isPPTRunnable = false;
    }

    /**
     * 是否打断？
     * @param forscreen_id
     * @return
     */
    private boolean projectionIsBreak(String forscreen_id){
        boolean isBreak = false;
        if (projectionIdMap!=null&&projectionIdMap.size()>0&&!TextUtils.isEmpty(forscreen_id)){
            if (projectionIdMap.containsKey(forscreen_id)){
                String breakState = projectionIdMap.get(forscreen_id);
                if (breakState.equals(PROJECTION_STATE_BREAK)){
                    isBreak = true;
                }
            }
        }
        return isBreak;
    }

    public void startProjection(int action,String forscreen_id){
        try {
            if (GlobalValues.INTERACTION_ADS_PLAY==0){
                ProjectionGuideImg guideImg = session.getGuideImg();
                int delayTime = guideImg.getShow_time();
                int isShow = guideImg.getIs_show();
                int forscreenNum = guideImg.getForscreen_num();
                if (guideImg!=null&&isShow==1){
                    if (!TextUtils.isEmpty(openid)
                            &&GlobalValues.IMG_NUM.containsKey(openid)
                            &&GlobalValues.IMG_NUM.get(openid)>=forscreenNum){
                        String videoName = guideImg.getVideo_filename();
                        String filePath = AppUtils.getFilePath(AppUtils.StorageFile.cache)+videoName;
                        if (new File(filePath).exists()){
                            ProjectOperationListener.getInstance(context).showImage(1,filePath,true,forscreen_id,String.valueOf(delayTime),null,null,currentAction, FROM_SERVICE_MINIPROGRAM);
                            GlobalValues.IMG_NUM.put(openid,-1);
                            return;
                        }
                    }else if (!TextUtils.isEmpty(openid)
                            &&GlobalValues.VIDEO_NUM.containsKey(openid)
                            &&GlobalValues.VIDEO_NUM.get(openid)>=forscreenNum){
                        String imageName = guideImg.getImage_filename();
                        String filePath = AppUtils.getFilePath(AppUtils.StorageFile.cache)+imageName;
                        if (new File(filePath).exists()){
                            ProjectOperationListener.getInstance(context).showImage(1,filePath,true,forscreen_id,String.valueOf(delayTime),null,null,currentAction, FROM_SERVICE_MINIPROGRAM);
                            GlobalValues.VIDEO_NUM.put(openid,-1);
                            return;
                        }
                    }else if (!TextUtils.isEmpty(openid)
                            &&GlobalValues.FILE_NUM.containsKey(openid)
                            &&GlobalValues.FILE_NUM.get(openid)>=forscreenNum){
                        String videoName = guideImg.getVideo_filename();
                        String filePath = AppUtils.getFilePath(AppUtils.StorageFile.cache)+videoName;
                        if (new File(filePath).exists()){
                            ProjectOperationListener.getInstance(context).showImage(1,filePath,true,forscreen_id,String.valueOf(delayTime),null,null,currentAction, FROM_SERVICE_MINIPROGRAM);
                            GlobalValues.FILE_NUM.put(openid,-1);
                            return;
                        }
                    }
                }
            }
            if (action!=2&&action!=4&&action==currentAction){
                return;
            }
            if (mpProjection==null){
                return;
            }
            switch (currentAction){
                case 2:
                case 4:
                    handleImgAndVideoProjection(forscreen_id);
                    break;
                case 3:
                    exitProjection();
                case 5:
                    onDemandSetTopBoxVideo(mpProjection);
                    break;
                case 6:
                    onDemandBirthdayVideo(mpProjection);
                    break;
                case 9:
                    callBigQrCodeVideo(mpProjection);
                    break;
                case 101:
                    launchMonkeyGame(action,mpProjection);
                    break;
                default:
                    break;
            }


        }catch (Exception e){
            GlobalValues.INTERACTION_ADS_PLAY = 0;
            e.printStackTrace();
        }
    }

    private void handleImgAndVideoProjection(String id){
        final String forscreenId = mpProjection.getForscreen_id();
        if (GlobalValues.INTERACTION_ADS_PLAY==1){
            if (!TextUtils.isEmpty(forscreenId)){
                updateProjectionState(forscreenId);
            }
            if (GlobalValues.PROJECT_IMAGES.size()>0){
                if (!isPPTRunnable){
                    LogUtils.d("1212:启动轮播图片,集合中的值为="+GlobalValues.PROJECT_IMAGES);
                    handler.removeCallbacks(mProjectShowImageRunnable);
                    currentIndex =0;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            projectShowImage(currentIndex, words, headPic, nickName);
                        }
                    }).start();
                }
            }else if (GlobalValues.PROJECTION_VIDEO_PATH!=null){
                if (currentAction==42){
                    int time = 0;
                    if (mpProjection!=null){
                        time =mpProjection.getPlay_times();
                    }
                    ProjectOperationListener.getInstance(context).showRestVideo(GlobalValues.PROJECTION_VIDEO_PATH,true, headPic, nickName,time);
                }else {
                    ProjectOperationListener.getInstance(context).showVideo(GlobalValues.PROJECTION_VIDEO_PATH,true,forscreenId, headPic, nickName, FROM_SERVICE_MINIPROGRAM);
                }
            }
            GlobalValues.INTERACTION_ADS_PLAY=0;
            LogUtils.d("handleImgAndVideo111>>>INTERACTION_ADS_PLAY="+GlobalValues.INTERACTION_ADS_PLAY);
        }else if (GlobalValues.INTERACTION_ADS_PLAY==2){
            if (!TextUtils.isEmpty(id)&&!TextUtils.isEmpty(forscreenId)&&!forscreenId.equals(id)){
                if (!TextUtils.isEmpty(forscreenId)){
                    updateProjectionState(forscreenId);
                }
                if (GlobalValues.PROJECT_IMAGES.size()>0){
                    if (!isPPTRunnable){
                        LogUtils.d("1212:启动轮播图片,集合中的值为="+GlobalValues.PROJECT_IMAGES);
                        handler.removeCallbacks(mProjectShowImageRunnable);
                        currentIndex =0;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                projectionIdMap.put(forscreenId,PROJECTION_STATE_PLAY);
                                projectShowImage(currentIndex, words, headPic, nickName);
                            }
                        }).start();
                    }
                }else if (GlobalValues.PROJECTION_VIDEO_PATH!=null){
                    if (currentAction==42){
                        int time = 0;
                        if (mpProjection!=null){
                            time =mpProjection.getPlay_times();
                        }
                        ProjectOperationListener.getInstance(context).showRestVideo(GlobalValues.PROJECTION_VIDEO_PATH,true, headPic, nickName,time);
                    }else {
                        ProjectOperationListener.getInstance(context).showVideo(GlobalValues.PROJECTION_VIDEO_PATH,true,forscreenId, headPic, nickName, FROM_SERVICE_MINIPROGRAM);
                    }
                }
            }
            GlobalValues.INTERACTION_ADS_PLAY=0;
            LogUtils.d("handleImgAndVideo222>>>INTERACTION_ADS_PLAY="+GlobalValues.INTERACTION_ADS_PLAY);
        }else {
            if (preOrNextAdsBean!=null&&preOrNextAdsBean.getPlay_position()==2){
                String path = preOrNextAdsBean.getMediaPath();
                String duration = preOrNextAdsBean.getDuration();
                if (preOrNextAdsBean.getMedia_type()==1){
                    ProjectOperationListener.getInstance(context).showVideo(path, true,forscreenId, true,duration,currentAction, FROM_SERVICE_MINIPROGRAM);
                }else{
                    ProjectOperationListener.getInstance(context).showImage(5, path, true,forscreenId, words, headPic, nickName,duration,"",currentAction, FROM_SERVICE_MINIPROGRAM);
                }
                postForscreenAdsLog(preOrNextAdsBean.getVid());
                GlobalValues.INTERACTION_ADS_PLAY=2;
                preOrNextAdsBean = null;
            }else{
                GlobalValues.INTERACTION_ADS_PLAY=0;
            }
            LogUtils.d("handleImgAndVideo333>>>INTERACTION_ADS_PLAY="+GlobalValues.INTERACTION_ADS_PLAY);
        }
    }
}

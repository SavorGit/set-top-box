package com.savor.ads.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jar.savor.box.interfaces.OnRemoteOperationListener;
import com.jar.savor.box.vo.BaseResponse;
import com.jar.savor.box.vo.PlayResponseVo;
import com.jar.savor.box.vo.ProgramResponseVo;
import com.jar.savor.box.vo.QueryPosBySessionIdResponseVo;
import com.jar.savor.box.vo.QueryStatusResponseVo;
import com.jar.savor.box.vo.ResponseT;
import com.jar.savor.box.vo.RotateResponseVo;
import com.jar.savor.box.vo.SeekResponseVo;
import com.jar.savor.box.vo.StopResponseVo;
import com.jar.savor.box.vo.VolumeResponseVo;
import com.savor.ads.BuildConfig;
import com.savor.ads.activity.LotteryDrawResultActivity;
import com.savor.ads.activity.LotteryDrawingActivity;
import com.savor.ads.bean.BigImgBean;
import com.savor.ads.bean.BirthdayOndemandBean;
import com.savor.ads.bean.FileQueueParam;
import com.savor.ads.bean.ImgQueueParam;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.MiniProgramProjection;
import com.savor.ads.bean.ProgramBean;
import com.savor.ads.bean.ProjectionGuideImg;
import com.savor.ads.bean.ProjectionImg;
import com.savor.ads.bean.ProjectionLogBean;
import com.savor.ads.bean.ProjectionLogHistory;
import com.savor.ads.bean.SelectContentBean;
import com.savor.ads.bean.SelectContentResult;
import com.savor.ads.bean.SetTopBoxBean;
import com.savor.ads.bean.ShopGoodsBean;
import com.savor.ads.bean.ShopGoodsResult;
import com.savor.ads.bean.VersionInfo;
import com.savor.ads.bean.VideoQueueParam;
import com.savor.ads.bean.WelcomeResourceBean;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.database.DBHelper;
import com.savor.ads.dialog.ProjectionImgListDialog;
import com.savor.ads.log.LogParamValues;
import com.savor.ads.log.LogReportUtil;
import com.savor.ads.projection.ProjectionManager;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;
import com.savor.ads.utils.StreamUtils;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import static com.savor.ads.utils.GlobalValues.FROM_SERVICE_MINIPROGRAM;
import static com.savor.ads.utils.GlobalValues.FROM_SERVICE_REMOTE;


/**
 * Created by zhanghq on 2016/12/22.
 */

public class RemoteService extends Service {
    private String TAG = "RemoteService";
    private Server server = new Server(ConstantValues.SERVER_REQUEST_PORT);
    private static OnRemoteOperationListener listener;
    private RemoteService.ServerThread mServerAsyncTask;
    private int INTERVAL_TIME=1000*30;
    private int REST_INTERVAL_TIME=1000*30;
    Handler handler=new Handler(Looper.getMainLooper());
    private Context context;
    ProjectionImgListDialog projectionImgListDialog = null;
    static ConcurrentLinkedQueue<VideoQueueParam> videoQueue = new ConcurrentLinkedQueue<>();
    static ConcurrentLinkedQueue<ImgQueueParam> imgQueue = new ConcurrentLinkedQueue<>();
    static ConcurrentLinkedQueue<FileQueueParam> fileQueue = new ConcurrentLinkedQueue<>();
    //投图片
    private int TYPE_IMG = 1;
    //投视频
    private int TYPE_VIDEO = 2;

    //是否正在播放ppt
    private boolean isPPTRunnable = false;
    //是否正在进行某一次投屏任务
    private boolean isTasking = false;

    private int delayTime = 1000*3;

    /**增加投屏前置或者后置广告,前置：1，后置：2*/
    private MediaLibBean preOrNextAdsBean=null;
    //给每一类请求分配一个action，用来区分业务方便操作
    private int currentAction;
    public ControllHandler controllHandler;
    public static MiniProgramProjection mpProjection = new MiniProgramProjection();
    public RemoteService() {
    }

    public void setOnRemoteOpreationListener(OnRemoteOperationListener listener1) {
        listener = listener1;
    }

    public void onCreate() {
        super.onCreate();
        context = this;
        projectionImgListDialog = new ProjectionImgListDialog(context);
        LogUtils.d("-------------------> Service onCreate");
        this.mServerAsyncTask = new RemoteService.ServerThread();
        this.mServerAsyncTask.start();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d("-------------------> Service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        LogUtils.e("RemoteService" + "onDestroy");
        super.onDestroy();
        if (RemoteService.this.server != null) {
            try {
                RemoteService.this.server.stop();
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }
        if (this.mServerAsyncTask != null) {
            this.mServerAsyncTask.interrupt();
            this.mServerAsyncTask = null;
        }

    }

    public IBinder onBind(Intent intent) {
        return new OperationBinder();
    }

    /**
     * 处理jetty服务的核心请求方法
     */
    public class ControllHandler extends AbstractHandler{

        private ControllHandler() {
            mLock = new Object();
            controllHandler = this;
        }

        private Object mLock;
        private String avatarUrl = null;
        private String nickName = null;
        private String musicPath = null;
        /**openid*/
        private String deviceId;
        /**手机品牌*/
        private String deviceName;
        /**手机型号*/
        private String device_model;
        /**当前轮播图片的游标*/
        private int currentIndex=0;
        /** 投屏时屏幕显示的文字*/
        private String words;
        /**投屏唯一标示id*/
        private String forscreenId;
        /**投屏开始时间*/
        private String res_sup_time;
        /**投屏结束时间*/
        private String res_eup_time;
        /**机顶盒接受到的图片块*/
        private HashMap<String,Integer> acceptNum = new HashMap<>();
        /**开始时间*/
        private HashMap<String,String> acceptsTime = new HashMap<>();
        /**结束时间*/
        private HashMap<String,String> accepteTime = new HashMap<>();

        private HashMap<String,Boolean> converted = new HashMap<>();

        public void handle(String target, Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
            synchronized (mLock) {
                LogUtils.w("***********一次请求处理开始...***********");
                LogUtils.d("target = " + target);
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(200);
                baseRequest.setHandled(true);
                LogUtils.e("enter method listener.handle");
                String boxMac = Session.get(context).getEthernetMac();
                String clientMac = request.getParameter("box_mac");
                if(!boxMac.equals(clientMac)){
                    BaseResponse baseResponse = new BaseResponse();
                    baseResponse.setMsg("非本机mac,您得wifi可能迷路啦");
                    baseResponse.setCode(ConstantValues.SERVER_RESPONSE_CODE_MAC_ERROR);
                    String resp = new Gson().toJson(baseResponse);
                    LogUtils.d("返回结果:" + resp);
                    response.getWriter().println(resp);
                }else if (GlobalValues.IS_BOX_BUSY) {
                    BaseResponse baseResponse = new BaseResponse();
                    baseResponse.setMsg("机顶盒繁忙，请稍候再试");
                    baseResponse.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                    String resp = new Gson().toJson(baseResponse);
                    LogUtils.d("返回结果:" + resp);
                    response.getWriter().println(resp);
                } else {
//                    String version = request.getHeader("version");
                    try {
                        handleRequestV10(request, response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                LogUtils.w("***********一次请求处理结束...***********");
            }
        }

        /**
         * v1.0接口处理请求的逻辑
         *
         * @param request
         * @param response
         * @throws IOException
         * @throws ServletException
         */
        private void handleRequestV10(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            String resJson = "";
            String path = request.getPathInfo();
            LogUtils.d("request:--" + request.toString());
            LogUtils.e("enter method listener1.handleRequestV10");
            if (TextUtils.isEmpty(path)) {
                BaseResponse baseResponse = new BaseResponse();
                baseResponse.setMsg("错误的功能");
                baseResponse.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                resJson = new Gson().toJson(baseResponse);
            }else if (path.contains("h5Test")){
                BaseResponse baseResponse = new BaseResponse();
                baseResponse.setMsg("网络畅通");
                baseResponse.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                resJson = new Gson().toJson(baseResponse);
                resJson = "h5turbine(" + resJson + ")";
                LogUtils.e("enter method listener1.h5Test");
                response.getWriter().println(resJson);
                return;
            }else {
                if (!currentMeetingAction()){
                    resJson = distributeRequest(request, path);
                }
            }

            if (TextUtils.isEmpty(resJson)) {
                BaseResponse baseResponse = new BaseResponse();
                baseResponse.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                baseResponse.setMsg("操作失败");
                resJson = new Gson().toJson(baseResponse);
            }
            try{
                response.getWriter().println(resJson);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        private String distributeRequest(final HttpServletRequest request,String action){
            String resJson = "";
            // 标识是否强行投屏
            LogUtils.d("enter method request.distributeRequest");
            // forceProject等于0表示不是强制抢投，等于1表示强制投，等于-1表示是老版移动端调用
            int forceProject = -1;
            switch (action) {
                case "/vod":
                    currentAction = 0;
                    break;
                case "/video":
                    currentAction = 1;
                    break;
                case "/videoH5":
                case "/h5/video":
                    currentAction = 2;
                    resJson = handleVideoH5Request(request);
                    break;
                case "/h5/restVideo":
                    currentAction = 3;
                    resJson = handleVideoH5Request(request);
                    break;
                case "/pic":
                    currentAction = 4;
                    break;
                case "/picH5":
                    currentAction = 5;//极简版重投
                    resJson = handlePicH5Request(request);
                    break;
                case "/h5/restPicture":
                    currentAction = 6;
                    resJson = handlePicH5Request(request);
                    break;
                case "/h5/singleImg":
                case "/h5/restSingleImg":
                    currentAction = 7;//极简版滑动单张
                    resJson = handleSinglePicRequest(request);
                    break;
                case "/h5/stop":
                    currentAction = 8;
                    resJson = handleH5StopRequest();
                    break;
                case "/showMiniProgramCode":
                    currentAction = 9;
                    resJson = handleShowMiniProgramCode(request);
                    break;
                case "/stop":
                    currentAction = 10;
                    resJson = handleStopRequest(request);
                    break;
                case "/rotate":
                    currentAction = 11;
                    resJson = handleRotateRequest(request);
                    break;
                case "/resume":
                    currentAction = 12;
                    resJson = handleResumeRequest(request);
                    break;
                case "/pause":
                    currentAction = 13;
                    resJson = handlePauseRequest(request);
                    break;
                case "/seek":
                    currentAction = 14;
                    resJson = handleSeekRequest(request);
                    break;
                case "/volume":
                    currentAction = 15;
                    resJson = handleVolumeRequest(request);
                    break;
                case "/switchProgram":
                    currentAction = 16;
                    resJson = handleProgramRequest(request);
                    break;
                case "/h5/birthday_ondemand":
                    currentAction = 17;
                    resJson = handleH5BirthdayOndemand(request);
                    break;
                case "/query":
                    currentAction = 18;
                    resJson = handleQueryRequest(request);
                    break;
                case "/queryStatus":
                    currentAction = 19;
                    resJson = handleQueryStatusRequest();
                    break;
                case "/verify":
                    currentAction = 21;
                    resJson = handleVerifyCodeRequest(request);
                    break;
                case "/bigImgPartUpload":
                    currentAction = 22;
                    resJson = handleBigImgBlockUploadRequest(request);
                    break;
                case "/videoUploadSpeed":
                    currentAction = 23;
                    resJson = handleVideoBlockUploadRequest(request);
                    break;
                case "/fileBlockUpload":
                    currentAction = 24;
                    resJson = handleFileBlockUploadRequest(request);
                    break;
                case "/h5/showFileImg":
                    currentAction = 25;
                    resJson = showFileImgRequest(request);
                    break;
                case "/h5/projectionLog":
                    resJson = findProjectionHistory(request);
                    break;
                case "/h5/projectionThumbnail":
                    resJson = findProjectionThumbnail(request);
                    break;
                case "/h5/fileImgList":
                    resJson = findFileImgList(request);
                    break;
                case "/WLAN/getProAdvListData":
                    resJson = findProAdvListData();
                    break;
                case "/WLAN/getAdsListData":
                    resJson = findAdsListData();
                    break;
                case "/WLAN/getShopgoodsProData":
                    resJson = findshowgoodsData();
                    break;
                case "/WLAN/getHotPlayProData":
                    resJson = findHotPlayProData();
                    break;
                default:
                    currentAction = -1;
                    LogUtils.d(" not enter any method");
                    BaseResponse baseResponse = new BaseResponse();
                    baseResponse.setMsg("错误的功能");
                    baseResponse.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                    resJson = new Gson().toJson(baseResponse);
                    break;
            }
            return resJson;
        }
        /**获取接口基础参数，openid,设备名称，设备型号，微信头像昵称等*/
        private void initBaseParam(HttpServletRequest request){
            deviceId = request.getParameter("deviceId");
            deviceName = request.getParameter("deviceName");
            device_model = request.getParameter("device_model");
            avatarUrl = request.getParameter("avatarUrl");
            nickName = request.getParameter("nickName");
        }

        private boolean currentMeetingAction(){
            Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
            if (activity instanceof LotteryDrawingActivity
                    ||activity instanceof LotteryDrawResultActivity){
                return true;
            }
            return false;
        }

        private String handleVideoH5Request(HttpServletRequest request){

            initBaseParam(request);
            String forscreen_id = request.getParameter("forscreen_id");
            String video_id = System.currentTimeMillis()+"";
            final MiniProgramProjection minipp = new MiniProgramProjection();
            minipp.setVideo_id(video_id);
            if (TextUtils.isEmpty(GlobalValues.CURRENT_FORSCREEN_ID)
                    ||!GlobalValues.CURRENT_FORSCREEN_ID.equals(forscreen_id)){
                clearProjectionMark(forscreen_id);
                //記錄投屏次數
                if (GlobalValues.VIDEO_NUM.containsKey(deviceId)){
                    if (GlobalValues.VIDEO_NUM.get(deviceId)!=-1){
                        GlobalValues.VIDEO_NUM.put(deviceId,GlobalValues.VIDEO_NUM.get(deviceId)+1);
                    }
                }else{
                    GlobalValues.VIDEO_NUM.put(deviceId,1);
                }
                res_sup_time = String.valueOf(System.currentTimeMillis());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (projectionImgListDialog!=null){
                            if (projectionImgListDialog.isShowing()){
                                projectionImgListDialog.clearContent();
                                projectionImgListDialog.dismiss();
                            }

                            projectionImgListDialog.show();
                            projectionImgListDialog.setProjectionPersonInfo(avatarUrl,nickName);

                            ArrayList<ProjectionImg> list = new ArrayList<>();
                            ProjectionImg img = new ProjectionImg();
                            img.setVideo_id(minipp.getVideo_id());
                            list.add(img);
                            projectionImgListDialog.setContent(list,TYPE_VIDEO);
                        }
                    }
                });
            }
            // 视频流投屏处理
            String resJson = downloadStreamVideoProjection(request,video_id);
            return resJson;
        }

        private String downloadStreamVideoProjection(HttpServletRequest request,final String video_id){
            String respJson = "";
            try{
                boolean repeat = false;
                BaseResponse object = null;
                MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String) null);
                request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                String action = request.getParameter("action");
                String filename = request.getParameter("filename");
                String forscreen_id = request.getParameter("forscreen_id");
                String resource_size = request.getParameter("resource_size");
                String resource_type = request.getParameter("resource_type");
                String duration = request.getParameter("duration");
                String play_time = request.getParameter("play_times");
                String serial_number = request.getParameter("serial_number");
                res_sup_time = System.currentTimeMillis()+"";
                if (TextUtils.isEmpty(filename)){
                    object = new BaseResponse();
                    object.setMsg("上传异常-文件名称为空");
                    object.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                    return new Gson().toJson(object);
                }
                // 临时文件
                String name = filename+".mp4";
                File videoTmpFile = new File(path + "tmp_" + name);
                //结果文件
                File resultFile = new File(path+name);
                boolean isDownloaded;
                if (resultFile.exists()){
                    repeat = true;
                    isDownloaded = true;
                    handler.post(()->projectionImgListDialog.setImgDownloadProgress(video_id,"100%"));
                }else{
                    int start = 0;
                    long startTime = System.currentTimeMillis();
                    Part part = request.getPart("fileUpload");
                    // 存文件
                    RandomAccessFile raf = new RandomAccessFile(videoTmpFile, "rw");
                    raf.seek(start);
                    byte[] byteBuffer = new byte[1024*1024*1];
                    int len = 0;
                    final long totalSize = part.getSize();
                    int currentSize = 0;
                    // 注意，part.getInputStream()切记不要多次调用
                    InputStream inputStream = part.getInputStream();
                    while ((len = inputStream.read(byteBuffer)) > 0) {
                        raf.write(byteBuffer, 0, len);
                        currentSize = currentSize +len;
                        setDownloadProgress(video_id,totalSize,currentSize);

                    }
                    raf.close();
                    inputStream.close();
                    isDownloaded = true;
                    videoTmpFile.renameTo(resultFile);

                    String useTime = String.valueOf(System.currentTimeMillis()-startTime);
                    String resourceSize = String.valueOf(resultFile.length());
                    String mUUID = String.valueOf(System.currentTimeMillis());
                    LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.download, LogParamValues.speed_size,resourceSize);
                    Thread.sleep(500);
                    LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.download, LogParamValues.speed_duration,useTime);
                    Thread.sleep(500);
                    LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.download, LogParamValues.speed_serial,serial_number);

                }

                if (isDownloaded){
                    mpProjection.setReq_id(serial_number);
                    mpProjection.setFilename(filename);
                    mpProjection.setForscreen_id(forscreen_id);
                    mpProjection.setResource_id(filename);
                    mpProjection.setOpenid(deviceId);
                    res_eup_time = String.valueOf(System.currentTimeMillis());
                    String media_path = resultFile.getAbsolutePath();
                    handler.post(this::closeDownloadWindow);

                    GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                    GlobalValues.CURRENT_PROJECT_DEVICE_NAME = deviceName;
                    GlobalValues.IS_RSTR_PROJECTION = true;
                    GlobalValues.CURRENT_PROJECT_DEVICE_IP = request.getRemoteHost();
                    if (currentAction == 2){
                        if (GlobalValues.INTERACTION_ADS_PLAY!=0){
                            //处理抢投
                            GlobalValues.PROJECTION_VIDEO_PATH = resultFile.getAbsolutePath();
                        }else{
                            preOrNextAdsBean = AppUtils.getInteractionAds(context);
                            if (preOrNextAdsBean!=null&&preOrNextAdsBean.getPlay_position()==1){
                                GlobalValues.INTERACTION_ADS_PLAY=1;
                                String adspath = preOrNextAdsBean.getMediaPath();
                                String adsduration = preOrNextAdsBean.getDuration();
                                if (preOrNextAdsBean.getMedia_type()==1){
                                    ProjectOperationListener.getInstance(context).showVideo(adspath,true,forscreen_id, true,adsduration,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                                }else{
                                    ProjectOperationListener.getInstance(context).showImage(5, adspath, true,forscreen_id, "", avatarUrl, nickName,adsduration,"",currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                                }
                                preOrNextAdsBean = null;
                                GlobalValues.PROJECTION_VIDEO_PATH = resultFile.getAbsolutePath();
                            }else{
                                GlobalValues.PROJECTION_VIDEO_PATH = null;
                                RemoteService.listener.showVideo(media_path, true,forscreen_id,avatarUrl,nickName,null,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                                postSimpleMiniProgramProjectionLog(action,duration,"",forscreen_id,filename,resource_size,resource_type,media_path,serial_number,repeat);
                            }

                        }
                    }else if (currentAction==3){
                        int time = 0;
                        if (!TextUtils.isEmpty(play_time)&&!play_time.equals("0")){
                            time = Integer.valueOf(play_time);
                        }
                        RemoteService.listener.showRestVideo(resultFile.getAbsolutePath(),true, avatarUrl, nickName,time,currentAction,FROM_SERVICE_MINIPROGRAM);
                        postSimpleMiniProgramProjectionLog(action,duration,"",forscreen_id,filename,resource_size,resource_type,media_path,serial_number,repeat);
                    }
                    object = new BaseResponse();
                    object.setMsg("上传成功");
                    object.setResult(new JsonObject());
                    object.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                }
                respJson = new Gson().toJson(object);
            }catch (Exception e){
                e.printStackTrace();
                handler.post(this::closeDownloadWindow);
            }
            return respJson;
        }
        /**
         * 切片上传边写入边播放
         */
        private String handleVideoBlockUploadRequest(HttpServletRequest request){
            InputStream inputStream = null;
            String index = null;
            BaseResponse object;
            String respJson;
            initBaseParam(request);
            String forscreen_id = request.getParameter("forscreen_id");
            if (TextUtils.isEmpty(GlobalValues.CURRENT_FORSCREEN_ID)){
                clearProjectionMark(forscreen_id);
                res_sup_time = String.valueOf(System.currentTimeMillis());
                videoQueue.clear();
            }else {
                long preProjectId = Long.valueOf(GlobalValues.CURRENT_FORSCREEN_ID);
                long nowProjectId = Long.valueOf(forscreen_id);
                if (nowProjectId>preProjectId){
                    clearProjectionMark(forscreen_id);
                    res_sup_time = String.valueOf(System.currentTimeMillis());
                    videoQueue.clear();
                }else if (nowProjectId<preProjectId){
                    object = new BaseResponse();
                    object.setMsg("已被抢投");
                    object.setResult(new JsonObject());
                    object.setCode(ConstantValues.SERVER_RESPONSE_CODE_AHEAD);
                    respJson = new Gson().toJson(object);
                    return respJson;
                }
            }
            try {
                index = request.getParameter("index");
                inputStream = request.getInputStream();
                String action = request.getParameter("action");
                String duration = request.getParameter("duration");
                String filename = request.getParameter("filename");
                String position = request.getParameter("position");
                String resource_size = request.getParameter("resource_size");
                String resource_type = request.getParameter("resource_type");
                String chunkSize = request.getParameter("chunkSize");
                String totalChunks = request.getParameter("totalChunks");
                String serial_number = request.getParameter("serial_number");
                String is_share = request.getParameter("is_share");
                String public_text = request.getParameter("public_text");
                String forscreen_char = "";
                VideoQueueParam param = new VideoQueueParam();
                param.setAction(action);
                param.setDuration(duration);
                param.setForscreen_id(forscreen_id);
                param.setIndex(index);
                param.setResource_type(resource_type);
                param.setFileName(filename);
                param.setPosition(position);
                param.setTotalSize(resource_size);
                param.setChunkSize(chunkSize);
                param.setTotalChunks(totalChunks);
                param.setInputContent(StreamUtils.toByteArray(inputStream));
                param.setSerial_number(serial_number);
                if (!TextUtils.isEmpty(is_share)){
                    param.setIs_share(Integer.valueOf(is_share));
                }
                param.setPublic_text(public_text);
                videoQueue.offer(param);
                Log.d(TAG,"写入队列，数据块index==="+index+"||input=="+param.getInputContent());
                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                String outPath = path + filename + ".mp4";
                if (TextUtils.isEmpty(index)){
                    mpProjection.setReq_id(serial_number);
                    mpProjection.setFilename(filename);
                    mpProjection.setForscreen_id(forscreen_id);
                    mpProjection.setResource_id(filename);
                    mpProjection.setOpenid(deviceId);
                    //記錄投屏次數
                    if (GlobalValues.VIDEO_NUM.containsKey(deviceId)){
                        if (GlobalValues.VIDEO_NUM.get(deviceId)!=-1){
                            GlobalValues.VIDEO_NUM.put(deviceId,GlobalValues.VIDEO_NUM.get(deviceId)+1);
                        }
                    }else{
                        GlobalValues.VIDEO_NUM.put(deviceId,1);
                    }
                    VideoWriter writer = new VideoWriter(context,forscreen_id,videoQueue,outPath,currentAction);
                    writer.setUserInfo(avatarUrl,nickName);
                    writer.setToPlayListener(new ToPlayInterface() {
                        @Override
                        public void playProjection(Object obj) {
                            if (obj instanceof VideoQueueParam){
                                VideoQueueParam vqParam = (VideoQueueParam)obj;
                                mpProjection.setReq_id(vqParam.getSerial_number());
                                mpProjection.setForscreen_id(vqParam.getForscreen_id());
                                mpProjection.setResource_id(vqParam.getFileName());
                                mpProjection.setOpenid(deviceId);
                                res_eup_time = String.valueOf(System.currentTimeMillis());
                                int is_share = vqParam.getIs_share();
                                String public_text = vqParam.getPublic_text();
                                postSimpleMiniProgramProjectionLog(action,duration,forscreen_char,forscreen_id,filename,resource_size,resource_type,outPath,serial_number,null,is_share,public_text,"",false);
                            }
                        }
                    });
                    new Thread(writer).start();
                }
                object = new BaseResponse();
                object.setMsg("上传成功");
                object.setResult(new JsonObject());
                object.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);

            }catch (Exception e){
                object = new BaseResponse();
                object.setMsg("上传报错");
                object.setResult(index);
                object.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                e.printStackTrace();
            }finally {
                try {
                    if (inputStream != null)
                        inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            respJson = new Gson().toJson(object);
            return respJson;
        }

        private void getMusicPathMethod(String basePath,String music_id,String music_oss_addr){
            if (!TextUtils.isEmpty(music_id)&&!"0".equals(music_id)){
                String selection = DBHelper.MediaDBInfo.FieldName.ID + "=? ";
                String[] selectionArgs = new String[]{music_id};
                List<WelcomeResourceBean> musicList = DBHelper.get(context).findWelcomeResourceList(selection,selectionArgs);
                if (musicList!=null&&musicList.size()>0){
                    WelcomeResourceBean bean = musicList.get(0);
                    musicPath = basePath+bean.getName();
                    File file = new File(musicPath);
                    if (!file.exists()){
                        musicPath = BuildConfig.OSS_ENDPOINT + music_oss_addr;
                    }
                }else{
                    musicPath = BuildConfig.OSS_ENDPOINT + music_oss_addr;
                }
            }
        }

        /**
         * 切片上传大图片
         */
        private String handleBigImgBlockUploadRequest(HttpServletRequest request){
            InputStream inputStream = null;
            String index = null;
            BaseResponse object;
            String respJson;
            initBaseParam(request);
            String forscreen_id = request.getParameter("forscreen_id");
            String forscreen_nums = request.getParameter("forscreen_nums");
            if (TextUtils.isEmpty(GlobalValues.CURRENT_FORSCREEN_ID)){
                clearProjectionMark(forscreen_id);
                imgQueue.clear();
                acceptNum.clear();
                acceptsTime.clear();
                accepteTime.clear();
                isTasking = false;
            }else {
                long preProjectId = Long.valueOf(GlobalValues.CURRENT_FORSCREEN_ID);
                long nowProjectId = Long.valueOf(forscreen_id);
                if (nowProjectId>preProjectId){
                    clearProjectionMark(forscreen_id);
                    res_sup_time = String.valueOf(System.currentTimeMillis());
                    imgQueue.clear();
                    acceptNum.clear();
                    acceptsTime.clear();
                    accepteTime.clear();
                    isTasking = false;
                }else if (nowProjectId<preProjectId){
                    object = new BaseResponse();
                    object.setMsg("已被抢投");
                    object.setResult(new JsonObject());
                    if (GlobalValues.CURRENT_OPEN_ID.equals(deviceId)){
                        object.setCode(ConstantValues.SERVER_RESPONSE_CODE_SELF);
                    }else {
                        object.setCode(ConstantValues.SERVER_RESPONSE_CODE_AHEAD);
                    }
                    respJson = new Gson().toJson(object);
                    return respJson;
                }
            }
            try {
                index = request.getParameter("index");
                inputStream = request.getInputStream();
                String action = request.getParameter("action");
                String filename = request.getParameter("filename");
                String position = request.getParameter("position");
                String resource_size = request.getParameter("resource_size");
                String resource_type = request.getParameter("resource_type");
                String chunkSize = request.getParameter("chunkSize");
                String totalChunks = request.getParameter("totalChunks");
                String serial_number = request.getParameter("serial_number");
                String forscreen_char = request.getParameter("forscreen_char");
                LogUtils.d("forscreen_char>>>>"+forscreen_char);
                String img_size = request.getParameter("img_size");
                String resource_id = request.getParameter("img_size");
                //0:原图，1:缩略图
                String thumbnail = request.getParameter("thumbnail");
                String music_id = request.getParameter("music_id");
                String music_oss_addr = request.getParameter("music_oss_addr");
                String is_share = request.getParameter("is_share");
                String public_text = request.getParameter("public_text");
                ImgQueueParam param = new ImgQueueParam();
                param.setAction(action);
                param.setForscreen_id(forscreen_id);
                param.setIndex(index);
                param.setResource_type(resource_type);
                param.setFileName(filename);
                param.setPosition(position);
                param.setTotalSize(resource_size);
                param.setChunkSize(chunkSize);
                param.setTotalChunks(totalChunks);
                param.setSerial_number(serial_number);
                param.setDeviceId(deviceId);
                if (!TextUtils.isEmpty(is_share)){
                    param.setIs_share(Integer.valueOf(is_share));
                }
                param.setPublic_text(public_text);
                if (!acceptNum.containsKey(filename)){
                    String startTime = System.currentTimeMillis()+"";
                    acceptsTime.put(filename,startTime);
                    acceptNum.put(filename,0);
                }
//                param.setInputContent(StreamUtils.toByteArray(inputStream));
                param.setInputContent(IOUtils.toByteArray(inputStream));
                int parts = acceptNum.get(filename)+1;
                acceptNum.put(filename,parts);
                if (acceptNum.get(filename)==Integer.valueOf(totalChunks)){
                    String endTime = System.currentTimeMillis()+"";
                    accepteTime.put(filename,endTime);
                }
                param.setSerial_number(serial_number);
                param.setThumbnail(thumbnail);
                param.setForscreen_nums(forscreen_nums);
                param.setSize(img_size);
                imgQueue.offer(param);
                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                if (!isTasking){
                    musicPath = null;
                    String basePath = AppUtils.getFilePath(AppUtils.StorageFile.welcome_resource);
                    getMusicPathMethod(basePath,music_id,music_oss_addr);
                    //記錄投屏次數
                    if (GlobalValues.IMG_NUM.containsKey(deviceId)){
                        if (GlobalValues.IMG_NUM.get(deviceId)!=-1){
                            GlobalValues.IMG_NUM.put(deviceId,GlobalValues.IMG_NUM.get(deviceId)+1);
                        }
                    }else{
                        GlobalValues.IMG_NUM.put(deviceId,1);
                    }
                    showDownloadWindow();
                    BigImgWriter writer = new BigImgWriter(forscreen_id,imgQueue,path);
                    writer.setToPlayListener(new ToPlayInterface() {
                        @Override
                        public void playProjection(Object object) {
                            ImgQueueParam imgQueue = null;
                            if (object instanceof ImgQueueParam){
                                imgQueue = (ImgQueueParam)object;
                            }
                            String forscreen_id = imgQueue.getForscreen_id();
                            if (!GlobalValues.CURRENT_FORSCREEN_ID.equals(forscreen_id)){
                                return;
                            }
                            String filePath = imgQueue.getFilePath();
                            String filename = imgQueue.getFileName();
                            String[] filename_ids = filename.split("\\.");
                            String filename_id = filename_ids[0];
                            boolean isExitBig = false;
                            if (GlobalValues.PROJECT_THUMBNIAL_IMAGE.size()>0){
                                for (BigImgBean bean:GlobalValues.PROJECT_THUMBNIAL_IMAGE){
                                    if (!TextUtils.isEmpty(imgQueue.getThumbnail())&&imgQueue.getThumbnail().equals("0")){
                                        String keyId = filename_id;
                                        if (bean.getFilenameId().equals(keyId)){
                                            isExitBig = true;
                                            bean.setBigPath(filePath);
                                            break;
                                        }
                                    }else if (!TextUtils.isEmpty(imgQueue.getThumbnail())&&imgQueue.getThumbnail().equals("1")){
                                        String keyId = filename_id.substring(2);
                                        if (bean.getFilenameId().equals(keyId)){
                                            isExitBig = true;
                                            bean.setThumbnailPath(filePath);
                                            break;
                                        }
                                    }

                                }
                            }
                            if (!isExitBig){
                                BigImgBean bigImgBean = new BigImgBean();
                                if (!TextUtils.isEmpty(imgQueue.getThumbnail())&&imgQueue.getThumbnail().equals("0")){
                                    bigImgBean.setFilenameId(filename_id);
                                    bigImgBean.setBigPath(filePath);
                                    res_sup_time = acceptsTime.get(filename);
                                    res_eup_time = accepteTime.get(filename);
                                }else if (!TextUtils.isEmpty(imgQueue.getThumbnail())&&imgQueue.getThumbnail().equals("1")){
                                    bigImgBean.setFilenameId(filename_id.substring(2));
                                    bigImgBean.setThumbnailPath(filePath);
                                    res_sup_time = acceptsTime.get(filename.substring(2));
                                    res_eup_time = accepteTime.get(filename.substring(2));
                                }

                                bigImgBean.setDeviceId(deviceId);
                                bigImgBean.setForscreen_id(forscreen_id);
                                bigImgBean.setSerial_number(imgQueue.getSerial_number());
                                GlobalValues.PROJECT_THUMBNIAL_IMAGE.add(bigImgBean);

                                String outPath = imgQueue.getFilePath();
                                LogUtils.d("数据插入，开始，forscreenId=" + imgQueue.getForscreen_id());
                                int is_share = imgQueue.getIs_share();
                                String public_text = imgQueue.getPublic_text();
                                String forscreen_nums = imgQueue.getForscreen_nums();
                                postSimpleMiniProgramProjectionLog(action,"",forscreen_char,forscreen_id,imgQueue.getFileName(),imgQueue.getSize(),resource_type,outPath,serial_number,musicPath,is_share,public_text,forscreen_nums,false);

                                String img_id = System.currentTimeMillis()+"";
                                ProjectionImg img = new ProjectionImg();
                                img.setImg_id(img_id);
                                GlobalValues.PROJECT_STREAM_IMAGE_NUMS.add(img);
                                handler.post(()->projectionImgListDialog.setContent(GlobalValues.PROJECT_STREAM_IMAGE_NUMS,TYPE_IMG,"100%"));
                            }

                           if (GlobalValues.PROJECT_THUMBNIAL_IMAGE.size()==Integer.valueOf(forscreen_nums)){
                               handler.postDelayed(()->closeDownloadWindow(),500);
                               isTasking = false;
                               acceptNum.clear();
                               acceptsTime.clear();
                               accepteTime.clear();
                           }

                            if (!isPPTRunnable){
                                currentIndex=0;
                                int time = 0;
                                if (currentAction==22&&GlobalValues.INTERACTION_ADS_PLAY==0){
                                    preOrNextAdsBean = AppUtils.getInteractionAds(context);
                                    if (preOrNextAdsBean!=null&&preOrNextAdsBean.getPlay_position()==1){
                                        GlobalValues.INTERACTION_ADS_PLAY = 1;
                                        final String adspath = preOrNextAdsBean.getMediaPath();
                                        final String adsduration = preOrNextAdsBean.getDuration();

                                        if (preOrNextAdsBean.getMedia_type()==1){
                                            ProjectOperationListener.getInstance(context).showVideo(adspath,true,forscreen_id,true,adsduration,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                                        }else {
                                            ProjectOperationListener.getInstance(context).showImage(5, adspath, true,forscreen_id, words, avatarUrl, nickName,adsduration,"",currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                                        }
                                        preOrNextAdsBean = null;
                                    }else{
                                        isPPTRunnable = true;
                                        projectShowImage(forscreen_char,forscreen_id,time);
                                    }
                                }


                            }
                        }
                    });
                    new Thread(writer).start();
                    isTasking = true;
                }
                object = new BaseResponse();
                object.setMsg("上传成功");
                object.setResult(new JsonObject());
                object.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);

            }catch (Exception e){
                object = new BaseResponse();
                object.setMsg("上传报错");
                object.setResult(index);
                object.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                e.printStackTrace();
            }finally {
                try {
                    if (inputStream != null)
                        inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            respJson = new Gson().toJson(object);
            return respJson;
        }

        /**
         * 切片上传文件(暂时只支持PDF)
         * @param request
         * @return
         */
        @Deprecated
        private String handleFileBlockUploadRequest(HttpServletRequest request){
            InputStream inputStream = null;
            String index = null;
            BaseResponse object;
            String respJson;
            initBaseParam(request);
            String forscreen_id = request.getParameter("forscreen_id");
            if (TextUtils.isEmpty(GlobalValues.CURRENT_FORSCREEN_ID)){
                clearProjectionMark(forscreen_id);
                res_sup_time = String.valueOf(System.currentTimeMillis());
                fileQueue.clear();
            }else {
                long preProjectId = Long.valueOf(GlobalValues.CURRENT_FORSCREEN_ID);
                long nowProjectId = Long.valueOf(forscreen_id);
                if (nowProjectId>preProjectId){
                    clearProjectionMark(forscreen_id);
                    res_sup_time = String.valueOf(System.currentTimeMillis());
                    fileQueue.clear();
                }else if (nowProjectId<preProjectId){
                    object = new BaseResponse();
                    object.setMsg("已被抢投");
                    object.setResult(new JsonObject());
                    object.setCode(ConstantValues.SERVER_RESPONSE_CODE_AHEAD);
                    respJson = new Gson().toJson(object);
                    return respJson;
                }
            }
            try {
                index = request.getParameter("index");
                inputStream = request.getInputStream();
                String action = request.getParameter("action");
                String duration = request.getParameter("duration");
                String filename = request.getParameter("filename");
                String position = request.getParameter("position");
                String resource_size = request.getParameter("resource_size");
                String resource_type = request.getParameter("resource_type");
                String chunkSize = request.getParameter("chunkSize");
                String totalChunks = request.getParameter("totalChunks");
                String save_type = request.getParameter("save_type");
                String serial_number = request.getParameter("serial_number");
                FileQueueParam param = new FileQueueParam();
                param.setAction(action);
                param.setDuration(duration);
                param.setForscreen_id(forscreen_id);
                param.setIndex(index);
                param.setResource_type(resource_type);
                param.setFileName(filename);
                param.setPosition(position);
                param.setTotalSize(resource_size);
                param.setChunkSize(chunkSize);
                param.setTotalChunks(totalChunks);
                param.setInputContent(StreamUtils.toByteArray(inputStream));
                param.setSave_type(save_type);
                param.setSerial_number(serial_number);
                fileQueue.offer(param);
                Log.d(TAG,"写入队列，数据块index==="+index+"||input=="+param.getInputContent());
                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                String suffix = AppUtils.getFileSuffix(filename);
                String fileDir = AppUtils.getMD5(filename);
                String outPath = path + fileDir + suffix;
                if ("0".equals(index)){
                    FileImgWriter writer = new FileImgWriter(context,forscreen_id,fileQueue,outPath);
                    writer.setUserInfo(avatarUrl,nickName);
                    writer.setToPlayListener(obj -> {
                        if (obj instanceof FileQueueParam){
                            FileQueueParam fParam = (FileQueueParam)obj;
                            res_eup_time = String.valueOf(System.currentTimeMillis());
                            handler.post(()->ShowMessage.showToast(context,"下载完成，开始转换文件"));
                            converted.clear();
                            converted.put(fParam.getFileName(),true);
//                            convertFileToImg(fParam,outPath);
                        }

                    });
                    new Thread(writer).start();
                }
                object = new BaseResponse();
                object.setMsg("上传成功");
                object.setResult(new JsonObject());
                object.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);

            }catch (Exception e){
                object = new BaseResponse();
                object.setMsg("上传报错");
                object.setResult(index);
                object.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                e.printStackTrace();
            }finally {
                try {
                    if (inputStream != null)
                        inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            respJson = new Gson().toJson(object);
            return respJson;
        }

//        private void convertFileToImg(FileQueueParam fParam,String filePath){
//            String action = fParam.getAction();
//            String filename = fParam.getFileName();
//            String fileDir = AppUtils.getMD5(filename);
//            String forscreen_id = fParam.getForscreen_id();
//            String resource_size = fParam.getTotalSize();
//            String resource_type = fParam.getResource_type();
//            String save_type = fParam.getSave_type();
//            String serial_number = fParam.getSerial_number();
//            Bitmap pageImage;
//            // Render the page and save it to an image file
//            FileOutputStream fileOut = null;
//            InputStream inputStream = null;
//            try {
//                /**
//                 *因为正常的文件名称中可能存在中文以及特殊字符
//                 * 所以创建文件目录时，将forscreenid当做文件目录名称
//                 */
//                String basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
//                File root = new File(basePath+fileDir);
//                if (root.isDirectory()&&root.listFiles().length>0){
//                    String path = root.getAbsolutePath() + "/1.jpg";
//                    if (new File(path).exists()){
//                        ProjectOperationListener.getInstance(context).showImage(2, path, true,forscreen_id,"", avatarUrl, nickName,"","",currentAction, FROM_SERVICE_REMOTE);
//                    }
//                }else{
//                    if (!root.exists()){
//                        root.mkdir();
//                    }
//                }
//                // Load in an already created PDF
//                File pdfFile = new File(filePath);
//                if (!pdfFile.exists()){
//                    return;
//                }
//                String md5 = AppUtils.getMD5(pdfFile);
//                inputStream = new FileInputStream(pdfFile);
////                  AssetManager assetManager = getAssets();
////                  PDDocument document = PDDocument.load(assetManager.open("123.pdf"));
//                PDDocument document = PDDocument.load(inputStream);
//                // Create a renderer for the document
//                PDFRenderer renderer = new PDFRenderer(document);
//                // Render the image to an RGB Bitmap
////                  pageImage = renderer.renderImage(0, 1, ImageType.RGB);
//                int pages = document.getNumberOfPages();
//                for (int i=0;i<pages;i++){
////                        LogUtils.d("下载pdf完成，开始转换第"+i+"张");
//                    String path = root.getAbsolutePath() + "/"+(i+1)+".jpg";
//                    if (!new File(path).exists()){
//                        LogUtils.d("下载pdf完成，开始获取第"+i+"张的bitmap");
//                        pageImage = renderer.renderImage(i, 1f, Bitmap.Config.ARGB_4444);
//                        LogUtils.d("下载pdf完成，完成获取第"+i+"张的bitmap");
//                        // Save the render result to an image
//                        File renderFile = new File(path);
//                        fileOut = new FileOutputStream(renderFile);
//                        pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
//                    }
////                        LogUtils.d("下载pdf完成，完成转换第"+i+"张");
//                    if (i == 0){
//                        ProjectOperationListener.getInstance(context).showImage(2, path, true,forscreen_id,"", avatarUrl, nickName,"","",currentAction, FROM_SERVICE_REMOTE);
//                        postSimpleMiniProgramProjectionLog(action,forscreen_id,filename,resource_size,resource_type,filePath,serial_number,md5,pages,save_type);
//                    }
//                }
//                AppUtils.uplopadProjectionFile(context,root.getAbsolutePath(),fileDir);
//            }
//            catch (IOException e){
//                converted.put(filename,false);
//                Log.e("PdfBox-Android-Sample", "Exception thrown while rendering file", e);
//            }finally {
//                if (fileOut!=null){
//                    try{
//                        fileOut.close();
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//                if (inputStream!=null){
//                    try {
//                        inputStream.close();
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }

        private String showFileImgRequest(HttpServletRequest request){
            String resJson = null;
            initBaseParam(request);
            String forscreen_id = request.getParameter("forscreen_id");
            String filename = request.getParameter("filename");
            String selection = DBHelper.MediaDBInfo.FieldName.RESOURCE_ID + "=? ";
            String[] selectionArgs = new String[]{filename};
            ProjectionLogHistory projectionLog = DBHelper.get(context).findProjectionHistoryById(selection,selectionArgs);
            String duration = projectionLog.getDuration();
            String forscreen_char = projectionLog.getForscreen_char();
            String resource_size = "";
            String resource_type = request.getParameter("resource_type");
            String serial_number = projectionLog.getSerial_number();
            if (TextUtils.isEmpty(GlobalValues.CURRENT_FORSCREEN_ID)
                    ||!GlobalValues.CURRENT_FORSCREEN_ID.equals(forscreen_id)){
                clearProjectionMark(forscreen_id);
                closeProjectionDialog();
            }
            try {
                BaseResponse object;
                String startTime = String.valueOf(System.currentTimeMillis());
                res_sup_time = startTime;
                String action = request.getParameter("action");
                String playTimes = request.getParameter("play_times");
                String fileDir = AppUtils.getMD5(filename);
                String path = request.getParameter("imgpath");
                String[] imgnames = path.split("/");
                String imgName = imgnames[imgnames.length-1];
                String basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                String imgPath = basePath+fileDir+"/"+imgName;
                File file = new File(imgPath);
                boolean isDownload = false;
                if (file.exists()){
                    isDownload = true;
                }
                if (isDownload) {
                    ProjectOperationListener.getInstance(context).showImage(2, imgPath, true,playTimes,forscreen_id, avatarUrl, nickName,currentAction, FROM_SERVICE_REMOTE);
                    String endTime = String.valueOf(System.currentTimeMillis());
                    res_eup_time = endTime;
                    postSimpleMiniProgramProjectionLog(action,duration,forscreen_char,forscreen_id,imgName,resource_size,resource_type,imgPath,serial_number,false);
                    GlobalValues.PROJECT_STREAM_IMAGE.add(imgPath);
                    object = new BaseResponse();
                    object.setMsg("滑动成功");
                    object.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                }else{
                    object = new BaseResponse();
                    object.setMsg("滑动失败");
                    object.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                }

                resJson = new Gson().toJson(object);
            }catch (Exception e){
                e.printStackTrace();
            }
            return resJson;
        }

        private void setDownloadProgress(final String resource_id,final long totalSize,final long currentSize){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BigDecimal bd = new BigDecimal(currentSize*1.0/ totalSize);
                    float f1 = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                        LogUtils.d("downloadProgress|当前下载进度:currentSize="+currentSize+"除以总数:totalSize="+totalSize);
                    LogUtils.d("downloadProgress|currentSize/currentSize保留两位后得到："+f1);
                    if (f1 >= 0.01f) {
                        String value = String.valueOf(f1 * 100);
                        final int progress = Integer.valueOf(value.split("\\.")[0]);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                projectionImgListDialog.setImgDownloadProgress(resource_id,progress+"%");
                            }
                        });
                    }
                }
            }).start();


        }

        public void postSimpleMiniProgramProjectionLog(String action,String startTime,String endTime,String duration,String forscreen_char,String forscreen_id,
                                                       String resource_id,String resource_size,String resource_type,String media_path,String serial_number,
                                                       String music_path,int is_share,String public_text,String forscreen_nums,boolean repeat){
            HashMap<String,Object> params = new HashMap<>();
            String create_time = String.valueOf(System.currentTimeMillis());

            params.put("box_mac", Session.get(RemoteService.this).getEthernetMac());
            params.put("duration", duration);
            if (!TextUtils.isEmpty(forscreen_char)){
                params.put("forscreen_char", forscreen_char);
            }
            if (repeat){
                params.put("action",8);
            }else {
                params.put("action",action);
            }

            params.put("resource_id", resource_id);
            params.put("forscreen_id", forscreen_id);
            params.put("mobile_brand", deviceName);
            params.put("mobile_model", device_model);
            params.put("openid", deviceId);
            params.put("resource_size", resource_size);
            params.put("resource_type", resource_type);
            params.put("serial_number",serial_number);
            params.put("music_id", music_path);
            params.put("is_share", is_share);
            params.put("public_text", public_text);
            params.put("forscreen_nums", forscreen_nums);
            params.put("small_app_id", ConstantValues.SMALL_APP_ID_SIMPLE);
            params.put("create_time", create_time);
            params.put("res_sup_time",startTime);
            params.put("res_eup_time",endTime);
            AppApi.postSimpleMiniProgramProjectionLog(RemoteService.this,apiRequestListener,params,forscreen_id);
            if (currentAction==5||currentAction==7){
                return;//重投和滑动操作不计入本地投屏历史
            }
            ProjectionLogBean bean = new ProjectionLogBean();
            bean.setAction(action);
            bean.setSerial_number(serial_number);
            bean.setBox_mac(Session.get(RemoteService.this).getEthernetMac());
            bean.setDuration(duration);
            bean.setForscreen_char(forscreen_char);
            bean.setForscreen_id(forscreen_id);
            bean.setMobile_brand(deviceName);
            bean.setMobile_model(device_model);
            bean.setOpenid(deviceId);
            bean.setResource_id(resource_id);
            bean.setResource_size(resource_size);
            bean.setResource_type(resource_type);
            bean.setMedia_path(media_path);
            bean.setIs_share(is_share);
            if (repeat){
                bean.setRepeat("1");
            }else {
                bean.setRepeat("0");
            }
            bean.setSmall_app_id(ConstantValues.SMALL_APP_ID_SIMPLE);
            bean.setCreate_time(create_time);
            try{
                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                String[] filePaths = media_path.split("\\/");
                final String fileName = filePaths[filePaths.length-1];
                if ("1".equals(resource_type)&&!action.equals("31")){
                    String shotcutName = "img_ys_"+fileName;
                    String filePath = path+shotcutName;
                    Bitmap imageBitmap = AppUtils.getImageThumbnail(media_path,400,400);
                    AppUtils.saveImage(imageBitmap,filePath);
                    String screenshotUrl = "http://"+AppUtils.getEthernetIP()+":8080/projection/"+shotcutName;
                    bean.setMedia_screenshot_path(screenshotUrl);
                }else if ("2".equals(resource_type)){
                    Bitmap bitmap = GlideImageLoader.loadVideoScreenshot(context,media_path,2*1000*1000);
                    String shotcutName ="video_ys_1_"+fileName.split("\\.")[0]+".jpg";
                    String filePath = path+shotcutName;
                    AppUtils.saveImage(bitmap,filePath);
                    String screenshotUrl = "http://"+AppUtils.getEthernetIP()+":8080/projection/"+shotcutName;
                    bean.setMedia_screenshot_path(screenshotUrl);
                }
                /**第一次插入数据，设置未上传*/
                bean.setUpload("0");
                DBHelper.get(context).insertProjectionLog(bean);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        public void postSimpleMiniProgramProjectionLog(String action,String duration,String forscreen_char,String forscreen_id,String resource_id,
                                                       String resource_size,String resource_type,String media_path,String serial_number,
                                                       String music_path,int is_share,String public_text,String forscreen_nums,boolean repeat){
            String startTime = res_sup_time;
            String endTime = res_eup_time;
            postSimpleMiniProgramProjectionLog(action,startTime,endTime,duration,forscreen_char,forscreen_id,
                    resource_id,resource_size,resource_type,media_path,serial_number,music_path,is_share,public_text,forscreen_nums,repeat);
        }

        public void postSimpleMiniProgramProjectionLog(String action,String duration,String forscreen_char,String forscreen_id,String resource_id,
                                                       String resource_size,String resource_type,String media_path,String serial_number,boolean repeat){
            String startTime = res_sup_time;
            String endTime = res_eup_time;
            String music_path = "";
            postSimpleMiniProgramProjectionLog(action,startTime,endTime,duration,forscreen_char,forscreen_id,
                    resource_id,resource_size,resource_type,media_path,serial_number,music_path,0,"","",repeat);
        }

        //展示下载时大屏右侧的窗口列表
        private void showDownloadWindow(){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (projectionImgListDialog!=null){
                        if (projectionImgListDialog.isShowing()){
                            LogUtils.d(TAG+":"+"极简关闭窗口:初始化");
                            projectionImgListDialog.dismiss();
                        }

                        projectionImgListDialog.clearContent();
                        projectionImgListDialog.show();
                        projectionImgListDialog.setProjectionPersonInfo(avatarUrl,nickName);
                    }
                }
            });
        }
        //关闭下载时大屏右侧的窗口列表
        private void closeDownloadWindow(){
            if (projectionImgListDialog!=null&&projectionImgListDialog.isShowing()){
                LogUtils.d(TAG+":"+"极简关闭窗口:没有获取到投屏数关闭");
                projectionImgListDialog.clearContent();
                projectionImgListDialog.dismiss();
            }
        }

        private String handlePicH5Request(final HttpServletRequest request){
            String resJson;
            LogUtils.d("enter method request.handlePicH5Request");
            initBaseParam(request);
            String forscreen_id = request.getParameter("forscreen_id");
            if (TextUtils.isEmpty(GlobalValues.CURRENT_FORSCREEN_ID)
                    ||!GlobalValues.CURRENT_FORSCREEN_ID.equals(forscreen_id)){
                clearProjectionMark(forscreen_id);
                if (GlobalValues.IMG_NUM.containsKey(deviceId)){
                    if (GlobalValues.IMG_NUM.get(deviceId)!=-1){
                        GlobalValues.IMG_NUM.put(deviceId,GlobalValues.IMG_NUM.get(deviceId)+1);
                    }
                }else{
                    GlobalValues.IMG_NUM.put(deviceId,1);
                }
                showDownloadWindow();
            }
            LogUtils.d(TAG+":"+"极简图片下载:forscreen_id="+forscreen_id);
            resJson = downloadStreamImageProjection(request);

            return resJson;
        }


        private String downloadStreamImageProjection(HttpServletRequest request){
            String respJson = "";
            try {
                boolean repeat= false;
                BaseResponse object;
                String startTimee = String.valueOf(System.currentTimeMillis());
                res_sup_time = startTimee;
                String action = request.getParameter("action");
                final String forscreen_nums = request.getParameter("forscreen_nums");
                final String forscreen_char = request.getParameter("forscreen_char");
                LogUtils.d("forscreen_char===="+forscreen_char);
                words = request.getParameter("forscreen_char");
                forscreenId = request.getParameter("forscreen_id");
                String device_model = request.getParameter("device_model");
                String filename = request.getParameter("filename");
                final String forscreen_id = request.getParameter("forscreen_id");
                String resource_size = request.getParameter("resource_size");
                String resource_type = request.getParameter("resource_type");
                String duration = request.getParameter("duration");
                String serial_number = request.getParameter("serial_number");
                String music_id = request.getParameter("music_id");
                String music_oss_addr = request.getParameter("music_oss_addr");
                final String img_id = System.currentTimeMillis()+"";
                ProjectionImg img = new ProjectionImg();
                img.setImg_id(img_id);
                GlobalValues.PROJECT_STREAM_IMAGE_NUMS.add(img);
                handler.post(()->projectionImgListDialog.setContent(GlobalValues.PROJECT_STREAM_IMAGE_NUMS,TYPE_IMG));

                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
//                String fileName = filename+".jpg";小程序自主把文件名補全，modify20201225
                File file = new File(path+filename);
                int start = 0;
                boolean isDownload = false;
                LogUtils.d(TAG+":"+"极简下载:fileName="+filename+"开始下载");
                if (file.exists()){
                    repeat = true;
                    isDownload = true;
                }else {
                    MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String) null);
                    request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
                    if (request.getParts() != null) {
                        for (Part part : request.getParts()) {
                            switch (part.getName()) {
                                case "fileUpload":
                                    long startTime = System.currentTimeMillis();
                                    // 存文件
                                    RandomAccessFile raf = new RandomAccessFile(file, "rw");
                                    raf.seek(start);
                                    byte[] byteBuffer = new byte[1024*1024*10];
                                    int len = 0;
                                    long totalSize = part.getSize();
                                    int currentSize = 0;
                                    // 注意，part.getInputStream()切记不要多次调用
                                    InputStream inputStream = part.getInputStream();
                                    while ((len = inputStream.read(byteBuffer)) > 0) {
                                        raf.write(byteBuffer, 0, len);
                                        setDownloadProgress(img_id,totalSize,currentSize);
                                    }
                                    raf.close();
                                    part.delete();
                                    isDownload = true;
                                    String useTime = String.valueOf(System.currentTimeMillis()-startTime);
                                    String resourceSize = String.valueOf(file.length());
                                    String mUUID = String.valueOf(System.currentTimeMillis());
                                    LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.download, LogParamValues.speed_size,resourceSize);
                                    Thread.sleep(500);
                                    LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.download, LogParamValues.speed_duration,useTime);
                                    Thread.sleep(500);
                                    LogReportUtil.get(context).downloadLog(mUUID,LogParamValues.download, LogParamValues.speed_serial,serial_number);
                                    break;
                                default:
                                    part.delete();
                                    break;
                            }
                        }
                    }
                }

                if (isDownload) {
                    String endTime = String.valueOf(System.currentTimeMillis());
                    res_eup_time = endTime;
                    String media_path = file.getAbsolutePath();
                    LogUtils.d(TAG+":"+"极简下载:fileName="+filename+"结束下载");
                    GlobalValues.PROJECT_STREAM_IMAGE.add(media_path);

                    postSimpleMiniProgramProjectionLog(action,duration,forscreen_char,forscreen_id,filename,resource_size,resource_type,media_path,serial_number,repeat);

                    handler.post(()->projectionImgListDialog.setImgDownloadProgress(img_id,100+"%"));
                    object = new BaseResponse();
                    object.setMsg("上传成功");
                    object.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                    GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                    GlobalValues.CURRENT_PROJECT_DEVICE_NAME = deviceName;
                    GlobalValues.IS_RSTR_PROJECTION = false;
                    GlobalValues.CURRENT_PROJECT_DEVICE_IP = request.getRemoteHost();
                    AppApi.resetPhoneInterface(GlobalValues.CURRENT_PROJECT_DEVICE_IP);

                }else{
                    GlobalValues.PROJECT_STREAM_FAIL_IMAGE.add(file.getAbsolutePath());
                    object = new BaseResponse();
                    object.setMsg("上传失败");
                    object.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                }
                if (TextUtils.isEmpty(forscreen_nums)){
                    handler.postDelayed(()->closeDownloadWindow(),delayTime);
                }else {
                    int nums = Integer.valueOf(forscreen_nums);
                    if (GlobalValues.PROJECT_STREAM_IMAGE.size()+GlobalValues.PROJECT_STREAM_FAIL_IMAGE.size()==nums){
                        handler.postDelayed(()->closeDownloadWindow(),delayTime);
                    }
                }

                if (!isPPTRunnable){
                    musicPath = null;
                    String basePath = AppUtils.getFilePath(AppUtils.StorageFile.welcome_resource);
                    getMusicPathMethod(basePath,music_id,music_oss_addr);
                    currentIndex=0;
                    int time = 0;
                    if (currentAction==6&&GlobalValues.PROJECT_STREAM_IMAGE.size()==1){
                        String timeStr = request.getParameter("play_times");
                        if (!TextUtils.isEmpty(timeStr)&&!timeStr.equals("0")){
                            time = Integer.valueOf(timeStr);
                        }
                        final int t = time;
                        String url = GlobalValues.PROJECT_STREAM_IMAGE.get(currentIndex);
                        RemoteService.listener.showRestImage(4,url,0,true,forscreen_char,avatarUrl,nickName,time,GlobalValues.FROM_SERVICE_REMOTE);
                        currentIndex=1;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isPPTRunnable = true;
                                projectShowImage(forscreen_char,forscreen_id,t);
                            }
                        },1000*30);
                    }else if (currentAction==5&&GlobalValues.INTERACTION_ADS_PLAY==0){
                        preOrNextAdsBean = AppUtils.getInteractionAds(context);
                        if (preOrNextAdsBean!=null&&preOrNextAdsBean.getPlay_position()==1){
                            GlobalValues.INTERACTION_ADS_PLAY = 1;
                            final String adspath = preOrNextAdsBean.getMediaPath();
                            final String adsduration = preOrNextAdsBean.getDuration();

                            if (preOrNextAdsBean.getMedia_type()==1){
                                ProjectOperationListener.getInstance(context).showVideo(adspath,true,forscreen_id,true,adsduration,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                            }else {
                                ProjectOperationListener.getInstance(context).showImage(5, adspath, true,forscreen_id, "", avatarUrl, nickName,adsduration,"",currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                            }
                            preOrNextAdsBean = null;
                        }else{
                            isPPTRunnable = true;
                            projectShowImage(forscreen_char,forscreen_id,time);
                        }
                    }


                }
                respJson = new Gson().toJson(object);
            }catch (Exception e){
                e.printStackTrace();
            }
            return respJson;
        }


        private void projectShowImage(String forscreen_char,String forscreenId,int time){
            if (!GlobalValues.CURRENT_FORSCREEN_ID.equals(forscreenId)){
                return;
            }
            boolean isGo = false;

            if (currentAction==5||currentAction==28){
                if (GlobalValues.PROJECT_STREAM_IMAGE.size()>currentIndex){
                    isGo = true;
                    String url = GlobalValues.PROJECT_STREAM_IMAGE.get(currentIndex);
                    if (currentIndex==0){
                        RemoteService.listener.showImage(1,url,true,forscreenId,forscreen_char,avatarUrl,nickName,null,musicPath,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                    }else{
                        RemoteService.listener.showImage(1,url,false,forscreenId,forscreen_char,avatarUrl,nickName,null,null,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                    }
                }
                if (isGo){
                    handler.postDelayed(new ProjectShowImageRunnable(forscreen_char,forscreenId,time),INTERVAL_TIME);
                }else{
                    closeProjectionDialog();
                }
            }else if (currentAction==22){
                if (GlobalValues.PROJECT_THUMBNIAL_IMAGE.size()>currentIndex){
                    isGo = true;
                    BigImgBean bean = GlobalValues.PROJECT_THUMBNIAL_IMAGE.get(currentIndex);
                    String url;
                    if (!TextUtils.isEmpty(bean.getBigPath())){
                        url = bean.getBigPath();
                    }else{
                        url = bean.getThumbnailPath();
                    }
                    mpProjection.setReq_id(bean.getSerial_number());
                    mpProjection.setFilename(bean.getFilenameId());
                    mpProjection.setForscreen_id(bean.getForscreen_id());
                    mpProjection.setOpenid(bean.getDeviceId());
                    if (currentIndex==0){
                        RemoteService.listener.showImage(1,url,true,forscreenId,forscreen_char,avatarUrl,nickName,null,musicPath,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                    }else{
                        RemoteService.listener.showImage(1,url,false,forscreenId,forscreen_char,avatarUrl,nickName,null,null,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                    }
                }
                if (isGo){
                    handler.postDelayed(new ProjectShowImageRunnable(forscreen_char,forscreenId,time),INTERVAL_TIME);
                }else{
                    closeProjectionDialog();
                }
            }else if (currentAction==6){

                if (currentIndex>=GlobalValues.PROJECT_STREAM_IMAGE.size()&&GlobalValues.PROJECT_STREAM_IMAGE.size()>1){
                    if (time>0){
                        currentIndex = 0;
                    }
                }
                if (currentIndex>=GlobalValues.PROJECT_STREAM_IMAGE.size()){
                    isGo = false;
                }else {
                    isGo = true;
                }
                if (isGo){
                    String url = GlobalValues.PROJECT_STREAM_IMAGE.get(currentIndex);
                    RemoteService.listener.showRestImage(4,url,0,false,forscreen_char,avatarUrl,nickName,time,GlobalValues.FROM_SERVICE_REMOTE);
                    handler.postDelayed(new ProjectShowImageRunnable(forscreen_char,forscreenId,time),REST_INTERVAL_TIME);
                }else{
                    closeProjectionDialog();
                }


            }

        }

        private class ProjectShowImageRunnable implements Runnable{
            String forscreen_char;
            String forscreenId;
            int time;
            public ProjectShowImageRunnable(){

            }
            public ProjectShowImageRunnable(String forscreen_char,String forscreen_id,int time){
                this.forscreen_char = forscreen_char;
                this.forscreenId = forscreen_id;
                this.time = time;
            }
            @Override
            public void run() {
                currentIndex ++;
                projectShowImage(forscreen_char,forscreenId,time);
            }
        }


        private String handleSinglePicRequest(final HttpServletRequest request){

            String resJson;
            initBaseParam(request);
            String forscreen_id = request.getParameter("forscreen_id");
            if (TextUtils.isEmpty(GlobalValues.CURRENT_FORSCREEN_ID)
                    ||!GlobalValues.CURRENT_FORSCREEN_ID.equals(forscreen_id)){
                clearProjectionMark(forscreen_id);
                closeProjectionDialog();
                if (GlobalValues.IMG_NUM.containsKey(deviceId)){
                    if (GlobalValues.IMG_NUM.get(deviceId)!=-1){
                        GlobalValues.IMG_NUM.put(deviceId,GlobalValues.IMG_NUM.get(deviceId)+1);
                    }
                }else{
                    GlobalValues.IMG_NUM.put(deviceId,1);
                }
            }

            resJson = downloadSingleImageProjection(request);
            return resJson;

        }

        private String downloadSingleImageProjection(HttpServletRequest request){
            String respJson = "";
            boolean repeat = false;
            try {
                BaseResponse object;
                String startTimee = String.valueOf(System.currentTimeMillis());
                res_sup_time = startTimee;
                String action = request.getParameter("action");
                String forscreen_char = request.getParameter("forscreen_char");
                String filename = request.getParameter("filename");
                String forscreen_id = request.getParameter("forscreen_id");
                String resource_size = request.getParameter("resource_size");
                String resource_type = request.getParameter("resource_type");
                String duration = request.getParameter("duration");
                String serial_number = request.getParameter("serial_number");
                final String img_id = System.currentTimeMillis()+"";
                final MiniProgramProjection minipp = new MiniProgramProjection();
                minipp.setImg_id(img_id);
                ArrayList<MiniProgramProjection> list = new ArrayList<>();
                list.add(minipp);

                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
//                String fileName = filename+".jpg";小程序自主把文件名補全，modify20201225
                File file = new File(path+filename);
                int start = 0;
                boolean isDownload = false;
                LogUtils.d(TAG+":"+"极简下载:fileName="+filename+"开始下载");
                if (file.exists()){
//                    repeat = true;
                    isDownload = true;
                }else {
                    MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String) null);
                    request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
                    if (request.getParts() != null) {
                        for (Part part : request.getParts()) {
                            switch (part.getName()) {
                                case "fileUpload":
                                    long startTime = System.currentTimeMillis();
                                    // 存文件
                                    RandomAccessFile raf = new RandomAccessFile(file, "rw");
                                    raf.seek(start);
                                    byte[] byteBuffer = new byte[1024*1024*10];
                                    int len = 0;
                                    long totalSize = part.getSize();
                                    int currentSize = 0;
                                    // 注意，part.getInputStream()切记不要多次调用
                                    InputStream inputStream = part.getInputStream();
                                    while ((len = inputStream.read(byteBuffer)) > 0) {
                                        raf.write(byteBuffer, 0, len);
                                        setDownloadProgress(img_id,totalSize,currentSize);
                                    }
                                    raf.close();
                                    part.delete();
                                    isDownload = true;
                                    String useTime = String.valueOf(System.currentTimeMillis()-startTime);
                                    String resourceSize = String.valueOf(file.length());
                                    String mUUID = String.valueOf(System.currentTimeMillis());
                                    LogReportUtil.get(context).downloadLog(mUUID, LogParamValues.download,LogParamValues.speed_size,resourceSize);
                                    Thread.sleep(500);
                                    LogReportUtil.get(context).downloadLog(mUUID, LogParamValues.download,LogParamValues.speed_duration,useTime);
                                    Thread.sleep(500);
                                    LogReportUtil.get(context).downloadLog(mUUID, LogParamValues.download,LogParamValues.speed_serial,serial_number);
                                    break;
                                default:
                                    part.delete();
                                    break;
                            }
                        }
                    }
                }

                if (isDownload) {
                    mpProjection.setReq_id(serial_number);
                    mpProjection.setFilename(filename);
                    mpProjection.setForscreen_id(forscreen_id);
                    mpProjection.setResource_id(filename);
                    mpProjection.setOpenid(deviceId);
                    String endTime = String.valueOf(System.currentTimeMillis());
                    res_eup_time = endTime;
                    String media_path = path+filename;
                    LogUtils.d(TAG+":"+"极简下载:fileName="+filename+"结束下载");
                    postSimpleMiniProgramProjectionLog(action,duration,forscreen_char,forscreen_id,filename,resource_size,resource_type,media_path,serial_number,repeat);
                    GlobalValues.PROJECT_STREAM_IMAGE.add(media_path);
                    object = new BaseResponse();
                    object.setMsg("上传成功");
                    object.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                    GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                    GlobalValues.CURRENT_PROJECT_DEVICE_NAME = deviceName;
                    GlobalValues.IS_RSTR_PROJECTION = false;
                    GlobalValues.CURRENT_PROJECT_DEVICE_IP = request.getRemoteHost();
                    AppApi.resetPhoneInterface(GlobalValues.CURRENT_PROJECT_DEVICE_IP);

                }else{
                    object = new BaseResponse();
                    object.setMsg("上传失败");
                    object.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                }
                if (GlobalValues.PROJECT_STREAM_IMAGE.size()>0){
                    currentIndex=0;
                    String url = GlobalValues.PROJECT_STREAM_IMAGE.get(currentIndex);
                    RemoteService.listener.showImage(11,url,false,forscreen_id,forscreen_char,avatarUrl,nickName,"","",currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                }
                respJson = new Gson().toJson(object);
            }catch (Exception e){
                e.printStackTrace();
            }
            return respJson;
        }

        /**
         * 处理退出投屏请求
         *
         * @param request
         * @return
         */
        private String handleStopRequest(HttpServletRequest request) {
            LogUtils.e("enter method listener.stop");
            String resJson = null;
            initBaseParam(request);
            if (!TextUtils.isEmpty(deviceId) && deviceId.equals(GlobalValues.CURRENT_PROJECT_DEVICE_ID)) {
                String projectId = request.getParameter("projectId");
                StopResponseVo object = RemoteService.listener.stop(projectId);
                resJson = new Gson().toJson(object);

                GlobalValues.CURRENT_PROJECT_IMAGE_ID = null;
            }
            return resJson;
        }

        /**
         * 处理图片旋转请求
         *
         * @param request
         * @return
         */
        private String handleRotateRequest(HttpServletRequest request) {
            LogUtils.d("enter method listener.rotate");
            String resJson = null;
            initBaseParam(request);
            if (!TextUtils.isEmpty(deviceId) && deviceId.equals(GlobalValues.CURRENT_PROJECT_DEVICE_ID)) {
                String projectId = request.getParameter("projectId");
                RotateResponseVo object = RemoteService.listener.rotate(90, projectId);
                resJson = new Gson().toJson(object);
            }
            return resJson;
        }

        /**
         * 处理视频恢复播放请求
         *
         * @param request
         * @return
         */
        private String handleResumeRequest(HttpServletRequest request) {
            LogUtils.d("enter method listener.resume");
            String resJson = null;
            initBaseParam(request);
            if (!TextUtils.isEmpty(deviceId) && deviceId.equals(GlobalValues.CURRENT_PROJECT_DEVICE_ID)) {
                String projectId = request.getParameter("projectId");
                PlayResponseVo object = RemoteService.listener.play(1, projectId);
                resJson = new Gson().toJson(object);
            }
            return resJson;
        }

        /**
         * 处理视频暂停播放请求
         *
         * @param request
         * @return
         */
        private String handlePauseRequest(HttpServletRequest request) {
            LogUtils.d("enter method listener.pause");
            String resJson = null;
            initBaseParam(request);
            if (!TextUtils.isEmpty(deviceId) && deviceId.equals(GlobalValues.CURRENT_PROJECT_DEVICE_ID)) {
                String projectId = request.getParameter("projectId");
                PlayResponseVo object = RemoteService.listener.play(0, projectId);
                resJson = new Gson().toJson(object);
            }
            return resJson;
        }

        /**
         * 处理视频拖动进度请求
         *
         * @param request
         * @return
         */
        private String handleSeekRequest(HttpServletRequest request) {
            LogUtils.d("enter method listener.seek");
            String resJson = null;
            initBaseParam(request);
            int positionSeek = Integer.parseInt(request.getParameter("position"));
            if (!TextUtils.isEmpty(deviceId) && deviceId.equals(GlobalValues.CURRENT_PROJECT_DEVICE_ID)) {
                String projectId = request.getParameter("projectId");
                SeekResponseVo object = RemoteService.listener.seek(positionSeek, projectId);
                resJson = new Gson().toJson(object);
            }
            return resJson;
        }

        /**
         * 处理视频改变音量请求
         *
         * @param request
         * @return
         */
        private String handleVolumeRequest(HttpServletRequest request) {
            LogUtils.d("enter method listener.volume");
            String resJson = null;
            initBaseParam(request);
            int volumeAction = Integer.parseInt(request.getParameter("action"));
            if (!TextUtils.isEmpty(deviceId)) {
                GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                String projectId = request.getParameter("projectId");
                VolumeResponseVo object = RemoteService.listener.volume(volumeAction, projectId);
                resJson = new Gson().toJson(object);
            }
            return resJson;
        }

        /**
         * 处理切换视频节目请求
         * @param request
         * @return
         */
        private String handleProgramRequest(HttpServletRequest request){
            String resJson = null;
            initBaseParam(request);
            int switchAction = Integer.parseInt(request.getParameter("action"));
            if (!TextUtils.isEmpty(deviceId)) {
                GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                String projectId = request.getParameter("projectId");
                ProgramResponseVo object = RemoteService.listener.switchProgram(switchAction, projectId);
                resJson = new Gson().toJson(object);
            }
            return resJson;
        }

        private String handleShowMiniProgramCode(HttpServletRequest request){
            String resJson;
            initBaseParam(request);
            if (!TextUtils.isEmpty(deviceId)) {
                String filename = request.getParameter("filename");
                GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                GlobalValues.PROJECT_STREAM_IMAGE.clear();
                GlobalValues.PROJECT_STREAM_FAIL_IMAGE.clear();
                GlobalValues.PROJECT_STREAM_IMAGE_NUMS.clear();
//                String forscreen_id = request.getParameter("forscreen_id");
                String forscreen_id = System.currentTimeMillis()+"";
                String serial_number = System.currentTimeMillis()+"";
                handler.removeCallbacks(new ProjectShowImageRunnable());
                mpProjection.setReq_id(serial_number);
                mpProjection.setFilename(filename);
                mpProjection.setForscreen_id(forscreen_id);
                mpProjection.setResource_id(filename);
                mpProjection.setOpenid(deviceId);
                RemoteService.listener.showMiniProgramCode(filename,currentAction, FROM_SERVICE_REMOTE);
                BaseResponse vo = new BaseResponse();

                vo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                vo.setMsg("操作正确");
                resJson = new Gson().toJson(vo);
            }else{
                BaseResponse vo = new BaseResponse();
                vo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                resJson = new Gson().toJson(vo);
            }
            return resJson;
        }

        /**
         * 处理查询视频播放进度请求
         *
         * @param request
         * @return
         */
        private String handleQueryRequest(HttpServletRequest request) {
            String resJson;
            initBaseParam(request);
            LogUtils.d("enter method listener.query");
            if (!TextUtils.isEmpty(deviceId) &&
                    (deviceId.equals(GlobalValues.CURRENT_PROJECT_DEVICE_ID) ||
                            deviceId.equals(GlobalValues.LAST_PROJECT_DEVICE_ID))) {
                String projectId = request.getParameter("projectId");
                Object object = RemoteService.listener.query(projectId);
                resJson = new Gson().toJson(object);
            } else {
                QueryPosBySessionIdResponseVo vo = new QueryPosBySessionIdResponseVo();
                vo.setResult(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                resJson = new Gson().toJson(vo);
            }
            return resJson;
        }

        /**
         * 处理查询投屏进度请求
         *
         * @return
         */
        private String handleQueryStatusRequest() {
            String resJson;
            LogUtils.d("enter method listener.queryStatus");
            QueryStatusResponseVo statusVo = new QueryStatusResponseVo();
            statusVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
            statusVo.setMsg("查询成功");
            if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_DEVICE_ID)) {
                statusVo.setStatus(1);
                statusVo.setDeviceId(GlobalValues.CURRENT_PROJECT_DEVICE_ID);
                statusVo.setDeviceName(GlobalValues.CURRENT_PROJECT_DEVICE_NAME);
            } else {
                statusVo.setStatus(0);
            }
            resJson = new Gson().toJson(statusVo);
            return resJson;
        }

        /**
         * 处理验码请求
         *
         * @param request
         * @return
         */
        private String handleVerifyCodeRequest(HttpServletRequest request) {
            String resJson;
            LogUtils.d("enter method listener.verify");
            initBaseParam(request);
            if (!TextUtils.isEmpty(deviceId)) {
                String code = request.getParameter("code");
                ResponseT vo = RemoteService.listener.verify(code);
                resJson = new Gson().toJson(vo);
            } else {
                ResponseT vo = new ResponseT();
                vo.setCode(10001);
                resJson = new Gson().toJson(vo);
            }
            return resJson;
        }

        private String handleH5StopRequest() {
            LogUtils.e("enter method listener.h5/stop");
            BaseResponse stopResponse = new BaseResponse();

            GlobalValues.PROJECT_STREAM_IMAGE.clear();
            GlobalValues.PROJECT_STREAM_FAIL_IMAGE.clear();
            GlobalValues.PROJECT_THUMBNIAL_IMAGE.clear();
            handler.removeCallbacks(new ProjectShowImageRunnable());
            if (projectionImgListDialog!=null&&projectionImgListDialog.isShowing()){
                projectionImgListDialog.dismiss();
            }
            RemoteService.listener.rstrStop();
            isPPTRunnable = false;
            stopResponse.setMsg("成功");
            stopResponse.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);

            GlobalValues.IS_RSTR_PROJECTION = false;
            GlobalValues.CURRENT_PROJECT_IMAGE_ID = null;

            return new Gson().toJson(stopResponse);
        }

        /**
         * 生日星座相关点播
         * @param request
         * @return
         */
        private String handleH5BirthdayOndemand(HttpServletRequest request){
            BaseResponse response = new BaseResponse();
            try {
                initBaseParam(request);
                GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                LogUtils.d("ondemand birthday");
                clearProjectionMark("");
                String media_name = request.getParameter("media_name");
                String media_url = request.getParameter("media_url");
                String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
                String[] selectionArgs = new String[]{media_name};
                List<BirthdayOndemandBean> list = DBHelper.get(context).findBirthdayOndemandByWhere(selection,selectionArgs);
                if (list!=null&&list.size()>0){
                    String path = AppUtils.getFilePath(AppUtils.StorageFile.birthday_ondemand) + media_name;
                    File file = new File(path);
                    if (file.exists()){
                        ProjectOperationListener.getInstance(context).showVod(media_name, "3", 0, false, true,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                    }else{
                        ProjectOperationListener.getInstance(context).showVideo("",media_url,true,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                    }
                }else{
                    ProjectOperationListener.getInstance(context).showVideo("",media_url, true,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                }
                response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
            }catch (Exception e){
                e.printStackTrace();
                response.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            }

            return new Gson().toJson(response);
        }


        private String findProjectionHistory(HttpServletRequest request){
            BaseResponse response = new BaseResponse();
            String openid = request.getParameter("openid");
            String selection = DBHelper.MediaDBInfo.FieldName.OPENID + "=? and "
                    + DBHelper.MediaDBInfo.FieldName.RESOURCE_TYPE + "!= ? and "
                    + DBHelper.MediaDBInfo.FieldName.ACTION + "!= ? ";
            String[] selectionArgs = new String[]{openid,"3","31"};
            List<ProjectionLogHistory> list = DBHelper.get(context).findProjectionHistory(selection,selectionArgs);
            LogUtils.d("投屏历史查询返回");
            if (list!=null&&list.size()>0){
                response.setResult(list);
                response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                response.setMsg("查询成功");
            }else{
                response.setCode(ConstantValues.SERVER_RESPONSE_CODE_NULL);
                response.setMsg("查询數據誒空");
            }

            return new Gson().toJson(response);
        }

        private String findProjectionThumbnail(HttpServletRequest request){
            BaseResponse response = new BaseResponse();
            String forscreen_id = request.getParameter("forscreen_id");
            String selection = DBHelper.MediaDBInfo.FieldName.FORSCREEN_ID + "=? ";
            String[] selectionArgs = new String[]{forscreen_id};
            ProjectionLogHistory projectionLog = DBHelper.get(context).findProjectionHistoryById(selection,selectionArgs);
            LogUtils.d("投屏历史缩略图查询返回");
            if (projectionLog!=null){
                response.setResult(projectionLog);
                response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                response.setMsg("查询成功");
            }else{
                response.setCode(ConstantValues.SERVER_RESPONSE_CODE_NULL);
                response.setMsg("查询數據誒空");
            }

            return new Gson().toJson(response);
        }

        private String findFileImgList(HttpServletRequest request){
            BaseResponse response = new BaseResponse();
            /**forscreen_id既为原文件名和目录名*/
            String filename = request.getParameter("filename");
            if (converted.containsKey(filename)&&!converted.get(filename)){
                response.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                response.setMsg("文件转换异常");
                return new Gson().toJson(response);
            }
            String fileDir = AppUtils.getMD5(filename);
            String selection = DBHelper.MediaDBInfo.FieldName.RESOURCE_ID + "=? ";
            String[] selectionArgs = new String[]{filename};
            ProjectionLogHistory projectionLog = DBHelper.get(context).findProjectionHistoryById(selection,selectionArgs);
            if (projectionLog==null){
                response.setCode(ConstantValues.SERVER_RESPONSE_CODE_NULL);
                response.setMsg("查询數據誒空");
                return new Gson().toJson(response);
            }
            String basePath = AppUtils.getFilePath(AppUtils.StorageFile.projection);
            File fileImg = new File(basePath+fileDir);

            JSONObject jsonObject = new JSONObject();
            try{
                jsonObject.accumulate("status",2);
                jsonObject.accumulate("pages",projectionLog.getPages());
                jsonObject.accumulate("task_id",0);
                jsonObject.accumulate("percent",100);
                jsonObject.accumulate("oss_host","http://"+AppUtils.getEthernetIP()+":8080");
                JSONArray jsonArray = new JSONArray();
                if (fileImg.isDirectory()&&fileImg.listFiles().length>0){
                    for (File file:fileImg.listFiles()){
                        String name = file.getName();
                        String imgPath = "projection/"+fileDir+"/"+name;
                        jsonArray.put(imgPath);
                    }
                }
                jsonObject.accumulate("imgs",jsonArray);
                if (jsonObject!=null){
                    response.setResult(jsonObject);
                    response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                    response.setMsg("查询成功");
                }else{
                    response.setCode(ConstantValues.SERVER_RESPONSE_CODE_NULL);
                    response.setMsg("查询數據誒空");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return new Gson().toJson(response);
        }
        /**
         * 局域网提供节目单-节目下载数据
         */
        private String findProAdvListData(){
            BaseResponse response = new BaseResponse();
            String selection = "";
            String[] selectionArgs = new String[]{};
            List<MediaLibBean> list = DBHelper.get(context).findPlayListByWhere(selection,selectionArgs);
            try{
                if (list!=null){
                    SetTopBoxBean setTopBoxBean = new SetTopBoxBean();
                    setTopBoxBean.setPeriod(Session.get(context).getProPeriod());
                    setTopBoxBean.setAdvPeriod(Session.get(context).getAdvPeriod());
                    setTopBoxBean.setPlay_list((ArrayList<MediaLibBean>) list);
                    response.setResult(setTopBoxBean);
                    response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                    response.setMsg("查询节目列表成功");
                }else{
                    response.setCode(ConstantValues.SERVER_RESPONSE_CODE_NULL);
                    response.setMsg("查询节目列表數據誒空");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return new Gson().toJson(response);
        }
        /**
         * 局域网提供节目单-广告下载数据
         */
        private String findAdsListData(){
            BaseResponse response = new BaseResponse();
            String selection = "";
            String[] selectionArgs = new String[]{};
            List<MediaLibBean> list = DBHelper.get(context).findAdsByWhere(selection,selectionArgs);
            try{
                if (list!=null){
                    ProgramBean programBean = new ProgramBean();
                    programBean.setMedia_lib(list);
                    VersionInfo version = new VersionInfo();
                    version.setVersion(Session.get(context).getAdsPeriod());
                    programBean.setVersion(version);
                    response.setResult(programBean);
                    response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                    response.setMsg("查询广告列表成功");
                }else{
                    response.setCode(ConstantValues.SERVER_RESPONSE_CODE_NULL);
                    response.setMsg("查询广告列表數據誒空");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return new Gson().toJson(response);
        }
        /**
         * 局域网提供商城商品数据
         * @return
         */
        private String findshowgoodsData(){
            BaseResponse response = new BaseResponse();
            String selection = "";
            String[] selectionArgs = new String[]{};
            List<ShopGoodsBean> list = DBHelper.get(context).findShopGoodsAds(selection,selectionArgs);
            try{
                ShopGoodsResult result = new ShopGoodsResult();
                result.setDatalist(list);
                result.setPeriod(Session.get(context).getShopGoodsAdsPeriod());
                response.setResult(result);
                response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                response.setMsg("查询商城商品列表成功");
            }catch (Exception e){
                e.printStackTrace();
            }
            return new Gson().toJson(response);
        }
        /**
         * 局域网提供热播内容数据
         * @return
         */
        private String findHotPlayProData(){
            BaseResponse response = new BaseResponse();
            String selection = "";
            String[] selectionArgs = new String[]{};
            List<SelectContentBean> hotPlayContentList = DBHelper.get(context).findHotPlayContentList(selection,selectionArgs);
            try{
                if (hotPlayContentList!=null&&hotPlayContentList.size()>0){
                    SelectContentResult result = new SelectContentResult();
                    result.setDatalist(hotPlayContentList);
                    result.setPeriod(Session.get(context).getHotContentPeriod());
                    response.setResult(result);
                    response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                    response.setMsg("查询商城商品列表成功");
                }else{
                    response.setCode(ConstantValues.SERVER_RESPONSE_CODE_NULL);
                    response.setMsg("查询商城商品列表數據誒空");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return new Gson().toJson(response);
        }


        /**
         * 在每次新的投屏动作开始之前需要清除一下之前的投屏痕迹，相当于抢断
         * @param forscreen_id
         */
        private void clearProjectionMark(String forscreen_id){
            GlobalValues.PROJECT_STREAM_IMAGE.clear();
            GlobalValues.PROJECT_STREAM_FAIL_IMAGE.clear();
            GlobalValues.PROJECT_THUMBNIAL_IMAGE.clear();
            GlobalValues.PROJECT_STREAM_IMAGE_NUMS.clear();
            GlobalValues.PROJECTION_VIDEO_PATH = null;
            GlobalValues.PROJECT_IMAGES.clear();
            GlobalValues.PROJECT_FAIL_IMAGES.clear();
            GlobalValues.CURRENT_FORSCREEN_ID = forscreen_id;
            GlobalValues.CURRENT_OPEN_ID = deviceId;
            handler.removeCallbacks(new ProjectShowImageRunnable());
            isPPTRunnable = false;
            isTasking = false;
            handler.post(()->closeDownloadWindow());
            //清除一下多余视频当空间不足的时候
            if (AppUtils.getAvailableExtSize() < ConstantValues.EXTSD_LEAST_AVAILABLE_SPACE/2) {
                AppUtils.deleteOldMedia(RemoteService.this,false);
                AppUtils.deleteProjectionData(RemoteService.this);
            }
        }

        /**
         * 展示投屏时下载窗口列表
         */
        private void showProjectionDialog(){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (projectionImgListDialog!=null){
                        if (projectionImgListDialog.isShowing()){
                            LogUtils.d(TAG+":"+"极简关闭窗口:初始化");
                            projectionImgListDialog.clearContent();
                            projectionImgListDialog.dismiss();
                        }

                        projectionImgListDialog.show();
                        projectionImgListDialog.setProjectionPersonInfo(avatarUrl,nickName);
                    }
                }
            });
        }

        /**
         * 更新窗口列表内容，比如说有几个需要现在就展示几个图片
         */
        private String updateProjectionDialogNum(int projectionType){
            final String img_id = System.currentTimeMillis()+"";
            ProjectionImg img = new ProjectionImg();
            img.setImg_id(img_id);
            GlobalValues.PROJECT_STREAM_IMAGE_NUMS.add(img);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    LogUtils.d(TAG+":"+"极简关闭窗口:此处更新");
                    projectionImgListDialog.setContent(GlobalValues.PROJECT_STREAM_IMAGE_NUMS,projectionType);
                }
            });

            return img_id;
        }

        /**
         * 关闭投屏下载列表窗口
         */
        private void closeProjectionDialog(){
            handler.removeCallbacks(new ProjectShowImageRunnable());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (projectionImgListDialog!=null&&projectionImgListDialog.isShowing()){
                        LogUtils.d(TAG+":"+"极简关闭窗口:退出关闭");
                        projectionImgListDialog.clearContent();
                        projectionImgListDialog.dismiss();
                    }
                }
            });
        }

        ApiRequestListener apiRequestListener = new ApiRequestListener() {
            @Override
            public void onSuccess(AppApi.Action method, Object obj) {
                ProjectionManager.log("Notify stop success");
                LogUtils.d("Notify stop response: " + obj);
                switch (method){
                    case CP_POST_SIMPLE_MINIPROGRAM_FORSCREEN_LOG_JSON:
                        if (obj instanceof String){
                            LogUtils.d("ProjectionLog:日志上传成功删除记录="+obj);
                        }
                        break;
                }
            }

            @Override
            public void onError(AppApi.Action method, Object obj) {
                ProjectionManager.log("Notify stop error");
                switch (method){
                    case CP_POST_SIMPLE_MINIPROGRAM_FORSCREEN_LOG_JSON:

                        break;
                }
            }

            @Override
            public void onNetworkFailed(AppApi.Action method) {
                ProjectionManager.log("Notify stop network failed");
                switch (method){
                    case CP_POST_SIMPLE_MINIPROGRAM_FORSCREEN_LOG_JSON:

                        break;
                }
            }
        };

        public void startRemoteProjecion(int currentAction,String fid){
            try {
                if (GlobalValues.INTERACTION_ADS_PLAY==0){
                    Session session = Session.get(context);
                    ProjectionGuideImg guideImg = session.getGuideImg();
                    int delayTime = guideImg.getShow_time();
                    int isShow = guideImg.getIs_show();
                    int forscreenNum = guideImg.getForscreen_num();
                    if (guideImg!=null&&isShow==1){
                        if (!TextUtils.isEmpty(deviceId)
                                &&GlobalValues.IMG_NUM.containsKey(deviceId)
                                &&GlobalValues.IMG_NUM.get(deviceId)>=forscreenNum){
                            String videoName = guideImg.getVideo_filename();
                            String filePath = AppUtils.getFilePath(AppUtils.StorageFile.cache)+videoName;
                            if (new File(filePath).exists()){
                                ProjectOperationListener.getInstance(context).showImage(1,filePath,true,fid,String.valueOf(delayTime),null,null,currentAction,GlobalValues.FROM_SERVICE_MINIPROGRAM);
                                GlobalValues.IMG_NUM.put(deviceId,-1);
                                return;
                            }
                        }else if (!TextUtils.isEmpty(deviceId)
                                &&GlobalValues.VIDEO_NUM.containsKey(deviceId)
                                &&GlobalValues.VIDEO_NUM.get(deviceId)>=forscreenNum){
                            String imageName = guideImg.getImage_filename();
                            String filePath = AppUtils.getFilePath(AppUtils.StorageFile.cache)+imageName;
                            if (new File(filePath).exists()){
                                ProjectOperationListener.getInstance(context).showImage(1,filePath,true,fid,String.valueOf(delayTime),null,null,currentAction,GlobalValues.FROM_SERVICE_MINIPROGRAM);
                                GlobalValues.VIDEO_NUM.put(deviceId,-1);
                                return;
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            switch (currentAction){
                case 2:
                case 22:
                case 5:
                case 23:
                    if (GlobalValues.INTERACTION_ADS_PLAY==1){
                        if (GlobalValues.PROJECT_STREAM_IMAGE.size()>0){
                            if (!isPPTRunnable){
                                LogUtils.d("1212:启动轮播图片,集合中的值为="+GlobalValues.PROJECT_STREAM_IMAGE);
                                handler.removeCallbacks(new ProjectShowImageRunnable());
                                currentIndex =0;
                                isPPTRunnable= true;
                                projectShowImage(words,fid,0);
                            }
                        }else if (GlobalValues.PROJECTION_VIDEO_PATH!=null){
                            ProjectOperationListener.getInstance(context).showVideo(GlobalValues.PROJECTION_VIDEO_PATH,true,forscreenId, avatarUrl, nickName,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                        }
                        GlobalValues.INTERACTION_ADS_PLAY=0;
                    }else if (GlobalValues.INTERACTION_ADS_PLAY==2){
                        if (!TextUtils.isEmpty(fid)&&!TextUtils.isEmpty(forscreenId)&&!forscreenId.equals(fid)){
                            if (GlobalValues.PROJECT_STREAM_IMAGE.size()>0){
                                if (!isPPTRunnable){
                                    LogUtils.d("1212:启动轮播图片,集合中的值为="+GlobalValues.PROJECT_STREAM_IMAGE);
                                    handler.removeCallbacks(new ProjectShowImageRunnable());
                                    currentIndex =0;
                                    isPPTRunnable= true;
                                    projectShowImage(words,fid,0);
                                }
                            }else if (GlobalValues.PROJECTION_VIDEO_PATH!=null){
                                ProjectOperationListener.getInstance(context).showVideo(GlobalValues.PROJECTION_VIDEO_PATH,true,forscreenId, avatarUrl, nickName,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                            }
                        }
                        GlobalValues.INTERACTION_ADS_PLAY=0;
                    }else {
                        if (preOrNextAdsBean!=null&&preOrNextAdsBean.getPlay_position()==2){
                            String adspath = preOrNextAdsBean.getMediaPath();
                            String adsduration = preOrNextAdsBean.getDuration();
                            if (preOrNextAdsBean.getMedia_type()==1){
                                ProjectOperationListener.getInstance(context).showVideo(adspath, true,forscreenId, true,adsduration,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                            }else{
                                ProjectOperationListener.getInstance(context).showImage(5, adspath, true,forscreenId, words, avatarUrl, nickName,adsduration,"",currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                            }
                            GlobalValues.INTERACTION_ADS_PLAY=2;
                            preOrNextAdsBean = null;
                        }else{
                            GlobalValues.INTERACTION_ADS_PLAY=0;
                        }
                    }
                    break;
                default:
                    GlobalValues.PROJECT_STREAM_IMAGE.clear();
                    break;


            }
        }
    }

    public class OperationBinder extends Binder {
        public OperationBinder() {
        }

        public RemoteService getController() {
            return RemoteService.this;
        }
    }

    private class ServerThread extends Thread {
        private ServerThread() {
        }

        public void run() {
            super.run();
            if (RemoteService.this.server != null) {
                try {
//                    RemoteService.this.server.setHandler(new ControllHandler());
                    ResourceHandler resourceHandler = new ResourceHandler();
                    resourceHandler.setDirectoriesListed(true);
                    resourceHandler.setResourceBase(AppUtils.getMainMediaPath());
                    resourceHandler.setStylesheet("");
                    HandlerList handlerList = new HandlerList();
                    handlerList.setHandlers(new org.eclipse.jetty.server.Handler[]{resourceHandler,new ControllHandler()});

//                    HandlerCollection hc = new HandlerCollection();
//                    hc.setHandlers(new org.eclipse.jetty.server.Handler[]{resourceHandler,new ControllHandler()});
                    RemoteService.this.server.setHandler(handlerList);
                    RemoteService.this.server.start();
                    RemoteService.this.server.join();

                } catch (Exception var2) {
                    var2.printStackTrace();
                }
            }

        }
    }

    public interface ToPlayInterface{
        void playProjection(Object o);
    }
}

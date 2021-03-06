package com.jar.savor.box.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.policy.IFaceLockCallback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jar.savor.box.interfaces.OnRemoteOperationListener;
import com.jar.savor.box.vo.BaseResponse;
import com.jar.savor.box.vo.PlayResponseVo;
import com.jar.savor.box.vo.PptRequestVo;
import com.jar.savor.box.vo.PptResponseVo;
import com.jar.savor.box.vo.PptVideoRequestVo;
import com.jar.savor.box.vo.PptVideoResponseVo;
import com.jar.savor.box.vo.ProgramResponseVo;
import com.jar.savor.box.vo.QueryPosBySessionIdResponseVo;
import com.jar.savor.box.vo.QueryStatusResponseVo;
import com.jar.savor.box.vo.ResponseT;
import com.jar.savor.box.vo.RotateResponseVo;
import com.jar.savor.box.vo.SeekResponseVo;
import com.jar.savor.box.vo.StopResponseVo;
import com.jar.savor.box.vo.VideoPrepareRequestVo;
import com.jar.savor.box.vo.VolumeResponseVo;
import com.savor.ads.BuildConfig;
import com.savor.ads.R;
import com.savor.ads.bean.BirthdayOndemandBean;
import com.savor.ads.bean.ContentInfo;
import com.savor.ads.bean.MediaItemBean;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.MiniProgramProjection;
import com.savor.ads.bean.PptImage;
import com.savor.ads.bean.PptVideo;
import com.savor.ads.bean.ProjectionImg;
import com.savor.ads.bean.ProjectionLogBean;
import com.savor.ads.bean.SimpleRequestBean;
import com.savor.ads.callback.ProjectOperationListener;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.database.DBHelper;
import com.savor.ads.dialog.ProjectionImgListDialog;
import com.savor.ads.log.LogParamValues;
import com.savor.ads.log.LogReportUtil;
import com.savor.ads.okhttp.coreProgress.download.ProgressDownloader;
import com.savor.ads.projection.ProjectionManager;
import com.savor.ads.service.MiniProgramNettyService;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.FileUtils;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.StringUtils;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Created by zhanghq on 2016/12/22.
 */

public class RemoteService extends Service {
    private String TAG = "RemoteService";
    private Server server = new Server(ConstantValues.SERVER_REQUEST_PORT);
    private static OnRemoteOperationListener listener;
    private RemoteService.ServerThread mServerAsyncTask;
    private int INTERVAL_TIME=1000*10;
    private int REST_INTERVAL_TIME=1000*30;
    Handler handler=new Handler(Looper.getMainLooper());
    private Context context;
    ProjectionImgListDialog projectionImgListDialog = null;
    //投图片
    private int TYPE_IMG = 1;
    //投视频
    private int TYPE_VIDEO = 2;

    //是否正在播放ppt
    private boolean isPPTRunnable = false;


    private int delayTime = 1000*3;

    /**增加投屏前置或者后置广告,前置：1，后置：2*/
    private MediaLibBean preOrNextAdsBean=null;
    //给每一类请求分配一个action，用来区分业务方便操作
    private int currentAction;
    public ControllHandler controllHandler;
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
    public class ControllHandler extends AbstractHandler implements ApiRequestListener {

        private ControllHandler() {
            mLock = new Object();
            controllHandler = this;
        }

        private Object mLock;
        private String avatarUrl = null;
        private String nickName = null;
        private int currentIndex=0;
        /**
         * 投屏时屏幕显示的文字
         */
        private String words;
        /**
         * 投屏唯一标示id
         */
        private String forscreenId;


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
                    String version = request.getHeader("version");
                    try {
                        String temp = request.getParameter("web");// 是否是h5来的请求
                        if (!TextUtils.isEmpty(temp)) {
                            final boolean isWebReq = Boolean.parseBoolean(temp);
                            // 由于h5不支持改http header，故这里对isWebReq做特殊处理
                            if ("1.0".equals(version) || isWebReq) {
                                handleRequestV10(request, response, isWebReq);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                LogUtils.w("***********一次请求处理结束...***********");
            }
        }

        @NonNull
        private String getBodyString(HttpServletRequest request) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream(), Charset.forName("UTF-8")));
            char[] chars = new char[1024];
            int length = 0;
            while ((length = bufferedReader.read(chars, 0, 1024)) != -1) {
                stringBuilder.append(chars, 0, length);
            }
            return stringBuilder.toString();
        }

        /**
         * v1.0接口处理请求的逻辑
         *
         * @param request
         * @param response
         * @throws IOException
         * @throws ServletException
         */
        private void handleRequestV10(HttpServletRequest request, HttpServletResponse response, boolean isWebReq) throws IOException, ServletException {
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
                String deviceId = request.getParameter("deviceId");
                String deviceName = request.getParameter("deviceName");
                avatarUrl = request.getParameter("avatarUrl");
                nickName = request.getParameter("nickName");
                if (!TextUtils.isEmpty(deviceId)) {
                    resJson = distributeRequest(request, isWebReq, path, deviceId, deviceName);
                }
            }

            if (TextUtils.isEmpty(resJson)) {
                BaseResponse baseResponse = new BaseResponse();
                baseResponse.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                baseResponse.setMsg("操作失败");
                resJson = new Gson().toJson(baseResponse);
            }

            if (isWebReq) {
                // h5请求的响应需要包裹，否则h5取不到json
//                resJson = "h5turbine(" + resJson + ")";
            }
            try{
                response.getWriter().println(resJson);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        private String distributeRequest(final HttpServletRequest request,boolean isWebReq,String action,final String deviceId,final String deviceName) throws IOException, ServletException {
            String resJson = "";
            // 标识是否强行投屏
            LogUtils.d("enter method request.distributeRequest");
            // forceProject等于0表示不是强制抢投，等于1表示强制投，等于-1表示是老版移动端调用
            int forceProject = -1;
            switch (action) {
                case "/vod":
                    currentAction = 0;
//                    resJson = handleVodRequest(request, isWebReq, deviceId, deviceName, forceProject);
                    break;
                case "/video":
                    currentAction = 1;
//                    resJson = handleVideoRequest(request, isWebReq, deviceId, deviceName, forceProject);
                    break;
                case "/videoH5":
                case "/h5/video":
                    currentAction = 2;
                    resJson = handleVideoH5Request(request,deviceId,deviceName,resJson);
                    break;
                case "/h5/restVideo":
                    currentAction = 3;
                    resJson = handleVideoH5Request(request,deviceId,deviceName,resJson);
                    break;
                case "/pic":
                    currentAction = 4;
//                    resJson = handlePicRequest(request, isWebReq, deviceId, deviceName, resJson, forceProject);
                    break;
                case "/picH5":
                case "/h5/pic":
                    currentAction = 5;
                    resJson = handlePicH5Request(request,deviceId,deviceName);
                    break;
                case "/h5/restPicture":
                    currentAction = 6;
                    resJson = handlePicH5Request(request,deviceId,deviceName);
                    break;
                case "/h5/singleImg":
                    currentAction = 7;
                    resJson = handleSinglePicRequest(request,deviceId,deviceName,ConstantValues.SMALL_APP_ID_SIMPLE);
                    break;
                case "/h5/restSingleImg":
                    currentAction = 7;
                    resJson = handleSinglePicRequest(request,deviceId,deviceName,ConstantValues.SMALL_APP_ID_REST);
                    break;
                case "/h5/stop":
                    currentAction = 8;
                    resJson = handleH5StopRequest();
                    break;
                case "/h5/birthday_ondemand":
                    currentAction = 9;
                    resJson = handleH5BirthdayOndemand(request,deviceId);
                    break;
                case "/stop":
                    currentAction = 10;
                    resJson = handleStopRequest(request, deviceId, resJson);
                    break;
                case "/rotate":
                    currentAction = 11;
                    resJson = handleRotateRequest(request, deviceId, resJson);
                    break;
                case "/resume":
                    currentAction = 12;
                    resJson = handleResumeRequest(request, deviceId, resJson);
                    break;
                case "/pause":
                    currentAction = 13;
                    resJson = handlePauseRequest(request, deviceId, resJson);
                    break;
                case "/seek":
                    currentAction = 14;
                    resJson = handleSeekRequest(request, deviceId, resJson);
                    break;
                case "/volume":
                    currentAction = 15;
                    resJson = handleVolumeRequest(request, deviceId, resJson);
                    break;
                case "/switchProgram":
                    currentAction = 16;
                    resJson = handleProgramRequest(request,deviceId,resJson);
                    break;
                case "/showMiniProgramCode":
                    currentAction = 17;
                    resJson = handleShowMiniProgramCode(deviceId);
                    break;
                case "/query":
                    currentAction = 18;
                    resJson = handleQueryRequest(request, deviceId);
                    break;
                case "/queryStatus":
                    currentAction = 19;
                    resJson = handleQueryStatusRequest();
                    break;
                case "/showCode":
                    currentAction = 20;
                    resJson = handleShowCodeRequest(deviceId);
                    break;
                case "/verify":
                    currentAction = 21;
                    resJson = handleVerifyCodeRequest(request, deviceId);
                    break;
                case "/restaurant/stop":
                    currentAction = 26;
                    resJson = handleRstrStopRequest();
                    break;
                case "/h5/goods_ondemand":
                    currentAction = 27;
                    resJson = handleH5GoodsOndemand(request,deviceId);
                    break;
                case "/h5/discover_ondemand":
                    currentAction = 28;
                    resJson = handleH5DiscoverOndemand(request);
                    break;
                case "/h5/findGoods":
                    currentAction = 29;
                    resJson = findGoods(request);
                    break;
                case "/h5/findDiscover":
                    currentAction = 30;
                    resJson = findDiscover(request);
                    break;
                case "/h5/discover_ondemand_nonetwork":
                    currentAction = 31;
                    resJson = handleH5DiscoverOndemandNoNetwork(request);
                    break;
                case "/h5/findHotShow":
                    currentAction = 32;
                    resJson = findHotShow(request);
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

        private String handleVideoH5Request(HttpServletRequest request, String deviceId, String deviceName,String resJson){


            String forscreen_id = request.getParameter("forscreen_id");
            String video_id = System.currentTimeMillis()+"";
            final MiniProgramProjection minipp = new MiniProgramProjection();
            minipp.setVideo_id(video_id);
            if (TextUtils.isEmpty(GlobalValues.CURRRNT_PROJECT_ID)
                    ||!GlobalValues.CURRRNT_PROJECT_ID.equals(forscreen_id)){
                clearProjectionMark(forscreen_id);

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
            if (request.getContentType().contains("multipart/form-data;")) {
                // 视频流投屏处理
                resJson = downloadStreamVideoProjection(request,deviceId, deviceName,video_id);
            }
            return resJson;
        }

        private String downloadStreamVideoProjection(HttpServletRequest request,String deviceId, String deviceName,final String video_id){
            String respJson = "";
            try{
                BaseResponse object = null;
                MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String) null);
                request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
                if (request.getParts() != null) {
                    String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                    String action = request.getParameter("action");
                    String device_model = request.getParameter("device_model");
                    String filename = request.getParameter("filename");
                    String forscreen_id = request.getParameter("forscreen_id");
                    String resource_size = request.getParameter("resource_size");
                    String resource_type = request.getParameter("resource_type");
                    String duration = request.getParameter("duration");
                    String play_time = request.getParameter("play_times");
                    String serial_number = request.getParameter("serial_number");
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
                        isDownloaded = true;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                projectionImgListDialog.setImgDownloadProgress(video_id,"100%");
                            }
                        });

                    }else{
                        int start = 0;
                        long startTime = System.currentTimeMillis();
                        for (Part part : request.getParts()) {
                            switch (part.getName()) {
                                case "fileUpload":
                                    // 存文件
                                    RandomAccessFile raf = new RandomAccessFile(videoTmpFile, "rw");
                                    raf.seek(start);
                                    byte[] byteBuffer = new byte[1024*1024*10];
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
                                    part.delete();
                                    break;
                                default:
                                    part.delete();
                            }
                        }
                        isDownloaded = true;
                        videoTmpFile.renameTo(resultFile);
                        String useTime = String.valueOf(System.currentTimeMillis()-startTime);
                        String mUUid = String.valueOf(System.currentTimeMillis());
                        LogReportUtil.get(context).downloadLog(mUUid, LogParamValues.download,LogParamValues.speed_size,resource_size);
                        Thread.sleep(500);
                        LogReportUtil.get(context).downloadLog(mUUid, LogParamValues.download,LogParamValues.speed_duration,useTime);
                        Thread.sleep(500);
                        LogReportUtil.get(context).downloadLog(mUUid, LogParamValues.download,LogParamValues.speed_serial,serial_number);
                    }

                    if (isDownloaded){
                        String media_path = resultFile.getAbsolutePath();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (projectionImgListDialog!=null&&projectionImgListDialog.isShowing()){
                                    projectionImgListDialog.clearContent();
                                    projectionImgListDialog.dismiss();
                                }
                            }
                        });

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
                                        ProjectOperationListener.getInstance(context).showVideo(adspath, 0, true,forscreen_id, avatarUrl, nickName,adsduration,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                                    }else{
                                        ProjectOperationListener.getInstance(context).showImage(5, adspath, true,forscreen_id, "", avatarUrl, nickName,adsduration,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                                    }
                                    preOrNextAdsBean = null;
                                    GlobalValues.PROJECTION_VIDEO_PATH = resultFile.getAbsolutePath();
                                }else{
                                    GlobalValues.PROJECTION_VIDEO_PATH = null;
                                    RemoteService.listener.showVideo(media_path, 0, true,forscreen_id,avatarUrl,nickName,null,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                                    postSimpleMiniProgramProjectionLog(action,duration,null,forscreen_id,deviceName,device_model,deviceId,filename,resource_size,resource_type,media_path,serial_number,ConstantValues.SMALL_APP_ID_SIMPLE);
                                }

                            }
                        }else if (currentAction==3){
                            int time = 0;
                            if (!TextUtils.isEmpty(play_time)&&!play_time.equals("0")){
                                time = Integer.valueOf(play_time);
                            }
                            RemoteService.listener.showRestVideo(resultFile.getAbsolutePath(), 0, true, avatarUrl, nickName,time);
                            postSimpleMiniProgramProjectionLog(action,duration,null,forscreen_id,deviceName,device_model,deviceId,filename,resource_size,resource_type,media_path,serial_number,ConstantValues.SMALL_APP_ID_REST);
                        }
                        object = new BaseResponse();
                        object.setMsg("上传成功");
                        object.setResult(new JsonObject());
                        object.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                    }

                }
                respJson = new Gson().toJson(object);
            }catch (Exception e){
                e.printStackTrace();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (projectionImgListDialog!=null&&projectionImgListDialog.isShowing()){
                            projectionImgListDialog.clearContent();
                            projectionImgListDialog.dismiss();
                        }
                    }
                });
            }
            return respJson;
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

        private void postSimpleMiniProgramProjectionLog(String action,String duration,String forscreen_char,String forscreen_id,
                                                        String mobile_brand,String mobile_model,String openid,String resource_id,
                                                        String resource_size,String resource_type,String media_path,String serial_number,
                                                        String small_app_id){
            HashMap<String,Object> params = new HashMap<>();
            String create_time = String.valueOf(System.currentTimeMillis());
            params.put("action",action);
            params.put("serial_number",serial_number);
            params.put("box_mac", Session.get(RemoteService.this).getEthernetMac());
            params.put("duration", duration);
            if (!TextUtils.isEmpty(forscreen_char)){
                params.put("forscreen_char", forscreen_char);
            }
            params.put("forscreen_id", forscreen_id);
            params.put("mobile_brand", mobile_brand);
            params.put("mobile_model", mobile_model);
            params.put("openid", openid);
            params.put("resource_id", resource_id);
            params.put("resource_size", resource_size);
            params.put("resource_type", resource_type);
            params.put("create_time", create_time);
            params.put("small_app_id", small_app_id);
            AppApi.postSimpleMiniProgramProjectionLog(RemoteService.this,ControllHandler.this,params,forscreen_id);
            ProjectionLogBean bean = new ProjectionLogBean();
            bean.setAction(action);
            bean.setSerial_number(serial_number);
            bean.setBox_mac(Session.get(RemoteService.this).getEthernetMac());
            bean.setDuration(duration);
            bean.setForscreen_char(forscreen_char);
            bean.setForscreen_id(forscreen_id);
            bean.setMobile_brand(mobile_brand);
            bean.setMobile_model(mobile_model);
            bean.setOpenid(openid);
            bean.setResource_id(resource_id);
            bean.setResource_size(resource_size);
            bean.setResource_type(resource_type);
            bean.setMedia_path(media_path);
            bean.setSmall_app_id(small_app_id);
            bean.setCreate_time(create_time);
            DBHelper.get(context).insertProjectionLog(bean);
        }

        private String handleStreamImageProjection(HttpServletRequest request, int imageType,
                                                   String deviceId, String deviceName, String isThumbnail,
                                                   String imageId, int rotation, String seriesId, int forceProject,
                                                   boolean isWebReq) throws IOException, ServletException {
            String respJson = "";
            if (TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_DEVICE_ID) ||
                    deviceId.equals(GlobalValues.CURRENT_PROJECT_DEVICE_ID)) {

                BaseResponse object = null;

                boolean showImage = false;
                if ("1".equals(isThumbnail)) {
                    // 缩略图
                    GlobalValues.CURRENT_PROJECT_IMAGE_ID = imageId;
                    showImage = true;
                } else {
                    // 大图
                    if (!TextUtils.isEmpty(imageId) &&
                            imageId.equals(GlobalValues.CURRENT_PROJECT_IMAGE_ID)) {
                        showImage = true;
                    }
                }

                if (showImage) {
                    Bitmap bitmap = null;

                    MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String) null);
                    request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
                    if (request.getParts() != null) {
                        for (Part part : request.getParts()) {
                            switch (part.getName()) {
                                case "fileUpload":
                                    bitmap = BitmapFactory.decodeStream(part.getInputStream());
                                    part.delete();
                                    break;
                                default:
                                    part.delete();
                                    break;
                            }
                        }

//                        FileOutputStream outputStream = new FileOutputStream(AppUtils.getSDCardPath() + System.currentTimeMillis() + ".jpg");
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        // 显示图片
                        GlobalValues.CURRENT_PROJECT_BITMAP = bitmap;
                        object = RemoteService.listener.showImage(imageType, rotation, "1".equals(isThumbnail), seriesId, TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_DEVICE_ID),GlobalValues.FROM_SERVICE_REMOTE);
                    } else {
                        // 请求格式错误
                        object = new BaseResponse();
                        object.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                    }
                } else {
                    // 图片被忽略
                    object = new BaseResponse();
                    object.setCode(ConstantValues.SERVER_RESPONSE_CODE_IMAGE_ID_CHECK_FAILED);
                }
                if (object.getCode() == AppApi.HTTP_RESPONSE_STATE_SUCCESS) {
                    GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                    GlobalValues.CURRENT_PROJECT_DEVICE_NAME = deviceName;
                    GlobalValues.IS_RSTR_PROJECTION = false;
                    GlobalValues.CURRENT_PROJECT_DEVICE_IP = request.getRemoteHost();
                    AppApi.resetPhoneInterface(GlobalValues.CURRENT_PROJECT_DEVICE_IP);
                }

                respJson = new Gson().toJson(object);
            } else {
                if (isWebReq || GlobalValues.IS_LOTTERY) {
                    BaseResponse vo = new BaseResponse();
                    vo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                    if (GlobalValues.IS_LOTTERY) {
                        vo.setMsg("请稍等，" + GlobalValues.CURRENT_PROJECT_DEVICE_NAME + " 正在砸蛋");
                    } else {
                        vo.setMsg("请稍等，" + GlobalValues.CURRENT_PROJECT_DEVICE_NAME + " 正在投屏");
                    }
                    respJson = new Gson().toJson(vo);
                } else {
                    if (forceProject == 1) {
                        BaseResponse object = null;

                        boolean showImage = false;
                        if ("1".equals(isThumbnail)) {
                            // 缩略图
                            GlobalValues.CURRENT_PROJECT_IMAGE_ID = imageId;
                            showImage = true;
                        } else {
                            // 大图
                            if (!TextUtils.isEmpty(imageId) &&
                                    imageId.equals(GlobalValues.CURRENT_PROJECT_IMAGE_ID)) {
                                showImage = true;
                            }
                        }

                        if (showImage) {
                            Bitmap bitmap = null;

                            MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String) null);
                            request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
                            if (request.getParts() != null) {
                                for (Part part : request.getParts()) {
                                    switch (part.getName()) {
                                        case "fileUpload":
                                            bitmap = BitmapFactory.decodeStream(part.getInputStream());
                                            part.delete();
                                            break;
                                        default:
                                            part.delete();
                                            break;
                                    }
                                }

                                // 显示图片
                                GlobalValues.CURRENT_PROJECT_BITMAP = bitmap;
                                object = RemoteService.listener.showImage(imageType, rotation, "1".equals(isThumbnail), seriesId, true,GlobalValues.FROM_SERVICE_REMOTE);
                            } else {
                                // 请求格式错误
                                object = new BaseResponse();
                                object.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                            }
                        } else {
                            // 图片被忽略
                            object = new BaseResponse();
                            object.setCode(ConstantValues.SERVER_RESPONSE_CODE_IMAGE_ID_CHECK_FAILED);
                        }
                        if (object.getCode() == AppApi.HTTP_RESPONSE_STATE_SUCCESS) {
                            // 通知上一个投屏者已被抢投
                            if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_DEVICE_IP)) {
                                AppApi.notifyStop(RemoteService.this, this, 1, deviceName);
                            }

                            GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                            GlobalValues.CURRENT_PROJECT_DEVICE_NAME = deviceName;
                            GlobalValues.IS_RSTR_PROJECTION = false;
                            GlobalValues.CURRENT_PROJECT_DEVICE_IP = request.getRemoteHost();
                            AppApi.resetPhoneInterface(GlobalValues.CURRENT_PROJECT_DEVICE_IP);
                        }

                        respJson = new Gson().toJson(object);
                    } else if (forceProject == -1) {
                        BaseResponse vo = new BaseResponse();
                        vo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                        if (GlobalValues.IS_LOTTERY) {
                            vo.setMsg("请稍等，" + GlobalValues.CURRENT_PROJECT_DEVICE_NAME + " 正在砸蛋");
                        } else {
                            vo.setMsg("请稍等，" + GlobalValues.CURRENT_PROJECT_DEVICE_NAME + " 正在投屏");
                        }
                        respJson = new Gson().toJson(vo);
                    } else {
                        BaseResponse vo = new BaseResponse();
                        vo.setCode(ConstantValues.SERVER_RESPONSE_CODE_ANOTHER_PROJECT);
                        vo.setMsg(GlobalValues.CURRENT_PROJECT_DEVICE_NAME);

                        respJson = new Gson().toJson(vo);
                    }
                }
            }
            return respJson;
        }

        private String handlePicH5Request(final HttpServletRequest request,final String deviceId, final String deviceName){
            String resJson = null;
            LogUtils.d("enter method request.handlePicH5Request");
            String forscreen_id = request.getParameter("forscreen_id");
            if (TextUtils.isEmpty(GlobalValues.CURRRNT_PROJECT_ID)
                    ||!GlobalValues.CURRRNT_PROJECT_ID.equals(forscreen_id)){
                clearProjectionMark(forscreen_id);

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
            if (request.getContentType().contains("multipart/form-data;")) {
                LogUtils.d(TAG+":"+"极简图片下载:forscreen_id="+forscreen_id);
                resJson = downloadStreamImageProjection(request,deviceId, deviceName);
            }
            return resJson;
        }


        private String downloadStreamImageProjection(HttpServletRequest request,String deviceId, String deviceName){
            String respJson = "";
            try {
                BaseResponse object = null;
                String action = request.getParameter("action");
                final String forscreen_nums = request.getParameter("forscreen_nums");
                final String forscreen_char = request.getParameter("forscreen_char");
                words = request.getParameter("forscreen_char");
                forscreenId = request.getParameter("forscreen_id");
                String device_model = request.getParameter("device_model");
                String filename = request.getParameter("filename");
                final String forscreen_id = request.getParameter("forscreen_id");
                String resource_size = request.getParameter("resource_size");
                String resource_type = request.getParameter("resource_type");
                String duration = request.getParameter("duration");
                String serial_number = request.getParameter("serial_number");
                final String img_id = System.currentTimeMillis()+"";
                ProjectionImg img = new ProjectionImg();
                img.setImg_id(img_id);
                GlobalValues.PROJECT_STREAM_IMAGE_NUMS.add(img);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.d(TAG+":"+"极简关闭窗口:此处更新");
                        projectionImgListDialog.setContent(GlobalValues.PROJECT_STREAM_IMAGE_NUMS,TYPE_IMG);
                    }
                });

                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                String fileName = filename+".jpg";
                File file = new File(path+fileName);
                int start = 0;
                boolean isDownload = false;
                LogUtils.d(TAG+":"+"极简下载:fileName="+fileName+"开始下载");
                if (file.exists()){
                    isDownload = true;
                }else {
                    long startTime = System.currentTimeMillis();
                    MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String) null);
                    request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
                    if (request.getParts() != null) {
                        for (Part part : request.getParts()) {
                            switch (part.getName()) {
                                case "fileUpload":
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
                                    break;
                                default:
                                    part.delete();
                                    break;
                            }
                        }
                        String useTime = String.valueOf(System.currentTimeMillis()-startTime);
                        String mUUid = String.valueOf(System.currentTimeMillis());
                        LogReportUtil.get(context).downloadLog(mUUid,LogParamValues.download,LogParamValues.speed_size,resource_size);
                        Thread.sleep(500);
                        LogReportUtil.get(context).downloadLog(mUUid,LogParamValues.download,LogParamValues.speed_duration,useTime);
                        Thread.sleep(500);
                        LogReportUtil.get(context).downloadLog(mUUid,LogParamValues.download,LogParamValues.speed_serial,serial_number);
                    }
                }

                if (isDownload) {
                    String media_path = file.getAbsolutePath();
                    LogUtils.d(TAG+":"+"极简下载:fileName="+fileName+"结束下载");
                    GlobalValues.PROJECT_STREAM_IMAGE.add(media_path);
                    if (currentAction==5){
                        postSimpleMiniProgramProjectionLog(action,duration,forscreen_char,forscreen_id,deviceName,device_model,deviceId,filename,resource_size,resource_type,media_path,serial_number,ConstantValues.SMALL_APP_ID_SIMPLE);
                    }else{
                        postSimpleMiniProgramProjectionLog(action,duration,forscreen_char,forscreen_id,deviceName,device_model,deviceId,filename,resource_size,resource_type,media_path,serial_number,ConstantValues.SMALL_APP_ID_REST);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            projectionImgListDialog.setImgDownloadProgress(img_id,100+"%");
                        }
                    });
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
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (projectionImgListDialog!=null&&projectionImgListDialog.isShowing()){
                                LogUtils.d(TAG+":"+"极简关闭窗口:没有获取到投屏数关闭");
                                projectionImgListDialog.clearContent();
                                projectionImgListDialog.dismiss();
                            }
                        }
                    },delayTime);
                }else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int nums = Integer.valueOf(forscreen_nums);
                            if (GlobalValues.PROJECT_STREAM_IMAGE.size()+GlobalValues.PROJECT_STREAM_FAIL_IMAGE.size()==nums){
                                if (projectionImgListDialog!=null&&projectionImgListDialog.isShowing()){
                                    LogUtils.d(TAG+":"+"极简关闭窗口:下载完成关闭");
                                    projectionImgListDialog.clearContent();
                                    projectionImgListDialog.dismiss();
                                }
                            }
                        }
                    },delayTime);
                }

                if (!isPPTRunnable){
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
                                ProjectOperationListener.getInstance(context).showVideo(adspath, 0, true,forscreen_id, avatarUrl, nickName,adsduration,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                            }else {
                                ProjectOperationListener.getInstance(context).showImage(5, adspath, true,forscreen_id, words, avatarUrl, nickName,adsduration,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                            }
                            preOrNextAdsBean = null;
                        }else{
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


        private void projectShowImage(String words,String forscreenId,int time){
            boolean isGo = false;
            isPPTRunnable = true;
            if (currentAction==5||currentAction==28){
                if (GlobalValues.PROJECT_STREAM_IMAGE.size()>currentIndex){
                    isGo = true;
                    String url = GlobalValues.PROJECT_STREAM_IMAGE.get(currentIndex);
                    if (currentIndex==0){
                        RemoteService.listener.showImage(1,url,false,forscreenId,words,avatarUrl,nickName,null,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                    }else{
                        RemoteService.listener.showImage(1,url,true,forscreenId,words,avatarUrl,nickName,null,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                    }
                }
                if (isGo){
                    handler.postDelayed(new ProjectShowImageRunnable(words,forscreenId,time),INTERVAL_TIME);
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
                    RemoteService.listener.showRestImage(4,url,0,false,words,avatarUrl,nickName,time,GlobalValues.FROM_SERVICE_REMOTE);
                    handler.postDelayed(new ProjectShowImageRunnable(words,forscreenId,time),REST_INTERVAL_TIME);
                }else{
                    closeProjectionDialog();
                }


            }

        }

        private class ProjectShowImageRunnable implements Runnable{
            String words;
            String forscreenId;
            int time;
            public ProjectShowImageRunnable(){

            }
            public ProjectShowImageRunnable(String words,String forscreen_id,int time){
                this.words = words;
                this.forscreenId = forscreen_id;
                this.time = time;
            }
            @Override
            public void run() {
                currentIndex ++;
                projectShowImage(words,forscreenId,time);
            }
        }


        private String handleSinglePicRequest(final HttpServletRequest request,final String deviceId, final String deviceName,String projectionFrom){

            String resJson = null;
            String forscreen_id = request.getParameter("forscreen_id");
            if (TextUtils.isEmpty(GlobalValues.CURRRNT_PROJECT_ID)
                    ||!GlobalValues.CURRRNT_PROJECT_ID.equals(forscreen_id)){
                clearProjectionMark(forscreen_id);

                closeProjectionDialog();
            }
            if (request.getContentType().contains("multipart/form-data;")) {
                resJson = downloadSingleImageProjection(request,deviceId, deviceName,projectionFrom);
            }
            return resJson;

        }

        private String downloadSingleImageProjection(HttpServletRequest request,String deviceId, String deviceName,String projectionFrom){
            String respJson = "";
            try {
                BaseResponse object = null;
                String action = request.getParameter("action");
                String forscreen_char = request.getParameter("forscreen_char");
                String device_model = request.getParameter("device_model");
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
                String fileName = filename+".jpg";
                File file = new File(path+fileName);
                int start = 0;
                boolean isDownload = false;
                LogUtils.d(TAG+":"+"极简下载:fileName="+fileName+"开始下载");
                if (file.exists()){
                    isDownload = true;
                }else {
                    long startTime = System.currentTimeMillis();
                    MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String) null);
                    request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfigElement);
                    if (request.getParts() != null) {
                        for (Part part : request.getParts()) {
                            switch (part.getName()) {
                                case "fileUpload":
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
                                    break;
                                default:
                                    part.delete();
                                    break;
                            }
                        }
                        String useTime = String.valueOf(System.currentTimeMillis()-startTime);
                        String mUUid = String.valueOf(System.currentTimeMillis());
                        LogReportUtil.get(context).downloadLog(mUUid,LogParamValues.download,LogParamValues.speed_size,resource_size);
                        Thread.sleep(500);
                        LogReportUtil.get(context).downloadLog(mUUid,LogParamValues.download,LogParamValues.speed_duration,useTime);
                        Thread.sleep(500);
                        LogReportUtil.get(context).downloadLog(mUUid,LogParamValues.download,LogParamValues.speed_serial,serial_number);
                    }
                }

                if (isDownload) {
                    String media_path = path+fileName;
                    LogUtils.d(TAG+":"+"极简下载:fileName="+fileName+"结束下载");
                    postSimpleMiniProgramProjectionLog(action,duration,forscreen_char,forscreen_id,deviceName,device_model,deviceId,filename,resource_size,resource_type,media_path,serial_number,projectionFrom);
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
                    RemoteService.listener.showImage(1,url,true,forscreen_id,forscreen_char,avatarUrl,nickName,GlobalValues.FROM_SERVICE_REMOTE);
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
         * @param deviceId
         * @param resJson
         * @return
         */
        private String handleStopRequest(HttpServletRequest request, String deviceId, String resJson) {
            LogUtils.e("enter method listener.stop");
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
         * @param deviceId
         * @param resJson
         * @return
         */
        private String handleRotateRequest(HttpServletRequest request, String deviceId, String resJson) {
            LogUtils.d("enter method listener.rotate");
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
         * @param deviceId
         * @param resJson
         * @return
         */
        private String handleResumeRequest(HttpServletRequest request, String deviceId, String resJson) {
            LogUtils.d("enter method listener.resume");
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
         * @param deviceId
         * @param resJson
         * @return
         */
        private String handlePauseRequest(HttpServletRequest request, String deviceId, String resJson) {
            LogUtils.d("enter method listener.pause");
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
         * @param deviceId
         * @param resJson
         * @return
         */
        private String handleSeekRequest(HttpServletRequest request, String deviceId, String resJson) {
            LogUtils.d("enter method listener.seek");
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
         * @param deviceId
         * @param resJson
         * @return
         */
        private String handleVolumeRequest(HttpServletRequest request, String deviceId, String resJson) {
            LogUtils.d("enter method listener.volume");
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
         * @param deviceId
         * @param resJson
         * @return
         */
        private String handleProgramRequest(HttpServletRequest request, String deviceId, String resJson){
            int switchAction = Integer.parseInt(request.getParameter("action"));
            if (!TextUtils.isEmpty(deviceId)) {
                GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                String projectId = request.getParameter("projectId");
                ProgramResponseVo object = RemoteService.listener.switchProgram(switchAction, projectId);
                resJson = new Gson().toJson(object);
            }
            return resJson;
        }

        private String handleShowMiniProgramCode(String deviceId){
            String resJson;
            if (!TextUtils.isEmpty(deviceId)) {
                GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                GlobalValues.PROJECT_STREAM_IMAGE.clear();
                GlobalValues.PROJECT_STREAM_FAIL_IMAGE.clear();
                GlobalValues.PROJECT_STREAM_IMAGE_NUMS.clear();
                handler.removeCallbacks(new ProjectShowImageRunnable());
                RemoteService.listener.showMiniProgramCode();
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
         * @param deviceId
         * @return
         */
        private String handleQueryRequest(HttpServletRequest request, String deviceId) {
            String resJson;
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
         * 处理显示数字码请求
         *
         * @param deviceId
         * @return
         */
        private String handleShowCodeRequest(String deviceId) {
            String resJson;
            LogUtils.d("enter method listener.showCode");
            if (!TextUtils.isEmpty(deviceId)) {
                GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                RemoteService.listener.showCode();
                ResponseT vo = new ResponseT();

                vo.setCode(10000);
                resJson = new Gson().toJson(vo);
            } else {
                ResponseT vo = new ResponseT();
                vo.setCode(10001);
                resJson = new Gson().toJson(vo);
            }
            return resJson;
        }

        /**
         * 处理验码请求
         *
         * @param request
         * @param deviceId
         * @return
         */
        private String handleVerifyCodeRequest(HttpServletRequest request, String deviceId) {
            String resJson;
            LogUtils.d("enter method listener.verify");
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
         * @param deviceId
         * @return
         */
        private String handleH5BirthdayOndemand(HttpServletRequest request,String deviceId){
            BaseResponse response = new BaseResponse();
            try {
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
                        ProjectOperationListener.getInstance(context).showVod(media_name, "3", 0, false, true,GlobalValues.FROM_SERVICE_REMOTE);
                    }else{
                        ProjectOperationListener.getInstance(context).showVideo(media_url, 0, true,GlobalValues.FROM_SERVICE_REMOTE);
                    }
                }else{
                    ProjectOperationListener.getInstance(context).showVideo(media_url, 0, true,GlobalValues.FROM_SERVICE_REMOTE);
                }
                response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
            }catch (Exception e){
                e.printStackTrace();
                response.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            }

            return new Gson().toJson(response);
        }

        /***
         * 处理餐厅端退出投屏请求
         * @return
         */
        private String handleRstrStopRequest() {
            LogUtils.e("enter method listener.restaurant/stop");
            BaseResponse stopResponse = new BaseResponse();

            GlobalValues.PROJECT_STREAM_IMAGE.clear();
            handler.removeCallbacks(new ProjectShowImageRunnable());
            if (projectionImgListDialog!=null&&projectionImgListDialog.isShowing()){
                projectionImgListDialog.dismiss();
            }
            RemoteService.listener.rstrStop();
            stopResponse.setMsg("成功");
            stopResponse.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);

            GlobalValues.IS_RSTR_PROJECTION = false;
            GlobalValues.CURRENT_PROJECT_IMAGE_ID = null;

            return new Gson().toJson(stopResponse);
        }

        /**
         * 优选商品点播
         * @param request
         * @param deviceId
         * @return
         */
        private String handleH5GoodsOndemand(HttpServletRequest request,String deviceId){
            BaseResponse response = new BaseResponse();
            try {
                GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                LogUtils.d("ondemand goods");
                String forscreen_id = request.getParameter("forscreen_id");
                clearProjectionMark(forscreen_id);
                String media_id = request.getParameter("media_id");
                String media_name = request.getParameter("media_name");
                String media_url = request.getParameter("media_url");
                String selection = DBHelper.MediaDBInfo.FieldName.VID + "=? ";
                String[] selectionArgs = new String[]{media_id};
                List<MediaLibBean> list = DBHelper.get(context).findActivityAdsByWhere(selection,selectionArgs);
                if (list!=null&&list.size()>0){
                    String path = AppUtils.getFilePath(AppUtils.StorageFile.activity_ads) + media_name;
                    File file = new File(path);
                    if (file.exists()){
                        ProjectOperationListener.getInstance(context).showVideo(path, 0,true,GlobalValues.FROM_SERVICE_REMOTE);
                    }else{
                        ProjectOperationListener.getInstance(context).showVideo(media_url, 0, true,GlobalValues.FROM_SERVICE_REMOTE);
                    }
                }else{
                    ProjectOperationListener.getInstance(context).showVideo(media_url, 0, true,GlobalValues.FROM_SERVICE_REMOTE);
                }
                response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
            }catch (Exception e){
                e.printStackTrace();
                response.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            }

            return new Gson().toJson(response);
        }

        /**
         * 查询本机已经存在的优选商品
         * @param request
         * @return
         */
        private String findGoods(HttpServletRequest request){
            BaseResponse response = new BaseResponse();
            String selection = null;
            String[] selectionArgs = null;
            List<MediaLibBean> list = DBHelper.get(context).findActivityAdsByWhere(selection,selectionArgs);
            JsonArray jsonArray = new JsonArray();
            String basePath = AppUtils.getFilePath(AppUtils.StorageFile.activity_ads);
            if (list!=null&&list.size()>0){
               for (MediaLibBean bean:list){
                    String mediaName = bean.getName();
                    String path = basePath+mediaName;
                    File file = new File(path);
                    if (file.exists()){
                        try {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("goods_id",bean.getGoods_id());
                            jsonArray.add(jsonObject);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

               }
            }
            if (jsonArray!=null&&jsonArray.size()>0){
                response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                response.setMsg("机顶盒优选数据");
                response.setResult(jsonArray);
            }
            return new Gson().toJson(response);
        }

        /**
         * 发现视频点播(无网状态)
         * @param request
         * @return
         */
        private String handleH5DiscoverOndemandNoNetwork(HttpServletRequest request){
            BaseResponse response = new BaseResponse();
            try{
                LogUtils.d("ondemand discover");
                String filename = request.getParameter("filename");

                String path = AppUtils.getFilePath(AppUtils.StorageFile.select_content)+filename;
                if (new File(path).exists()){
                    ProjectOperationListener.getInstance(context).showVideo(path, 0,true,GlobalValues.FROM_SERVICE_REMOTE);
                }else{
                    path =  AppUtils.getFilePath(AppUtils.StorageFile.media)+filename;
                    if (new File(path).exists()){
                        ProjectOperationListener.getInstance(context).showVideo(path, 0,true,GlobalValues.FROM_SERVICE_REMOTE);
                    }
                }
                response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                response.setMsg("播放成功");
                response.setResult(new JsonObject());
            }catch (Exception e){
                e.printStackTrace();
                response.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            }
            return new Gson().toJson(response);
        }

        /**
         * 查询本机发现商品，如发现不存在则查询节目单数据返回
         * @param request
         * @return
         */
        private String findDiscover(HttpServletRequest request){
            BaseResponse response = new BaseResponse();
            String selection = DBHelper.MediaDBInfo.FieldName.TYPE + "=? ";
            String[] selectionArgs = new String[]{"2"};
            List<MediaItemBean> listItem = DBHelper.get(context).findMediaItemList(selection,selectionArgs);
            JsonArray jsonArray = new JsonArray();
            if (listItem!=null&&listItem.size()>0){
                String baseSelectPath = AppUtils.getFilePath(AppUtils.StorageFile.select_content);
                for (MediaItemBean item:listItem){
                    String mediaName = item.getName();
                    String path = baseSelectPath+mediaName;
                    File file = new File(path);
                    if (file.exists()){
                        try {
                            String url = "http://"+AppUtils.getEthernetIP()+":"+ConstantValues.SERVER_REQUEST_PORT+"/"+AppUtils.StorageFile.select_content+"/"+mediaName;
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("url",url);
                            jsonArray.add(jsonObject);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

            }else{
                String baseMediaPath = AppUtils.getFilePath(AppUtils.StorageFile.media);
                Map<String,String> urlMap = new HashMap<>();
                selection = DBHelper.MediaDBInfo.FieldName.MEDIATYPE + "=? and " +
                        DBHelper.MediaDBInfo.FieldName.PERIOD + "=? ";
                selectionArgs = new String[]{ConstantValues.PRO,Session.get(context).getProPeriod()};
                List<MediaLibBean> listLib = DBHelper.get(context).findPlayListByWhere(selection,selectionArgs);
                if (listLib!=null&&listLib.size()>0){
                    for (MediaLibBean lib:listLib){
                        String mediaName = lib.getName();
                        String path = baseMediaPath+mediaName;
                        File file = new File(path);
                        if (file.exists()){
                            String url = "http://"+AppUtils.getEthernetIP()+":"+ConstantValues.SERVER_REQUEST_PORT+"/"+AppUtils.StorageFile.media+"/"+mediaName;
                            urlMap.put(mediaName,url);
                        }
                    }
                    for (Map.Entry<String, String> m : urlMap.entrySet()) {
                        try {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("url",m.getValue());
                            jsonArray.add(jsonObject);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }
            }

            if (jsonArray!=null&&jsonArray.size()>0){
                response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                response.setMsg("机顶盒缓存发现以及节目单数据");
                response.setResult(jsonArray);
            }
            return new Gson().toJson(response);
        }

        /**
         * 发现热播视频点播
         * @param request
         * @return
         */
        private String handleH5DiscoverOndemand(HttpServletRequest request){
            BaseResponse response = new BaseResponse();
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            try{
                LogUtils.d("ondemand discover");
                String deviceId = request.getParameter("deviceId");
                String media_id = request.getParameter("media_id");
                String media_url = request.getParameter("media_url");
                String forscreen_id = request.getParameter("forscreen_id");
                String resource_type = request.getParameter("resource_type");//1图片，2视频
                clearProjectionMark(forscreen_id);
                showProjectionDialog();
                List<ContentInfo> listContent= gson.fromJson(media_url, new TypeToken<List<ContentInfo>>() {
                }.getType());

                String selection = DBHelper.MediaDBInfo.FieldName.ID + "=? ";
                String[] selectionArgs = new String[]{media_id};
                if (listContent!=null&&listContent.size()>0){
                    String basePath = AppUtils.getFilePath(AppUtils.StorageFile.select_content);
                    if ("1".equals(resource_type)){
                        boolean isDownload;
                        List<MediaItemBean> listItem = DBHelper.get(context).findMediaItemList(selection,selectionArgs);

                        if (listItem!=null&&listItem.size()>0) {
                            for (MediaItemBean bean:listItem){
                                String img_id = updateProjectionDialogNum(TYPE_IMG);
                                String fileName = bean.getName();
                                String path = basePath + fileName;
                                File file = new File(path);
                                if (file.exists()){
                                    handler.post(()->projectionImgListDialog.setImgDownloadProgress(img_id,100+"%"));
                                    GlobalValues.PROJECT_STREAM_IMAGE.add(path);
                                    if (!isPPTRunnable){
                                        currentIndex=0;
                                        projectShowImage("",forscreen_id,0);
                                    }
                                }
                            }
                            isDownload = true;
                        }else{
                            for (ContentInfo content:listContent){
                                String img_id = updateProjectionDialogNum(TYPE_IMG);
                                String fileName = content.getFilename();
                                String url = BuildConfig.OSS_ENDPOINT+content.getForscreen_url();
                                String path = basePath + fileName;
                                boolean isSuccess;
                                if (new File(path).exists()){
                                    isSuccess = true;
                                }else{
                                    isSuccess = new ProgressDownloader(context,url,basePath,fileName,true).downloadByRange();
                                }
                                if (isSuccess){
                                    handler.post(()->projectionImgListDialog.setImgDownloadProgress(img_id,100+"%"));

                                    GlobalValues.PROJECT_STREAM_IMAGE.add(path);
                                    if (!isPPTRunnable){
                                        currentIndex=0;
                                        projectShowImage("",forscreen_id,0);
                                    }
                                }
                            }
                            isDownload = true;
                        }
                        if (isDownload) {
                            closeProjectionDialog();
                            response.setMsg("上传成功");
                            response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                            GlobalValues.CURRENT_PROJECT_DEVICE_ID = deviceId;
                            GlobalValues.IS_RSTR_PROJECTION = false;
                            GlobalValues.CURRENT_PROJECT_DEVICE_IP = request.getRemoteHost();
                            AppApi.resetPhoneInterface(GlobalValues.CURRENT_PROJECT_DEVICE_IP);

                        }else{
                            closeProjectionDialog();
                            response.setMsg("上传失败");
                            response.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                        }

                    }else{
                        ContentInfo content = listContent.get(0);
                        String mediaName = content.getFilename();
                        String url = BuildConfig.OSS_ENDPOINT+content.getForscreen_url();
                        String video_id = updateProjectionDialogNum(TYPE_VIDEO);
                        List<MediaLibBean> list = DBHelper.get(context).findActivityAdsByWhere(selection,selectionArgs);
                        if (list!=null&&list.size()>0){
                            String path = AppUtils.getFilePath(AppUtils.StorageFile.select_content) + mediaName;
                            File file = new File(path);
                            if (file.exists()){
                                handler.post(()->projectionImgListDialog.setImgDownloadProgress(video_id,100+"%"));
                                ProjectOperationListener.getInstance(context).showVideo(path, 0,true,GlobalValues.FROM_SERVICE_REMOTE);
                            }else{
                                handler.post(()->projectionImgListDialog.setImgDownloadProgress(video_id,100+"%"));
                                ProjectOperationListener.getInstance(context).showVideo(url, 0, true,GlobalValues.FROM_SERVICE_REMOTE);
                            }
                        }else{
                            handler.post(()->projectionImgListDialog.setImgDownloadProgress(video_id,100+"%"));
                            ProjectOperationListener.getInstance(context).showVideo(url, 0, true,GlobalValues.FROM_SERVICE_REMOTE);
                        }
                        closeProjectionDialog();
                        response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                response.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            }
            return new Gson().toJson(response);
        }

        /**
         * 单独查询热播视频内容
         * @param request
         * @return
         */
        private String findHotShow(HttpServletRequest request){
            BaseResponse response = new BaseResponse();
            String selection = DBHelper.MediaDBInfo.FieldName.TYPE + "=? ";
            String[] selectionArgs = new String[]{"1"};
            List<MediaItemBean> listItem = DBHelper.get(context).findMediaItemList(selection,selectionArgs);
            JsonArray jsonArray = new JsonArray();
            Map<Object,Object> idmap = new HashMap<>();
            if (listItem!=null&&listItem.size()>0){
                String baseSelectPath = AppUtils.getFilePath(AppUtils.StorageFile.select_content);
                for (MediaItemBean item:listItem){
                    String mediaName = item.getName();
                    String path = baseSelectPath+mediaName;
                    File file = new File(path);
                    if (file.exists()){
                        idmap.put(item.getId(),item.getId());
                    }
                }
                if (!idmap.isEmpty()&&idmap.size()>0){
                    for (Map.Entry<Object, Object> map : idmap.entrySet()) {
                        try {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("media_id",map.getValue().toString());
                            jsonArray.add(jsonObject);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                response.setResult(jsonArray);
                response.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                response.setMsg("查询成功");
            }else{
                response.setResult(jsonArray);
                response.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
                response.setMsg("热播数据为空");
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
            GlobalValues.PROJECT_STREAM_IMAGE_NUMS.clear();
            GlobalValues.PROJECTION_VIDEO_PATH = null;
            GlobalValues.CURRRNT_PROJECT_ID = forscreen_id;
            handler.removeCallbacks(new ProjectShowImageRunnable());
            isPPTRunnable = false;
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

        @Override
        public void onSuccess(AppApi.Action method, Object obj) {
            ProjectionManager.log("Notify stop success");
            LogUtils.d("Notify stop response: " + obj);
            switch (method){
                case CP_POST_SIMPLE_MINIPROGRAM_FORSCREEN_LOG_JSON:
                    if (obj instanceof String){
                        LogUtils.d("ProjectionLog:日志上传成功删除记录="+obj);
//                        String forscreenId = (String)obj;
//                        String selection = DBHelper.MediaDBInfo.FieldName.FORSCREEN_ID + "=? ";
//                        String[] selectionArgs = new String[]{forscreenId};
//                        DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.PROJECTION_LOG,selection,selectionArgs);
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

        public void startRemoteProjecion(int currentAction,String fid){
            switch (currentAction){
                case 2:
                case 5:
                    if (GlobalValues.INTERACTION_ADS_PLAY==1){
                        if (GlobalValues.PROJECT_STREAM_IMAGE.size()>0){
                            if (!isPPTRunnable){
                                LogUtils.d("1212:启动轮播图片,集合中的值为="+GlobalValues.PROJECT_STREAM_IMAGE);
                                handler.removeCallbacks(new ProjectShowImageRunnable());
                                currentIndex =0;

                                projectShowImage(words,fid,0);
                            }
                        }else if (GlobalValues.PROJECTION_VIDEO_PATH!=null){
                            ProjectOperationListener.getInstance(context).showVideo(GlobalValues.PROJECTION_VIDEO_PATH, 0, true,forscreenId, avatarUrl, nickName,GlobalValues.FROM_SERVICE_REMOTE);
                        }
                        GlobalValues.INTERACTION_ADS_PLAY=0;
                    }else if (GlobalValues.INTERACTION_ADS_PLAY==2){
                        if (!TextUtils.isEmpty(fid)&&!TextUtils.isEmpty(forscreenId)&&!forscreenId.equals(fid)){
                            if (GlobalValues.PROJECT_STREAM_IMAGE.size()>0){
                                if (!isPPTRunnable){
                                    LogUtils.d("1212:启动轮播图片,集合中的值为="+GlobalValues.PROJECT_STREAM_IMAGE);
                                    handler.removeCallbacks(new ProjectShowImageRunnable());
                                    currentIndex =0;

                                    projectShowImage(words,fid,0);
                                }
                            }else if (GlobalValues.PROJECTION_VIDEO_PATH!=null){
                                ProjectOperationListener.getInstance(context).showVideo(GlobalValues.PROJECTION_VIDEO_PATH, 0, true,forscreenId, avatarUrl, nickName,GlobalValues.FROM_SERVICE_REMOTE);
                            }
                        }
                        GlobalValues.INTERACTION_ADS_PLAY=0;
                    }else {
                        if (preOrNextAdsBean!=null&&preOrNextAdsBean.getPlay_position()==2){
                            String adspath = preOrNextAdsBean.getMediaPath();
                            String adsduration = preOrNextAdsBean.getDuration();
                            if (preOrNextAdsBean.getMedia_type()==1){
                                ProjectOperationListener.getInstance(context).showVideo(adspath, 0, true,forscreenId, avatarUrl, nickName,adsduration,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
                            }else{
                                ProjectOperationListener.getInstance(context).showImage(5, adspath, true,forscreenId, words, avatarUrl, nickName,adsduration,currentAction,GlobalValues.FROM_SERVICE_REMOTE);
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

}

package com.savor.ads.callback;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.jar.savor.box.interfaces.OnRemoteOperationListener;
import com.jar.savor.box.vo.CodeVerifyBean;
import com.jar.savor.box.vo.PlayResponseVo;
import com.jar.savor.box.vo.PrepareResponseVoNew;
import com.jar.savor.box.vo.ProgramResponseVo;
import com.jar.savor.box.vo.QueryPosBySessionIdResponseVo;
import com.jar.savor.box.vo.ResponseT;
import com.jar.savor.box.vo.RotateResponseVo;
import com.jar.savor.box.vo.SeekResponseVo;
import com.jar.savor.box.vo.StopResponseVo;
import com.jar.savor.box.vo.VolumeResponseVo;
import com.savor.ads.SavorApplication;
import com.savor.ads.activity.AdsPlayerActivity;
import com.savor.ads.activity.ScreenProjectionActivity;
import com.savor.ads.bean.BirthdayOndemandBean;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.database.DBHelper;
import com.savor.ads.projection.ProjectionManager;
import com.savor.ads.projection.action.ImageAction;
import com.savor.ads.projection.action.ImageRestaurantAction;
import com.savor.ads.projection.action.ImageWelcomeAction;
import com.savor.ads.projection.action.PlayAction;
import com.savor.ads.projection.action.ProgramAction;
import com.savor.ads.projection.action.RotateAction;
import com.savor.ads.projection.action.SeekAction;
import com.savor.ads.projection.action.StopAction;
import com.savor.ads.projection.action.VideoAction;
import com.savor.ads.projection.action.VideoBirthdayAction;
import com.savor.ads.projection.action.VideoRestAction;
import com.savor.ads.projection.action.VodAction;
import com.savor.ads.projection.action.VolumeAction;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.MiniProgramQrCodeWindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 点播、投屏类操作的接收回调
 * Created by zhanghq on 2016/12/14.
 */

public class ProjectOperationListener implements OnRemoteOperationListener {
    private final Context mContext;

    private static ProjectOperationListener instance;
    public static ProjectOperationListener getInstance(Context context) {
        if (instance == null) {
            instance = new ProjectOperationListener(context.getApplicationContext());
        }
        return instance;
    }

    private ProjectOperationListener(Context context) {
        mContext = context;
    }

    @Override
    public PrepareResponseVoNew showVod(String mediaName, String vodType, int position, boolean isFromWeb, boolean isNewDevice,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        String vid = "";
        String url = "";
        boolean vodCheckPass = true;
        DBHelper dbHelper = DBHelper.get(mContext);

        if ("2".equals(vodType)) {
            // 酒楼宣传片点播
            List<MediaLibBean> list = dbHelper.findPlayListByWhere(DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=?", new String[]{mediaName});

            if (list != null && !list.isEmpty()) {
                MediaLibBean bean = list.get(0);
                String filePath = AppUtils.getFilePath(AppUtils.StorageFile.media) + bean.getName();
                String md5 = bean.getMd5();
                File file = new File(filePath);
                if (file.exists()) {
                    String vodMd5 = AppUtils.getEasyMd5(file);
                    if (!vodMd5.equals(md5)) {
//                                    file.delete();
//                                    dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.PLAYLIST,
//                                            DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=?", new String[]{url});

                        localResult.setMsg("该点播视频无法播放，请稍后再试");
                        vodCheckPass = false;
                    }
                } else {
                    localResult.setMsg("没有找到点播视频！");
                    vodCheckPass = false;
                }

                url = filePath;
                vid = bean.getVid();
            } else {
                localResult.setMsg("没有找到点播视频！");
                vodCheckPass = false;
            }
        } else if ("3".equals(vodType)){
            //生日点播
            String filePath = AppUtils.getFilePath(AppUtils.StorageFile.birthday_ondemand) + mediaName;
            File file = new File(filePath);
            if (file.exists()){
                url = filePath;
            }
            String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
            String[] selectionArgs = new String[]{mediaName};
            List<BirthdayOndemandBean> list = DBHelper.get(mContext).findBirthdayOndemandByWhere(selection,selectionArgs);
            if (list!=null&&list.size()>0){
                vid = list.get(0).getMedia_id();
            }
        } else if ("4".equals(vodType)){
            String filePath = AppUtils.getFilePath(AppUtils.StorageFile.activity_ads) + mediaName;
            File file = new File(filePath);
            if (file.exists()){
                url = filePath;
            }
            String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
            String[] selectionArgs = new String[]{mediaName};
            List<MediaLibBean> list = DBHelper.get(mContext).findActivityAdsByWhere(selection,selectionArgs);
            if (list!=null&&list.size()>0){
                vid = list.get(0).getVid();
            }
        }else {
            // 普通点播视频点播
            String selection=DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
            String[] selectionArgs=new String[]{mediaName};
            List<MediaLibBean> list = dbHelper.findAdsByWhere(selection, selectionArgs);
            List<MediaLibBean> listPlayList = dbHelper.findPlayListByWhere(selection,selectionArgs);
            if ((list != null && !list.isEmpty())) {
                MediaLibBean bean = list.get(0);
                String filePath = AppUtils.getFilePath(AppUtils.StorageFile.media)+bean.getName();
                String md5 = bean.getMd5();
                File file = new File(filePath);
                if (file.exists()) {
                    String vodMd5=null;
                    if (file.exists()){
                        vodMd5 = AppUtils.getEasyMd5(file);
                        url = filePath;
                    }
                    if (TextUtils.isEmpty(vodMd5)||!vodMd5.equals(md5)) {

                        localResult.setMsg("该点播视频无法播放，请稍后再试");
                        vodCheckPass = false;
                    }
                } else {
                    localResult.setMsg("没有找到点播视频！");
                    vodCheckPass = false;
                }


                vid = bean.getVid();
            }else if (listPlayList!=null&&listPlayList.size()>0){
                MediaLibBean bean = listPlayList.get(0);
                String filePath = AppUtils.getFilePath(AppUtils.StorageFile.media)+bean.getName();
                String md5 = bean.getMd5();
                File file = new File(filePath);
                if (file.exists()) {
                    String vodMd5 = null;
                    if (file.exists()){
                        vodMd5 = AppUtils.getEasyMd5(file);
                        url = filePath;
                    }else{
                        vodMd5 = AppUtils.getEasyMd5(file);
                        url = filePath;
                    }

                    if (TextUtils.isEmpty(vodMd5)||!vodMd5.equals(md5)) {
                        file.delete();
                        dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.PLAYLIST,selection, selectionArgs);

                        localResult.setMsg("该点播视频无法播放，请稍后再试");
                        vodCheckPass = false;
                    }
                } else {
                    localResult.setMsg("没有找到点播视频！");
                    vodCheckPass = false;
                }
                vid = bean.getVid();
            }else {
                localResult.setMsg("没有找到点播视频！");
                vodCheckPass = false;
            }
        }

//        dbHelper.close();
        if (vodCheckPass) {
            if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
                GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
            }

            localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
            localResult.setMsg("加载成功！");

            VodAction vodAction = new VodAction(mContext, vid, url, isFromWeb, isNewDevice,fromService);
            ProjectionManager.getInstance().enqueueAction(vodAction);
        } else {
            localResult.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
        }

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showImage(int imageType, int rotation, boolean isThumbnail, String seriesId, boolean isNewDevice,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (isThumbnail) {
            if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
                GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
            }

            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        } else {
            // 大图的时候不生成新的ProjectId
            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID);
        }
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        ImageAction imageAction = new ImageAction(mContext, imageType, rotation, isThumbnail,seriesId, isNewDevice,fromService);
        ProjectionManager.getInstance().enqueueAction(imageAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showImage(int imageType, String imagePath,boolean isThumbnail,String forscreenId,String words,String avatarUrl,String nickname,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!isThumbnail) {
            if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
                GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
            }

            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        } else {
            // 大图的时候不生成新的ProjectId
            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID);
        }
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        ImageAction imageAction = new ImageAction(mContext, imageType, imagePath,isThumbnail,forscreenId,words,avatarUrl,nickname,fromService);
        ProjectionManager.getInstance().enqueueAction(imageAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showImage(int imageType, String imageUrl, boolean isThumbnail, String forscreenId, String words, String avatarUrl, String nickname, String delayTime,int action,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!isThumbnail) {
            if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
                GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
            }

            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        } else {
            // 大图的时候不生成新的ProjectId
            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID);
        }
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        ImageAction imageAction = new ImageAction(mContext, imageType, imageUrl,isThumbnail,forscreenId,words,avatarUrl,nickname,delayTime,action,fromService);
        ProjectionManager.getInstance().enqueueAction(imageAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showImage(int imageType, String imageUrl, boolean isThumbnail, String price,int storeSale,String delayTime,int action,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!isThumbnail) {
            if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
                GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
            }

            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        } else {
            // 大图的时候不生成新的ProjectId
            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID);
        }
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        ImageAction imageAction = new ImageAction(mContext, imageType, imageUrl,isThumbnail,price,storeSale,delayTime,action,fromService);
        ProjectionManager.getInstance().enqueueAction(imageAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showRestImage(int imageType, String imagePath,int rotation, boolean isThumbnail, String words, String avatarUrl, String nickname, int projectionTime,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (isThumbnail) {
            if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
                GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
            }

            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        } else {
            // 大图的时候不生成新的ProjectId
            localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID);
        }
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        ImageRestaurantAction restaurantAction = new ImageRestaurantAction(mContext, imageType, imagePath,rotation,isThumbnail,words,avatarUrl,nickname,projectionTime,fromService);
        ProjectionManager.getInstance().enqueueAction(restaurantAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showRestImage(int imageType, String imagePath,int rotation,String musicPath, String words,String wordsSize,String wordsColor, String fontPath,int projectionTime, int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
            GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        }

        localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        ImageWelcomeAction welcomeAction = new ImageWelcomeAction(mContext,imageType,imagePath,rotation,musicPath,words,wordsSize,wordsColor,fontPath,projectionTime,fromService);
        ProjectionManager.getInstance().enqueueAction(welcomeAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showRestImage(int imageType, String imagePath,int rotation,String musicPath, String words,String wordsSize,String wordsColor, String fontPath,String waiterIconUrl,String waiterName,int projectionTime, int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
            GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        }

        localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        ImageWelcomeAction welcomeAction = new ImageWelcomeAction(mContext,imageType,imagePath,rotation,musicPath,words,wordsSize,wordsColor,fontPath,waiterIconUrl,waiterName,projectionTime,fromService);
        ProjectionManager.getInstance().enqueueAction(welcomeAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showRestVideo(String videoPath, boolean isNewDevice,String avatarUrl,String nickname,int projectionTime) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
            GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        }

        localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        VideoRestAction videoAction = new VideoRestAction(mContext, videoPath, isNewDevice,avatarUrl,nickname,projectionTime);
        ProjectionManager.getInstance().enqueueAction(videoAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showRestVideo(String videoPath,String videoUrl, boolean isNewDevice,String avatarUrl,String nickname,int projectionTime) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
            GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        }

        localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        VideoRestAction videoAction = new VideoRestAction(mContext, videoPath,videoUrl, isNewDevice,avatarUrl,nickname,projectionTime);
        ProjectionManager.getInstance().enqueueAction(videoAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showVideo(String videoPath, boolean isNewDevice,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
            GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        }

        localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        VideoAction videoAction = new VideoAction(mContext, videoPath, isNewDevice,fromService);
        ProjectionManager.getInstance().enqueueAction(videoAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showVideo(String videoPath, boolean isNewDevice,String price,int storeSale,String delayTime,int action,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
            GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        }

        localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        VideoAction videoAction = new VideoAction(mContext, videoPath,isNewDevice,price,storeSale,delayTime,action,fromService);
        ProjectionManager.getInstance().enqueueAction(videoAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showVideo(String videoPath, String videoUrl,boolean isNewDevice,String forscreenId,String avatarUrl,String nickname,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
            GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        }

        localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        VideoAction videoAction = new VideoAction(mContext, videoPath,videoUrl,isNewDevice,forscreenId,avatarUrl,nickname,fromService);
        ProjectionManager.getInstance().enqueueAction(videoAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showVideo(String videoPath, boolean isNewDevice,String forscreenId,String avatarUrl,String nickname,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
            GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        }

        localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        VideoAction videoAction = new VideoAction(mContext, videoPath, isNewDevice,forscreenId,avatarUrl,nickname,fromService);
        ProjectionManager.getInstance().enqueueAction(videoAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showVideo(String videoPath, boolean isNewDevice, String forscreenId, String avatarUrl, String nickname, String delayTime,int action,int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
            GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        }

        localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = UUID.randomUUID().toString());
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        VideoAction videoAction = new VideoAction(mContext, videoPath,isNewDevice,forscreenId,avatarUrl,nickname,delayTime,action,fromService);
        ProjectionManager.getInstance().enqueueAction(videoAction);

        return localResult;
    }

    @Override
    public PrepareResponseVoNew showVideoBirthday(String videoUrl,String videoPath,String forscreenId, int action, int fromService) {
        PrepareResponseVoNew localResult = new PrepareResponseVoNew();
        if (!TextUtils.isEmpty(GlobalValues.CURRENT_PROJECT_ID)) {
            GlobalValues.LAST_PROJECT_ID = GlobalValues.CURRENT_PROJECT_ID;
        }

        localResult.setProjectId(GlobalValues.CURRENT_PROJECT_ID = forscreenId);
        localResult.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
        localResult.setMsg("加载成功！");

        VideoBirthdayAction videoAction = new VideoBirthdayAction(mContext,videoUrl, videoPath,forscreenId,action,fromService);
        ProjectionManager.getInstance().enqueueAction(videoAction);

        return localResult;
    }

    @Override
    public SeekResponseVo seek(int position, String projectId) {
            if (!TextUtils.isEmpty(projectId) && projectId.equals(GlobalValues.CURRENT_PROJECT_ID)) {
                SeekAction seekAction = new SeekAction(position);
                ProjectionManager.getInstance().enqueueAction(seekAction);

                SeekResponseVo responseVo = new SeekResponseVo();
                responseVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                responseVo.setMsg("成功");
                return responseVo;
            } else {
                SeekResponseVo responseVo = new SeekResponseVo();
                responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_PROJECT_ID_CHECK_FAILED);
                responseVo.setMsg("操作失败");
                return responseVo;
            }
    }

    @Override
    public PlayResponseVo play(int action, String projectId) {
        if (!TextUtils.isEmpty(projectId) && projectId.equals(GlobalValues.CURRENT_PROJECT_ID)) {

            PlayAction playAction = new PlayAction(action, projectId);
            ProjectionManager.getInstance().enqueueAction(playAction);

            PlayResponseVo responseVo = new PlayResponseVo();
            responseVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);

            return responseVo;
        } else {
            PlayResponseVo responseVo = new PlayResponseVo();
            responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_PROJECT_ID_CHECK_FAILED);
            responseVo.setMsg("操作失败");
            return responseVo;
        }
    }

    @Override
    public StopResponseVo stop(String projectId) {
        StopAction stopAction = new StopAction(projectId, false);
        ProjectionManager.getInstance().enqueueAction(stopAction);

        StopResponseVo stopResponseVo = new StopResponseVo();
        stopResponseVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);

        return stopResponseVo;
    }

    @Override
    public void rstrStop() {
        StopAction stopAction = new StopAction(null,true);
        ProjectionManager.getInstance().enqueueAction(stopAction);
    }

    /**
     * 旋转图片
     *
     * @param rotateDegree
     * @return
     */
    @Override
    public RotateResponseVo rotate(int rotateDegree, String projectId) {
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof ScreenProjectionActivity) {
            if (!TextUtils.isEmpty(projectId) && projectId.equals(GlobalValues.CURRENT_PROJECT_ID)) {
                RotateAction rotateAction = new RotateAction(rotateDegree, projectId);
                ProjectionManager.getInstance().enqueueAction(rotateAction);

                RotateResponseVo responseVo = new RotateResponseVo();
                responseVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                responseVo.setMsg("成功");
                responseVo.setRotateValue(rotateDegree);
                return responseVo;
            } else {
                RotateResponseVo responseVo = new RotateResponseVo();
                responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_PROJECT_ID_CHECK_FAILED);
                responseVo.setMsg("操作失败");
                return responseVo;
            }
        } else {
            RotateResponseVo responseVo = new RotateResponseVo();
            responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            responseVo.setMsg("操作失败");
            return responseVo;
        }
    }

    @Override
    public VolumeResponseVo volume(int action, String projectId) {
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (!TextUtils.isEmpty(projectId)) {
            VolumeResponseVo responseVo = new VolumeResponseVo();
            if (activity instanceof ScreenProjectionActivity){
                VolumeAction volumeAction = new VolumeAction(action, projectId);
                ProjectionManager.getInstance().enqueueAction(volumeAction);
                responseVo.setMsg("操作成功");
                responseVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
            }else{
                responseVo.setMsg("操作失败");
                responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            }

            return responseVo;
        } else {
            VolumeResponseVo responseVo = new VolumeResponseVo();
            responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_PROJECT_ID_CHECK_FAILED);
            responseVo.setMsg("操作失败");
            return responseVo;
        }
    }

    @Override
    public ProgramResponseVo switchProgram(int action, String projectId) {
        if (!TextUtils.isEmpty(projectId)) {
            Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
            ProgramResponseVo responseVo = new ProgramResponseVo();
            if (activity instanceof ScreenProjectionActivity){
                responseVo.setMsg("当前正在投屏中，无法切换节目");
                responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
            }else {
                ProgramAction programAction = new ProgramAction(action, projectId);
                ProjectionManager.getInstance().enqueueAction(programAction);
                responseVo.setCode(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
            }

            return responseVo;
        } else {
            ProgramResponseVo responseVo = new ProgramResponseVo();
            responseVo.setCode(ConstantValues.SERVER_RESPONSE_CODE_PROJECT_ID_CHECK_FAILED);
            responseVo.setMsg("操作失败");
            return responseVo;
        }
    }

    @Override
    public void showMiniProgramCode() {
        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof AdsPlayerActivity||activity instanceof ScreenProjectionActivity) {
            DBHelper dbHelper= DBHelper.get(mContext);
            String selection = DBHelper.MediaDBInfo.FieldName.VID + "=? ";
            String[] selectionArgs = new String[]{ConstantValues.QRCODE_CALL_VIDEO_ID};
            List<MediaLibBean> listPlayList = dbHelper.findNewPlayListByWhere(selection, selectionArgs);
            if (listPlayList != null && listPlayList.size() > 0) {
                MediaLibBean bean = listPlayList.get(0);
                String path = AppUtils.getFilePath(AppUtils.StorageFile.media) + bean.getName();
                File file = new File(path);
                if (file.exists()) {
                    VodAction vodAction = new VodAction(mContext, ConstantValues.QRCODE_CALL_VIDEO_ID, path,false, true,GlobalValues.FROM_SERVICE_REMOTE);
                    ProjectionManager.getInstance().enqueueAction(vodAction);
                }
            }
            try {
                Thread.sleep(800);
            }catch (Exception e){
                e.printStackTrace();
            }
            MiniProgramQrCodeWindowManager.get(mContext).setCurrentPlayMediaId(ConstantValues.QRCODE_CALL_VIDEO_ID);
            if (Session.get(mContext).getQrcodeType()==2){
                ((SavorApplication) mContext).showMiniProgramQrCodeWindow(ConstantValues.MINI_PROGRAM_SQRCODE_CALL_TYPE);
            }
        }
    }

    /**
     * 点播者查询播放进度等信息
     *
     * @return
     */
    @Override
    public Object query(String projectId) {
        if (TextUtils.isEmpty(projectId)) {
            QueryPosBySessionIdResponseVo responseVo = new QueryPosBySessionIdResponseVo();
            responseVo.setResult(ConstantValues.SERVER_RESPONSE_CODE_PROJECT_ID_CHECK_FAILED);
            return responseVo;
        }

        Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
        if (activity instanceof ScreenProjectionActivity) {
            if (projectId.equals(GlobalValues.CURRENT_PROJECT_ID)) {
                return ((ScreenProjectionActivity) activity).query();
            } else {
                QueryPosBySessionIdResponseVo responseVo = new QueryPosBySessionIdResponseVo();
                responseVo.setResult(ConstantValues.SERVER_RESPONSE_CODE_PROJECT_ID_CHECK_FAILED);
                return responseVo;
            }
        } else {
            if (projectId.equals(GlobalValues.CURRENT_PROJECT_ID)) {
                // 播放正在准备，还没来得及跳到投屏页
                QueryPosBySessionIdResponseVo queryResponse = new QueryPosBySessionIdResponseVo();
                queryResponse.setResult(AppApi.HTTP_RESPONSE_STATE_SUCCESS);
                queryResponse.setPos(0);
                return queryResponse;
            } else if (projectId.equals(GlobalValues.LAST_PROJECT_ID)) {
                // 播放已结束
                QueryPosBySessionIdResponseVo queryResponse = new QueryPosBySessionIdResponseVo();
                queryResponse.setResult(ConstantValues.SERVER_RESPONSE_CODE_VIDEO_COMPLETE);
                return queryResponse;
            }
        }

        QueryPosBySessionIdResponseVo queryResponse = new QueryPosBySessionIdResponseVo();
        queryResponse.setResult(ConstantValues.SERVER_RESPONSE_CODE_FAILED);
        return queryResponse;
    }

    @Override
    public void showCode() {
        if (mContext instanceof SavorApplication) {
            ((SavorApplication) mContext).showQrCodeWindow(null);
        }
    }

    @Override
    public ResponseT<CodeVerifyBean> verify(String code) {
        ResponseT responseT = new ResponseT();
        Session session = Session.get(mContext);
        if (!TextUtils.isEmpty(code) && code.equals(session.getAuthCode())) {
            responseT.setCode(10000);
            CodeVerifyBean bean = new CodeVerifyBean();
            bean.setBox_id(session.getBoxId());
            bean.setBox_ip(AppUtils.getLocalIPAddress());
            bean.setBox_mac(session.getEthernetMac());
            bean.setHotel_id(session.getBoiteId());
            bean.setRoom_id(session.getRoomId());
            bean.setSsid(AppUtils.getShowingSSID(mContext));
            responseT.setResult(bean);
        } else {
            responseT.setCode(10001);
            responseT.setMsg("输入有误，请重新输入");
        }
        return responseT;
    }
}

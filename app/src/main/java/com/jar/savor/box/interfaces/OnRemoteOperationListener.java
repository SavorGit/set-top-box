package com.jar.savor.box.interfaces;

import com.jar.savor.box.vo.CodeVerifyBean;
import com.jar.savor.box.vo.HitEggResponseVo;
import com.jar.savor.box.vo.PlayResponseVo;
import com.jar.savor.box.vo.PptRequestVo;
import com.jar.savor.box.vo.PptVideoRequestVo;
import com.jar.savor.box.vo.PrepareResponseVoNew;
import com.jar.savor.box.vo.ProgramResponseVo;
import com.jar.savor.box.vo.ResponseT;
import com.jar.savor.box.vo.RotateResponseVo;
import com.jar.savor.box.vo.SeekResponseVo;
import com.jar.savor.box.vo.StopResponseVo;
import com.jar.savor.box.vo.VolumeResponseVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghq on 2016/12/22.
 */

public interface OnRemoteOperationListener {

    PrepareResponseVoNew showVod(String mediaName, String vodType, int position, boolean isFromWeb, boolean isNewDevice,int currentAction,int fromService);
    /**投屏引导图*/
    PrepareResponseVoNew showImage(int imageType, String imageUrl,boolean isThumbnail,String forscreenId,String delayTime,String avatarUrl,String nickname,int action,int fromService);
    PrepareResponseVoNew showImage(int imageType, String imageUrl,boolean isThumbnail,String forscreenId,String words,String avatarUrl,String nickname,int fromService);
    PrepareResponseVoNew showImage(int imageType, String imageUrl,boolean isThumbnail,String forscreenId,String words,String avatarUrl,String nickname,String delayTime,String musicPath,int action,int fromService);
    PrepareResponseVoNew showImage(int imageType, String imageUrl,boolean isThumbnail,String price,int storeSale,String delayTime,int action,int fromService);
    /**商务宴请-欢迎词*/
    PrepareResponseVoNew showBusinessImage(int imageType,boolean isThumbnail, String imageUrl,String words,String wordsSize,String wordsColor,String fontPath,String musicPath,int projectionTime,int fromService);

    PrepareResponseVoNew showVideo(String videoPath,String videoUrl, boolean isNewDevice,int action,int fromService);
    PrepareResponseVoNew showVideo(String videoPath, boolean isNewDevice,String price,int storeSale,String delayTime,int action,int fromService);
    PrepareResponseVoNew showVideo(String videoPath, boolean isNewDevice,String forscreenId,String avatarUrl,String nickname,int fromService);
    PrepareResponseVoNew showVideo(String videoPath, String videoUrl, boolean isNewDevice,String forscreenId,String avatarUrl,String nickname,int action,int fromService);
    PrepareResponseVoNew showVideo(String videoPath, boolean isNewDevice,String forscreenId,String avatarUrl,String nickname,String delayTime,int action,int fromService);
    PrepareResponseVoNew showVideo(String videoPath, boolean isNewDevice,String forscreenId,boolean ads,String delayTime,int action,int fromService);

    SeekResponseVo seek(int position, String projectId);

    /**
     * 点播生日歌相关
     * @param videoPath
     * @param forscreenId
     * @param action
     * @param fromService
     * @return
     */
    PrepareResponseVoNew showVideoBirthday(String videoUrl,String videoPath,String forscreenId,int action,int fromService);

    /**
     * 控制播放、暂停
     * @param action 0：暂停；
     *               1：播放
     * @return
     */
    PlayResponseVo play(int action, String projectId);

    StopResponseVo stop(String projectId);


    PrepareResponseVoNew showRestImage(int imageType, String imageUrl,int rotation,boolean isThumbnail,String words,String avatarUrl,String nickname,int projectionTime,int fromService);
    /**欢迎词专用*/
    PrepareResponseVoNew showRestImage(int imageType, String imageUrl,int rotation,String musicPath,String words,String wordsSize,String wordsColor,String fontPath,int projectionTime,int fromService);
    /**欢迎词专用-携带服务人员信息*/
    PrepareResponseVoNew showRestImage(int imageType, String imageUrl,int rotation,String musicPath,String words,String wordsSize,String wordsColor,String fontPath,String waiterName,String waiterIconUrl,int projectionTime,int fromService);

    PrepareResponseVoNew showRestVideo(String videoPath,boolean isNewDevice,String avatarUrl,String nickname,int projectionTime);
    PrepareResponseVoNew showRestVideo(String videoPath,String videoUrl,boolean isNewDevice,String avatarUrl,String nickname,int projectionTime);
    void rstrStop();

    RotateResponseVo rotate(int rotateDegree, String projectId);

    /**
     * 控制音量
     * @param action 音量操作类型
     * 1：静音
     * 2：取消静音
     * 3：音量减
     * 4：音量加
     * @return
     */
    VolumeResponseVo volume(int action, String projectId);

    /**
     * 调整节目上一个下一个
     * @param action 1:上一个节目,2:下一个节目
     * @param projectId
     * @return
     */
    ProgramResponseVo switchProgram(int action,String projectId);

    /**
     * 呼出小程序码大码
     */
    void showMiniProgramCode(String fileName,int action,int from_service);

    Object query(String projectId);


    ResponseT<CodeVerifyBean> verify(String code);

}

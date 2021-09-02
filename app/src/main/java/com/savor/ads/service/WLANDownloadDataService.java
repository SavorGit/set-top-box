package com.savor.ads.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.savor.ads.bean.BirthdayOndemandResult;
import com.savor.ads.bean.JsonBean;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.SetBoxTopResult;
import com.savor.ads.bean.SetTopBoxBean;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.database.DBHelper;
import com.savor.ads.okhttp.coreProgress.download.FileDownloader;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlobalValues;
import com.savor.ads.utils.LogUtils;

import org.json.JSONObject;

import java.util.List;

public class WLANDownloadDataService extends Service {

    private Context context;
    private Session session;
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    private DBHelper dbHelper;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        session = Session.get(context);
        dbHelper = DBHelper.get(context);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //局域网传输节目单数据
        handleProgramGuidesData();
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleProgramGuidesData(){
        try {
            JsonBean jsonBean = AppApi.getProgramGuidesData(context,requestListener,"00226D583D92");
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                return;
            }
            SetTopBoxBean setTopBoxBean = gson.fromJson(jsonBean.getConfigJson(), new TypeToken<SetTopBoxBean>() {
            }.getType());
            if (setTopBoxBean.getPeriod().equals(session.getProPeriod())) {
                return;
            }
            List<MediaLibBean> listLib = setTopBoxBean.getPlay_list();
            if (listLib!=null&&listLib.size()>0){
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWPLAYLIST,null,null);
                int downloadedCount = 0;
                String currentProPeriod=setTopBoxBean.getPeriod();
                String currentAdvPeriod = null;
                for (MediaLibBean mediaItem:listLib){
                    String fileName = mediaItem.getName();
                    boolean checked = false;
                    if (!TextUtils.isEmpty(fileName)){
                        if (mediaItem.getType().equals(ConstantValues.ADV)){
                            currentAdvPeriod = mediaItem.getPeriod();
                        }
                        String basePath = AppUtils.getFilePath(AppUtils.StorageFile.media);
                        String path = basePath + fileName;
                        String md5 = mediaItem.getMd5();
                        String url = AppApi.WLAN_BASE_URL+"media/"+fileName;
                        if (AppUtils.isDownloadEasyCompleted(path, md5)||AppUtils.isDownloadCompleted(path, md5)){
                            checked = true;
                        }else{
                            boolean isDownloaded = new FileDownloader(context,url,basePath,fileName,true).downloadByRange();
                            if (mediaItem.getMedia_type()==1){
                                isDownloaded =  AppUtils.isDownloadEasyCompleted(path, mediaItem.getMd5());
                            }else if (mediaItem.getMedia_type()==2){
                                isDownloaded = AppUtils.isDownloadCompleted(path, mediaItem.getMd5());
                            }
                            if (isDownloaded){
                                checked = true;
                            }
                        }
                    }else{
                        //没有文件的数据相当于是占位符，直接检测通过
                        checked = true;
                    }
                    if (checked){
                        if (dbHelper.insertOrUpdateNewPlayListLib(mediaItem, -1)){
                            downloadedCount++;
                        }
                    }
                }
                if (downloadedCount==listLib.size()){
                    dbHelper.copyTableMethod(DBHelper.MediaDBInfo.TableName.NEWPLAYLIST,DBHelper.MediaDBInfo.TableName.PLAYLIST);
                    session.setProPeriod(currentProPeriod);
                    session.setAdvPeriod(currentAdvPeriod);
                    notifyToPlay();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void notifyToPlay() {
        if (AppUtils.fillPlaylist(this, null, 1)) {
            LogUtils.d("发送通知更新播放列表广播");
            context.sendBroadcast(new Intent(ConstantValues.UPDATE_PLAYLIST_ACTION));
        }
    }

    ApiRequestListener requestListener = new ApiRequestListener() {
        @Override
        public void onSuccess(AppApi.Action method, Object obj) {

        }

        @Override
        public void onError(AppApi.Action method, Object obj) {

        }

        @Override
        public void onNetworkFailed(AppApi.Action method) {

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

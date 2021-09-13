package com.savor.ads.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.savor.ads.BuildConfig;
import com.savor.ads.bean.BirthdayOndemandResult;
import com.savor.ads.bean.JsonBean;
import com.savor.ads.bean.MediaItemBean;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.ProgramBean;
import com.savor.ads.bean.SelectContentBean;
import com.savor.ads.bean.SelectContentResult;
import com.savor.ads.bean.SetBoxTopResult;
import com.savor.ads.bean.SetTopBoxBean;
import com.savor.ads.bean.ShopGoodsBean;
import com.savor.ads.bean.ShopGoodsResult;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WLANDownloadDataService extends Service {

    private Context context;
    private Session session;
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    private DBHelper dbHelper;
    private Handler handler=new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        session = Session.get(context);
        dbHelper = DBHelper.get(context);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(()->{
            handleWLANDownloadParam();
            //局域网传输节目单数据
            handleProgramGuidesData();
            //局域网传输节目单-广告数据
            handleAdsListData();
            //局域网传输商城商品数据
            handleShopGoodsListData();
            //局域网传输热播内容数据
            handleHotPlayProData();
            //局域网下载情况上报
            reportDownloadState();
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleWLANDownloadParam(){
        GlobalValues.completionRate = 0;
        //下载1小时如果未下载完成则停止下载
        handler.postDelayed(completionRunnable,1000*60*60);
    }

    private Runnable completionRunnable = ()->GlobalValues.completionRate =-1;

    private void handleProgramGuidesData(){
        try {
            JsonBean jsonBean = AppApi.getProgramGuidesData(context,requestListener,session.getLan_mac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                return;
            }
            SetTopBoxBean setTopBoxBean = gson.fromJson(jsonObject.getString("result"), new TypeToken<SetTopBoxBean>() {
            }.getType());
            if (setTopBoxBean.getPeriod().equals(session.getProPeriod())) {
                GlobalValues.completionRate +=1;
                LogUtils.d("WLAN客户端----节目和宣传片最新==="+GlobalValues.completionRate);
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
                    GlobalValues.completionRate +=1;
                    LogUtils.d("WLAN客户端----节目和宣传片下载完成==="+GlobalValues.completionRate);
                    AppUtils.deleteOldMedia(context,true);
                    notifyToPlay();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void handleAdsListData(){
        try {
            JsonBean jsonBean = AppApi.getAdsListData(context,requestListener,session.getLan_mac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                GlobalValues.completionRate +=1;
                LogUtils.d("WLAN客户端----广告没有数据==="+GlobalValues.completionRate);
                AppUtils.deleteOldMedia(context,true);
                return;
            }
            ProgramBean programBean = gson.fromJson(jsonObject.getString("result"), new TypeToken<ProgramBean>() {
            }.getType());
            if (programBean.getVersion().getVersion().equals(session.getAdsPeriod())) {
                GlobalValues.completionRate +=1;
                LogUtils.d("WLAN客户端----广告最新==="+GlobalValues.completionRate);
                return;
            }
            List<MediaLibBean> listLib = programBean.getMedia_lib();
            if (listLib!=null&&listLib.size()>0){
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWADSLIST,null,null);
                int downloadedCount = 0;
                String currentAdsPeriod=programBean.getVersion().getVersion();
                for (MediaLibBean mediaItem:listLib){
                    String fileName = mediaItem.getName();
                    boolean checked = false;
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
                    if (checked){
                        if (dbHelper.insertOrUpdateNewAdsList(mediaItem, -1)){
                            downloadedCount++;
                        }
                    }
                }
                if (downloadedCount==listLib.size()){
                    dbHelper.copyTableMethod(DBHelper.MediaDBInfo.TableName.NEWADSLIST,DBHelper.MediaDBInfo.TableName.ADSLIST);
                    session.setProPeriod(currentAdsPeriod);
                    GlobalValues.completionRate +=1;
                    LogUtils.d("WLAN客户端----广告下载完成==="+GlobalValues.completionRate);
                    AppUtils.deleteOldMedia(context,true);
                    notifyToPlay();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void handleShopGoodsListData(){
        try {
            JsonBean jsonBean = AppApi.getShopGoodsListData(context,requestListener,session.getLan_mac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                return;
            }
            ShopGoodsResult result = gson.fromJson(jsonObject.getString("result"), new TypeToken<ShopGoodsResult>() {
            }.getType());
            if (result.getPeriod().equals(session.getShopGoodsAdsPeriod())) {
                GlobalValues.completionRate +=1;
                LogUtils.d("WLAN客户端----商城商品最新==="+GlobalValues.completionRate);
                return;
            }
            List<ShopGoodsBean> shopGoodsBeanList = result.getDatalist();
            if (shopGoodsBeanList==null||shopGoodsBeanList.size()==0){
                AppUtils.deleteShopGoodsAdsMedia(context);
                GlobalValues.completionRate +=1;
                LogUtils.d("WLAN客户端----商城商品为空==="+GlobalValues.completionRate);
                notifyToPlay();
                return;
            }
            if (shopGoodsBeanList!=null&&shopGoodsBeanList.size()>0){
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SHOP_GOODS_ADS,null,null);
                int downloadedCount = 0;
                List<String> fileNames = new ArrayList<>();
                for (ShopGoodsBean mediaItem:shopGoodsBeanList){
                    String fileName = mediaItem.getName();
                    boolean checked = false;
                    String basePath = AppUtils.getFilePath(AppUtils.StorageFile.goods_ads);
                    String path = basePath + fileName;
                    String md5 = mediaItem.getMd5();
                    String url = AppApi.WLAN_BASE_URL+"goods_ads/"+fileName;
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
                    if (checked){
                        if (dbHelper.insertShopGoodsAds(mediaItem)){
                            fileNames.add(fileName);
                            downloadedCount++;
                        }
                    }
                }
                if (downloadedCount==shopGoodsBeanList.size()){
                    session.setShopGoodsAdsPeriod(result.getPeriod());
                    GlobalValues.completionRate +=1;
                    LogUtils.d("WLAN客户端----商城商品下载完成==="+GlobalValues.completionRate);
                    String goodsAds = AppUtils.getFilePath(AppUtils.StorageFile.goods_ads);
                    File[] goodsFiles = new File(goodsAds).listFiles();
                    for (File file : goodsFiles) {
                        String fileName = file.getName();
                        if (!fileNames.contains(fileName)) {
                            file.delete();
                            LogUtils.d("删除文件===================" + file.getName());
                        }
                    }
                    notifyToPlay();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleHotPlayProData(){
        try {
            JsonBean jsonBean = AppApi.getHotPlayProData(context,requestListener,session.getLan_mac());
            JSONObject jsonObject = new JSONObject(jsonBean.getConfigJson());
            //记录接口总共返回多少条数据
            List<String> fileSize = new ArrayList<>();
            //记录成功下载多少条数据
            List<String> fileNames = new ArrayList<>();
            String selection = "";
            String[] selectionArgs = new String[]{};
            if (jsonObject.getInt("code")!=AppApi.HTTP_RESPONSE_STATE_SUCCESS){
                AppUtils.deleteHotContentMedia(fileNames);
                GlobalValues.completionRate +=1;
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SELECT_CONTENT,selection,selectionArgs);
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.MEDIA_ITEM,selection,selectionArgs);
                LogUtils.d("WLAN客户端----热播内容为空==="+GlobalValues.completionRate);
                return;
            }
            SelectContentResult result = gson.fromJson(jsonObject.getString("result"), new TypeToken<SelectContentResult>() {
            }.getType());
            if (result.getPeriod().equals(session.getHotContentPeriod())) {
                GlobalValues.completionRate +=1;
                LogUtils.d("WLAN客户端----热播内容最新==="+GlobalValues.completionRate);
                return;
            }
            List<SelectContentBean> list = result.getDatalist();
            if (list==null ||list.size()==0){
                AppUtils.deleteHotContentMedia(fileNames);
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SELECT_CONTENT,selection,selectionArgs);
                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.MEDIA_ITEM,selection,selectionArgs);
                GlobalValues.completionRate +=1;
                return;
            }
            String basePath = AppUtils.getFilePath(AppUtils.StorageFile.hot_content);
            for (SelectContentBean bean:list){
                if (bean.getSubdata()!=null&&bean.getSubdata().size()>0){
                    List<String> subNames = new ArrayList<>();
                    for (MediaItemBean item:bean.getSubdata()){
                        boolean isDownloaded = false;
                        String fileName = item.getName();
                        String path = basePath + item.getName();
                        fileSize.add(fileName);
                        if (bean.getMedia_type()==1){
                            isDownloaded = AppUtils.isDownloadEasyCompleted(path, item.getMd5());
                        }else if (bean.getMedia_type()==2||bean.getMedia_type()==21){
                            isDownloaded = AppUtils.isDownloadCompleted(path, item.getMd5().toUpperCase());
                        }
                        if (!isDownloaded&&!AppUtils.isInProjection()){
                            String url = BuildConfig.OSS_ENDPOINT+item.getOss_path();
                            isDownloaded = new FileDownloader(context,url,basePath,fileName,true).downloadByRange();
                            if (isDownloaded
                                    && (AppUtils.isDownloadEasyCompleted(path, item.getMd5())
                                    ||AppUtils.isDownloadCompleted(path, item.getMd5().toUpperCase()))) {
                                isDownloaded = true;
                            }else{
                                isDownloaded = false;
                            }
                        }
                        if (isDownloaded){
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
                            fileNames.addAll(subNames);
                        }
                    }
                }
            }
            if (fileSize.size()==fileNames.size()){
                //热播精选内容下载完成
                session.setHotContentPeriod(result.getPeriod());
                GlobalValues.completionRate +=1;
                LogUtils.d("WLAN客户端----热播内容下载完成==="+GlobalValues.completionRate);
                AppUtils.deleteHotContentMedia(fileNames);
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

    private void reportDownloadState(){
        if (GlobalValues.completionRate==4){
            LogUtils.d("WLAN客户端----下载成功,开始上传=="+GlobalValues.completionRate);
            AppApi.reportBoxDownloadState(context,requestListener,session.getEthernetMac(),1);
        }else if (GlobalValues.completionRate==-1){
            LogUtils.d("WLAN客户端----下载超时,开始上传=="+GlobalValues.completionRate);
            AppApi.reportBoxDownloadState(context,requestListener,session.getEthernetMac(),2);
        }else if (GlobalValues.completionRate>=0&&GlobalValues.completionRate<4){
            LogUtils.d("WLAN客户端----下载失败,开始上传=="+GlobalValues.completionRate);
            AppApi.reportBoxDownloadState(context,requestListener,session.getEthernetMac(),3);
        }
        handler.removeCallbacks(completionRunnable);
        GlobalValues.completionRate = 0;
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

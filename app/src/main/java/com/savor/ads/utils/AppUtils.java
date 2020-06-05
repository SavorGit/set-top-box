package com.savor.ads.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.protobuf.ByteString;
import com.savor.ads.BuildConfig;
import com.savor.ads.activity.MainActivity;
import com.savor.ads.activity.ScreenProjectionActivity;
import com.savor.ads.bean.BaiduAdLocalBean;
import com.savor.ads.bean.JDmomediaLocalBean;
import com.savor.ads.bean.MediaItemBean;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.MeiAdLocalBean;
import com.savor.ads.bean.OOHLinkAdLocalBean;
import com.savor.ads.bean.ProjectionLogBean;
import com.savor.ads.bean.PushRTBItem;
import com.savor.ads.bean.ShopGoodsBean;
import com.savor.ads.bean.VersionInfo;
import com.savor.ads.bean.WelcomeResourceBean;
import com.savor.ads.bean.YishouAdLocalBean;
import com.savor.ads.bean.ZmengAdLocalBean;
import com.savor.ads.core.ApiRequestListener;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.database.DBHelper;
import com.savor.ads.oss.OSSUtils;
import com.savor.ads.oss.OSSValues;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * @author 朵朵花开
 * <p>
 * 常用系统工具类
 */
public class AppUtils {


    public static final String BoxLogDir = "log";
    public static final String BoxlogedDir = "loged";
    public static final String BoxMediaDir = "media";
    public static final String BoxMulticast = "multicast";//已删除
    public static final String BoxLotteryDir = "lottery";//已删除
    public static final String BoxPPTDir = "ppt";//已删除
    public static final String BoxSpecialtyDir = "specialty";//已删除
    public static final String rtbAdsDir = "rtb_ads";
    public static final String polyAdsDir = "poly_ads";
    public static final String polyAdsOnlineDir = "poly_ads_online";
    public static final String BoxQRCodeLog = "qrcode_log";
    public static final String BoxBirthdayOndemandDir = "birthday_ondemand";
    public static final String BoxInteractionAdsDir = "interaction_ads";
    public static final String ActivityAds = "activity_ads";
    public static final String GoodsAds = "goods_ads";
    public static final String OptimizeAds = "optimize_ads";//已删除
    public static final String BoxProjectionDir = "projection";
    public static final String BoxSelectContentDir = "select_content";
    public static final String cacheDir = "cache";
    public static final String welcomdeResourceDir = "welcome_resource";
    // UTF-8 encoding
    private static final String ENCODING_UTF8 = "UTF-8";

    /**
     * DATE FORMAT 日期格式 例如"yyyy-MM-dd HH:mm:ss"
     */
    public static final String DATEFORMAT_YYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final String DATEFORMAT_YYMMDD = "yyyy-MM-dd";

    public static enum StorageMode {
        /**
         * 手机内存
         */
        MobileMemory,
        /**
         * 存储卡
         */
        SDCard;
    }

    public static enum StorageFile {
        /**
         * 缓存
         */
        cache,
        /**
         * 老规则日志
         */
        log,
        /**
         * 已上传到小平台的日志
         */
        loged,
        /**
         * 广告视频目录
         */
        media,
        /**
         * RTB广告目录
         */
        rtb_ads,
        /**
         * poly聚屏广告预下载目录
         */
        poly_ads,
        /**
         * poly聚屏广告在线下载目录
         */
        poly_ads_online,
        /**
         * 小程序码显示log
         */
        qrcode_log,
        /***
         * 生日点播
         */
        birthday_ondemand,
        /**
         * 互动广告（片头片尾）
         */
        interaction_ads,
        /**
         * 商家活动广告
         */
        activity_ads,
        /**商城商品广告*/
        goods_ads,
        /**
         * 投屏文件存储目录(视频，图片)
         */
        projection,
        /**
         * 用户精选内容
         */
        select_content,
        /**
         * 欢迎词资源
         */
        welcome_resource

    }

    private static TrustManager[] trustAllCerts;
    private static StorageMode storageMode;

    /** SDCard是否可用 **/

    /**
     * SDCard的根路径
     **/
    private static String SDCARD_PATH;
    public static String EXTERNAL_SDCARD_PATH = "";
    public static final int NOCONNECTION = 0;
    public static final int WIFI = 1;
    public static final int MOBILE = 2;

    /**
     * 返回手机连接网络类型
     *
     * @param context
     * @return 0： 无连接  1：wifi  2： mobile
     */
    public static int getNetworkType(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        int networkType = NOCONNECTION;
        if (networkInfo != null) {
            int type = networkInfo.getType();
            networkType = type == ConnectivityManager.TYPE_WIFI ? WIFI : MOBILE;
        }
        return networkType;
    }


    /**
     * 取得SD卡路径，以/结尾
     *
     * @return SD卡路径
     */
    public static String getSDCardPath() {
        boolean IS_MOUNTED = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        if (!IS_MOUNTED) {
            return null;
        }
        if (null != SDCARD_PATH) {
            return SDCARD_PATH;
        }
        File path = Environment.getExternalStorageDirectory();
        String SDCardPath = path.getAbsolutePath();
        SDCardPath += SDCardPath.endsWith(File.separator) ? "" : File.separator;
        SDCARD_PATH = SDCardPath;
        return SDCardPath;
    }

    /**
     * 获取主媒体路径
     *
     * @return
     */
    public static String getMainMediaPath() {
        if (AppUtils.isMstar()) {
            if (!TextUtils.isEmpty(EXTERNAL_SDCARD_PATH)) {
                return EXTERNAL_SDCARD_PATH;
            }
            String sdcard_path = null;
            String sd_default = Environment.getExternalStorageDirectory().getAbsolutePath();
            LogUtils.d(sd_default);
            if (sd_default.endsWith("/")) {
                sd_default = sd_default.substring(0, sd_default.length() - 1);
            }
            // 得到路径
            try {
                Runtime runtime = Runtime.getRuntime();
                Process proc = runtime.exec("mount");
                InputStream is = proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                String line;
                BufferedReader br = new BufferedReader(isr);
                while ((line = br.readLine()) != null) {
                    if (line.contains("secure"))
                        continue;
                    if (line.contains("asec"))
                        continue;
                    if (line.contains("fat") && line.contains("/mnt/")) {
                        String columns[] = line.split(" ");
                        if (columns != null && columns.length > 1) {
                            if (sd_default.trim().equals(columns[1].trim())) {
                                continue;
                            }
                            sdcard_path = columns[1];
                        }
                    } else if (line.contains("fuse") && line.contains("/mnt/")) {
                        String columns[] = line.split(" ");
                        if (columns != null && columns.length > 1) {
                            if (sd_default.trim().equals(columns[1].trim())) {
                                continue;
                            }
                            sdcard_path = columns[1];
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            EXTERNAL_SDCARD_PATH = sdcard_path;
            return sdcard_path;
        } else {
            return getSDCardPath();
        }
    }

    /**
     * @param mode    StorageFile.cache or StorageFile.file
     * @return
     */
    public static String getFilePath(StorageFile mode) {
        String path = getMainMediaPath();

        File targetLogFile = new File(path + File.separator, BoxLogDir);
        if (!targetLogFile.exists()) {
            targetLogFile.mkdir();
        }
        File targetLogedFile = new File(path + File.separator, BoxlogedDir);
        if (!targetLogedFile.exists()) {
            targetLogedFile.mkdir();
        }
        File targetMediaFile = new File(path + File.separator, BoxMediaDir);
        if (!targetMediaFile.exists()) {
            targetMediaFile.mkdir();
        }
        File targetCacheFile = new File(path + File.separator, cacheDir);
        if (!targetCacheFile.exists()) {
            targetCacheFile.mkdir();
        }
        File rtbFile = new File(path + File.separator, rtbAdsDir);
        if (!rtbFile.exists()) {
            rtbFile.mkdir();
        }
        File polyFile = new File(path + File.separator, polyAdsDir);
        if (!polyFile.exists()) {
            polyFile.mkdir();
        }
        File polyOnlineFile = new File(path + File.separator, polyAdsOnlineDir);
        if (!polyOnlineFile.exists()){
            polyOnlineFile.mkdir();
        }
        File qrcodeFile = new File(path+File.separator,BoxQRCodeLog);
        if (!qrcodeFile.exists()){
            qrcodeFile.mkdir();
        }
        File birthdayFile = new File(path+File.separator,BoxBirthdayOndemandDir);
        if (!birthdayFile.exists()){
            birthdayFile.mkdir();
        }
        File iAdsFile = new File(path+File.separator,BoxInteractionAdsDir);
        if (!iAdsFile.exists()){
            iAdsFile.mkdir();
        }
        File actFile = new File(path+File.separator,ActivityAds);
        if (!actFile.exists()){
            actFile.mkdir();
        }
        File goodsFile = new File(path+File.separator,GoodsAds);
        if (!goodsFile.exists()){
            goodsFile.mkdir();
        }
        File projectionFile = new File(path+File.separator,BoxProjectionDir);
        if (!projectionFile.exists()){
            projectionFile.mkdir();
        }
        File userSelectFile = new File(path+File.separator,BoxSelectContentDir);
        if (!userSelectFile.exists()){
            userSelectFile.mkdir();
        }
        File welcomeResourceFile = new File(path+File.separator,welcomdeResourceDir);
        if (!welcomeResourceFile.exists()){
            welcomeResourceFile.mkdir();
        }
        if (mode == StorageFile.log) {
            path = targetLogFile.getAbsolutePath() + File.separator;
        } else if (mode == StorageFile.loged) {
            path = targetLogedFile.getAbsolutePath() + File.separator;
        } else if (mode == StorageFile.media) {
            path = targetMediaFile.getAbsolutePath() + File.separator;
        }  else if (mode == StorageFile.cache) {
            path = targetCacheFile.getAbsolutePath() + File.separator;
        } else if (mode == StorageFile.rtb_ads) {
            path = rtbFile.getAbsolutePath() + File.separator;
        } else if (mode == StorageFile.poly_ads) {
            path = polyFile.getAbsolutePath() + File.separator;
        } else if (mode==StorageFile.poly_ads_online){
            path = polyOnlineFile.getAbsolutePath()+File.separator;
        }else if (mode == StorageFile.qrcode_log) {
            path = qrcodeFile.getAbsolutePath() + File.separator;
        } else if (mode == StorageFile.birthday_ondemand){
            path = birthdayFile.getAbsolutePath()+File.separator;
        } else if(mode == StorageFile.interaction_ads){
            path = iAdsFile.getAbsolutePath()+File.separator;
        } else if (mode == StorageFile.activity_ads){
            path = actFile.getAbsolutePath()+File.separator;
        }else if (mode == StorageFile.goods_ads){
            path = goodsFile.getAbsolutePath()+File.separator;
        } else if (mode == StorageFile.projection){
            path = projectionFile.getAbsolutePath()+File.separator;
        } else if (mode == StorageFile.select_content){
            path = userSelectFile.getAbsolutePath() +File.separator;
        } else if (mode == StorageFile.welcome_resource){
            path = welcomeResourceFile.getAbsolutePath()+File.separator;
        }
        return path;
    }

    /**
     * 删除闲置文件目录
     */
    public static void deleteIdleDirectory(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = getMainMediaPath();
                File lotteryFile = new File(path + File.separator,BoxLotteryDir);
                if (lotteryFile.exists()){
                    if (lotteryFile.isDirectory()&&lotteryFile.listFiles().length>0){
                        com.savor.ads.utils.FileUtils.deleteFile(lotteryFile);
                    }
                    lotteryFile.delete();
                }
                File pptFile = new File(path+File.separator,BoxPPTDir);
                if (pptFile.exists()){
                    if (pptFile.isDirectory()&&pptFile.listFiles().length>0){
                        com.savor.ads.utils.FileUtils.deleteFile(pptFile);
                    }
                    pptFile.delete();
                }
                File specialtyFile = new File(path+File.separator,BoxSpecialtyDir);
                if (specialtyFile.exists()){
                    if (specialtyFile.isDirectory()&&specialtyFile.listFiles().length>0){
                        com.savor.ads.utils.FileUtils.deleteFile(specialtyFile);
                    }
                    specialtyFile.delete();
                }
                File optimizeFile = new File(path+File.separator,OptimizeAds);
                if (optimizeFile.exists()){
                    if (optimizeFile.isDirectory()&&optimizeFile.listFiles().length>0){
                        com.savor.ads.utils.FileUtils.deleteFile(optimizeFile);
                    }
                    optimizeFile.delete();
                }
                File multicastFile = new File(path+File.separator,BoxMulticast);
                if (multicastFile.exists()){
                    if (multicastFile.isDirectory()&&multicastFile.listFiles().length>0){
                        com.savor.ads.utils.FileUtils.deleteFile(multicastFile);
                    }
                    multicastFile.delete();
                }
            }
        }).start();
    }

    /**
     * 判断文件夹是否为空
     *
     * @param path
     * @return
     */
    public static boolean isDirNull(String path) {

        File file = new File(path);
        if (file.exists()) {
            return true;
        } else
            return false;

    }

    /**
     * 判断文件是否存在
     *
     * @param path
     * @return
     */
    public static boolean isFileExist(String path) {

        if (path == null || path.length() <= 0) {
            return false;
        }
        File file = new File(path);
        if (file.exists() && file.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    //复制文件
    public static void copyFile(String srcFile, String destFile) {
        // 复制文件
        int byteread = 0; // 读取的字节数
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];

            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static String getEasyMd5(File f) {
        InputStream in = null;
        byte[] frontb = null;
        byte[] backb = null;
        byte[] newb1 = null;
        byte[] newb2 = null;
        String endb = null;
        try {

            FileInputStream stream = new java.io.FileInputStream(f);
            LogUtils.i("一次读多个字节,当前文件" + f.getName() + "的字节数是：" + showAvailableBytes(stream));
//            allTex.append(String.valueOf(showAvailableBytes(stream)));
            int pos = 0;// 从第几个字节开始读
            int len = 200;// 读几个字节
            //			stream.skip(pos); // 跳过之前的字节数
            frontb = new byte[len];
            stream.read(frontb);

            int allChar = showAvailableBytes(stream);
            if (allChar > 400) {
                int poss = allChar - 200;// 从第几个字节开始读
                int lens = 200;// 读几个字节
                stream.skip(poss); // 跳过之前的字节数
                byte[] bs = new byte[200];
                stream.read(bs);
                backb = bs;
            }

            newb1 = new byte[frontb.length];
            System.arraycopy(frontb, 0, newb1, 0, frontb.length);
            newb2 = new byte[backb.length];
            System.arraycopy(backb, 0, newb2, 0, backb.length);
//            newb = new byte[frontb.length + backb.length];
//            System.arraycopy(frontb, 0, newb, 0, frontb.length);
//            System.arraycopy(backb, 0, newb, frontb.length, backb.length);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
        // 调取md5方法，生成一个md5串
        String md5Vod1 = MD5(newb1);
        String md5Vod2 = MD5(newb2);

        String md5Vod = getMD5(md5Vod1 + md5Vod2);
        return md5Vod;
    }

    /**
     * 显示输入流中还剩的字节数
     */
    private static int showAvailableBytes(InputStream in) {
        try {
            //       LogUtils.i(("当前字节输入流中的字节数为:" + in.available());
            return in.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * MD5加密
     *
     * @param
     * @return
     */
    public final static String MD5(byte[] b) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(b);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void trustAllSSLForHttpsURLConnection() {
        // Create a trust manager that does not validate certificate chains
        if (trustAllCerts == null) {
            trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
        }
        // Install the all-trusting trust manager
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
        HttpsURLConnection.setDefaultHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }

    /**
     * Returns whether the network is available
     */
    public static boolean isNetworkAvailable(Context context) {

        if (context == null) {
            return false;
        }

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            LogUtils.e("couldn't get connectivity manager");
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0, length = info.length; i < length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the network is mobile
     */
    public static boolean isMobileNetwork(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            // LogUtils.w(Constants.TAG, "couldn't get connectivity manager");
        } else {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWifiNetwork(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            // LogUtils.w(Constants.TAG, "couldn't get connectivity manager");
        } else {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }

    public static String getCurTime() {
        SimpleDateFormat df = new SimpleDateFormat(DATEFORMAT_YYMMDD_HHMMSS);//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }

    /**
     * 根据日期格式获取当前日期
     */
    public static String getCurTime(String format) {
        SimpleDateFormat dfTemp = new SimpleDateFormat(format);//设置日期格式
        return dfTemp.format(new Date());// new Date()为获取当前系统时间
    }

    public static Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(DATEFORMAT_YYMMDD_HHMMSS);//设置日期格式
        return df.parse(dateStr);
    }

    public static int calculateMonthDiff(String dateSmall, String dateBig, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar bef = Calendar.getInstance();
        Calendar aft = Calendar.getInstance();
        bef.setTime(sdf.parse(dateSmall));
        aft.setTime(sdf.parse(dateBig));
        int result = aft.get(Calendar.MONTH) - bef.get(Calendar.MONTH);
        int month = (aft.get(Calendar.YEAR) - bef.get(Calendar.YEAR)) * 12;
        return month + result;
    }

    /**
     * 根据日期格式解析日期
     */
    public static Date parseDate(String dateStr, String format) throws ParseException {
        SimpleDateFormat dfTemp = new SimpleDateFormat(format);//设置日期格式
        return dfTemp.parse(dateStr);
    }

    /**
     * 根据日期格式获取当前日期
     */
    public static String getStrTime(String time) {
        String mTime = time.replaceAll("-", "");
        return mTime;// new Date()为获取当前系统时间
    }

    /**
     * 时间戳转字符串格式
     * @param seconds
     * @param format
     * @return
     */
    public static String timeStamp2Date(long seconds,String format) {
        if(seconds == 0){
            return "";
        }
       if(format == null || format.isEmpty()){
                format = "yyyy-MM-dd HH:mm:ss";
            }
         SimpleDateFormat sdf = new SimpleDateFormat(format);
         return sdf.format(new Date(seconds));
     }


    public static void deleteOldMedia(final Context context,final boolean delCache) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // PlayListVersion为空说明没有一个完整的播放列表（初装的时候），这时不做删除操作，以免删掉了手动拷入的视频
                if (TextUtils.isEmpty(Session.get(context).getProPeriod())) {
                    return;
                }

                //排除当前已经完整下载的文件和正在下载的文件，其他删除
                String path = AppUtils.getFilePath(AppUtils.StorageFile.media);
                File[] listFiles = new File(path).listFiles();
                if (listFiles == null || listFiles.length == 0) {
                    return;
                }
                try {
                    DBHelper dbHelper = DBHelper.get(context);
                    if (dbHelper.findPlayListByWhere(null, null) == null &&
                            dbHelper.findNewPlayListByWhere(null, null) == null) {
                        return;
                    }
                    for (File file : listFiles) {
                        if (file.isFile()) {
                            String fileName;
                            int length = file.getName().length();
                            if (file.getName().endsWith(ConstantValues.CACHE)){
                                if (delCache){
                                    fileName = file.getName().substring(0,length-6);
                                }else{
                                    continue;
                                }
                            }else{
                                fileName = file.getName();
                            }
                            String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=?";
                            String[] selectionArgs = new String[]{fileName};

                            if (dbHelper.findPlayListByWhere(selection, selectionArgs) == null &&
                                    dbHelper.findNewPlayListByWhere(selection, selectionArgs) == null &&
                                    dbHelper.findAdsByWhere(selection, selectionArgs) == null &&
                                    dbHelper.findNewAdsByWhere(selection, selectionArgs) == null) {
                                    file.delete();
                                LogFileUtil.write("数据库中未发现该文件记录--删除文件:"+file.getName());
                                LogUtils.d("删除文件===================" + file.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 启动清除投屏数据
     * @param context
     */
    public static void deleteProjectionData(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String projectionPath = AppUtils.getFilePath(StorageFile.projection);
                File[] listProjectionFiles = new File(projectionPath).listFiles();
                if (listProjectionFiles == null || listProjectionFiles.length == 0) {
                    return;
                }
                for (File file:listProjectionFiles){
                    if (file.isFile()){
                        file.delete();
                    }
                }

                DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.PROJECTION_LOG,null,null);
            }
        }).start();
    }

    /**
     * 清除奥凌sdp广告物料
     * @param context
     */
    public static void cleanDspOnlineAdsData(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {

                String selection = DBHelper.MediaDBInfo.FieldName.MEDIATYPE + "=? ";
                String[] selectionArgs = new String[]{ConstantValues.POLY_ADS_ONLINE};
                List<MediaLibBean> list = DBHelper.get(context).findRtbadsMediaLibByWhere(selection, selectionArgs);
                if (list==null||list.size()==0){
                    File[] listFiles = new File(AppUtils.getFilePath(StorageFile.poly_ads_online)).listFiles();
                    for (File file:listFiles){
                        if (file.isFile()){
                            file.delete();
                        }else {
                            com.savor.ads.utils.FileUtils.deleteFile(file);
                        }
                    }
                    return;
                }
                List<MediaLibBean> listNames = new ArrayList<>();
                for (MediaLibBean bean:list){
                    String createTime = bean.getCreateTime();
                    long time;
                    if (!TextUtils.isEmpty(createTime)){
                        time = Long.valueOf(createTime);
                        long nowTime = System.currentTimeMillis();
                        long diffTime = nowTime-time;
                        if (diffTime/1000/3600/24>3){
                            listNames.add(bean);
                        }
                    }

                }
                if (!listNames.isEmpty()){
                    for (MediaLibBean bean:listNames){
                        String md5 = bean.getTp_md5();
                        String path = bean.getMediaPath();
                        selection = DBHelper.MediaDBInfo.FieldName.TP_MD5 + "=? ";
                        selectionArgs = new String[]{md5};
                        DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.RTB_ADS,selection,selectionArgs);
                        File file = new File(path);
                        if (file.exists()){
                            file.delete();
                        }
                    }
                }

            }
        }).start();

    }

    public static void deleteInteractionAdsMedia(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    LogUtils.d("删除点播视频");

                    //内存不足情况下删除点播视频
                    String media = AppUtils.getFilePath(StorageFile.interaction_ads);
                    File[] adsFiles = new File(media).listFiles();
                    for (File file : adsFiles) {
                        if (file.isFile()) {
                            file.delete();
                            LogUtils.d("删除文件===================" + file.getName());
                        } else {
                            com.savor.ads.utils.FileUtils.deleteFile(file);
                        }
                    }
                    DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.INTERACTION_ADS,null,null);
                }catch (Exception e){
                    LogUtils.e("删除视频失败",e);
                }

            }
        }).start();
    }
    /**清除活动商品广告数据*/
    public static void deleteActivityAdsMedia(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    LogUtils.d("删除活动广告视频");

                    //内存不足情况下删除点播视频
                    String media = AppUtils.getFilePath(StorageFile.activity_ads);
                    File[] adsFiles = new File(media).listFiles();
                    for (File file : adsFiles) {
                        if (file.isFile()) {
                            file.delete();
                            LogUtils.d("删除文件===================" + file.getName());
                        } else {
                            com.savor.ads.utils.FileUtils.deleteFile(file);
                        }
                    }
                    String selection = DBHelper.MediaDBInfo.FieldName.PLAY_TYPE + "=? or "
                            +DBHelper.MediaDBInfo.FieldName.PLAY_TYPE + "=? ";
                    String[] selectionArgs = new String[]{"1","2"};
                    DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.ACTIVITY_ADS,selection,selectionArgs);
                }catch (Exception e){
                    LogUtils.e("删除视频失败",e);
                }

            }
        }).start();
    }

    public static void deleteShopGoodsAdsMedia(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    LogUtils.d("删除活动广告视频");

                    //内存不足情况下删除点播视频
                    String goods = AppUtils.getFilePath(StorageFile.goods_ads);
                    File[] goodsFiles = new File(goods).listFiles();
                    for (File file : goodsFiles) {
                        if (file.isFile()) {
                            file.delete();
                            LogUtils.d("删除文件===================" + file.getName());
                        } else {
                            com.savor.ads.utils.FileUtils.deleteFile(file);
                        }
                    }
                    String selection = "";
                    String[] selectionArgs = new String[]{};
                    DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SHOP_GOODS_ADS,selection,selectionArgs);
                }catch (Exception e){
                    LogUtils.e("删除视频失败",e);
                }

            }
        }).start();
    }

    public static void deleteSelectContentMedia(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    LogUtils.d("删除精选和发现视频");
                    List<MediaLibBean> selectContentList = DBHelper.get(context).findSelectContentList(null,null);
                    String media = AppUtils.getFilePath(StorageFile.select_content);
                    File[] adsFiles = new File(media).listFiles();
                    if (selectContentList==null){

                        for (File file : adsFiles) {
                            if (file.isFile()) {
                                file.delete();
                                LogUtils.d("删除文件===================" + file.getName());
                            } else {
                                com.savor.ads.utils.FileUtils.deleteFile(file);
                            }
                        }
                        DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SELECT_CONTENT,null,null);
                        DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.MEDIA_ITEM,null,null);
                    }else{

                        for (File file:adsFiles){
                            String fileName = file.getName();
                            String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
                            String[] selectionArgs = new String[]{fileName};
                            List<MediaItemBean> listItem = DBHelper.get(context).findMediaItemList(selection,selectionArgs);
                            if (listItem==null){
                                file.delete();
                            }
                        }

                    }


                }catch (Exception e){
                    LogUtils.e("删除视频失败",e);
                }

            }
        }).start();
    }

    public static void deleteWelcomeResource(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    LogUtils.d("删除欢迎词资源");
                    List<WelcomeResourceBean> welcomeResourceBeanList = DBHelper.get(context).findWelcomeResourceList(null,null);
                    String welcome = AppUtils.getFilePath(StorageFile.welcome_resource);
                    File[] welcomeFiles = new File(welcome).listFiles();
                    if (welcomeResourceBeanList==null){
                        //内存不足情况下删除点播视频
                        for (File file : welcomeFiles) {
                            if (file.isFile()) {
                                file.delete();
                                LogUtils.d("删除文件===================" + file.getName());
                            } else {
                                com.savor.ads.utils.FileUtils.deleteFile(file);
                            }
                        }
                        DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.WELCOME_RESOURCE,null,null);
                    }else{
                        for (File file:welcomeFiles){
                            String fileName = file.getName();
                            String selection = DBHelper.MediaDBInfo.FieldName.MEDIANAME + "=? ";
                            String[] selectionArgs = new String[]{fileName};
                            List<WelcomeResourceBean> listBean = DBHelper.get(context).findWelcomeResourceList(selection,selectionArgs);
                            if (listBean==null){
                                file.delete();
                            }
                        }

                    }


                }catch (Exception e){
                    LogUtils.e("删除视频失败",e);
                }

            }
        }).start();
    }

    public static void deleteAdsData(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    LogUtils.d("删除广告视频");
                    List<MediaLibBean> listAds = DBHelper.get(context).findAdsByWhere(null,null);
                    if (listAds!=null&&listAds.size()>0){
                        for (MediaLibBean bean:listAds){
                           String mediaPath = bean.getMediaPath();
                           if (!TextUtils.isEmpty(mediaPath)){
                               File file = new File(mediaPath);
                               if (file.isFile()){
                                   file.delete();
                               }
                           }
                        }
                    }
                    DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.NEWADSLIST,null,null);
                    DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.ADSLIST,null,null);
                }catch (Exception e){
                    LogUtils.e("删除视频失败",e);
                }

            }
        }).start();
    }


    public static void clearAllCache(final Context context) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 删除sdcard中form表单处理可能遗留的临时文件
                String sdcardPath = getSDCardPath();
                if (!TextUtils.isEmpty(sdcardPath)) {
                    File sdcardDirectory = new File(sdcardPath);
                    if (sdcardDirectory.exists()) {
                        LogFileUtil.write("clearAllCache will clear MultiPart files");
                        File[] files = sdcardDirectory.listFiles();

                        if (files != null) {
                            for (File file : files) {
                                if (file.isFile() && file.getName().startsWith("MultiPart")) {
                                    com.savor.ads.utils.FileUtils.deleteFile(file);
                                }
                            }
                        }
                    }
                }
            }
        }).start();
    }

    public static void deleteCacheData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String cachePath = AppUtils.getFilePath(StorageFile.cache);
                File[] cacheFiles = new File(cachePath).listFiles();
                if (cacheFiles == null || cacheFiles.length == 0) {
                    return;
                }
                for (File file:cacheFiles){
                    String name = file.getName();
                    if (file.isFile()&&!name.contains("getBoxQrcode")){
                        file.delete();
                    }
                }
            }
        }).start();
    }

    public static void updateProjectionLog(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Activity activity = ActivitiesManager.getInstance().getCurrentActivity();
                    if (activity instanceof ScreenProjectionActivity){

                    }else{
                        List<ProjectionLogBean> list = DBHelper.get(context).findProjectionLogs(null,null);
                        if (list!=null&&list.size()>0){
                            for (ProjectionLogBean bean:list){
                                final String mediaPath = bean.getMedia_path();
                                long resuorceSize = 0;
                                if (!TextUtils.isEmpty(bean.getResource_size())){
                                    resuorceSize = Long.valueOf(bean.getResource_size());
                                }
                                String path = AppUtils.getFilePath(AppUtils.StorageFile.projection);
                                JSONArray jsonArray = new JSONArray();
                                if (TextUtils.isEmpty(mediaPath)){
                                    continue;
                                }
                                File suorcefile = new File(mediaPath);
                                if (!suorcefile.exists()){
                                    String selection = DBHelper.MediaDBInfo.FieldName.FORSCREEN_ID + "=? ";
                                    String[] selectionArgs = new String[]{bean.getForscreen_id()};
                                    DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.PROJECTION_LOG,selection,selectionArgs);
                                    continue;
                                }
                                long simpleUploadSize = Session.get(context).getSimple_upload_size();
                                boolean flag;
                                String ossPath;
                                String[] filePaths = bean.getMedia_path().split("\\/");
                                final String fileName = filePaths[filePaths.length-1];
                                if (resuorceSize<simpleUploadSize){
                                    ossPath = OSSValues.uploadSimplePath+fileName;
                                    OSSUtils ossUtils =  new OSSUtils(context,
                                            BuildConfig.OSS_BUCKET_NAME,
                                            ossPath,
                                            mediaPath);
                                    flag = ossUtils.syncUploadFile();
                                    if (flag){
                                        jsonArray.put(ossPath);
                                    }
                                }else {
                                    String resourceType = bean.getResource_type();
                                    //0和1:图片,2:视频
                                    if ("0".equals(resourceType)||"1".equals(resourceType)){
                                        String filePath = path+"img_ys"+fileName;
                                        File destFile = new File(filePath);
                                        FileUtils.copyFile(suorcefile,destFile);
                                        Bitmap imageBitmap = BitmapFactory.decodeFile(filePath);
                                        saveImage(imageBitmap,filePath);
                                        ossPath = OSSValues.uploadSimplePath+"img_ys"+fileName;
                                        OSSUtils ossUtils =  new OSSUtils(context,
                                                BuildConfig.OSS_BUCKET_NAME,
                                                ossPath,
                                                filePath);
                                        flag = ossUtils.syncUploadFile();
                                        if (flag){
                                            jsonArray.put(ossPath);
                                        }
                                    }else if ("2".equals(resourceType)){
                                        long duation = Float.valueOf(bean.getDuration()).longValue()*1000;
                                        long time = duation/4;
                                        for (int i =1;i<5;i++){

                                            Bitmap bitmap = GlideImageLoader.loadVideoScreenshot(context,mediaPath,time*i*1000);
                                            if (bitmap==null){
                                                continue;
                                            }
                                            String filePath = path+"video_ys_"+i+"_"+fileName.split("\\.")[0]+".jpg";
                                            saveImage(bitmap,filePath);

                                            ossPath = OSSValues.uploadSimplePath+"video_ys_"+i+"_"+fileName.split("\\.")[0]+".jpg";
                                            OSSUtils ossUtils =  new OSSUtils(context,
                                                    BuildConfig.OSS_BUCKET_NAME,
                                                    ossPath,
                                                    filePath);
                                            flag = ossUtils.syncUploadFile();
                                            if (flag){
                                                jsonArray.put(ossPath);
                                            }
                                        }
                                    }
                                }
                                if (jsonArray.length()>0){
                                    updateSimpleProjectionLog(context,bean,jsonArray);
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public static void updateSimpleProjectionLog(final Context context, ProjectionLogBean bean, JSONArray jsonArray){
        long time  = Long.parseLong(bean.getCreate_time());
        if (jsonArray.length()==0){
            return;
        }
        HashMap<String,Object> params = new HashMap<>();
        params.put("box_mac", Session.get(context).getEthernetMac());
        params.put("forscreen_id", bean.getForscreen_id());
        params.put("resource_addr", jsonArray.toString());
        params.put("resource_id", bean.getResource_id());
        AppApi.updateSimpleProjectionLog(context, new ApiRequestListener() {
            @Override
            public void onSuccess(AppApi.Action method, Object obj) {
                if (obj instanceof String){
                    String forscreenId = (String)obj;
                    String selection = DBHelper.MediaDBInfo.FieldName.FORSCREEN_ID + "=? ";
                    String[] selectionArgs = new String[]{forscreenId};
                    DBHelper.get(context).deleteDataByWhere(DBHelper.MediaDBInfo.TableName.PROJECTION_LOG,selection,selectionArgs);
                }
            }

            @Override
            public void onError(AppApi.Action method, Object obj) {

            }

            @Override
            public void onNetworkFailed(AppApi.Action method) {

            }
        }, params, bean.getForscreen_id());
    }

    /**
     * <p>
     * Get UTF8 bytes from a string
     * </p>
     *
     * @param string String
     * @return UTF8 byte array, or null if failed to get UTF8 byte array
     */
    public static byte[] getUTF8Bytes(String string) {
        if (string == null)
            return new byte[0];

        try {
            return string.getBytes(ENCODING_UTF8);
        } catch (UnsupportedEncodingException e) {
            /*
             * If system doesn't support UTF-8, use another way
             */
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeUTF(string);
                byte[] jdata = bos.toByteArray();
                bos.close();
                dos.close();
                byte[] buff = new byte[jdata.length - 2];
                System.arraycopy(jdata, 2, buff, 0, buff.length);
                return buff;
            } catch (IOException ex) {
                return new byte[0];
            }
        }
    }

    /**
     * <p>
     * Get string in UTF-8 encoding
     * </p>
     *
     * @param b byte array
     * @return string in utf-8 encoding, or empty if the byte array is not encoded with UTF-8
     */
    public static String getUTF8String(byte[] b) {
        if (b == null)
            return "";
        return getUTF8String(b, 0, b.length);
    }

    /**
     * <p>
     * Get string in UTF-8 encoding
     * </p>
     */
    public static String getUTF8String(byte[] b, int start, int length) {
        if (b == null) {
            return "";
        } else {
            try {
                return new String(b, start, length, ENCODING_UTF8);
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        }
    }

    public static void chmod(String permission, String path) {
        try {
            String command = "chmod " + permission + " " + path;
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(command);
            if (proc != null) {
                BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                String line = null;
                while ((line = is.readLine()) != null) {
                    LogUtils.d("aMarket line:" + line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断当前应用是否是顶栈
     *
     * @param context
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String curPackage = info.packageName;
            ActivityManager mActivityManager = ((ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE));
            List<RunningTaskInfo> tasksInfo = mActivityManager.getRunningTasks(1);
            if (tasksInfo != null && tasksInfo.size() > 0) {
                if (!TextUtils.isEmpty(curPackage) && curPackage.equals(tasksInfo.get(0).topActivity.getPackageName())) {
                    /**当前应用是顶栈*/
                    return true;
                }

            }
        } catch (Exception ex) {
            LogUtils.e(ex.toString());
        }
        return false;
    }

    public static long getFileSizes(File f) {
        long s = 0;
        FileInputStream fis = null;
        try {
            if (!f.exists()) {
                return s;
            }
            fis = new FileInputStream(f);
            s = fis.available();
        } catch (Exception ex) {
            LogUtils.e(ex.toString());
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fis = null;
        }

        return s;
    }


    /**
     * (获得本机的IP地址
     *
     * @return
     */
    public static String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (android.os.Build.VERSION.SDK_INT > 10) {
                        /**android 4.0以上版本*/
                        if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                            return inetAddress.getHostAddress().toString();
                        }
                    } else {
                        if (!inetAddress.isLoopbackAddress()) {
                            return inetAddress.getHostAddress().toString();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 计算md5值
     */
    public static byte[] getMd5(String str) {
        if (str == null) {
            return null;
        }
        byte[] result = null;
        try {
            result = getMd5(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }
        return result;
    }

    /**
     * 计算md5值
     */
    public static byte[] getMd5(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StreamUtils su = new StreamUtils(true);
        try {
            su.copyStreamInner(new ByteArrayInputStream(bytes), null);
        } catch (IOException e) {
        }
        return su.getMD5();
    }

    /**
     * Get MD5 Code
     */
    public static String getMD5(String text) {
        try {
            byte[] byteArray = text.getBytes("utf8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(byteArray, 0, byteArray.length);
            return StringUtils.toHexString(md.digest(), false);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Get MD5 Code
     */
    public static String getMD5(byte[] byteArray) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(byteArray, 0, byteArray.length);
            return StringUtils.toHexString(md.digest(), false);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String getMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return StringUtils.toHexString(digest.digest(), true);

    }

    /**
     * 隐藏软键盘
     *
     * @param activity
     */
    public static void hideSoftKeybord(Activity activity) {

        if (null == activity) {
            return;
        }
        try {
            final View v = activity.getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        } catch (Exception e) {

        }
    }

    /**
     * 强制聚焦并打开键盘
     *
     * @param activity
     * @param editText
     */
    public static void tryFocusEditText(Activity activity, EditText editText) {

        if (editText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * 应用是否在后台运行
     * @param context
     * @return
     */
    public static boolean isRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        // 枚举进程
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void appToFront(Context context){
        if (isRunningForeground(context)) {
//        if (!getTopPackageName().equals("com.savor.ads")) {
            //获取ActivityManager
            ActivityManager mAm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            //获得当前运行的task
            List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(100);
            for (ActivityManager.RunningTaskInfo rti : taskList) {
                //找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台
                if (rti.topActivity.getPackageName().equals(context.getPackageName())) {
                    mAm.moveTaskToFront(rti.id, 0);
                    return;
                }
            }
            //若没有找到运行的task，用户结束了task或被系统释放，则重新启动mainactivity
            Intent resultIntent = new Intent(context, MainActivity.class);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(resultIntent);
        }
    }

//    public static String getTopPackageName(){
//        String	topPackageName = "";
//        IActivityManager am1 = android.app.ActivityManagerNative.getDefault();
//
//        try {
//            int stackId = am1.getFocusedStackId();
//            StackInfo info = am1.getStackInfo(stackId);
//            if(info.topActivity !=null){
//                topPackageName= info.topActivity.getPackageName();
//            }
//        } catch (RemoteException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    /**
     * wifi IP
     */
    public static String getWifiIp(Context con) {
        WifiManager wifiManager = (WifiManager) con
                .getSystemService(Context.WIFI_SERVICE);// 获取WifiManager

        WifiInfo wifiinfo = wifiManager.getConnectionInfo();

        int i = wifiinfo.getIpAddress();
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + ((i >> 24) & 0xFF);

    }

    public static String getWifiName(Context con) {

        WifiManager wifiManager = (WifiManager) con
                .getSystemService(Context.WIFI_SERVICE);// 获取WifiManager
        WifiInfo wifiinfo = wifiManager.getConnectionInfo();
        return wifiinfo.getSSID();
    }

    /**
     * 根据下标返回信号源描述
     *
     * @param index
     * @return
     */
    public static String getInputType(int index) {
        String type = "ANT IN";
        switch (index) {
            case 0:
                type = "ANT IN";
                break;
            case 1:
                type = "HDMI IN";
                break;
            case 2:
                type = "AV IN";
                break;
        }
        return type;
    }

    /**
     * 获取以太网MAC地址
     *
     * @return
     */
    public static String getEthernetMacAddr() {
        String cmd = "busybox ifconfig eth0";
        Process process = null;
        InputStream is = null;
        BufferedReader reader = null;
        String result = "";
        try {
            process = Runtime.getRuntime().exec(cmd);
            is = process.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(is));
            String line = reader.readLine();
            if (!TextUtils.isEmpty(line)) {
                result = line.substring(line.indexOf("HWaddr") + 6).trim();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取以太网 IP
     *
     * @return
     */
    public static String getEthernetIP() {
        String cmd = "busybox ifconfig eth0";
        Process process = null;
        InputStream is = null;
        BufferedReader reader = null;
        String result = "";
        try {
            process = Runtime.getRuntime().exec(cmd);
            is = process.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                if (!TextUtils.isEmpty(line) && line.trim().startsWith("inet ")) {
                    result = line.substring(line.indexOf("addr:") + 5);
                    result = result.substring(0, result.indexOf(" ")).trim();
                    break;
                }
                line = reader.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取Wlan MAC地址
     *
     * @return
     */
    public static String getWlanMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }



    /**
     * 获取Wlan IP
     *
     * @return
     */
    public static String getWlanIP() {
        String cmd = "busybox ifconfig wlan0";
        Process process = null;
        InputStream is = null;
        BufferedReader reader = null;
        String result = "";
        try {
            process = Runtime.getRuntime().exec(cmd);
            is = process.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                if (!TextUtils.isEmpty(line) && line.trim().startsWith("inet ")) {
                    result = line.substring(line.indexOf("addr:") + 5);
                    result = result.substring(0, result.indexOf(" ")).trim();
                    break;
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    //检查redian文件夹下是否有图有真相
    public static boolean checkIsImageFile(String fName) {

        boolean isImageFile;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg") || FileEnd.equals("bmp")) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }

    public static boolean zipFile(File srcFile, File zipFile, String comment) throws Exception {
//        BufferedInputStream in = null;

        ZipOutputStream zOutStream = null;
        if (srcFile == null || zipFile == null) return false;

        long fileSize = srcFile.length();
        if (!srcFile.exists()) {
            return false;
        }
        try {
            if (zipFile.exists()) {
                zipFile.delete();
            }
            // 创建字节输入流对象
//            in = new BufferedInputStream(new FileInputStream(srcFile));//文件输入流
            boolean flag = zipFile.createNewFile();
            zOutStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            ZipEntry en = new ZipEntry(srcFile.getName());
            en.setSize(srcFile.length());
            zOutStream.putNextEntry(en);
            zOutStream.setComment(comment);
            // 向压缩文件中输出数据
//            int temp = 0;
//            byte b[] =new byte[1024];
//            while ((temp = in.read(b))!=-1) {//读取内容
//                zOutStream.write(b,0,temp);//压缩输出
//            }
            byte[] byFile = FileUtils.readFileToByteArray(srcFile);
            zOutStream.write(byFile);
            zOutStream.flush();
            zOutStream.closeEntry();

        } catch (Exception e) {
            // TODO: handle exception
            LogUtils.e(e.toString());
            return false;
        } finally {
            try {
//                if (in!=null){
//                    in.close();
//                }
                if (zOutStream != null) {
                    zOutStream.close();
                    zOutStream = null;
                }
            } catch (Exception e2) {
                // TODO: handle exception
                LogUtils.e(e2.toString());
            }
        }
        return true;
    }

    /**
     * @param context
     * @param serverVersion
     * @param type：1是rom升级，2是apk升级
     * @return
     */
    public static boolean needUpdate(Context context, String serverVersion, int type) {
        Session session = Session.get(context);
        if (serverVersion == null) {
            return false;
        }
        String localVersion = null;
        if (type == 1) {
            String rom = session.getRomVersion();
            if (!TextUtils.isEmpty(rom)) {
                localVersion = rom.replace("V", "").trim();
            } else {
                return false;
            }
        } else {
            localVersion = session.getVersionName();
        }
        if (!TextUtils.isEmpty(serverVersion) && !localVersion.equals(serverVersion)) {
            return true;
        }
        return false;
    }


    public static Bitmap getLoacalBitmap(String url) {

        try {

            FileInputStream fis = new FileInputStream(url);

            return BitmapFactory.decodeStream(fis);

        } catch (FileNotFoundException e) {

            e.printStackTrace();

            return null;

        }

    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    /**
     * double转成int
     * @param number
     * @return
     */
    public static int getInt(double number){
        BigDecimal bd=new BigDecimal(number).setScale(0, BigDecimal.ROUND_HALF_UP);
        return Integer.parseInt(bd.toString());
    }

    //获取热点状态
    public static int getWifiAPState(Context context) {
        int state = -1;
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method2 = wifiManager.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state;
    }

    public static boolean setWifiApEnabled(Context context, boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.allowedAuthAlgorithms.clear();
            wifiConfig.allowedGroupCiphers.clear();
            wifiConfig.allowedKeyManagement.clear();
            wifiConfig.allowedPairwiseCiphers.clear();
            wifiConfig.allowedProtocols.clear();
            //配置热点的名称
            String ssid = "";
            if (!TextUtils.isEmpty(Session.get(context).getBoxName())) {
                ssid = Session.get(context).getBoxName();
            } else {
                String mac = getEthernetMacAddr();
                if (!TextUtils.isEmpty(mac) && mac.length() > 3) {
                    mac = mac.replaceAll(":", "");
                    ssid = "RD" + mac.substring(mac.length() - 3);
                } else {
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    ssid = "RD" + timestamp.substring(timestamp.length() - 5);
                }
            }
            wifiConfig.SSID = ssid;
            wifiConfig.preSharedKey = "11111111";
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.allowedKeyManagement.set(4 /*WifiConfiguration.KeyMgmt.NONE*/);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN/*WPA*/);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.status = WifiConfiguration.Status.ENABLED;
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, wifiConfig, enabled);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static String getWifiApIp() {
        String cmd = "ip route show";
        Process process = null;
        InputStream is = null;
        BufferedReader reader = null;
        String result = "";
        try {
            process = Runtime.getRuntime().exec(cmd);
            is = process.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(is));
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains("wlan0") && line.contains("proto kernel")) {
                    result = line.substring(line.lastIndexOf(" ") + 1).trim();
                    break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String getWifiApName(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfig = (WifiConfiguration) method.invoke(wifiManager);
            return wifiConfig.SSID;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检测是否可播放下一期
     *
     * @param context
     * @return
     */
    public static boolean checkPlayTime(Context context) {
        boolean canPlayNext = false;
        Session session = Session.get(context);
        String pubTime = session.getProNextMediaPubTime();
        if (!TextUtils.isEmpty(pubTime)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date pubDate = format.parse(pubTime);
                if (pubDate.getTime() < System.currentTimeMillis()) {
                    LogUtils.d("checkPlayTime 已到达发布时间，将更新期号");
                    LogFileUtil.write("checkPlayTime 已到达发布时间，将更新期号");
                    canPlayNext = true;


                    session.setProPeriod(session.getProNextPeriod());
                    session.setProNextPeriod(null);
                    session.setProNextMediaPubTime(null);

                    session.setAdvPeriod(session.getAdvNextPeriod());
                    session.setAdvNextPeriod(null);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                LogFileUtil.write("checkPlayTime 检测发布时间异常:" + e.getLocalizedMessage());
            }
        }
        return canPlayNext;
    }

    public static String getShowingSSID(Context context) {
        if (isMstar()) {
            String ssid = AppUtils.getWifiName(context);
            if (TextUtils.isEmpty(ssid)) {
                ssid = Session.get(context).getBoxName();
            }
            return ssid;
        } else {
            return Session.get(context).getBoxName();
        }
    }

    public static long getAvailableExtSize() {
        StatFs stat = new StatFs(getMainMediaPath());
        long blockSize = stat.getBlockSize();
        long blocks = stat.getAvailableBlocks();
        return blockSize * blocks;
    }

    public static String findSpecifiedPeriodByType(ArrayList<VersionInfo> versionList, String type) {
        String period = "";
        if (versionList != null && type != null) {
            for (VersionInfo versionInfo : versionList) {
                if (type.equals(versionInfo.getType())) {
                    period = versionInfo.getVersion();
                    break;
                }
            }
        }
        return period;
    }

    public static boolean isMstar() {
        return Build.MODEL.contains("MStar");
    }

    public static boolean isSVT(){
        return Build.BRAND.contains("XRD");
    }

    public static boolean isGiec(){
        return  Build.MODEL.contains("t962e");
    }

    public static boolean isLeTV() {
        boolean flag = false;
        if (Build.MODEL.contains("Y55")||Build.MODEL.contains("Y65")){
            flag = true;
        }
        return flag;
    }

    /**
     * 根据Pid获取当前进程的名字，一般就是当前app的包名
     *
     * @param context 上下文
     * @return 返回进程的名字
     */
    public static String getProcessName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List list = activityManager.getRunningAppProcesses();
        Iterator i = list.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == android.os.Process.myPid()) {
                    // 根据进程的信息获取当前进程的名字
                    return info.processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 没有匹配的项，返回为null
        return null;
    }

    /**
     * 获取投屏前后是否加互动广告
     * @param context
     * @return
     */
    public static MediaLibBean getInteractionAds(Context context){
        MediaLibBean bean = null;
        Session session = Session.get(context);
        try {
            if (session.isOpenInteractscreenad()){
                int num = session.getSystemSappForscreenNums();
                if (num==0){
                    return null;
                }
                if (GlobalValues.INTERVAL_INTERACTION_ADS_NUM>=num){
                    GlobalValues.INTERVAL_INTERACTION_ADS_NUM=1;
                    List<MediaLibBean> list = DBHelper.get(context).findInteractionAdsByWhere(null,null);
                    if (list!=null&&list.size()>0){
                        if (GlobalValues.CURRENT_INTERACTION_ADS_NUM>=list.size()){
                            GlobalValues.CURRENT_INTERACTION_ADS_NUM =0;
                        }
                        bean = list.get(GlobalValues.CURRENT_INTERACTION_ADS_NUM);

                        GlobalValues.CURRENT_INTERACTION_ADS_NUM++;
                    }
                }else {
                    GlobalValues.INTERVAL_INTERACTION_ADS_NUM ++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return bean;
    }


    /**
     * 填充播放列表
     *
     * @param context
     * @param resultList 填充后的播放列表
     * @param type       填充目的。1：播放；2：上报
     * @return 填充是否成功
     */
    public static <T extends MediaLibBean> boolean fillPlaylist(Context context, ArrayList<MediaLibBean> resultList, int type) {
        if (resultList == null && type == 2) {
            return false;
        }

        DBHelper dbHelper = DBHelper.get(context);
        ArrayList<MediaLibBean> playList = dbHelper.getOrderedPlayList();

//        ArrayList<MediaLibBean> rtbMedias = new ArrayList<>();
//        ArrayList<Long> rtbEndTimes = new ArrayList<>();
//        if (Session.get(context).getRTBPushItems() != null) {
//            for (PushRTBItem item : Session.get(context).getRTBPushItems()) {
//                String selection = DBHelper.MediaDBInfo.FieldName.VID + "=? ";
//                String[] selectionArgs = new String[]{item.getId()};
//                List<MediaLibBean> list = dbHelper.findRtbadsMediaLibByWhere(selection, selectionArgs);
//                if (list != null) {
//                    rtbMedias.add(list.get(0));
//                    rtbEndTimes.add(item.getRemain_time());
//                }
//            }
//        }
        String selection = DBHelper.MediaDBInfo.FieldName.PLAY_TYPE + "=? ";
        //1为插入节目单，2为没有插入节目单
        String[] selectionArgs = new String[]{"1"};
        List<MediaLibBean> activityAdsList = dbHelper.findActivityAdsByWhere(selection,selectionArgs);
        selection = DBHelper.MediaDBInfo.FieldName.TYPE + "=? ";
        //type:类型 1热播内容(上大屏内容) 2发现内容
        selectionArgs = new String[]{"1"};
        List<MediaLibBean> selectContentList = dbHelper.findSelectContentList(selection,selectionArgs);
        List<MediaLibBean> shopGoodsAdsList = dbHelper.findShopGoodsAdsByWhere(new String(),new String[]{});
        if (playList != null && !playList.isEmpty()) {
            int rtbIndex = 0;
            int polyIndex = 0;
            int actIndex = 0;
            int scIndex = 0;
            for (int i = 0; i < playList.size(); i++) {
                MediaLibBean bean = playList.get(i);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // 特殊处理ads数据
                if (bean.getType().equals(ConstantValues.ADS)) {
                    selection = DBHelper.MediaDBInfo.FieldName.LOCATION_ID + "=? ";
                    selectionArgs = new String[]{bean.getLocation_id()};
                    List<MediaLibBean> list = dbHelper.findAdsByWhere(selection, selectionArgs);
                    if (list != null && !list.isEmpty()) {
                        for (MediaLibBean item : list) {
                            Date startDate = null;
                            Date endDate = null;
                            try {
                                startDate = format.parse(item.getStart_date());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            try {
                                endDate = format.parse(item.getEnd_date());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Date now = new Date();
                            if (startDate != null && endDate != null &&
                                    now.after(startDate) && now.before(endDate)) {
                                bean.setVid(item.getVid());
                                bean.setDuration(item.getDuration());
                                bean.setMd5(item.getMd5());
                                bean.setName(item.getName());
                                bean.setMediaPath(item.getMediaPath());
                                bean.setSuffix(item.getSuffix());
                                bean.setChinese_name(item.getChinese_name());
                                bean.setStart_date(item.getStart_date());
                                bean.setEnd_date(item.getEnd_date());
                                bean.setIs_sapp_qrcode(item.getIs_sapp_qrcode());
                                break;
                            }
                        }
                    }
                }
//                if (ConstantValues.RTB_ADS.equals(bean.getType())) {
//                    if (!rtbMedias.isEmpty()) {
//                        if (rtbIndex < rtbEndTimes.size() &&
//                                rtbEndTimes.get(rtbIndex) > System.currentTimeMillis()) {
//                            MediaLibBean rtbItem = rtbMedias.get(rtbIndex);
//                            bean.setName(rtbItem.getName());
//                            bean.setMediaPath(rtbItem.getMediaPath());
//                            bean.setAdmaster_sin(rtbItem.getAdmaster_sin());
//                            bean.setChinese_name(rtbItem.getChinese_name());
//                            bean.setDuration(rtbItem.getDuration());
//                            bean.setVid(rtbItem.getVid());
//                            bean.setMd5(rtbItem.getMd5());
//                            bean.setPeriod(rtbItem.getPeriod());
//                            bean.setEnd_date(format.format(new Date(rtbEndTimes.get(rtbIndex))));
//                        }
//
//                        rtbIndex = (++rtbIndex) % rtbMedias.size();
//                    }
//                }
                //活动广告数据
                //活动商品上大屏
                if (ConstantValues.ACTGOODS.equals(bean.getType())){
                    if (activityAdsList!=null&&!activityAdsList.isEmpty()){
                        if (actIndex>=activityAdsList.size()){
                            actIndex = 0;
                        }
                        MediaLibBean actAdsItem = activityAdsList.get(actIndex);
                        try {
                            long nowTime = System.currentTimeMillis();
                            Date dateStart = AppUtils.parseDate(actAdsItem.getStart_date());
                            Date dateEnd = AppUtils.parseDate(actAdsItem.getEnd_date());
                            long startTime = dateStart.getTime();
                            long endTime = dateEnd.getTime();
                            if (nowTime>startTime&&nowTime<endTime){
                                bean.setVid(actAdsItem.getVid());
                                bean.setGoods_id(actAdsItem.getGoods_id());
                                bean.setName(actAdsItem.getName());
                                bean.setChinese_name(actAdsItem.getChinese_name());
                                bean.setType(actAdsItem.getType());
                                bean.setMedia_type(actAdsItem.getMedia_type());
                                bean.setIs_storebuy(actAdsItem.getIs_storebuy());
                                bean.setMd5(actAdsItem.getMd5());
                                bean.setDuration(actAdsItem.getDuration());
                                bean.setMediaPath(actAdsItem.getMediaPath());
                                bean.setPrice(actAdsItem.getPrice());
                                bean.setQrcode_path(actAdsItem.getQrcode_path());
                                bean.setQrcode_url(actAdsItem.getQrcode_url());
                                bean.setCreateTime(actAdsItem.getCreateTime());
                                bean.setStart_date(actAdsItem.getStart_date());
                                bean.setEnd_date(actAdsItem.getEnd_date());
                            }else if (nowTime>endTime){
                                selection = DBHelper.MediaDBInfo.FieldName.VID + "=? ";
                                selectionArgs = new String[]{actAdsItem.getVid()};
                                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.ACTIVITY_ADS,selection,selectionArgs);
                                activityAdsList.remove(actAdsItem);
                            }
                            actIndex++;
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
                //商城商品上大屏
                if (ConstantValues.SHOP_GOODS_ADS.equals(bean.getType())){
                    String location_id = bean.getLocation_id();
                    if (shopGoodsAdsList==null||shopGoodsAdsList.isEmpty()){
                        bean.setVid("");
                    }else{
                        for (MediaLibBean goods:shopGoodsAdsList){
                            String lid = goods.getLocation_id();
                            if (lid.equals(location_id)){
                                bean.setVid(goods.getVid());
                                bean.setGoods_id(goods.getGoods_id());
                                bean.setName(goods.getName());
                                bean.setChinese_name(goods.getChinese_name());
                                bean.setMedia_type(goods.getMedia_type());
                                bean.setMd5(goods.getMd5());
                                bean.setDuration(goods.getDuration());
                                bean.setMediaPath(goods.getMediaPath());
                                bean.setQrcode_path(goods.getQrcode_path());
                                bean.setQrcode_url(goods.getQrcode_url());
                                bean.setCreateTime(goods.getCreateTime());
                            }
                        }
                    }
                }
                //精选内容上大屏数据
                if (ConstantValues.SELECT_CONTENT.equals(bean.getType())){
                    if (selectContentList!=null&&!selectContentList.isEmpty()){
                        if (scIndex>=selectContentList.size()){
                            scIndex = 0;
                        }
                        MediaLibBean scItem = selectContentList.get(scIndex);
                        try {
                            selection = DBHelper.MediaDBInfo.FieldName.ID + "=? ";
                            selectionArgs = new String[]{String.valueOf(scItem.getId())};
                            List<MediaItemBean> listItem = dbHelper.findMediaItemList(selection,selectionArgs);
                            String paths = "";
                            if (listItem!=null&&listItem.size()>0){
                                for(MediaItemBean item:listItem){
                                    if (!TextUtils.isEmpty(item.getOss_path())){
                                        File file = new File(item.getOss_path());
                                        if (!TextUtils.isEmpty(item.getMd5()) &&file.exists()) {
                                            if (item.getMd5().equals(AppUtils.getEasyMd5(file))
                                                    ||item.getMd5().toUpperCase().equals(AppUtils.getMD5(file))) {
                                                paths = paths+item.getOss_path()+",";
                                                if (item.getMedia_type()==1){
                                                    //处理遇到视频情况下，把子条目md5赋值给父，为了下面校验使用
                                                    scItem.setMd5(item.getMd5());
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!TextUtils.isEmpty(paths)){
                                    paths = paths.substring(0,paths.length()-1);
                                }

                            }

                            long nowTime = System.currentTimeMillis();
                            Date dateStart = AppUtils.parseDate(scItem.getStart_date());
                            Date dateEnd = AppUtils.parseDate(scItem.getEnd_date());
                            long startTime = dateStart.getTime();
                            long endTime = dateEnd.getTime();
                            if (nowTime>startTime&&nowTime<endTime){
                                bean.setVid(scItem.getId()+"");
                                bean.setId(scItem.getId());
                                bean.setName(scItem.getName());
                                bean.setChinese_name("用户精选");
                                bean.setMedia_type(scItem.getMedia_type());
                                bean.setMd5(scItem.getMd5());
                                bean.setDuration(scItem.getDuration());
                                bean.setMediaPath(paths);
                                bean.setCreateTime(scItem.getCreateTime());
                                bean.setStart_date(scItem.getStart_date());
                                bean.setEnd_date(scItem.getEnd_date());
                                bean.setNickName(scItem.getNickName());
                                bean.setAvatarUrl(scItem.getAvatarUrl());
                            }else if (nowTime>endTime){
                                selection = DBHelper.MediaDBInfo.FieldName.ID + "=? ";
                                selectionArgs = new String[]{scItem.getVid()};
                                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.SELECT_CONTENT,selection,selectionArgs);
                                dbHelper.deleteDataByWhere(DBHelper.MediaDBInfo.TableName.MEDIA_ITEM,selection,selectionArgs);
                                selectContentList.remove(scItem);
                            }
                            scIndex++;
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
                boolean fileCheck=true;
                File mediaFile = null;
                if (!TextUtils.isEmpty(bean.getMediaPath())){
                    if (bean.getMedia_type()==21){
                        fileCheck = false;
                    }else{
                        mediaFile = new File(bean.getMediaPath());
                        if (!TextUtils.isEmpty(bean.getMd5()) &&
                                !TextUtils.isEmpty(bean.getMediaPath()) &&
                                mediaFile.exists()) {
                            if (bean.getMd5().equals(AppUtils.getEasyMd5(mediaFile))
                                    ||bean.getMd5().toUpperCase().equals(AppUtils.getMD5(mediaFile))) {
                                fileCheck = false;
                            }
                        }
                    }

                }

                if (fileCheck) {
                    if (!TextUtils.isEmpty(bean.getVid())) {
                        LogUtils.e("媒体文件校验失败! vid:" + bean.getVid());
                    }
                    // 校验失败时将文件路径置空，下面会删除掉为空的项
                    bean.setMediaPath(null);
                    if (mediaFile!=null&&mediaFile.exists()) {
                        mediaFile.delete();
                    }
                }
            }


            // 填充百度聚屏数据
            if (GlobalValues.POLY_BAIDU_ADS_PLAY_LIST != null && !GlobalValues.POLY_BAIDU_ADS_PLAY_LIST.isEmpty()) {
                boolean stopIterator = false;
                // 这里遍历两次是为了处理当末尾的情况
                for (int j = 0; j < 2; j++) {
                    if (stopIterator)
                        break;
                    for (int i = 0; i < playList.size(); i++) {
                        MediaLibBean bean = playList.get(i);
                        if (polyIndex >= GlobalValues.POLY_BAIDU_ADS_PLAY_LIST.size()) {
                            stopIterator = true;
                            break;
                        }
                        if (ConstantValues.POLY_ADS.equals(bean.getType()) &&
                                GlobalValues.CURRENT_MEDIA_ORDER < bean.getOrder() + playList.get(playList.size() - 1).getOrder() * j) {
                            BaiduAdLocalBean polyItem = GlobalValues.POLY_BAIDU_ADS_PLAY_LIST.get(polyIndex++);
                            polyItem.setOrder(bean.getOrder());
                            polyItem.setPeriod(bean.getPeriod());
                            polyItem.setLocation_id(bean.getLocation_id());
                            playList.set(i, polyItem);
                        }
                    }
                }
            }
            //填充众盟广告数据
            if (GlobalValues.DSP_ZMENG_ADS_PLAY_LIST != null && !GlobalValues.DSP_ZMENG_ADS_PLAY_LIST.isEmpty()) {
                boolean stopIterator = false;
                // 这里遍历两次是为了处理当末尾的情况
                for (int j = 0; j < 2; j++) {
                    if (stopIterator)
                        break;
                    for (int i = 0; i < playList.size(); i++) {
                        MediaLibBean bean = playList.get(i);
                        if (polyIndex >= GlobalValues.DSP_ZMENG_ADS_PLAY_LIST.size()) {
                            stopIterator = true;
                            break;
                        }
                        if (ConstantValues.POLY_ADS.equals(bean.getType()) &&
                                GlobalValues.CURRENT_MEDIA_ORDER < bean.getOrder() + playList.get(playList.size() - 1).getOrder() * j) {
                            ZmengAdLocalBean polyItem = GlobalValues.DSP_ZMENG_ADS_PLAY_LIST.get(polyIndex++);
                            polyItem.setOrder(bean.getOrder());
                            polyItem.setPeriod(bean.getPeriod());
                            polyItem.setLocation_id(bean.getLocation_id());
                            playList.set(i, polyItem);
                        }
                    }
                }
            }
            //填充钛镁聚屏广告数据
            if (GlobalValues.POLY_MEI_ADS_PLAY_LIST!=null&&!GlobalValues.POLY_MEI_ADS_PLAY_LIST.isEmpty()){
                boolean stopIterator = false;
                // 这里遍历两次是为了处理当末尾的情况
                for (int j = 0; j < 2; j++) {
                    if (stopIterator)
                        break;
                    for (int i = 0; i < playList.size(); i++) {
                        MediaLibBean bean = playList.get(i);
                        if (polyIndex >= GlobalValues.POLY_MEI_ADS_PLAY_LIST.size()) {
                            stopIterator = true;
                            break;
                        }
                        if (ConstantValues.POLY_ADS.equals(bean.getType()) &&
                                GlobalValues.CURRENT_MEDIA_ORDER < bean.getOrder() + playList.get(playList.size() - 1).getOrder() * j) {
                            MeiAdLocalBean polyItem = GlobalValues.POLY_MEI_ADS_PLAY_LIST.get(polyIndex++);
                            polyItem.setOrder(bean.getOrder());
                            polyItem.setPeriod(bean.getPeriod());
                            polyItem.setLocation_id(bean.getLocation_id());
                            playList.set(i, polyItem);
                        }
                    }
                }
                GlobalValues.POLY_MEI_ADS_PLAY_LIST.clear();
            }
            //填充奥凌聚屏广告数据(poly_online)
            if (GlobalValues.DSP_OOHLINK_ADS_PLAY_LIST!=null&&!GlobalValues.DSP_OOHLINK_ADS_PLAY_LIST.isEmpty()){
                boolean stopIterator = false;
                // 这里遍历两次是为了处理当末尾的情况
                for (int j = 0; j < 2; j++) {
                    if (stopIterator)
                        break;
                    for (int i = 0; i < playList.size(); i++) {
                        MediaLibBean bean = playList.get(i);
                        if (polyIndex >= GlobalValues.DSP_OOHLINK_ADS_PLAY_LIST.size()) {
                            stopIterator = true;
                            break;
                        }
                        if (ConstantValues.POLY_ADS.equals(bean.getType()) &&
                                GlobalValues.CURRENT_MEDIA_ORDER < bean.getOrder() + playList.get(playList.size() - 1).getOrder() * j) {
                            OOHLinkAdLocalBean polyItem = GlobalValues.DSP_OOHLINK_ADS_PLAY_LIST.get(polyIndex++);
                            polyItem.setOrder(bean.getOrder());
                            polyItem.setPeriod(bean.getPeriod());
                            polyItem.setLocation_id(bean.getLocation_id());
                            playList.set(i, polyItem);
                        }
                    }
                }
                GlobalValues.DSP_OOHLINK_ADS_PLAY_LIST.clear();
            }
            //填充京东钼媒聚屏广告数据(poly_online)
            if (GlobalValues.DSP_JDMOMEDIA_ADS_PLAY_LIST!=null&&!GlobalValues.DSP_JDMOMEDIA_ADS_PLAY_LIST.isEmpty()){
                boolean stopIterator = false;
                // 这里遍历两次是为了处理当末尾的情况
                for (int j = 0; j < 2; j++) {
                    if (stopIterator)
                        break;
                    for (int i = 0; i < playList.size(); i++) {
                        MediaLibBean bean = playList.get(i);
                        if (polyIndex >= GlobalValues.DSP_JDMOMEDIA_ADS_PLAY_LIST.size()) {
                            stopIterator = true;
                            break;
                        }
                        if (ConstantValues.POLY_ADS.equals(bean.getType()) &&
                                GlobalValues.CURRENT_MEDIA_ORDER < bean.getOrder() + playList.get(playList.size() - 1).getOrder() * j) {
                            JDmomediaLocalBean polyItem = GlobalValues.DSP_JDMOMEDIA_ADS_PLAY_LIST.get(polyIndex++);
                            polyItem.setOrder(bean.getOrder());
                            polyItem.setPeriod(bean.getPeriod());
                            polyItem.setLocation_id(bean.getLocation_id());
                            playList.set(i, polyItem);
                        }
                    }
                }
                GlobalValues.DSP_JDMOMEDIA_ADS_PLAY_LIST.clear();
            }
            //填充易售聚屏广告数据(poly_online)
            if (GlobalValues.DSP_YISHOU_ADS_PLAY_LIST!=null&&!GlobalValues.DSP_YISHOU_ADS_PLAY_LIST.isEmpty()){
                boolean stopIterator = false;
                // 这里遍历两次是为了处理当末尾的情况
                for (int j = 0; j < 2; j++) {
                    if (stopIterator)
                        break;
                    for (int i = 0; i < playList.size(); i++) {
                        MediaLibBean bean = playList.get(i);
                        if (polyIndex >= GlobalValues.DSP_YISHOU_ADS_PLAY_LIST.size()) {
                            stopIterator = true;
                            break;
                        }
                        if (ConstantValues.POLY_ADS.equals(bean.getType()) &&
                                GlobalValues.CURRENT_MEDIA_ORDER < bean.getOrder() + playList.get(playList.size() - 1).getOrder() * j) {
                            YishouAdLocalBean polyItem = GlobalValues.DSP_YISHOU_ADS_PLAY_LIST.get(polyIndex++);
                            polyItem.setOrder(bean.getOrder());
                            polyItem.setPeriod(bean.getPeriod());
                            polyItem.setLocation_id(bean.getLocation_id());
                            playList.set(i, polyItem);
                        }
                    }
                }
                GlobalValues.DSP_YISHOU_ADS_PLAY_LIST.clear();
            }
        }

        if (playList != null && !playList.isEmpty()) {
            // 处理已下载的新节目插队
            ArrayList<MediaLibBean> list = new ArrayList<>();
            ArrayList<MediaLibBean> tempList = dbHelper.getTempProList();
            LogUtils.e("临时节目集合 " + tempList);
            int tempMediaIndex = 0;
            for (int i = playList.size() - 1; i >= 0; i--) {
                MediaLibBean bean = playList.get(i);
                if (!TextUtils.isEmpty(bean.getMediaPath()) || ConstantValues.POLY_ADS.equals(bean.getType())) {
                    if (type == 1) {
                        boolean doReplace = false;
                        if (tempList != null && !tempList.isEmpty()) {
                            if (ConstantValues.PRO.equals(bean.getType()) && tempMediaIndex < tempList.size()) {
                                doReplace = true;
                            }
                        }
                        if (doReplace) {
                            LogUtils.d("做替换 下标为" + i);
                            MediaLibBean temp = tempList.get(tempMediaIndex++);
                            // 这里改media本身的order是为了后面计算播放列表更新后计算播放位置做铺垫
                            temp.setOrder(bean.getOrder());
                            list.add(0, temp);
                        } else {
                            list.add(0, bean);
                        }
                    } else {
                        list.add(0, bean);
                        resultList.add(0, bean);
                    }
                }
            }
            GlobalValues.getInstance().PLAY_LIST = list;
        } else {
            File mediaDir = new File(AppUtils.getFilePath(AppUtils.StorageFile.media));
            if (mediaDir.exists() && mediaDir.isDirectory()) {
                File[] files = mediaDir.listFiles();
                ArrayList<MediaLibBean> filePlayList = new ArrayList<>();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()&&!file.getName().endsWith(ConstantValues.CACHE)) {
                            MediaLibBean bean = new MediaLibBean();
                            bean.setMediaPath(file.getPath());
                            bean.setChinese_name(file.getName());
                            filePlayList.add(bean);
                            if (type == 2) {
                                resultList.add(bean);
                            }
                        }
                    }
                }
                GlobalValues.getInstance().PLAY_LIST = filePlayList;
            }
        }

        return (type == 2 && resultList != null && !resultList.isEmpty()) ||
                (type == 1 && GlobalValues.getInstance().PLAY_LIST != null && !GlobalValues.getInstance().PLAY_LIST.isEmpty());
    }

    public static String getDeviceId(Context context) {
        String deviceId = "";
        try {
            deviceId = getIMEI(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (deviceId == null || "".equals(deviceId) || "0".equals(deviceId)) {
            try {
                deviceId = getLocalMac(context).replace(":", "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (deviceId == null || "".equals(deviceId) || "0".equals(deviceId)) {
            try {
                deviceId = getAndroidId(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return deviceId;
    }
    //IMEI号
    public static String getIMEI(Context context){
        TelephonyManager telephonyManager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei=telephonyManager.getDeviceId();
        return imei;
    }
    // Mac地址
    private static String getLocalMac(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }
    // Mac地址
    private static String getAndroidId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }



    /**
     * 文件是否下载完成判定(通过简单md5值校验，前200字节和后200字节)
     *
     * @param path
     * @param md5
     * @return
     * @throws IOException
     */
    public static boolean isDownloadEasyCompleted(String path, String md5){
        if (AppUtils.isFileExist(path)) {
            String realMd5 = AppUtils.getEasyMd5(new File(path));
            if (!TextUtils.isEmpty(md5) && md5.equals(realMd5)) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     *验证文件夏赞完成校验判定(完整md5值校验)
     * @param path
     * @param md5
     * @return
     */
    public static boolean isDownloadCompleted(String path, String md5){
        if (AppUtils.isFileExist(path)) {
            File file = new File(path);
            String realMd5 = AppUtils.getMD5(file);
            if (!TextUtils.isEmpty(md5) && md5.equals(realMd5)) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }



    public static Bitmap getVideoThumbnail(String filePath){
        MediaMetadataRetriever mmr  = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        //获取指定位置指定宽高的缩略图
        long timeUs = 1000*1000;
//        return mmr.getScaledFrameAtTime(timeUs,MediaMetadataRetriever.OPTION_CLOSEST,512,384);
        return mmr.getFrameAtTime();
    }


    public static File saveImage(Bitmap bmp,String localPath) {

        File file = new File(localPath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.close();
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            bmp.recycle();
        }
        return file;
    }


    public static String bytesToString(ByteString src, String charSet) {
        if(org.apache.commons.lang3.StringUtils.isEmpty(charSet)) {
            charSet = "UTF-8";
        }
        return bytesToString(src.toByteArray(), charSet);
    }

    public static String bytesToString(byte[] input, String charSet) {

        if(ArrayUtils.isEmpty(input)) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }

        ByteBuffer buffer = ByteBuffer.allocate(input.length);
        buffer.put(input);
        buffer.flip();

        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;

        try {
            charset = Charset.forName(charSet);
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());

            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取svt主板电视信号源在那个通道
     * @param context
     * @return
     */
    public static int queryCurInputSrc(Context context) {
        int sourceIndex = -1;
        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/systemsetting"),
                null, null, null, null);
        if (cursor.moveToFirst()) {
            sourceIndex = cursor.getInt(cursor.getColumnIndex("enInputSourceType"));
        }
        cursor.close();
        return sourceIndex;
    }

}
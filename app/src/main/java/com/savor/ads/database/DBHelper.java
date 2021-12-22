package com.savor.ads.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.savor.ads.bean.ActivityGoodsBean;
import com.savor.ads.bean.BirthdayOndemandBean;
import com.savor.ads.bean.MediaItemBean;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.ProjectionLogBean;
import com.savor.ads.bean.ProjectionLogDetail;
import com.savor.ads.bean.ProjectionLogHistory;
import com.savor.ads.bean.SelectContentBean;
import com.savor.ads.bean.ShopGoodsBean;
import com.savor.ads.bean.WelcomeOrderBean;
import com.savor.ads.bean.WelcomeResourceBean;
import com.savor.ads.core.Session;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.ACTION;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.ADS_ID;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.ADS_ORDER;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.AVATAR_URL;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.BOX_MAC;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.CHINESE_NAME;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.CREATETIME;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.DURATION;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.END_DATE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.FORSCREEN_CHAR;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.FORSCREEN_ID;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.GOODS_ID;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.ID;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.IS_SAPP_QRCODE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.IS_SHARE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.IS_STOREBUY;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.LOCATION_ID;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.MD5;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.MEDIAID;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.MEDIANAME;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.MEDIATYPE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.MEDIA_PATH;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.MEDIA_SCREENSHOT_PATH;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.MOBILE_BRAND;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.MOBILE_MODEL;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.NEW_RESOURCE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.NICK_NAME;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.OPENID;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.PAGES;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.PERIOD;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.PLAY_POSITION;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.PLAY_TYPE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.PRICE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.QRCODE_PATH;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.QRCODE_URL;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.REPEAT;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.RESOURCE_ID;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.RESOURCE_SIZE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.RESOURCE_TYPE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.SERIAL_NUMBER;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.SMALL_APP_ID;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.START_DATE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.SURFIX;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.TYPE;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.UPLOADED;
import static com.savor.ads.database.DBHelper.MediaDBInfo.FieldName.VID;
import static com.savor.ads.database.DBHelper.MediaDBInfo.TableName;

/**
 * Created by zhanghq on 2016/12/9.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";
    private SQLiteDatabase db = null;
    private static DBHelper dbHelper = null;

    public static class MediaDBInfo {

        public static class FieldName {
            public static final String ID = "id";
            public static final String SERIAL_NUMBER = "serial_number";
            public static final String LOCATION_ID = "location_id";
            public static final String PERIOD = "period";
            public static final String ADS_ORDER = "ads_order";
            public static final String VID = "vid";
            public static final String ADS_ID = "ads_id";
            public static final String GOODS_ID = "goods_id";
            public static final String MD5 = "md5";
            public static final String MEDIAID = "media_id";
            public static final String MEDIANAME = "media_name";
            public static final String MEDIATYPE = "media_type";
            public static final String RESOURCE_TYPE = "resource_type";
            public static final String MEDIA_PATH = "media_path";
            public static final String MEDIA_SCREENSHOT_PATH = "media_screenshot_path";
            public static final String QRCODE_PATH = "qrcode_path";
            public static final String QRCODE_URL = "qrcode_url";
            public static final String IS_SAPP_QRCODE = "is_sapp_qrcode";
            public static final String CHINESE_NAME = "chinese_name";
            public static final String SURFIX = "surfix";
            public static final String DURATION = "duration";
            public static final String PRICE = "price";
            public static final String PLAY_TYPE = "play_type";
            public static final String IS_STOREBUY = "is_storebuy";
            public static final String CREATETIME = "create_time";
            public static final String PLAY_POSITION = "play_position";
            public static final String START_DATE = "start_date";
            public static final String END_DATE = "end_date";
            public static final String ADMASTER_SIN = "admaster_sin";
            /**聚屏类型：1.百度*/
            public static final String TPMEDIA_ID = "tpmedia_id";
            /**百度返回md5值*/
            public static final String TP_MD5 = "tp_md5";
            public static final String ACTION = "p_action";
            public static final String BOX_MAC = "box_mac";
            public static final String FORSCREEN_CHAR = "forscreen_char";
            public static final String ROTATION = "rotation";
            public static final String FORSCREEN_ID = "forscreen_id";
            public static final String MOBILE_BRAND = "mobile_brand";
            public static final String MOBILE_MODEL = "mobile_model";
            public static final String OPENID = "openid";
            public static final String RESOURCE_ID = "resource_id";
            public static final String RESOURCE_SIZE = "resource_size";
            public static final String SMALL_APP_ID = "small_app_id";
            public static final String NICK_NAME = "nick_name";
            public static final String AVATAR_URL = "avatar_url";
            public static final String TYPE = "type";
            public static final String PAGES = "pages";
            public static final String IS_SHARE = "is_share";
            //是否已经上传,0:未上传，1：已上传
            public static final String UPLOADED = "uploaded";
            //是否已经上传,0:一投，1：重投
            public static final String REPEAT = "repeat";
            //是否是新一期的资源
            public static final String NEW_RESOURCE = "new_resource";
        }

        public static class TableName {
            public static final String NEWPLAYLIST = "newplaylist_talbe";
            public static final String PLAYLIST = "playlist_talbe";
            public static final String NEWADSLIST = "new_adslist_table";
            public static final String ADSLIST = "adslist_table";
            public static final String MULTICASTMEDIALIB = "multicastmedialib_table";//已删除
            public static final String SPECIALTY = "specialty_table";//已删除
            public static final String MEDIALIB = "medialib_table";//已删除
            public static final String RTB_ADS = "rtb_ads_table";
            public static final String BIRTHDAY_ONDEMAND = "birthday_ondemand_table";
            public static final String INTERACTION_ADS = "interaction_ads_table";
            public static final String PROJECTION_LOG = "projection_log_table";
            public static final String ACTIVITY_ADS = "activity_ads_table";
            public static final String SHOP_GOODS_ADS = "goods_ads_table";
            public static final String SELECT_CONTENT = "select_content_table";
            public static final String MEDIA_ITEM = "media_item_table";
            public static final String WELCOME_RESOURCE = "welcome_resource_table";
            public static final String LOCAL_LIFE_ADS = "local_life_ads_table";
        }
    }

    /**
     * 数据库名称
     */
    public static final String DATABASE_NAME = "dbsavor.db";


    private static final int DB_VERSION = 40;

    private Context mContext;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);

        mContext = context;
        open();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogFileUtil.writeKeyLogInfo("-------Database onCreate-------");
        /**
         * 创建新一期的播放列表
         * */
        createTable_newplaylistTrace(db);
        /**
         * 创建正在播放的播放列表
         */
        createTable_playlistTrace(db);
        /**
         *创建新的一期广告内容表
         */
        createTable_newAdsListTrace(db);
        /**
         * 创建正在播放的广告内容表
         */
        createTable_adsListTrace(db);

        /**
         * 创建实时竞价广告表
         */
        createTable_rtbads(db);
        /**
         * 创建生日点播表
         */
        createTable_birthdayOndemand(db);
        /**
         *创建互动广告表
         */
        createTable_interactionAds(db);

        createTable_projectionLog(db);

        createTable_activityAds(db);

        createTable_selectContentAds(db);

        createTable_mediaItem(db);

        createTable_welcomeResource(db);

        createTable_shopgoodsAds(db);

        createTable_localLifeAds(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        LogFileUtil.writeKeyLogInfo("-------Database onUpgrade-------oldVersion=" + oldVersion + ", newVersion=" + newVersion);

        if (oldVersion<20){
            //20版本加入生日歌/星座点播视频
            try{
                createTable_birthdayOndemand(sqLiteDatabase);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<21){
            try {
                String alternewPlaylist = "ALTER TABLE " + TableName.NEWPLAYLIST + " ADD " + FieldName.RESOURCE_TYPE + " INTEGER;";
                sqLiteDatabase.execSQL(alternewPlaylist);
                String alterPlaylist = "ALTER TABLE " + TableName.PLAYLIST + " ADD " + FieldName.RESOURCE_TYPE + " INTEGER;";
                sqLiteDatabase.execSQL(alterPlaylist);

                String alterNewAdslist = "ALTER TABLE " + TableName.NEWADSLIST + " ADD " + FieldName.RESOURCE_TYPE + " INTEGER;";
                sqLiteDatabase.execSQL(alterNewAdslist);
                String alterAdslist = "ALTER TABLE " + TableName.ADSLIST + " ADD " + FieldName.RESOURCE_TYPE + " INTEGER;";
                sqLiteDatabase.execSQL(alterAdslist);
                createTable_interactionAds(sqLiteDatabase);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<22){
            //22版本加入投屏失败入库等待下次上传
            try {
                createTable_projectionLog(sqLiteDatabase);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<23){
            /**
             * 1.删除medialib_table和specialty_table废弃的两张表
             * 2.插入活动广告数据，根本播放时间加入节目单
             */
            try{
                String dropMediaLibTable = "DROP TABLE IF EXISTS " +TableName.MEDIALIB;
                sqLiteDatabase.execSQL(dropMediaLibTable);
                String dropSpecialtyTable = "DROP TABLE IF EXISTS "+TableName.SPECIALTY;
                sqLiteDatabase.execSQL(dropSpecialtyTable);

                createTable_activityAds(sqLiteDatabase);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<24){
            /**
             * 创建用户精选数据表
             */
            try{
                createTable_selectContentAds(sqLiteDatabase);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<25){
            try {
                String dropSelectContentTable = "DROP TABLE IF EXISTS " +TableName.SELECT_CONTENT;
                sqLiteDatabase.execSQL(dropSelectContentTable);
                createTable_selectContentAds(sqLiteDatabase);
                createTable_mediaItem(sqLiteDatabase);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<26){
            try{
                String alterAdslist = "ALTER TABLE " + TableName.PROJECTION_LOG + " ADD " + MEDIA_PATH + " TEXT;";
                sqLiteDatabase.execSQL(alterAdslist);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<27){
            try{
                String alterAdslist = "ALTER TABLE " + TableName.ACTIVITY_ADS + " ADD " + IS_STOREBUY + " INTEGER;";
                sqLiteDatabase.execSQL(alterAdslist);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<28){
            //删除点播表，已经废弃 20191125
            String dropSelectContentTable = "DROP TABLE IF EXISTS " +TableName.MULTICASTMEDIALIB;
            sqLiteDatabase.execSQL(dropSelectContentTable);
        }
        if (oldVersion<29){
            try{
                String alterSelectContent = "ALTER TABLE " + TableName.SELECT_CONTENT + " ADD " + TYPE + " INTEGER;";
                sqLiteDatabase.execSQL(alterSelectContent);
                String alterMediaItem = "ALTER TABLE " + TableName.MEDIA_ITEM + " ADD " + TYPE + " INTEGER;";
                sqLiteDatabase.execSQL(alterMediaItem);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<30){
            try{
                createTable_welcomeResource(sqLiteDatabase);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<31){
            try{
                String alterActivityAds = "ALTER TABLE " + TableName.ACTIVITY_ADS + " ADD " + TYPE + " INTEGER;";
                sqLiteDatabase.execSQL(alterActivityAds);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(oldVersion<33){
            try{
                createTable_shopgoodsAds(sqLiteDatabase);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(oldVersion<34){
            try{
                String alterAdslist = "ALTER TABLE " + TableName.PROJECTION_LOG + " ADD " + SERIAL_NUMBER + " TEXT;";
                sqLiteDatabase.execSQL(alterAdslist);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<35){
            try{
                String alterShortcut = "ALTER TABLE " + TableName.PROJECTION_LOG + " ADD " + MEDIA_SCREENSHOT_PATH + " TEXT;";
                sqLiteDatabase.execSQL(alterShortcut);
                String alterUpload = "ALTER TABLE " + TableName.PROJECTION_LOG + " ADD " + UPLOADED + " TEXT;";
                sqLiteDatabase.execSQL(alterUpload);
                String alterRepeat = "ALTER TABLE " + TableName.PROJECTION_LOG + " ADD " + REPEAT + " TEXT;";
                sqLiteDatabase.execSQL(alterRepeat);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<36){
            try{
                try{
                    createTable_localLifeAds(sqLiteDatabase);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<37){
            //versionName 2.2.8
            try{
                String addQrcodePath = "ALTER TABLE " + TableName.LOCAL_LIFE_ADS + " ADD " + QRCODE_PATH + " TEXT;";
                sqLiteDatabase.execSQL(addQrcodePath);
                String addQrcodeUrl = "ALTER TABLE " + TableName.LOCAL_LIFE_ADS + " ADD " + QRCODE_URL + " TEXT;";
                sqLiteDatabase.execSQL(addQrcodeUrl);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<38){
            //versionName 2.3.4
            try{
                String addPages = "ALTER TABLE " + TableName.PROJECTION_LOG + " ADD " + PAGES + " INTEGER;";
                sqLiteDatabase.execSQL(addPages);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<39){
            //versionName 2.3.7
            try{
                String addPages = "ALTER TABLE " + TableName.PROJECTION_LOG + " ADD " + IS_SHARE + " INTEGER;";
                sqLiteDatabase.execSQL(addPages);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (oldVersion<40){
            //versionName 2.4.5
            try{
                String alterNewPlaylist = "ALTER TABLE " + TableName.NEWPLAYLIST + " ADD " + NEW_RESOURCE + " INTEGER DEFAULT 0;";
                sqLiteDatabase.execSQL(alterNewPlaylist);
                String alterPlaylist = "ALTER TABLE " + TableName.PLAYLIST + " ADD " + NEW_RESOURCE + " INTEGER DEFAULT 0;";
                sqLiteDatabase.execSQL(alterPlaylist);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogFileUtil.writeKeyLogInfo("-------Database onUpgrade-------oldVersion=" + oldVersion + ", newVersion=" + newVersion);
    }

    private void createTable_newplaylistTrace(SQLiteDatabase db) {
        String DATABASE_CREATE = "create table "
                + TableName.NEWPLAYLIST
                + " (id INTEGER PRIMARY KEY, "
                + FieldName.VID + " TEXT, "
                + FieldName.PERIOD + " TEXT, "
                + FieldName.ADS_ORDER + " INTEGER, "
                + MEDIANAME + " TEXT, "
                + MEDIATYPE + " TEXT, "
                + RESOURCE_TYPE + " INTEGER, "
                + CHINESE_NAME + " TEXT, "
                + FieldName.CREATETIME + " TEXT, "
                + SURFIX + " TEXT, "
                + FieldName.DURATION + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + FieldName.LOCATION_ID + " TEXT, "
                + FieldName.IS_SAPP_QRCODE + " INTEGER, "
                + MD5 + " TEXT, "
                + NEW_RESOURCE + " INTEGER DEFAULT 0" + ");";
        db.execSQL(DATABASE_CREATE);
    }

    private void createTable_playlistTrace(SQLiteDatabase db) {
        String DATABASE_CREATE = "create table "
                + TableName.PLAYLIST
                + " (id INTEGER PRIMARY KEY, "
                + FieldName.VID + " TEXT, "
                + FieldName.PERIOD + " TEXT, "
                + FieldName.ADS_ORDER + " INTEGER, "
                + MEDIANAME + " TEXT, "
                + MEDIATYPE + " TEXT, "
                + RESOURCE_TYPE + " INTEGER, "
                + CHINESE_NAME + " TEXT, "
                + FieldName.CREATETIME + " TEXT, "
                + SURFIX + " TEXT, "
                + FieldName.DURATION + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + FieldName.LOCATION_ID + " TEXT, "
                + FieldName.IS_SAPP_QRCODE + " INTEGER, "
                + MD5 + " TEXT, "
                + NEW_RESOURCE + " INTEGER DEFAULT 0" + ");";

        db.execSQL(DATABASE_CREATE);
    }

    /**
     * 创建广告表
     *
     * @param db
     */
    private void createTable_newAdsListTrace(SQLiteDatabase db) {
        String DATABASE_CREATE = "create table "
                + TableName.NEWADSLIST
                + " (" + FieldName.ID + " INTEGER PRIMARY KEY, "
                + FieldName.VID + " TEXT, "
                + FieldName.LOCATION_ID + " TEXT, "
                + MEDIANAME + " TEXT, "
                + MEDIATYPE + " TEXT, "
                + RESOURCE_TYPE + " INTEGER, "
                + CHINESE_NAME + " TEXT, "
                + MD5 + " TEXT, "
                + FieldName.PERIOD + " TEXT, "
                + FieldName.ADS_ORDER + " INTEGER, "
                + FieldName.CREATETIME + " TEXT, "
                + SURFIX + " TEXT, "
                + FieldName.DURATION + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + FieldName.IS_SAPP_QRCODE + " INTEGER, "
                + FieldName.START_DATE + " TEXT, "
                + FieldName.END_DATE + " TEXT " + ");";
        db.execSQL(DATABASE_CREATE);
    }

    /**
     * 创建广告表
     *
     * @param db
     */
    private void createTable_adsListTrace(SQLiteDatabase db) {
        String DATABASE_CREATE = "create table "
                + TableName.ADSLIST
                + " (" + FieldName.ID + " INTEGER PRIMARY KEY, "
                + FieldName.VID + " TEXT, "
                + FieldName.LOCATION_ID + " TEXT, "
                + MEDIANAME + " TEXT, "
                + MEDIATYPE + " TEXT, "
                + RESOURCE_TYPE + " INTEGER, "
                + CHINESE_NAME + " TEXT, "
                + MD5 + " TEXT, "
                + FieldName.PERIOD + " TEXT, "
                + FieldName.ADS_ORDER + " INTEGER, "
                + FieldName.CREATETIME + " TEXT, "
                + SURFIX + " TEXT, "
                + FieldName.DURATION + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + FieldName.IS_SAPP_QRCODE + " INTEGER, "
                + FieldName.START_DATE + " TEXT, "
                + FieldName.END_DATE + " TEXT " + ");";
        db.execSQL(DATABASE_CREATE);
    }

    private void createTable_rtbads(SQLiteDatabase db) {
        String DATABASE_CREATE = "create table "
                + TableName.RTB_ADS
                + " (" + FieldName.ID + " INTEGER PRIMARY KEY, "
                + FieldName.VID + " TEXT, "
                + FieldName.LOCATION_ID + " TEXT, "
                + MEDIANAME + " TEXT, "
                + MEDIATYPE + " TEXT, "
                + CHINESE_NAME + " TEXT, "
                + MD5 + " TEXT, "
                + FieldName.PERIOD + " TEXT, "
                + FieldName.ADS_ORDER + " INTEGER, "
                + FieldName.CREATETIME + " TEXT, "
                + SURFIX + " TEXT, "
                + FieldName.DURATION + " TEXT, "
                + FieldName.ADMASTER_SIN + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + FieldName.IS_SAPP_QRCODE + " INTEGER, "
                + FieldName.TPMEDIA_ID + " TEXT, "
                + FieldName.TP_MD5 + " TEXT" + ");";
        db.execSQL(DATABASE_CREATE);
    }

    private void createTable_birthdayOndemand(SQLiteDatabase db){
        String DATABASE_CREATE = "create table "
                + TableName.BIRTHDAY_ONDEMAND
                + " (" + FieldName.ID + " INTEGER PRIMARY KEY, "
                + MEDIAID + " TEXT, "
                + MEDIANAME + " TEXT, "
                + CHINESE_NAME + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + MEDIATYPE + " TEXT, "
                + SURFIX + " TEXT, "
                + MD5 + " TEXT" +");";
        db.execSQL(DATABASE_CREATE);
    }

    private void createTable_interactionAds(SQLiteDatabase db){
        String DATABASE_CREATE = "create table "
                + TableName.INTERACTION_ADS
                + " (" + FieldName.ID + " INTEGER PRIMARY KEY, "
                + MEDIAID + " TEXT, "
                + MEDIANAME + " TEXT, "
                + CHINESE_NAME + " TEXT, "
                + MEDIATYPE + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + MD5 + " TEXT, "
                + PERIOD + " TEXT, "
                + DURATION + " TEXT, "
                + SURFIX + " TEXT, "
                + RESOURCE_TYPE + " INTEGER, "
                + PLAY_POSITION + " INTEGER, "
                + START_DATE + " TEXT, "
                + END_DATE + " TEXT" +");";
        db.execSQL(DATABASE_CREATE);
    }

    /**
     * 创建投屏日志表
     * @param db
     */
    private void createTable_projectionLog(SQLiteDatabase db){
        String DATABASE_CREATE = "create table "
                + TableName.PROJECTION_LOG
                + " (" + FieldName.ID + " INTEGER PRIMARY KEY, "
                + ACTION + " TEXT, "
                + SERIAL_NUMBER + " TEXT, "
                + BOX_MAC + " TEXT, "
                + DURATION + " TEXT, "
                + FORSCREEN_CHAR + " TEXT, "
                + FORSCREEN_ID + " TEXT, "
                + MOBILE_BRAND + " TEXT, "
                + MOBILE_MODEL + " TEXT, "
                + OPENID + " TEXT, "
                + RESOURCE_ID + " TEXT, "
                + RESOURCE_SIZE + " TEXT, "
                + RESOURCE_TYPE + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + MEDIA_SCREENSHOT_PATH + " TEXT, "
                + UPLOADED + " TEXT, "
                + REPEAT + " TEXT, "
                + SMALL_APP_ID + " TEXT, "
                + PAGES + " INTEGER, "
                + IS_SHARE + " INTEGER, "
                + CREATETIME + " TEXT "
                +");";
        db.execSQL(DATABASE_CREATE);
    }
    /**
     * 创建活动广告商品数据表
     */
    private void createTable_activityAds(SQLiteDatabase db){
        String DATABASE_CREATE = "create table "
                + TableName.ACTIVITY_ADS
                + " (" + FieldName.ID + " INTEGER PRIMARY KEY, "
                + GOODS_ID + " INTEGER, "
                + VID + " TEXT, "
                + MEDIANAME + " TEXT, "
                + CHINESE_NAME + " TEXT, "
                + RESOURCE_TYPE + " INTEGER, "
                + MD5 + " TEXT, "
                + DURATION + " TEXT, "
                + PRICE + " TEXT, "
                + TYPE + " INTEGER, "
                + PLAY_TYPE + " INTEGER, "
                + IS_STOREBUY + " INTEGER, "
                + MEDIA_PATH + " TEXT, "
                + QRCODE_PATH + " TEXT, "
                + QRCODE_URL + " TEXT, "
                + PERIOD + " TEXT, "
                + CREATETIME + " TEXT, "
                + START_DATE + " TEXT, "
                + END_DATE + " TEXT " + ");";
        db.execSQL(DATABASE_CREATE);

    }

    /**
     * 创建用户精选上大屏数据表
     * @param db
     */
    private void createTable_selectContentAds(SQLiteDatabase db){
        String DATABASE_CREATE = "create table "
                + TableName.SELECT_CONTENT
                + " (" + FieldName.ID + " INTEGER, "
                + DURATION + " TEXT, "
                + RESOURCE_TYPE + " INTEGER, "
                + PERIOD + " TEXT, "
                + TYPE + " INTEGER, "
                + NICK_NAME + " TEXT, "
                + AVATAR_URL + " TEXT, "
                + CREATETIME + " TEXT, "
                + START_DATE + " TEXT, "
                + END_DATE + " TEXT " + ");";
        db.execSQL(DATABASE_CREATE);

    }

    /**
     * 创建每个节目下的具体资源，含多图
     * @param db
     */
    private void createTable_mediaItem(SQLiteDatabase db){
        String DATABASE_CREATE = "create table "
                + TableName.MEDIA_ITEM
                + " (" + FieldName.ID + " INTEGER, "
                + VID + " TEXT, "
                + MEDIANAME + " TEXT, "
                + CHINESE_NAME + " TEXT, "
                + RESOURCE_TYPE + " INTEGER, "
                + TYPE + " INTEGER, "
                + MD5 + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + CREATETIME + " TEXT " + ");";
        db.execSQL(DATABASE_CREATE);
    }

    /**
     * 创建欢迎词相关资源表
     * @param db
     */
    private void createTable_welcomeResource(SQLiteDatabase db){
        String DATABASE_CREATE = "create table "
                + TableName.WELCOME_RESOURCE
                + " (" + FieldName.ID + " TEXT, "
                + CHINESE_NAME + " TEXT, "
                + MEDIANAME + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + MD5 + " TEXT, "
                + TYPE + " TEXT, "
                + MEDIATYPE + " TEXT, "
                + CREATETIME+ " TEXT "
                + ");";
        db.execSQL(DATABASE_CREATE);
    }

    private void createTable_shopgoodsAds(SQLiteDatabase db){
        String DATABASE_CREATE = "create table "
                + TableName.SHOP_GOODS_ADS
                + " (" + FieldName.ID + " INTEGER PRIMARY KEY, "
                + GOODS_ID + " INTEGER, "
                + VID + " TEXT, "
                + MEDIANAME + " TEXT, "
                + CHINESE_NAME + " TEXT, "
                + RESOURCE_TYPE + " INTEGER, "
                + MD5 + " TEXT, "
                + DURATION + " TEXT, "
                + TYPE + " INTEGER, "
                + LOCATION_ID + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + QRCODE_PATH + " TEXT, "
                + QRCODE_URL + " TEXT, "
                + PERIOD + " TEXT, "
                + CREATETIME + " TEXT " + ");";
        db.execSQL(DATABASE_CREATE);

    }

    /**
     * 创建本地生活广告数据表
     * @param db
     */
    private void createTable_localLifeAds(SQLiteDatabase db){
        String DATABASE_CREATE = "create table "
                + TableName.LOCAL_LIFE_ADS
                + " (" + FieldName.ID + " INTEGER PRIMARY KEY, "
                + VID + " TEXT, "
                + ADS_ID + " TEXT, "
                + MD5 + " TEXT, "
                + CHINESE_NAME + " TEXT, "
                + MEDIA_PATH + " TEXT, "
                + DURATION + " TEXT, "
                + SURFIX + " TEXT, "
                + START_DATE + " TEXT, "
                + END_DATE + " TEXT, "
                + RESOURCE_TYPE + " INTEGER, "
                + TYPE + " INTEGER, "
                + LOCATION_ID + " TEXT, "
                + MEDIANAME + " TEXT, "
                + QRCODE_PATH + " TEXT, "
                + QRCODE_URL + " TEXT, "
                + PERIOD + " TEXT, "
                + CREATETIME + " TEXT " + ");";
        db.execSQL(DATABASE_CREATE);
    }

    /**
     * 打开数据库
     *
     * @return
     * @throws SQLException
     */
    public SQLiteDatabase open() {
        if (db == null || !db.isOpen()) {
            db = getWritableDatabase();
            db.enableWriteAheadLogging();
        }
        return db;
    }

    public synchronized static DBHelper get(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    /**
     * 关闭数据库
     */
    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }


    /**
     * 向广告播放列表数据库中插入数据
     *
     * @param playList
     * @throws JSONException
     */
    public boolean insertOrUpdateNewPlayListLib(MediaLibBean playList, long id) {
        if (playList == null) {
            return false;
        }
        long in = 0;
        try {
//            open();
            ContentValues initialValues = new ContentValues();
            initialValues.put(VID, playList.getVid());
            initialValues.put(MEDIANAME, playList.getName());
            initialValues.put(MEDIATYPE, playList.getType());
            initialValues.put(RESOURCE_TYPE,playList.getMedia_type());
            initialValues.put(CHINESE_NAME, playList.getChinese_name());
            initialValues.put(SURFIX, playList.getSuffix());
            initialValues.put(CREATETIME, AppUtils.getCurTime("yyyyMMddHHmm"));
            initialValues.put(MD5, playList.getMd5());
            initialValues.put(PERIOD, playList.getPeriod());
            initialValues.put(ADS_ORDER, playList.getOrder());
            initialValues.put(DURATION, playList.getDuration());
            initialValues.put(LOCATION_ID, playList.getLocation_id());
            initialValues.put(MEDIA_PATH, playList.getMediaPath());
            initialValues.put(IS_SAPP_QRCODE,playList.getIs_sapp_qrcode());
            if (-1 != id) {
                String selection = ID + "=? ";
                String[] selectionArgs = new String[]{String.valueOf(id)};
                in = db.update(TableName.NEWPLAYLIST, initialValues, selection, selectionArgs);
            } else {
                in = db.insert(TableName.NEWPLAYLIST, null, initialValues);
            }

            if (in > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return false;
    }

    //查询播放列表本期数据是否存在
    public List<MediaLibBean> findNewPlayListByWhere(String selection, String[] selectionArgs) throws SQLException {

        Cursor cursor = null;
        List<MediaLibBean> playList = null;
        try {
//            open();
            cursor = db.query(TableName.NEWPLAYLIST, null,
                    selection, selectionArgs, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                playList = new ArrayList<>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    MediaLibBean bean = new MediaLibBean();
                    bean.setId(cursor.getInt(cursor.getColumnIndex(FieldName.ID)));
                    bean.setVid(cursor.getString(cursor.getColumnIndex(FieldName.VID)));
                    bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                    bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                    bean.setType(cursor.getString(cursor.getColumnIndex(MEDIATYPE)));
                    bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                    bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                    bean.setSuffix(cursor.getString(cursor.getColumnIndex(SURFIX)));
                    bean.setPeriod(cursor.getString(cursor.getColumnIndex(FieldName.PERIOD)));
                    bean.setOrder(cursor.getInt(cursor.getColumnIndex(FieldName.ADS_ORDER)));
                    bean.setIs_sapp_qrcode(cursor.getInt(cursor.getColumnIndex(FieldName.IS_SAPP_QRCODE)));
                    playList.add(bean);
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e2) {
                LogUtils.e(e2.toString());
            }

        }
        return playList;
    }

    /**
     * 更新下载资源到播放列表中
     * @param playList
     * @param id
     * @return
     */
    public boolean updatePlayListLib(MediaLibBean playList, long id) {
        if (playList == null) {
            return false;
        }
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(VID, playList.getVid());
            initialValues.put(PERIOD, playList.getPeriod());
            initialValues.put(ADS_ORDER, playList.getOrder());
            initialValues.put(MEDIANAME, playList.getName());
            initialValues.put(MEDIATYPE, playList.getType());
            initialValues.put(RESOURCE_TYPE,playList.getMedia_type());
            initialValues.put(CHINESE_NAME, playList.getChinese_name());
            initialValues.put(CREATETIME, AppUtils.getCurTime("yyyyMMddHHmm"));
            initialValues.put(SURFIX, playList.getSuffix());
            initialValues.put(DURATION, playList.getDuration());
            initialValues.put(MEDIA_PATH, playList.getMediaPath());
            initialValues.put(LOCATION_ID, playList.getLocation_id());
            initialValues.put(IS_SAPP_QRCODE,playList.getIs_sapp_qrcode());
            initialValues.put(MD5, playList.getMd5());
            initialValues.put(NEW_RESOURCE,playList.getNewResource());
            String selection = ID + "=? ";
            String[] selectionArgs = new String[]{String.valueOf(id)};
            long in = db.update(TableName.PLAYLIST, initialValues, selection, selectionArgs);

            if (in > 0) {
//                LogUtils.d("到达率debug-----数据库替换("+playList.getChinese_name()+")成功");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return false;
    }
    //查询播放列表本期数据是否存在
    public List<MediaLibBean> findPlayListByWhere(String selection, String[] selectionArgs) throws SQLException {

        String[] columns = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        Cursor cursor = null;
        List<MediaLibBean> playList = null;
        try {
//            open();
            cursor = db.query(TableName.PLAYLIST, columns,selection, selectionArgs, groupBy, having, orderBy, null);
            if (cursor != null && cursor.moveToFirst()) {
                playList = new ArrayList<>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    MediaLibBean bean = new MediaLibBean();
                    bean.setId(cursor.getInt(cursor.getColumnIndex(FieldName.ID)));
                    bean.setVid(cursor.getString(cursor.getColumnIndex(VID)));
                    bean.setPeriod(cursor.getString(cursor.getColumnIndex(PERIOD)));
                    bean.setOrder(cursor.getInt(cursor.getColumnIndex(ADS_ORDER)));
                    bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                    bean.setType(cursor.getString(cursor.getColumnIndex(MEDIATYPE)));
                    bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                    bean.setSuffix(cursor.getString(cursor.getColumnIndex(SURFIX)));
                    bean.setDuration(cursor.getString(cursor.getColumnIndex(DURATION)));
                    bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                    bean.setLocation_id(cursor.getString(cursor.getColumnIndex(LOCATION_ID)));
                    bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                    bean.setIs_sapp_qrcode(cursor.getInt(cursor.getColumnIndex(IS_SAPP_QRCODE)));
                    bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                    bean.setNewResource(cursor.getInt(cursor.getColumnIndex(NEW_RESOURCE)));
                    playList.add(bean);
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e2) {
                LogUtils.e(e2.toString());
            }

        }
        return playList;
    }

    public List<MediaLibBean> findAdsByWhere(String selection, String[] selectionArgs) throws SQLException {
        Cursor cursor = null;
        List<MediaLibBean> playList = null;
        try {
//            open();
            cursor = db.query(TableName.ADSLIST, null,
                    selection, selectionArgs, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                playList = new ArrayList<>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    MediaLibBean bean = new MediaLibBean();
                    bean.setVid(cursor.getString(cursor.getColumnIndex(FieldName.VID)));
                    bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                    bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                    bean.setType(cursor.getString(cursor.getColumnIndex(MEDIATYPE)));
                    bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                    bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                    bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                    bean.setSuffix(cursor.getString(cursor.getColumnIndex(SURFIX)));
                    bean.setPeriod(cursor.getString(cursor.getColumnIndex(FieldName.PERIOD)));
                    bean.setDuration(cursor.getString(cursor.getColumnIndex(FieldName.DURATION)));
                    bean.setOrder(cursor.getInt(cursor.getColumnIndex(FieldName.ADS_ORDER)));
                    bean.setLocation_id(cursor.getString(cursor.getColumnIndex(FieldName.LOCATION_ID)));
                    bean.setIs_sapp_qrcode(cursor.getInt(cursor.getColumnIndex(FieldName.IS_SAPP_QRCODE)));
                    bean.setStart_date(cursor.getString(cursor.getColumnIndex(FieldName.START_DATE)));
                    bean.setEnd_date(cursor.getString(cursor.getColumnIndex(FieldName.END_DATE)));
                    playList.add(bean);
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e2) {
                LogUtils.e(e2.toString());
            }

        }
        return playList;
    }

    public List<MediaLibBean> findNewAdsByWhere(String selection, String[] selectionArgs) throws SQLException {
        Cursor cursor = null;
        List<MediaLibBean> playList = null;
        try {
//            open();
            cursor = db.query(TableName.NEWADSLIST, null,
                    selection, selectionArgs, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                playList = new ArrayList<>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    MediaLibBean bean = new MediaLibBean();
                    bean.setVid(cursor.getString(cursor.getColumnIndex(FieldName.VID)));
                    bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                    bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                    bean.setType(cursor.getString(cursor.getColumnIndex(MEDIATYPE)));
                    bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                    bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                    bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                    bean.setSuffix(cursor.getString(cursor.getColumnIndex(SURFIX)));
                    bean.setPeriod(cursor.getString(cursor.getColumnIndex(FieldName.PERIOD)));
                    bean.setDuration(cursor.getString(cursor.getColumnIndex(FieldName.DURATION)));
                    bean.setOrder(cursor.getInt(cursor.getColumnIndex(FieldName.ADS_ORDER)));
                    bean.setLocation_id(cursor.getString(cursor.getColumnIndex(FieldName.LOCATION_ID)));
                    bean.setIs_sapp_qrcode(cursor.getInt(cursor.getColumnIndex(FieldName.IS_SAPP_QRCODE)));
                    bean.setStart_date(cursor.getString(cursor.getColumnIndex(FieldName.START_DATE)));
                    bean.setEnd_date(cursor.getString(cursor.getColumnIndex(FieldName.END_DATE)));
                    playList.add(bean);
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e2) {
                LogUtils.e(e2.toString());
            }

        }
        return playList;
    }

    /**
     * 广告数据单独入库
     *
     * @param playList
     * @param id
     * @return
     */
    public boolean insertOrUpdateNewAdsList(MediaLibBean playList, int id) {
        if (playList == null) {
            return false;
        }
        long in = 0;
        try {
//            open();
            ContentValues initialValues = new ContentValues();
            initialValues.put(FieldName.VID, playList.getVid());
            initialValues.put(FieldName.LOCATION_ID, playList.getLocation_id());
            initialValues.put(MEDIANAME, playList.getName());
            initialValues.put(CHINESE_NAME, playList.getChinese_name());
            initialValues.put(MEDIATYPE, playList.getType());
            initialValues.put(RESOURCE_TYPE,playList.getMedia_type());
            initialValues.put(MD5, playList.getMd5());
            initialValues.put(FieldName.PERIOD, playList.getPeriod());
            initialValues.put(FieldName.ADS_ORDER, playList.getOrder());
            initialValues.put(FieldName.CREATETIME, AppUtils.getCurTime("yyyyMMddHHmm"));
            initialValues.put(SURFIX, playList.getSuffix());
            initialValues.put(FieldName.DURATION, playList.getDuration());
            initialValues.put(MEDIA_PATH, playList.getMediaPath());
            initialValues.put(FieldName.IS_SAPP_QRCODE,playList.getIs_sapp_qrcode());
            initialValues.put(FieldName.START_DATE, playList.getStart_date());
            initialValues.put(FieldName.END_DATE, playList.getEnd_date());
            if (-1 != id) {
                String selection = FieldName.ID + "=? ";
                String[] selectionArgs = new String[]{String.valueOf(id)};
                in = db.update(TableName.NEWADSLIST, initialValues, selection, selectionArgs);
            } else {
                in = db.insert(TableName.NEWADSLIST, null, initialValues);
            }

            if (in > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return false;
    }

    /**
     * RTB广告数据单独入库
     *
     * @param playList
     * @param isUpdate
     * @return
     */
    public boolean insertOrUpdateRTBAdsList(MediaLibBean playList, boolean isUpdate) {
        if (playList == null) {
            return false;
        }
        long in = 0;
        try {
//            open();
            ContentValues initialValues = new ContentValues();
            initialValues.put(FieldName.VID, playList.getVid());
            initialValues.put(FieldName.LOCATION_ID, playList.getLocation_id());
            initialValues.put(MEDIANAME, playList.getName());
            initialValues.put(CHINESE_NAME, playList.getChinese_name());
            initialValues.put(MEDIATYPE, playList.getType());
            initialValues.put(MD5, playList.getMd5());
            initialValues.put(FieldName.PERIOD, playList.getPeriod());
            initialValues.put(FieldName.ADS_ORDER, playList.getOrder());
            initialValues.put(FieldName.CREATETIME, AppUtils.getCurTime("yyyyMMddHHmmss"));
            initialValues.put(SURFIX, playList.getSuffix());
            initialValues.put(FieldName.DURATION, playList.getDuration());
            initialValues.put(MEDIA_PATH, playList.getMediaPath());
            initialValues.put(FieldName.ADMASTER_SIN, playList.getAdmaster_sin());
            initialValues.put(FieldName.IS_SAPP_QRCODE,playList.getIs_sapp_qrcode());
            initialValues.put(FieldName.TPMEDIA_ID,playList.getTpmedia_id());
            initialValues.put(FieldName.TP_MD5,playList.getTp_md5());
            long successCount = 0;
            if (isUpdate) {
                successCount = db.update(TableName.RTB_ADS,
                        initialValues, FieldName.VID + "=? ",
                        new String[]{playList.getVid()});
            } else {
                successCount = db.insert(TableName.RTB_ADS,
                        null,
                        initialValues);
            }

            return successCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 按顺序查询播放表
     *
     * @return
     */
    public ArrayList<MediaLibBean> getOrderedPlayList() {
        ArrayList<MediaLibBean> playList = null;
        Cursor cursor = null;
        Session session = Session.get(mContext);
        if (!TextUtils.isEmpty(session.getProPeriod())
                && !TextUtils.isEmpty(session.getAdvPeriod())) {
            try {
                // 拼接查询条件
                String selection = null;
                String[] args = null;
                //            open();
                // 拼接查询条件
                selection = FieldName.PERIOD + "=? OR " + FieldName.PERIOD + "=?";
                args = new String[]{session.getProPeriod(), session.getAdvPeriod()};



                cursor = db.query(TableName.PLAYLIST, null, selection, args, null, null, FieldName.ADS_ORDER);
                if (cursor != null && cursor.moveToFirst()) {
                    playList = new ArrayList<>();
                    do {
                        MediaLibBean bean = new MediaLibBean();
                        bean.setVid(cursor.getString(cursor.getColumnIndex(FieldName.VID)));
                        bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                        bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                        bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                        bean.setType(cursor.getString(cursor.getColumnIndex(MEDIATYPE)));
                        bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                        bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                        bean.setSuffix(cursor.getString(cursor.getColumnIndex(SURFIX)));
                        bean.setPeriod(cursor.getString(cursor.getColumnIndex(FieldName.PERIOD)));
                        bean.setDuration(cursor.getString(cursor.getColumnIndex(FieldName.DURATION)));
                        bean.setOrder(cursor.getInt(cursor.getColumnIndex(FieldName.ADS_ORDER)));
                        bean.setLocation_id(cursor.getString(cursor.getColumnIndex(FieldName.LOCATION_ID)));
                        bean.setIs_sapp_qrcode(cursor.getInt(cursor.getColumnIndex(FieldName.IS_SAPP_QRCODE)));
                        bean.setNewResource(cursor.getInt(cursor.getColumnIndex(NEW_RESOURCE)));
                        playList.add(bean);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return playList;
    }

    /**
     * 查询临时节目列表
     *
     * @return
     */
    public ArrayList<MediaLibBean> getTempProList() {
        ArrayList<MediaLibBean> proList = null;
        Cursor cursor = null;
        Session session = Session.get(mContext);
        if (!TextUtils.isEmpty(session.getProPeriod())
                && !TextUtils.isEmpty(session.getAdvPeriod())) {
            try {
                // 拼接查询条件
                String selection =  FieldName.PERIOD + "!=? AND " + MEDIATYPE + "=?";
                String[] args = new String[]{session.getProPeriod(), ConstantValues.PRO};


                cursor = db.query(TableName.NEWPLAYLIST, null, selection, args, null, null, FieldName.CREATETIME);
                if (cursor != null && cursor.moveToFirst()) {
                    proList = new ArrayList<>();
                    do {
                        MediaLibBean bean = new MediaLibBean();
                        bean.setVid(cursor.getString(cursor.getColumnIndex(FieldName.VID)));
                        bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                        bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                        bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                        bean.setType(cursor.getString(cursor.getColumnIndex(MEDIATYPE)));
                        bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                        bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                        bean.setSuffix(cursor.getString(cursor.getColumnIndex(SURFIX)));
                        bean.setPeriod(cursor.getString(cursor.getColumnIndex(FieldName.PERIOD)));
                        bean.setDuration(cursor.getString(cursor.getColumnIndex(FieldName.DURATION)));
                        bean.setOrder(cursor.getInt(cursor.getColumnIndex(FieldName.ADS_ORDER)));
                        bean.setLocation_id(cursor.getString(cursor.getColumnIndex(FieldName.LOCATION_ID)));
                        bean.setIs_sapp_qrcode(cursor.getInt(cursor.getColumnIndex(FieldName.IS_SAPP_QRCODE)));
                        proList.add(bean);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return proList;
    }

    /**
     * 数据删除
     */
    public boolean deleteDataByWhere(String DBtable, String selection, String[] selectionArgs) {
        boolean flag = false;
        try {
//            open();
            flag = db.delete(DBtable, selection, selectionArgs) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

        return flag;
    }


    /**
     * 复制数据
     *
     * @param fromTable
     * @param toTable
     */
    public void copyTableMethod(String fromTable, String toTable) {
        try {
//            open();
            db.delete(toTable, "1=1", null);
            db.execSQL("insert into " + toTable + " select * from  " + fromTable);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 查询RTB广告表
     *
     * @param selection
     * @param selectionArgs
     * @return
     * @throws SQLException
     */
    public List<MediaLibBean> findRtbadsMediaLibByWhere(String selection, String[] selectionArgs) throws SQLException {
        List<MediaLibBean> list = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
//                open();
//                db.beginTransaction();
                cursor = db.query(TableName.RTB_ADS, null,
                        selection, selectionArgs, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        list = new ArrayList<>();
                        do {
                            MediaLibBean bean = new MediaLibBean();
                            bean.setVid(cursor.getString(cursor.getColumnIndex(FieldName.VID)));
                            bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                            bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                            bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                            bean.setType(cursor.getString(cursor.getColumnIndex(MEDIATYPE)));
                            bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                            bean.setSuffix(cursor.getString(cursor.getColumnIndex(SURFIX)));
                            bean.setPeriod(cursor.getString(cursor.getColumnIndex(FieldName.PERIOD)));
                            bean.setDuration(cursor.getString(cursor.getColumnIndex(FieldName.DURATION)));
                            bean.setOrder(cursor.getInt(cursor.getColumnIndex(FieldName.ADS_ORDER)));
                            bean.setLocation_id(cursor.getString(cursor.getColumnIndex(FieldName.LOCATION_ID)));
                            bean.setAdmaster_sin(cursor.getString(cursor.getColumnIndex(FieldName.ADMASTER_SIN)));
                            bean.setTpmedia_id(cursor.getString(cursor.getColumnIndex(FieldName.TPMEDIA_ID)));
                            bean.setTp_md5(cursor.getString(cursor.getColumnIndex(FieldName.TP_MD5)));
                            bean.setIs_sapp_qrcode(cursor.getInt(cursor.getColumnIndex(FieldName.IS_SAPP_QRCODE)));
                            bean.setCreateTime(cursor.getString(cursor.getColumnIndex(FieldName.CREATETIME)));
                            list.add(bean);
                        } while (cursor.moveToNext());
                    }
//                    db.setTransactionSuccessful();

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                        cursor = null;
                    }
//                    db.endTransaction();
//                    db.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * 插入生日点播数据
     * @param bean
     * @param isUpdate
     * @return
     */
    public boolean insertOrUpdateBirthdayOndemand(BirthdayOndemandBean bean, boolean isUpdate) {
        if (bean == null) {
            return false;
        }
        boolean flag = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(FieldName.MEDIAID, bean.getMedia_id());
            initialValues.put(FieldName.MEDIANAME, bean.getMedia_name());
            initialValues.put(FieldName.CHINESE_NAME, bean.getName());
            initialValues.put(FieldName.SURFIX, bean.getSurfix());
            initialValues.put(FieldName.MEDIA_PATH, bean.getMedia_path());
            initialValues.put(FieldName.MEDIATYPE, bean.getType());
            initialValues.put(FieldName.MD5, bean.getMd5());
            long success;
            if (isUpdate) {
                success = db.update(TableName.BIRTHDAY_ONDEMAND,
                        initialValues, FieldName.MEDIAID + "=? ",
                        new String[]{bean.getMedia_id()});
            } else {
                success = db.insert(TableName.BIRTHDAY_ONDEMAND,
                        null,
                        initialValues);
            }

            if (success > 0) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return flag;
    }

    /**
     * 查询生日点播数据
     * @param selection
     * @param selectionArgs
     * @return
     * @throws SQLException
     */
    public List<BirthdayOndemandBean> findBirthdayOndemandByWhere(String selection, String[] selectionArgs) throws SQLException {
        List<BirthdayOndemandBean> list = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.BIRTHDAY_ONDEMAND, null,
                        selection, selectionArgs, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        list = new ArrayList<>();
                        do {
                            BirthdayOndemandBean bean = new BirthdayOndemandBean();
                            bean.setMedia_id(cursor.getString(cursor.getColumnIndex(MEDIAID)));
                            bean.setMedia_name(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                            bean.setName(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                            bean.setSurfix(cursor.getString(cursor.getColumnIndex(SURFIX)));
                            bean.setMedia_path(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                            bean.setType(cursor.getInt(cursor.getColumnIndex(MEDIATYPE)));
                            bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                            list.add(bean);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * 插入投屏互动广告数据
     * @param bean
     * @return
     */
    public boolean insertInteractionAds(MediaLibBean bean) {
        if (bean == null) {
            return false;
        }
        boolean flag = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(FieldName.MEDIAID, bean.getVid());
            initialValues.put(MEDIANAME, bean.getName());
            initialValues.put(CHINESE_NAME, bean.getChinese_name());
            initialValues.put(MEDIATYPE, bean.getType());
            initialValues.put(MD5, bean.getMd5());
            initialValues.put(MEDIA_PATH, bean.getMediaPath());
            initialValues.put(FieldName.PERIOD, bean.getPeriod());
            initialValues.put(FieldName.DURATION, bean.getDuration());
            initialValues.put(FieldName.SURFIX, bean.getSuffix());
            initialValues.put(FieldName.RESOURCE_TYPE, bean.getMedia_type());
            initialValues.put(FieldName.PLAY_POSITION, bean.getPlay_position());
            initialValues.put(FieldName.START_DATE, bean.getStart_date());
            initialValues.put(FieldName.END_DATE, bean.getEnd_date());

            long success = db.insert(TableName.INTERACTION_ADS,null,initialValues);

            if (success > 0) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return flag;
    }

    /**
     * 查询互动投屏广告数据
     * @param selection
     * @param selectionArgs
     * @return
     * @throws SQLException
     */
    public List<MediaLibBean> findInteractionAdsByWhere(String selection, String[] selectionArgs)throws SQLException{
        List<MediaLibBean> list = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.INTERACTION_ADS, null,
                        selection, selectionArgs, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        list = new ArrayList<>();
                        do {
                            MediaLibBean bean = new MediaLibBean();
                            bean.setVid(cursor.getString(cursor.getColumnIndex(MEDIAID)));
                            bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                            bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                            bean.setType(cursor.getString(cursor.getColumnIndex(MEDIATYPE)));
                            bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                            bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                            bean.setDuration(cursor.getString(cursor.getColumnIndex(DURATION)));
                            bean.setSuffix(cursor.getString(cursor.getColumnIndex(SURFIX)));
                            bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                            bean.setPlay_position(cursor.getInt(cursor.getColumnIndex(PLAY_POSITION)));
                            bean.setStart_date(cursor.getString(cursor.getColumnIndex(START_DATE)));
                            bean.setEnd_date(cursor.getString(cursor.getColumnIndex(END_DATE)));
                            list.add(bean);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * 插入投屏日志数据
     * @param bean
     * @return
     */
    public boolean insertProjectionLog(ProjectionLogBean bean){
        boolean flag = false;
        if (bean==null){
            return false;
        }
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(ACTION,bean.getAction());
            initialValues.put(SERIAL_NUMBER,bean.getSerial_number());
            initialValues.put(BOX_MAC,bean.getBox_mac());
            initialValues.put(DURATION,bean.getDuration());
            initialValues.put(FORSCREEN_CHAR,bean.getForscreen_char());
            initialValues.put(FORSCREEN_ID,bean.getForscreen_id());
            initialValues.put(MOBILE_BRAND,bean.getMobile_brand());
            initialValues.put(MOBILE_MODEL,bean.getMobile_model());
            initialValues.put(OPENID,bean.getOpenid());
            initialValues.put(RESOURCE_ID,bean.getResource_id());
            initialValues.put(RESOURCE_SIZE,bean.getResource_size());
            initialValues.put(RESOURCE_TYPE,bean.getResource_type());
            initialValues.put(MEDIA_PATH,bean.getMedia_path());
            initialValues.put(MEDIA_SCREENSHOT_PATH,bean.getMedia_screenshot_path());
            initialValues.put(UPLOADED,bean.getUpload());
            initialValues.put(REPEAT,bean.getRepeat());
            initialValues.put(PAGES,bean.getPages());
            initialValues.put(IS_SHARE,bean.getIs_share());
            initialValues.put(SMALL_APP_ID,bean.getSmall_app_id());
            initialValues.put(CREATETIME,bean.getCreate_time());
            long success = db.insert(TableName.PROJECTION_LOG,null,initialValues);
            LogUtils.d("数据插入，成功，forscreenId=" + bean.getForscreen_id());
            if (success>0){
                flag = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return flag;
    }

    public boolean uploadProjectionLog(String resource_id,String createTime,String upload){
        boolean flag = false;
        try {
            String selection = RESOURCE_ID + "=? and "
                             + CREATETIME + "=? ";
            String[] selectionArgs = new String[]{resource_id,createTime};
            ContentValues initialValues = new ContentValues();
            initialValues.put(FieldName.UPLOADED, upload);
            long success = db.update(TableName.PROJECTION_LOG,initialValues, selection,selectionArgs);
            if (success > 0) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return flag;
    }
    /**
     * 条件查询投屏日志数据
     * @param selection
     * @param selectionArgs
     * @return
     */
    public List<ProjectionLogBean> findProjectionLogs(String selection,String[] selectionArgs){
        List<ProjectionLogBean> logBeanList = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.PROJECTION_LOG, null,
                        selection, selectionArgs, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        logBeanList = new ArrayList<>();
                        do {
                            ProjectionLogBean bean = new ProjectionLogBean();
                            bean.setAction(cursor.getString(cursor.getColumnIndex(ACTION)));
                            bean.setSerial_number(cursor.getString(cursor.getColumnIndex(SERIAL_NUMBER)));
                            bean.setBox_mac(cursor.getString(cursor.getColumnIndex(BOX_MAC)));
                            bean.setDuration(cursor.getString(cursor.getColumnIndex(DURATION)));
                            bean.setForscreen_char(cursor.getString(cursor.getColumnIndex(FORSCREEN_CHAR)));
                            bean.setForscreen_id(cursor.getString(cursor.getColumnIndex(FORSCREEN_ID)));
                            bean.setMobile_brand(cursor.getString(cursor.getColumnIndex(MOBILE_BRAND)));
                            bean.setMobile_model(cursor.getString(cursor.getColumnIndex(MOBILE_MODEL)));
                            bean.setOpenid(cursor.getString(cursor.getColumnIndex(OPENID)));
                            bean.setResource_id(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                            bean.setResource_size(cursor.getString(cursor.getColumnIndex(RESOURCE_SIZE)));
                            bean.setResource_type(cursor.getString(cursor.getColumnIndex(RESOURCE_TYPE)));
                            bean.setMedia_path(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                            bean.setMedia_screenshot_path(cursor.getString(cursor.getColumnIndex(MEDIA_SCREENSHOT_PATH)));
                            bean.setPages(cursor.getInt(cursor.getColumnIndex(PAGES)));
                            bean.setIs_share(cursor.getInt(cursor.getColumnIndex(IS_SHARE)));
                            bean.setSmall_app_id(cursor.getString(cursor.getColumnIndex(SMALL_APP_ID)));
                            bean.setCreate_time(cursor.getString(cursor.getColumnIndex(CREATETIME)));
                            logBeanList.add(bean);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return logBeanList;
    }

    /**
     * 查询投屏历史数据，一次投屏行为是一条数据,比如说一次投多图，算作一条数据记录
     * @param selection
     * @param selectionArgs
     * @return
     */
    public List<ProjectionLogHistory> findProjectionHistory(String selection, String[] selectionArgs){
        List<ProjectionLogHistory> historyList = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.PROJECTION_LOG, null,
                        selection, selectionArgs, FORSCREEN_ID, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        historyList = new ArrayList<>();
                        do {
                            ProjectionLogHistory history = new ProjectionLogHistory();
                            history.setAction(cursor.getString(cursor.getColumnIndex(ACTION)));
                            history.setSerial_number(cursor.getString(cursor.getColumnIndex(SERIAL_NUMBER)));
                            history.setBox_mac(cursor.getString(cursor.getColumnIndex(BOX_MAC)));
                            history.setForscreen_char(cursor.getString(cursor.getColumnIndex(FORSCREEN_CHAR)));
                            history.setForscreen_id(cursor.getString(cursor.getColumnIndex(FORSCREEN_ID)));
                            history.setMobile_brand(cursor.getString(cursor.getColumnIndex(MOBILE_BRAND)));
                            history.setMobile_model(cursor.getString(cursor.getColumnIndex(MOBILE_MODEL)));
                            history.setOpenid(cursor.getString(cursor.getColumnIndex(OPENID)));
                            history.setResource_type(cursor.getString(cursor.getColumnIndex(RESOURCE_TYPE)));
//                            selection = FORSCREEN_ID + "=? AND "+REPEAT +" = ? ";
                            selection = FORSCREEN_ID + "=? ";
                            selectionArgs = new String[]{history.getForscreen_id()};
                            List<ProjectionLogDetail> listDetail = findProjectionDetail(selection,selectionArgs);
                            if (listDetail!=null&&listDetail.size()>0){
                                history.setList(listDetail);
                            }
                            historyList.add(history);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return historyList;
    }

    public List<ProjectionLogDetail> findProjectionDetail(String selection,String[] selectionArgs){
        List<ProjectionLogDetail> logDetailList = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.PROJECTION_LOG, null,
                        selection, selectionArgs, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        logDetailList = new ArrayList<>();
                        do {
                            ProjectionLogDetail bean = new ProjectionLogDetail();
                            bean.setDuration(cursor.getString(cursor.getColumnIndex(DURATION)));
                            bean.setResource_id(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                            bean.setResource_size(cursor.getString(cursor.getColumnIndex(RESOURCE_SIZE)));
                            bean.setMedia_path(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                            bean.setMedia_screenshot_path(cursor.getString(cursor.getColumnIndex(MEDIA_SCREENSHOT_PATH)));
                            bean.setSmall_app_id(cursor.getString(cursor.getColumnIndex(SMALL_APP_ID)));
                            bean.setCreate_time(cursor.getString(cursor.getColumnIndex(CREATETIME)));
                            logDetailList.add(bean);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return logDetailList;
    }

    public ProjectionLogHistory findProjectionHistoryById(String selection, String[] selectionArgs){
        ProjectionLogHistory history = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.PROJECTION_LOG, null,
                        selection, selectionArgs, FORSCREEN_ID, null, null, null);
                if (cursor != null&&cursor.moveToFirst()) {
                    history = new ProjectionLogHistory();
                    history.setAction(cursor.getString(cursor.getColumnIndex(ACTION)));
                    history.setSerial_number(cursor.getString(cursor.getColumnIndex(SERIAL_NUMBER)));
                    history.setBox_mac(cursor.getString(cursor.getColumnIndex(BOX_MAC)));
                    history.setForscreen_char(cursor.getString(cursor.getColumnIndex(FORSCREEN_CHAR)));
                    history.setForscreen_id(cursor.getString(cursor.getColumnIndex(FORSCREEN_ID)));
                    history.setMobile_brand(cursor.getString(cursor.getColumnIndex(MOBILE_BRAND)));
                    history.setMobile_model(cursor.getString(cursor.getColumnIndex(MOBILE_MODEL)));
                    history.setOpenid(cursor.getString(cursor.getColumnIndex(OPENID)));
                    history.setResource_type(cursor.getString(cursor.getColumnIndex(RESOURCE_TYPE)));
                    history.setResource_id(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                    history.setPages(cursor.getInt(cursor.getColumnIndex(PAGES)));
                    history.setResource_size(cursor.getString(cursor.getColumnIndex(RESOURCE_SIZE)));
                    selection = FORSCREEN_ID + "=? ";
                    selectionArgs = new String[]{history.getForscreen_id()};
                    List<ProjectionLogDetail> listDetail = findProjectionDetail(selection,selectionArgs);
                    if (listDetail!=null&&listDetail.size()>0){
                        history.setList(listDetail);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return history;
    }

    /**
     * 接受到活动商品广告入库
     * @param bean
     * @return
     */
    public boolean insertActivityAds(ActivityGoodsBean bean){
        if (bean == null) {
            return false;
        }
        boolean flag = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(GOODS_ID, bean.getGoods_id());
            initialValues.put(VID, bean.getGoods_id());
            initialValues.put(MEDIANAME, bean.getName());
            initialValues.put(CHINESE_NAME, bean.getChinese_name());
            initialValues.put(RESOURCE_TYPE, bean.getMedia_type());
            initialValues.put(MD5, bean.getMd5());
            initialValues.put(DURATION, bean.getDuration());
            initialValues.put(PRICE,bean.getPrice());
            initialValues.put(TYPE,bean.getType());
            initialValues.put(PLAY_TYPE,bean.getPlay_type());
            initialValues.put(IS_STOREBUY,bean.getIs_storebuy());
            initialValues.put(MEDIA_PATH, bean.getMediaPath());
            initialValues.put(QRCODE_PATH, bean.getQrcode_path());
            initialValues.put(QRCODE_URL, bean.getQrcode_url());
            initialValues.put(PERIOD,bean.getPeriod());
            initialValues.put(START_DATE, bean.getStart_date());
            initialValues.put(END_DATE, bean.getEnd_date());
            initialValues.put(CREATETIME, bean.getCreateTime());

            long success = db.insert(TableName.ACTIVITY_ADS,null,initialValues);

            if (success > 0) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return flag;

    }

    /**
     * 条件查询活动广告
     * @param selection
     * @param selectionArgs
     * @return
     */
    public List<MediaLibBean> findActivityAdsByWhere(String selection, String[] selectionArgs){
        List<MediaLibBean> list = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.ACTIVITY_ADS, null,
                        selection, selectionArgs, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        list = new ArrayList<>();
                        do {
                            MediaLibBean bean = new MediaLibBean();
                            bean.setGoods_id(cursor.getInt(cursor.getColumnIndex(GOODS_ID)));
                            bean.setVid(cursor.getString(cursor.getColumnIndex(VID)));
                            bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                            bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                            bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                            bean.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                            bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                            bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                            bean.setDuration(cursor.getString(cursor.getColumnIndex(DURATION)));
                            bean.setPrice(cursor.getString(cursor.getColumnIndex(PRICE)));
                            bean.setPlay_type(cursor.getInt(cursor.getColumnIndex(PLAY_TYPE)));
                            bean.setIs_storebuy(cursor.getInt(cursor.getColumnIndex(IS_STOREBUY)));
                            bean.setQrcode_path(cursor.getString(cursor.getColumnIndex(QRCODE_PATH)));
                            bean.setQrcode_url(cursor.getString(cursor.getColumnIndex(QRCODE_URL)));
                            bean.setPeriod(cursor.getString(cursor.getColumnIndex(PERIOD)));
                            bean.setStart_date(cursor.getString(cursor.getColumnIndex(START_DATE)));
                            bean.setEnd_date(cursor.getString(cursor.getColumnIndex(END_DATE)));
                            bean.setCreateTime(cursor.getString(cursor.getColumnIndex(CREATETIME)));
                            list.add(bean);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * 插入用户精选数据
     * @param bean
     * @return
     */
    public boolean insertSelectContent(SelectContentBean bean){
        if (bean == null) {
            return false;
        }
        boolean flag = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(ID, bean.getId());
            initialValues.put(RESOURCE_TYPE, bean.getMedia_type());
            initialValues.put(DURATION, bean.getDuration());
            initialValues.put(PERIOD,bean.getPeriod());
            initialValues.put(NICK_NAME,bean.getNickName());
            initialValues.put(AVATAR_URL,bean.getAvatarUrl());
            initialValues.put(START_DATE, bean.getStart_date());
            initialValues.put(END_DATE, bean.getEnd_date());
            initialValues.put(CREATETIME, bean.getCreateTime());
            initialValues.put(TYPE,bean.getType());

            long success = db.insert(TableName.SELECT_CONTENT,null,initialValues);

            if (success > 0) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return flag;
    }

    public boolean insertMediaItem(MediaItemBean item){
        if (item == null) {
            return false;
        }
        boolean flag = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(ID, item.getId());
            initialValues.put(VID, item.getVid());
            initialValues.put(MEDIANAME, item.getName());
            initialValues.put(CHINESE_NAME,item.getChinese_name());
            initialValues.put(RESOURCE_TYPE,item.getMedia_type());
            initialValues.put(TYPE,item.getType());
            initialValues.put(MD5,item.getMd5());
            initialValues.put(MEDIA_PATH,item.getOss_path());
            initialValues.put(CREATETIME,item.getCreateTime());
            long success = db.insert(TableName.MEDIA_ITEM,null,initialValues);
            if (success>0){
                flag = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 条件查询用户精选内容
     * @return
     */
    public List<SelectContentBean> findHotPlayContentList(String selection, String[] selectionArgs){
        List<SelectContentBean> list = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.SELECT_CONTENT, null,
                        selection, selectionArgs, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        list = new ArrayList<>();
                        do {
                            SelectContentBean bean = new SelectContentBean();
                            bean.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                            bean.setDuration(cursor.getInt(cursor.getColumnIndex(DURATION)));
                            bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                            bean.setPeriod(cursor.getString(cursor.getColumnIndex(PERIOD)));
                            bean.setNickName(cursor.getString(cursor.getColumnIndex(NICK_NAME)));
                            bean.setAvatarUrl(cursor.getString(cursor.getColumnIndex(AVATAR_URL)));
                            bean.setCreateTime(cursor.getString(cursor.getColumnIndex(CREATETIME)));
                            bean.setStart_date(cursor.getString(cursor.getColumnIndex(START_DATE)));
                            bean.setEnd_date(cursor.getString(cursor.getColumnIndex(END_DATE)));
                            bean.setType(cursor.getInt(cursor.getColumnIndex(TYPE)));
                            String selectionSub = ID + "=? ";
                            String[] selectionArgsSub = new String[]{String.valueOf(bean.getId())};
                            List<MediaItemBean> item = findMediaItemList(selectionSub,selectionArgsSub);
                            bean.setSubdata(item);
                            list.add(bean);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public List<MediaItemBean> findMediaItemList(String selection, String[] selectionArgs){
        List<MediaItemBean> list = null;
        Cursor cursor = null;
        try {
            cursor = db.query(TableName.MEDIA_ITEM, null,
                    selection, selectionArgs, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    list = new ArrayList<>();
                    do {
                        MediaItemBean bean = new MediaItemBean();
                        bean.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                        bean.setVid(cursor.getString(cursor.getColumnIndex(VID)));
                        bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                        bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                        bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                        bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                        bean.setOss_path(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                        bean.setCreateTime(cursor.getString(cursor.getColumnIndex(CREATETIME)));
                        bean.setType(cursor.getInt(cursor.getColumnIndex(TYPE)));
                        list.add(bean);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return list;
    }


    public boolean insertWelcomeResource(WelcomeResourceBean bean){
        if (bean == null) {
            return false;
        }
        boolean flag = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(ID, bean.getId());
            initialValues.put(CHINESE_NAME, bean.getChinese_name());
            initialValues.put(MEDIANAME,bean.getName());
            initialValues.put(MEDIA_PATH,bean.getMedia_path());
            initialValues.put(MD5,bean.getMd5());
            initialValues.put(TYPE,bean.getType());
            initialValues.put(MEDIATYPE,bean.getMedia_type());
            initialValues.put(CREATETIME,bean.getCreateTime());
            long success = db.insert(TableName.WELCOME_RESOURCE,null,initialValues);
            if (success>0){
                flag = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    public List<WelcomeResourceBean> findWelcomeResourceList(String selection, String[] selectionArgs){
        Cursor cursor = null;
        List<WelcomeResourceBean> list = null;
        try {
            cursor = db.query(TableName.WELCOME_RESOURCE, null,
                    selection, selectionArgs, null, null, null, null);
            if (cursor != null&&cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    WelcomeResourceBean bean = new WelcomeResourceBean();
                    bean.setId(cursor.getString(cursor.getColumnIndex(ID)));
                    bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                    bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                    bean.setMedia_path(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                    bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                    bean.setType(cursor.getInt(cursor.getColumnIndex(TYPE)));
                    bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(MEDIATYPE)));
                    bean.setCreateTime(cursor.getString(cursor.getColumnIndex(CREATETIME)));
                    list.add(bean);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public boolean insertShopGoodsAds(ShopGoodsBean bean){
        if (bean == null) {
            return false;
        }
        boolean flag = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(GOODS_ID, bean.getGoods_id());
            initialValues.put(VID, bean.getGoods_id());
            initialValues.put(MEDIANAME,bean.getName());
            initialValues.put(CHINESE_NAME, bean.getChinese_name());
            initialValues.put(RESOURCE_TYPE,bean.getMedia_type());
            initialValues.put(MD5,bean.getMd5());
            initialValues.put(DURATION,bean.getDuration());
            initialValues.put(TYPE,bean.getType());
            initialValues.put(LOCATION_ID,bean.getLocation_id());
            initialValues.put(MEDIA_PATH,bean.getMediaPath());
            initialValues.put(QRCODE_PATH,bean.getQrcode_path());
            initialValues.put(QRCODE_URL,bean.getQrcode_url());
            initialValues.put(PERIOD,bean.getPeriod());
            initialValues.put(CREATETIME,bean.getCreateTime());
            long success = db.insert(TableName.SHOP_GOODS_ADS,null,initialValues);
            if (success>0){
                flag = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    public List<ShopGoodsBean> findShopGoodsAds(String selection, String[] selectionArgs){
        List<ShopGoodsBean> list = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.SHOP_GOODS_ADS, null,
                        selection, selectionArgs, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        list = new ArrayList<>();
                        do {
                            ShopGoodsBean bean = new ShopGoodsBean();
                            bean.setGoods_id(cursor.getInt(cursor.getColumnIndex(GOODS_ID)));
                            bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                            bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                            bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                            bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                            bean.setDuration(cursor.getString(cursor.getColumnIndex(DURATION)));
                            bean.setType(cursor.getInt(cursor.getColumnIndex(TYPE)));
                            bean.setLocation_id(cursor.getString(cursor.getColumnIndex(LOCATION_ID)));
                            bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                            bean.setQrcode_path(cursor.getString(cursor.getColumnIndex(QRCODE_PATH)));
                            bean.setQrcode_url(cursor.getString(cursor.getColumnIndex(QRCODE_URL)));
                            bean.setPeriod(cursor.getString(cursor.getColumnIndex(PERIOD)));
                            bean.setCreateTime(cursor.getString(cursor.getColumnIndex(CREATETIME)));
                            list.add(bean);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public List<MediaLibBean> findShopGoodsAdsByWhere(String selection, String[] selectionArgs){
        List<MediaLibBean> list = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.SHOP_GOODS_ADS, null,
                        selection, selectionArgs, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        list = new ArrayList<>();
                        do {
                            MediaLibBean bean = new MediaLibBean();
                            bean.setGoods_id(cursor.getInt(cursor.getColumnIndex(GOODS_ID)));
                            bean.setVid(cursor.getString(cursor.getColumnIndex(VID)));
                            bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                            bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                            bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                            bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                            bean.setDuration(cursor.getString(cursor.getColumnIndex(DURATION)));
                            bean.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                            bean.setLocation_id(cursor.getString(cursor.getColumnIndex(LOCATION_ID)));
                            bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                            bean.setQrcode_path(cursor.getString(cursor.getColumnIndex(QRCODE_PATH)));
                            bean.setQrcode_url(cursor.getString(cursor.getColumnIndex(QRCODE_URL)));
                            bean.setPeriod(cursor.getString(cursor.getColumnIndex(PERIOD)));
                            bean.setCreateTime(cursor.getString(cursor.getColumnIndex(CREATETIME)));
                            list.add(bean);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public boolean insertLocalLifeAds(MediaLibBean bean){
        if (bean == null) {
            return false;
        }
        boolean flag = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(VID, bean.getVid());
            initialValues.put(ADS_ID, bean.getAds_id());
            initialValues.put(MD5,bean.getMd5());
            initialValues.put(CHINESE_NAME, bean.getChinese_name());
            initialValues.put(MEDIA_PATH,bean.getMediaPath());
            initialValues.put(DURATION,bean.getDuration());
            initialValues.put(SURFIX,bean.getSuffix());
            initialValues.put(START_DATE,bean.getStart_date());
            initialValues.put(END_DATE,bean.getEnd_date());
            initialValues.put(RESOURCE_TYPE,bean.getMedia_type());
            initialValues.put(TYPE,bean.getType());
            initialValues.put(MEDIANAME,bean.getName());
            initialValues.put(QRCODE_PATH,bean.getQrcode_path());
            initialValues.put(QRCODE_URL,bean.getQrcode_url());
            initialValues.put(PERIOD,bean.getPeriod());
            initialValues.put(CREATETIME,bean.getCreateTime());
            long success = db.insert(TableName.LOCAL_LIFE_ADS,null,initialValues);
            if (success>0){
                flag = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    public List<MediaLibBean> findLocalLifeAdsByWhere(String selection, String[] selectionArgs){
        List<MediaLibBean> list = null;
        synchronized (dbHelper) {
            Cursor cursor = null;
            try {
                cursor = db.query(TableName.LOCAL_LIFE_ADS, null,
                        selection, selectionArgs, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        list = new ArrayList<>();
                        do {
                            MediaLibBean bean = new MediaLibBean();
                            bean.setVid(cursor.getString(cursor.getColumnIndex(VID)));
                            bean.setAds_id(cursor.getString(cursor.getColumnIndex(ADS_ID)));
                            bean.setMd5(cursor.getString(cursor.getColumnIndex(MD5)));
                            bean.setChinese_name(cursor.getString(cursor.getColumnIndex(CHINESE_NAME)));
                            bean.setMediaPath(cursor.getString(cursor.getColumnIndex(MEDIA_PATH)));
                            bean.setDuration(cursor.getString(cursor.getColumnIndex(DURATION)));
                            bean.setSuffix(cursor.getString(cursor.getColumnIndex(SURFIX)));
                            bean.setStart_date(cursor.getString(cursor.getColumnIndex(START_DATE)));
                            bean.setEnd_date(cursor.getString(cursor.getColumnIndex(END_DATE)));
                            bean.setMedia_type(cursor.getInt(cursor.getColumnIndex(RESOURCE_TYPE)));
                            bean.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                            bean.setName(cursor.getString(cursor.getColumnIndex(MEDIANAME)));
                            bean.setQrcode_path(cursor.getString(cursor.getColumnIndex(QRCODE_PATH)));
                            bean.setQrcode_url(cursor.getString(cursor.getColumnIndex(QRCODE_URL)));
                            bean.setPeriod(cursor.getString(cursor.getColumnIndex(PERIOD)));
                            bean.setCreateTime(cursor.getString(cursor.getColumnIndex(CREATETIME)));
                            list.add(bean);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }
}

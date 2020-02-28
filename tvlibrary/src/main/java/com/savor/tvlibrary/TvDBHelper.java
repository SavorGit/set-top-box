package com.savor.tvlibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTransactionListener;
import android.media.tv.TvContract;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by zhang.haiqiang on 2017/10/31.
 */

public class TvDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "TvDBHelper";

    /**
     * 数据库名称
     */
    public static final String DB_NAME = "tv.db";
    private static final int DB_VERSION = 1;

    private static TvDBHelper dbHelper = null;
    private Context mContext;

    public static class FieldName {

        public static final String ID = "_id";
        public static final String CHANNEL_ID = "channel_id";
        public static final String CHANNEL_NAME = "channel_name";
    }

    public static class TableName {
        public static final String ATV_CHANNEL = "atv_channel_table";
    }


    private TvDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        mContext = context;
    }


    public synchronized static TvDBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new TvDBHelper(context);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // 创建电视频道表
        createTable_atvChannel(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    private void createTable_atvChannel(SQLiteDatabase db) {
        String DATABASE_CREATE = "create table "
                + TableName.ATV_CHANNEL
                + " ("
                + FieldName.ID + " INTEGER PRIMARY KEY, "
                + FieldName.CHANNEL_ID + " INTEGER, "
                + FieldName.CHANNEL_NAME + " TEXT "
                + ");";
        db.execSQL(DATABASE_CREATE);
    }

    ArrayList<AtvChannel> getAtvChannels() {
        ArrayList<AtvChannel> channels = null;
        Cursor cursor = null;
        SQLiteDatabase db = getReadableDatabase();
        try {
            cursor = db.query(TableName.ATV_CHANNEL, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                channels = new ArrayList<>();
                do {
                    AtvChannel bean = new AtvChannel();
                    bean.setChannelName(cursor.getString(cursor.getColumnIndex(FieldName.CHANNEL_NAME)));
                    bean.setId(cursor.getInt(cursor.getColumnIndex(FieldName.CHANNEL_ID)));

                    channels.add(bean);
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
        db.close();

        return channels;
    }

    ArrayList<AtvChannel> getSysChannels() {
        ArrayList<AtvChannel> channels = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase db = getReadableDatabase();
        try {
            cursor = mContext.getContentResolver().query(TvContract.Channels.CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int i = 0;
                do {
                    AtvChannel bean = new AtvChannel();

                    bean.setId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels._ID)));
                    bean.setInputId(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_INPUT_ID)));
                    bean.setDisplayNumber(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER)));
                    bean.setDisplayName(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)));
                    bean.setType(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_TYPE)));
                    bean.setIsBrowsable(cursor.getInt(cursor.getColumnIndex("browsable")));
                    bean.setServiceType(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_TYPE)));
                    bean.setServiceId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_ID)));
                    bean.setProviderData(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA)));

                    bean.setChannelName(bean.getDisplayName());
                    bean.setChannelNum(++i);
                    channels.add(bean);
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
        db.close();

        return channels;
    }

    void cleanChannelDb() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TableName.ATV_CHANNEL, null, null);
        db.close();
    }

    void mappingChannelFromSysDb() {
        Cursor cursor = mContext.getContentResolver().query(TvContract.Channels.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                try {
                    SQLiteDatabase db = getWritableDatabase();
                    db.beginTransaction();

                    db.delete(TableName.ATV_CHANNEL, null, null);

                    int i = 0;
                    do {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(FieldName.CHANNEL_ID, cursor.getInt(cursor.getColumnIndex(TvContract.Channels._ID)));
                        contentValues.put(FieldName.CHANNEL_NAME, cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)));

                        db.insert(TableName.ATV_CHANNEL, null, contentValues);
                    } while (cursor.moveToNext());

                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
    }

    void updateSysDb(ArrayList<AtvChannel> newChannels) {
        if (newChannels != null) {
            mContext.getContentResolver().delete(TvContract.Channels.CONTENT_URI, null, null);
            for (AtvChannel channel :
                    newChannels) {
                ContentValues values = new ContentValues();
                values.put(TvContract.Channels.COLUMN_INPUT_ID, channel.getInputId());
                values.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, channel.getDisplayNumber());
                values.put(TvContract.Channels.COLUMN_DISPLAY_NAME, channel.getDisplayName());
                values.put(TvContract.Channels.COLUMN_TYPE, channel.getType());
//        values.put(Channels.COLUMN_BROWSABLE, channel.isBrowsable() ? 1 : 0);
                values.put("browsable", channel.getIsBrowsable());
                values.put(TvContract.Channels.COLUMN_SERVICE_TYPE, channel.getServiceType());
                values.put(TvContract.Channels.COLUMN_SERVICE_ID, channel.getServiceId());
                values.put(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA, channel.getProviderData());
                mContext.getContentResolver().insert(TvContract.Channels.CONTENT_URI, values);
            }
        }
    }
}

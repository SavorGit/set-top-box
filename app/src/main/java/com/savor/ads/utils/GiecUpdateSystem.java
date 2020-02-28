package com.savor.ads.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.storage.StorageManager;

import com.amlogic.update.OtaUpgradeUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GiecUpdateSystem {
	
	private Context mContext;
	private SharedPreferences mPrefs;
	private static final String PREFS_DOWNLOAD_FILELIST = "download_filelist";
	public GiecUpdateSystem(Context context){
		mContext=context;
		mPrefs = context.getSharedPreferences ( "update", Context.MODE_PRIVATE );
	}
	
	public int createAmlScript(String fullpath, boolean wipe_data, boolean wipe_cache) {

        File file;
        String res = "";
        int UpdateMode = OtaUpgradeUtils.UPDATE_UPDATE;
        file = new File("/cache/recovery/command");

        try {
            File parent = file.getParentFile();
            if (file.exists()) {
                file.delete();
            }
            if (!parent.exists()) {
                parent.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        res += "--update_package=";
        Class<?> deskInfoClass = null;
        Method isSd = null;
        Method isUsb = null;
        Object info = getDiskInfo(fullpath);
        if (info == null) {
            res += "/cache/";
            UpdateMode = OtaUpgradeUtils.UPDATE_OTA;
        } else {
            try {
                deskInfoClass = Class.forName("android.os.storage.DiskInfo");
                isSd = deskInfoClass.getMethod("isSd");
                isUsb = deskInfoClass.getMethod("isUsb");
                if ( info != null ) {
                    if ( Boolean.parseBoolean(isSd.invoke(info).toString())){
                        res += "/sdcard/";
                    }else if ( Boolean.parseBoolean(isUsb.invoke(info).toString())) {
                        res += "/udisk/";
                    }else {
                        res += "/cache/";
                        UpdateMode = OtaUpgradeUtils.UPDATE_OTA;
                    }
                } else {
                    res += "/cache/";
                    UpdateMode = OtaUpgradeUtils.UPDATE_OTA;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                res += "/cache/";
                UpdateMode = OtaUpgradeUtils.UPDATE_OTA;
            }
        }
        res += new File(fullpath).getName();
        res += ("\n--locale=" + Locale.getDefault().toString());
        res += (wipe_data? "\n--wipe_data" : "");
        res += (wipe_cache? "\n--wipe_media" : "");

        //res += (mWipeCache.isChecked() ? "\n--wipe_cache" : "");
        try {
            FileOutputStream fout = new FileOutputStream(file);
            byte[] buffer = res.getBytes();
            fout.write(buffer);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
           
        }
        return UpdateMode;
    }
	
	public Object getDiskInfo(String filePath){
        StorageManager mStorageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> volumeInfoC = null;
        Class<?> deskInfoC = null;
        Method getvolume = null;
        Method getDisk = null;
        Method isMount = null;
        Method getPath = null;
        Method getType = null;
        List<?> mVolumes = null;
        try {
            volumeInfoC = Class.forName("android.os.storage.VolumeInfo");
            deskInfoC = Class.forName("android.os.storage.DiskInfo");
            getvolume = StorageManager.class.getMethod("getVolumes");
            mVolumes = (List<?>)getvolume.invoke(mStorageManager);//mStorageManager.getVolumes();
            isMount = volumeInfoC.getMethod("isMountedReadable");
            getDisk = volumeInfoC.getMethod("getDisk");
            getPath = volumeInfoC.getMethod("getPath");
            getType = volumeInfoC.getMethod("getType");
            for (Object vol : mVolumes) {
                if (vol != null && Boolean.parseBoolean(isMount.invoke(vol).toString()) && Integer.parseInt(getType.invoke(vol).toString()) == 0) {
                    Object info = getDisk.invoke(vol);
                   
                    if ( info != null && filePath.contains(((File)getPath.invoke(vol)).getAbsolutePath()) ) {
                        
                        return info;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;

    }
	
	void write2File() {
        String flagParentPath = getCanWritePath();
        if ( flagParentPath.isEmpty() ) {
            return;
        }
        File flagFile = new File( flagParentPath, ".wipe_record" );
        if ( !flagFile.exists() ) {
            try {
                flagFile.createNewFile();
            } catch ( IOException excep ) {
            }
        }
        if ( !flagFile.canWrite() ) {
            return;
        }

        FileWriter fw = null;
        try {
            fw = new FileWriter( flagFile );
        } catch ( IOException excep ) {
        }
        BufferedWriter output = new BufferedWriter( fw );
        Set<String> downfiles = mPrefs.getStringSet ( PREFS_DOWNLOAD_FILELIST,
                                null );
        if ( ( downfiles != null ) && ( downfiles.size() > 0 ) ) {
            String[] downlist = downfiles.toArray ( new String[0] );
            for ( int i = 0; i < downlist.length; i++ ) {
                try {
                    output.write ( downlist[i] );
                    output.newLine();
                } catch ( IOException ex ) {
                }
            }
        }
        try {
            output.close();
        } catch ( IOException e ) {
        }
    }

	private String getCanWritePath(){
        ArrayList<File> externalDevs =  getExternalStorageList();
        String filePath = "";
        for ( int j = 0; (externalDevs != null) && j < externalDevs.size(); j++ ) {
            File dir = externalDevs.get(j);
            if ( dir.isDirectory() && dir.canWrite() ) {
                filePath = dir.getAbsolutePath();
                filePath += "/";
                break;
            }
        }
        return filePath;
    }
	public ArrayList<File> getExternalStorageList(){
        Class<?> volumeInfoC = null;
        Method getvolume = null;
        Method isMount = null;
        Method getType = null;
        Method getPath = null;
        List<?> mVolumes = null;
        StorageManager mStorageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
        ArrayList<File> devList = new ArrayList<File>();
        try {
            volumeInfoC = Class.forName("android.os.storage.VolumeInfo");
            getvolume = StorageManager.class.getMethod("getVolumes");
            isMount = volumeInfoC.getMethod("isMountedReadable");
            getType = volumeInfoC.getMethod("getType");
            getPath = volumeInfoC.getMethod("getPath");
            mVolumes = (List<?>)getvolume.invoke(mStorageManager);

            for (Object vol : mVolumes) {
                if (vol != null && Boolean.parseBoolean(isMount.invoke(vol).toString()) && Integer.parseInt(getType.invoke(vol).toString()) == 0) {
                    devList.add((File)getPath.invoke(vol));
                   
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            return devList;
        }
    }
	
	Set<String> getDownFileSet() {
        return mPrefs.getStringSet ( PREFS_DOWNLOAD_FILELIST, null );
    }
	
	public void copyBKFile() {
        copyBackup(true);
    }
	
	public void copyBackup(boolean outside){
        String backupInrFile = "/data/data/com.droidlogic.otaupgrade/BACKUP";
        String backupOutFile = getCanWritePath();


        File dev = new File( backupOutFile );
        if ( !new File(backupInrFile).exists() ) { return; }
        if ( backupOutFile.isEmpty() || dev == null || !dev.canWrite() ) {
            return;
        }
       if ( dev.isDirectory() && dev.canWrite() && !dev.getName().startsWith(".") ) {
            backupOutFile = dev.getAbsolutePath();
            backupOutFile += "/BACKUP";
            if ( !backupOutFile.equals ( "" ) ) {
                try {
                    if ( outside )
                        copyFile ( backupInrFile, backupOutFile );
                    else
                        copyFile ( backupOutFile, backupInrFile);
                } catch ( Exception ex ) {
                    ex.printStackTrace();
                }
            }
        }
    }
	
	public static  void copyFile (String fileFromPath, String fileToPath ) throws Exception {

        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fi = new FileInputStream( new File( fileFromPath ) );
            in = fi.getChannel();
            fo = new FileOutputStream( new File( fileToPath ) );
            out = fo.getChannel();
            in.transferTo ( 0, in.size(), out );
        } finally {
            try{
                fi.close();
                fo.close();
                in.close();
                out.close();
            }catch(Exception ex){
            }
        }
    }


}

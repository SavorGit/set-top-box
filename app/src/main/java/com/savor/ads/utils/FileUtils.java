package com.savor.ads.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/17.
 */

public class FileUtils {

    public static void deleteFile(File file){
        try{
            if (file.exists()){
                if (file.isDirectory()){
                    File[] files = file.listFiles();
                    for (File f:files){
                        if(f.isFile()){
                            f.delete();
                        }else{
                            deleteFile(f);
                        }
                    }
                }
                file.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void copyDir(String srcDirPath, String dstDirPath) {
        File srcDir = new File(srcDirPath);
        File dstDir = new File(dstDirPath);
        if (!dstDir.isDirectory())
            return;
        if (!dstDir.exists()) {
            dstDir.mkdir();
        }

        if (srcDir.exists() && srcDir.isDirectory()) {
            File[] files = srcDir.listFiles();
            for (File file : files) {
                File dstFile = new File(dstDirPath + file.getName());
                if (file.isFile()) {
                    if (dstFile.exists() && dstFile.length() == file.length()) {
                        continue;
                    }

                    try {
                        copyFile(file, dstFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    copyDir(file.getPath(), dstFile.getPath());
                }
            }
        }
    }

    public static void copyFilesFromAssets(Context context, String assetsPath, String storagePath) {
        String temp = "";

        if (TextUtils.isEmpty(storagePath)) {
            return;
        } else if (storagePath.endsWith(File.separator)) {
            storagePath = storagePath.substring(0, storagePath.length() - 1);
        }

        if (TextUtils.isEmpty(assetsPath) || assetsPath.equals(File.separator)) {
            assetsPath = "";
        } else if (assetsPath.endsWith(File.separator)) {
            assetsPath = assetsPath.substring(0, assetsPath.length() - 1);
        }

        try {
            AssetManager assetManager = context.getAssets();
            File file = new File(storagePath);
            if (!file.exists()) {//如果文件夹不存在，则创建新的文件夹
                file.mkdirs();
            }

            // 获取assets目录下的所有文件及目录名
            String[] fileNames = assetManager.list(assetsPath);
            if (fileNames.length > 0) {
                for (String fileName : fileNames) {
                    if (!TextUtils.isEmpty(assetsPath)) {
                        temp = assetsPath + File.separator + fileName;//补全assets资源路径
                    }

                    String[] childFileNames = assetManager.list(temp);
                    if (!TextUtils.isEmpty(temp) && childFileNames.length > 0) {//判断是文件还是文件夹：如果是文件夹
                        copyFilesFromAssets(context, temp, storagePath + File.separator + fileName);
                    } else {//如果是文件
                        InputStream inputStream = assetManager.open(temp);
                        readInputStream(storagePath + File.separator + fileName, inputStream);
                    }
                }
            } else {
                InputStream inputStream = assetManager.open(assetsPath);
                if (assetsPath.contains(File.separator)) {
                    assetsPath = assetsPath.substring(assetsPath.lastIndexOf(File.separator), assetsPath.length());
                }
                readInputStream(storagePath + File.separator + assetsPath, inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 从assets目录拷贝单个文件到指定目录
     * @param context
     * @param fileName
     * @param storageFilePath
     */
    public static void copyFileFormAssets(Context context, String fileName, String storageFilePath){
        if (TextUtils.isEmpty(fileName)){
            return;
        }
        if (TextUtils.isEmpty(storageFilePath)) {
            return;
        }
        File storageFile = new File(storageFilePath);
        InputStream is=null;
        FileOutputStream fos=null;
        try {
            is=context.getAssets().open(fileName);
            fos = new FileOutputStream(storageFile);
            byte[] buffer = new byte[1024*1024*5];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                is.close();
                fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    /**
     * 读取输入流中的数据写入输出流
     *
     * @param storagePath 目标文件路径
     * @param inputStream 输入流
     */
    public static void readInputStream(String storagePath, InputStream inputStream) {
        File file = new File(storagePath);
        try {
            if (!file.exists()) {
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[inputStream.available()];
                int length = 0;
                while ((length = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                    fos.flush();
                }
                fos.flush();
                fos.close();
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制单个文件
     */
    public static void copyFile(String oldPath, String newPath) throws Exception{
        copyFile(new File(oldPath), new File(newPath));
    }

    public static void copyFile(File oldFile, File newFile) throws Exception{
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            if (!newFile.getParentFile().exists()) {
                newFile.getParentFile().mkdirs();
            }
            if (oldFile.exists()) { //文件存在时
                inStream = new FileInputStream(oldFile); //读入原文件
                fs = new FileOutputStream(newFile);
                byte[] buffer = new byte[4096];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
//                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                    fs.flush();
                }
            }
        } catch (Exception e) {
            throw new Exception();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void write(String filePath, String content) {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String read(String filePath) {
        String content = null;
        File file = new File(filePath);
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                int length = fileInputStream.available();

                byte[] buffer = new byte[length];
                fileInputStream.read(buffer);

                content = new String(buffer, "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    public static String read(String filePath, String charsetName) {
        StringBuilder sb = new StringBuilder();
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream fileInputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                fileInputStream = new FileInputStream(file);
                inputStreamReader = new InputStreamReader(fileInputStream, charsetName);
                bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line + "\r\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return sb.toString();
    }


    /**
     * 读取文件的内容
     * @param file 想要读取的文件对象
     * @return 返回文件内容
     */
    public static List<String> readFile(File file){
        BufferedReader br = null;
        List<String> cfgList = new ArrayList<>();
        try{
            String string = null;
            br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            while((string = br.readLine())!=null){//使用readLine方法，一次读一行
                cfgList.add(string);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (br != null){
                try{
                    br.close();
                }catch (Exception e){}
            }
        }
        return cfgList;
    }

    public static String readFileToStr(File file) {
        BufferedReader br = null;
        StringBuffer jsonContent = new StringBuffer();
        try {
            String string = null;
            br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            while ((string = br.readLine()) != null) {//使用readLine方法，一次读一行
                jsonContent.append(string);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
        }
        return jsonContent.toString();
    }
    public static byte[] readByte(String filePath) {
        byte[] buffer = null;
        File file = new File(filePath);
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                int length = fileInputStream.available();

                buffer = new byte[length];
                fileInputStream.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer;

    }

    /**
     * 判断图片是否是完整的
     * @param path
     * @return
     */
    public static boolean isCompletePicture(String path){
        boolean flag = true;
        try {
            if (TextUtils.isEmpty(path)){
                return false;
            }
            File tarFile = new File(path);
            if (tarFile.exists()){
                BitmapFactory.Options options = null;
                if (options == null){
                    options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeFile(path, options); //filePath代表图片路径
                    if (options.mCancel || options.outWidth == -1
                            || options.outHeight == -1) {
                        //表示图片已损毁
                        Log.e("FileUtils","{"+path+"}图片已损坏");
                        flag = false;
                    }
                }
            }else {
                flag = false;
            }

        }catch (Exception e){
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }
}

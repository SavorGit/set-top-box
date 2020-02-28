package com.savor.ads.utils;

import android.os.Handler;
import android.util.Log;

import com.savor.ads.core.AppApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghq on 2016/12/14.
 */

public class ShellUtils {

    /**
     * 通过shell命令
     * @param list 要执行的shell命令
     * @param action 0:不返回执行命令结果；1：返回命令结果
     * @return
     */
    public static JSONArray universalShellCommandMethod(List<String> list,int action){
        DataOutputStream dos = null;
        InputStream is = null;
        BufferedReader reader = null;
        JSONArray jsonArray = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(list.get(0));
            dos = new DataOutputStream(process.getOutputStream());
            list.remove(0);
            for (String str:list){
                dos.writeBytes(str+"\n");
                dos.flush();
            }
//            if (action==1){
//                jsonArray = new JSONArray();
//                is = process.getInputStream();
//                reader = new BufferedReader(new InputStreamReader(is));
//                String len;
//                while ((len = reader.readLine())!=null){
//                    jsonArray.put(len+"\n");
//                }
//            }
//            process.waitFor();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (dos!=null){
                    dos.close();
                }
                if (is!=null){
                    is.close();
                }
                if (reader!=null){
                    reader.close();
                }
                if (process!=null){
                    process.waitFor();
                    process.destroy();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return jsonArray;
    }



    public static boolean deleteFile(String filePath) {

        boolean result = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(process.getOutputStream());
            try {
                dos.writeBytes("rm -r " + filePath + "\n");
                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                dos.close();
            }
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;

    }

    public static boolean resetNetwork() {

        boolean result = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(process.getOutputStream());
            try {
                dos.writeBytes("ifconfig eth0 down\n");
                dos.flush();
                dos.writeBytes("ifconfig eth0 up\n");
                dos.flush();
                dos.writeBytes("ifconfig wlan0 down\n");
                dos.flush();
                dos.writeBytes("ifconfig wlan0 up\n");
                dos.flush();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    dos.close();
                    if (process != null) {
                        process.waitFor();
                        process.destroy();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    public static boolean unmountWlan1() {

        boolean result = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(process.getOutputStream());
            try {
                dos.writeBytes("ifconfig wlan1 down\n");
                dos.flush();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    dos.close();
                    if (process != null) {
                        process.waitFor();
                        process.destroy();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    public static void reboot() {
        try {
            Runtime.getRuntime().exec("su -c reboot");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * check whether has root permission
     *
     * @return
     */
//    public static boolean checkRootPermission() {
//        return execCommand("echo root", true, false).result == 0;
//    }

    public static boolean checkRootPermission(){
        Process process = null;
        DataOutputStream os = null;
        try{
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0){
                return true;
            } else{
                return false;
            }
        } catch (Exception e){
            Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+ e.getMessage());
                return false;
        } finally{
            try{
                if (os != null){
                    os.close();
                }
                process.exitValue();
            } catch (Exception e){
                e.printStackTrace();
                process.destroy();
            }
        }
    }

//    //更新启动图的位置
//    public static boolean updateLogoPic(String arg) {
//        try {
//            if(AppUtils.isFileExist(GlobalValues.LOGO_FILE_PATH)){
//                File file = new File(GlobalValues.LOGO_FILE_PATH);
//                file.delete();
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        boolean isflag = false;
//        try {
//            java.lang.Process proc = Runtime.getRuntime().exec("su");
//            DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
//            if (dos != null) {
//                try {
//
//                    dos.writeBytes("cat " + arg + " > " + GlobalValues.LOGO_FILE_PATH +"\n");
//                    dos.flush();
//                    dos.writeBytes("exit\n");
//                    dos.flush();
//                    isflag = true;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    LogUtils.d(e.toString());
//                } finally {
//                    if (dos!=null){
//                        dos.close();
//                    }
//                }
//            }
//            try {
//                proc.waitFor();
//            } catch (InterruptedException e) {
//                LogUtils.d(e.toString());
//                e.printStackTrace();
//            }
//
//            try {
//                if (proc != null) {
//                    proc.exitValue();
//                }
//            } catch (IllegalThreadStateException e) {
//                proc.destroy();
//            }
//        } catch (IOException e) {
//            LogUtils.d(e.toString());
//            e.printStackTrace();
//        }
//        return isflag;
//
//    }

    public static void setAmvecmPcMode(){
        try {
            Process suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
            os.writeBytes("echo 0 > /sys/class/amvecm/pc_mode\n");
            os.flush();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    /**
     * 返回的命令结果
     */
    public static class CommandResult {
        /**
         * 结果码
         **/
        public int result;
        /**
         * 成功信息
         **/
        public String successMsg;
        /**
         * 错误信息
         **/
        public String errorMsg;

        public CommandResult(final int result, final String successMsg, final String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }


    /**
     * 是否是在root下执行命令
     *
     * @param command 命令
     * @param isRoot 是否需要root权限执行
     * @return CommandResult
     */
    public static CommandResult execCmd(final String command, final boolean isRoot) {
        return execCmd(new String[] { command }, isRoot, true);
    }

    /**
     * 是否是在root下执行命令
     *
     * @param commands 命令数组
     * @param isRoot 是否需要root权限执行
     * @param isNeedResultMsg 是否需要结果消息
     * @return CommandResult
     */
    public static CommandResult execCmd(final String[] commands, final boolean isRoot,
                                        final boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) continue;
                os.write(command.getBytes());
                os.writeBytes("\n");
                os.flush();
            }
            os.writeBytes("exit\n");
            os.flush();
            result = process.waitFor();
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "UTF-8"));
                errorResult = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), "UTF-8"));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIO(os, successResult, errorResult);
            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(),
                errorMsg == null ? null : errorMsg.toString());
    }


    public static boolean copyBootVideo(File file){

        if (file.length() <= 0) {
            file.delete();
            LogFileUtil.writeException(new Throwable("cpoy bootvideo fatal, bootvideo.mp4 length is 0"));
            return false;
        }

        boolean isflag = false;
        Process proc = null;
        String targetPath = ConstantValues.BOOT_VIDEO_STORAGE + ConstantValues.BOOT_VIDEO_NAME;
        try {
            proc = Runtime.getRuntime().exec("su");
            try {
                DataOutputStream dos = new DataOutputStream(proc.getOutputStream());
                dos.writeBytes("mount -o remount,rw /system\n");
                dos.flush();

                String catCommand = "cat " + file.getPath() + " > " + targetPath + "\n";
                dos.writeBytes(catCommand);
                dos.flush();
                Thread.sleep(2000);

                dos.writeBytes("chmod 755 " + targetPath + "\n");
                dos.flush();
                Thread.sleep(1000);
                isflag = true;

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (proc != null) {
                try {
                    proc.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return isflag;

    }
}

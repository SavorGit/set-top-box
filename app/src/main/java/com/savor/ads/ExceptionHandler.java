package com.savor.ads;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.widget.Toast;

import com.savor.ads.activity.MainActivity;
import com.savor.ads.utils.ActivitiesManager;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShellUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by zhanghq on 2016/12/19.
 */

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Application mContext;
    private final Thread.UncaughtExceptionHandler mDefaultExceptionHandler;

    public ExceptionHandler(Application context) {
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        mContext = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        LogUtils.e("ExceptionHandler" + "uncaughtException");
        LogFileUtil.writeException(e);

        // 友盟保存统计信息
        MobclickAgent.onKillProcess(mContext);


        showCrashTips();

        closeResource();

        // 退出并重启应用
        exitAndRestart();
    }

    private void showCrashTips() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "亲 ，程序出了点小问题即将重启哦", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }).start();

        try {
            Thread.sleep(1000 * 2);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private void closeResource() {
        if (mContext instanceof SavorApplication) {
            ((SavorApplication) mContext).stopScreenProjectionService();
        }
    }

    private void exitAndRestart() {
        ActivitiesManager.getInstance().popAllActivities();
//        System.exit(-1);
//        Process.killProcess(Process.myPid());

//        restartApp();

        ShellUtils.reboot();
    }

    private void restartApp() {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //退出程序
        AlarmManager mgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
//        Intent intent = new Intent(mContext, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                Intent.FLAG_ACTIVITY_NEW_TASK);
//        mContext.startActivity(intent);
    }
}

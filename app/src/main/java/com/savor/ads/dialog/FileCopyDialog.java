package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.savor.ads.R;
import com.savor.ads.adapter.FileCopyListAdapter;
import com.savor.ads.core.Session;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.FileUtils;
import com.savor.ads.utils.KeyCode;
import com.savor.ads.utils.ShowMessage;

import java.io.File;
import java.util.ArrayList;

import static u.aly.cw.i;

/**
 * Created by zhanghq on 2016/12/12.
 */
public class FileCopyDialog extends Dialog {

    private Context mContext;
    private Session mSession;
    private TextView titleTV;
    private TextView mtipsTV;
    ArrayList<String> listtips;
    private boolean mIsProcessing;
    private long mLastRefreshTime;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.obj!=null){
                String text = msg.obj.toString();
                switch (msg.what){
                    case 1:
                        try {
                            mtipsTV.setText(mtipsTV.getText() + text + "\r\n");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                        break;
                    case 2:
                        titleTV.setText(text);
                        titleTV.setTextColor(Color.parseColor("#FF0000"));
                        break;
                }
            }
            return true;
        }
    });

    public FileCopyDialog(Context context) {
        super(context, R.style.channel_searching_dialog_theme);

        mContext = context;
        mSession = Session.get(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_file_copy);
        setDialogAttributes();
        titleTV = findViewById(R.id.tv_title);
        mtipsTV = findViewById(R.id.tv_tips);
    }

    private void setDialogAttributes() {
        Window window = getWindow(); // 得到对话框
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = DensityUtil.dip2px(getContext(), 800);
        wl.height = DensityUtil.dip2px(getContext(), 600);
        wl.gravity = Gravity.CENTER;
        window.setAttributes(wl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if (keyCode == KeyCode.KEY_CODE_BACK) {
            if (mIsProcessing) {
                ShowMessage.showToast(mContext, "正在拷贝中，请稍候");
            } else {
                dismiss();
            }
            handled = true;
        }
        return handled || super.onKeyDown(keyCode, event);
    }

    @Override
    public void show() {
        super.show();
        mIsProcessing = true;
        titleTV.setText("网络机顶盒初装");
        titleTV.setTextColor(Color.parseColor("#333333"));
        listtips = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File mediaDir = new File(mSession.getUsbPath() + ConstantValues.USB_FILE_HOTEL_MEDIA_PATH);
                File birthdayDir = new File(mSession.getUsbPath() + "birthday_ondemand");
                File activityAdsDir = new File(mSession.getUsbPath() + "activity_ads");
                File selectContentDir = new File(mSession.getUsbPath()+ "select_content");
                File welcomeDir = new File(mSession.getUsbPath()+ "welcome_resource");
                String sdPath = AppUtils.getSDCardPath();
                if (mediaDir.isDirectory() && mediaDir.isDirectory()) {
                    File sdMediaFile = new File(sdPath+ConstantValues.USB_FILE_HOTEL_MEDIA_PATH);
                    if (!sdMediaFile.exists()) {
                        sdMediaFile.mkdir();
                    }
                    File[] listFiles = mediaDir.listFiles();
                    for (int i = 0; i < listFiles.length; i++) {
                        File file = listFiles[i];
                        String value = "开始拷贝" + file.getPath() + "(" + (i + 1) + "/" + listFiles.length + ")";
                        mHandler.sendMessage(mHandler.obtainMessage(1, value));
                        boolean isSuccess = true;
                        File dstFile = new File(sdMediaFile, file.getName());
                        if (!dstFile.exists() || dstFile.length() != file.length()) {
                            try {
                                FileUtils.copyFile(file, dstFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                                isSuccess = false;
                            }
                        }
                        String resultMsg = "拷贝" + file.getPath();
                        if (isSuccess) {
                            resultMsg += "成功";
                        } else {
                            resultMsg += "失败";
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(1, resultMsg));
                        try {
                            Thread.sleep(1000);
                        }catch (Exception e){
                        }
                    }
                }
                if (birthdayDir.exists() && birthdayDir.isDirectory()) {
                    File sdBirthdayVodFile = new File(sdPath+"birthday_ondemand");
                    if (!sdBirthdayVodFile.exists()) {
                        sdBirthdayVodFile.mkdir();
                    }
                    File[] listFiles = birthdayDir.listFiles();
                    for (int i = 0; i < listFiles.length; i++) {
                        File file = listFiles[i];
                        String value = "开始拷贝" + file.getPath() + "(" + (i + 1) + "/" + listFiles.length + ")";
                        mHandler.sendMessage(mHandler.obtainMessage(1,value));
                        boolean isSuccess = true;
                        File dstFile = new File(sdBirthdayVodFile.getAbsolutePath(), file.getName());
                        if (!dstFile.exists() || dstFile.length() != file.length()) {
                            try {
                                FileUtils.copyFile(file, dstFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                                isSuccess = false;
                            }
                        }
                        String resultMsg = "拷贝" + file.getPath();
                        if (isSuccess) {
                            resultMsg += "成功";
                        } else {
                            resultMsg += "失败";
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(1, resultMsg));
                        try {
                            Thread.sleep(1000);
                        }catch (Exception e){
                        }
                    }
                }
                if (activityAdsDir.isDirectory() && activityAdsDir.isDirectory()) {
                    File sdActivityAdsFile = new File(sdPath+"activity_ads");
                    if (!sdActivityAdsFile.exists()) {
                        sdActivityAdsFile.mkdir();
                    }
                    File[] listFiles = activityAdsDir.listFiles();
                    for (int i = 0; i < listFiles.length; i++) {
                        File file = listFiles[i];
                        String value = "开始拷贝" + file.getPath() + "(" + (i + 1) + "/" + listFiles.length + ")";
                        mHandler.sendMessage(mHandler.obtainMessage(1, value));
                        boolean isSuccess = true;
                        File dstFile = new File(sdActivityAdsFile.getAbsolutePath(), file.getName());
                        if (!dstFile.exists() || dstFile.length() != file.length()) {
                            try {
                                FileUtils.copyFile(file, dstFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                                isSuccess = false;
                            }
                        }
                        String resultMsg = "拷贝" + file.getPath();
                        if (isSuccess) {
                            resultMsg += "成功";
                        } else {
                            resultMsg += "失败";
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(1, resultMsg));
                        try {
                            Thread.sleep(1000);
                        }catch (Exception e){
                        }
                    }
                }
                if (selectContentDir.isDirectory() && selectContentDir.isDirectory()) {
                    File sdSelectContentFile = new File(sdPath+"select_content");
                    if (!sdSelectContentFile.exists()) {
                        sdSelectContentFile.mkdir();
                    }
                    File[] listFiles = selectContentDir.listFiles();
                    for (int i = 0; i < listFiles.length; i++) {
                        File file = listFiles[i];
                        String value = "开始拷贝" + file.getPath() + "(" + (i + 1) + "/" + listFiles.length + ")";
                        mHandler.sendMessage(mHandler.obtainMessage(1, value));
                        boolean isSuccess = true;
                        File dstFile = new File(sdSelectContentFile.getAbsolutePath(), file.getName());
                        if (!dstFile.exists() || dstFile.length() != file.length()) {
                            try {
                                FileUtils.copyFile(file, dstFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                                isSuccess = false;
                            }
                        }
                        String resultMsg = "拷贝" + file.getPath();
                        if (isSuccess) {
                            resultMsg += "成功";
                        } else {
                            resultMsg += "失败";
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(1, resultMsg));
                        try {
                            Thread.sleep(1000);
                        }catch (Exception e){
                        }
                    }
                }
                if (welcomeDir.isDirectory() && welcomeDir.isDirectory()) {
                    File sdWelcomeFile = new File(sdPath+"welcome_resource");
                    if (!sdWelcomeFile.exists()) {
                        sdWelcomeFile.mkdir();
                    }
                    File[] listFiles = welcomeDir.listFiles();
                    for (int i = 0; i < listFiles.length; i++) {
                        File file = listFiles[i];
                        String value = "开始拷贝" + file.getPath() + "(" + (i + 1) + "/" + listFiles.length + ")";
                        mHandler.sendMessage(mHandler.obtainMessage(1, value));
                        boolean isSuccess = true;
                        File dstFile = new File(sdWelcomeFile.getAbsolutePath(), file.getName());
                        if (!dstFile.exists() || dstFile.length() != file.length()) {
                            try {
                                FileUtils.copyFile(file, dstFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                                isSuccess = false;
                            }
                        }
                        String resultMsg = "拷贝" + file.getPath();
                        if (isSuccess) {
                            resultMsg += "成功";
                        } else {
                            resultMsg += "失败";
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(1, resultMsg));
                        try {
                            Thread.sleep(1000);
                        }catch (Exception e){
                        }
                    }
                }

                mHandler.sendMessage(mHandler.obtainMessage(2, "网络机顶盒初装完成"));
                mIsProcessing = false;
            }
        }).start();
    }



    private Runnable mRefreshListRunnable = new Runnable() {
        @Override
        public void run() {
            mLastRefreshTime = System.currentTimeMillis();
        }
    };
}

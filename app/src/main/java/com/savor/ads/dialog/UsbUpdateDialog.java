package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savor.ads.R;
import com.savor.ads.core.Session;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.FileUtils;
import com.savor.ads.utils.KeyCode;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;
import com.savor.ads.utils.UsbUpdateHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghq on 2016/12/12.
 */
public class UsbUpdateDialog extends Dialog implements View.OnClickListener {
    private TextView mHotelTv, mUpdateTipsTv;
    private Button mCancelBtn, mConfirmBtn;
    private LinearLayout mActionLl;

    private ArrayList<TextView> mActionTvList;
    private ArrayList<ProgressBar> mActionProgressBarList;

    private Session mSession;
    private Context mContext;
    private List<String> mActionList;

    private boolean mIsProcessing;

    private Handler mHandler = new Handler();

    public UsbUpdateDialog(Context context) {
        super(context, R.style.channel_searching_dialog_theme);

        mContext = context;
        mSession = Session.get(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_usb_update);
        setDialogAttributes();

        mHotelTv = (TextView) findViewById(R.id.tv_hotel);
        mUpdateTipsTv = (TextView) findViewById(R.id.tv_action_tips);
        mCancelBtn = (Button) findViewById(R.id.btn_cancel);
        mConfirmBtn = (Button) findViewById(R.id.btn_ok);
        mActionLl = (LinearLayout) findViewById(R.id.ll_action);

        mHotelTv.setText("当前酒楼ID：" + mSession.getBoiteId());
        mCancelBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);
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

    private void setViews() {
        mConfirmBtn.setEnabled(true);
        mCancelBtn.setEnabled(true);
        mUpdateTipsTv.setText("将执行以下动作，若确认无误请点击下方【确定】按钮");
        mUpdateTipsTv.setTextColor(0xFF333333);

        mActionLl.removeAllViews();
        String cfgPath = mSession.getUsbPath() + File.separator +
                ConstantValues.USB_FILE_HOTEL_PATH + File.separator +
                mSession.getBoiteId() + File.separator +
                ConstantValues.USB_FILE_HOTEL_UPDATE_CFG;
        List<String> commands = FileUtils.readFile(new File(cfgPath));
        if (commands != null && !commands.isEmpty()) {
            mActionProgressBarList = new ArrayList<>();
            mActionTvList = new ArrayList<>();
            mActionList = new ArrayList<>();
            for (String action : commands) {
                boolean isKnownAction = true;
                View view = View.inflate(getContext(), R.layout.layout_action_item, null);
                TextView actionTv = (TextView) view.findViewById(R.id.tv_action);
                ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                switch (action) {
                    case ConstantValues.USB_FILE_HOTEL_GET_CHANNEL:
                        actionTv.setText("读取电视节目");
                        break;
                    case ConstantValues.USB_FILE_HOTEL_SET_CHANNEL:
                        actionTv.setText("写入电视节目");
                        break;
                    case ConstantValues.USB_FILE_HOTEL_GET_LOG:
                        actionTv.setText("提取log日志");
                        break;
                    case ConstantValues.USB_FILE_HOTEL_GET_LOGED:
                        actionTv.setText("提取loged日志");
                        break;
                    case ConstantValues.USB_FILE_HOTEL_UPDATE_MEIDA:
                        actionTv.setText("更新视频节目");
                        break;
                    case ConstantValues.USB_FILE_HOTEL_UPDATE_APK:
                        actionTv.setText("更新应用");
                        break;
                    case ConstantValues.USB_FILE_HOTEL_UPDATE_LOGO:
                        actionTv.setText("更新LOGO");
                        break;
                    default:
                        isKnownAction = false;
                        break;
                }

                if (isKnownAction) {
                    mActionList.add(action);
                    mActionLl.addView(view);

                    mActionProgressBarList.add(progressBar);
                    mActionTvList.add(actionTv);
                }
            }

            if (mActionList.contains(ConstantValues.USB_FILE_HOTEL_UPDATE_APK) && mActionList.size() > 1) {
//                ShowMessage.showToastLong(mContext, "【更新应用】动作只能单独执行！！");
//                mConfirmBtn.setEnabled(false);

                // 手动将APK升级放到最后执行
                mActionList.remove(ConstantValues.USB_FILE_HOTEL_UPDATE_APK);
                mActionList.add(ConstantValues.USB_FILE_HOTEL_UPDATE_APK);
            }

            LogUtils.d("UDisk Action List is " + mActionList);
        }
    }

    @Override
    public void show() {
        super.show();
        setViews();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if (keyCode == KeyCode.KEY_CODE_BACK) {
            if (!mIsProcessing) {
                dismiss();
            } else {
                ShowMessage.showToast(mContext, "正在处理中，请稍候");
            }
            handled = true;
        }
        return handled || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                if (!mIsProcessing) {
                    dismiss();
                } else {
                    ShowMessage.showToast(mContext, "正在处理中，请稍候");
                }
                break;
            case R.id.btn_ok:

                if (mActionList!=null&&mActionList.size()>0){
                    mIsProcessing = true;
                    mConfirmBtn.setEnabled(false);
                    mCancelBtn.setEnabled(false);
                    mUpdateTipsTv.setText("机顶盒更新中，切勿关机！！！");
                    mUpdateTipsTv.setTextColor(0xFFFF0000);
                    doAction();
                }else{
                    mUpdateTipsTv.setTextColor(0xFF5C5CCD);
                    mUpdateTipsTv.setText("请检查你的update.cfg文件，没有可执行功能!!!");
                }

                break;
        }
    }

    private void doAction() {
        final UsbUpdateHandler usbUpdateHandler = new UsbUpdateHandler(mContext, mActionList, new UsbUpdateHandler.ProgressCallback() {

            @Override
            public void onStart(final int index) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (index < mActionProgressBarList.size()) {
                            mActionProgressBarList.get(index).setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onActionComplete(final int index, final boolean success, final String msg) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (index < mActionTvList.size()) {
                            if (success) {
                                mActionTvList.get(index).setText(msg);
                                mActionTvList.get(index).setTextColor(0xFF00FF00);
                                mActionProgressBarList.get(index).setVisibility(View.GONE);
                            } else {
                                if (!TextUtils.isEmpty(msg)){
                                    mActionTvList.get(index).setText(msg);
                                }
                                mActionTvList.get(index).setTextColor(0xFFFF0000);
                                mActionProgressBarList.get(index).setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }

            @Override
            public void onAllComplete(boolean mIsAllSuccess) {
                // 执行过一次U盘更新则认为已经是单机版
//                mSession.setStandalone(true);
                final boolean allSuccess = mIsAllSuccess;
                mIsProcessing = false;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (allSuccess){
                            mUpdateTipsTv.setText("所有动作已执行完毕");
                            mUpdateTipsTv.setTextColor(0xFF333333);
                        }

                    }
                });
            }

            @Override
            public void onActionProgress(final int index,final String msg) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mActionTvList.get(index).setText(msg);
                    }
                });
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                usbUpdateHandler.execute();
            }
        }).start();
    }
}

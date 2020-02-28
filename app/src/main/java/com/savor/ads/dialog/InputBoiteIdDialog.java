package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.savor.ads.R;
import com.savor.ads.bean.BoiteBean;
import com.savor.ads.bean.BoxBean;
import com.savor.ads.bean.RoomBean;
import com.savor.ads.bean.SetTopBoxBean;
import com.savor.ads.core.Session;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.FileUtils;
import com.savor.ads.utils.KeyCode;
import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by zhanghq on 2016/12/12.
 */

public class InputBoiteIdDialog extends Dialog implements View.OnClickListener {
    private TextView mBoiteIdTv;
    private Button mSaveBtn;

    private Context mContext;
    private Session mSession;

    private ArrayList<BoiteBean> mBoiteList;

    private Callback mCallback;

    public InputBoiteIdDialog(Context context, Callback callback) {
        super(context, R.style.channel_searching_dialog_theme);

        mContext = context;
        mCallback = callback;
        mSession = Session.get(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_input_boite_id);
        setDialogAttributes();

        mBoiteIdTv = (TextView) findViewById(R.id.tv_boite_id);
        mSaveBtn = (Button) findViewById(R.id.btn_save);

        mSaveBtn.setOnClickListener(this);
    }

    private void setDialogAttributes() {
        Window window = getWindow(); // 得到对话框
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = DensityUtil.dip2px(getContext(), 700);
        wl.height = DensityUtil.dip2px(getContext(), 300);
        wl.gravity = Gravity.CENTER;
        window.setAttributes(wl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if (keyCode == KeyCode.KEY_CODE_BACK) {
            dismiss();
            handled = true;

        }
        return handled || super.onKeyDown(keyCode, event);
    }

    @Override
    public void show() {
        super.show();

        mSaveBtn.requestFocus();
        File hotelListFile = new File(mSession.getUsbPath() + File.separator +
                ConstantValues.USB_FILE_HOTEL_LIST_JSON);
        if (hotelListFile.exists()) {
            String str = FileUtils.read(hotelListFile.getPath());
            if (!TextUtils.isEmpty(str)) {
                try {
                    mBoiteList = new Gson().fromJson(str, new TypeToken<ArrayList<BoiteBean>>() {
                    }.getType());
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                String input = mBoiteIdTv.getText().toString();
                if (!TextUtils.isEmpty(input)) {
                    checkAndSaveBoiteId(input);
                }
                break;
        }
    }

    private void checkAndSaveBoiteId(String boiteId) {
        boolean foundMatchHotel = false;
        boolean foundMatchRoom = false;
        String boiteName = null, roomId = null, roomName = null, roomType = null, boxName = null ;
        if (mBoiteList != null) {
            for (BoiteBean boite : mBoiteList) {
                if (boiteId.equals(boite.getHotel_id())) {
                    foundMatchHotel = true;
                    boiteName = boite.getHotel_name();
                    break;
                }
            }
        }

        if (foundMatchHotel) {
//            mSession.setStandalone(true);
            mSession.setBoiteId(boiteId);
            mSession.setBoiteName(boiteName);
            if (mCallback != null) {
                mCallback.onBoiteIdCheckPass();
            }
            dismiss();
        } else {
            ShowMessage.showToast(mContext, "请输入合法的酒楼ID");
        }
    }

    public interface Callback {
        void onBoiteIdCheckPass();
    }
}

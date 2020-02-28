package com.savor.ads.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.savor.ads.R;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.KeyCode;

/**
 * Created by zhanghq on 2016/12/12.
 */

public class TvChannelSearchingDialog extends Dialog {
    private TextView mProgressTv;

    public TvChannelSearchingDialog(Context context) {
        super(context, R.style.channel_searching_dialog_theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_auto_turning);
        setDialogAttributes();

        mProgressTv = (TextView) findViewById(R.id.tv_searching_progress);
    }

    private void setDialogAttributes() {
        Window window = getWindow(); // 得到对话框
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = DensityUtil.dip2px(getContext(), 350);
        wl.height = DensityUtil.dip2px(getContext(), 150);
        wl.gravity = Gravity.CENTER;
        window.setAttributes(wl);
    }

    public void updateProgress(final int progress) {
        mProgressTv.post(new Runnable() {
            @Override
            public void run() {
                mProgressTv.setText("正在搜台 " + progress + "%");
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if (keyCode == KeyCode.KEY_CODE_BACK) {
            dismiss();
            handled = true;

        }
//        if (getContext() instanceof Activity) {
//            return handled || ((Activity) getContext()).onKeyDown(keyCode, event);
//        }
        return handled || super.onKeyDown(keyCode, event);
    }
}

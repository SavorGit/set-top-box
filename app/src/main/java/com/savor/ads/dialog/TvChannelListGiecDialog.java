package com.savor.ads.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.savor.ads.R;
import com.savor.ads.adapter.ChannelListGiecAdapter;
import com.savor.ads.core.Session;
import com.savor.ads.utils.KeyCode;
import com.savor.tvlibrary.AtvChannel;

import java.util.ArrayList;

/**
 * Created by zhanghq on 2016/12/12.
 */

public class TvChannelListGiecDialog extends Dialog {

    private ListView mChannelsLv;
    private ChannelSelectCallback mChannelSelectCallback;
    private ArrayList<AtvChannel> mChannels;
    private ChannelListGiecAdapter mAdapter;

    private Handler mHandler = new Handler();

    private Runnable mHideChannelListRunnable = new Runnable() {
        @Override
        public void run() {
            if (isShowing()) {
                dismiss();
            }
        }
    };

    public TvChannelListGiecDialog(Context context, ArrayList<AtvChannel> channels, ChannelSelectCallback callback) {
        super(context, R.style.channel_list_dialog_theme);
        mChannels = channels;
        mChannelSelectCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_channel_list);
        setDialogAttributes();

        mChannelsLv = (ListView) findViewById(R.id.lv_channel_list);
        mAdapter = new ChannelListGiecAdapter(getContext(), mChannels);
        mChannelsLv.setAdapter(mAdapter);
        mChannelsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mChannelSelectCallback != null) {
                    mChannelSelectCallback.onChannelSelect(position);
                }
                dismiss();
            }
        });
    }

    private void setDialogAttributes() {
        Window window = getWindow(); // 得到对话框
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wl.height = WindowManager.LayoutParams.MATCH_PARENT;
        wl.gravity = Gravity.LEFT;
        window.setAttributes(wl);
    }

    public void setChannels(ArrayList<AtvChannel> channels) {
        mChannels = channels;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void show() {
        if (mChannels != null && mChannels.size() > 0) {
            mHandler.removeCallbacks(mHideChannelListRunnable);

            super.show();
            for (int i = 0; i < mChannels.size(); i++) {
                AtvChannel program = mChannels.get(i);
                if (program.getChannelNum() == Session.get(getContext()).getTvCurrentChannelNumber()) {
                    mChannelsLv.setSelection(i);
                    break;
                }
            }

            mHandler.postDelayed(mHideChannelListRunnable, 15 * 1000);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if (keyCode == KeyCode.KEY_CODE_UP) {
            mChannelsLv.setSelection(mChannelsLv.getSelectedItemPosition() - 1 < 0 ?
                    mChannels.size() - 1 : mChannelsLv.getSelectedItemPosition() - 1);
            delayHide();
            handled = true;

        } else if (keyCode == KeyCode.KEY_CODE_DOWN) {
            mChannelsLv.setSelection(mChannelsLv.getSelectedItemPosition() + 1 >= mChannelsLv.getCount() ?
                    0 : mChannelsLv.getSelectedItemPosition() + 1);
            delayHide();
            handled = true;

        } else if (keyCode == KeyCode.KEY_CODE_BACK) {
            mHandler.removeCallbacks(mHideChannelListRunnable);
            dismiss();
            handled = true;

        }
        return handled || super.onKeyDown(keyCode, event);
    }

    private void delayHide() {
        mHandler.removeCallbacks(mHideChannelListRunnable);
        mHandler.postDelayed(mHideChannelListRunnable, 15 * 1000);
    }

    public void onDestroy() {
        dismiss();
        mHandler.removeCallbacks(null);
        mHandler = null;
    }

    public interface ChannelSelectCallback {
        void onChannelSelect(int index);
    }
}

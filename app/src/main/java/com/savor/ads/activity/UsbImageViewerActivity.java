package com.savor.ads.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;

import com.savor.ads.R;
import com.savor.ads.adapter.StringPagerAdapter;

import java.util.ArrayList;

public class UsbImageViewerActivity extends BaseActivity {

    public static final String EXTRA_URLS = "extra_urls";
    public static final String EXTRA_USB_PATH = "extra_usb_path";

    private static final int SWITCH_TIME = 1000 * 10;

    private ViewPager mImagesVp;

    private ArrayList<String> mDataSource;
    private String mCurrentUsbPath;

    private int mCurrentIndex;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mCurrentIndex = (mCurrentIndex + 1) % mDataSource.size();
            mImagesVp.setCurrentItem(mCurrentIndex, true);
            mHandler.sendEmptyMessageDelayed(0, SWITCH_TIME);
            return true;
        }
    });
    private StringPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_image_viewer);

        mImagesVp = (ViewPager) findViewById(R.id.vp_images);

        if (getIntent() != null) {
            mDataSource = (ArrayList<String>) getIntent().getSerializableExtra(EXTRA_URLS);
            mCurrentUsbPath = getIntent().getStringExtra(EXTRA_USB_PATH);
        }

        mAdapter = new StringPagerAdapter(mContext, mDataSource);
        mImagesVp.setAdapter(mAdapter);
        if (mDataSource != null && mDataSource.size() > 0) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.sendEmptyMessageDelayed(0, SWITCH_TIME);
        }
    }

    public void resetImages(ArrayList<String> usbImgPathList, String currentUsbPath) {
        mAdapter.setDataSource(usbImgPathList);
        mImagesVp.setCurrentItem(0, false);
        mCurrentIndex = 0;

        mCurrentUsbPath = currentUsbPath;
    }

    public String getCurrentUsbPath() {
        return mCurrentUsbPath;
    }
}

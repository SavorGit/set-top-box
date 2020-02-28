package com.savor.ads.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.savor.ads.utils.LogFileUtil;
import com.savor.ads.utils.LogUtils;

/**
 * Created by zhanghq on 2017/2/9.
 */

public class NetworkBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo etherInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
//        etherInfo.getState();
        LogFileUtil.write("NetworkBroadcastReceiver ethernet state:" + etherInfo.getState() + ", wifi state:" + wifiInfo.getState() +
                ", activeInfo:" + activeInfo);
        if (activeInfo == null) {
            LogUtils.e("无可用网络");
        }
    }
}

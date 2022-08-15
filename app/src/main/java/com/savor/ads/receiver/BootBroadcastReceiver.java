package com.savor.ads.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (context.getPackageName().equals(packageName)) {
                // 获取默认启动activity
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            }
        }
    }
}
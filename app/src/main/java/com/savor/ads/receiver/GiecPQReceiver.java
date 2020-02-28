package com.savor.ads.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.savor.ads.service.GiecPQService;

public class GiecPQReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        ShowMessage.showToast(context,"收到广播" + intent.getAction());
        Intent startGiec = new Intent(context, GiecPQService.class);
        context.startService(startGiec);
    }
}

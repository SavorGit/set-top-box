package com.savor.ads.service;

import android.app.IntentService;
import android.content.Intent;

import com.savor.ads.utils.ShowMessage;

import java.io.DataOutputStream;
import java.io.IOException;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GiecPQService extends IntentService {

    public GiecPQService() {
        super("GiecPQService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            //os.writeBytes("mount -o remount,rw -t yaffs /system\n");
            //os.flush();
            os.writeBytes("echo 0 > /sys/class/amvecm/pc_mode\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

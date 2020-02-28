package com.jar.savor.box;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jar.savor.box.interfaces.OnRemoteOperationListener;
import com.jar.savor.box.services.RemoteService;

/**
 * Created by zhanghq on 2016/12/22.
 */

public class ServiceUtil {
    public static final String ACTION_REMOTE_SERVICE = "com.jar.savor.box.REMOTE1";

    public ServiceUtil() {
    }

    public static ServiceConnection registerService(final OnRemoteOperationListener listener) {
        ServiceConnection conn = new ServiceConnection() {
            private RemoteService.OperationBinder binder;

            public void onServiceDisconnected(ComponentName name) {
                this.binder = null;
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                this.binder = (RemoteService.OperationBinder)service;
                RemoteService controller = this.binder.getController();
                controller.setOnRemoteOpreationListener(listener);
            }
        };
        return conn;
    }
}

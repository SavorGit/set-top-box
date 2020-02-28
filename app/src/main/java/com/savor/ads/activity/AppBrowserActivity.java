package com.savor.ads.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.GridView;

import com.savor.ads.R;
import com.savor.ads.adapter.AppBrowserAdapter;
import com.savor.ads.bean.AppBean;
import com.savor.ads.utils.KeyCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppBrowserActivity extends BaseActivity {

    private GridView mGridView;
    private ArrayList<AppBean> mApps;
    private AppBrowserAdapter mAppAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_browser);

        mGridView = findViewById(R.id.gv_apps);
        mApps = getLaunchAppList();
        mAppAdapter = new AppBrowserAdapter(this, mApps);
        mGridView.setAdapter(mAppAdapter);
    }

    private ArrayList<AppBean> getLaunchAppList() {
        PackageManager localPackageManager = getPackageManager();
        Intent localIntent = new Intent("android.intent.action.MAIN");
        localIntent.addCategory("android.intent.category.LAUNCHER");
        List localList = localPackageManager.queryIntentActivities(localIntent, 0);
        ArrayList<AppBean> localArrayList = null;
        Iterator localIterator = null;
        if (localList != null) {
            localArrayList = new ArrayList();
            localIterator = localList.iterator();
        }
        while (true) {
            if (!localIterator.hasNext())
                break;
            ResolveInfo localResolveInfo = (ResolveInfo) localIterator.next();
            AppBean localAppBean = new AppBean();
            localAppBean.setIcon(localResolveInfo.activityInfo.loadIcon(localPackageManager));
            localAppBean.setName(localResolveInfo.activityInfo.loadLabel(localPackageManager).toString());
            localAppBean.setPackageName(localResolveInfo.activityInfo.packageName);
            localAppBean.setDataDir(localResolveInfo.activityInfo.applicationInfo.publicSourceDir);
            localAppBean.setLauncherName(localResolveInfo.activityInfo.name);
            String pkgName = localResolveInfo.activityInfo.packageName;
            PackageInfo mPackageInfo;
            try {
                mPackageInfo = getPackageManager().getPackageInfo(pkgName, 0);
                if ((mPackageInfo.applicationInfo.flags & mPackageInfo.applicationInfo.FLAG_SYSTEM) > 0) {//系统预装
                    localAppBean.setSysApp(true);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (!localAppBean.getPackageName().contains(getPackageName())) {
                localArrayList.add(localAppBean);
            }
        }
        return localArrayList;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if (keyCode == KeyCode.KEY_CODE_SHOW_APP_INSTALLED) {
            handled = true;

        }
        return handled || super.onKeyDown(keyCode, event);
    }
}

package com.savor.ads.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.savor.ads.R;
import com.savor.ads.bean.AppBean;

import java.util.ArrayList;

/**
 * Created by zhanghq on 2016/12/10.
 */

public class AppBrowserAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<AppBean> mAppBeen;

    public AppBrowserAdapter(Context context, ArrayList<AppBean> appBeen) {
        mContext = context;
        mAppBeen = appBeen;
    }

    @Override
    public int getCount() {
        return mAppBeen == null ? 0 : mAppBeen.size();
    }

    @Override
    public Object getItem(int i) {
        return mAppBeen.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.item_app, null);
            holder = new ViewHolder();
            holder.mIconIv = view.findViewById(R.id.iv_icon);
            holder.mNameTv = view.findViewById(R.id.tv_name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.mIconIv.setImageDrawable(mAppBeen.get(i).getIcon());
        holder.mNameTv.setText(mAppBeen.get(i).getName());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppBean info = mAppBeen.get(i);

                //该应用的包名
                String pkg = info.getPackageName();
                //应用的主activity类
                String cls = info.getLauncherName();

                ComponentName component = new ComponentName(pkg, cls);

                Intent i = new Intent();
                i.setComponent(component);
                mContext.startActivity(i);
            }
        });
        return view;
    }

    class ViewHolder {
        ImageView mIconIv;
        TextView mNameTv;
    }
}

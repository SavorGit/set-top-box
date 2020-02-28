package com.savor.ads.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.savor.ads.R;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.core.Session;
import com.savor.ads.utils.ConstantValues;

import java.util.ArrayList;

/**
 * Created by zhang.haiqiang on 2018/1/17.
 */

public class PlaylistAdapter <T extends MediaLibBean> extends BaseAdapter {
    private Context mContext;
    private ArrayList<T> mPlaylist;

    public PlaylistAdapter(Context context, ArrayList<T> playlist) {
        mContext = context;
        mPlaylist = playlist;
    }

    @Override
    public int getCount() {
        return mPlaylist == null ? 0 : mPlaylist.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlaylist == null ? null : mPlaylist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_play_list, null);

            viewHolder = new ViewHolder();
            viewHolder.mMediaNameTv = (TextView) convertView.findViewById(R.id.tv_media_name);
            viewHolder.mIsAdsTv = (TextView) convertView.findViewById(R.id.tv_is_ads);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MediaLibBean mediaLibBean = mPlaylist.get(position);
        viewHolder.mMediaNameTv.setText((position + 1) + "、" + mediaLibBean.getChinese_name());
        if (ConstantValues.ADS.equals(mediaLibBean.getType())) {
            viewHolder.mIsAdsTv.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mIsAdsTv.setVisibility(View.GONE);
        }
        if (ConstantValues.PRO.equals(mediaLibBean.getType()) &&
                !TextUtils.isEmpty(Session.get(mContext).getProPeriod()) &&
                !mediaLibBean.getPeriod().equals(Session.get(mContext).getProPeriod())) {
            viewHolder.mMediaNameTv.setTextColor(0xFF0000FF);
        } else {
            viewHolder.mMediaNameTv.setTextColor(0xFFFFFFFF);
        }
        return convertView;
    }

    public void setPlaylist(ArrayList<T> playlist) {
        mPlaylist = playlist;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView mMediaNameTv;
        TextView mIsAdsTv;
    }
}

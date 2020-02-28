package com.savor.ads.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.savor.ads.R;
import com.savor.ads.bean.AtvProgramInfo;

import java.util.ArrayList;

/**
 * 电视频道列表适配器
 * Created by zhanghq on 2016/12/12.
 */

public class ChannelListAdapter extends BaseAdapter {

    private ArrayList<AtvProgramInfo> mChannels;
    private Context mContext;

    public ChannelListAdapter(Context context, ArrayList<AtvProgramInfo> channels) {
        mContext = context;
        mChannels = channels;
    }

    @Override
    public int getCount() {
        return mChannels == null ? 0 : mChannels.size();
    }

    @Override
    public Object getItem(int position) {
        return mChannels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_channel, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.mChannelTv = (TextView) convertView.findViewById(R.id.tv_channel_name);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mChannelTv.setText(String.format("%03d %s", mChannels.get(position).getChennalNum(), mChannels.get(position).getChannelName()));
        return convertView;
    }

    class ViewHolder {
        TextView mChannelTv;
    }
}

package com.savor.ads.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.savor.ads.R;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.utils.ConstantValues;

import java.util.ArrayList;

/**
 * Created by zhang.haiqiang on 2018/1/17.
 */

public class DownloadListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<MediaLibBean> mPlaylist;

    public DownloadListAdapter(Context context, ArrayList<MediaLibBean> playlist) {
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
            convertView = View.inflate(mContext, R.layout.item_download_list, null);

            viewHolder = new ViewHolder();
            viewHolder.mMediaNameTv = (TextView) convertView.findViewById(R.id.tv_media_name);
            viewHolder.mIsAdsTv = (TextView) convertView.findViewById(R.id.tv_is_ads);
            viewHolder.mStateTv = (TextView) convertView.findViewById(R.id.tv_state);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MediaLibBean mediaLibBean = mPlaylist.get(position);
        viewHolder.mMediaNameTv.setText(/*(position + 1) + "、" + */mediaLibBean.getChinese_name());
        switch (mediaLibBean.getType()) {
            case ConstantValues.ADS:
                viewHolder.mIsAdsTv.setVisibility(View.VISIBLE);
                viewHolder.mIsAdsTv.setText("广告");
                break;
            case ConstantValues.ADV:
                viewHolder.mIsAdsTv.setVisibility(View.VISIBLE);
                viewHolder.mIsAdsTv.setText("宣传片");
                break;
            case ConstantValues.PRO:
                viewHolder.mIsAdsTv.setVisibility(View.VISIBLE);
                viewHolder.mIsAdsTv.setText("节目");
                break;
            default:
                viewHolder.mIsAdsTv.setVisibility(View.GONE);
        }

        switch (mediaLibBean.getDownload_state()) {
            case 0:
                viewHolder.mStateTv.setVisibility(View.VISIBLE);
                viewHolder.mStateTv.setText("未下载");
                viewHolder.mStateTv.setTextColor(0xFFFF0000);
                break;
            case 1:
                viewHolder.mStateTv.setVisibility(View.VISIBLE);
                viewHolder.mStateTv.setText("已下载");
                viewHolder.mStateTv.setTextColor(0xFF050505);
                break;
            case 2:
                viewHolder.mStateTv.setVisibility(View.VISIBLE);
                viewHolder.mStateTv.setText("下载中");
                viewHolder.mStateTv.setTextColor(0xFFFF0000);
                break;
            default:
                viewHolder.mStateTv.setVisibility(View.GONE);
                break;
        }

        return convertView;
    }

    public void setPlaylist(ArrayList<MediaLibBean> playlist) {
        mPlaylist = playlist;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView mMediaNameTv;
        TextView mIsAdsTv;
        TextView mStateTv;
    }
}

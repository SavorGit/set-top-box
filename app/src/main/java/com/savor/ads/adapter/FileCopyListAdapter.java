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

public class FileCopyListAdapter extends BaseAdapter {

    private ArrayList<String> fileRecord;
    private Context mContext;

    public FileCopyListAdapter(Context context,ArrayList<String> list) {
        mContext = context;
        this.fileRecord = list;
    }

    @Override
    public int getCount() {
        return fileRecord == null ? 0 : fileRecord.size();
    }

    @Override
    public Object getItem(int position) {
        return fileRecord == null?null:fileRecord.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_filecopy, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.mFileTv = convertView.findViewById(R.id.tv_file_record);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mFileTv.setText(fileRecord.get(position));
        return convertView;
    }

    public void setItemData(ArrayList<String> list){
        this.fileRecord = list;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView mFileTv;
    }
}

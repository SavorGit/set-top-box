package com.savor.ads.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.savor.ads.BuildConfig;
import com.savor.ads.R;
import com.savor.ads.bean.MiniProgramProjection;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.GlideImageLoader;

import java.util.List;

public class ProjectionImgAdapter extends BaseAdapter{

    private Context context;
    private List<MiniProgramProjection> list;
    public ProjectionImgAdapter(Context context,List<MiniProgramProjection> list){
        this.context = context;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list!=null?list.size():0;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView==null){
            convertView = View.inflate(context, R.layout.item_projection_img,null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.projection_img = (ImageView) convertView.findViewById(R.id.projection_img);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String url = list.get(position).getUrl();
        if (!TextUtils.isEmpty(url)){
            url = BuildConfig.OSS_ENDPOINT+url+ ConstantValues.PROJECTION_IMG_THUMBNAIL_PARAM;
        }
        GlideImageLoader.loadImage(context,url,viewHolder.projection_img);
        return convertView;
    }

    public void setData(List<MiniProgramProjection> datalist){
        this.list = datalist;
        notifyDataSetChanged();
    }

    class ViewHolder{
        ImageView projection_img;
    }
}

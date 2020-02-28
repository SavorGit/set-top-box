package com.savor.ads.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.savor.ads.R;
import com.savor.ads.utils.GlideImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhanghq on 2017/1/3.
 */
public class StringPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<String> mImages;

    private HashMap<Integer, View> mViewList;

    public StringPagerAdapter(Context context, List<String> images) {
        mContext = context;
        mImages = images;
        mViewList = new HashMap<>();
    }

    @Override
    public int getCount() {
        return mImages == null ? 0 : mImages.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View view = View.inflate(mContext, R.layout.view_image_item,
                null);
        ImageView imageView =view.findViewById(R.id.image);
//            imageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    ImageViewerDialog.this.dismiss();
//                }
//            });
        GlideImageLoader.loadImageWithoutCache(mContext, mImages.get(position), imageView, 0, 0);
        mViewList.put(position, view);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    public void setDataSource(ArrayList<String> usbImgPathList) {
        mImages = usbImgPathList;
        notifyDataSetChanged();
    }
}

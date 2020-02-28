package com.savor.ads.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.savor.ads.R;
import com.savor.ads.bean.PptImage;
import com.savor.ads.customview.CircleProgressBar;
import com.savor.ads.utils.AppUtils;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.GlobalValues;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 幻灯片投屏ViewPager Adapter
 * Created by zhang.haiqiang on 2017/6/13.
 */
public class PptVpAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<PptImage> pptImages;

    private HashMap<Integer, View> mViewList;

    private ImageLoadCallback imageLoadCallback;
    private ArrayList<String> specialtyImages;
    /**
     * 数据源类型：
     * 1：幻灯片
     * 2：特色菜
     */
    private int mSourceType = 1;

    public PptVpAdapter(Context mContext, ArrayList<PptImage> pptImages, ImageLoadCallback imageLoadCallback) {
        this.mContext = mContext;
        this.pptImages = pptImages;
        mViewList = new HashMap<Integer, View>();
        this.imageLoadCallback = imageLoadCallback;
    }

    @Override
    public int getCount() {
        if (1 == mSourceType) {
            if (pptImages == null) {
                return 0;
            } else {
                return pptImages.size();
            }
        } else {
            if (specialtyImages == null) {
                return 0;
            } else {
                return specialtyImages.size();
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        position = position % pptImages.size();
        container.removeView(mViewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
//        position = position % pptImages.size();
        final View view = View.inflate(mContext, R.layout.view_image_item,
                null);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        final RelativeLayout loadingRl = (RelativeLayout) view.findViewById(R.id.rl_loading_tip);
        loadingRl.setVisibility(View.VISIBLE);
        String path = null;
        if (1 == mSourceType) {
            path = pptImages.get(position).getName();
        } else {
            path = specialtyImages.get(position);
        }
        GlideImageLoader.loadImage(mContext, path, imageView, 0, 0, new RequestListener() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                loadingRl.setVisibility(View.GONE);
                imageLoadCallback.onLoadDone(position, false);
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                loadingRl.setVisibility(View.GONE);
                imageLoadCallback.onLoadDone(position, true);
                return false;
            }
        });
        mViewList.put(position, view);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setPptDataSource(ArrayList<PptImage> pptImages) {
        this.pptImages = pptImages;
        this.specialtyImages = null;
        mViewList.clear();
        notifyDataSetChanged();
    }

    public void setSpecialtyDataSource(ArrayList<String> specialtyImages) {
        this.specialtyImages = specialtyImages;
        this.pptImages = null;
        mViewList.clear();
        notifyDataSetChanged();
    }

    public void setSourceType(int sourceType) {
        mSourceType = sourceType;
    }

    public interface ImageLoadCallback {
        void onLoadDone(int position, boolean success);
    }
}

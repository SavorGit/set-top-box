package com.savor.ads.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

/**
 * Glide图片加载工具类
 * 1、调用者须确保在主线程调用load***相关方法
 * 2、应用于CircleImageView等圆形ImageView时须使用禁用动画效果
 * <p>
 * 详细说明文档参见：https://github.com/bumptech/glide
 * Created by zhanghq on 2016/6/25.
 */
public class GlideImageLoader {

    private static int globalPlaceholderResId;
    private static int globalFailedResId;

    public static void clearCache(final Context context, boolean memory, boolean disk) {
        if (memory) {
            Glide.get(context).clearMemory();
        }
        if (disk) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(context).clearDiskCache();
                }
            }).start();
        }
    }

    public static void loadCoverImage(Context context, String url, ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().frameOf(2000000).centerCrop())
                .load(url)
                .into(imageView);
    }

    public static void loadImage(Context context, String imgPath, ImageView imageView) {
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        loadImage(appContext, imgPath, imageView, globalPlaceholderResId, globalFailedResId);
    }

    public static void loadImageWithoutCache(Context context, String imgPath, ImageView imageView, int placeholderResId, int failedResId) {
        if (context == null) {
            return;
        }
        RequestOptions requestOptions = new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)//不使用硬盘缓存
                .placeholder(placeholderResId)
                .error(failedResId);
        Context appContext = context.getApplicationContext();
        Glide.with(appContext)
                .load(imgPath)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void loadImageWithoutCache(Context context, String imgPath, ImageView imageView, RequestListener listener) {
        if (context == null) {
            return;
        }
        RequestOptions requestOptions = new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)//不使用硬盘缓存
                .dontAnimate();
        Context appContext = context.getApplicationContext();
        Glide.with(appContext)
                .load(imgPath)
                .listener(listener)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void loadLocalImage(Context context, File file, ImageView imageView) {
        if (context == null) {
            return;
        }
        RequestOptions requestOptions = new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(context)
                .load(file)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void loadLocalImage(Context context, int resourceId, ImageView imageView) {
        if (context == null) {
            return;
        }
        RequestOptions requestOptions = new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(context)
                .load(resourceId)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void loadImage(Context context, String imgPath, ImageView imageView, int placeholderResId, int failedResId) {
        if (context == null) {
            return;
        }
        RequestOptions requestOptions = new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)//不使用硬盘缓存
                .placeholder(placeholderResId)
                .error(failedResId)
                .dontAnimate();
        Context appContext = context.getApplicationContext();
        Glide.with(appContext)
                    .load(imgPath)
                .apply(requestOptions)
                .into(imageView);
    }
    public static void loadImageWithDrawable(Context context, String imgPath, ImageView imageView, Drawable placeholderResId) {
        if (context==null){
            return;
        }
        RequestOptions requestOptions = new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)//不使用硬盘缓存
                .placeholder(placeholderResId)
                .dontAnimate();
        Glide.with(context)
                .load(imgPath)
                .apply(requestOptions)
                .into(imageView);
    }
    public static void loadImage(Context context, String imgPath, ImageView imageView, Drawable placeholderResId) {
        if (context==null){
            return;
        }
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .centerCrop();
        Glide.with(context)
                .load(imgPath)
                .apply(requestOptions)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }
    public static void loadImage(Context context, String imgPath, ImageView imageView, int placeholderResId, int failedResId, RequestListener listener) {
        if (!(listener instanceof RequestListener))
            throw new RuntimeException("this listener is not RequestListener type!");
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(placeholderResId)
                .error(failedResId)
                .centerCrop();
        Glide.with(context)
                .load(imgPath)
                .listener(listener)
                .apply(requestOptions)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }
    public static void loadRoundImage(Context context, String imgPath, ImageView imageView, int defaultId) {
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        loadRoundImage(appContext, imgPath, imageView, defaultId, defaultId);
    }
    public static void loadRoundImage(final Context context, String imgPath, final ImageView imageView, int placeholderResId, int failedResId) {
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(placeholderResId)
                .error(failedResId)
                .circleCrop();
        Glide.with(appContext).
                load(imgPath)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void loadRoundImageWithAngle(Context context, String imgPath, final ImageView imageView, int defaultResId,int angle){
        if (context == null) {
            return;
        }
        RequestOptions requestOptions = new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)//不使用硬盘缓存
                .placeholder(defaultResId)
                .error(defaultResId)
                .centerCrop()
                .transform(new GlideRoundTransform(context,angle));
        Context appContext = context.getApplicationContext();
        Glide.with(appContext).
                load(imgPath)
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     *
     * @param context
     * @param path 视频地址
     * @param time 微秒
     */
    public static Bitmap loadVideoScreenshot(Context context,String path,long time){
        RequestOptions requestOptions = new RequestOptions()
                .frame(time)
                .centerCrop();
        Bitmap bitmap = null;
        try {
            Bitmap myBitmap=Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .asBitmap()
                    .load(path)
                    .submit().get();

            bitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }
}

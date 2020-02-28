package com.savor.ads.customview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.savor.ads.R;
import com.savor.ads.utils.GlideImageLoader;
import com.savor.ads.utils.LogUtils;
import com.savor.ads.utils.ShowMessage;

/**
 * Created by jeanboy on 2017/4/20.
 */

public class PanelItemView extends FrameLayout implements ItemView{

    private View overlay;
    private ImageView weixinHeadTV;
    private Context mContext;
    public PanelItemView(Context context) {
        this(context, null);
    }

    public PanelItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanelItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_panel_item, this);
        mContext = context;
        overlay = findViewById(R.id.overlay);
        weixinHeadTV = (ImageView) findViewById(R.id.weixin_head);
    }

    @Override
    public void setFocus(boolean isFocused) {
        if (overlay != null) {
            overlay.setVisibility(isFocused ? INVISIBLE : VISIBLE);
        }
    }

    @Override
    public void setImageViewSrc(String uri) {
        if (weixinHeadTV!=null){
            GlideImageLoader.loadImage(mContext,uri,weixinHeadTV);
//            GlideImageLoader.loadImageWithoutCache(mContext, uri, weixinHeadTV, new RequestListener() {
//                @Override
//                public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
//                    ShowMessage.showToast(mContext, "加载邀请码失败");
//                    return false;
//                }
//
//                @Override
//                public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
//
//
//                    return false;
//                }
//            });
//            weixinHeadTV.setImageResource(src);
        }
    }

    @Override
    public Drawable getImageViewDrawable() {
        if (weixinHeadTV!=null){
            return weixinHeadTV.getDrawable();
        }
        return null;
    }
}

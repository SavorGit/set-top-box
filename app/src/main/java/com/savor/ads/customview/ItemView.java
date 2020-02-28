package com.savor.ads.customview;

import android.graphics.drawable.Drawable;

/**
 * Created by jeanboy on 2017/4/25.
 */

public interface ItemView {

    void setFocus(boolean isFocused);

    void setImageViewSrc(String uri);

    Drawable getImageViewDrawable();
}

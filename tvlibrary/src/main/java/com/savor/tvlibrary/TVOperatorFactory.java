package com.savor.tvlibrary;

import android.content.Context;

/**
 * Created by zhang.haiqiang on 2017/7/20.
 */

public class TVOperatorFactory {

    public static ITVOperator getTVOperator(Context context, TVType tvType) {
        ITVOperator itvOperator = null;
        switch (tvType) {
            case GIEC:
                itvOperator = new GiecTVOperator(context);
                break;
        }
        return itvOperator;
    }

    public enum TVType {
        GIEC,
    }
}

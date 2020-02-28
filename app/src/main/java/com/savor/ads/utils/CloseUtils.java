package com.savor.ads.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by gaowen on 2017/7/24.
 */

public class CloseUtils {

    public static void closeIO(final Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

package com.danikula.videocache.file;

import android.text.TextUtils;

import com.danikula.videocache.ProxyCacheUtils;

/**
 * Implementation of {@link FileNameGenerator} that uses MD5 of url as file name
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class MyFileNameGenerator implements FileNameGenerator {


    @Override
    public String generate(String url) {
        String extension = getExtension(url);
        return extension;
    }

    private String getExtension(String url) {
        if (!url.contains("/")){
            return null;
        }
        int slashIndex = url.lastIndexOf('/');

        return url.substring(slashIndex + 1);
    }
}

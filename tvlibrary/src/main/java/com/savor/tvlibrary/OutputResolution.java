package com.savor.tvlibrary;

/**
 * Created by zhang.haiqiang on 2017/11/1.
 */

public enum OutputResolution {
    RESOLUTION_1080p("1080p60hz"),
    RESOLUTION_720p("720p60hz"),
    RESOLUTION_576p("576p60hz");

    String strValue;
    OutputResolution(String value) {
        this.strValue = value;
    }
}

package com.savor.ads.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
/**
 * CRLF：这个参数看起来比较眼熟，它就是Win风格的换行符，意思就是使用CR LF这一对作为一行的结尾而不是Unix风格的LF
 * DEFAULT：这个参数是默认，使用默认的方法来编码
 * NO_PADDING：这个参数是略去编码字符串最后的“=”
 * NO_WRAP：这个参数意思是略去所有的换行符（设置后CRLF就没用了）
 * URL_SAFE：这个参数意思是编码时不使用对URL和文件名有特殊意义的字符来作为编码字符，具体就是以-和_取代+和
 */
public class Base64Utils {
    private static final String charsetName="utf-8";
    // 编码
    public static String getBase64(String str) {
        String result = "";
        if( str != null) {
            try {
                result = new String(Base64.encode(str.getBytes(charsetName), Base64.DEFAULT),charsetName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 解码
    public static String getFromBase64(String str) {
        String result = "";
        if (str != null) {
            try {
                result = new String(Base64.decode(str, Base64.DEFAULT), charsetName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}

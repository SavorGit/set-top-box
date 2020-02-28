/*
 * Copyright (C) 2010 mAPPn.Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.savor.ads.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.widget.TextView;

/**
 * 字符串处理工具类
 *
 * @author bichao
 * @date 2011-4-26
 * @since Version 0.7.0
 */
public class StringUtils {

    /**
     * 用来将字节转换成 16 进制表示的字符
     */
    private static final char HEX_DIGIST[] = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 从URI中获取文件名
     */
    public static String getFileNameFromUrl(final String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 格式化文件大小
     */
    public static String formatSize(long size) {
        if (size < 1048576L)
            return new DecimalFormat("##0").format((float) size / 1024f) + "K";
        else if (size < 1073741824L)
            return new DecimalFormat("###0.##").format((float) size / 1048576f) + "M";
        else
            return new DecimalFormat("#######0.##").format((float) size / 1073741824f) + "G";
    }

    /**
     * 格式化文件大小
     */
    public static String formatSize(String size) {
        return formatSize(getLong(size));
    }

    /**
     * <p>
     * Parse long value from string
     * </p>
     *
     * @param value string
     * @return long value
     */
    public static long getLong(String value) {
        if (value == null)
            return 0L;

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 格式化下载量数据
     */
    public static String getDownloadInterval(int downloadNum) {
        if (downloadNum < 50) {
            return "小于50";
        } else if (downloadNum >= 50 && downloadNum < 100) {
            return "50 - 100";
        } else if (downloadNum >= 100 && downloadNum < 500) {
            return "100 - 500";
        } else if (downloadNum >= 500 && downloadNum < 1000) {
            return "500 - 1,000";
        } else if (downloadNum >= 1000 && downloadNum < 5000) {
            return "1,000 - 5,000";
        } else if (downloadNum >= 5000 && downloadNum < 10000) {
            return "5,000 - 10,000";
        } else if (downloadNum >= 10000 && downloadNum < 50000) {
            return "10,000 - 50,000";
        } else if (downloadNum >= 50000 && downloadNum < 250000) {
            return "50,000 - 250,000";
        } else {
            return "大于250,000";
        }
    }

    /**
     * 使用十六进制字符串生成字节数组
     *
     * @param hexString 十六进制字符串
     */
    public static byte[] fromHexString(String hexString) {
        if (hexString != null && (hexString.length() & 1) == 0) {
            char[] chars = hexString.toLowerCase().toCharArray();
            byte[] bytes = new byte[chars.length >>> 1];
            for (int i = 0, h = 0, l = 0; i < bytes.length; i++) {
                h = getAsciiCode(chars[i << 1]);
                l = getAsciiCode(chars[(i << 1) + 1]);
                if (h == -1 || l == -1) {
                    return null;
                }
                bytes[i] = (byte) ((h << 4) + l);
            }
            return bytes;
        }
        return null;
    }

    /**
     * 生成十六进制字符串
     *
     * @param source      字节数组
     * @param isUpperCase 是否使用大写字母
     */
    public static String toHexString(byte[] source, boolean isUpperCase) {
        if (source == null) {
            return "";
        }
        char str[] = new char[source.length << 1];
        int k = 0;
        for (int i = 0; i < source.length; i++) {
            byte byte0 = source[i];
            str[k++] = HEX_DIGIST[byte0 >>> 4 & 0xf];
            str[k++] = HEX_DIGIST[byte0 & 0xf];
        }
        String s = new String(str);
        if (isUpperCase)
            return s.toUpperCase();
        else
            return s;
    }

    private static int getAsciiCode(char c) {
        int i = c - 48;
        i = i > 9 ? c - 87 : i;
        return i < 0 || i > 15 ? -1 : i;
    }

    public static int getStringWidth(TextView tv, String text) {
        Rect bounds = new Rect();
        TextPaint paint;

        paint = tv.getPaint();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int width = bounds.width();

        return width;
    }/**
     * 将InputStream转换成String
     *
     * @param in InputStream
     * @return String
     * @throws Exception
     */
    public static String inputStreamToString(InputStream in) throws IOException {
        int BUFFER_SIZE = 512;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
            outStream.write(data, 0, count);
        }
        data = null;
        return new String(outStream.toByteArray(), "utf-8");
    }




    private static String bytes2HexString(final byte[] bytes) {
        if (bytes == null) return null;
        int len = bytes.length;
        if (len <= 0) return null;
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = HEX_DIGIST[bytes[i] >>> 4 & 0x0f];
            ret[j++] = HEX_DIGIST[bytes[i] & 0x0f];
        }
        return new String(ret);
    }

    /**
     * MD5加密
     *
     * @param data 明文字符串
     * @return 16进制密文
     */
    public static String encryptMD5ToString(final String data) {
        return encryptMD5ToString(data.getBytes());
    }

    /**
     * MD5加密
     *
     * @param data 明文字节数组
     * @return 16进制密文
     */
    public static String encryptMD5ToString(final byte[] data) {
        return bytes2HexString(encryptMD5(data));
    }

    /**
     * MD5加密
     *
     * @param data 明文字节数组
     * @return 密文字节数组
     */
    public static byte[] encryptMD5(final byte[] data) {
        return hashTemplate(data, "MD5");
    }

    /**
     * hash加密模板
     *
     * @param data      数据
     * @param algorithm 加密算法
     * @return 密文字节数组
     */
    private static byte[] hashTemplate(final byte[] data, final String algorithm) {
        if (data == null || data.length <= 0) return null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 将字符串转成MD5值
     *
     * @param string
     * @return
     */
    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

}

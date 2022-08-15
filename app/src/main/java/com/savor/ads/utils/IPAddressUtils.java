package com.savor.ads.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IPAddressUtils {

    /**
     * (获得本机的IP地址
     *
     * @return
     */
    public static String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (android.os.Build.VERSION.SDK_INT > 10) {
                        /**android 4.0以上版本*/
                        if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                            return inetAddress.getHostAddress();
                        }
                    } else {
                        if (!inetAddress.isLoopbackAddress()) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 获取以太网 IP
     *
     * @return
     */
    @Deprecated
    public static String getEthernetIP() {
        String cmd = "busybox ifconfig eth0";
        Process process = null;
        InputStream is = null;
        BufferedReader reader = null;
        String result = "";
        try {
            process = Runtime.getRuntime().exec(cmd);
            is = process.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                if (!TextUtils.isEmpty(line) && line.trim().startsWith("inet ")) {
                    result = line.substring(line.indexOf("addr:") + 5);
                    result = result.substring(0, result.indexOf(" ")).trim();
                    break;
                }
                line = reader.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取Wlan IP
     *
     * @return
     */
    @Deprecated
    public static String getWlanIP() {
        String cmd = "busybox ifconfig wlan0";
        Process process = null;
        InputStream is = null;
        BufferedReader reader = null;
        String result = "";
        try {
            process = Runtime.getRuntime().exec(cmd);
            is = process.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                if (!TextUtils.isEmpty(line) && line.trim().startsWith("inet ")) {
                    result = line.substring(line.indexOf("addr:") + 5);
                    result = result.substring(0, result.indexOf(" ")).trim();
                    break;
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}

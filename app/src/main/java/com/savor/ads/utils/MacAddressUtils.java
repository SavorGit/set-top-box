package com.savor.ads.utils;

import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

public class MacAddressUtils {
    private static final String TAG = "MacAddressUtils";

    private MacAddressUtils() {
    }

    /**
     * 获取当前系统连接网络的网卡的mac地址
     */
    public static final String getMac() {
        byte[] mac = null;
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces != null && netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (ip.isAnyLocalAddress() || !(ip instanceof Inet4Address) || ip.isLoopbackAddress())
                        continue;
                    if (ip.isSiteLocalAddress()) {
                        mac = ni.getHardwareAddress();
                        Log.e(TAG, "获取Mac地址的网卡名：" + ni.getDisplayName());
                    } else if (!ip.isLinkLocalAddress()) {
                        mac = ni.getHardwareAddress();
                        Log.e(TAG, "获取Mac地址的网卡名：" + ni.getDisplayName());
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return getMacString(mac);
    }

    /**
     * 获取wifi模块的mac地址
     */
    public static String getWifiMac() {
        return getNetworkInterfaceMac("wlan0");
    }

    public static String getP2pMac() {
        return getNetworkInterfaceMac("p2p0");
    }

    public static String getIp6tnl0Mac() {
        return getNetworkInterfaceMac("ip6tnl0");
    }

    public static String getIpVti0Mac() {
        return getNetworkInterfaceMac("ip_vti0");
    }

    public static String getLoMac() {
        return getNetworkInterfaceMac("lo");
    }

    public static String getTeql0Mac() {
        return getNetworkInterfaceMac("teql0");
    }

    public static String getSit0Mac() {
        return getNetworkInterfaceMac("sit0");
    }

    public static String getIp6Vti0Mac() {
        return getNetworkInterfaceMac("ip6_vti0");
    }

    /**
     * 获取有线网卡模块的mac地址
     */
    public static String getEthernetMacByJavaMethod() {
        return getNetworkInterfaceMac("eth0");
    }

    /**
     * 获取指定网卡mac地址
     */
    private static String getNetworkInterfaceMac(String networkInterfaceName) {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                if (networkInterfaceName.equals(ni.getName())) {
                    return getMacString(getMacBytes(ni));
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getMacBytes(NetworkInterface ni) {
        byte[] mac = null;
        try {
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress ip = address.nextElement();
                if (ip.isAnyLocalAddress() || !(ip instanceof Inet4Address) || ip.isLoopbackAddress())
                    continue;
                if (ip.isSiteLocalAddress())
                    mac = ni.getHardwareAddress();
                else if (!ip.isLinkLocalAddress()) {
                    mac = ni.getHardwareAddress();
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return mac;
    }

    private static String getMacString(byte[] mac) {
        if (mac != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mac.length; i++) {
                sb.append(parseByte(mac[i]));
            }
            return sb.substring(0, sb.length() - 1).replace("_","").toUpperCase(Locale.ROOT);
        }
        return null;
    }

    private static String parseByte(byte b) {
        String s = "00" + Integer.toHexString(b) + "_";
        return s.substring(s.length() - 3);
    }

    /**
     * 获取以太网MAC地址
     *
     * @return
     */
    public static String getEthernetMacAddr() {
        String cmd = "busybox ifconfig eth0";
        Process process = null;
        InputStream is = null;
        BufferedReader reader = null;
        String result = "";
        try {
            process = Runtime.getRuntime().exec(cmd);
            is = process.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            if (!TextUtils.isEmpty(line)) {
                result = line.substring(line.indexOf("HWaddr") + 6).trim();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        if (TextUtils.isEmpty(result)){
            if (AppUtils.isSMART_CLOUD_TV()||AppUtils.isAmv()){
                result = getEthernetMacByIpCommand();
            }else {
                result = getEthernetMacByJavaMethod();
            }
        }
        return result;
    }

    /*
     * Load file content to String
     */
    public static String getEthernetMacByIpCommand(){
        String command = "ip a show eth0";
        Process process = null;
        InputStream is = null;
        BufferedReader reader = null;
        String result = "";
        try {
            process = Runtime.getRuntime().exec(command);
            is = process.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            //保存读取一行的变量
            String str;
            while ((str=reader.readLine())!=null){
                if (str.contains("ether")&&str.contains("brd")){
                    break;
                }
            }
            if (!TextUtils.isEmpty(str)) {
                result = StringUtils.substringBetween(str,"ether","brd").trim();
            }
            if (!TextUtils.isEmpty(result)){
                result = result.replace(":","").toUpperCase();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

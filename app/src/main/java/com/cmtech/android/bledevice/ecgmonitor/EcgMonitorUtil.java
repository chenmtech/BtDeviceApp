package com.cmtech.android.bledevice.ecgmonitor;

import android.text.TextUtils;

public class EcgMonitorUtil {
    // 创建ECG文件名：去掉'：'的macAddress + 时间 + ".bme"
    public static String makeFileName(String macAddress, long timeInMillis) {
        String address = noColon(macAddress);
        return address + timeInMillis + ".bme";
    }

    // 去掉冒号
    public static String noColon(String str) {
        if(TextUtils.isEmpty(str)) return "";

        char[] arr = str.toCharArray();      //把字符串转换为字符数组
        StringBuilder address = new StringBuilder();
        for(char c : arr) {
            if(c != ':') {
                address.append(c);
            }
        }
        return address.toString();
    }

    // 创建Ecg recordName：去掉'：'的macAddress + 时间
    public static String makeRecordName(String macAddress, long timeInMillis) {
        String address = noColon(macAddress);
        return address + timeInMillis;
    }
}

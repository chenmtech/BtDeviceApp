package com.cmtech.android.bledevice.ecgmonitor;

import java.util.Date;

public class EcgMonitorUtil {
    // 创建ECG文件名：去掉'：'的macAddress + timeinmillis + ".bme"
    public static String createFileName(String macAddress) {
        if(macAddress == null || macAddress.equals("")) return "";

        char num[] = macAddress.toCharArray();//把字符串转换为字符数组
        StringBuffer digitAddress = new StringBuffer();//把数字放到hire中
        for (int i = 0; i < num.length; i++) {
            if (num[i] != ':') {
                digitAddress.append(num[i]);// 如果输入的是数字，把它赋给hire
            }
        }
        digitAddress.append(String.valueOf(new Date().getTime())+".bme");
        return digitAddress.toString();
    }
}

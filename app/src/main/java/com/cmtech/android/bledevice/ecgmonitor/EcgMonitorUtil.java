package com.cmtech.android.bledevice.ecgmonitor;

import com.cmtech.android.bledevice.ecgmonitor.ecgfile.EcgFileHead;
import com.vise.log.ViseLog;

import java.util.Date;

public class EcgMonitorUtil {
    // 创建ECG文件名：去掉'：'的macAddress + timeinmillis + ".bme"
    public static String createFileName(String macAddress, long timeInMillis) {
        String simpleMacAddress = simpleMacAddress(macAddress);

        return simpleMacAddress + String.valueOf(timeInMillis) + ".bme";
    }

    public static String simpleMacAddress(String macAddress) {
        if(macAddress == null || macAddress.equals("")) return "";

        char num[] = macAddress.toCharArray();//把字符串转换为字符数组
        StringBuffer digitAddress = new StringBuffer();//把数字放到hire中
        for (int i = 0; i < num.length; i++) {
            if (num[i] != ':') {
                digitAddress.append(num[i]);
            }
        }
        return digitAddress.toString();
    }
}

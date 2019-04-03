package com.cmtech.android.bledevice.ecgmonitor;

public class EcgMonitorUtil {
    // 创建ECG文件名：去掉'：'的macAddress + timeinmillis + ".bme"
    public static String makeFileName(String macAddress, long timeInMillis) {
        String address = cutColonInMacAddress(macAddress);

        return address + String.valueOf(timeInMillis) + ".bme";
    }

    // 去掉macAddress中的冒号
    public static String cutColonInMacAddress(String macAddress) {
        if(macAddress == null || macAddress.equals("")) return "";

        char arr[] = macAddress.toCharArray();      //把字符串转换为字符数组
        StringBuilder address = new StringBuilder();
        for(char c : arr) {
            if(c != ':') {
                address.append(c);
            }
        }
        return address.toString();
    }
}

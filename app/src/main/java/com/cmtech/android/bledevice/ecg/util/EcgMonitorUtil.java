package com.cmtech.android.bledevice.ecg.util;

import android.text.TextUtils;

public class EcgMonitorUtil {
    // 去掉字符串中的冒号
    public static String deleteColon(String str) {
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
}

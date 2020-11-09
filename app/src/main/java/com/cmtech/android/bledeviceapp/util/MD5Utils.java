package com.cmtech.android.bledeviceapp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

//MD5加密
public class MD5Utils {

    //加密字符串
    public static String getMD5Code(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes(StandardCharsets.UTF_8));
            byte[] encryption = md5.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : encryption) {
                if (Integer.toHexString(0xff & b).length() == 1) {
                    stringBuilder.append("0").append(Integer.toHexString(0xff & b));
                } else {
                    stringBuilder.append(Integer.toHexString(0xff & b));
                }
            }
            return stringBuilder.toString();
        } catch (Exception e) {
//            e.printStackTrace();
            return "";
        }
    }
}

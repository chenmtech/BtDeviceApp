package com.cmtech.android.bledevice.core;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * UuidUtil: UUID与字符串之间的转换辅助类
 * Created by bme on 2018/3/1.
 */

public class UuidUtil {
    private UuidUtil() {

    }

    // byteArray转换为UUID
    public static UUID byteArrayToUuid(byte[] bytes) {
        byte[] tmp = new byte[bytes.length];
        for(int i = 0; i < tmp.length; i++) {
            tmp[i] = bytes[tmp.length-i-1];
        }
        ByteBuffer bb = ByteBuffer.wrap(tmp);
        return new UUID(bb.getLong(), bb.getLong());
    }

    // 16位短字符串转换为长字符串
    public static String shortToLongString(String shortString, String baseUuid) {
        if(shortString == null || shortString.length() != 4) return null;

        String sub = baseUuid.substring(4, 8);

        return baseUuid.replaceFirst(sub, shortString);
    }

    // 长字符串转换为短字符串
    public static String longToShortString(String longString) {
        return longString.substring(4, 8);
    }

    // 将16位UUID短字符串转换为UUID
    public static UUID shortStringToUuid(String shortString, String baseUuid) {
        String uuid = shortToLongString(shortString, baseUuid);
        return (uuid == null) ? null : UUID.fromString(uuid);
    }

}

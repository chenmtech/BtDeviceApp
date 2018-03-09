package com.cmtech.android.btdeviceapp.util;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by bme on 2018/3/1.
 */

public class Uuid {
    public static final String BASEUUID = "0a20XXXX-cce5-4025-a156-38ea833f6ef8";
    public static final String CCCUUID = "00002902-0000-1000-8000-00805f9b34fb";

    private Uuid() {

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

    // 短字符串转换为长字符串
    public static String shortToLongString(String shortString) {
        if(shortString == null || shortString.length() != 4) return null;

        String sub = BASEUUID.substring(4, 8);

        return BASEUUID.replaceFirst(sub, shortString);
    }

    public static String longToShortString(String longString) {
        return longString.substring(4, 8);
    }

    // 短字符串转换为UUID
    public static UUID shortStringToUuid(String shortString) {
        String uuid = shortToLongString(shortString);
        return (uuid == null) ? null : UUID.fromString(uuid);
    }
}

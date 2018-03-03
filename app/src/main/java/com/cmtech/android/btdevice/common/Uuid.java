package com.cmtech.android.btdevice.common;

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

    public static String getUuidFromByteArray(byte[] bytes) {
        byte[] tmp = new byte[bytes.length];
        for(int i = 0; i < tmp.length; i++) {
            tmp[i] = bytes[tmp.length-i-1];
        }
        ByteBuffer bb = ByteBuffer.wrap(tmp);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }

    public static String from16To128(String uuid16bits) {
        if(uuid16bits == null || uuid16bits.length() != 4) return null;

        return BASEUUID.replaceFirst("XXXX", uuid16bits);
    }

    public static UUID from16(String uuid16bits) {
        String uuid = from16To128(uuid16bits);
        return (uuid == null) ? null : UUID.fromString(uuid);
    }
}

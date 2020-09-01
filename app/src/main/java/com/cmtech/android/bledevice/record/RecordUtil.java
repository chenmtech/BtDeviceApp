package com.cmtech.android.bledevice.record;

import java.util.List;

public class RecordUtil {
    private RecordUtil() {
    }

    public static <T extends Number> String listToString(List<T> list) {
        if(list == null || list.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        for(T ele : list) {
            builder.append(ele).append(',');
        }
        return builder.toString();
    }
}

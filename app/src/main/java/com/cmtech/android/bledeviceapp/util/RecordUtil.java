package com.cmtech.android.bledeviceapp.util;

import java.lang.reflect.Constructor;
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

    public static <T extends Number> void stringToList(String str, List<T> list, Class<T> type) {
        if(str == null || list == null) return;
        list.clear();
        try {
            Constructor<T> constructor = type.getConstructor(String.class);
            constructor.setAccessible(true);
            String[] strings = str.split(",");
            for(String s : strings) {
                list.add(constructor.newInstance(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.cmtech.android.bledeviceapp.util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListStringUtil {
    private ListStringUtil() {
    }

    /**
     * to transfer the List<T> to a string
     * @param list
     * @param <T>
     * @return
     */
    public static <T extends Number> String listToString(List<T> list) {
        if(list == null || list.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        for(T ele : list) {
            builder.append(ele).append(',');
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    /**
     * to transfer the string to a List<T>
     * @param str
     * @param list
     * @param type
     * @param <T>
     */
    public static <T extends Number> void stringToList(String str, List<T> list, Class<T> type) {
        if(str == null || list == null) return;
        list.clear();
        try {
            Constructor<T> constructor = type.getConstructor(String.class);
            constructor.setAccessible(true);
            String[] strings = str.split(",");
            for(String s : strings) {
                list.add(constructor.newInstance(s.trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String strListToString(List<String> strList) {
        StringBuilder builder = new StringBuilder();
        for(String ele : strList) {
            builder.append(ele).append(',');
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    public static List<String> stringToStrList(String str) {
        List<String> strList = new ArrayList<>();
        Collections.addAll(strList, str.split(","));
        return strList;
    }
}

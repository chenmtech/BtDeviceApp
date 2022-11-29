package com.cmtech.android.bledeviceapp.util;

import java.lang.reflect.Constructor;
import java.util.List;

public class ListStringUtil {
    private ListStringUtil() {
    }

    /**
     * 将List<T>转换为String,每个元素转为String，用逗号分开
     * 但是T必须为数值型，不能为String类型
     * @param list
     * param <T>
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
     * 将String转换为一个List<T>
     * T必须为数值型，不能为String
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

    public static void stringToStrList(String str, List<String> strList) {
        strList.clear();
        String[] strArr = str.split(",");
        for(String s : strArr)
            strList.add(s.trim());
    }
}

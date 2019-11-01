package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

/**
 * EcgAppendixType: 心电附加信息类型
 * Created by bme on 2019/1/9.
 */

public enum EcgAppendixType {
    HEART_RATE(0, "心率信息"),
    NORMAL_COMMENT(1, "一般留言");

    private final int code;
    private final String description;

    EcgAppendixType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public static String getDescriptionFromCode(int code) {
        for(EcgAppendixType ele : EcgAppendixType.values()) {
            if(ele.code == code) {
                return ele.description;
            }
        }
        return "";
    }
    public static EcgAppendixType getFromCode(int code) {
        for(EcgAppendixType ele : EcgAppendixType.values()) {
            if(ele.code == code) {
                return ele;
            }
        }
        return null;
    }
}

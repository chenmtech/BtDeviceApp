package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

/**
 * EcgAppendixType: 心电附加信息类型
 * Created by bme on 2019/1/9.
 */

public enum EcgAppendixType {
    HR_INFO(0, "心率信息"),
    NORMAL_COMMENT(1, "一般留言");

    EcgAppendixType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    private int code;
    private String description;

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

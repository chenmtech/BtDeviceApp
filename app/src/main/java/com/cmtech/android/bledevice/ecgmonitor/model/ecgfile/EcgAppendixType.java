package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

/**
 * EcgAppendixType: 心电附加信息类型
 * Created by bme on 2019/1/9.
 */

public enum EcgAppendixType {
    COMMENT_NORMAL(0, "一般留言"),
    COMMENT_SYS(1, "系统留言"),
    BODY_STATUS(2, "身体状态");

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

package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

/**
 * EcgAppendixType: 心电附加信息类型
 * Created by bme on 2019/1/9.
 */

public enum EcgAppendixType {
    INVALID_APPENDIX(0, "无效附加信息"),
    NORMAL_COMMENT(1, "一般留言"),
    LOCATED_COMMENT(2, "可数据定位的留言"),
    REST_MARKER(3, "安静时间段标记");

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

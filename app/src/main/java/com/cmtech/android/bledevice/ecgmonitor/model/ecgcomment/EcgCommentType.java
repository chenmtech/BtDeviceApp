package com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment;

/**
 * EcgCommentType: 心电留言类型
 * Created by bme on 2019/1/9.
 */

public enum EcgCommentType {
    HEART_RATE(0, "心率信息"),
    NORMAL_COMMENT(1, "一般留言");

    private final int code;
    private final String description;

    EcgCommentType(int code, String description) {
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
        for(EcgCommentType ele : EcgCommentType.values()) {
            if(ele.code == code) {
                return ele.description;
            }
        }
        return "";
    }
    public static EcgCommentType getFromCode(int code) {
        for(EcgCommentType ele : EcgCommentType.values()) {
            if(ele.code == code) {
                return ele;
            }
        }
        return null;
    }
}

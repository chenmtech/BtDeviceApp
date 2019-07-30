package com.cmtech.android.bledevice.siggenerator.model;

public enum SignalType {
    NONE(0, "无信号"),
    SIN(1, "正弦"),
    SQU(2, "方波"),
    TRI(3, "三角波"),
    RND(4, "随机数");

    SignalType(int code, String description) {
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
        for(SignalType ele : SignalType.values()) {
            if(ele.code == code) {
                return ele.description;
            }
        }
        return "";
    }

    public static SignalType getFromCode(int code) {
        for(SignalType ele : SignalType.values()) {
            if(ele.code == code) {
                return ele;
            }
        }
        return null;
    }
}

package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

/**
 * EcgAbnormal: 心电异常
 * Created by bme on 2018/11/21.
 */

public enum EcgAbnormal {
    COMMENT_BUSHUFU(0, "不舒服"),
    COMMENT_XIONGMEN(1, "胸闷"),
    COMMENT_XINTIAOKUAI(2, "心跳快"),
    COMMENT_HUXIKUNNAN(3, "呼吸困难");

    EcgAbnormal(int code, String description) {
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
        for(EcgAbnormal ele : EcgAbnormal.values()) {
            if(ele.code == code) {
                return ele.description;
            }
        }
        return "";
    }

    public static EcgAbnormal getFromCode(int code) {
        for(EcgAbnormal ele : EcgAbnormal.values()) {
            if(ele.code == code) {
                return ele;
            }
        }
        return null;
    }
}

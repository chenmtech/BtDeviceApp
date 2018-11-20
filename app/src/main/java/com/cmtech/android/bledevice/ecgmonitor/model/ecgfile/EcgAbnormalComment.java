package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;


public enum EcgAbnormalComment {
    COMMENT_BUSHUFU(0, "不舒服"),
    COMMENT_XIONGMEN(1, "胸闷"),
    COMMENT_XINTIAOKUAI(2, "心跳快");

    EcgAbnormalComment(int code, String description) {
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
        for(EcgAbnormalComment ele : EcgAbnormalComment.values()) {
            if(ele.code == code) {
                return ele.description;
            }
        }
        return "";
    }

    public static EcgAbnormalComment getFromCode(int code) {
        for(EcgAbnormalComment ele : EcgAbnormalComment.values()) {
            if(ele.code == code) {
                return ele;
            }
        }
        return null;
    }
}

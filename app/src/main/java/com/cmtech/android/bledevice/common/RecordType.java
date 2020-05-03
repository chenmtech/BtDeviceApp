package com.cmtech.android.bledevice.common;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.common
 * ClassName:      RecordType
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/5/4 上午5:34
 * UpdateUser:     更新者
 * UpdateDate:     2020/5/4 上午5:34
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public enum RecordType {
    ECG("心电", 1), HR("心率", 2), THERMO("体温", 3), TH("温湿度", 4);

    private String name;
    private int code;

    RecordType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static RecordType getType(int code) {
        for (RecordType type : RecordType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

    public static String getName(int code) {
        for (RecordType type : RecordType.values()) {
            if (type.getCode() == code) {
                return type.name;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}

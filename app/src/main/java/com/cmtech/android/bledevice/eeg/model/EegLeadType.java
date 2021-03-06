package com.cmtech.android.bledevice.eeg.model;

/**
 * EcgLeadType: enumeration of ecg lead type
 * Created by bme on 2018/4/21.
 */

public enum EegLeadType {
    LEAD_I(0, "I"),
    LEAD_II(1, "II"),
    LEAD_III(2, "III");

    EegLeadType(int code, String description) {
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
        for(EegLeadType ele : EegLeadType.values()) {
            if(ele.code == code) {
                return ele.description;
            }
        }
        return "";
    }

    public static EegLeadType getFromCode(int code) {
        for(EegLeadType ele : EegLeadType.values()) {
            if(ele.code == code) {
                return ele;
            }
        }
        return null;
    }
}

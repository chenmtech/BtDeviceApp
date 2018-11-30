package com.cmtech.android.bledevicecore.model;

/**
 * BleDeviceConnectState: BleDevice连接类型
 * Created by bme on 2018/4/21.
 */

public enum BleDeviceConnectState {
    CONNECT_INIT(-1, "连接初始化"),
    CONNECT_PROCESS(0x00, "连接中"),
    CONNECT_SUCCESS(0x01, "连接成功"),
    CONNECT_FAILURE(0x02, "连接失败"),
    CONNECT_TIMEOUT(0x03, "连接超时"),
    CONNECT_DISCONNECT(0x04, "连接断开"),

    SCAN_PROCESS(0x05, "扫描中"),
    STOP(0x06, "停止");

    BleDeviceConnectState(int code, String description) {
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
        for(BleDeviceConnectState ele : BleDeviceConnectState.values()) {
            if(ele.code == code) {
                return ele.description;
            }
        }
        return "";
    }

    public static BleDeviceConnectState getFromCode(int code) {
        for(BleDeviceConnectState ele : BleDeviceConnectState.values()) {
            if(ele.code == code) {
                return ele;
            }
        }
        return null;
    }
}

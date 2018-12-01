package com.cmtech.android.bledevicecore.model;

import com.cmtech.android.bledeviceapp.R;

/**
 * BleDeviceConnectState: BleDevice连接类型
 * Created by bme on 2018/4/21.
 */

public enum BleDeviceConnectState {
    CONNECT_INIT(-1, "连接初始化", true, true, R.mipmap.ic_connect_disconnect),
    CONNECT_PROCESS(0x00, "连接中", false, false, R.drawable.connectingdrawable),
    CONNECT_SUCCESS(0x01, "连接成功", true, true, R.mipmap.ic_connect_connected),
    CONNECT_FAILURE(0x02, "连接失败", true, true, R.mipmap.ic_connect_disconnect),
    CONNECT_TIMEOUT(0x03, "连接超时", true, true, R.mipmap.ic_connect_disconnect),
    CONNECT_DISCONNECT(0x04, "连接断开", true, true, R.mipmap.ic_connect_disconnect),
    SCAN_PROCESS(0x05, "扫描中", false, false, R.drawable.connectingdrawable),
    STOP(0x06, "停止", true, true, R.mipmap.ic_connect_disconnect);

    BleDeviceConnectState(int code, String description, boolean enableConnect, boolean enableClose, int icon) {
        this.code = code;
        this.description = description;
        this.enableConnect = enableConnect;
        this.enableClose = enableClose;
        this.icon = icon;
    }

    private int code;
    private String description;
    boolean enableConnect;
    boolean enableClose;
    int icon;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnableConnect() {
        return enableConnect;
    }

    public boolean isEnableClose() {
        return enableClose;
    }

    public int getIcon() {
        return icon;
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

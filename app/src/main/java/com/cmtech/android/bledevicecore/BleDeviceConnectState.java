package com.cmtech.android.bledevicecore;

import com.cmtech.android.bledeviceapp.R;

/**
 * BleDeviceConnectState: BleDevice连接状态类型，在BLE包基础上增加了扫描和关闭两个状态
 * Created by bme on 2018/4/21.
 */

public enum BleDeviceConnectState {
    CONNECT_INIT(-1, "连接初始化", R.mipmap.ic_connect_disconnect),
    CONNECT_PROCESS(0x00, "连接中...", R.mipmap.ic_connect_3),
    CONNECT_SUCCESS(0x01, "连接成功", R.mipmap.ic_connect_connected),
    CONNECT_FAILURE(0x02, "连接错误", R.mipmap.ic_connect_disconnect),
    CONNECT_TIMEOUT(0x03, "连接超时", R.mipmap.ic_connect_disconnect),
    CONNECT_DISCONNECT(0x04, "连接断开", R.mipmap.ic_connect_disconnect),

    CONNECT_SCAN(0x05, "扫描中...", R.mipmap.ic_connect_3),
    CONNECT_CLOSED(0x06, "连接关闭", R.mipmap.ic_connect_disconnect);

    /**
     *
     * @param code
     * @param description
     * @param icon：该状态下在Toobar上显示的图标或动画
     */
    BleDeviceConnectState(int code, String description, int icon) {
        this.code = code;
        this.description = description;
        this.icon = icon;
    }

    private int code;
    private String description;
    private int icon;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
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

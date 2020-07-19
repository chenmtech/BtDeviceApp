package com.cmtech.android.ble.core;

public class WebDeviceCommonInfo extends DeviceCommonInfo {
    private final String broadcastId; // 该网络设备的广播者ID
    private String broadcastName = "";

    public WebDeviceCommonInfo(String address, String uuid, String broadcastId) {
        super(address, uuid, false);
        this.broadcastId = broadcastId;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public String getBroadcastName() {
        return broadcastName;
    }

    public void setBroadcastName(String broadcastName) {
        this.broadcastName = broadcastName;
    }

}

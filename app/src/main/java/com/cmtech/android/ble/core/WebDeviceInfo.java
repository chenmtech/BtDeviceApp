package com.cmtech.android.ble.core;

public class WebDeviceInfo extends DeviceInfo {
    private final String broadcastId; // 该网络设备的广播者ID
    private String broadcastName = "";

    public WebDeviceInfo(String address, String uuid, String broadcastId) {
        super(address, uuid);
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

    @Override
    public boolean isLocal() {
        return false;
    }
}

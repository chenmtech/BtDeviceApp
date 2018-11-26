package com.cmtech.android.bledevicecore.model;

public interface IBleDeviceFragmentActivity {
    // Activity必须有能力关闭Fragment对应的设备，并销毁Fragment
    void closeDevice(BleDeviceFragment fragment);
    // 可以由设备的mac地址找到BleDevice对象
    BleDevice getDeviceByMac(String mac);
}

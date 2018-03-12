package com.cmtech.android.btdeviceapp.fragment;

import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;

/**
 * Created by bme on 2018/3/12.
 */

public interface IDeviceFragmentObserver {
    // 用Fragment找到对应的Device
    MyBluetoothDevice findDeviceFromFragment(DeviceFragment fragment);

    // 关闭Fragment及其对应的Device
    void closeFragmentAndDevice(DeviceFragment fragment);
}

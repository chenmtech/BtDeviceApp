package com.cmtech.android.btdeviceapp.fragment;

import com.cmtech.android.btdeviceapp.model.IMyBluetoothDeviceObserver;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;

/**
 * Created by bme on 2018/3/12.
 */

public interface IDeviceFragment extends IMyBluetoothDeviceObserver {
    public interface IDeviceFragmentListener {
        // 用Fragment找到相应的Device
        MyBluetoothDevice findDeviceFromFragment(DeviceFragment fragment);
        // 关闭Fragment及其相应的Device
        void closeFragmentAndDevice(DeviceFragment fragment);
    }
}

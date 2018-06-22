package com.cmtech.android.btdeviceapp.interfa;

import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;

/**
 * DeviceFragment观察者，一般是创建Fragment的Activity
 * Created by bme on 2018/3/12.
 */

public interface IDeviceFragmentObserver {

    // 用Fragment找到对应的Device
    MyBluetoothDevice findDevice(DeviceFragment fragment);

    // 用Fragment找到对应的Controller
    BLEDeviceController findController(DeviceFragment fragment);

    // 删除Fragment
    void deleteFragment(DeviceFragment fragment);
}

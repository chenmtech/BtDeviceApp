package com.cmtech.android.bledeviceapp;

import com.cmtech.android.ble.BleConfig;
import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.bledeviceapp.model.BleDeviceType;

/**
 * BleDeviceConfig: 进行一些初始化和配置
 * Created by bme on 2018/10/22.
 */

public class BleDeviceConfig {
    // 添加支持的设备类型
    public static void addSupportedDeviceType(BleDeviceType deviceType) {
        BleDeviceType.addSupportedType(deviceType);
    }

    // 设置设备状态的文字描述
    public static void setStateDescription(BleDeviceState deviceState, String description) {
        deviceState.setDescription(description);
    }

    // 设置设备状态的显示图标
    public static void setStateIcon(BleDeviceState deviceState, int icon) {
        deviceState.setIcon(icon);
    }

}

package com.cmtech.android.bledeviceapp;

import com.cmtech.android.ble.core.DeviceState;
import com.cmtech.android.bledeviceapp.model.DeviceType;

/**
 * DeviceConfig: 进行一些设备配置
 * Created by bme on 2018/10/22.
 */

public class DeviceConfig {
    // 添加支持的设备类型
    public static void addSupportedDeviceType(DeviceType deviceType) {
        DeviceType.addSupportedType(deviceType);
    }

    // 设置设备状态的文字描述
    public static void setStateDescription(DeviceState deviceState, String description) {
        deviceState.setDescription(description);
    }

    // 设置设备状态的显示图标
    public static void setStateIcon(DeviceState deviceState, int icon) {
        deviceState.setIcon(icon);
    }

}

package com.cmtech.android.bledevicecore.model;

/**
 * Created by bme on 2018/10/22.
 */

public class BleDeviceConfig {
    private static BleDeviceConfig instance;  //入口操作管理

    private BleDeviceConfig() {
    }

    /**
     * 单例
     *
     * @return 返回BleDeviceConfig
     */
    public static BleDeviceConfig getInstance() {
        if (instance == null) {
            synchronized (BleDeviceConfig.class) {
                if (instance == null) {
                    instance = new BleDeviceConfig();
                }
            }
        }
        return instance;
    }

}

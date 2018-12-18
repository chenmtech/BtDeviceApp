package com.cmtech.android.bledevicecore;

import com.cmtech.android.ble.ViseBle;
import com.cmtech.android.ble.common.BleConfig;
import com.cmtech.android.bledeviceapp.MyApplication;

import static com.cmtech.android.bledevicecore.BleDeviceConstant.MY_BASE_UUID;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.OPDATA_RETRY_COUNT;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.SCAN_DEVICE_NAME;

/**
 * Created by bme on 2018/10/22.
 */

public class BleDeviceConfig {
    private static BleDeviceConfig instance;  //入口操作管理

    // ViseBle单件实例
    private ViseBle viseBle;

    // 扫描时会过滤的设备名称
    private String scanDeviceName = SCAN_DEVICE_NAME;
    public String getScanDeviceName() {
        return scanDeviceName;
    }

    // 采用的基础UUID
    private String baseUuid = MY_BASE_UUID;
    public String getBaseUuid() {
        return baseUuid;
    }

    private BleDeviceConfig() {
        viseBle = ViseBle.getInstance();
        viseBle.init(MyApplication.getContext());
        BleConfig.getInstance().setOperateRetryCount(OPDATA_RETRY_COUNT);
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

    // 配置基础UUID
    public BleDeviceConfig setBaseUuid(String baseUuid) {
        this.baseUuid = baseUuid;
        return this;
    }

    // 配置扫描超时时间
    public BleDeviceConfig setScanTimeout(int scanTimeout) {
        BleConfig.getInstance().setScanTimeout(scanTimeout);
        return this;
    }

    // 配置连接超时时间
    public BleDeviceConfig setConnectTimeout(int connectTimeout) {
        BleConfig.getInstance().setConnectTimeout(connectTimeout);
        return this;
    }

    // 配置重连时间间隔
    public BleDeviceConfig setReconnectInterval(int reconnectInterval) {
        BleConfig.getInstance().setConnectRetryInterval(reconnectInterval);
        return this;
    }

    // 配置重连次数
    public BleDeviceConfig setConnectRetryCount(int connectRetryCount) {
        BleConfig.getInstance().setConnectRetryCount(connectRetryCount);
        return this;
    }

    // 配置扫描过滤设备名
    public BleDeviceConfig setScanDeviceName(String scanDeviceName) {
        this.scanDeviceName = scanDeviceName;
        return this;
    }

}

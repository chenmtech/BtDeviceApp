package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.btdevice.common.BluetoothGattElement;
import com.cmtech.android.btdevice.common.Uuid;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

/**
 * Created by bme on 2018/3/1.
 */

public class ThermoManager {
    private static final String thermoServiceUuid = "aa30";     // 体温计服务UUID:aa30
    private static final String thermoDataUuid = "aa31";        // 体温数据特征UUID:aa31
    private static final String thermoControlUuid = "aa31";     // 体温测量控制UUID:aa32
    private static final String thermoPeriodUuid = "aa32";      // 体温采样周期UUID:aa33

    public static final BluetoothGattElement THERMODATA =
            new BluetoothGattElement(thermoServiceUuid, thermoDataUuid, null);

    public static final BluetoothGattElement THERMOCTL =
            new BluetoothGattElement(thermoServiceUuid, thermoControlUuid, null);

    public static final BluetoothGattElement THERMOPERIOD =
            new BluetoothGattElement(thermoServiceUuid, thermoPeriodUuid, null);

    public static final BluetoothGattElement THERMODATACCC =
            new BluetoothGattElement(thermoServiceUuid, thermoDataUuid, Uuid.CCCUUID);

    public Object find(ConfiguredDevice device, BluetoothGattElement element) {
        if(device == null || element == null) return null;
        return element.find(device.getDeviceMirror());
    }
}

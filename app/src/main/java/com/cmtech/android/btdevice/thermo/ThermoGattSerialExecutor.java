package com.cmtech.android.btdevice.thermo;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.btdevice.common.DeviceGattSerialExecutor;
import com.cmtech.android.btdevice.common.BluetoothGattElement;
import com.cmtech.android.btdevice.common.Uuid;

/**
 * Created by bme on 2018/3/1.
 */

public class ThermoGattSerialExecutor extends DeviceGattSerialExecutor {
    private static final String thermoServiceUuid = "aa30";     // 体温计服务UUID:aa30
    private static final String thermoDataUuid = "aa31";        // 体温数据特征UUID:aa31
    private static final String thermoControlUuid = "aa32";     // 体温测量控制UUID:aa32
    private static final String thermoPeriodUuid = "aa33";      // 体温采样周期UUID:aa33

    public static final BluetoothGattElement THERMODATA =
            new BluetoothGattElement(thermoServiceUuid, thermoDataUuid, null);

    public static final BluetoothGattElement THERMOCONTROL =
            new BluetoothGattElement(thermoServiceUuid, thermoControlUuid, null);

    public static final BluetoothGattElement THERMOPERIOD =
            new BluetoothGattElement(thermoServiceUuid, thermoPeriodUuid, null);

    public static final BluetoothGattElement THERMODATACCC =
            new BluetoothGattElement(thermoServiceUuid, thermoDataUuid, Uuid.CCCUUID);

    public ThermoGattSerialExecutor(DeviceMirror deviceMirror) {
        super(deviceMirror);
    }

}

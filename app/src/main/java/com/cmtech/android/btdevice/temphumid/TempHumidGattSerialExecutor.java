package com.cmtech.android.btdevice.temphumid;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.btdevice.common.BluetoothGattElement;
import com.cmtech.android.btdevice.common.DeviceGattSerialExecutor;
import com.cmtech.android.btdevice.common.Uuid;

/**
 * Created by bme on 2018/3/1.
 */

public class TempHumidGattSerialExecutor extends DeviceGattSerialExecutor {
    private static final String tempHumidServiceUuid = "aa60";     // 温湿度计服务UUID:aa60
    private static final String tempHumidDataUuid = "aa61";        // 温湿度数据特征UUID:aa61
    private static final String tempHumidCtrlUuid = "aa62";     // 测量控制UUID:aa62
    private static final String tempHumidPeriodUuid = "aa63";      // 采样周期UUID:aa63

    public static final BluetoothGattElement TEMPHUMIDDATA =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidDataUuid, null);

    public static final BluetoothGattElement TEMPHUMIDCTRL =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidCtrlUuid, null);

    public static final BluetoothGattElement TEMPHUMIDPERIOD =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidPeriodUuid, null);

    public static final BluetoothGattElement TEMPHUMIDDATACCC =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidDataUuid, Uuid.CCCUUID);

    public TempHumidGattSerialExecutor(DeviceMirror deviceMirror) {
        super(deviceMirror);
    }

}

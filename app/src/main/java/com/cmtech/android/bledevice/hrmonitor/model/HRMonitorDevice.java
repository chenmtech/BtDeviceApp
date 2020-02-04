package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.util.UUID;

import static com.cmtech.android.bledeviceapp.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.MY_BASE_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.STANDARD_BLE_UUID;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      HRMonitorDevice
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020-02-04 06:16
 * UpdateUser:     更新者
 * UpdateDate:     2020-02-04 06:16
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HRMonitorDevice extends AbstractDevice {
    private static final String hrMonitorServiceUuid    = "180D";           // 标准心率计服务UUID
    private static final String hrMonitorMeasurementUuid  = "2A37";           // 心率测量特征UUID
    private static final String hrMonitorSensLocUuid   = "2A38";           // 测量位置UUID
    private static final String hrMonitorCtrlPtUuid     = "2A39";           // 控制点UUID

    private static final UUID hrMonitorServiceUUID    = UuidUtil.stringToUuid(hrMonitorServiceUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorMeasurementUUID       = UuidUtil.stringToUuid(hrMonitorMeasurementUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorSensLocUUID   = UuidUtil.stringToUuid(hrMonitorSensLocUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorCtrlPtUUID     = UuidUtil.stringToUuid(hrMonitorCtrlPtUuid, STANDARD_BLE_UUID);

    private static final BleGattElement HRMONITORMEAS =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorMeasurementUUID, null, "心率测量值");
    private static final BleGattElement HRMONITORMEASCCC =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorMeasurementUUID, CCC_UUID, "心率测量CCC");
    private static final BleGattElement HRMONITORSENSLOC =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorSensLocUUID, null, "测量位置");
    private static final BleGattElement HRMONITORCTRLPT =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorCtrlPtUUID, null, "控制点");

    private IHRMonitorDeviceListener listener;

    public HRMonitorDevice(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public boolean onConnectSuccess() {
        BleGattElement[] elements = new BleGattElement[]{HRMONITORMEAS, HRMONITORMEASCCC, HRMONITORSENSLOC};
        if(!((BleDeviceConnector)connector).containGattElements(elements)) {
            return false;
        }

        readSensorLocation();

        startHRMeasure();

        return true;
    }

    @Override
    public void onConnectFailure() {

    }

    @Override
    public void onDisconnect() {

    }

    public void setListener(IHRMonitorDeviceListener listener) {
        this.listener = listener;
    }

    private void readSensorLocation() {
        ((BleDeviceConnector)connector).read(HRMONITORSENSLOC, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                int sensLoc = data[0];

                if(listener != null) {
                    listener.onHRSensLocUpdated(sensLoc);
                }
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void startHRMeasure() {
        IBleDataCallback notifyCallback = new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                if(listener != null) {
                    listener.onHRMeasureUpdated(data);
                }
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };

        ((BleDeviceConnector)connector).notify(HRMONITORMEASCCC, true, notifyCallback);
    }
}

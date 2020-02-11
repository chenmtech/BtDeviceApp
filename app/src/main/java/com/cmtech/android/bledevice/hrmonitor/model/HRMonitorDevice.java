package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;

import java.util.UUID;

import static com.cmtech.android.bledeviceapp.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.STANDARD_BLE_UUID;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      HRMonitorDevice
 * Description:    心率计设备
 * Author:         作者名
 * CreateDate:     2020-02-04 06:16
 * UpdateUser:     更新者
 * UpdateDate:     2020-02-04 06:16
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HRMonitorDevice extends AbstractDevice {
    private static final String hrMonitorServiceUuid = "180D"; // 标准心率计服务UUID
    private static final String hrMonitorMeasUuid = "2A37"; // 心率测量特征UUID
    private static final String hrMonitorSensLocUuid = "2A38"; // 测量位置UUID
    private static final String hrMonitorCtrlPtUuid = "2A39"; // 控制点UUID

    private static final UUID hrMonitorServiceUUID = UuidUtil.stringToUuid(hrMonitorServiceUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorMeasUUID = UuidUtil.stringToUuid(hrMonitorMeasUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorSensLocUUID = UuidUtil.stringToUuid(hrMonitorSensLocUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorCtrlPtUUID = UuidUtil.stringToUuid(hrMonitorCtrlPtUuid, STANDARD_BLE_UUID);

    private static final BleGattElement HRMONITORMEAS =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorMeasUUID, null, "心率测量值");
    private static final BleGattElement HRMONITORMEASCCC =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorMeasUUID, CCC_UUID, "心率测量CCC");
    private static final BleGattElement HRMONITORSENSLOC =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorSensLocUUID, null, "测量位置");
    private static final BleGattElement HRMONITORCTRLPT =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorCtrlPtUUID, null, "控制点");

    private static final String battServiceUuid = "180F";
    private static final String battLevelUuid = "2A19";
    private static final UUID battServiceUUID = UuidUtil.stringToUuid(battServiceUuid, STANDARD_BLE_UUID);
    private static final UUID battLevelUUID = UuidUtil.stringToUuid(battLevelUuid, STANDARD_BLE_UUID);
    private static final BleGattElement BATTLEVEL = new BleGattElement(battServiceUUID, battLevelUUID, null, "电池电量百分比");
    private static final BleGattElement BATTLEVELCCC = new BleGattElement(battServiceUUID, battLevelUUID, CCC_UUID, "电池电量CCC");

    private IHRMonitorDeviceListener listener;

    public HRMonitorDevice(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public boolean onConnectSuccess() {
        BleDeviceConnector connector = (BleDeviceConnector)this.connector;

        BleGattElement[] elements = new BleGattElement[]{HRMONITORMEAS, HRMONITORMEASCCC};
        if(!connector.containGattElements(elements)) {
            return false;
        }

        if(connector.containGattElement(HRMONITORSENSLOC)) {
            readSensorLocation();
        }

        startHRMeasure();

        elements = new BleGattElement[]{BATTLEVEL, BATTLEVELCCC};
        if(connector.containGattElements(elements)) {

        }

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

    public void removeListener() {
        this.listener = null;
    }

    private void readSensorLocation() {
        ((BleDeviceConnector)connector).read(HRMONITORSENSLOC, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                if(listener != null) {
                    listener.onHRSensLocUpdated(data[0]);
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
                try {
                    BleHeartRateData heartRateData = new BleHeartRateData(data);
                    if(listener != null) {
                        listener.onHRMeasureUpdated(heartRateData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };
        ((BleDeviceConnector)connector).notify(HRMONITORMEASCCC, false, null);

        ((BleDeviceConnector)connector).notify(HRMONITORMEASCCC, true, notifyCallback);
    }


}

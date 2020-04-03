package com.cmtech.android.bledevice.thermo.model;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.vise.log.ViseLog;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import static com.cmtech.android.bledeviceapp.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.STANDARD_BLE_UUID;

/**
 * ThermoDevice: Thermometer device
 * Created by bme on 2018/9/20.
 */


public class ThermoDevice extends AbstractDevice {
    private static final String thermoServiceUuid = "1809"; // thermometer service UUID
    private static final String thermoTempUuid = "2A1C"; // temperature measurement UUID
    private static final String thermoTypeUuid = "2A1D"; // temperature type UUID
    private static final String thermoIntervalUuid = "2A21"; // measurement interval UUID
    private static final String thermoIRangeUuid = "2906"; // measurement interval range UUID

    private static final UUID thermoServiceUUID = UuidUtil.stringToUUUID(thermoServiceUuid, STANDARD_BLE_UUID);
    private static final UUID thermoTempUUID = UuidUtil.stringToUUUID(thermoTempUuid, STANDARD_BLE_UUID);
    private static final UUID thermoTypeUUID = UuidUtil.stringToUUUID(thermoTypeUuid, STANDARD_BLE_UUID);
    private static final UUID thermoIntervalUUID = UuidUtil.stringToUUUID(thermoIntervalUuid, STANDARD_BLE_UUID);
    private static final UUID thermoIRangeUUID = UuidUtil.stringToUUUID(thermoIRangeUuid, STANDARD_BLE_UUID);

    private static final BleGattElement THERMOTEMP =
            new BleGattElement(thermoServiceUUID, thermoTempUUID, null, "体温值");
    private static final BleGattElement THERMOTEMPCCC =
            new BleGattElement(thermoServiceUUID, thermoTempUUID, CCC_UUID, "体温CCC");
    private static final BleGattElement THERMOTYPE =
            new BleGattElement(thermoServiceUUID, thermoTypeUUID, null, "体温类型");
    private static final BleGattElement THERMOINTERVAL =
            new BleGattElement(thermoServiceUUID, thermoIntervalUUID, null, "测量间隔(s)");
    private static final BleGattElement THERMOIRANGE =
            new BleGattElement(thermoServiceUUID, thermoIntervalUUID, thermoIRangeUUID, "测量间隔范围");

    private static final short DEFAULT_MEAS_INTERVAL = 2;

    private OnThermoDeviceListener listener;

    private float highestTemp = 0.0f;
    private BleThermoRecord10 record;
    private boolean isRecord = false;

    public ThermoDevice(DeviceInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public boolean onConnectSuccess() {
        BleGattElement[] elements = new BleGattElement[]{THERMOTEMP, THERMOTEMPCCC};
        if(!((BleConnector)connector).containGattElements(elements)) {
            return false;
        }

        // read temp type, which means the location of the thermometer on the body
        if(((BleConnector) connector).containGattElement(THERMOTYPE)) {
            ((BleConnector) connector).read(THERMOTYPE, new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    ViseLog.e("The temperature type is " + data[0]);
                    if(listener != null)
                        listener.onTempTypeUpdated(data[0]);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            });
        }

        // read measure interval
        if(((BleConnector) connector).containGattElement(THERMOINTERVAL)) {
            ((BleConnector) connector).read(THERMOINTERVAL, new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    int interval = ByteUtil.getShort(data);
                    ViseLog.e("The measurement interval data is " + data[0] + " " + data[1] + Arrays.toString(data));
                    if(listener != null)
                        listener.onMeasIntervalUpdated(interval);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            });
        }

        startTempMeasurement();

        return true;
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onConnectFailure() {

    }

    public void restart() {
        highestTemp = 0.0f;
    }

    public boolean isRecord() {
        return isRecord;
    }

    public void setRecord(boolean isRecord) {
        if(this.isRecord == isRecord) return;

        if(isRecord) {
            record = BleThermoRecord10.create(new byte[]{0x01,0x00}, getAddress(), AccountManager.getInstance().getAccount());
        } else {
            if(record != null) {
                record.setCreateTime(new Date().getTime());
                record.save();
                record = null;
            }
        }
        this.isRecord = isRecord;
        if(listener != null) {
            listener.onRecordStatusUpdated(isRecord);
        }
    }

    // 登记体温数据观察者
    public void registerListener(OnThermoDeviceListener listener) {
        this.listener = listener;
    }

    // 删除体温数据观察者
    public void removeListener() {
        this.listener = null;
    }

    private void startTempMeasurement() {
        IBleDataCallback indicateCallback = new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                byte[] tempArr = new byte[]{data[1],data[2],data[3],data[4]};
                float temp = ByteUtil.getFloat(tempArr);
                ViseLog.e("The temperature data is " + Arrays.toString(data) + "temp = " + temp);
                if(listener != null) {
                    listener.onTempUpdated(temp);

                }
                if(isRecord && record != null) {
                    record.addTemp(temp);
                }
                if(highestTemp < temp) {
                    highestTemp = temp;
                    if(listener != null)
                        listener.onHighestTempUpdated(highestTemp);
                    if(isRecord && record != null) {
                        record.setHighestTemp(highestTemp);
                    }
                }
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };
        ((BleConnector)connector).indicate(THERMOTEMPCCC, true, indicateCallback);
    }
}

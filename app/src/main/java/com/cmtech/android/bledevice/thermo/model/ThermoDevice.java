package com.cmtech.android.bledevice.thermo.model;

import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.DEFAULT_RECORD_VER;
import static com.cmtech.android.bledeviceapp.global.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.STANDARD_BLE_UUID;

import android.content.Context;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BleThermoRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordFactory;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    private static final UUID thermoServiceUUID = UuidUtil.stringToUUID(thermoServiceUuid, STANDARD_BLE_UUID);
    private static final UUID thermoTempUUID = UuidUtil.stringToUUID(thermoTempUuid, STANDARD_BLE_UUID);
    private static final UUID thermoTypeUUID = UuidUtil.stringToUUID(thermoTypeUuid, STANDARD_BLE_UUID);
    private static final UUID thermoIntervalUUID = UuidUtil.stringToUUID(thermoIntervalUuid, STANDARD_BLE_UUID);
    private static final UUID thermoIRangeUUID = UuidUtil.stringToUUID(thermoIRangeUuid, STANDARD_BLE_UUID);

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

    private OnThermoListener listener;

    private float highestTemp = 0.0f;
    private BleThermoRecord record;
    private boolean isRecord = false;

    public ThermoDevice(Context context, DeviceCommonInfo registerInfo) {
        super(context, registerInfo);
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
        if(listener != null)
            listener.onHighestTempUpdated(highestTemp);
    }

    public boolean isRecord() {
        return isRecord;
    }

    public BleThermoRecord getRecord() {
        return record;
    }

    public void setRecord(boolean isRecord) {
        if(this.isRecord == isRecord) return;

        if(isRecord) {
            record = (BleThermoRecord) RecordFactory.create(RecordType.THERMO, DEFAULT_RECORD_VER, MyApplication.getAccountId(), new Date().getTime(), getAddress(),
                    1, 1, "1", "C");
            Toast.makeText(MyApplication.getContext(), R.string.start_record, Toast.LENGTH_SHORT).show();
        } else {
            if(record != null) {
                record.save();
                Toast.makeText(MyApplication.getContext(), R.string.save_record_success, Toast.LENGTH_SHORT).show();
                record = null;
            }
        }
        this.isRecord = isRecord;
        if(listener != null) {
            listener.onRecordStatusUpdated(isRecord);
        }
    }

    // 登记体温数据观察者
    public void registerListener(OnThermoListener listener) {
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
                    setNotificationInfo(MyApplication.getStr(R.string.temperature_of_body) + highestTemp + MyApplication.getStr(R.string.temperature));
                }
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };
        ((BleConnector)connector).indicate(THERMOTEMPCCC, true, indicateCallback);
    }
}

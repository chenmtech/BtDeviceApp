package com.cmtech.android.bledevice.thermo.model;

import android.os.Handler;
import android.os.Looper;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.vise.log.ViseLog;

import java.util.LinkedList;
import java.util.List;
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

    private static final UUID thermoServiceUUID       = UuidUtil.stringToUuid(thermoServiceUuid, STANDARD_BLE_UUID);
    private static final UUID thermoTempUUID          = UuidUtil.stringToUuid(thermoTempUuid, STANDARD_BLE_UUID);
    private static final UUID thermoTypeUUID       = UuidUtil.stringToUuid(thermoTypeUuid, STANDARD_BLE_UUID);
    private static final UUID thermoIntervalUUID        = UuidUtil.stringToUuid(thermoIntervalUuid, STANDARD_BLE_UUID);
    private static final UUID thermoIRangeUUID        = UuidUtil.stringToUuid(thermoIRangeUuid, STANDARD_BLE_UUID);

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

    private final List<OnThermoDeviceListener> thermoListeners = new LinkedList<>();

    public ThermoDevice(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public boolean onConnectSuccess() {
        BleGattElement[] elements = new BleGattElement[]{THERMOTEMP, THERMOTEMPCCC};
        if(!((BleDeviceConnector)connector).containGattElements(elements)) {
            return false;
        }

        if(((BleDeviceConnector) connector).containGattElement(THERMOTYPE)) {
            ((BleDeviceConnector) connector).read(THERMOTYPE, new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    ViseLog.e("The temperature type is " + data[0]);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            });
        }

        if(((BleDeviceConnector) connector).containGattElement(THERMOINTERVAL)) {
            ((BleDeviceConnector) connector).read(THERMOINTERVAL, new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    short interval = ByteUtil.getShort(data);
                    ViseLog.e("The measurement interval is " + interval);
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

    // 登记体温数据观察者
    public void registerListener(OnThermoDeviceListener listener) {
        if(!thermoListeners.contains(listener)) {
            thermoListeners.add(listener);
        }
    }

    // 删除体温数据观察者
    public void removeListener(OnThermoDeviceListener listener) {
        int index = thermoListeners.indexOf(listener);
        if(index >= 0) {
            thermoListeners.remove(index);
        }
    }

    private void startTempMeasurement() {
        IBleDataCallback indicateCallback = new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                float temp = ByteUtil.getShort(data)/100.0f;

                updateTemperature(temp);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };
        ((BleDeviceConnector)connector).indicate(THERMOTEMPCCC, true, indicateCallback);
    }

    //
    private void updateTemperature(final float temp) {
        for(final OnThermoDeviceListener listener : thermoListeners) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(listener != null)
                        listener.onTemperatureUpdated(temp);
                }
            });
        }
    }
}

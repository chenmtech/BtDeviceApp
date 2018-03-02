package com.cmtech.android.btdevice.common;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.HexUtil;
import com.cmtech.android.btdevice.thermo.ThermoManager;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.cmtech.android.btdevice.thermo.ThermoManager.THERMOPERIOD;

/**
 * Created by bme on 2018/3/2.
 */

public class DeviceManager {
    private final DeviceMirror deviceMirror;
    private Queue<BluetoothGattCommand> commandList = new LinkedList<>();

    public DeviceManager(DeviceMirror deviceMirror) {
        this.deviceMirror = deviceMirror;
    }

    public DeviceMirror getDeviceMirror() {return deviceMirror;}

    public Object findElement(BluetoothGattElement element) {
        if(deviceMirror == null || element == null) return null;
        return element.retrieve(deviceMirror);
    }

    public boolean readElement(BluetoothGattElement element, IBleCallback dataOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_READ)
                .setDataOpCallback(dataOpCallback).build();
        if(command == null) return false;
        return addCommandToList(command);
    }

    public boolean writeElement(BluetoothGattElement element, byte[] data, IBleCallback dataOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setData(data)
                .setDataOpCallback(dataOpCallback).build();
        if(command == null) return false;
        return addCommandToList(command);
    }

    private synchronized boolean addCommandToList(BluetoothGattCommand command) {
        boolean flag = commandList.offer(command);
        if(!flag) notifyAll();
        return flag;
    }

}

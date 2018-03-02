package com.cmtech.android.btdevice.common;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.cmtech.android.ble.core.DeviceMirror;

import java.util.UUID;

/**
 * Created by bme on 2018/3/1.
 */

public class BluetoothGattElement {
    private final int TYPE_NOTHING = 0;
    private final int TYPE_SERVICE = 1;
    private final int TYPE_CHARACTERISTIC = 2;
    private final int TYPE_DESCRIPTOR = 3;

    private UUID serviceUuid;
    private UUID characteristicUuid;
    private UUID descriptorUuid;

    public BluetoothGattElement(String serviceUuid, String characteristicUuid, String descriptorUuid) {
        this(Uuid.from16(serviceUuid), Uuid.from16(characteristicUuid), Uuid.from16(descriptorUuid));
    }

    public BluetoothGattElement(UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid) {
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
        this.descriptorUuid = descriptorUuid;
    }

    public UUID getServiceUuid() {
        return serviceUuid;
    }

    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    public UUID getDescriptorUuid() {
        return descriptorUuid;
    }

    public Object retrieve(DeviceMirror deviceMirror) {
        if(deviceMirror == null || deviceMirror.getBluetoothGatt() == null) return null;
        BluetoothGatt gatt = deviceMirror.getBluetoothGatt();
        BluetoothGattService service;
        BluetoothGattCharacteristic characteristic;
        BluetoothGattDescriptor descriptor;

        Object element = null;
        if( (service = gatt.getService(serviceUuid)) != null) {
            element = service;
            if( (characteristic = service.getCharacteristic(characteristicUuid)) != null ) {
                element = characteristic;
                if( (descriptor = characteristic.getDescriptor(descriptorUuid)) != null ) {
                    element = descriptor;
                }
            }
        }
        return element;
    }

    public int getType() {
        if(descriptorUuid != null) return TYPE_DESCRIPTOR;
        if(characteristicUuid != null) return TYPE_CHARACTERISTIC;
        if(serviceUuid != null) return TYPE_SERVICE;
        return TYPE_NOTHING;
    }

    @Override
    public String toString() {
        return "BluetoothGattElement{" +
                "serviceUuid=" + serviceUuid +
                ", characteristicUuid=" + characteristicUuid +
                ", descriptorUuid=" + descriptorUuid +
                '}';
    }
}

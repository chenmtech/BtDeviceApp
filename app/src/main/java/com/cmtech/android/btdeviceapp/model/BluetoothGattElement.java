package com.cmtech.android.btdeviceapp.model;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.btdeviceapp.util.Uuid;

import java.util.UUID;

/**
 * Created by bme on 2018/3/1.
 */

public class BluetoothGattElement {
    private final int TYPE_NULL = 0;                // 空ELement
    private final int TYPE_SERVICE = 1;             // service element
    private final int TYPE_CHARACTERISTIC = 2;      // characteristic element
    private final int TYPE_DESCRIPTOR = 3;          // descriptor element

    // null element
    private static final BluetoothGattElement NULLELEMENT = new BluetoothGattElement((UUID)null, null, null);

    private final UUID serviceUuid;
    private final UUID characteristicUuid;
    private final UUID descriptorUuid;

    private final String description;

    public BluetoothGattElement(String serviceShortString, String characteristicShortString, String descriptorShortString) {
        this(Uuid.shortStringToUuid(serviceShortString), Uuid.shortStringToUuid(characteristicShortString), Uuid.shortStringToUuid(descriptorShortString));
        }

    public BluetoothGattElement(UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid) {
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
        this.descriptorUuid = descriptorUuid;
        String servStr = (serviceUuid == null) ? null : Uuid.longToShortString(serviceUuid.toString());
        String charaStr = (characteristicUuid == null) ? null : Uuid.longToShortString(characteristicUuid.toString());
        String descStr = (descriptorUuid == null) ? null : Uuid.longToShortString(descriptorUuid.toString());
        description = "[service= " + servStr
                + ",characteristic= " + charaStr
                + ",descriptor= " + descStr + "]";
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

    // 从设备镜像中搜寻此element的Gatt Object
    public Object retrieveGattObject(DeviceMirror deviceMirror) {
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

    // element的类型
    public int getType() {
        if(descriptorUuid != null) return TYPE_DESCRIPTOR;
        if(characteristicUuid != null) return TYPE_CHARACTERISTIC;
        if(serviceUuid != null) return TYPE_SERVICE;
        return TYPE_NULL;
    }

    @Override
    public String toString() {
        return description;
    }
}

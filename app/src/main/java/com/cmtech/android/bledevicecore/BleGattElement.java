package com.cmtech.android.bledevicecore;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.cmtech.android.ble.core.DeviceMirror;

import java.util.UUID;


/**
 * Created by bme on 2018/3/1.
 */

public class BleGattElement {
    private static final int TYPE_NULL = 0;                // 空ELement 类型
    private static final int TYPE_SERVICE = 1;             // service element类型
    private static final int TYPE_CHARACTERISTIC = 2;      // characteristic element类型
    private static final int TYPE_DESCRIPTOR = 3;          // descriptor element类型


    // 服务UUID
    private final UUID serviceUuid;

    // 特征UUID
    private final UUID characteristicUuid;

    // 描述符UUID
    private final UUID descriptorUuid;

    // element的描述
    private final String description;


    // 用短的字符串构建Element
    public BleGattElement(String serviceShortString, String characteristicShortString, String descriptorShortString) {
        this(Uuid.shortStringToUuid(serviceShortString, BleDeviceConfig.getInstance().getBaseUuid()),
                Uuid.shortStringToUuid(characteristicShortString, BleDeviceConfig.getInstance().getBaseUuid()),
                Uuid.shortStringToUuid(descriptorShortString, BleDeviceConfig.getInstance().getBaseUuid()));
    }

    // 用UUID构建Element
    public BleGattElement(UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid) {
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
        this.descriptorUuid = descriptorUuid;
        String servStr = (serviceUuid == null) ? null : Uuid.longToShortString(serviceUuid.toString());
        String charaStr = (characteristicUuid == null) ? null : Uuid.longToShortString(characteristicUuid.toString());
        String descStr = (descriptorUuid == null) ? null : Uuid.longToShortString(descriptorUuid.toString());
        description = "BleGattElement[ service= " + servStr
                + ",characteristic= " + charaStr
                + ",descriptor= " + descStr + " ]";
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

    // 从设备中搜寻element对应的Gatt Object，可用于验证Element是否存在于设备中
    public Object retrieveGattObject(BleDevice device) {
        if(device == null) return null;
        DeviceMirror deviceMirror = BleDeviceUtil.getDeviceMirror(device);
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

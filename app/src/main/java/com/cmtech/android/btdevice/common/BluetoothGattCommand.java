package com.cmtech.android.btdevice.common;

import android.bluetooth.BluetoothGatt;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;

import java.util.UUID;

/**
 * Created by bme on 2018/3/1.
 */

public class BluetoothGattCommand {
    private DeviceMirror deviceMirror;
    private BluetoothGattChannel channel;       // 通道
    private IBleCallback dataOpCallback;        // 数据操作回调
    private byte[] writtenData;                 // 如果是写命令，存放要写的数据；如果是notify或indicate，存放enable数据
    private IBleCallback notifyOpCallback;      // 如果是notify或indicate操作，存放notify或indicate的回调

    private BluetoothGattCommand(DeviceMirror deviceMirror, BluetoothGattChannel channel,
                                 IBleCallback dataOpCallback,
                                 byte[] writtenData, IBleCallback notifyOpCallback) {
        this.deviceMirror = deviceMirror;
        this.channel = channel;
        this.dataOpCallback = dataOpCallback;
        this.writtenData = writtenData;
        this.notifyOpCallback = notifyOpCallback;
    }

    public void execute() {
        switch (channel.getPropertyType()) {
            case PROPERTY_READ:
                deviceMirror.bindChannel(dataOpCallback, channel);
                deviceMirror.readData();
                break;
            case PROPERTY_WRITE:
                deviceMirror.bindChannel(dataOpCallback, channel);
                deviceMirror.writeData(writtenData);
                break;
            case PROPERTY_NOTIFY:
                deviceMirror.bindChannel(dataOpCallback, channel);
                if(writtenData[0] == 1) {
                    deviceMirror.registerNotify(false);
                    deviceMirror.setNotifyListener(channel.getGattInfoKey(), notifyOpCallback);
                } else {
                    deviceMirror.unregisterNotify(false);
                }
                break;
            case PROPERTY_INDICATE:
                deviceMirror.bindChannel(dataOpCallback, channel);
                if(writtenData[0] == 1) {
                    deviceMirror.registerNotify(true);
                    deviceMirror.setNotifyListener(channel.getGattInfoKey(), notifyOpCallback);
                } else {
                    deviceMirror.unregisterNotify(true);
                }
                break;
            default:
                break;
        }
    }

    public static class Builder {
        private BluetoothGattElement element;
        private PropertyType propertyType;
        private DeviceMirror deviceMirror;
        private byte[] data;
        private IBleCallback dataOpCallback;
        private IBleCallback notifyOpCallback;

        public Builder() {
        }

        public Builder setDeviceMirror(DeviceMirror deviceMirror) {
            this.deviceMirror = deviceMirror;
            return this;
        }

        public Builder setBluetoothElement(BluetoothGattElement element) {
            this.element = element;
            return this;
        }

        public Builder setPropertyType(PropertyType propertyType) {
            this.propertyType = propertyType;
            return this;
        }

        public Builder setData(byte[] data) {
            this.data = data;
            return this;
        }

        public Builder setDataOpCallback(IBleCallback dataOpCallback) {
            this.dataOpCallback = dataOpCallback;
            return this;
        }

        public Builder setNotifyOpCallback(IBleCallback notifyOpCallback) {
            this.notifyOpCallback = notifyOpCallback;
            return this;
        }

        public BluetoothGattCommand build() {
            if(deviceMirror == null || element == null || dataOpCallback == null) return null;

            if(propertyType == PropertyType.PROPERTY_WRITE
                    || propertyType == PropertyType.PROPERTY_NOTIFY
                    || propertyType == PropertyType.PROPERTY_INDICATE) {
                if(data == null || data.length == 0) return null;
            }

            if(propertyType == PropertyType.PROPERTY_NOTIFY
                    || propertyType == PropertyType.PROPERTY_INDICATE
                    || notifyOpCallback == null) {
                return null;
            }

            BluetoothGattChannel.Builder builder = new BluetoothGattChannel.Builder();
            BluetoothGattChannel channel = builder.setBluetoothGatt(deviceMirror.getBluetoothGatt())
                    .setPropertyType(propertyType)
                    .setServiceUUID(element.getServiceUuid())
                    .setCharacteristicUUID(element.getCharacteristicUuid())
                    .setDescriptorUUID(element.getDescriptorUuid()).builder();

            return new BluetoothGattCommand(deviceMirror, channel, dataOpCallback, data, notifyOpCallback);
        }
    }
}

package com.cmtech.android.btdeviceapp.model;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;

/**
 * Created by bme on 2018/3/1.
 */

public class BluetoothGattCommand {
    private final DeviceMirror deviceMirror;          // 执行命令的设备镜像
    private final BluetoothGattChannel channel;       // 执行命令的通道
    private final IBleCallback dataOpCallback;        // 数据操作回调
    private final byte[] writtenData;                 // 如果是写操作，存放要写的数据；如果是notify或indicate操作，存放enable数据
    private final IBleCallback notifyOpCallback;      // 如果是notify或indicate操作，存放notify或indicate的回调

    private BluetoothGattCommand(DeviceMirror deviceMirror, BluetoothGattChannel channel,
                                 IBleCallback dataOpCallback,
                                 byte[] writtenData, IBleCallback notifyOpCallback) {
        this.deviceMirror = deviceMirror;
        this.channel = channel;
        this.dataOpCallback = dataOpCallback;
        this.writtenData = writtenData;
        this.notifyOpCallback = notifyOpCallback;
    }

    public BluetoothGattChannel getChannel() {
        return channel;
    }

    // 执行命令
    public boolean execute() {
        if(deviceMirror == null || channel == null) return false;

        switch (channel.getPropertyType()) {
            case PROPERTY_READ:
                deviceMirror.bindChannel( dataOpCallback, channel);
                deviceMirror.readData();
                break;
            case PROPERTY_WRITE:
                deviceMirror.bindChannel( dataOpCallback, channel);
                deviceMirror.writeData(writtenData);
                break;
            case PROPERTY_NOTIFY:
                deviceMirror.bindChannel( dataOpCallback, channel);
                if(writtenData[0] == 1) {
                    deviceMirror.registerNotify(false);
                    deviceMirror.setNotifyListener(channel.getGattInfoKey(), notifyOpCallback);
                } else {
                    deviceMirror.unregisterNotify(false);
                    deviceMirror.removeReceiveCallback(channel.getGattInfoKey());
                }
                break;
            case PROPERTY_INDICATE:
                deviceMirror.bindChannel( dataOpCallback, channel);
                if(writtenData[0] == 1) {
                    deviceMirror.registerNotify(true);
                    deviceMirror.setNotifyListener(channel.getGattInfoKey(), notifyOpCallback);
                } else {
                    deviceMirror.unregisterNotify(true);
                    deviceMirror.removeReceiveCallback(channel.getGattInfoKey());
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public String toString() {
        BluetoothGattElement element =
                new BluetoothGattElement(channel.getServiceUUID(), channel.getCharacteristicUUID(),channel.getDescriptorUUID());
        return "BluetoothGattCommand{" + channel.getPropertyType() +
                " element=" + element.toString() +
                '}';
    }

    // 获取Gatt信息key
    public String getGattInfoKey() { return channel.getGattInfoKey(); }

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

        public Builder setDataOpCallback(IBleCallback dataOpCallback) {
            this.dataOpCallback = dataOpCallback;
            return this;
        }

        public Builder setData(byte[] data) {
            this.data = data;
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
                    || propertyType == PropertyType.PROPERTY_INDICATE) {
                if(notifyOpCallback == null) return null;
            }

            BluetoothGattChannel.Builder builder = new BluetoothGattChannel.Builder();
            BluetoothGattChannel channel = builder.setBluetoothGatt(deviceMirror.getBluetoothGatt())
                    .setPropertyType(propertyType)
                    .setServiceUUID(element.getServiceUuid())
                    .setCharacteristicUUID(element.getCharacteristicUuid())
                    .setDescriptorUUID(element.getDescriptorUuid()).builder();

            BluetoothGattCommand command = new BluetoothGattCommand(deviceMirror, channel, dataOpCallback, data, notifyOpCallback);

            return command;
        }
    }
}

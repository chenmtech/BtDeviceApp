package com.cmtech.android.bledevicecore;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.utils.HexUtil;

/**
 * BleGattCommand: 表示要执行的一条Gatt操作命令
 * Created by bme on 2018/3/1.
 */

public class BleGattCommand{
    private final DeviceMirror deviceMirror;          // 执行命令的设备镜像
    private final BluetoothGattChannel channel;       // 执行命令的通道
    private final IBleCallback dataOpCallback;        // 数据操作回调
    private final byte[] writtenData;                 // 如果是写操作，存放要写的数据；如果是notify或indicate操作，存放enable数据
    private final IBleCallback notifyOpCallback;      // 如果是notify或indicate操作，存放notify或indicate的回调
    private final String elementDescription; // 命令操作的element的描述符
    private final boolean isInstantCommand; // 是否是立刻执行的命令，即不需要通过蓝牙通信的命令

    private BleGattCommand(DeviceMirror deviceMirror, BluetoothGattChannel channel,
                           IBleCallback dataOpCallback,
                           byte[] writtenData, IBleCallback notifyOpCallback, String elementDescription) {
        this.deviceMirror = deviceMirror;
        this.channel = channel;
        this.dataOpCallback = dataOpCallback;
        this.writtenData = writtenData;
        this.notifyOpCallback = notifyOpCallback;
        this.elementDescription = elementDescription;
        this.isInstantCommand = false;
    }

    private BleGattCommand(DeviceMirror deviceMirror, BluetoothGattChannel channel,
                           IBleCallback dataOpCallback,
                           byte[] writtenData, IBleCallback notifyOpCallback, String elementDescription, boolean isInstantCommand) {
        this.deviceMirror = deviceMirror;
        this.channel = channel;
        this.dataOpCallback = dataOpCallback;
        this.writtenData = writtenData;
        this.notifyOpCallback = notifyOpCallback;
        this.elementDescription = elementDescription;
        this.isInstantCommand = isInstantCommand;
    }

    public BluetoothGattChannel getChannel() {
        return channel;
    }

    public PropertyType getPropertyType() {
        return channel.getPropertyType();
    }

    // 创建即时命令，即时命令在执行的时候会立刻执行dataOpCallback.onSuccess()
    public static BleGattCommand createInstantCommand(IBleCallback dataOpCallback) {
        return new BleGattCommand(null, null, dataOpCallback, null, null, "instant cmd", true);
    }

    public boolean isInstantCommand() {
        return isInstantCommand;
    }

    // 执行命令
    public synchronized boolean execute() {
        if(isInstantCommand() && dataOpCallback != null) {
            dataOpCallback.onSuccess(null, null, null);
            return true;
        }

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
        if(isInstantCommand()) return elementDescription;

        if(channel.getPropertyType() != PropertyType.PROPERTY_READ) {
            return channel.getPropertyType() + " " + elementDescription + " " + HexUtil.encodeHexStr(writtenData);
        } else
            return channel.getPropertyType() + " " + elementDescription;
    }

    // 获取Gatt信息key
    public String getGattInfoKey() {
        if(isInstantCommand()) return "";

        return channel.getGattInfoKey();
    }

    public static class Builder {
        private BleGattElement element;
        private PropertyType propertyType;
        private DeviceMirror deviceMirror;
        private byte[] data;
        private IBleCallback dataOpCallback;
        private IBleCallback notifyOpCallback;

        Builder() {
        }

        public Builder setDeviceMirror(DeviceMirror deviceMirror) {
            this.deviceMirror = deviceMirror;
            return this;
        }

        public Builder setBluetoothElement(BleGattElement element) {
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

        public BleGattCommand build() {
            if(deviceMirror == null || element == null || dataOpCallback == null) return null;

            if(propertyType == PropertyType.PROPERTY_WRITE
                    || propertyType == PropertyType.PROPERTY_NOTIFY
                    || propertyType == PropertyType.PROPERTY_INDICATE) {
                if(data == null || data.length == 0) return null;
            }

            if(propertyType == PropertyType.PROPERTY_NOTIFY
                    || propertyType == PropertyType.PROPERTY_INDICATE) {
                if(data[0] == 1 && notifyOpCallback == null) return null;
            }

            BluetoothGattChannel.Builder builder = new BluetoothGattChannel.Builder();
            BluetoothGattChannel channel = builder.setBluetoothGatt(deviceMirror.getBluetoothGatt())
                    .setPropertyType(propertyType)
                    .setServiceUUID(element.getServiceUuid())
                    .setCharacteristicUUID(element.getCharacteristicUuid())
                    .setDescriptorUUID(element.getDescriptorUuid()).builder();

            return new BleGattCommand(deviceMirror, channel, dataOpCallback, data, notifyOpCallback, element.toString());
        }
    }
}

package com.cmtech.android.bledevice.core;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.vise.log.ViseLog;

/**
 * BleDeviceGattOperator: Gatt操作者抽象类
 * Created by bme on 2018/12/20.
 */

public class BleDeviceGattOperator {

    private BleGattCommandExecutor commandExecutor; // Gatt命令执行器

    protected final BleDevice device; // BLE设备

    BleDeviceGattOperator(BleDevice device) {
        this.device = device;
    }

    // 检测GattElement是否存在于device中
    public boolean checkElements(BleGattElement[] elements) {
        for(BleGattElement element : elements) {
            if(BleDeviceUtil.getGattObject(device, element) == null) return false;
        }

        return true;
    }

    // 启动Gatt命令执行器
    public void start() {
        if((commandExecutor != null) && commandExecutor.isAlive()) return;

        DeviceMirror deviceMirror = device.getDeviceMirror();

        if(deviceMirror == null) {
            throw new NullPointerException();
        }

        commandExecutor = new BleGattCommandExecutor(deviceMirror);

        commandExecutor.start();
        ViseLog.i("success to create new command executor.");
    }

    // 停止Gatt命令执行器
    public void stop() {
        if((commandExecutor != null) && commandExecutor.isAlive()) {
            commandExecutor.stop();
        }
    }

    public boolean isAlive() {
        return (commandExecutor != null && commandExecutor.isAlive());
    }

    // Gatt操作命令
    // 取命令
    public final void read(BleGattElement element, IBleDataOpCallback dataOpCallback) {
        IBleCallback callback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        commandExecutor.addReadCommand(element, callback);
    }

    // 写多字节命令
    public final void write(BleGattElement element, byte[] data, IBleDataOpCallback dataOpCallback) {
        IBleCallback callback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        commandExecutor.addWriteCommand(element, data, callback);
    }

    // 写单字节命令
    public final void write(BleGattElement element, byte data, IBleDataOpCallback dataOpCallback) {
        IBleCallback callback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        commandExecutor.addWriteCommand(element, data, callback);
    }

    // Notify命令
    public final void notify(BleGattElement element, boolean enable
            , IBleDataOpCallback dataOpCallback, IBleDataOpCallback notifyOpCallback) {
        IBleCallback dataCallback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        IBleCallback notifyCallback = (notifyOpCallback == null) ? null : new BleDataOpCallbackAdapter(notifyOpCallback);
        commandExecutor.addNotifyCommand(element, enable, dataCallback, notifyCallback);
    }

    // Notify命令
    public final void notify(BleGattElement element, boolean enable, IBleDataOpCallback notifyOpCallback) {
        notify(element, enable, null, notifyOpCallback);
    }

    // Indicate命令
    public final void indicate(BleGattElement element, boolean enable
            , IBleDataOpCallback dataOpCallback, IBleDataOpCallback indicateOpCallback) {
        IBleCallback dataCallback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        IBleCallback indicateCallback = (indicateOpCallback == null) ? null : new BleDataOpCallbackAdapter(indicateOpCallback);
        commandExecutor.addIndicateCommand(element, enable, dataCallback, indicateCallback);
    }

    // Indicate命令
    public final void indicate(BleGattElement element, boolean enable, IBleDataOpCallback indicateOpCallback) {
        indicate(element, enable, null, indicateOpCallback);
    }

    // 不需要蓝牙通信立刻执行的命令
    public final void instExecute(IBleDataOpCallback dataOpCallback) {
        IBleCallback dataCallback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        commandExecutor.addInstantCommand(dataCallback);
    }

}

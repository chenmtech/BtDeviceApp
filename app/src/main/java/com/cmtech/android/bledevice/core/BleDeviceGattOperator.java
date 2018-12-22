package com.cmtech.android.bledevice.core;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.vise.log.ViseLog;

/**
 * BleDeviceGattOperator: Gatt操作者抽象类
 * Created by bme on 2018/12/20.
 */

public abstract class BleDeviceGattOperator {

    private BleGattCommandExecutor commandExecutor;
    protected BleDevice device;

    public BleDeviceGattOperator() {

    }

    public BleDevice getDevice() { return device; }
    public void setDevice(BleDevice device) {
        this.device = device;
    }

    public abstract boolean checkBasicService();

    // 启动Gatt命令执行器
    public void start() {
        if((commandExecutor != null) && commandExecutor.isAlive()) return;
        DeviceMirror deviceMirror = BleDeviceUtil.getDeviceMirror(device);
        if(deviceMirror == null) return;

        commandExecutor = new BleGattCommandExecutor(deviceMirror);
        commandExecutor.start();
        ViseLog.i("create new command executor.");
    }

    // 停止Gatt命令执行器
    public void stop() {
        if((commandExecutor != null) && commandExecutor.isAlive()) {
            ViseLog.i("stop command executor.");
            commandExecutor.stop();
        }
    }

    // 添加Gatt操作命令
    // 添加读取命令
    public final void addReadCommand(BleGattElement element, IBleDataOpCallback dataOpCallback) {
        if(commandExecutor != null) {
            IBleCallback callback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
            commandExecutor.addReadCommand(element, callback);
        }
    }

    // 添加写入多字节命令
    public final void addWriteCommand(BleGattElement element, byte[] data, IBleDataOpCallback dataOpCallback) {
        if(commandExecutor != null) {
            IBleCallback callback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
            commandExecutor.addWriteCommand(element, data, callback);
        }
    }

    // 添加写入单字节命令
    public final void addWriteCommand(BleGattElement element, byte data, IBleDataOpCallback dataOpCallback) {
        if(commandExecutor != null) {
            IBleCallback callback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
            commandExecutor.addWriteCommand(element, data, callback);
        }
    }

    // 添加Notify命令
    public final void addNotifyCommand(BleGattElement element, boolean enable
            , IBleDataOpCallback dataOpCallback, IBleDataOpCallback notifyOpCallback) {
        if(commandExecutor != null) {
            IBleCallback dataCallback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
            IBleCallback notifyCallback = (notifyOpCallback == null) ? null : new BleDataOpCallbackAdapter(notifyOpCallback);
            commandExecutor.addNotifyCommand(element, enable, dataCallback, notifyCallback);
        }
    }

    // 添加Indicate命令
    public final void addIndicateCommand(BleGattElement element, boolean enable
            , IBleDataOpCallback dataOpCallback, IBleDataOpCallback indicateOpCallback) {
        if(commandExecutor != null) {
            IBleCallback dataCallback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
            IBleCallback indicateCallback = (indicateOpCallback == null) ? null : new BleDataOpCallbackAdapter(indicateOpCallback);
            commandExecutor.addIndicateCommand(element, enable, dataCallback, indicateCallback);
        }
    }

    // 添加Instant命令
    public final void addInstantCommand(IBleDataOpCallback dataOpCallback) {
        if(commandExecutor != null) {
            IBleCallback dataCallback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
            commandExecutor.addInstantCommand(dataCallback);
        }
    }

}

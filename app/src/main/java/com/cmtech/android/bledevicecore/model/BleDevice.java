package com.cmtech.android.bledevicecore.model;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.TimeoutException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.bledevicecore.devicestate.BleDeviceCloseState;
import com.cmtech.android.bledevicecore.devicestate.BleDeviceConnectedState;
import com.cmtech.android.bledevicecore.devicestate.BleDeviceConnectingState;
import com.cmtech.android.bledevicecore.devicestate.BleDeviceDisconnectingState;
import com.cmtech.android.bledevicecore.devicestate.BleDeviceOpenState;
import com.cmtech.android.bledevicecore.devicestate.BleDeviceScanState;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledevicecore.devicestate.IBleDeviceState;
import com.cmtech.android.bledevicecore.interfa.IBleDeviceStateObserver;
import com.vise.log.ViseLog;


import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bme on 2018/2/19.
 */

public abstract class BleDevice implements Serializable{
    // 设备镜像池
    private final static DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();

    // 设备基本信息
    private final BleDeviceBasicInfo basicInfo;

    // ViseBle内部设备
    private BluetoothLeDevice bluetoothLeDevice = null;

    // GATT命令串行执行器
    private BleGattCommandExecutor commandExecutor;

    // 设备状态观察者列表
    private final List<IBleDeviceStateObserver> deviceStateObserverList = new LinkedList<>();

    // 是否正在关闭
    //private boolean isClosing = false;

    // 几个设备状态
    private final BleDeviceCloseState closeState = new BleDeviceCloseState(this);       // 关闭状态
    private final BleDeviceOpenState openState = new BleDeviceOpenState(this);          // 打开状态
    private final BleDeviceScanState scanState = new BleDeviceScanState(this);          // 扫描状态
    private final BleDeviceConnectingState connectingState = new BleDeviceConnectingState(this);        // 连接中状态
    private final BleDeviceConnectedState connectedState = new BleDeviceConnectedState(this);           // 连接状态
    private final BleDeviceDisconnectingState disconnectingState = new BleDeviceDisconnectingState(this);       // 断开中状态

    // 设备状态变量
    private IBleDeviceState state = closeState;

    // 连接回调
    private final IConnectCallback connectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror mirror) {
            synchronized (BleDevice.this) {
                state.onDeviceConnectSuccess(mirror);
            }
        }

        @Override
        public void onConnectFailure(final BleException exception) {
            synchronized (BleDevice.this) {
                if (exception instanceof TimeoutException)
                    state.onDeviceConnectTimeout();
                else
                    state.onDeviceConnectFailure();
            }
        }
        @Override
        public void onDisconnect(final boolean isActive) {
            synchronized (BleDevice.this) {
                state.onDeviceDisconnect();
            }
        }

        @Override
        public void onScanFinish(boolean result) {
            synchronized (BleDevice.this) {
                if (result) {
                    state.onDeviceScanSuccess();
                }
                else {
                    state.onDeviceScanFailure();
                }
            }
        }
    };

    // 处理Gatt操作回调消息
    protected final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            processGattCallbackMessage(msg);
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    // 构造器
    public BleDevice(BleDeviceBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public String getMacAddress() {
        return basicInfo.getMacAddress();
    }

    public String getNickName() {
        return basicInfo.getNickName();
    }

    public String getUuidString() {
        return basicInfo.getUuidString();
    }

    public boolean autoConnect() {
        return basicInfo.autoConnect();
    }

    public String getImagePath() {
        return basicInfo.getImagePath();
    }

    public BleDeviceBasicInfo getBasicInfo() {
        return basicInfo;
    }

    public BluetoothLeDevice getBluetoothLeDevice() {
        return bluetoothLeDevice;
    }

    // 获取几个设备状态常量
    public BleDeviceCloseState getCloseState() {
        return closeState;
    }
    public BleDeviceOpenState getOpenState() {
        return openState;
    }
    public BleDeviceScanState getScanState() {
        return scanState;
    }
    public BleDeviceConnectingState getConnectingState() {
        return connectingState;
    }
    public BleDeviceConnectedState getConnectedState() {
        return connectedState;
    }
    public BleDeviceDisconnectingState getDisconnectingState() {
        return disconnectingState;
    }

    // 设置设备状态
    public void setState(IBleDeviceState state) {
        this.state = state;
        notifyDeviceStateObservers();
    }

    public Handler getHandler() {
        return handler;
    }

    public IConnectCallback getConnectCallback() {
        return connectCallback;
    }

    // 打开设备
    public synchronized void open() {
        state.open();
        if(autoConnect())
            state.scan();
    }

    // 关闭设备
    public synchronized void close() {
        state.close();
    }

    // 扫描设备，扫描到设备后会自动连接设备
    public synchronized void scan() {
        state.scan();
    }

    // 断开设备
    public synchronized void disconnect() {
        state.disconnect();
    }

    // 转换设备状态
    public synchronized void switchState() {
        state.switchState();
    }

    // 获取设备描述信息
    public synchronized String getStateDescription() {
        return state.getStateDescription();
    }

    // 是否可连接
    public synchronized boolean canConnect() {
        return state.canConnect();
    }

    // 是否可断开
    public synchronized boolean canDisconnect() {
        return state.canDisconnect();
    }

    // 是否可关闭
    public synchronized boolean canClose() {
        return state.canClose();
    }

    // 是否已连接
    public synchronized boolean isConnected() {
        return (state == connectedState);
    }

    // 是否可执行命令
    /*public synchronized boolean canExecuteCommand() {
        return (isConnected() && isCommandExecutorAlive());
    }*/

    // 添加读取命令
    public synchronized boolean addReadCommand(BleGattElement element, IBleCallback dataOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addReadCommand(element, dataOpCallback));
    }

    // 添加写入多字节命令
    public synchronized boolean addWriteCommand(BleGattElement element, byte[] data, IBleCallback dataOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addWriteCommand(element, data, dataOpCallback));
    }

    // 添加写入单字节命令
    public synchronized boolean addWriteCommand(BleGattElement element, byte data, IBleCallback dataOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addWriteCommand(element, data, dataOpCallback));
    }

    // 添加Notify命令
    public synchronized boolean addNotifyCommand(BleGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback notifyOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addNotifyCommand(element, enable, dataOpCallback, notifyOpCallback));
    }

    // 添加Indicate命令
    public synchronized boolean addIndicateCommand(BleGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback indicateOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addIndicateCommand(element, enable, dataOpCallback, indicateOpCallback));
    }

    // 添加Instant命令
    public synchronized boolean addInstantCommand(IBleCallback dataOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addInstantCommand(dataOpCallback));
    }

    // 登记设备状态观察者
    public void registerDeviceStateObserver(IBleDeviceStateObserver observer) {
        if(!deviceStateObserverList.contains(observer)) {
            deviceStateObserverList.add(observer);
        }
    }

    // 删除设备状态观察者
    public void removeDeviceStateObserver(IBleDeviceStateObserver observer) {
        int index = deviceStateObserverList.indexOf(observer);
        if(index >= 0) {
            deviceStateObserverList.remove(index);
        }
    }

    // 通知设备状态观察者
    public void notifyDeviceStateObservers() {
        for(final IBleDeviceStateObserver observer : deviceStateObserverList) {
            if(observer != null) {
                // 保证在主线程更新连接状态
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        observer.updateDeviceState(BleDevice.this);
                    }
                });
            }
        }
    }


    // 构造之后的初始化操作
    public abstract void initializeAfterConstruction();

    // 连接成功后执行的操作
    public abstract void executeAfterConnectSuccess();

    // 连接错误后执行的操作
    public abstract void executeAfterConnectFailure();

    // 断开连接后执行的操作
    public abstract void executeAfterDisconnect();

    // 处理Gatt回调消息函数
    public abstract void processGattCallbackMessage(Message msg);

    // 打包配置信息
    public abstract Bundle bundleConfigure();

    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    // 创建Gatt命令执行器
    private boolean createGattCommandExecutor() {
        ViseLog.i("create new command executor.");
        if(isCommandExecutorAlive()) return false;
        DeviceMirror deviceMirror = deviceMirrorPool.getDeviceMirror(bluetoothLeDevice);
        if(deviceMirror == null) return false;

        commandExecutor = new BleGattCommandExecutor(deviceMirror);
        commandExecutor.start();
        return true;
    }

    // 停止Gatt命令执行器
    private void stopCommandExecutor() {
        if(isCommandExecutorAlive()) commandExecutor.stop();
    }

    // Gatt命令执行器是否Alive
    private boolean isCommandExecutorAlive() {
        return ((commandExecutor != null) && commandExecutor.isAlive());
    }

    // 获取设备上element对应的Gatt Object
    protected Object getGattObject(BleGattElement element) {
        DeviceMirror deviceMirror = deviceMirrorPool.getDeviceMirror(bluetoothLeDevice);
        if(deviceMirror == null || element == null) return null;
        return element.retrieveGattObject(deviceMirror);
    }

    // 发送Gatt回调后的消息
    protected void sendGattCallbackMessage(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }


    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////

    public void processConnectSuccess(DeviceMirror mirror) {
        bluetoothLeDevice = mirror.getBluetoothLeDevice();

        ViseLog.i("onConnectSuccess");

        // 创建Gatt串行命令执行器
        if(createGattCommandExecutor()) {
            executeAfterConnectSuccess();
        } else {
            // 创建失败，断开连接
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            }, 500);
        }
    }

    public void processConnectFailure() {
        stopCommandExecutor();

        bluetoothLeDevice = null;

        executeAfterConnectFailure();
    }

    public void processDisconnect() {
        stopCommandExecutor();

        bluetoothLeDevice = null;

        executeAfterDisconnect();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDevice that = (BleDevice) o;
        String thisAddress = getMacAddress();
        String thatAddress = that.getMacAddress();

        return thisAddress != null ? thisAddress.equals(thatAddress) : thatAddress == null;
    }

    @Override
    public int hashCode() {
        return getMacAddress() != null ? getMacAddress().hashCode() : 0;
    }

}

package com.cmtech.android.btdeviceapp.model;

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
import com.cmtech.android.btdeviceapp.devicestate.BleDeviceCloseState;
import com.cmtech.android.btdeviceapp.devicestate.BleDeviceConnectedState;
import com.cmtech.android.btdeviceapp.devicestate.BleDeviceConnectingState;
import com.cmtech.android.btdeviceapp.devicestate.BleDeviceDisconnectingState;
import com.cmtech.android.btdeviceapp.devicestate.BleDeviceOpenState;
import com.cmtech.android.btdeviceapp.devicestate.BleDeviceScanState;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceConnectStateObserver;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.devicestate.IBleDeviceState;
import com.vise.log.ViseLog;


import java.util.LinkedList;
import java.util.List;

/**
 * Created by bme on 2018/2/19.
 */

public abstract class BleDevice {
    // 设备镜像池
    private static DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();

    // 设备基本信息
    private final BleDeviceBasicInfo basicInfo;

    // ViseBle内部设备
    private BluetoothLeDevice bluetoothLeDevice = null;

    // GATT命令串行执行器
    private GattCommandSerialExecutor commandExecutor;

    // 连接状态观察者列表
    private final List<IBleDeviceConnectStateObserver> connectStateObserverList = new LinkedList<>();

    // 是否正在关闭
    //private boolean isClosing = false;

    private BleDeviceCloseState closeState = new BleDeviceCloseState(this);
    private BleDeviceOpenState openState = new BleDeviceOpenState(this);
    private BleDeviceScanState scanState = new BleDeviceScanState(this);
    private BleDeviceConnectingState connectingState = new BleDeviceConnectingState(this);
    private BleDeviceConnectedState connectedState = new BleDeviceConnectedState(this);
    private BleDeviceDisconnectingState disconnectingState = new BleDeviceDisconnectingState(this);
    // 设备连接状态
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

    public boolean isAutoConnected() {
        return basicInfo.isAutoConnected();
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
    public void setState(IBleDeviceState state) {
        this.state = state;
        notifyConnectStateObservers();
    }

    public Handler getHandler() {
        return handler;
    }

    public IConnectCallback getConnectCallback() {
        return connectCallback;
    }

    public void open() {
        state.open();
        state.scan();
    }

    public void close() {
        state.close();
    }

    public void scan() {
        state.scan();
    }

    public void disconnect() {
        state.disconnect();
    }

    public void switchState() {
        state.switchState();
    }

    public String getStateDescription() {
        return state.getStateDescription();
    }

    public boolean canConnect() {
        return state.canConnect();
    }

    public boolean canDisconnect() {
        return state.canDisconnect();
    }

    public boolean canClose() {
        return state.canClose();
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

    // 登记连接状态观察者
    public void registerConnectStateObserver(IBleDeviceConnectStateObserver observer) {
        if(!connectStateObserverList.contains(observer)) {
            connectStateObserverList.add(observer);
        }
    }

    // 删除连接状态观察者
    public void removeConnectStateObserver(IBleDeviceConnectStateObserver observer) {
        int index = connectStateObserverList.indexOf(observer);
        if(index >= 0) {
            connectStateObserverList.remove(index);
        }
    }

    // 通知连接状态观察者
    public void notifyConnectStateObservers() {
        for(final IBleDeviceConnectStateObserver observer : connectStateObserverList) {
            if(observer != null) {
                // 保证在主线程更新连接状态
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        observer.updateConnectState(BleDevice.this);
                    }
                });
            }
        }
    }

    public boolean addReadCommand(BleGattElement element, IBleCallback dataOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addReadCommand(element, dataOpCallback));
    }

    public boolean addWriteCommand(BleGattElement element, byte[] data, IBleCallback dataOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addWriteCommand(element, data, dataOpCallback));
    }

    public boolean addWriteCommand(BleGattElement element, byte data, IBleCallback dataOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addWriteCommand(element, data, dataOpCallback));
    }

    public boolean addNotifyCommand(BleGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback notifyOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addNotifyCommand(element, enable, dataOpCallback, notifyOpCallback));
    }

    public boolean addIndicateCommand(BleGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback indicateOpCallback) {
        return ((commandExecutor != null) && commandExecutor.addIndicateCommand(element, enable, dataOpCallback, indicateOpCallback));
    }


    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    // 创建Gatt命令执行器
    private boolean createGattCommandExecutor() {
        ViseLog.i("create new command executor.");
        if(isCommandExecutorAlive()) return false;
        DeviceMirror deviceMirror = deviceMirrorPool.getDeviceMirror(bluetoothLeDevice);
        if(deviceMirror == null) return false;

        commandExecutor = new GattCommandSerialExecutor(deviceMirror);
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

        ViseLog.d("onDisconnect");

        executeAfterDisconnect();
    }


/*    // 发起连接
    public synchronized void connect() {
        if(canConnect()) {
            handler.removeCallbacksAndMessages(null);

            setDeviceConnectState(CONNECT_CONNECTING);
            notifyConnectStateObservers();

            MyApplication.getViseBle().connectByMac(getMacAddress(), connectCallback);
        }
    }

    // 断开连接
    public synchronized void disconnect() {
        if(canDisconnect()) {
            stopCommandExecutor();

            setDeviceConnectState(CONNECT_DISCONNECTING);
            notifyConnectStateObservers();

            deviceMirrorPool.disconnect(bluetoothLeDevice);
        }
    }

    // 关闭设备
    public synchronized void close() {
        if(canClose()) {
            if(state != CONNECT_DISCONNECT) {
                stopCommandExecutor();

                setDeviceConnectState(CONNECT_DISCONNECTING);
                notifyConnectStateObservers();

                deviceMirrorPool.disconnect(bluetoothLeDevice);
                isClosing = true;
            } else {
                setDeviceConnectState(CONNECT_WAITING);
                notifyConnectStateObservers();
            }
        }
    }*/

    // 连接回调处理函数
    /*
    private synchronized void processConnectCallback(com.cmtech.android.btdeviceapp.model.BleDeviceConnectingState state) {
        setDeviceConnectState(state);
        notifyConnectStateObservers();

        switch (state) {
            case CONNECT_SUCCESS:
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
                break;

            case CONNECT_CONNECTTIMEOUT:
            case CONNECT_CONNECTFAILURE:
                stopCommandExecutor();

                executeAfterConnectFailure();

                // 连接错误或超时，重新连接
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connect();
                    }
                }, 500);

                break;

            case CONNECT_DISCONNECT:
                stopCommandExecutor();

                executeAfterDisconnect();

                if(isClosing) {
                    setDeviceConnectState(CONNECT_WAITING);
                    notifyConnectStateObservers();
                    isClosing = false;
                }

                break;

            case CONNECT_SCANFAILURE:
                // 扫描错误，什么也不做
                break;

            case CONNECT_SCANSUCCESS:
                // 扫描成功，什么也不做。内部会自动发起连接
                break;

            default:
                break;
        }
    }
    */
}

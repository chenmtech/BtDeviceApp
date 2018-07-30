package com.cmtech.android.btdeviceapp.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.TimeoutException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceConnectStateObserver;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.vise.log.ViseLog;


import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.btdeviceapp.model.BleDeviceConnectState.*;

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

    // 设备连接状态
    private BleDeviceConnectState state = BleDeviceConnectState.CONNECT_WAITING;

    // GATT命令串行执行器
    protected GattCommandSerialExecutor commandExecutor;

    // 连接状态观察者列表
    private final List<IBleDeviceConnectStateObserver> connectStateObserverList = new LinkedList<>();

    private boolean isClosing = false;

    // 连接回调
    private final IConnectCallback connectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror mirror) {
            synchronized (BleDevice.this) {
                bluetoothLeDevice = mirror.getBluetoothLeDevice();

                ViseLog.i("onConnectSuccess");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        processConnectCallback(CONNECT_SUCCESS);
                    }
                });
            }
        }
        @Override
        public void onConnectFailure(final BleException exception) {
            synchronized (BleDevice.this) {
                stopCommandExecutor();

                bluetoothLeDevice = null;

                final BleDeviceConnectState state;
                if (exception instanceof TimeoutException)
                    state = CONNECT_CONNECTTIMEOUT;
                else
                    state = CONNECT_CONNECTFAILURE;

                ViseLog.i("onConnectFailure with state = " + state);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        processConnectCallback(state);
                    }
                });
            }
        }
        @Override
        public void onDisconnect(final boolean isActive) {
            synchronized (BleDevice.this) {
                stopCommandExecutor();

                bluetoothLeDevice = null;

                ViseLog.d("onDisconnect");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        processConnectCallback(CONNECT_DISCONNECT);
                    }
                });
            }
        }

        @Override
        public void onScanFinish(boolean result) {
            synchronized (BleDevice.this) {
                ViseLog.d("onScanFailure");

                final BleDeviceConnectState state;
                if (result)
                    state = CONNECT_SCANSUCCESS;
                else
                    state = CONNECT_SCANFAILURE;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        processConnectCallback(state);
                    }
                });
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

    public BleDeviceConnectState getDeviceConnectState() {
        return state;
    }

    public void setDeviceConnectState(BleDeviceConnectState state) {
        this.state = state;
    }

    // 发起连接
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
            deviceMirrorPool.disconnect(bluetoothLeDevice);
            isClosing = true;
        }
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


    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    // 可连接
    public synchronized boolean canConnect() {
        return (state != CONNECT_SUCCESS && state != CONNECT_CONNECTING && state != CONNECT_DISCONNECTING && state != CONNECT_SCANSUCCESS);
    }
    // 可断开
    public synchronized boolean canDisconnect() {
        return (state == CONNECT_SUCCESS);
    }
    // 可关闭
    public synchronized boolean canClose() {
        return (state != CONNECT_WAITING && isClosing == false);
    }

    // 创建Gatt命令执行器
    protected synchronized void createGattCommandExecutor() {
        ViseLog.i("create new command executor.");
        if(isCommandExecutorAlive()) return;
        DeviceMirror deviceMirror = deviceMirrorPool.getDeviceMirror(bluetoothLeDevice);
        if(deviceMirror == null) return;

        commandExecutor = new GattCommandSerialExecutor(deviceMirror);
        commandExecutor.start();
    }

    // 停止Gatt命令执行器
    protected void stopCommandExecutor() {
        if(commandExecutor != null) commandExecutor.stop();
    }

    // Gatt命令执行器是否Alive
    protected boolean isCommandExecutorAlive() {
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
    // 连接回调处理函数
    private synchronized void processConnectCallback(BleDeviceConnectState state) {
        setDeviceConnectState(state);
        notifyConnectStateObservers();

        switch (state) {
            case CONNECT_SUCCESS:
                executeAfterConnectSuccess();
                break;

            case CONNECT_CONNECTTIMEOUT:
            case CONNECT_CONNECTFAILURE:
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
}

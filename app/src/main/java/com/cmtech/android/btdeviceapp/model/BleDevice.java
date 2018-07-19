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
import com.cmtech.android.btdeviceapp.interfa.IBleDevice;
import com.vise.log.ViseLog;


import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.btdeviceapp.model.BleDeviceConnectState.*;

/**
 * Created by bme on 2018/2/19.
 */

public abstract class BleDevice implements IBleDevice {
    // 设备镜像池
    private static DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();

    // 连接相关回调消息
    private static final int MSG_CONNECTCALLBACK       =  0;

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

    // 连接回调
    final IConnectCallback connectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror mirror) {
            synchronized (BleDevice.this) {
                bluetoothLeDevice = mirror.getBluetoothLeDevice();

                ViseLog.i("onConnectSuccess");

                sendMessage(MSG_CONNECTCALLBACK, BleDeviceConnectState.CONNECT_SUCCESS);
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

                sendMessage(MSG_CONNECTCALLBACK, state);
            }
        }
        @Override
        public void onDisconnect(final boolean isActive) {
            synchronized (BleDevice.this) {
                stopCommandExecutor();

                bluetoothLeDevice = null;

                ViseLog.d("onDisconnect");

                sendMessage(MSG_CONNECTCALLBACK, BleDeviceConnectState.CONNECT_DISCONNECT);
            }
        }
        @Override
        public void onScanFinish(boolean result) {
            ViseLog.d("onScanFailure");

            if(result)
                sendMessage(MSG_CONNECTCALLBACK, BleDeviceConnectState.CONNECT_SCANSUCCESS);
            else
                sendMessage(MSG_CONNECTCALLBACK, BleDeviceConnectState.CONNECT_SCANFAILURE);
        }
    };

    // 消息分发：用来处理连接和通信回调后产生的消息，由于有些要修改GUI，所以使用主线程
    protected final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_CONNECTCALLBACK) {
                // 处理连接回调消息
                processConnectCallbackMessage((BleDeviceConnectState)msg.obj);
            } else {
                // 处理Gatt相关消息
                processGattCallbackMessage(msg);
            }
        }
    };



    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    // 构造器
    public BleDevice(BleDeviceBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    @Override
    public String getMacAddress() {
        return basicInfo.getMacAddress();
    }

    @Override
    public void setMacAddress(String macAddress) {
        basicInfo.setMacAddress(macAddress);
    }

    @Override
    public String getNickName() {
        return basicInfo.getNickName();
    }

    @Override
    public void setNickName(String nickName) {
        basicInfo.setNickName(nickName);
    }

    @Override
    public String getUuidString() {
        return basicInfo.getUuidString();
    }

    @Override
    public void setUuidString(String uuidString) {
        basicInfo.setUuidString(uuidString);
    }

    @Override
    public boolean isAutoConnected() {
        return basicInfo.isAutoConnected();
    }

    @Override
    public void setAutoConnected(boolean autoConnected) {
        basicInfo.setAutoConnected(autoConnected);
    }

    @Override
    public String getImagePath() {
        return basicInfo.getImagePath();
    }

    @Override
    public void setImagePath(String imagePath) {
        basicInfo.setImagePath(imagePath);
    }

    @Override
    public BleDeviceBasicInfo getBasicInfo() {
        return basicInfo;
    }

    @Override
    public BleDeviceConnectState getDeviceConnectState() {
        return state;
    }

    @Override
    public void setDeviceConnectState(BleDeviceConnectState state) {
        this.state = state;
    }

    // 发起连接
    @Override
    public synchronized void connect() {
        if(canConnect()) {
            setDeviceConnectState(CONNECT_CONNECTING);
            notifyConnectStateObservers();

            MyApplication.getViseBle().connectByMac(getMacAddress(), connectCallback);
        }
    }

    // 断开连接
    @Override
    public synchronized void disconnect() {
        stopCommandExecutor();

        if(canDisconnect()) {

            setDeviceConnectState(CONNECT_DISCONNECTING);
            notifyConnectStateObservers();

            deviceMirrorPool.disconnect(bluetoothLeDevice);
        }
    }

    // 关闭设备
    @Override
    public synchronized void close() {
        disconnect();
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
    @Override
    public void registerConnectStateObserver(IBleDeviceConnectStateObserver observer) {
        if(!connectStateObserverList.contains(observer)) {
            connectStateObserverList.add(observer);
        }
    }

    // 删除连接状态观察者
    @Override
    public void removeConnectStateObserver(IBleDeviceConnectStateObserver observer) {
        int index = connectStateObserverList.indexOf(observer);
        if(index >= 0) {
            connectStateObserverList.remove(index);
        }
    }

    // 通知连接状态观察者
    @Override
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
    public boolean canConnect() {
        return (state != CONNECT_SUCCESS && state != CONNECT_CONNECTING && state != CONNECT_DISCONNECTING && state != CONNECT_SCANSUCCESS);
    }

    public boolean canDisconnect() {
        return (state == CONNECT_SUCCESS);
    }

    public boolean canClose() {
        return (state != CONNECT_CONNECTING && state != CONNECT_DISCONNECTING && state != CONNECT_SCANSUCCESS);
    }

    // 停止命令执行器
    protected void stopCommandExecutor() {
        if(commandExecutor != null) commandExecutor.stop();
    }

    protected boolean isCommandExecutorAlive() {
        return ((commandExecutor != null) && commandExecutor.isAlive());
    }

    protected synchronized void createGattCommandExecutor() {
        ViseLog.i("create new command executor.");
        if(isCommandExecutorAlive()) return;
        DeviceMirror deviceMirror = deviceMirrorPool.getDeviceMirror(bluetoothLeDevice);
        if(deviceMirror == null) return;

        commandExecutor = new GattCommandSerialExecutor(deviceMirror);
        commandExecutor.start();
    }

    // 获取设备上element对应的Gatt Object
    protected Object getGattObject(BleGattElement element) {
        DeviceMirror deviceMirror = deviceMirrorPool.getDeviceMirror(bluetoothLeDevice);
        if(deviceMirror == null || element == null) return null;
        return element.retrieveGattObject(deviceMirror);
    }

    // 发送消息
    protected void sendMessage(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }


    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    // 连接结果处理函数
    private synchronized void processConnectCallbackMessage(BleDeviceConnectState state) {
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
                break;

            case CONNECT_SCANFAILURE:
                // 扫描错误，什么也不做
                break;

            case CONNECT_SCANSUCCESS:
                break;

            default:
                break;
        }
    }
}

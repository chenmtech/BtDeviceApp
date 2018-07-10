package com.cmtech.android.btdeviceapp.model;

import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.TimeoutException;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceConnectStateObserver;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceInterface;
import com.vise.log.ViseLog;


import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.btdeviceapp.model.DeviceConnectState.*;

/**
 * Created by bme on 2018/2/19.
 */

public abstract class BLEDevice implements IBLEDeviceInterface {
    // 连接相关回调消息
    private static final int MSG_CONNECTCALLBACK       =  0;

    // 设备基本信息
    private final BLEDeviceBasicInfo basicInfo;

    // 设备镜像，连接成功后才会赋值。连接断开后，其Gatt将close
    protected DeviceMirror deviceMirror = null;

    // 设备连接状态
    private DeviceConnectState state = DeviceConnectState.CONNECT_WAITING;

    // GATT命令串行执行器
    protected GattCommandSerialExecutor commandExecutor;

    // 连接状态观察者列表
    private final List<IBLEDeviceConnectStateObserver> connectStateObserverList = new LinkedList<>();

    // 构造器
    public BLEDevice(BLEDeviceBasicInfo basicInfo) {
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
    public BLEDeviceBasicInfo getBasicInfo() {
        return basicInfo;
    }

    @Override
    public DeviceConnectState getDeviceConnectState() {
        return state;
    }

    @Override
    public void setDeviceConnectState(DeviceConnectState state) {
        this.state = state;
    }

    private boolean isCommandExecutorAlive() {
        return ((commandExecutor != null) && commandExecutor.isAlive());
    }

    public synchronized void createGattCommandExecutor() {
        ViseLog.i("create new command executor.");
        if(isCommandExecutorAlive()) return;

        commandExecutor = new GattCommandSerialExecutor(deviceMirror);
        commandExecutor.start();
    }

    // 连接结果类
    static final class ConnectResultObject {
        final DeviceConnectState state;
        final Object obj;

        ConnectResultObject(DeviceConnectState state, Object obj) {
            this.state = state;
            this.obj = obj;
        }
    }

    // 连接回调
    final IConnectCallback connectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror mirror) {
            DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();

            if (deviceMirrorPool.isContainDevice(mirror)) {
                deviceMirror = mirror;
                ViseLog.i("onConnectSuccess");

                sendMessage(MSG_CONNECTCALLBACK, new ConnectResultObject(DeviceConnectState.CONNECT_SUCCESS, deviceMirror));
            }
        }

        @Override
        public void onConnectFailure(BleException exception) {
            DeviceConnectState state;
            if(exception instanceof TimeoutException)
                state = CONNECT_TIMEOUT;
            else
                state = CONNECT_FAILURE;

            ViseLog.i("onConnectFailure with state = " + state);

            sendMessage(MSG_CONNECTCALLBACK, new ConnectResultObject(state, exception));
        }

        @Override
        public void onDisconnect(boolean isActive) {
            ViseLog.d("onDisconnect");

            sendMessage(MSG_CONNECTCALLBACK, new ConnectResultObject(DeviceConnectState.CONNECT_DISCONNECT, isActive));
        }
    };

    // 消息分发：用来处理连接和通信回调后产生的消息，由于有些要修改GUI，所以使用主线程
    final protected Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            ViseLog.i("processing the message " + msg);
            if(msg.what == MSG_CONNECTCALLBACK) {
                // 处理连接相关消息
                processConnectMessage((ConnectResultObject)msg.obj);
            } else {
                // 处理Gatt相关消息
                processGattMessage(msg);
            }
        }
    };

    // 连接结果处理函数
    private synchronized void processConnectMessage(ConnectResultObject result) {
        setDeviceConnectState(result.state);
        notifyConnectStateObservers();

        switch (result.state) {
            case CONNECT_SUCCESS:
                onConnectSuccess((DeviceMirror)result.obj);
                break;
            case CONNECT_TIMEOUT:
            case CONNECT_FAILURE:
                onConnectFailure((BleException)result.obj);
                break;
            case CONNECT_DISCONNECT:
                onDisconnect((Boolean) result.obj);
                break;

            default:
                break;
        }
    }

    // 连接成功处理
    private void onConnectSuccess(DeviceMirror mirror) {
        executeAfterConnectSuccess();
    }

    // 连接失败处理
    private void onConnectFailure(BleException exception) {
        // 连接失败后，立刻断开连接，不做其他处理
        disconnect();
    }

    // 断开连接处理
    private void onDisconnect(Boolean isActive) {
        executeAfterDisconnect(isActive);
    }

    // 停止命令执行器
    public void stopCommandExecutor() {
        if(commandExecutor != null) commandExecutor.stop();
    }

    // 判断是否连接
    public boolean isConnect() {
        return ((deviceMirror != null) && MyApplication.getViseBle().isConnect(deviceMirror.getBluetoothLeDevice()));
    }

    // 发起连接
    @Override
    public synchronized void connect() {
        if(state == CONNECT_SUCCESS || state == CONNECT_CONNECTING || state == CONNECT_DISCONNECTING) return;

        setDeviceConnectState(CONNECT_CONNECTING);
        notifyConnectStateObservers();

        MyApplication.getViseBle().connectByMac(getMacAddress(), connectCallback);
    }

    // 断开连接
    @Override
    public synchronized void disconnect() {
        if(state == CONNECT_SUCCESS) {

            setDeviceConnectState(CONNECT_DISCONNECTING);
            notifyConnectStateObservers();

            stopCommandExecutor();

            if (deviceMirror != null) {
                deviceMirror.disconnect();
                deviceMirror.removeAllCallback();
                //MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(deviceMirror);
            } else {
                setDeviceConnectState(CONNECT_DISCONNECT);
                notifyConnectStateObservers();
            }
        }
    }

    // 关闭设备
    @Override
    public synchronized void close() {
        stopCommandExecutor();

        // 断开连接
        disconnect();

        if(deviceMirror != null)
            deviceMirror.close();

        state = CONNECT_WAITING;

        notifyConnectStateObservers();

        // 清空连接状态观察者列表
        connectStateObserverList.clear();
    }

    private void clearDeviceMirror() {
        if (deviceMirror != null) {
            MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(deviceMirror);
            //deviceMirror.clear();     // 不能clear，否则下次连接出错
        }
    }

    // 获取设备的服务列表
    @Override
    public List<BluetoothGattService> getServices() {
        if(deviceMirror != null && deviceMirror.getBluetoothGatt() != null) {
            return deviceMirror.getBluetoothGatt().getServices();
        }
        return null;
    }

    // 获取设备上element对应的Gatt Object
    protected Object getGattObject(BluetoothGattElement element) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BLEDevice that = (BLEDevice) o;
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
    public void registerConnectStateObserver(IBLEDeviceConnectStateObserver observer) {
        if(!connectStateObserverList.contains(observer)) {
            connectStateObserverList.add(observer);
        }
    }

    // 删除连接状态观察者
    @Override
    public void removeConnectStateObserver(IBLEDeviceConnectStateObserver observer) {
        int index = connectStateObserverList.indexOf(observer);
        if(index >= 0) {
            connectStateObserverList.remove(index);
        }
    }

    // 通知连接状态观察者
    @Override
    public void notifyConnectStateObservers() {
        for(final IBLEDeviceConnectStateObserver observer : connectStateObserverList) {
            if(observer != null) {
                // 保证在主线程更新连接状态
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        observer.updateConnectState(BLEDevice.this);
                    }
                });
            }
        }
    }

}

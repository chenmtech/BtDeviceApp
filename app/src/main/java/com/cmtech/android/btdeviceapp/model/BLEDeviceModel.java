package com.cmtech.android.btdeviceapp.model;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.TimeoutException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceConnectStateObserver;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceModelInterface;
import com.vise.log.ViseLog;


import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.btdeviceapp.model.DeviceConnectState.*;

/**
 * Created by bme on 2018/2/19.
 */

public abstract class BLEDeviceModel implements IBLEDeviceModelInterface{

    private static final int MSG_CONNECTCALLBACK       =  0;                // 连接相关回调消息
    private static final int MSG_NORMALGATTCALLBACK    =  1;                // Gatt相关回调消息
    //private static final int MSG_GATTFAILURE           =  2;                // Gatt错误消息

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
    public BLEDeviceModel(BLEDeviceBasicInfo basicInfo) {
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

    public synchronized void createCommandExecutor() {
        ViseLog.i("create new command executor.");
        if(isCommandExecutorAlive()) return;

        commandExecutor = new GattCommandSerialExecutor(deviceMirror);
        commandExecutor.start();
    }

    // 连接结果类
    static class ConnectResultObject {
        DeviceConnectState state;
        Object obj;

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
                //setDeviceConnectState(CONNECT_SUCCESS);
                //notifyConnectStateObservers();
                ViseLog.i("onConnectSuccess");
                Message msg = new Message();
                msg.what = MSG_CONNECTCALLBACK;
                msg.obj = new ConnectResultObject(DeviceConnectState.CONNECT_SUCCESS, deviceMirror);
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onConnectFailure(BleException exception) {
            DeviceConnectState state;
            if(exception instanceof TimeoutException)
                state = CONNECT_SCANTIMEOUT;
            else
                state = CONNECT_FAILURE;

            setDeviceConnectState(state);
            notifyConnectStateObservers();

            ViseLog.i("onConnectFailure with state = " + state);
            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(state, exception);
            //handler.sendMessage(msg);
        }

        @Override
        public void onDisconnect(boolean isActive) {
            ViseLog.d("onDisconnect");

            setDeviceConnectState(CONNECT_DISCONNECT);
            notifyConnectStateObservers();

            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(DeviceConnectState.CONNECT_DISCONNECT, isActive);
            //handler.sendMessage(msg);
        }
    };

    // 消息分发：用来处理连接和通信回调后产生的消息，由于有些要修改GUI，所以使用主线程
    final protected Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            ViseLog.i("message what = " + msg.what);
            switch (msg.what) {
                // 连接消息
                case MSG_CONNECTCALLBACK:
                    processConnectMessage((ConnectResultObject)msg.obj);
                    break;

                // 一般Gatt消息
                case MSG_NORMALGATTCALLBACK:
                    //processCommonGattMessage((BluetoothGattChannel) msg.obj);
                    break;

                // 主要用来处理Notify和Indicate之类的消息
                default:
                    processSpecialGattMessage(msg);
                    break;

            }

        }
    };

    // 连接结果处理函数
    private synchronized void processConnectMessage(ConnectResultObject result) {
        //if(state == result.state) return;   // 有时候会有连续多次回调，忽略后面的回调处理

        setDeviceConnectState(result.state);
        notifyConnectStateObservers();

        switch (result.state) {
            case CONNECT_SUCCESS:
                onConnectSuccess((DeviceMirror)result.obj);
                break;
            case CONNECT_SCANTIMEOUT:
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
        //clearDeviceMirror();
        disconnect();

        executeAfterConnectFailure();

    }

    // 断开连接处理
    private void onDisconnect(Boolean isActive) {
        //clearDeviceMirror();

        executeAfterDisconnect(isActive);
    }

    public void stopCommandExecutor() {
        if(commandExecutor != null) commandExecutor.stop();
    }

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

            if (deviceMirror != null) {
                //MyApplication.getViseBle().disconnect(deviceMirror.getBluetoothLeDevice());
                deviceMirror.disconnect();
                deviceMirror.removeAllCallback();
                //MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(deviceMirror);
                stopCommandExecutor();

            } else {
                setDeviceConnectState(CONNECT_DISCONNECT);
                notifyConnectStateObservers();
            }
        }
    }

    // 关闭设备
    @Override
    public synchronized void close() {
        //stopCommandExecutor();

        // 断开连接
        //clearDeviceMirror();
        disconnect();

        //clearDeviceMirror();

        state = CONNECT_WAITING;

        notifyConnectStateObservers();

        // 清空连接状态观察者列表
        connectStateObserverList.clear();
    }

    private void clearDeviceMirror() {
        if (deviceMirror != null) {
            //deviceMirror.disconnect();
            //MyApplication.getViseBle().disconnect(deviceMirror.getBluetoothLeDevice());
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BLEDeviceModel that = (BLEDeviceModel) o;
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
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        observer.updateConnectState(BLEDeviceModel.this);
                    }
                });

            }
        }
    }

}

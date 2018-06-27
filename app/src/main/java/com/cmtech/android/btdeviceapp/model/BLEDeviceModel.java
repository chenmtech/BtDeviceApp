package com.cmtech.android.btdeviceapp.model;

import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.TimeoutException;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceConnectStateObserver;
import com.cmtech.android.btdeviceapp.MyApplication;


import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.btdeviceapp.model.DeviceState.*;

/**
 * Created by bme on 2018/2/19.
 */

public abstract class BLEDeviceModel {

    private static final int MSG_CONNECTCALLBACK    =  0;         // 连接相关回调消息

    // 设备基本信息
    private final BLEDeviceBasicInfo basicInfo;

    public BLEDeviceModel(BLEDeviceBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public int getId() {
        return basicInfo.getId();
    }

    public void setId(int id) {
        basicInfo.setId(id);
    }

    public String getMacAddress() {
        return basicInfo.getMacAddress();
    }

    public void setMacAddress(String macAddress) {
        basicInfo.setMacAddress(macAddress);
    }

    public String getNickName() {
        return basicInfo.getNickName();
    }

    public void setNickName(String nickName) {
        basicInfo.setNickName(nickName);
    }

    public String getUuidString() {
        return basicInfo.getUuidString();
    }

    public void setUuidString(String uuidString) {
        basicInfo.setUuidString(uuidString);
    }

    public boolean isAutoConnected() {
        return basicInfo.isAutoConnected();
    }

    public void setAutoConnected(boolean autoConnected) {
        basicInfo.setAutoConnected(autoConnected);
    }

    public String getImagePath() {
        return basicInfo.getImagePath();
    }

    public void setImagePath(String imagePath) {
        basicInfo.setImagePath(imagePath);
    }

    public BLEDeviceBasicInfo getBasicInfo() {
        return basicInfo;
    }

    // 设备状态
    DeviceState state = DeviceState.CONNECT_WAITING;

    public DeviceState getDeviceState() {
        return state;
    }

    public void setDeviceState(DeviceState state) {
        this.state = state;
    }

    // 设备镜像，连接成功后才会赋值。连接断开后，其Gatt将close
    DeviceMirror deviceMirror = null;

    // 连接状态观察者列表
    final List<IBLEDeviceConnectStateObserver> connectStateObserverList = new LinkedList<>();

    // 用来处理连接和通信回调后产生的消息，由于有些要修改GUI，所以使用主线程
    final protected Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECTCALLBACK:
                    processConnectResultObject((ConnectResultObject)msg.obj);
                    break;

                default:
                    processDeviceSpecialMessage(msg);
                    break;

            }

        }
    };

    // 连接结果类
    static class ConnectResultObject {
        DeviceState state;
        Object obj;

        ConnectResultObject(DeviceState state, Object obj) {
            this.state = state;
            this.obj = obj;
        }
    }

    // 连接回调
    final IConnectCallback connectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            Log.d("CONNECTCALLBACK", "onConnectSuccess");
            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(DeviceState.CONNECT_SUCCESS, deviceMirror);
            handler.removeMessages(MSG_CONNECTCALLBACK);
            handler.sendMessage(msg);
        }

        @Override
        public void onConnectFailure(BleException exception) {
            DeviceState state;
            if(exception instanceof TimeoutException)
                state = CONNECT_SCANTIMEOUT;
            else
                state = CONNECT_FAILURE;

            Log.d("CONNECTCALLBACK", "onConnectFailure with state = " + state);
            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(state, exception);
            handler.removeMessages(MSG_CONNECTCALLBACK);
            handler.sendMessage(msg);
        }

        @Override
        public void onDisconnect(boolean isActive) {
            Log.d("CONNECTCALLBACK", "onDisconnect");
            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(DeviceState.CONNECT_DISCONNECT, isActive);
            handler.removeMessages(MSG_CONNECTCALLBACK);
            handler.sendMessage(msg);
        }
    };

    // 连接结果处理函数
    private void processConnectResultObject(ConnectResultObject result) {
        //if(state == result.state) return;   // 有时候会有连续多次回调，忽略后面的回调处理

        setDeviceState(result.state);
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
        DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();

        if (deviceMirrorPool.isContainDevice(mirror)) {
            this.deviceMirror = mirror;
            executeAfterConnectSuccess();
        }
    }

    // 连接失败处理
    private void onConnectFailure(BleException exception) {
        //clearDeviceMirror();
        executeAfterConnectFailure();
    }

    // 断开连接处理
    private void onDisconnect(Boolean isActive) {
        //clearDeviceMirror();
        executeAfterDisconnect(isActive);
    }



    // 发起连接
    public synchronized void connect() {
        if(state == CONNECT_SUCCESS || state == CONNECT_CONNECTING || state == CONNECT_DISCONNECTING) return;
        setDeviceState(CONNECT_CONNECTING);
        notifyConnectStateObservers();

        MyApplication.getViseBle().connectByMac(getMacAddress(), connectCallback);
    }

    // 断开连接
    public synchronized void disconnect() {
        if(state == CONNECT_SUCCESS) {
            setDeviceState(CONNECT_DISCONNECTING);
            notifyConnectStateObservers();

            if (deviceMirror != null) {
                MyApplication.getViseBle().disconnect(deviceMirror.getBluetoothLeDevice());
            } else {
                setDeviceState(CONNECT_DISCONNECT);
                notifyConnectStateObservers();
            }
        }
    }

    // 关闭设备
    public synchronized void close() {
        // 清空连接状态观察者列表
        connectStateObserverList.clear();

        // 断开连接
        clearDeviceMirror();
    }

    private void clearDeviceMirror() {
        if (deviceMirror != null) {
            MyApplication.getViseBle().disconnect(deviceMirror.getBluetoothLeDevice());
            MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(deviceMirror);
            //deviceMirror.clear();     // 不能clear，否则下次连接出错
        }
    }

    // 获取设备的服务列表
    public List<BluetoothGattService> getServices() {
        if(deviceMirror != null && deviceMirror.getBluetoothGatt() != null) {
            return deviceMirror.getBluetoothGatt().getServices();
        }
        return null;
    }

    // 获取设备上element对应的Gatt Object
    public Object getGattObject(BluetoothGattElement element) {
        if(deviceMirror == null || element == null) return null;
        return element.retrieveGattObject(deviceMirror);
    }

    /**
     * 执行"读数据单元"操作
     * @param element 数据单元
     * @param dataOpCallback 读回调
     * @return 是否添加成功
     */
    public boolean executeReadCommand(BluetoothGattElement element, IBleCallback dataOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_READ)
                .setDataOpCallback(dataOpCallback).build();
        return executeGattCommand(command);
    }

    /**
     * 执行"写数据单元"操作
     * @param element 数据单元
     * @param data 数据
     * @param dataOpCallback 写回调
     * @return 是否添加成功
     */
    public boolean executeWriteCommand(BluetoothGattElement element, byte[] data, IBleCallback dataOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setData(data)
                .setDataOpCallback(dataOpCallback).build();
        return executeGattCommand(command);
    }

    /**
     * 执行"数据单元Notify"操作
     * @param element 数据单元
     * @param enable 使能或失能
     * @param dataOpCallback 写回调
     * @param notifyOpCallback Notify数据回调
     * @return 是否添加成功
     */
    public boolean executeNotifyCommand(BluetoothGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback notifyOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                .setData((enable) ? new byte[]{0x01} : new byte[]{0x00})
                .setDataOpCallback(dataOpCallback)
                .setNotifyOpCallback(notifyOpCallback).build();
        return executeGattCommand(command);
    }

    /**
     * 执行"数据单元Indicate"操作
     * @param element 数据单元
     * @param enable 使能或失能
     * @param dataOpCallback 写回调
     * @param indicateOpCallback Notify数据回调
     * @return 是否添加成功
     */
    public boolean executeIndicateCommand(BluetoothGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback indicateOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_INDICATE)
                .setData((enable) ? new byte[]{0x01} : new byte[]{0x00})
                .setDataOpCallback(dataOpCallback)
                .setNotifyOpCallback(indicateOpCallback).build();
        return executeGattCommand(command);
    }

    private boolean executeGattCommand(BluetoothGattCommand command) {
        if(command == null || state != CONNECT_SUCCESS) return false;
        return command.execute();
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
    public void registerConnectStateObserver(IBLEDeviceConnectStateObserver obersver) {
        if(!connectStateObserverList.contains(obersver)) {
            connectStateObserverList.add(obersver);
        }
    }

    // 删除连接状态观察者
    public void removeConnectStateObserver(IBLEDeviceConnectStateObserver obersver) {
        int index = connectStateObserverList.indexOf(obersver);
        if(index >= 0) {
            connectStateObserverList.remove(index);
        }
    }

    // 通知连接状态观察者
    public void notifyConnectStateObservers() {
        for(final IBLEDeviceConnectStateObserver obersver : connectStateObserverList) {
            if(obersver != null) {
                obersver.updateConnectState(this);
            }
        }
    }

    public abstract void executeAfterConnectSuccess();

    public abstract void executeAfterConnectFailure();

    public abstract void executeAfterDisconnect(boolean isActive);

    public abstract void processDeviceSpecialMessage(Message msg);
}

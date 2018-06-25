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
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceObserver;
import com.cmtech.android.btdeviceapp.MyApplication;


import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.btdeviceapp.interfa.IBLEDeviceObserver.*;
import static com.cmtech.android.btdeviceapp.model.DeviceState.*;

/**
 * Created by bme on 2018/2/19.
 */

public abstract class BLEDeviceModel {

    private static final int MSG_CONNECTCALLBACK    =  0;         // 连接相关回调消息

    private final BLEDevicePersistantInfo persistantInfo;

    public BLEDeviceModel(BLEDevicePersistantInfo persistantInfo) {
        this.persistantInfo = persistantInfo;
    }

    public int getId() {
        return persistantInfo.getId();
    }

    public void setId(int id) {
        persistantInfo.setId(id);
    }

    public String getMacAddress() {
        return persistantInfo.getMacAddress();
    }

    public void setMacAddress(String macAddress) {
        persistantInfo.setMacAddress(macAddress);
    }

    public String getNickName() {
        return persistantInfo.getNickName();
    }

    public void setNickName(String nickName) {
        persistantInfo.setNickName(nickName);
    }

    public String getUuidString() {
        return persistantInfo.getUuidString();
    }

    public void setUuidString(String uuidString) {
        persistantInfo.setUuidString(uuidString);
    }

    public boolean isAutoConnected() {
        return persistantInfo.isAutoConnected();
    }

    public void setAutoConnected(boolean autoConnected) {
        persistantInfo.setAutoConnected(autoConnected);
    }

    public String getImagePath() {
        return persistantInfo.getImagePath();
    }

    public void setImagePath(String imagePath) {
        persistantInfo.setImagePath(imagePath);
    }

    public BLEDevicePersistantInfo getPersistantInfo() {
        return persistantInfo;
    }

    // 设备状态
    DeviceState state = DeviceState.CONNECT_WAITING;

    // 设备镜像，连接成功后才会赋值。连接断开会赋值null
    DeviceMirror deviceMirror = null;

    // 观察者列表
    final List<IBLEDeviceObserver> observerList = new LinkedList<>();


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

    static class ConnectResultObject {
        DeviceState state;
        Object obj;

        ConnectResultObject(DeviceState state, Object obj) {
            this.state = state;
            this.obj = obj;
        }
    }

    private void processConnectResultObject(ConnectResultObject result) {
        setDeviceState(result.state);
        notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

        switch (result.state) {
            case CONNECT_SUCCESS:
                onConnectSuccess((DeviceMirror)result.obj);
                break;
            case CONNECT_SCANTIMEOUT:
            case CONNECT_ERROR:
                onConnectFailure((BleException)result.obj);
                break;
            case CONNECT_DISCONNECT:
                onDisconnect((Boolean) result.obj);
                break;

            default:
                break;
        }

    }

    private void onConnectSuccess(DeviceMirror mirror) {
        DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();

        if (deviceMirrorPool.isContainDevice(mirror)) {

            this.deviceMirror = mirror;

            executeAfterConnectSuccess();
        }
    }

    private void onConnectFailure(BleException exception) {
        removeDeviceMirrorFromPool();
        executeAfterConnectFailure();
    }

    private void onDisconnect(Boolean isActive) {
        removeDeviceMirrorFromPool();
        executeAfterDisconnect(isActive);
    }

    private void removeDeviceMirrorFromPool() {
        if(deviceMirror != null) {
            MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(deviceMirror);
            deviceMirror.clear();
            deviceMirror = null;
        }
    }

    // 连接回调
    final IConnectCallback connectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(DeviceState.CONNECT_SUCCESS, deviceMirror);
            handler.sendMessage(msg);
        }

        @Override
        public void onConnectFailure(BleException exception) {
            DeviceState state;
            if(exception instanceof TimeoutException)
                state = CONNECT_SCANTIMEOUT;
            else
                state = CONNECT_ERROR;
            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(state, exception);
            handler.sendMessage(msg);
        }

        @Override
        public void onDisconnect(boolean isActive) {
            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(DeviceState.CONNECT_DISCONNECT, isActive);
            handler.sendMessage(msg);
        }
    };


    public DeviceState getDeviceState() {
        return state;
    }

    public void setDeviceState(DeviceState state) {
        this.state = state;
    }

    public List<BluetoothGattService> getServices() {
        if(deviceMirror != null && deviceMirror.getBluetoothGatt() != null) {
            return deviceMirror.getBluetoothGatt().getServices();
        }
        return null;
    }

    // 登记观察者
    public void registerDeviceObserver(IBLEDeviceObserver obersver) {
        if(!observerList.contains(obersver)) {
            observerList.add(obersver);
        }
    }

    // 删除观察者
    public void removeDeviceObserver(IBLEDeviceObserver obersver) {
        int index = observerList.indexOf(obersver);
        if(index >= 0) {
            observerList.remove(index);
        }
    }

    // 通知观察者
    public void notifyDeviceObservers(final int type) {
        for(final IBLEDeviceObserver obersver : observerList) {
            if(obersver != null) {
                obersver.updateDeviceInfo(this, type);
            }
        }
    }

    // 发起连接
    public synchronized void connect() {
        if(state == CONNECT_WAITING || state == CONNECT_DISCONNECT) {

            setDeviceState(CONNECT_PROCESS);
            notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

            MyApplication.getViseBle().connectByMac(getMacAddress(), connectCallback);
        }
    }

    // 断开连接
    public synchronized void disconnect() {
        if(state == CONNECT_SUCCESS || state == CONNECT_ERROR || state == CONNECT_SCANTIMEOUT) {
            setDeviceState(CONNECT_DISCONNECTING);
            notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

            if (deviceMirror != null) {
                MyApplication.getViseBle().disconnect(deviceMirror.getBluetoothLeDevice());
            } else {
                setDeviceState(CONNECT_DISCONNECT);
                notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);
            }
        }
    }

    // 关闭设备
    public synchronized void close() {
        observerList.clear();
        disconnect();
        removeDeviceMirrorFromPool();
    }

    // 在设备上获取element的Gatt Object
    public Object getGattObject(BluetoothGattElement element) {
        if(deviceMirror == null || element == null) return null;
        return element.retrieveGattObject(deviceMirror);
    }

    /**
     * 将"读数据单元"操作加入串行执行器
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
     * 将"写数据单元"操作加入串行执行器
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
     * 将"数据单元Notify"操作加入串行执行器
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
     * 生成"数据单元Indicate"命令，并加入串行执行器
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
        if(command == null) return false;
        if(state != CONNECT_SUCCESS) return false;
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


    public abstract void executeAfterConnectSuccess();

    public abstract void executeAfterConnectFailure();

    public abstract void executeAfterDisconnect(boolean isActive);

    public abstract void processDeviceSpecialMessage(Message msg);
}

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
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceObserver;
import com.cmtech.android.btdeviceapp.MyApplication;

import org.litepal.crud.DataSupport;

import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.btdeviceapp.interfa.IBLEDeviceObserver.*;
import static com.cmtech.android.btdeviceapp.model.DeviceState.*;

/**
 * Created by bme on 2018/2/19.
 */

public class BLEDeviceModel extends DataSupport {

    private static final int MSG_CONNECTCALLBACK    =  100;         // 连接相关回调消息


    // 数据库保存的字段
    // id
    private int id;

    // mac地址
    private String macAddress;

    // 设备昵称
    private String nickName;

    // 设备广播Uuid Short String
    private String uuidString;

    // 是否自动连接
    private boolean isAutoConnected;

    // 图标
    private String imagePath;

    // 数据库不保存的变量
    // 设备状态
    DeviceState state = DeviceState.CONNECT_WAITING;

    // 设备镜像，连接成功后才会赋值。连接失败会赋值null
    DeviceMirror deviceMirror = null;

    // 观察者列表
    final List<IBLEDeviceObserver> observerList = new LinkedList<>();


    // 用来处理连接和通信回调后产生的消息，由于有些要修改GUI，所以使用主线程
    final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECTCALLBACK:
                    processConnectResult((ConnectResult)msg.obj);
                    break;

                default:
                    break;

            }

        }
    };

    static class ConnectResult{
        DeviceState state;
        Object obj;

        ConnectResult(DeviceState state, Object obj) {
            this.state = state;
            this.obj = obj;
        }
    }

    // 连接回调
    final IConnectCallback connectCallback = new IConnectCallback() {
        private Message msg = new Message();

        {
            msg.what = MSG_CONNECTCALLBACK;
        }

        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            msg.obj = new ConnectResult(DeviceState.CONNECT_SUCCESS, deviceMirror);
            handler.sendMessage(msg);
        }

        @Override
        public void onConnectFailure(BleException exception) {
            DeviceState state;
            if(exception instanceof TimeoutException)
                state = CONNECT_SCANTIMEOUT;
            else
                state = CONNECT_ERROR;

            msg.obj = new ConnectResult(state, exception);
            handler.sendMessage(msg);
        }

        @Override
        public void onDisconnect(boolean isActive) {
            msg.obj = new ConnectResult(DeviceState.CONNECT_DISCONNECT, isActive);
            handler.sendMessage(msg);
        }
    };


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUuidString() {
        return uuidString;
    }

    public void setUuidString(String uuidString) {
        this.uuidString = uuidString;
    }

    public boolean isAutoConnected() {
        return isAutoConnected;
    }

    public void setAutoConnected(boolean autoConnected) {
        isAutoConnected = autoConnected;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

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

            MyApplication.getViseBle().connectByMac(macAddress, connectCallback);
        }
    }

    // 断开连接
    public synchronized void disconnect() {
        if(state == CONNECT_SUCCESS) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BLEDeviceModel that = (BLEDeviceModel) o;

        return macAddress != null ? macAddress.equals(that.macAddress) : that.macAddress == null;
    }

    @Override
    public int hashCode() {
        return macAddress != null ? macAddress.hashCode() : 0;
    }


    private void processConnectResult(ConnectResult result) {
        setDeviceState(result.state);
        notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

        switch (result.state) {
            case CONNECT_SUCCESS:
                onConnectSuccess((DeviceMirror)result.obj);
                break;
            case CONNECT_SCANTIMEOUT:
            case CONNECT_ERROR:
                onConnectFailure();
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

    private void onConnectFailure() {
        disconnect();
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

    public void executeAfterConnectSuccess() {

    }

    public void executeAfterDisconnect(boolean isActive) {

    }
}

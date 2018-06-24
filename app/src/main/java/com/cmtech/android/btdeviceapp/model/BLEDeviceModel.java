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
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.IConnectSuccessCallback;
import com.cmtech.android.btdeviceapp.interfa.IMyBluetoothDeviceObserver;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.vise.log.ViseLog;

import org.litepal.crud.DataSupport;

import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.btdeviceapp.interfa.IMyBluetoothDeviceObserver.*;
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

    // 存放打开Fragment
    DeviceFragment fragment;

    // Gatt命令串行执行器, 连接成功后才会创建。连接失败会赋值null
    GattSerialExecutor serialExecutor;

    // 观察者列表
    final List<IMyBluetoothDeviceObserver> obersvers = new LinkedList<>();


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

    // 连接成功后的回调。发起连接时要赋值
    IConnectSuccessCallback connectSuccessCallback;

    // 连接回调
    final IConnectCallback connectCallback = new IConnectCallback() {
        private Message msg = new Message();

        {
            msg.what = MSG_CONNECTCALLBACK;
        }

        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            msg.obj = new ConnectResult(DeviceState.CONNECT_SUCCESS, (Object)deviceMirror);
            handler.sendMessage(msg);
        }

        @Override
        public void onConnectFailure(BleException exception) {
            DeviceState state;
            if(exception instanceof TimeoutException)
                state = CONNECT_SCANTIMEOUT;
            else
                state = CONNECT_ERROR;

            msg.obj = new ConnectResult(state, null);
            handler.sendMessage(msg);
        }

        @Override
        public void onDisconnect(boolean isActive) {
            msg.obj = new ConnectResult(DeviceState.CONNECT_DISCONNECT, (Object)isActive);
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

    public GattSerialExecutor getSerialExecutor() {
        return serialExecutor;
    }

    public List<BluetoothGattService> getServices() {
        if(deviceMirror != null && deviceMirror.getBluetoothGatt() != null) {
            return deviceMirror.getBluetoothGatt().getServices();
        }
        return null;
    }

    public DeviceFragment getFragment() {
        return fragment;
    }

    public void setFragment(DeviceFragment fragment) {
        if(this.fragment != null) removeDeviceObserver(this.fragment);
        this.fragment = fragment;
        if(fragment != null) registerDeviceObserver(fragment);
    }

    // 判断设备是否有了Fragment
    public boolean hasFragment() {
        return fragment != null;
    }

    // 登记观察者
    public void registerDeviceObserver(IMyBluetoothDeviceObserver obersver) {
        if(!obersvers.contains(obersver)) {
            obersvers.add(obersver);
        }
    }

    // 删除观察者
    public void removeDeviceObserver(IMyBluetoothDeviceObserver obersver) {
        int index = obersvers.indexOf(obersver);
        if(index >= 0) {
            obersvers.remove(index);
        }
    }

    // 通知观察者
    public void notifyDeviceObservers(final int type) {
        for(final IMyBluetoothDeviceObserver obersver : obersvers) {
            if(obersver != null) {
                obersver.updateDeviceInfo(this, type);
            }
        }
    }

    // 发起连接
    public synchronized void connect(IConnectSuccessCallback connectSuccessCallback) {
        if(state == CONNECT_SUCCESS || state == CONNECT_PROCESS || state == CONNECT_DISCONNECTING) return;

        setDeviceState(CONNECT_PROCESS);
        notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

        this.connectSuccessCallback = connectSuccessCallback;

        if(deviceMirror != null) {
            // 一定要从Pool中清除DeviceMirror
            MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(deviceMirror);
            deviceMirror.clear();
            deviceMirror = null;
        }

        MyApplication.getViseBle().connectByMac(macAddress, connectCallback);
    }

    // 断开连接
    public synchronized void disconnect() {
        if(state == CONNECT_DISCONNECT || state == CONNECT_PROCESS || state == CONNECT_DISCONNECTING) return;

        setDeviceState(CONNECT_DISCONNECTING);
        notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);
        // 停止执行器
        if(serialExecutor != null && !serialExecutor.isAlive())
            serialExecutor.stop();

        if(deviceMirror != null) {
            MyApplication.getViseBle().disconnect(deviceMirror.getBluetoothLeDevice());
        } else {
            setDeviceState(CONNECT_DISCONNECT);
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
        state = result.state;

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

            serialExecutor = new GattSerialExecutor(mirror);

            ViseLog.d("onConnectSuccess: " + getNickName()+ " has created serial executor.");

            setDeviceState(CONNECT_SUCCESS);
            notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

            connectSuccessCallback.doAfterConnectSuccess(this);
        }
    }

    private void onConnectFailure() {
        notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connect(connectSuccessCallback);
            }
        }, 2000);
    }

    private void onDisconnect(Boolean isActive) {

        if(serialExecutor != null && serialExecutor.isAlive()) {
            serialExecutor.stop();
        }

        notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

        if(deviceMirror != null) {
            // 一定要从Pool中清除DeviceMirror
            MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(deviceMirror);
            deviceMirror.clear();
            deviceMirror = null;
        }
    }


}

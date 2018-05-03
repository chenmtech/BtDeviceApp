package com.cmtech.android.btdeviceapp.model;

import android.bluetooth.BluetoothGattService;

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.TimeoutException;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.IConnectSuccessCallback;
import com.cmtech.android.btdeviceapp.interfa.IMyBluetoothDeviceObserver;
import com.cmtech.android.btdeviceapp.util.Uuid;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.vise.log.ViseLog;

import org.litepal.crud.DataSupport;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.cmtech.android.ble.model.adrecord.AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE;
import static com.cmtech.android.btdeviceapp.interfa.IMyBluetoothDeviceObserver.*;

/**
 * Created by bme on 2018/2/19.
 */

public class MyBluetoothDevice extends DataSupport {


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

    // 存放连接后打开的Fragment
    DeviceFragment fragment;

    // Gatt命令串行执行器, 连接成功后才会创建。连接失败会赋值null
    GattSerialExecutor serialExecutor;

    // 观察者列表
    final List<IMyBluetoothDeviceObserver> obersvers = new LinkedList<>();

    // 连接成功后的回调。发起连接时要赋值
    IConnectSuccessCallback connectSuccessCallback;

    // 连接回调
    final IConnectCallback connectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();
            if (deviceMirrorPool.isContainDevice(deviceMirror)) {

                MyBluetoothDevice.this.deviceMirror = deviceMirror;

                serialExecutor = new GattSerialExecutor(deviceMirror);

                ViseLog.d("IConnectCallback.onConnectSuccess: " + MyBluetoothDevice.this.getNickName()+
                        " create serial executor.");

                setDeviceState(DeviceState.CONNECT_SUCCESS);
                notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

                connectSuccessCallback.doAfterConnectSuccess(MyBluetoothDevice.this);
            }
        }

        @Override
        public void onConnectFailure(BleException exception) {

            if(serialExecutor != null && !serialExecutor.isInterruped()) {
                serialExecutor.stop();
            }

            if(exception instanceof TimeoutException)
                setDeviceState(DeviceState.SCAN_TIMEOUT);
            else
                setDeviceState(DeviceState.CONNECT_ERROR);
            notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

            if(deviceMirror != null) {
                DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();
                if(deviceMirrorPool.isContainDevice(deviceMirror)) {
                    deviceMirrorPool.removeDeviceMirror(deviceMirror);
                }
                deviceMirror = null;
            }
        }

        @Override
        public void onDisconnect(boolean isActive) {
            if(serialExecutor != null && !serialExecutor.isInterruped()) {
                serialExecutor.stop();
            }

            setDeviceState(DeviceState.CONNECT_DISCONNECT);
            notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

            if(deviceMirror != null) {
                DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();
                if(deviceMirrorPool.isContainDevice(deviceMirror)) {
                    deviceMirrorPool.removeDeviceMirror(deviceMirror);
                }
                deviceMirror = null;
            }
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

    // 获取设备广播中包含的UUID
    public UUID getDeviceUuidInAd() {
        if(deviceMirror == null || deviceMirror.getBluetoothLeDevice() == null) return null;

        AdRecord record = deviceMirror.getBluetoothLeDevice()
                .getAdRecordStore().getRecord(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(record == null) return null;
        return Uuid.byteArrayToUuid(record.getData());
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
        setDeviceState(DeviceState.CONNECT_PROCESS);
        notifyDeviceObservers(TYPE_MODIFY_CONNECTSTATE);

        this.connectSuccessCallback = connectSuccessCallback;

        // 如果没有连接过，或者连接没有成功过
        if(deviceMirror == null || !MyApplication.getViseBle().getDeviceMirrorPool().isContainDevice(deviceMirror))
            MyApplication.getViseBle().connectByMac(macAddress, connectCallback);
        else
            deviceMirror.connect(connectCallback);
    }

    // 断开连接
    public synchronized void disconnect() {
        // 停止执行器
        if(serialExecutor != null && !serialExecutor.isInterruped())
            serialExecutor.stop();

        if(deviceMirror != null)
            MyApplication.getViseBle().disconnect(deviceMirror.getBluetoothLeDevice());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyBluetoothDevice that = (MyBluetoothDevice) o;

        return macAddress != null ? macAddress.equals(that.macAddress) : that.macAddress == null;
    }

    @Override
    public int hashCode() {
        return macAddress != null ? macAddress.hashCode() : 0;
    }


}

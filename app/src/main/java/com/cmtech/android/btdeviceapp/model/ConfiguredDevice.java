package com.cmtech.android.btdeviceapp.model;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.btdevice.common.DeviceFragment;
import com.cmtech.android.btdevice.common.Uuid;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.activity.MainActivity;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.ble.model.adrecord.AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE;

/**
 * Created by bme on 2018/2/19.
 */

public class ConfiguredDevice extends DataSupport {
    public static final int TYPE_MODIFY = 0;
    public static final int TYPE_ADD = 1;
    public static final int TYPE_DELETE = 2;

    // 数据库会保存的字段
    // id
    private int id;

    // mac地址
    private String macAddress;

    // 设备别名
    private String nickName;

    // 是否自动连接
    private boolean isAutoConnected;

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
        notifyDeviceObservers(TYPE_MODIFY);
    }

    public boolean isAutoConnected() {
        return isAutoConnected;
    }

    public void setAutoConnected(boolean autoConnected) {
        isAutoConnected = autoConnected;
    }

    // 数据库不会保存的变量
    // 连接状态
    ConnectState connectState = ConnectState.CONNECT_INIT;

    // 设备镜像，连接成功后才会赋值
    DeviceMirror deviceMirror = null;

    // 设备的Fragment，打开fragment后会设置
    DeviceFragment fragment;

    // 设备信息观察者接口
    public interface IConfiguredDeviceObersver {
        void updateDeviceInfo(ConfiguredDevice device, int type);
    }

    // 观察者
    List<IConfiguredDeviceObersver> obersvers = new ArrayList<>();

    public DeviceFragment getFragment() {return fragment;}

    public void setFragment(DeviceFragment fragment) {this.fragment = fragment;}

    public ConnectState getConnectState() {return connectState;}

    public String getConnectStateString() {
        String rtn = "等待连接";
        switch (connectState) {
            case CONNECT_INIT:
                rtn = "等待连接";
                break;
            case CONNECT_PROCESS:
                rtn = "连接中...";
                break;
            case CONNECT_DISCONNECT:
                rtn = "连接断开";
                break;
            case CONNECT_FAILURE:
                rtn = "连接错误";
                break;
            case CONNECT_SUCCESS:
                rtn = "已连接";
                break;
            default:
                break;
        }
        return rtn;
    }

    public void setConnectState(ConnectState state) {this.connectState = state; notifyDeviceObservers(TYPE_MODIFY);}

    public DeviceMirror getDeviceMirror() {return deviceMirror;}

    public void setDeviceMirror(DeviceMirror deviceMirror) {this.deviceMirror = deviceMirror;}

    // 获取设备广播中包含的UUID
    public String getDeviceUuidInAd() {
        if(deviceMirror == null) return null;

        AdRecord record = deviceMirror.getBluetoothLeDevice()
                .getAdRecordStore().getRecord(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(record == null) return null;
        return Uuid.getUuidFromByteArray(record.getData()).toString();
    }

    // 登记观察者
    public void registerDeviceObserver(IConfiguredDeviceObersver obersver) {
        int index = obersvers.indexOf(obersver);
        if(index < 0) {
            obersvers.add(obersver);
        }
    }

    // 删除观察者
    public void removerDeviceObserver(IConfiguredDeviceObersver obersver) {
        int index = obersvers.indexOf(obersver);
        if(index >= 0) {
            obersvers.remove(index);
        }
    }

    // 通知观察者
    // @param type：状态改变的类型
    public void notifyDeviceObservers(final int type) {
        for(final IConfiguredDeviceObersver obersver : obersvers) {
            if(obersver != null) {
                obersver.updateDeviceInfo(ConfiguredDevice.this, type);
            }
        }
    }

    // 发起连接
    // @param: connectCallback 连接回调
    public void connect(IConnectCallback connectCallback) {
        setConnectState(ConnectState.CONNECT_PROCESS);
        MyApplication.getViseBle().connectByMac(macAddress, connectCallback);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfiguredDevice that = (ConfiguredDevice) o;

        return macAddress != null ? macAddress.equals(that.macAddress) : that.macAddress == null;
    }

    @Override
    public int hashCode() {
        return macAddress != null ? macAddress.hashCode() : 0;
    }
}

package com.cmtech.android.bledevice.ecgmonitor.device;

import android.content.Context;

import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.interfac.IEcgDevice;
import com.cmtech.android.bledevice.ecgmonitor.interfac.OnEcgMonitorListener;
import com.cmtech.android.bledevice.ecgmonitor.process.hr.HrStatisticsInfo;
import com.cmtech.android.bledevice.ecgmonitor.record.EcgRecord;

import org.litepal.LitePal;

public abstract class AbstractEcgDevice implements IEcgDevice {
    protected int sampleRate; // 采样率
    protected EcgLeadType leadType; // 导联类型
    protected int value1mV; // 定标之前1mV值
    protected OnEcgMonitorListener listener; // 心电监护仪监听器
    protected EcgRecord ecgRecord; // 心电记录，可记录心电信号数据、用户留言和心率信息
    protected final EcgMonitorConfiguration config; // 心电监护仪的配置信息

    private final IDevice deviceProxy;

    public AbstractEcgDevice(IDevice deviceProxy) {
        this.deviceProxy = deviceProxy;

        // 从数据库获取设备的配置信息
        EcgMonitorConfiguration config = LitePal.where("macAddress = ?", getAddress()).findFirst(EcgMonitorConfiguration.class);
        if(config == null) {
            config = new EcgMonitorConfiguration();
            config.setMacAddress(getAddress());
            config.save();
        }
        this.config = config;
    }

    @Override
    public final int getSampleRate() { return sampleRate; }
    @Override
    public final EcgLeadType getLeadType() {
        return leadType;
    }
    @Override
    public final int getValue1mV() { return value1mV; }
    @Override
    public final EcgMonitorConfiguration getConfig() {
        return config;
    }
    @Override
    public final void setEcgMonitorListener(OnEcgMonitorListener listener) {
        this.listener = listener;
    }
    @Override
    public final void removeEcgMonitorListener() {
        listener = null;
    }
    @Override
    public final void notifyHrAbnormal() {
        if(listener != null) {
            listener.onHrAbnormalNotified();
        }
    }
    @Override
    public final void onHrStatisticInfoUpdated(HrStatisticsInfo hrInfoObject) {
        if(listener != null) {
            listener.onHrStatisticsInfoUpdated(hrInfoObject);
        }
    }

    @Override
    public DeviceRegisterInfo getRegisterInfo() {
        return deviceProxy.getRegisterInfo();
    }

    @Override
    public void updateRegisterInfo(DeviceRegisterInfo registerInfo) {
        deviceProxy.updateRegisterInfo(registerInfo);
    }

    @Override
    public String getAddress() {
        return deviceProxy.getAddress();
    }

    @Override
    public String getName() {
        return deviceProxy.getName();
    }

    @Override
    public String getUuidString() {
        return deviceProxy.getUuidString();
    }

    @Override
    public String getImagePath() {
        return deviceProxy.getImagePath();
    }

    @Override
    public BleDeviceState getState() {
        return deviceProxy.getState();
    }

    @Override
    public boolean isScanning() {
        return deviceProxy.isScanning();
    }

    @Override
    public boolean isConnected() {
        return deviceProxy.isConnected();
    }

    @Override
    public boolean isDisconnected() {
        return deviceProxy.isDisconnected();
    }

    @Override
    public void setState(BleDeviceState state) {
        deviceProxy.setState(state);
    }

    @Override
    public void updateState() {
        deviceProxy.updateState();
    }

    @Override
    public int getBattery() {
        return deviceProxy.getBattery();
    }

    @Override
    public void setBattery(int battery) {
        deviceProxy.setBattery(battery);
    }

    @Override
    public void addListener(OnDeviceListener listener) {
        deviceProxy.addListener(listener);
    }

    @Override
    public void removeListener(OnDeviceListener listener) {
        deviceProxy.removeListener(listener);
    }

    @Override
    public void open(Context context) {
        deviceProxy.open(context);
    }

    @Override
    public void switchState() {
        deviceProxy.switchState();
    }

    @Override
    public void callDisconnect(boolean stopAutoScan) {
        deviceProxy.callDisconnect(stopAutoScan);
    }

    @Override
    public boolean isStopped() {
        return deviceProxy.isStopped();
    }

    @Override
    public void close() {
        deviceProxy.close();
    }

    @Override
    public void clear() {
        deviceProxy.clear();
    }

    @Override
    public void disconnect() {
        deviceProxy.disconnect();
    }
}

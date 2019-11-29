package com.cmtech.android.bledevice.ecgmonitor.device;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.interfac.IEcgDevice;
import com.cmtech.android.bledevice.ecgmonitor.process.hr.HrStatisticsInfo;
import com.cmtech.android.bledevice.ecgmonitor.record.EcgRecord;

import org.litepal.LitePal;

import static com.cmtech.android.bledevice.ecgmonitor.process.signal.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;
import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

public abstract class AbstractEcgDevice implements IEcgDevice {
    public static final int DEFAULT_VALUE_1MV = 164; // 缺省定标前1mV值
    public static final int DEFAULT_SAMPLE_RATE = 125; // 缺省ECG信号采样率,Hz
    public static final EcgLeadType DEFAULT_LEAD_TYPE = EcgLeadType.LEAD_I; // 缺省导联为LI

    private int sampleRate; // 采样率
    private EcgLeadType leadType; // 导联类型
    private int value1mV; // 定标之前1mV值

    protected OnEcgMonitorListener listener; // 心电监护仪监听器
    protected EcgRecord ecgRecord; // 心电记录，可记录心电信号数据、用户留言和心率信息
    protected final EcgMonitorConfiguration config; // 心电监护仪的配置信息
    protected boolean isRecord = false; // 是否在记录信号
    private boolean isSaveRecord = false; // 是否保存心电记录
    private final AbstractDevice deviceProxy;

    public AbstractEcgDevice(AbstractDevice deviceProxy) {
        this.deviceProxy = deviceProxy;

        // 从数据库获取设备的配置信息
        EcgMonitorConfiguration config = LitePal.where("macAddress = ?", getAddress()).findFirst(EcgMonitorConfiguration.class);
        if(config == null) {
            config = new EcgMonitorConfiguration();
            config.setMacAddress(getAddress());
            config.save();
        }
        this.config = config;

        sampleRate = DEFAULT_SAMPLE_RATE;
        leadType = DEFAULT_LEAD_TYPE;
        value1mV = DEFAULT_VALUE_1MV;
    }

    @Override
    public final int getSampleRate() { return sampleRate; }
    public final void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
    @Override
    public final EcgLeadType getLeadType() {
        return leadType;
    }
    public final void setLeadType(EcgLeadType leadType) {
        this.leadType = leadType;
    }
    @Override
    public final int getValue1mV() { return value1mV; }
    @Override
    public void setValue1mV(int value1mV) {
        this.value1mV = value1mV;
    }
    @Override
    public final EcgMonitorConfiguration getConfig() {
        return config;
    }
    @Override
    public void updateConfig(EcgMonitorConfiguration config) {
        this.config.copyFrom(config);
        this.config.save();
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
    public int getRecordSecond() {
        return (ecgRecord == null) ? 0 : ecgRecord.getRecordSecond();
    }
    @Override
    public long getRecordDataNum() { return (ecgRecord == null) ? 0 : ecgRecord.getDataNum(); }
    @Override
    public boolean isRecord() {
        return ((ecgRecord != null) && isRecord);
    }
    @Override
    public boolean isSaveRecord() {
        return isSaveRecord;
    }
    @Override
    public void setSaveRecord(boolean isSaveRecord) {
        this.isSaveRecord = isSaveRecord;
    }
    @Override
    public final void notifyHrAbnormal() {
        if(listener != null) {
            listener.onHrAbnormalNotified();
        }
    }
    @Override
    public void updateSignalValue(int ecgSignal) {
        if(listener != null) {
            listener.onEcgSignalUpdated(ecgSignal);
        }
    }
    protected void updateSignalShowSetup() {
        if(listener != null)
            listener.onShowSetupUpdated(sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION, DEFAULT_ZERO_LOCATION);
    }
    protected void updateSignalShowState(boolean isStart) {
        if(listener != null) {
            listener.onEcgSignalShowStateUpdated(isStart);
        }
    }
    protected void updateSampleRate() {
        if(listener != null)
            listener.onSampleRateUpdated(sampleRate);
    }
    protected void updateLeadType() {
        if(listener != null)
            listener.onLeadTypeUpdated(leadType);
    }
    protected void updateValue1mV() {
        if(listener != null)
            listener.onValue1mVUpdated(value1mV, STANDARD_VALUE_1MV_AFTER_CALIBRATION);
    }
    protected void updateRecordSecond(final int second) {
        if(listener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onEcgSignalRecordSecondUpdated(second);
                }
            });
        }
    }
    protected void updateRecordStatus(final boolean isRecord) {
        if(listener != null)
            listener.onRecordStateUpdated(isRecord);
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

    @Override
    public boolean isLocal() {
        return deviceProxy.isLocal();
    }

    @Override
    public int hashCode() {
        return getAddress().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof IDevice)
            return getAddress().equals(((IDevice) obj).getAddress());
        else
            return false;
    }
}
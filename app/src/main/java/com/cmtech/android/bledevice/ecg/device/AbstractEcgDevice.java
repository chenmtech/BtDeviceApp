package com.cmtech.android.bledevice.ecg.device;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.ecg.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecg.interfac.IEcgDevice;
import com.cmtech.android.bledevice.ecg.process.hr.HrStatisticsInfo;
import com.cmtech.android.bledevice.ecg.record.EcgRecord;
import com.cmtech.android.bledevice.ecg.record.ecgcomment.EcgNormalComment;

import org.litepal.LitePal;

import java.io.IOException;

import static com.cmtech.android.bledevice.ecg.process.signal.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;
import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

public abstract class AbstractEcgDevice extends AbstractDevice implements IEcgDevice {
    protected static final int DEFAULT_VALUE_1MV = 164; // 缺省定标前1mV值
    private static final int DEFAULT_SAMPLE_RATE = 125; // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEAD_TYPE = EcgLeadType.LEAD_I; // 缺省导联为LI
    protected final EcgConfiguration config; // 心电监护仪的配置信息
    protected OnEcgDeviceListener listener; // 心电监护仪监听器
    protected EcgRecord ecgRecord; // 心电记录，可记录心电信号数据、用户留言和心率信息
    protected boolean isRecord = false; // 是否在记录信号
    protected EcgNormalComment creatorComment; // 创建人留言；
    protected int[] wave1mV = new int[0]; // 1mV波形数据，数据长度与采样率有关，幅度变化恒定，在读取采样率之后初始化
    private int sampleRate; // 采样率
    private EcgLeadType leadType; // 导联类型
    private int value1mV; // 定标之前1mV值
    private boolean isSaveRecord = false; // 是否保存心电记录

    public AbstractEcgDevice(Context context, DeviceCommonInfo registerInfo) {
        super(context, registerInfo);

        // 从数据库获取设备的配置信息
        EcgConfiguration config = LitePal.where("macAddress = ?", getAddress()).findFirst(EcgConfiguration.class);
        if (config == null) {
            config = new EcgConfiguration();
            config.setMacAddress(getAddress());
            config.save();
        }
        this.config = config;

        sampleRate = DEFAULT_SAMPLE_RATE;
        leadType = DEFAULT_LEAD_TYPE;
        value1mV = DEFAULT_VALUE_1MV;
    }

    @Override
    public final int getSampleRate() {
        return sampleRate;
    }

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
    public final int getValue1mV() {
        return value1mV;
    }

    @Override
    public void setValue1mV(int value1mV) {
        this.value1mV = value1mV;
    }

    @Override
    public final EcgConfiguration getConfig() {
        return config;
    }

    @Override
    public void updateConfig(EcgConfiguration config) {
        this.config.copyFrom(config);
        this.config.save();
    }

    @Override
    public final void setEcgMonitorListener(OnEcgDeviceListener listener) {
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
    public long getRecordDataNum() {
        return (ecgRecord == null) ? 0 : ecgRecord.getDataNum();
    }

    @Override
    public boolean isRecord() {
        return ((ecgRecord != null) && isRecord);
    }

    @Override
    public synchronized void setRecord(boolean record) {
        if (ecgRecord != null && this.isRecord != record) {
            // 当前isRecord与要设置的isRecord不同，意味着要改变当前的isRecord状态
            try {
                if (record) {
                    ecgRecord.writeData(getWave1mV());
                    updateRecordSecond(ecgRecord.getRecordSecond());
                }
                isRecord = record;
                updateRecordStatus(isRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        if (listener != null) {
            listener.onAbnormalHrNotified();
        }
    }

    @Override
    public void updateSignalValue(int ecgSignal) {
        // 记录
        if (isRecord()) {
            try {
                ecgRecord.writeData(ecgSignal);
                updateRecordSecond(ecgRecord.getRecordSecond());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 显示
        if (listener != null) {
            listener.onEcgSignalUpdated(ecgSignal);
        }
    }

    @Override
    public void updateHrValue(final short hr) {
        // 记录
        if (ecgRecord != null) {
            ecgRecord.addHr(hr);
        }

        // 显示
        if (listener != null) {
            listener.onHrUpdated(hr);
        }
    }

    // 添加留言内容
    public synchronized void addCommentContent(String content) {
        if (creatorComment != null)
            creatorComment.appendContent(content);
    }

    protected void updateEcgViewSetup() {
        if (listener != null)
            listener.onEcgViewSetup(sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION, DEFAULT_ZERO_LOCATION);
    }

    protected void updateSignalShowState(boolean isStart) {
        if (listener != null) {
            listener.onEcgSignalShowStateUpdated(isStart);
        }
    }

    protected void updateSampleRate() {
        if (listener != null)
            listener.onSampleRateUpdated(sampleRate);
    }

    protected void updateLeadType() {
        if (listener != null)
            listener.onLeadTypeUpdated(leadType);
    }

    protected void updateValue1mV() {
        if (listener != null)
            listener.onValue1mVUpdated(value1mV, STANDARD_VALUE_1MV_AFTER_CALIBRATION);
    }

    protected void updateRecordSecond(final int second) {
        if (listener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onEcgSignalRecordSecondUpdated(second);
                }
            });
        }
    }

    protected void updateRecordStatus(final boolean isRecord) {
        if (listener != null)
            listener.onRecordStateUpdated(isRecord);
    }

    private int[] getWave1mV() {
        return wave1mV;
    }

    @Override
    public final void onHrStatisticInfoUpdated(HrStatisticsInfo hrInfoObject) {
        if (listener != null) {
            listener.onHrStatisticsInfoUpdated(hrInfoObject);
        }
    }

    @Override
    public int hashCode() {
        return getAddress().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof IDevice)
            return getAddress().equals(((IDevice) obj).getAddress());
        else
            return false;
    }

}

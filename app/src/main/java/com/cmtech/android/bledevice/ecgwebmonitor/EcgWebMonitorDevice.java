package com.cmtech.android.bledevice.ecgwebmonitor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.bledevice.ecgmonitor.device.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgMonitorState;
import com.cmtech.android.bledevice.ecgmonitor.process.hr.HrStatisticsInfo;
import com.vise.log.ViseLog;


/**
  *
  * ClassName:      EcgMonitorDevice
  * Description:    单导联心电监护仪设备
  * Author:         chenm
  * CreateDate:     2018-09-20 07:55
  * UpdateUser:     chenm
  * UpdateDate:     2019-07-03 07:55
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgWebMonitorDevice extends EcgMonitorDevice {
    private static final String TAG = "EcgWebMonitorDevice";

    // 心电网络监护仪监听器
    public interface OnEcgWebMonitorListener {
        void onStateUpdated(EcgMonitorState state); // 状态更新
        void onSampleRateUpdated(int sampleRate); // 采样率更新
        void onLeadTypeUpdated(EcgLeadType leadType); // 导联类型更新
        void onValue1mVUpdated(int value1mV, int value1mVAfterCalibration);  // 1mV值更新
        void onRecordStateUpdated(boolean isRecord); // 记录状态更新
        void onShowSetupUpdated(int sampleRate, int value1mV, double zeroLocation); // 信号显示设置更新
        void onEcgSignalUpdated(int ecgSignal); // 信号显示
        void onEcgSignalShowStarted(int sampleRate); // 信号显示启动
        void onEcgSignalShowStopped(); // 信号显示停止
        void onRecordSecondUpdated(int second); // 信号记录秒数更新
        void onHrUpdated(int hr); // 心率值更新，单位bpm
        void onHrStaticsInfoUpdated(HrStatisticsInfo hrStaticsInfoAnalyzer); // 心率统计信息更新
        void onHrAbnormalNotified(); // 心率值异常通知
        void onBatteryUpdated(int bat); // 电池电量更新
    }

    private OnEcgWebMonitorListener listener;

    // 构造器
    EcgWebMonitorDevice(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    protected boolean executeAfterConnectSuccess() {
        return super.executeAfterConnectSuccess();
    }

    @Override
    protected void executeAfterDisconnect() {
        super.executeAfterDisconnect();
    }

    @Override
    protected void executeAfterConnectFailure() {
        super.executeAfterConnectFailure();
    }

    @Override
    public void open(Context context) {
        ViseLog.e("EcgMonitorDevice.open()");

        super.open(context);
    }

    // 关闭设备
    @Override
    public void close() {
        super.close();
    }

    @Override
    protected void disconnect() {
        super.disconnect();
    }

    // 启动ECG信号采集
    public void startEcgSignalSampling() {
        /*IBleDataCallback receiveCallback = new IBleDataCallback() {
            @Override
            public void onSuccess(final byte[] data, BleGattElement element) {
                dataProcessor.processData(data, false);
            }

            @Override
            public void onFailure(BleException exception) {
                ViseLog.e(exception);
            }
        };

        // enable ECG data notification
        notify(ECGMONITOR_DATA_CCC, true, receiveCallback);

        write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_START_SIGNAL, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                setEcgMonitorState(EcgMonitorState.SAMPLEING);
                dataProcessor.start();

                ViseLog.e("启动ECG信号采样");
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });*/
    }

    // 停止数据采集
    public void stopDataSampling() {
        /*ViseLog.e("停止数据采样");

        notify(ECGMONITOR_DATA_CCC, false, null);
        write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_STOP, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                dataProcessor.stop();
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });*/
    }

    // 登记心电监护仪设备监听器
    public void setListener(OnEcgWebMonitorListener listener) {
        this.listener = listener;
    }

    // 删除心电监护仪设备监听器
    public void removeListener() {
        listener = null;
    }

    public void notifyHrAbnormal() {
        if(listener != null) {
            listener.onHrAbnormalNotified();
        }
    }

    public void updateRecordSecond(final int second) {
        if(listener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onRecordSecondUpdated(second);
                }
            });
        }
    }

    public void setValue1mV(final int value1mV) {
        /*ViseLog.e("定标前1mV值为: " + value1mV);

        this.value1mV = value1mV;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                updateValue1mV(value1mV);
            }
        });

        // 重置Ecg信号处理器
        dataProcessor.resetSignalProcessor();

        // 创建心电记录
        if(ecgRecord == null) {
            ecgRecord = EcgRecord.create(AccountManager.getInstance().getAccount(), sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION, getAddress(), leadType);
            if(ecgRecord != null) {
                ViseLog.e("ecgRecord: " + ecgRecord);
                try {
                    ecgRecord.openSigFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                creatorComment = EcgNormalComment.create();
                ecgRecord.addComment(creatorComment);
            }
        }

        if(webBroadcaster == null) {
            webBroadcaster = new EcgRecordWebBroadcaster(EcgMonitorUtil.deleteColon(getAddress()),
                    AccountManager.getInstance().getAccount().getPhone(),
                    sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION, leadType.getCode());
            webBroadcaster.start();
        }

        // 输出1mV定标信号
        for(int data : wave1mV) {
            updateSignalValue(data);
        }

        // 启动心电信号采样
        startEcgSignalSampling();*/
    }
}

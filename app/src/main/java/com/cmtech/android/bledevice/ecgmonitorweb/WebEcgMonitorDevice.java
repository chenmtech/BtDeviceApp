package com.cmtech.android.bledevice.ecgmonitorweb;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.ecgmonitor.device.AbstractEcgDevice;
import com.cmtech.android.bledevice.ecgmonitor.record.EcgRecord;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.WebDevice;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import static com.cmtech.android.bledevice.ecgmonitor.process.signal.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;


/**
  *
  * ClassName:      WebEcgMonitorDevice
  * Description:    网络心电监护仪设备
  * Author:         chenm
  * CreateDate:     2018-09-20 07:55
  * UpdateUser:     chenm
  * UpdateDate:     2019-07-03 07:55
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class WebEcgMonitorDevice extends AbstractEcgDevice {
    private static final String TAG = "WebEcgMonitorDevice";

    private static final int MSG_READ_DATA_PACKET = 0;

    private final LinkedBlockingQueue<Integer> dataCache = new LinkedBlockingQueue<>();	//要显示的信号数据缓存
    private int lastDataPackId = 0; // 最后收到的数据包ID
    private int sampleInterval = 0; // 采样间隔
    private Timer signalProcessTimer; // 信号处理定时器
    private int timerPeriod = 0; // 定时器周期

    // 单个信号数据处理任务
    private class SignalProcessTask extends TimerTask {
        @Override
        public void run() {
            try {
                updateSignalValue(dataCache.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 数据包处理Handler
    protected final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_READ_DATA_PACKET) {
                EcgHttpReceiver.readDataPackets(getAddress(), lastDataPackId, new EcgHttpReceiver.IEcgDataPacketCallback() {
                    @Override
                    public void onReceived(List<EcgHttpReceiver.EcgDataPacket> dataPacketList) {
                        if(dataPacketList != null && !dataPacketList.isEmpty()) {
                            for(EcgHttpReceiver.EcgDataPacket packet : dataPacketList) {
                                List<Integer> data = packet.getData();
                                for (int i = 0; i < data.size(); i++) {
                                    try {
                                        dataCache.put(data.get(i));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            lastDataPackId = dataPacketList.get(dataPacketList.size()-1).getId();

                            int size = dataCache.size();
                            if(size > 2*getSampleRate() && timerPeriod != sampleInterval-1) {
                                timerPeriod = sampleInterval-1;
                                signalProcessTimer.cancel();
                                signalProcessTimer = new Timer();
                                signalProcessTimer.schedule(new SignalProcessTask(), 0, timerPeriod);
                            } else if(size < getSampleRate() && timerPeriod != sampleInterval) {
                                timerPeriod = sampleInterval;
                                signalProcessTimer.cancel();
                                signalProcessTimer = new Timer();
                                signalProcessTimer.schedule(new SignalProcessTask(), 0, timerPeriod);
                            }
                            ViseLog.e("timerPeriod=" + timerPeriod + "sampleInterval=" + sampleInterval);
                        }
                        if (getState() == BleDeviceState.CONNECT)
                            handler.sendEmptyMessageDelayed(MSG_READ_DATA_PACKET, 1000);
                    }
                });
            }
        }
    };

    // 构造器
    private WebEcgMonitorDevice(WebDevice deviceProxy) {
        super(deviceProxy);
    }

    public static IDevice create(DeviceRegisterInfo registerInfo) {
        WebDevice webDevice = new WebDevice(registerInfo);
        final WebEcgMonitorDevice device = new WebEcgMonitorDevice(webDevice);
        webDevice.setCallback(new IConnectCallback() {
            @Override
            public boolean onConnectSuccess() {
                return device.executeAfterConnectSuccess();
            }

            @Override
            public void onConnectFailure() {
                device.executeAfterConnectFailure();
            }

            @Override
            public void onDisconnect() {
                device.executeAfterDisconnect();
            }
        });
        return device;
    }

    private boolean executeAfterConnectSuccess() {
        if(getValue1mV() != STANDARD_VALUE_1MV_AFTER_CALIBRATION) return false;

        updateSampleRate();
        updateValue1mV();
        updateLeadType();

        updateSignalShowSetup();
        updateSignalShowState(true);

        // 创建心电记录
        if(ecgRecord == null) {
            ecgRecord = EcgRecord.create(AccountManager.getInstance().getAccount(), getSampleRate(), STANDARD_VALUE_1MV_AFTER_CALIBRATION, getAddress(), getLeadType());
            if(ecgRecord != null) {
                ViseLog.e("ecgRecord: " + ecgRecord);
                try {
                    ecgRecord.openSigFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        lastDataPackId = 0;
        sampleInterval = 1000/getSampleRate();
        timerPeriod = sampleInterval;
        // 初始化并启动定时器
        if(signalProcessTimer != null) {
            signalProcessTimer.cancel();
        }
        signalProcessTimer = new Timer();
        signalProcessTimer.schedule(new SignalProcessTask(), 0, timerPeriod);

        handler.sendEmptyMessage(MSG_READ_DATA_PACKET);

        return true;
    }

    private void executeAfterDisconnect() {
        updateSignalShowState(false);
    }

    private void executeAfterConnectFailure() {
        updateSignalShowState(false);
    }

    @Override
    public void callDisconnect(boolean stopAutoScan) {
        if(signalProcessTimer != null) {
            signalProcessTimer.cancel();
            dataCache.clear();
        }
        handler.removeCallbacksAndMessages(null);
        super.callDisconnect(stopAutoScan);
    }

    @Override
    public void updateSignalValue(final int ecgSignal) {
        // 记录
        if(isRecord()) {
            try {
                ecgRecord.writeData(ecgSignal);
                updateRecordSecond(ecgRecord.getRecordSecond());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 显示
        super.updateSignalValue(ecgSignal);
    }

    @Override
    public void updateHrValue(final short hr) {
        // 记录
        if(ecgRecord != null) {
            ecgRecord.addHr(hr);
        }

        // 显示
        if(listener != null) {
            listener.onHrUpdated(hr);
        }
    }

    @Override
    public synchronized void setRecord(boolean record) {
        if(ecgRecord != null && this.isRecord != record) {
            // 当前isRecord与要设置的isRecord不同，意味着要改变当前的isRecord状态
            isRecord = record;
            updateRecordStatus(isRecord);
        }
    }
}

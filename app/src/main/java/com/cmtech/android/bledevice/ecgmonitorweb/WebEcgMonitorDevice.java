package com.cmtech.android.bledevice.ecgmonitorweb;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.ecgmonitor.device.AbstractEcgDevice;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgLeadType;
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
    private static final int DEFAULT_VALUE_1MV = 65535; // 缺省定标1mV值
    private static final int DEFAULT_SAMPLE_RATE = 125; // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEAD_TYPE = EcgLeadType.LEAD_I; // 缺省导联为L1

    private static final int MSG_READ_DATA_PACKET = 0;

    private Timer showTimer; // 定时器
    private final LinkedBlockingQueue<Integer> showCache = new LinkedBlockingQueue<>();	//要显示的信号数据缓存
    private int lastDataPackId = 0;

    private class ShowTask extends TimerTask {
        @Override
        public void run() {
            Integer i = showCache.poll();
            if(i != null)
                updateSignalValue(i);
        }
    }

    // 请求处理Handler
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
                                        showCache.put(data.get(i));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            lastDataPackId = dataPacketList.get(dataPacketList.size()-1).getId();
                            if (getState() == BleDeviceState.CONNECT)
                                handler.sendEmptyMessage(MSG_READ_DATA_PACKET);
                        } else {
                            handler.sendEmptyMessageDelayed(MSG_READ_DATA_PACKET, 50);
                        }
                    }
                });
            }
        }
    };

    // 构造器
    private WebEcgMonitorDevice(WebDevice deviceProxy) {
        super(deviceProxy);

        sampleRate = DEFAULT_SAMPLE_RATE;
        leadType = DEFAULT_LEAD_TYPE;
        value1mV = DEFAULT_VALUE_1MV;
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

    @Override
    public void setValue1mV(int value1mV) {
        this.value1mV = value1mV;
    }

    private boolean executeAfterConnectSuccess() {
        updateSampleRate(sampleRate);
        updateValue1mV(value1mV);
        updateLeadType(leadType);

        updateSignalShowSetup(sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION);
        if(listener != null) {
            listener.onEcgSignalShowStateUpdated(true);
        }

        // 初始化显示定时器
        if(showTimer != null) {
            showTimer.cancel();
        }
        showTimer = new Timer();
        int sampleInterval = 1000/getSampleRate();
        showTimer.scheduleAtFixedRate(new ShowTask(), sampleInterval, sampleInterval);

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
            }
        }

        lastDataPackId = 0;
        handler.sendEmptyMessage(MSG_READ_DATA_PACKET);

        return true;
    }

    private void executeAfterDisconnect() {
        if(listener != null) {
            listener.onEcgSignalShowStateUpdated(false);
        }
    }

    private void executeAfterConnectFailure() {
        if(listener != null) {
            listener.onEcgSignalShowStateUpdated(false);
        }
    }

    @Override
    public void callDisconnect(boolean stopAutoScan) {
        if(showTimer != null) {
            showTimer.cancel();
            showCache.clear();
        }
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
        if(listener != null) {
            listener.onEcgSignalUpdated(ecgSignal);
        }
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

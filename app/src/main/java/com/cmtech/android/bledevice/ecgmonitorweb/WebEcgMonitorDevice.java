package com.cmtech.android.bledevice.ecgmonitorweb;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

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
import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;


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

    private class ShowTask extends TimerTask {
        @Override
        public void run() {
            try {
                updateSignalValue(showCache.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 请求处理Handler
    protected final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_READ_DATA_PACKET) {
                EcgHttpBroadcastReceiver.readDataPackets("", "-1", new EcgHttpBroadcastReceiver.IEcgBroadcastDataPacketCallback() {
                    @Override
                    public void onReceived(List<EcgHttpBroadcastReceiver.EcgDataPacket> dataList) {
                        if(dataList != null) {
                            List<Integer> data = dataList.get(0).getData();
                            for (int i = 0; i < data.size(); i++) {
                                try {
                                    showCache.put(data.get(i));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                //updateSignalValue(data.get(i));
                                //Log.e(TAG, "data = " + data.get(i));
                            }
                            if (getState() == BleDeviceState.CONNECT)
                                handler.sendEmptyMessage(MSG_READ_DATA_PACKET);
                        }
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
        return new WebEcgMonitorDevice(webDevice);
    }

    @Override
    public void switchState() {
        super.switchState();
    }

    @Override
    public boolean isStopped() {
        return super.isStopped();
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public void setValue1mV(int value1mV) {
        this.value1mV = value1mV;
    }

    public int getRecordSecond() {
        return (ecgRecord == null) ? 0 : ecgRecord.getRecordSecond();
    }
    public long getRecordDataNum() { return (ecgRecord == null) ? 0 : ecgRecord.getDataNum(); }

    @Override
    public boolean executeAfterConnectSuccess() {
        EcgHttpBroadcastReceiver.retrieveBroadcastInfo("", new EcgHttpBroadcastReceiver.IEcgBroadcastInfoCallback() {
            @Override
            public void onReceived(String broadcastId, String deviceId, String creatorId, int sampleRate, int caliValue, int leadTypeCode) {
                broadcastId = "test";
                sampleRate = 250;
                caliValue = 65535;
                leadTypeCode = 1;
                if(!TextUtils.isEmpty(broadcastId)) {
                    WebEcgMonitorDevice.this.sampleRate = sampleRate;
                    updateSampleRate(sampleRate);
                    // 初始化信号显示设置
                    initializeSignalShowSetup(sampleRate);
                    if(listener != null) {
                        listener.onEcgSignalShowStateUpdated(true);
                    }

                    if(showTimer != null) {
                        showTimer.cancel();
                    }
                    // 初始化定时器
                    showTimer = new Timer();
                    int sampleInterval = 1000/getSampleRate();
                    showTimer.scheduleAtFixedRate(new ShowTask(), sampleInterval, sampleInterval);

                    WebEcgMonitorDevice.this.value1mV = caliValue;
                    updateValue1mV(caliValue);
                    WebEcgMonitorDevice.this.leadType = EcgLeadType.getFromCode(leadTypeCode);
                    updateLeadType(leadType);

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

                    handler.sendEmptyMessage(MSG_READ_DATA_PACKET);
                }
            }
        });

        return true;
    }

    // 初始化信号显示设置
    private void initializeSignalShowSetup(int sampleRate) {
        // 更新信号显示设置
        updateSignalShowSetup(sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION);
    }

    private void updateSignalShowSetup(int sampleRate, int value1mV) {
        if(listener != null)
            listener.onShowSetupUpdated(sampleRate, value1mV, DEFAULT_ZERO_LOCATION);
    }

    @Override
    public void executeAfterDisconnect() {

    }

    @Override
    public void executeAfterConnectFailure() {

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
    public void callDisconnect(boolean stopAutoScan) {
        if(showTimer != null) {
            showTimer.cancel();
            showCache.clear();
        }
        super.callDisconnect(stopAutoScan);
    }

    private void updateRecordSecond(final int second) {
        if(listener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onEcgSignalRecordSecondUpdated(second);
                }
            });
        }
    }

    private void updateSampleRate(final int sampleRate) {
        if(listener != null)
            listener.onSampleRateUpdated(sampleRate);
    }

    private void updateLeadType(final EcgLeadType leadType) {
        if(listener != null)
            listener.onLeadTypeUpdated(leadType);
    }

    private void updateValue1mV(final int value1mV) {
        if(listener != null)
            listener.onValue1mVUpdated(value1mV, STANDARD_VALUE_1MV_AFTER_CALIBRATION);
    }

    @Override
    public void updateSignalValue(final int ecgSignal) {
        // 记录
        if(true) {
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
}

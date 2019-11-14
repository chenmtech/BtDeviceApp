package com.cmtech.android.bledevice.ecgmonitorweb;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.interfac.OnEcgMonitorListener;
import com.cmtech.android.bledevice.ecgmonitor.record.EcgRecord;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.WebDevice;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.fragment.EcgMonitorFragment.ZERO_LOCATION_IN_ECG_VIEW;
import static com.cmtech.android.bledevice.ecgmonitor.process.signal.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;


/**
  *
  * ClassName:      WebEcgMonitorDevice
  * Description:    单导联心电监护仪网络设备
  * Author:         chenm
  * CreateDate:     2018-09-20 07:55
  * UpdateUser:     chenm
  * UpdateDate:     2019-07-03 07:55
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class WebEcgMonitorDevice extends WebDevice {
    private static final String TAG = "WebEcgMonitorDevice";
    private static final int DEFAULT_VALUE_1MV = 164; // 缺省定标前1mV值
    private static final int DEFAULT_SAMPLE_RATE = 125; // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEAD_TYPE = EcgLeadType.LEAD_I; // 缺省导联为L1

    private static final int MSG_READ_DATA_PACKET = 0;

    private OnEcgMonitorListener listener;
    private int sampleRate = DEFAULT_SAMPLE_RATE; // 采样率
    private EcgLeadType leadType = DEFAULT_LEAD_TYPE; // 导联类型
    private int value1mV = DEFAULT_VALUE_1MV; // 定标之前1mV值

    private EcgRecord ecgRecord; // 心电记录，可记录心电信号数据、用户留言和心率信息

    // 请求处理Handler
    protected final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_READ_DATA_PACKET) {
                EcgBroadcastReceiver.readDataPackets("", "-1", new EcgBroadcastReceiver.IEcgBroadcastDataPacketCallback() {
                    @Override
                    public void onReceived(List<EcgBroadcastReceiver.EcgDataPacket> dataList) {
                        List<Integer> data = dataList.get(0).getData();
                        for(int i = 0; i < data.size(); i++) {
                            updateSignalValue(data.get(i));
                            Log.e(TAG, "data = " + data.get(i));
                        }
                        if(state == BleDeviceState.CONNECT)
                            handler.sendEmptyMessage(MSG_READ_DATA_PACKET);
                    }
                });
            }
        }
    };

    // 构造器
    WebEcgMonitorDevice(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    public int getSampleRate() { return sampleRate; }
    public EcgLeadType getLeadType() {
        return leadType;
    }
    public int getValue1mV() { return value1mV; }
    public int getRecordSecond() {
        return (ecgRecord == null) ? 0 : ecgRecord.getRecordSecond();
    }
    public long getRecordDataNum() { return (ecgRecord == null) ? 0 : ecgRecord.getDataNum(); }

    @Override
    protected boolean executeAfterConnectSuccess() {
        EcgBroadcastReceiver.retrieveBroadcastInfo("", new EcgBroadcastReceiver.IEcgBroadcastInfoCallback() {
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
                        listener.onEcgSignalShowStarted(sampleRate);
                    }

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
            listener.onShowSetupUpdated(sampleRate, value1mV, ZERO_LOCATION_IN_ECG_VIEW);
    }

    @Override
    protected void executeAfterDisconnect() {

    }

    @Override
    protected void executeAfterConnectFailure() {

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

    // 登记心电监护仪设备监听器
    public void setListener(OnEcgMonitorListener listener) {
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
}

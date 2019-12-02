package com.cmtech.android.bledevice.ecg.webecg;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.bledevice.ecg.device.AbstractEcgDevice;
import com.cmtech.android.bledevice.ecg.device.EcgConfiguration;
import com.cmtech.android.bledevice.ecg.process.signal.EcgSignalProcessor;
import com.cmtech.android.bledevice.ecg.record.EcgRecord;
import com.cmtech.android.bledevice.ecg.record.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.WebDeviceConnector;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import static com.cmtech.android.bledevice.ecg.EcgConstant.DIR_ECG_SIGNAL;
import static com.cmtech.android.bledevice.ecg.process.signal.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;
import static com.cmtech.android.bledevice.ecg.view.ScanEcgView.PIXEL_PER_GRID;
import static com.cmtech.android.bledevice.ecg.view.ScanEcgView.SECOND_PER_GRID;


/**
  *
  * ClassName:      WebEcgDevice
  * Description:    网络心电监护仪设备
  * Author:         chenm
  * CreateDate:     2018-09-20 07:55
  * UpdateUser:     chenm
  * UpdateDate:     2019-07-03 07:55
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class WebEcgDevice extends AbstractEcgDevice {
    private static final String TAG = "WebEcgDevice";

    private static final int MSG_READ_DATA_PACKET = 0;

    private final LinkedBlockingQueue<Integer> dataCache = new LinkedBlockingQueue<>();	//要显示的信号数据缓存
    private int lastDataPackId = 0; // 最后收到的数据包ID
    private int sampleInterval = 0; // 采样间隔
    private Timer signalProcessTimer; // 信号处理定时器
    private int timerPeriod = 0; // 定时器周期
    private final EcgSignalProcessor signalProcessor; // 心电信号处理器

    // 单个信号数据处理任务
    private class SignalProcessTask extends TimerTask {
        @Override
        public void run() {
            try {
                int ecgSignal = dataCache.take();
                //updateSignalValue(ecgSignal);
                signalProcessor.process(ecgSignal);
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
                EcgHttpReceiver.readDataPackets(AccountManager.getInstance().getAccount().getHuaweiId(), getAddress(), lastDataPackId, new EcgHttpReceiver.IEcgDataPacketCallback() {
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
    public WebEcgDevice(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
        this.connector = new WebDeviceConnector(this);
        signalProcessor = new EcgSignalProcessor(this, false);
    }

    @Override
    public boolean onConnectSuccess() {
        if(getValue1mV() != STANDARD_VALUE_1MV_AFTER_CALIBRATION) return false;

        updateSampleRate();
        updateValue1mV();
        updateLeadType();

        updateEcgViewSetup();
        updateSignalShowState(true);

        signalProcessor.reset();

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
                creatorComment = EcgNormalComment.create();
                ecgRecord.addComment(creatorComment);
            }
        }

        // 生成1mV波形数据
        int pixelPerData = Math.round(PIXEL_PER_GRID / (SECOND_PER_GRID * getSampleRate()));
        int N = 15 * PIXEL_PER_GRID/pixelPerData; // 15个栅格所需数据个数
        wave1mV = new int[N];
        for(int i = 0; i < N; i++) {
            if(i > N/3 && i < N*2/3) {
                wave1mV[i] = STANDARD_VALUE_1MV_AFTER_CALIBRATION;
            } else {
                wave1mV[i] = 0;
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

    @Override
    public void onDisconnect() {
        updateSignalShowState(false);
    }

    @Override
    public void onConnectFailure() {
        updateSignalShowState(false);
    }

    @Override
    public synchronized void updateConfig(EcgConfiguration config) {
        super.updateConfig(config);
        signalProcessor.resetHrAbnormalProcessor();
    }
    @Override
    public void forceDisconnect(boolean forever) {
        if(signalProcessTimer != null) {
            signalProcessTimer.cancel();
            dataCache.clear();
        }
        handler.removeCallbacksAndMessages(null);
        super.forceDisconnect(forever);
    }

    // 添加留言内容
    public synchronized void addCommentContent(String content) {
        if(creatorComment != null)
            creatorComment.appendContent(content);
    }

    // 关闭设备
    @Override
    public void close() {
        if(!isDisconnectedForever()) {
            ViseLog.e("The device can't be closed currently.");
            return;
        }

        ViseLog.e("EcgDevice.close()");

        // 停止信号记录
        setRecord(false);
        // 关闭记录
        if(ecgRecord != null) {
            try {
                ecgRecord.closeSigFile();
                if(isSaveRecord()) {
                    saveEcgRecord();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ecgRecord = null;
                ViseLog.e("关闭Ecg记录。");
            }
        }

        // 重置数据处理器
        if(signalProcessor != null)
            signalProcessor.reset();

        super.close();
    }

    private void saveEcgRecord() {
        try {
            ecgRecord.moveSigFileTo(DIR_ECG_SIGNAL);
            if(!ecgRecord.save()) {
                ViseLog.e("Ecg record save fail.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.cmtech.android.bledevice.ecg.device;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.OtherException;
import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledevice.ecg.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecg.enumeration.EcgMonitorState;
import com.cmtech.android.bledevice.ecg.process.EcgDataProcessor;
import com.cmtech.android.bledevice.ecg.record.EcgRecord;
import com.cmtech.android.bledevice.ecg.record.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledevice.ecg.util.EcgMonitorUtil;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.cmtech.android.ble.core.DeviceConnectState.CONNECT;
import static com.cmtech.android.bledevice.ecg.EcgConstant.DIR_ECG_SIGNAL;
import static com.cmtech.android.bledevice.ecg.process.signal.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;
import static com.cmtech.android.bledevice.view.ScanEcgView.PIXEL_PER_GRID;
import static com.cmtech.android.bledevice.view.ScanEcgView.SECOND_PER_GRID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.MY_BASE_UUID;


/**
 * ClassName:      EcgDevice
 * Description:    单导联心电监护仪设备
 * Author:         chenm
 * CreateDate:     2018-09-20 07:55
 * UpdateUser:     chenm
 * UpdateDate:     2019-07-03 07:55
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class EcgDevice extends AbstractEcgDevice {
    private static final String TAG = "EcgDevice";
    private static final int DEFAULT_READ_BATTERY_PERIOD = 10; // 缺省读电池电量的周期，分钟

    // 心电监护仪Service相关UUID常量
    private static final String ecgMonitorServiceUuid = "aa40";           // 心电监护仪服务UUID:aa40
    private static final String ecgMonitorDataUuid = "aa41";           // ECG数据特征UUID:aa41
    private static final String ecgMonitorCtrlUuid = "aa42";           // 测量控制UUID:aa42
    private static final String ecgMonitorSampleRateUuid = "aa44";           // 采样率UUID:aa44
    private static final String ecgMonitorLeadTypeUuid = "aa45";           // 导联类型UUID:aa45

    // 电池电量Service相关UUID常量
    private static final String batteryServiceUuid = "aa90";           // 电池电量服务UUID:aa90
    private static final String batteryDataUuid = "aa91";           // 电池电量数据特征UUID:aa91

    private static final UUID ecgMonitorServiceUUID = UuidUtil.stringToUUID(ecgMonitorServiceUuid, MY_BASE_UUID);
    private static final UUID ecgMonitorDataUUID = UuidUtil.stringToUUID(ecgMonitorDataUuid, MY_BASE_UUID);
    private static final UUID ecgMonitorCtrlUUID = UuidUtil.stringToUUID(ecgMonitorCtrlUuid, MY_BASE_UUID);
    private static final UUID ecgMonitorSampleRateUUID = UuidUtil.stringToUUID(ecgMonitorSampleRateUuid, MY_BASE_UUID);
    private static final UUID ecgMonitorLeadTypeUUID = UuidUtil.stringToUUID(ecgMonitorLeadTypeUuid, MY_BASE_UUID);
    private static final UUID batteryServiceUUID = UuidUtil.stringToUUID(batteryServiceUuid, MY_BASE_UUID);
    private static final UUID batteryDataUUID = UuidUtil.stringToUUID(batteryDataUuid, MY_BASE_UUID);

    // Gatt Element常量
    private static final BleGattElement ECGMONITOR_DATA =
            new BleGattElement(ecgMonitorServiceUUID, ecgMonitorDataUUID, null, "心电数据");
    private static final BleGattElement ECGMONITOR_DATA_CCC =
            new BleGattElement(ecgMonitorServiceUUID, ecgMonitorDataUUID, CCC_UUID, "心电数据CCC");
    private static final BleGattElement ECGMONITOR_CTRL =
            new BleGattElement(ecgMonitorServiceUUID, ecgMonitorCtrlUUID, null, "心电Ctrl");
    private static final BleGattElement ECGMONITOR_SAMPLE_RATE =
            new BleGattElement(ecgMonitorServiceUUID, ecgMonitorSampleRateUUID, null, "采样率");
    private static final BleGattElement ECGMONITOR_LEAD_TYPE =
            new BleGattElement(ecgMonitorServiceUUID, ecgMonitorLeadTypeUUID, null, "导联类型");
    private static final BleGattElement BATTERY_DATA =
            new BleGattElement(batteryServiceUUID, batteryDataUUID, null, "电池电量数据");

    // ECGMONITOR_CTRL Element的控制常量
    private static final byte ECGMONITOR_CTRL_STOP = (byte) 0x00; // 停止采集
    private static final byte ECGMONITOR_CTRL_START_SIGNAL = (byte) 0x01; // 启动采集Ecg信号
    private static final byte ECGMONITOR_CTRL_START_1MV = (byte) 0x02; // 启动采集1mV值
    private final EcgDataProcessor dataProcessor; // 心电数据处理器, 在其内部的单线程池中执行数据处理
    private boolean containBatteryService = false; // 是否包含电池电量测量服务
    private volatile EcgMonitorState state = EcgMonitorState.INIT; // 设备状态
    private ScheduledExecutorService batteryService; // 电池电量测量Service
    private boolean isBroadcast = false; // 是否在广播信号
    private EcgHttpBroadcast broadcast; // 网络广播


    // 构造器
    public EcgDevice(Context context, DeviceCommonInfo registerInfo) {
        super(context, registerInfo);
        dataProcessor = new EcgDataProcessor(this);
    }

    public boolean isBroadcast() {
        return broadcast != null && isBroadcast;
    }

    public synchronized void setBroadcast(boolean isBroadcast) {
        if (broadcast != null && this.isBroadcast != isBroadcast) {
            this.isBroadcast = isBroadcast;
            updateBroadcastStatus(this.isBroadcast);
        }
    }

    public void addBroadcastReceiver(EcgHttpBroadcast.Receiver receiver) {
        if (broadcast != null) {
            broadcast.checkReceiver(receiver);
        }
    }

    public void deleteBroadcastReceiver(EcgHttpBroadcast.Receiver receiver) {
        if (broadcast != null) {
            broadcast.uncheckReceiver(receiver);
        }
    }

    public void updateBroadcastReceiver() {
        if (broadcast != null) {
            broadcast.updateReceivers();
        }
    }

    public EcgMonitorState getEcgMonitorState() {
        return state;
    }

    private void setEcgMonitorState(EcgMonitorState state) {
        if (this.state != state) {
            this.state = state;
            updateEcgMonitorState();
        }
    }

    @Override
    public synchronized void updateConfig(EcgConfiguration config) {
        super.updateConfig(config);
        dataProcessor.resetHrAbnormalProcessor();
    }

    @Override
    public boolean onConnectSuccess() {
        BleGattElement[] elements = new BleGattElement[]{ECGMONITOR_DATA, ECGMONITOR_DATA_CCC, ECGMONITOR_CTRL, ECGMONITOR_SAMPLE_RATE, ECGMONITOR_LEAD_TYPE};

        if (!((BleConnector) connector).containGattElements(elements)) {
            ViseLog.e("EcgMonitor Elements wrong.");
            return false;
        }

        updateSampleRate();
        updateLeadType();
        updateValue1mV();

        // 启动电池电量测量
        containBatteryService = ((BleConnector) connector).containGattElement(BATTERY_DATA);
        if (containBatteryService) {
            startBatteryMeasure();
        }

        // 读采样率
        readSampleRate();
        // 读导联类型
        readLeadType();
        // 停止采样
        //stopSampling();
        // 启动1mV值采样
        //startValue1mVSampling();
        final IBleDataCallback receiveCallback = new IBleDataCallback() {
            @Override
            public void onSuccess(final byte[] data, BleGattElement element) {
                dataProcessor.processData(data, false);
            }

            @Override
            public void onFailure(BleException exception) {
                ViseLog.e(exception);
            }
        };
        ((BleConnector) connector).notify(ECGMONITOR_DATA_CCC, true, receiveCallback);

        ((BleConnector)connector).runInstantly(new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                setValue1mV(DEFAULT_VALUE_1MV);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });

        return true;
    }

    @Override
    public void onDisconnect() {
        dataProcessor.stop();

        updateSignalShowState(false);

        if (containBatteryService) {
            stopBatteryMeasure();
        }
    }

    @Override
    public void onConnectFailure() {
        dataProcessor.stop();

        updateSignalShowState(false);

        if (containBatteryService) {
            stopBatteryMeasure();
        }
    }

    // 关闭设备
    @Override
    public void close() {
        super.close();

        ViseLog.e("EcgDevice.close()");

        // 停止信号记录
        setRecord(false);
        // 关闭记录
        if (ecgRecord != null) {
            try {
                ecgRecord.closeSigFile();
                if (isSaveRecord()) {
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
        if (dataProcessor != null)
            dataProcessor.reset();

        // 停止广播
        setBroadcast(false);
        if (broadcast != null) {
            broadcast.stop();
            broadcast = null;
        }
    }

    private void saveEcgRecord() {
        try {
            ecgRecord.moveSigFileTo(DIR_ECG_SIGNAL);
            if (!ecgRecord.save()) {
                ViseLog.e("Ecg record save fail.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect(final boolean forever) {
        ViseLog.e("EcgDevice.disconnect()");

        if (containBatteryService) {
            stopBatteryMeasure();
            containBatteryService = false;
        }
        if (super.getConnectState() == CONNECT && ((BleConnector) connector).isGattExecutorAlive()) {
            ((BleConnector) connector).notify(ECGMONITOR_DATA_CCC, false, null);
            stopSampling();
        }
        ((BleConnector)connector).runInstantly(new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                EcgDevice.super.disconnect(forever);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    // 读采样率
    private void readSampleRate() {
        ((BleConnector) connector).read(ECGMONITOR_SAMPLE_RATE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                setSampleRate((data[0] & 0xff) | ((data[1] << 8) & 0xff00));
                updateSampleRate();
                dataProcessor.resetValue1mVDetector();
                updateEcgViewSetup();
                updateSignalShowState(true);
                // 生成1mV波形数据
                int pixelPerData = Math.round(PIXEL_PER_GRID / (SECOND_PER_GRID * getSampleRate()));
                int N = 15 * PIXEL_PER_GRID / pixelPerData; // 15个栅格所需数据个数
                wave1mV = new int[N];
                for (int i = 0; i < N; i++) {
                    if (i > N / 3 && i < N * 2 / 3) {
                        wave1mV[i] = STANDARD_VALUE_1MV_AFTER_CALIBRATION;
                    } else {
                        wave1mV[i] = 0;
                    }
                }
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    // 读导联类型
    private void readLeadType() {
        ((BleConnector) connector).read(ECGMONITOR_LEAD_TYPE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                setLeadType(EcgLeadType.getFromCode(data[0]));
                updateLeadType();
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    // 启动ECG信号采集
    private void startEcgSignalSampling() {
        /*final IBleDataCallback receiveCallback = new IBleDataCallback() {
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
        ((BleConnector) connector).notify(ECGMONITOR_DATA_CCC, true, receiveCallback);*/

        ((BleConnector) connector).write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_START_SIGNAL, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                setEcgMonitorState(EcgMonitorState.SAMPLING);
                dataProcessor.start();

                ViseLog.e("Ecg signal sampling started.");
            }

            @Override
            public void onFailure(BleException exception) {
                handleException(new OtherException("start ecg sampling failure---" + exception.getDescription()));
            }
        });
    }

    // 启动1mV信号采样
    private void startValue1mVSampling() {
        IBleDataCallback receiveCallback = new IBleDataCallback() {
            @Override
            public void onSuccess(final byte[] data, BleGattElement element) {
                dataProcessor.processData(data, true);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };

        ((BleConnector) connector).notify(ECGMONITOR_DATA_CCC, true, receiveCallback);

        // start 1mv sampling
        ((BleConnector) connector).write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_START_1MV, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                setEcgMonitorState(EcgMonitorState.CALIBRATING);
                dataProcessor.start();

                ViseLog.e("1mV calibration value sampling started.");
            }

            @Override
            public void onFailure(BleException exception) {
                handleException(new OtherException("start 1mV sampling failure---" + exception.getDescription()));
            }
        });
    }

    // 停止数据采集
    private void stopSampling() {
        //((BleConnector) connector).notify(ECGMONITOR_DATA_CCC, false, null);

        ((BleConnector) connector).write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_STOP, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                ViseLog.e("Sampling stopped.");
                dataProcessor.stop();
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    // 开始电池电量测量
    private void startBatteryMeasure() {
        if (ExecutorUtil.isDead(batteryService)) {
            ViseLog.e("启动电池电量测量服务");

            batteryService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_Bat_Measure");
                }
            });
            batteryService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    ((BleConnector) connector).read(BATTERY_DATA, new IBleDataCallback() {
                        @Override
                        public void onSuccess(byte[] data, BleGattElement element) {
                            setBatteryLevel(data[0]);
                        }

                        @Override
                        public void onFailure(BleException exception) {

                        }
                    });
                }
            }, 0, DEFAULT_READ_BATTERY_PERIOD, TimeUnit.MINUTES);
        }
    }

    // 停止电池电量测量
    private void stopBatteryMeasure() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(batteryService);

        ViseLog.e("停止电池电量测量服务");
    }

    @Override
    public void updateSignalValue(final int ecgSignal) {
        super.updateSignalValue(ecgSignal);

        // 广播
        if (isBroadcast()) {
            broadcast.sendEcgSignal(ecgSignal);
        }
    }

    @Override
    public void updateHrValue(final short hr) {
        super.updateHrValue(hr);

        // 广播
        if (isBroadcast()) {
            broadcast.sendHrValue(hr);
        }
    }

    @Override
    public void setValue1mV(final int value1mV) {
        ViseLog.e("Calculated 1mV value before calibration: " + value1mV);

        super.setValue1mV(value1mV);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                updateValue1mV();
            }
        });

        // 重置Ecg信号处理器
        dataProcessor.resetSignalProcessor();

        // 创建心电记录
        if (ecgRecord == null) {
            ecgRecord = EcgRecord.create(MyApplication.getAccount(), getSampleRate(), STANDARD_VALUE_1MV_AFTER_CALIBRATION, getAddress(), getLeadType());
            if (ecgRecord != null) {
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

        if (broadcast == null) {
            broadcast = new EcgHttpBroadcast(MyApplication.getAccount().getPlatId(),
                    EcgMonitorUtil.deleteColon(getAddress()),
                    getSampleRate(), STANDARD_VALUE_1MV_AFTER_CALIBRATION, getLeadType().getCode());
            broadcast.setListener(listener);
            broadcast.start();
        }

        // 输出1mV定标信号
        ViseLog.e("wave1mV: " + Arrays.toString(wave1mV));
        for (int data : wave1mV) {
            updateSignalValue(data);
        }

        // 启动心电信号采样
        startEcgSignalSampling();
    }

    private void updateEcgMonitorState() {
        if (listener != null)
            listener.onStateUpdated(state);
    }

    private void updateBroadcastStatus(final boolean isBroadcast) {
        if (listener != null)
            listener.onBroadcastStateUpdated(isBroadcast);
    }
}

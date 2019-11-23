package com.cmtech.android.bledevice.ecgmonitor.device;

import android.os.Handler;
import android.os.Looper;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgMonitorState;
import com.cmtech.android.bledevice.ecgmonitor.process.EcgDataProcessor;
import com.cmtech.android.bledevice.ecgmonitor.record.EcgRecord;
import com.cmtech.android.bledevice.ecgmonitor.record.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.util.EcgMonitorUtil;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.cmtech.android.ble.BleConfig.CCC_UUID;
import static com.cmtech.android.ble.core.BleDeviceState.CONNECT;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.DIR_ECG_SIGNAL;
import static com.cmtech.android.bledevice.ecgmonitor.process.signal.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;
import static com.cmtech.android.bledevice.ecgmonitor.view.ScanEcgView.PIXEL_PER_GRID;
import static com.cmtech.android.bledevice.ecgmonitor.view.ScanEcgView.SECOND_PER_GRID;
import static com.cmtech.android.bledeviceapp.AppConstant.MY_BASE_UUID;


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

public class EcgMonitorDevice extends AbstractEcgDevice {
    private static final String TAG = "EcgMonitorDevice";
    private static final int DEFAULT_VALUE_1MV = 164; // 缺省定标前1mV值
    private static final int DEFAULT_SAMPLE_RATE = 125; // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEAD_TYPE = EcgLeadType.LEAD_I; // 缺省导联为LI
    private static final int DEFAULT_READ_BATTERY_PERIOD = 10; // 缺省读电池电量的周期，分钟

    // 心电监护仪Service相关UUID常量
    private static final String ecgMonitorServiceUuid       = "aa40";           // 心电监护仪服务UUID:aa40
    private static final String ecgMonitorDataUuid          = "aa41";           // ECG数据特征UUID:aa41
    private static final String ecgMonitorCtrlUuid          = "aa42";           // 测量控制UUID:aa42
    private static final String ecgMonitorSampleRateUuid    = "aa44";           // 采样率UUID:aa44
    private static final String ecgMonitorLeadTypeUuid      = "aa45";           // 导联类型UUID:aa45

    // 电池电量Service相关UUID常量
    private static final String batteryServiceUuid       = "aa90";           // 电池电量服务UUID:aa90
    private static final String batteryDataUuid          = "aa91";           // 电池电量数据特征UUID:aa91

    // Gatt Element常量
    private static final BleGattElement ECGMONITOR_DATA =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, null, MY_BASE_UUID, "心电数据");
    private static final BleGattElement ECGMONITOR_DATA_CCC =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, CCC_UUID, MY_BASE_UUID, "心电数据CCC");
    private static final BleGattElement ECGMONITOR_CTRL =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorCtrlUuid, null, MY_BASE_UUID, "心电Ctrl");
    private static final BleGattElement ECGMONITOR_SAMPLE_RATE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorSampleRateUuid, null, MY_BASE_UUID, "采样率");
    private static final BleGattElement ECGMONITOR_LEAD_TYPE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorLeadTypeUuid, null, MY_BASE_UUID, "导联类型");
    private static final BleGattElement BATTERY_DATA =
            new BleGattElement(batteryServiceUuid, batteryDataUuid, null, MY_BASE_UUID, "电池电量数据");

    // ECGMONITOR_CTRL Element的控制常量
    private static final byte ECGMONITOR_CTRL_STOP = (byte) 0x00; // 停止采集
    private static final byte ECGMONITOR_CTRL_START_SIGNAL = (byte) 0x01; // 启动采集Ecg信号
    private static final byte ECGMONITOR_CTRL_START_1MV = (byte) 0x02; // 启动采集1mV值

    private int[] wave1mV; // 1mV波形数据，数据长度与采样率有关，幅度变化恒定，在读取采样率之后初始化
    private boolean containBatteryService = false; // 是否包含电池电量测量服务
    private volatile EcgMonitorState state = EcgMonitorState.INIT; // 设备状态
    private final EcgDataProcessor dataProcessor; // 心电数据处理器, 在其内部的单线程池中执行数据处理
    private ScheduledExecutorService batteryService; // 电池电量测量Service
    private EcgNormalComment creatorComment; // 创建人留言；
    private boolean isBroadcast = false; // 是否在广播信号
    private EcgHttpBroadcast broadcaster; // 网络广播器

    private BleDevice deviceProxy;

    // 构造器
    private EcgMonitorDevice(BleDevice deviceProxy) {
        super(deviceProxy);

        this.deviceProxy = deviceProxy;

        sampleRate = DEFAULT_SAMPLE_RATE;
        leadType = DEFAULT_LEAD_TYPE;
        value1mV = DEFAULT_VALUE_1MV;

        dataProcessor = new EcgDataProcessor(this);
    }

    public static IDevice create(DeviceRegisterInfo registerInfo) {
        BleDevice bleDevice = new BleDevice(registerInfo);
        final EcgMonitorDevice device = new EcgMonitorDevice(bleDevice);
        bleDevice.setCallback(new IConnectCallback() {
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
    public synchronized void setRecord(boolean record) {
        if(ecgRecord != null && this.isRecord != record) {
            // 当前isRecord与要设置的isRecord不同，意味着要改变当前的isRecord状态
            try {
                if(record) {
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
    public boolean isBroadcast() {
        return broadcaster != null && isBroadcast;
    }
    public synchronized void setBroadcast(boolean isBroadcast, EcgHttpBroadcast.IGetReceiversCallback callback) {
        if(broadcaster != null && this.isBroadcast != isBroadcast) {
            this.isBroadcast = isBroadcast;
            updateBroadcastStatus(this.isBroadcast);
            if(isBroadcast) {
                broadcaster.getUsers(callback);
            }
        }
    }
    public void addBroadcastReceiver(Account receiver, EcgHttpBroadcast.IAddReceiverCallback callback) {
        if(broadcaster != null) {
            broadcaster.addReceiver(receiver.getHuaweiId(), callback);
        }
    }
    public void deleteBroadcastReceiver(Account receiver, EcgHttpBroadcast.IDeleteReceiverCallback callback) {
        if(broadcaster != null) {
            broadcaster.deleteReceiver(receiver.getHuaweiId(), callback);
        }
    }
    public EcgMonitorState getEcgMonitorState() {
        return state;
    }
    private void setEcgMonitorState(EcgMonitorState state) {
        if(this.state != state) {
            this.state = state;
            updateEcgMonitorState();
        }
    }
    @Override
    public synchronized void updateConfig(EcgMonitorConfiguration config) {
        super.updateConfig(config);
        dataProcessor.resetHrAbnormalProcessor();
    }

    private int[] getWave1mV() {
        return wave1mV;
    }

    private boolean executeAfterConnectSuccess() {
        BleGattElement[] elements = new BleGattElement[]{ECGMONITOR_DATA, ECGMONITOR_DATA_CCC, ECGMONITOR_CTRL, ECGMONITOR_SAMPLE_RATE, ECGMONITOR_LEAD_TYPE};

        if(!deviceProxy.containGattElements(elements)) {
            ViseLog.e("EcgMonitor Elements wrong.");
            return false;
        }

        updateSampleRate(DEFAULT_SAMPLE_RATE);
        updateLeadType(DEFAULT_LEAD_TYPE);
        updateValue1mV(value1mV);

        // 启动电池电量测量
        containBatteryService = deviceProxy.containGattElement(BATTERY_DATA);
        if(containBatteryService) {
            startBatteryMeasure();
        }

        // 读采样率
        readSampleRate();
        // 读导联类型
        readLeadType();
        // 停止采样
        stopSampling();
        // 启动1mV值采样
        startValue1mVSampling();

        return true;
    }

    private void executeAfterDisconnect() {
        dataProcessor.stop();

        if(listener != null) {
            listener.onEcgSignalShowStateUpdated(false);
        }

        if(containBatteryService) {
            stopBatteryMeasure();
        }
    }

    private void executeAfterConnectFailure() {
        dataProcessor.stop();

        if(listener != null) {
            listener.onEcgSignalShowStateUpdated(false);
        }

        if(containBatteryService) {
            stopBatteryMeasure();
        }
    }

    // 关闭设备
    @Override
    public void close() {
        if(!isStopped()) {
            ViseLog.e("The device can't be closed currently.");
            return;
        }

        ViseLog.e("EcgMonitorDevice.close()");

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
        if(dataProcessor != null)
            dataProcessor.reset();

        // 停止广播
        setBroadcast(false, null);
        if(broadcaster != null) {
            broadcaster.stop();
            broadcaster = null;
        }

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

    @Override
    public void disconnect() {
        ViseLog.e("EcgMonitorDevice.disconnect()");

        if(containBatteryService) {
            stopBatteryMeasure();
            containBatteryService = false;
        }
        if(super.getState() == CONNECT && deviceProxy.isGattExecutorAlive()) {
            stopSampling();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.disconnect();
    }

    // 添加留言内容
    public synchronized void addCommentContent(String content) {
        if(creatorComment != null)
            creatorComment.appendContent(content);
    }

    // 读采样率
    private void readSampleRate() {
        deviceProxy.read(ECGMONITOR_SAMPLE_RATE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                sampleRate = (data[0] & 0xff) | ((data[1] << 8) & 0xff00);
                updateSampleRate(sampleRate);
                dataProcessor.resetValue1mVDetector();
                updateSignalShowSetup(sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION);
                if(listener != null) {
                    listener.onEcgSignalShowStateUpdated(true);
                }
                // 生成1mV波形数据
                int pixelPerData = Math.round(PIXEL_PER_GRID / (SECOND_PER_GRID * sampleRate));
                int N = 15 * PIXEL_PER_GRID/pixelPerData; // 15个栅格所需数据个数
                wave1mV = new int[N];
                for(int i = 0; i < N; i++) {
                    if(i > N/3 && i < N*2/3) {
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
        deviceProxy.read(ECGMONITOR_LEAD_TYPE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                leadType = EcgLeadType.getFromCode(data[0]);
                updateLeadType(leadType);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    // 启动ECG信号采集
    private void startEcgSignalSampling() {
        IBleDataCallback receiveCallback = new IBleDataCallback() {
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
        deviceProxy.notify(ECGMONITOR_DATA_CCC, true, receiveCallback);

        deviceProxy.write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_START_SIGNAL, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                setEcgMonitorState(EcgMonitorState.SAMPLING);
                dataProcessor.start();

                ViseLog.e("Ecg signal sampling started.");
            }

            @Override
            public void onFailure(BleException exception) {

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

        deviceProxy.notify(ECGMONITOR_DATA_CCC, true, receiveCallback);

        deviceProxy.runInstantly(new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                setEcgMonitorState(EcgMonitorState.CALIBRATING);
                dataProcessor.start();
            }
            @Override
            public void onFailure(BleException exception) {

            }
        });

        // start 1mv sampling
        deviceProxy.write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_START_1MV, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                ViseLog.e("1mV cali value sampling started.");
            }
            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    // 停止数据采集
    private void stopSampling() {
        deviceProxy.notify(ECGMONITOR_DATA_CCC, false, null);
        deviceProxy.write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_STOP, new IBleDataCallback() {
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
        if(ExecutorUtil.isDead(batteryService)) {
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
                    deviceProxy.read(BATTERY_DATA, new IBleDataCallback() {
                        @Override
                        public void onSuccess(byte[] data, BleGattElement element) {
                            updateBattery(data[0]);
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

        // 广播
        if(isBroadcast()) {
            broadcaster.sendEcgSignal(ecgSignal);
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

        // 广播
        if(isBroadcast()) {
            broadcaster.sendHrValue(hr);
        }
    }

    @Override
    public void setValue1mV(final int value1mV) {
        ViseLog.e("Calculated 1mV value before calibration: " + value1mV);
        stopSampling();

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

        if(broadcaster == null) {
            broadcaster = new EcgHttpBroadcast(EcgMonitorUtil.deleteColon(getAddress()),
                    AccountManager.getInstance().getAccount().getPhone(),
                    sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION, leadType.getCode());
            broadcaster.start();
        }

        // 输出1mV定标信号
        for(int data : wave1mV) {
            updateSignalValue(data);
        }

        // 启动心电信号采样
        startEcgSignalSampling();
    }

    private void updateEcgMonitorState() {
        if(listener != null)
            listener.onStateUpdated(state);
    }

    private void updateBroadcastStatus(final boolean isBroadcast) {
        if(listener != null)
            listener.onBroadcastStateUpdated(isBroadcast);
    }

    private void updateBattery(final int bat) {
        setBattery(bat);

        if(listener != null)
            listener.onBatteryUpdated(bat);
    }
}

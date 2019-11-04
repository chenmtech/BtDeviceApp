package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.EcgDataProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStatisticsInfo;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.HrStatisticProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.cmtech.android.ble.BleConfig.CCC_UUID;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.DIR_ECG_SIGNAL;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;
import static com.cmtech.android.bledevice.ecgmonitor.view.EcgMonitorFragment.ZERO_LOCATION_IN_ECG_VIEW;
import static com.cmtech.android.bledevice.ecgmonitor.view.ScanEcgView.PIXEL_PER_GRID;
import static com.cmtech.android.bledevice.ecgmonitor.view.ScanEcgView.SECOND_PER_GRID;
import static com.cmtech.android.bledeviceapp.BleDeviceConstant.MY_BASE_UUID;


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

public class EcgMonitorDevice extends BleDevice implements HrStatisticProcessor.OnHrStatisticInfoUpdatedListener {
    private static final String TAG = "EcgMonitorDevice";
    private static final int DEFAULT_VALUE_1MV = 164; // 缺省定标前1mV值
    private static final int DEFAULT_SAMPLE_RATE = 125; // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEAD_TYPE = EcgLeadType.LEAD_I; // 缺省导联为L1
    private static final int READ_BATTERY_PERIOD = 10; // 读电池电量的周期，分钟

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

    private int sampleRate = DEFAULT_SAMPLE_RATE; // 采样率
    private EcgLeadType leadType = DEFAULT_LEAD_TYPE; // 导联类型
    private int value1mV = DEFAULT_VALUE_1MV; // 定标之前1mV值
    private int[] waveData1mV; // 1mV波形数据，它的长度与采样率有关，幅度变化恒定，在读取采样率之后初始化
    private boolean isSaveFile = false; // 是否保存心电文件
    private boolean containBatteryService = false; // 是否包含电池电量测量服务
    private volatile EcgMonitorState state = EcgMonitorState.INIT; // 设备状态

    private final EcgMonitorConfiguration config; // 心电监护仪的配置信息
    private final EcgDataProcessor dataProcessor; // 心电数据处理器,在其内部的单线程ExecutorService中执行
    private EcgSignalRecorder signalRecorder; // 心电信号记录仪
    private EcgRecord ecgRecord; // 心电记录，可记录心电信号数据、用户留言和心率信息
    private ScheduledExecutorService batteryService; // 电池电量测量Service
    private OnEcgMonitorListener listener; // 心电监护仪监听器

    // 心电监护仪监听器
    public interface OnEcgMonitorListener {
        void onStateUpdated(EcgMonitorState state); // 状态更新
        void onSampleRateUpdated(int sampleRate); // 采样率更新
        void onLeadTypeUpdated(EcgLeadType leadType); // 导联类型更新
        void onValue1mVUpdated(int value1mV, int value1mVAfterCalibration);  // 1mV值更新
        void onRecordStateUpdated(boolean isRecord); // 记录状态更新
        void onShowSetupUpdated(int sampleRate, int value1mV, double zeroLocation); // 信号显示设置更新
        void onEcgSignalShowed(int ecgSignal); // 信号显示
        void onEcgSignalShowStarted(int sampleRate); // 信号显示启动
        void onEcgSignalShowStopped(); // 信号显示停止
        void onRecordSecondUpdated(int second); // 信号记录秒数更新
        void onHrUpdated(int hr); // 心率值更新，单位bpm
        void onHrStaticsInfoUpdated(EcgHrStatisticsInfo hrStaticsInfoAnalyzer); // 心率统计信息更新
        void onHrAbnormalNotified(); // 心率值异常通知
        void onBatteryUpdated(int bat); // 电池电量更新
    }

    // 构造器
    EcgMonitorDevice(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);

        // 从数据库获取设备的配置信息
        List<EcgMonitorConfiguration> configs = LitePal.where("macAddress = ?", registerInfo.getMacAddress()).find(EcgMonitorConfiguration.class);
        if(configs == null || configs.isEmpty()) {
            config = new EcgMonitorConfiguration();
            config.setMacAddress(registerInfo.getMacAddress());
            config.save();
        } else {
            config = configs.get(0);
        }

        dataProcessor = new EcgDataProcessor(this);
    }

    public int getSampleRate() { return sampleRate; }
    public EcgLeadType getLeadType() {
        return leadType;
    }
    public int getValue1mV() { return value1mV; }
    public boolean isRecording() {
        return ((signalRecorder != null) && signalRecorder.isRecording());
    }
    public synchronized void setRecordStatus(boolean record) {
        if(signalRecorder != null && signalRecorder.isRecording() != record) {
            // 当前isRecord与要设置的isRecord不同，就意味着要改变当前的isRecord状态
            signalRecorder.setRecording(record);
            updateRecordStatus(record);
        }
    }
    public void setSaveFile(boolean saveFile) {
        this.isSaveFile = saveFile;
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
    public EcgMonitorConfiguration getConfig() {
        return config;
    }
    public void updateConfig(EcgMonitorConfiguration config) {
        this.config.copyFrom(config);
        this.config.save();
        dataProcessor.resetHrAbnormalProcessor();
    }
    public int getRecordSecond() {
        return (signalRecorder == null) ? 0 : signalRecorder.getSecond();
    }
    public long getRecordDataNum() { return (signalRecorder == null) ? 0 : signalRecorder.getDataNum(); }
    public EcgRecord getEcgRecord() {
        return ecgRecord;
    }
    public int[] getWaveData1mV() {
        return waveData1mV;
    }

    @Override
    protected boolean executeAfterConnectSuccess() {
        BleGattElement[] elements = new BleGattElement[]{ECGMONITOR_DATA, ECGMONITOR_DATA_CCC, ECGMONITOR_CTRL, ECGMONITOR_SAMPLE_RATE, ECGMONITOR_LEAD_TYPE};

        if(!containGattElements(elements)) {
            ViseLog.e("Ecg Monitor Elements有错。");
            return false;
        }

        updateSampleRate(DEFAULT_SAMPLE_RATE);
        updateLeadType(DEFAULT_LEAD_TYPE);
        updateValue1mV(value1mV);

        // 启动电池电量测量
        containBatteryService = containGattElement(BATTERY_DATA);
        if(containBatteryService) {
            startBatteryMeasure();
        }

        // 读采样率
        readSampleRate();
        // 读导联类型
        readLeadType();
        // 停止数据采样
        stopDataSampling();
        // 启动1mV值采样
        startValue1mVSampling();

        return true;
    }

    @Override
    protected void executeAfterDisconnect() {
        dataProcessor.stop();

        if(listener != null) {
            listener.onEcgSignalShowStopped();
        }
        if(containBatteryService) {
            stopBatteryMeasure();
        }
    }

    @Override
    protected void executeAfterConnectFailure() {
        dataProcessor.stop();

        if(listener != null) {
            listener.onEcgSignalShowStopped();
        }
        if(containBatteryService) {
            stopBatteryMeasure();
        }
    }

    @Override
    public void open(Context context) {
        ViseLog.e("EcgMonitorDevice.open()");

        super.open(context);
    }

    // 关闭设备
    @Override
    public void close() {
        if(!isStopped()) {
            ViseLog.e("The device can't be closed currently.");
        }

        ViseLog.e("EcgMonitorDevice.close()");

        // 关闭记录
        if(ecgRecord != null) {
            if(isSaveFile) {
                saveEcgRecord();
            }
            ecgRecord = null;
            ViseLog.e("关闭Ecg记录。");
        }

        // 关闭信号记录器
        if(signalRecorder != null) {
            signalRecorder.setRecording(false);
            signalRecorder = null;
        }

        // 重置数据处理器
        dataProcessor.reset();

        super.close();
    }

    private void saveEcgRecord() {
        try {
            ecgRecord.moveSigFileTo(DIR_ECG_SIGNAL);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ecgRecord.setHrList(dataProcessor.getHrList());
        if(signalRecorder != null)
            ecgRecord.addComment(signalRecorder.getComment());
        ecgRecord.save();
    }

    @Override
    protected void disconnect() {
        ViseLog.e("EcgMonitorDevice.disconnect()");

        if(containBatteryService) {
            stopBatteryMeasure();
            containBatteryService = false;
        }
        if(isConnected() && isGattExecutorAlive()) {
            stopDataSampling();
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
        if(signalRecorder != null)
            signalRecorder.addCommentContent(content);
    }

    // 读采样率
    private void readSampleRate() {
        read(ECGMONITOR_SAMPLE_RATE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                sampleRate = (data[0] & 0xff) | ((data[1] << 8) & 0xff00);
                updateSampleRate(sampleRate);
                dataProcessor.resetValue1mVDetector();
                // 初始化信号显示设置
                initializeSignalShowSetup(sampleRate);
                if(listener != null) {
                    listener.onEcgSignalShowStarted(sampleRate);
                }
                // 生成1mV波形数据
                int pixelPerData = Math.round(PIXEL_PER_GRID / (SECOND_PER_GRID * sampleRate));
                int N = 15*PIXEL_PER_GRID/pixelPerData; // 15个栅格所需数据个数
                waveData1mV = new int[N];
                for(int i = 0; i < N; i++) {
                    if(i > N/3 && i < N*2/3) {
                        waveData1mV[i] = STANDARD_VALUE_1MV_AFTER_CALIBRATION;
                    } else {
                        waveData1mV[i] = 0;
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
        read(ECGMONITOR_LEAD_TYPE, new IBleDataCallback() {
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
    public void startEcgSignalSampling() {
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
        });
    }

    // 启动1mV信号采样
    public void startValue1mVSampling() {
        // enable ECG data notification
        IBleDataCallback receiveCallback = new IBleDataCallback() {
            @Override
            public void onSuccess(final byte[] data, BleGattElement element) {
                dataProcessor.processData(data, true);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };
        notify(ECGMONITOR_DATA_CCC, true, receiveCallback);

        runInstantly(new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                ViseLog.e("启动1mV值采样");

                setEcgMonitorState(EcgMonitorState.CALIBRATING);
                dataProcessor.start();
            }
            @Override
            public void onFailure(BleException exception) {

            }
        });

        // start 1mv sampling
        write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_START_1MV, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {

            }
            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    // 停止数据采集
    public void stopDataSampling() {
        ViseLog.e("停止数据采样");

        notify(ECGMONITOR_DATA_CCC, false, null);
        write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_STOP, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
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
                    read(BATTERY_DATA, new IBleDataCallback() {
                        @Override
                        public void onSuccess(byte[] data, BleGattElement element) {
                            updateBattery(data[0]);
                        }

                        @Override
                        public void onFailure(BleException exception) {

                        }
                    });
                }
            }, 0, READ_BATTERY_PERIOD, TimeUnit.MINUTES);
        }
    }

    // 停止电池电量测量
    private void stopBatteryMeasure() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(batteryService);

        ViseLog.e("停止电池电量测量服务");
    }

    // 登记心电监护仪设备监听器
    public void setListener(OnEcgMonitorListener listener) {
        this.listener = listener;
    }

    // 删除心电监护仪设备监听器
    public void removeListener() {
        listener = null;
    }

    public void updateSignalValue(final int ecgSignal) {
        // 记录
        if(isRecording()) {
            try {
                signalRecorder.record(ecgSignal);
            } catch (IOException e) {
                ViseLog.e("无法记录心电信号。");
            }
        }
        // 显示
        if(listener != null) {
            listener.onEcgSignalShowed(ecgSignal);
        }
    }

    public void updateHrValue(final short hr) {
        if(listener != null) {
            listener.onHrUpdated(hr);
        }
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
        ViseLog.e("定标前1mV值为: " + value1mV);

        this.value1mV = value1mV;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                updateValue1mV(value1mV);
            }
        });

        // 重置Ecg信号处理器
        dataProcessor.resetSignalProcessor();

        // 创建心电记录文件
        if(ecgRecord == null) {
            ecgRecord = EcgRecord.create(UserManager.getInstance().getUser(), sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION, getMacAddress(), leadType);
        }

        ViseLog.e("ecgRecord: " + ecgRecord);

        // 创建心电信号记录仪
        if(ecgRecord != null && signalRecorder == null) {
            signalRecorder = new EcgSignalRecorder(this);
        }

        // 输出1mV定标信号
        for(int data : waveData1mV) {
            updateSignalValue(data);
        }

        // 启动心电信号采样
        startEcgSignalSampling();
    }

    // 初始化信号显示设置
    private void initializeSignalShowSetup(int sampleRate) {
        // 更新信号显示设置
        updateSignalShowSetup(sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION);
    }

    private void updateEcgMonitorState() {
        if(listener != null)
            listener.onStateUpdated(state);
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

    private void updateRecordStatus(final boolean isRecord) {
        if(listener != null)
            listener.onRecordStateUpdated(isRecord);
    }

    private void updateSignalShowSetup(int sampleRate, int value1mV) {
        if(listener != null)
            listener.onShowSetupUpdated(sampleRate, value1mV, ZERO_LOCATION_IN_ECG_VIEW);
    }

    private void updateBattery(final int bat) {
        setBattery(bat);

        if(listener != null)
            listener.onBatteryUpdated(bat);
    }

    @Override
    public void onHrStatisticInfoUpdated(final EcgHrStatisticsInfo hrStatisticsInfoAnalyzer) {
        if(listener != null) {
            listener.onHrStaticsInfoUpdated(hrStatisticsInfoAnalyzer);
        }
    }
}

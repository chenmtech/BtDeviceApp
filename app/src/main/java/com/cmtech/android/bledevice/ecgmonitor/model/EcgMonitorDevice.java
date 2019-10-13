package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.EcgDataProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.EcgSignalRecorder;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStatisticsInfoAnalyzer;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.HrStatisticProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.cmtech.android.ble.BleConfig.CCC_UUID;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_FILE_DIR;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;
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
    private static final float DEFAULT_SECOND_PER_GRID = 0.04f; // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f; // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10; // 缺省每个栅格包含的像素个数

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
    private final int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // EcgView中每小格的像素个数
    private int xPixelPerData = 1; // EcgView的横向分辨率
    private float yValuePerPixel = 100.0f; // EcgView的纵向分辨率
    private boolean saveFile = false; // 是否保存心电文件
    private boolean containBatMeasService = false; // 是否测量电池电量
    private volatile EcgMonitorState state = EcgMonitorState.INIT; // 设备状态

    private final EcgMonitorConfig config; // 心电监护仪的配置信息
    private final EcgDataProcessor dataProcessor; // 心电数据处理器,在其内部的单线程ExecutorService中执行
    private EcgSignalRecorder signalRecorder; // 心电信号记录仪
    private EcgFile ecgFile; // 心电记录文件，可记录心电信号数据、留言和心率信息
    private ScheduledExecutorService batMeasureService; // 电池电量测量Service
    private OnEcgMonitorListener listener; // 心电监护仪监听器

    // 心电监护仪监听器
    public interface OnEcgMonitorListener {
        void onEcgMonitorStateUpdated(EcgMonitorState state); // 更新状态
        void onSampleRateChanged(int sampleRate); // 更新采样率
        void onLeadTypeChanged(EcgLeadType leadType); // 更新导联类型
        void onValue1mVChanged(int value1mV, int value1mVAfterCalibration);  // 更新1mV值
        void onSignalRecordStateUpdated(boolean isRecord); // 更新记录状态
        void onEcgViewUpdated(int xPixelPerData, float yValuePerPixel, int gridPixels); // 更新EcgView
        void onEcgSignalUpdated(int ecgSignal); // 更新Ecg信号
        void onEcgSignalShowStarted(int sampleRate); // 启动信号显示
        void onEcgSignalShowStoped(); // 停止信号显示
        void onSignalSecondNumChanged(int second); // 更新信号记录秒数
        void onEcgHrChanged(int hr); // 更新心率值，单位bpm
        void onEcgHrStaticsInfoUpdated(EcgHrStatisticsInfoAnalyzer hrStaticsInfoAnalyzer); // 更新心率统计信息
        void onHrAbnormalNotified(); // 通知心率值异常
        void onBatteryChanged(int bat); // 电池电量改变
    }

    // 构造器
    EcgMonitorDevice(Context context, BleDeviceRegisterInfo registerInfo) {
        super(context, registerInfo);

        // 从数据库获取设备的配置信息
        List<EcgMonitorConfig> foundConfig = LitePal.where("macAddress = ?", registerInfo.getMacAddress()).find(EcgMonitorConfig.class);
        if(foundConfig == null || foundConfig.isEmpty()) {
            config = new EcgMonitorConfig();
            config.setMacAddress(registerInfo.getMacAddress());
            config.save();
        } else {
            config = foundConfig.get(0);
        }

        dataProcessor = new EcgDataProcessor(this);
    }

    public int getSampleRate() { return sampleRate; }
    public EcgLeadType getLeadType() {
        return leadType;
    }
    public int getValue1mV() { return value1mV; }
    public boolean isRecordSignal() {
        return ((signalRecorder != null) && signalRecorder.isRecord());
    }
    public synchronized void setRecordSignal(boolean isRecord) {
        if(signalRecorder != null && signalRecorder.isRecord() != isRecord) {
            // 当前isRecord与要设置的isRecord不同，就意味着要改变当前的isRecord状态
            signalRecorder.setRecord(isRecord);
            updateRecordStatus(isRecord);
        }
    }
    public void setSaveFile(boolean saveFile) {
        this.saveFile = saveFile;
    }
    public int getPixelPerGrid() { return pixelPerGrid; }
    public int getXPixelPerData() { return xPixelPerData; }
    public float getYValuePerPixel() { return yValuePerPixel; }
    public EcgMonitorState getEcgMonitorState() {
        return state;
    }
    private void setEcgMonitorState(EcgMonitorState state) {
        if(this.state != state) {
            this.state = state;
            updateEcgMonitorState();
        }
    }
    public EcgMonitorConfig getConfig() {
        return config;
    }
    public void updateConfig(EcgMonitorConfig config) {
        this.config.setWarnWhenHrAbnormal(config.isWarnWhenHrAbnormal());
        this.config.setHrLowLimit(config.getHrLowLimit());
        this.config.setHrHighLimit(config.getHrHighLimit());
        this.config.setMarkerList(config.getMarkerList());
        this.config.save();
        dataProcessor.resetHrAbnormalProcessor();
    }
    public int getRecordSignalSecond() {
        return (signalRecorder == null) ? 0 : signalRecorder.getSecond();
    }
    public long getRecordSignalDataNum() { return (signalRecorder == null) ? 0 : signalRecorder.getDataNum(); }
    public EcgFile getEcgFile() {
        return ecgFile;
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

        containBatMeasService = containGattElement(BATTERY_DATA);
        if(containBatMeasService) {
            startBatteryMeasure();
        }

        // 读采样率
        readSampleRate();
        // 读导联类型
        readLeadType();

        stopDataSampling();

        // 启动检测1mV值
        startValue1mVDetection();

        return true;
    }

    @Override
    protected void executeAfterDisconnect() {
        dataProcessor.stop();

        if(listener != null) {
            listener.onEcgSignalShowStoped();
        }

        if(containBatMeasService) {
            stopBatteryMeasure();
        }
    }

    @Override
    protected void executeAfterConnectFailure() {
        dataProcessor.stop();

        if(listener != null) {
            listener.onEcgSignalShowStoped();
        }

        if(containBatMeasService) {
            stopBatteryMeasure();
        }
    }

    @Override
    public void open() {
        ViseLog.e("EcgMonitorDevice.open()");

        super.open();
    }

    // 关闭设备
    @Override
    public void close() {
        if(!isDisconnected()) {
            throw new IllegalStateException("The device is not disconnected and can't be closed.");
        }

        ViseLog.e("EcgMonitorDevice.close()");

        // 关闭文件
        if(ecgFile != null) {
            try {
                if(saveFile) {
                    saveEcgFileTail();
                    ecgFile.close();
                    File toFile = FileUtil.getFile(ECG_FILE_DIR, ecgFile.getFile().getName());
                    FileUtil.moveFile(ecgFile.getFile(), toFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    ecgFile.close();
                    FileUtil.deleteFile(ecgFile.getFile());
                    ecgFile = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ViseLog.e("关闭Ecg文件。");
        }

        // 关闭信号记录器
        if(signalRecorder != null) {
            signalRecorder.setRecord(false);
            signalRecorder = null;
        }

        // 重置心率统计处理器
        dataProcessor.resetHrStatisticProcessor();

        super.close();
    }

    private void saveEcgFileTail() throws IOException{
        ecgFile.setHrList(dataProcessor.getHrList());
        if(signalRecorder != null)
            ecgFile.addComment(signalRecorder.getComment());
        ecgFile.save();
    }

    @Override
    protected void disconnect() {
        ViseLog.e("EcgMonitorDevice.disconnect()");

        if(containBatMeasService) {
            stopBatteryMeasure();
            containBatMeasService = false;
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

                dataProcessor.resetValue1mVCalculator();

                // 初始化EcgView
                initializeEcgView(sampleRate);
                if(listener != null) {
                    listener.onEcgSignalShowStarted(sampleRate);
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

    // 启动检测1mV值
    public void startValue1mVDetection() {
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
                ViseLog.e("启动检测1mV值");

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
        if(batMeasureService == null || batMeasureService.isTerminated()) {
            ViseLog.e("启动电池电量测量服务");

            batMeasureService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_Bat_Measure");
                }
            });

            batMeasureService.scheduleAtFixedRate(new Runnable() {
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
            }, 0, 10, TimeUnit.MINUTES);
        }
    }

    // 停止电池电量测量
    private void stopBatteryMeasure() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(batMeasureService);

        ViseLog.e("停止电池电量测量服务");
    }

    // 登记心电监护仪设备监听器
    public void setEcgMonitorListener(OnEcgMonitorListener listener) {
        this.listener = listener;
    }

    // 删除心电监护仪设备监听器
    public void removeEcgMonitorListener() {
        listener = null;
    }

    public void updateSignalValue(final int ecgSignal) {
        // 记录
        if(isRecordSignal()) {
            try {
                signalRecorder.record(ecgSignal);
            } catch (IOException e) {
                ViseLog.e("无法记录心电信号。");
            }
        }
        // 通知观察者
        if(listener != null) {
            listener.onEcgSignalUpdated(ecgSignal);
        }
    }

    public void updateHrValue(final short hr) {
        if(listener != null) {
            listener.onEcgHrChanged(hr);
        }
    }

    public void notifyHrAbnormal() {
        if(listener != null) {
            listener.onHrAbnormalNotified();
        }
    }

    public void updateRecordSecNum(final int second) {
        if(listener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onSignalSecondNumChanged(second);
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
        if(ecgFile == null) {
            try {
                ecgFile = EcgFile.create(sampleRate, STANDARD_VALUE_1MV_AFTER_CALIBRATION, getMacAddress(), leadType);
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(), "无法记录心电信息", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // 创建心电信号记录仪
        if(ecgFile != null && signalRecorder == null) {
            signalRecorder = new EcgSignalRecorder(this);
        }

        startEcgSignalSampling();
    }

    // 初始化EcgView
    private void initializeEcgView(int sampleRate) {
        //pixelPerGrid = DEFAULT_PIXEL_PER_GRID;                   // 每小格的像素个数
        // 计算EcgView分辨率
        xPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_GRID * sampleRate)); // 计算横向分辨率
        yValuePerPixel = STANDARD_VALUE_1MV_AFTER_CALIBRATION * DEFAULT_MV_PER_GRID / pixelPerGrid; // 计算纵向分辨率
        // 更新EcgView
        updateEcgView(xPixelPerData, yValuePerPixel, pixelPerGrid);
    }

    private void updateEcgMonitorState() {
        if(listener != null)
            listener.onEcgMonitorStateUpdated(state);
    }

    private void updateSampleRate(final int sampleRate) {
        if(listener != null)
            listener.onSampleRateChanged(sampleRate);
    }

    private void updateLeadType(final EcgLeadType leadType) {
        if(listener != null)
            listener.onLeadTypeChanged(leadType);
    }

    private void updateValue1mV(final int value1mV) {
        if(listener != null)
            listener.onValue1mVChanged(value1mV, STANDARD_VALUE_1MV_AFTER_CALIBRATION);
    }

    private void updateRecordStatus(final boolean isRecord) {
        if(listener != null)
            listener.onSignalRecordStateUpdated(isRecord);
    }

    private void updateEcgView(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        if(listener != null)
            listener.onEcgViewUpdated(xPixelPerData, yValuePerPixel, gridPixels);
    }

    private void updateBattery(final int bat) {
        setBattery(bat);

        if(listener != null)
            listener.onBatteryChanged(bat);
    }

    @Override
    public void onHrStatisticInfoUpdated(final EcgHrStatisticsInfoAnalyzer hrStatisticsInfoAnalyzer) {
        if(listener != null) {
            listener.onEcgHrStaticsInfoUpdated(hrStatisticsInfoAnalyzer);
        }
    }
}

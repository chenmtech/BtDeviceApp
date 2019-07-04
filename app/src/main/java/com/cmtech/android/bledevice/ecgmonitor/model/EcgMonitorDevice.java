package com.cmtech.android.bledevice.ecgmonitor.model;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceBasicInfo;
import com.cmtech.android.ble.extend.BleGattElement;
import com.cmtech.android.ble.extend.GattDataException;
import com.cmtech.android.ble.extend.IGattDataCallback;
import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.Ecg1mVCaliValueCalculator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.EcgDataProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.EcgSignalRecorder;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.ecghrprocess.EcgHrStatisticInfoAnalyzer;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.ecghrprocess.OnHrStatisticInfoListener;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_FILE_DIR;
import static com.cmtech.android.bledeviceapp.BleDeviceConstant.CCCUUID;
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

public class EcgMonitorDevice extends BleDevice implements OnHrStatisticInfoListener {
    private final static String TAG = "EcgMonitorDevice";

    // 常量
    private static final int DEFAULT_VALUE_1MV_AFTER_CALIBRATION = 65536; // 缺省1mV定标值

    private static final int DEFAULT_SAMPLERATE = 125;                          // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEADTYPE = EcgLeadType.LEAD_I;     // 缺省导联为L1
    private static final float DEFAULT_SECOND_PER_GRID = 0.04f;                 // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f;                      // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10;                       // 缺省每个栅格包含的像素个数

    // 心电监护仪Service UUID常量
    private static final String ecgMonitorServiceUuid       = "aa40";           // 心电监护仪服务UUID:aa40
    private static final String ecgMonitorDataUuid          = "aa41";           // ECG数据特征UUID:aa41
    private static final String ecgMonitorCtrlUuid          = "aa42";           // 测量控制UUID:aa42
    private static final String ecgMonitorSampleRateUuid    = "aa44";           // 采样率UUID:aa44
    private static final String ecgMonitorLeadTypeUuid      = "aa45";           // 导联类型UUID:aa45

    // 电池电量Service UUID常量
    private static final String batteryServiceUuid       = "aa90";           // 电池电量服务UUID:aa90
    private static final String batteryDataUuid          = "aa91";           // 电池电量数据特征UUID:aa91

    // Gatt Element常量
    private static final BleGattElement ECGMONITOR_DATA =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, null, MY_BASE_UUID, "心电数据");
    private static final BleGattElement ECGMONITOR_DATA_CCC =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, CCCUUID, MY_BASE_UUID, "心电数据CCC");
    private static final BleGattElement ECGMONITOR_CTRL =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorCtrlUuid, null, MY_BASE_UUID, "心电Ctrl");
    private static final BleGattElement ECGMONITOR_SAMPLERATE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorSampleRateUuid, null, MY_BASE_UUID, "采样率");
    private static final BleGattElement ECGMONITOR_LEADTYPE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorLeadTypeUuid, null, MY_BASE_UUID, "导联类型");
    private static final BleGattElement BATTERY_DATA =
            new BleGattElement(batteryServiceUuid, batteryDataUuid, null, MY_BASE_UUID, "电池电量数据");

    // ECGMONITOR_CTRL Element的控制常量
    private static final byte ECGMONITOR_CTRL_STOP =             (byte) 0x00;        // 停止采集
    private static final byte ECGMONITOR_CTRL_STARTSIGNAL =      (byte) 0x01;        // 启动采集Ecg信号
    private static final byte ECGMONITOR_CTRL_START1MV =         (byte) 0x02;        // 启动采集1mV定标


    private int sampleRate = DEFAULT_SAMPLERATE; // 采样率

    private EcgLeadType leadType = DEFAULT_LEADTYPE; // 导联类型

    private int value1mVBeforeCalibration = 0; // 定标之前1mV对应的数值

    private final int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // EcgView中每小格的像素个数

    private int xPixelPerData = 1; // EcgView的横向分辨率

    private float yValuePerPixel = 100.0f; // EcgView的纵向分辨率

    private boolean isSaveEcgFile = false; // 是否保存心电文件

    private boolean isMeasureBattery = false; // 是否测量电池电量

    private volatile EcgMonitorState state = EcgMonitorState.INIT; // 设备状态

    private final EcgMonitorDeviceConfig config; // 心电监护仪设备配置信息



    private ScheduledExecutorService batMeasureService; // 设备电量测量Service

    private final EcgDataProcessor ecgDataProcessor = new EcgDataProcessor(this); // 数据处理是在其内部的单线程ExecutorService中执行

    private EcgSignalRecorder signalRecorder; // 心电信号记录仪

    private EcgFile ecgFile; // 心电记录文件，可记录心电信号以及留言和心率信息



    private OnEcgMonitorDeviceListener listener; // 心电监护仪设备监听器



    // 构造器
    EcgMonitorDevice(BleDeviceBasicInfo basicInfo) {
        super(basicInfo);

        // 从数据库获取设备的配置信息
        List<EcgMonitorDeviceConfig> foundConfig = LitePal.where("macAddress = ?", basicInfo.getMacAddress()).find(EcgMonitorDeviceConfig.class);

        if(foundConfig == null || foundConfig.isEmpty()) {
            config = new EcgMonitorDeviceConfig();

            config.setMacAddress(basicInfo.getMacAddress());

            config.save();
        } else {
            config = foundConfig.get(0);
        }
    }

    public int getSampleRate() { return sampleRate; }

    public EcgLeadType getLeadType() {
        return leadType;
    }

    public int getValue1mVBeforeCalibration() { return value1mVBeforeCalibration; }

    public int getValue1mVAfterCalibration() { return DEFAULT_VALUE_1MV_AFTER_CALIBRATION; }

    public boolean isRecordEcgSignal() {
        return ((signalRecorder != null) && signalRecorder.isRecord());
    }

    public void setSaveEcgFile(boolean saveEcgFile) {
        isSaveEcgFile = saveEcgFile;
    }

    public int getPixelPerGrid() { return pixelPerGrid; }

    public int getXPixelPerData() { return xPixelPerData; }

    public float getYValuePerPixel() { return yValuePerPixel; }

    public EcgMonitorState getState() {
        return state;
    }

    private void setState(EcgMonitorState state) {
        if(this.state != state) {
            this.state = state;

            updateEcgMonitorState();
        }
    }

    public EcgMonitorDeviceConfig getConfig() {
        return config;
    }

    public void setConfig(EcgMonitorDeviceConfig config) {
        this.config.setWarnWhenHrAbnormal(config.isWarnWhenHrAbnormal());

        this.config.setHrLowLimit(config.getHrLowLimit());

        this.config.setHrHighLimit(config.getHrHighLimit());

        this.config.save();

        if(ecgDataProcessor.getSignalProcessor() != null) {
            ecgDataProcessor.getSignalProcessor().setHrAbnormalWarner(config.isWarnWhenHrAbnormal(), config.getHrLowLimit(), config.getHrHighLimit());
        }
    }

    public int getEcgSignalRecordSecond() {
        return (signalRecorder == null) ? 0 : signalRecorder.getSecond();
    }

    public long getEcgSignalRecordDataNum() { return (signalRecorder == null) ? 0 : signalRecorder.getDataNum(); }

    @Override
    protected void executeAfterConnectSuccess() {
        BleGattElement[] elements = new BleGattElement[]{ECGMONITOR_DATA, ECGMONITOR_DATA_CCC, ECGMONITOR_CTRL, ECGMONITOR_SAMPLERATE, ECGMONITOR_LEADTYPE};

        if(!isContainGattElements(elements)) {
            ViseLog.e("Ecg Monitor Elements are wrong.");

            disconnect();

            return;
        }

        updateSampleRate(DEFAULT_SAMPLERATE);

        updateLeadType(DEFAULT_LEADTYPE);

        updateCalibrationValue(value1mVBeforeCalibration);



        isMeasureBattery = isContainGattElement(BATTERY_DATA);

        startBatteryMeasure();


        // 停止采样
        stopDataSampling();

        // 读采样率
        readSampleRate();

        // 读导联类型
        readLeadType();

        // 启动1mV定标
        start1mVCalibration();
    }

    @Override
    protected void executeAfterDisconnect() {
        if(listener != null) {
            listener.onEcgSignalShowStoped();
        }

        ecgDataProcessor.close();
    }

    @Override
    protected void executeAfterConnectFailure() {
        if(listener != null) {
            listener.onEcgSignalShowStoped();
        }

        ecgDataProcessor.close();
    }

    @Override
    public void open() {
        ViseLog.e("EcgMonitorDevice open()");

        super.open();
    }

    // 关闭设备
    @Override
    public void close() {
        ViseLog.e("EcgMonitorDevice close()");

        if(isConnected()) {
            disconnect();
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // 关闭记录器
                if(signalRecorder != null) {
                    signalRecorder.close();

                    signalRecorder = null;

                    ViseLog.e("The signal recorder closed.");
                }

                if(ecgFile != null) {
                    try {
                        if(isSaveEcgFile) {
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

                    ViseLog.e("The ECG file closed.");
                }

                EcgMonitorDevice.super.close();
            }
        });

    }

    private void saveEcgFileTail() throws IOException{
        ecgFile.setHrList(ecgDataProcessor.getSignalProcessor().getHrList());

        ecgFile.addComment(signalRecorder.getComment());

        ecgFile.save();
    }

    @Override
    protected void disconnect() {
        ViseLog.e("EcgMonitorDevice disconnect()");

        if(listener != null) {
            listener.onEcgSignalShowStoped();
        }

        if(isConnected() && isGattExecutorAlive()) {
            final CountDownLatch lock = new CountDownLatch(1);

            stopDataSampling();

            if(isMeasureBattery) {
                stopBatteryMeasure();

                isMeasureBattery = false;
            }

            runInstantly(new IGattDataCallback() {
                @Override
                public void onSuccess(byte[] data) {
                    lock.countDown();
                }

                @Override
                public void onFailure(GattDataException exception) {
                    lock.countDown();
                }
            });

            try {
                lock.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                ecgDataProcessor.stop();
            }
        } else {
            ecgDataProcessor.stop();
        }

        ecgDataProcessor.close();

        super.disconnect();
    }



    // 设置是否记录心电信号
    public synchronized void setEcgSignalRecord(boolean isRecord) {
        if(signalRecorder == null || signalRecorder.isRecord() == isRecord) return;

        // 当前isRecord与要设置的isRecord不同，就意味着要改变当前的isRecord状态
        signalRecorder.setRecord(isRecord);

        updateRecordStatus(isRecord);
    }

    // 添加留言内容
    public synchronized void addCommentContent(String content) {
        signalRecorder.addCommentContent(content);
    }







    // 读采样率
    private void readSampleRate() {
        read(ECGMONITOR_SAMPLERATE, new IGattDataCallback() {
            @Override
            public void onSuccess(byte[] data) {
                int sampleRate = (data[0] & 0xff) | ((data[1] << 8) & 0xff00);

                updateSampleRate(sampleRate);

                // 有了采样率，可以初始化1mV定标值计算器
                Ecg1mVCaliValueCalculator caliValue1mVCalculator = new Ecg1mVCaliValueCalculator(EcgMonitorDevice.this, sampleRate);

                ecgDataProcessor.setCaliValueCalculator(caliValue1mVCalculator);
            }

            @Override
            public void onFailure(GattDataException exception) {

            }
        });
    }

    // 读导联类型
    private void readLeadType() {
        read(ECGMONITOR_LEADTYPE, new IGattDataCallback() {
            @Override
            public void onSuccess(byte[] data) {
                leadType = EcgLeadType.getFromCode(data[0]);

                updateLeadType(leadType);
            }

            @Override
            public void onFailure(GattDataException exception) {

            }
        });
    }

    // 启动ECG信号采集
    public void startEcgSignalSampling() {
        IGattDataCallback notificationCallback = new IGattDataCallback() {
            @Override
            public void onSuccess(final byte[] data) {
                ecgDataProcessor.processEcgData(data);
            }

            @Override
            public void onFailure(GattDataException exception) {
                ViseLog.e(exception);
                disconnect();
            }
        };

        // enable ECG data notification
        notify(ECGMONITOR_DATA_CCC, true, notificationCallback);

        write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_STARTSIGNAL, new IGattDataCallback() {
            @Override
            public void onSuccess(byte[] data) {
                ViseLog.e("The ECG signal sampling started");

                setState(EcgMonitorState.SAMPLEING);

                if(listener != null) {
                    listener.onEcgSignalShowStarted(sampleRate);
                }

                ecgDataProcessor.start();
            }

            @Override
            public void onFailure(GattDataException exception) {

            }
        });
    }

    // 启动1mV定标
    public void start1mVCalibration() {
        IGattDataCallback notificationCallback = new IGattDataCallback() {
            @Override
            public void onSuccess(final byte[] data) {
                ecgDataProcessor.processCalibrateData(data);
            }

            @Override
            public void onFailure(GattDataException exception) {

            }
        };

        // enable ECG data notification
        notify(ECGMONITOR_DATA_CCC, true, notificationCallback);

        write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_START1MV, new IGattDataCallback() {
            @Override
            public void onSuccess(byte[] data) {
                ViseLog.e("The 1mV Calibration started.");

                setState(EcgMonitorState.CALIBRATING);

                ecgDataProcessor.start();
            }

            @Override
            public void onFailure(GattDataException exception) {

            }
        });
    }

    // 停止数据采集
    public void stopDataSampling() {
        notify(ECGMONITOR_DATA_CCC, false, null);

        write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_STOP, new IGattDataCallback() {
            @Override
            public void onSuccess(byte[] data) {
                if(state == EcgMonitorState.CALIBRATING) {
                    ViseLog.e("The 1mV calibration stopped.");
                } else if(state == EcgMonitorState.SAMPLEING) {
                    ViseLog.e("The ECG signal sampling stopped.");
                }

                ecgDataProcessor.stop();
            }

            @Override
            public void onFailure(GattDataException exception) {

            }
        });
    }

    // 开始电池电量测量
    private void startBatteryMeasure() {
        if(isMeasureBattery && (batMeasureService == null || batMeasureService.isTerminated())) {
            ViseLog.e("The battery measure service starts.");

            batMeasureService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_Bat_Measure");
                }
            });

            batMeasureService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    read(BATTERY_DATA, new IGattDataCallback() {
                        @Override
                        public void onSuccess(byte[] data) {
                            updateBattery(data[0]);
                        }

                        @Override
                        public void onFailure(GattDataException exception) {

                        }
                    });
                }
            }, 0, 10, TimeUnit.MINUTES);
        }
    }

    // 停止电池电量测量
    private void stopBatteryMeasure() {
        ViseLog.e("The battery measure service stops.");

        ExecutorUtil.shutdownNowAndAwaitTerminate(batMeasureService);
    }









    // 登记心电监护仪设备监听器
    public void setEcgMonitorDeviceListener(OnEcgMonitorDeviceListener listener) {
        this.listener = listener;
    }

    // 删除心电监护仪设备监听器
    public void removeEcgMonitorDeviceListener() {
        listener = null;
    }

    public void onSignalValueUpdated(final int ecgSignal) {
        // 记录
        if(signalRecorder != null && signalRecorder.isRecord()) {
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

    public void onHrValueUpdated(final short hr) {
        if(listener != null) {
            listener.onEcgHrChanged(hr);
        }
    }

    @Override
    public void onHrStatisticInfoUpdated(final EcgHrStatisticInfoAnalyzer hrInfoObject) {
        if(listener != null) {
            listener.onEcgHrInfoUpdated(hrInfoObject);
        }
    }

    public void onHrAbnormalNotified() {
        if(listener != null) {
            listener.onNotifyHrAbnormal();
        }
    }

    public void onRecordSecNumUpdated(final int second) {
        if(listener != null) {
            listener.onSignalSecNumChanged(second);
        }
    }

    public void on1mVCaliValueUpdated(int caliValue1mV) {
        ViseLog.e("The 1mV Calibration Value is: " + caliValue1mV);

        stopDataSampling();

        value1mVBeforeCalibration = caliValue1mV;

        updateCalibrationValue(value1mVBeforeCalibration);

        // 重新创建Ecg信号处理器
        createEcgSignalProcessor(); // 这里每次连接都会重新创建处理器，有问题。

        // 创建心电记录文件
        if(ecgFile == null) {
            try {
                ecgFile = EcgFile.create(sampleRate, DEFAULT_VALUE_1MV_AFTER_CALIBRATION, getMacAddress(), leadType);
            } catch (IOException e) {
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(), "无法记录心电信息", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // 创建心电信号记录仪
        if(ecgFile != null && signalRecorder == null) {
            signalRecorder = new EcgSignalRecorder(this, sampleRate, ecgFile);
        }

        // 初始化EcgView
        initializeEcgView(sampleRate);

        startEcgSignalSampling();
    }

    // 创建心电信号处理器
    private void createEcgSignalProcessor() {
        EcgSignalProcessor signalProcessor = ecgDataProcessor.getSignalProcessor();

        signalProcessor.getEcgCalibrator().setValue1mVBeforeCalibration(value1mVBeforeCalibration);

        signalProcessor.getEcgFilter().setSampleRate(sampleRate);

        QrsDetector qrsDetector = new QrsDetector(sampleRate, DEFAULT_VALUE_1MV_AFTER_CALIBRATION);

        signalProcessor.setQrsDetector(qrsDetector);

        signalProcessor.setHrAbnormalWarner(config.isWarnWhenHrAbnormal(), config.getHrLowLimit(), config.getHrHighLimit());

    }

    // 初始化EcgView
    private void initializeEcgView(int sampleRate) {
        //pixelPerGrid = DEFAULT_PIXEL_PER_GRID;                   // 每小格的像素个数
        // 计算EcgView分辨率
        xPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_GRID * sampleRate)); // 计算横向分辨率

        yValuePerPixel = DEFAULT_VALUE_1MV_AFTER_CALIBRATION * DEFAULT_MV_PER_GRID / pixelPerGrid; // 计算纵向分辨率
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

    private void updateCalibrationValue(final int calibrationValueBefore) {
        if(listener != null)
            listener.onCalibrationValueChanged(calibrationValueBefore, DEFAULT_VALUE_1MV_AFTER_CALIBRATION);
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

}

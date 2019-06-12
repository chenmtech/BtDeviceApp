package com.cmtech.android.bledevice.ecgmonitor.model;

import android.os.Message;
import android.widget.Toast;

import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceBasicInfo;
import com.cmtech.android.ble.extend.BleGattElement;
import com.cmtech.android.ble.extend.GattDataException;
import com.cmtech.android.ble.extend.IGattDataCallback;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.OnEcgSignalProcessListener;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrInfoObject;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrProcessor;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_FILE_DIR;
import static com.cmtech.android.bledeviceapp.BleDeviceConstant.CCCUUID;
import static com.cmtech.android.bledeviceapp.BleDeviceConstant.MY_BASE_UUID;


/**
 * EcgMonitorDevice: 心电带设备类
 * Created by bme on 2018/9/20.
 *
 * Updated by chenm on 2019/4/8
 * 优化代码
 */

public class EcgMonitorDevice extends BleDevice implements OnEcgSignalProcessListener, EcgSignalRecorder.IEcgSignalSecNumUpdatedListener, EcgCalibrateDataProcessor.ICalibrateValueUpdatedListener {
    private final static String TAG = "EcgMonitorDevice";

    // 常量
    private static final int DEFAULT_SAMPLERATE = 125;                          // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEADTYPE = EcgLeadType.LEAD_I;     // 缺省导联为L1
    private static final int DEFAULT_CALIBRATIONVALUE = 65536;                  // 缺省1mV定标值
    private static final float DEFAULT_SECOND_PER_GRID = 0.04f;                 // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f;                      // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10;                       // 缺省每个栅格包含的像素个数

    // GATT消息常量
    private static final int MSG_SAMPLERATE_OBTAINED = 1;                          // 获取到采样率
    private static final int MSG_LEADTYPE_OBTAINED = 2;                            // 获取导联类型
    private static final int MSG_START_SAMPLINGSIGNAL = 3;                       // 开始采集Ecg信号
    private static final int MSG_BATTERY_OBTAINED = 4;

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

    private int value1mVBeforeCalibrate = 0; // 定标之前1mV对应的数值

    private final int value1mVAfterCalibrate = DEFAULT_CALIBRATIONVALUE; // 定标之后1mV对应的数值

    private final int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // EcgView中每小格的像素个数

    private int xPixelPerData = 2; // EcgView的横向分辨率

    private float yValuePerPixel = 100.0f; // EcgView的纵向分辨率

    private boolean isSaveEcgFile = false; // 是否保存心电文件

    private boolean isMeasureBattery = false;

    private volatile EcgMonitorState state = EcgMonitorState.INIT; // 设备状态

    private final EcgMonitorDeviceConfig config; // 心电监护仪设备配置信息

    private OnEcgMonitorListener listener; // 心电监护仪设备监听器

    private EcgCalibrateDataProcessor calibrateDataProcessor; // 标定数据处理器

    private EcgSignalProcessor signalProcessor; // 心电信号处理器

    private final EcgDataProcessor ecgDataProcessor = new EcgDataProcessor();

    private EcgSignalRecorder signalRecorder; // 心电信号记录仪

    private EcgFile ecgFile; // 心电记录文件，可记录心电信号以及留言和心率信息

    //private final LinkedBlockingQueue<Integer> dataBuff = new LinkedBlockingQueue<>();	//数据缓存

    private ScheduledExecutorService readBatteryService;

    private ExecutorService receiveService = Executors.newSingleThreadExecutor();

    // 数据处理线程Runnable
    private final Runnable processRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    //int value = dataBuff.take();
                    switch (state) {
                        case CALIBRATING:
                            ecgDataProcessor.processCalibrateData();
                            break;
                        case SAMPLE:
                            ecgDataProcessor.processEcgSignalData();
                            break;
                        default:
                            break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                ecgDataProcessor.clearData();
            }
        }
    };

    // 信号处理线程
    private Thread sigProcessThread;


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

    public int getValue1mVBeforeCalibrate() { return value1mVBeforeCalibrate; }

    public int getValue1mVAfterCalibrate() { return value1mVAfterCalibrate; }

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

        if(signalProcessor != null) {
            signalProcessor.setHrAbnormalWarner(config.isWarnWhenHrAbnormal(), config.getHrLowLimit(), config.getHrHighLimit(), this);
        }
    }

    public int getEcgSignalRecordSecond() {
        return (signalRecorder == null) ? 0 : signalRecorder.getSecond();
    }

    public long getEcgSignalRecordDataNum() { return (signalRecorder == null) ? 0 : signalRecorder.getDataNum(); }

    @Override
    protected boolean executeAfterConnectSuccess() {
        updateSampleRate(DEFAULT_SAMPLERATE);

        updateLeadType(DEFAULT_LEADTYPE);

        updateCalibrationValue(value1mVBeforeCalibrate, value1mVAfterCalibrate);

        // 启动gattOperator
        startGattExecutor();

        // 验证Gatt Elements
        BleGattElement[] batteryElements = new BleGattElement[]{BATTERY_DATA};

        if(checkElements(batteryElements)) {
            isMeasureBattery = true;
        }

        if(isMeasureBattery) {
            startBatteryMeasuring();
        }

        // 验证EcgMonitor Gatt Elements
        BleGattElement[] elements = new BleGattElement[]{ECGMONITOR_DATA, ECGMONITOR_DATA_CCC, ECGMONITOR_CTRL, ECGMONITOR_SAMPLERATE, ECGMONITOR_LEADTYPE};

        if(!checkElements(elements)) {
            return false;
        }

        // 先停止采样
        stopDataSampling();

        // 读采样率
        readSampleRate();

        // 读导联类型
        readLeadType();

        sigProcessThread = new Thread(processRunnable);

        sigProcessThread.start();

        // 启动1mV采样进行定标
        start1mVSampling();

        return true;
    }

    @Override
    protected void executeAfterDisconnect() {
        stopGattExecutor();

        if(isMeasureBattery) {
            stopBatteryMeasuring();

            isMeasureBattery = false;
        }

        if(sigProcessThread != null) {
            sigProcessThread.interrupt();

            try {
                sigProcessThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void executeAfterConnectFailure() {
        stopGattExecutor();

        if(isMeasureBattery) {
            stopBatteryMeasuring();
            isMeasureBattery = false;
        }

        if(sigProcessThread != null) {
            sigProcessThread.interrupt();
        }
    }

    @Override
    protected void processMessage(Message msg)
    {
        ViseLog.e("processMessage " + msg + " in " + Thread.currentThread());

        switch (msg.what) {
            // 接收到采样率
            case MSG_SAMPLERATE_OBTAINED:
                if(msg.obj != null) {
                    sampleRate = (Integer) msg.obj;

                    updateSampleRate(sampleRate);
                    // 有了采样率，可以初始化定标数据处理器
                    calibrateDataProcessor = new EcgCalibrateDataProcessor(sampleRate, this);

                    ecgDataProcessor.setCalibrateDataProcessor(calibrateDataProcessor);
                }
                break;

            // 接收到导联类型
            case MSG_LEADTYPE_OBTAINED:
                if(msg.obj != null) {
                    Number num = (Number)msg.obj;

                    leadType = EcgLeadType.getFromCode(num.intValue());

                    updateLeadType(leadType);
                }
                break;

            // 启动采集ECG信号
            case MSG_START_SAMPLINGSIGNAL:
                ecgDataProcessor.clearData();

                setState(EcgMonitorState.SAMPLE);

                break;

            case MSG_BATTERY_OBTAINED:
                if(msg.obj != null) {
                    updateBattery(((Number)msg.obj).intValue());
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void open() {
        super.open();

        signalProcessor = null;

        ecgDataProcessor.setSignalProcessor(signalProcessor);

        signalRecorder = null;

        calibrateDataProcessor = null;

        ecgDataProcessor.setCalibrateDataProcessor(null);

        ecgFile = null;
    }

    // 关闭设备
    @Override
    public void close() {
        super.close();

        if(sigProcessThread != null) {
            sigProcessThread.interrupt();
        }

        // 关闭记录器
        if(signalRecorder != null) {
            signalRecorder.close();
        }

        // 关闭处理器
        if(signalProcessor != null) {
            signalProcessor.close();
        }

        if(calibrateDataProcessor != null)
            calibrateDataProcessor.close();

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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        signalProcessor = null;

        ecgDataProcessor.setSignalProcessor(null);

        signalRecorder = null;

        calibrateDataProcessor = null;

        ecgDataProcessor.setCalibrateDataProcessor(null);

        ecgFile = null;
    }

    private void saveEcgFileTail() throws IOException{
        ecgFile.setHrList(signalProcessor.getHrList());

        ecgFile.addComment(signalRecorder.getComment());

        ecgFile.save();
    }

    @Override
    protected void disconnect() {
        ViseLog.e("EcgMonitorDevice disconnect()");

        if(isMeasureBattery) {
            stopBatteryMeasuring();

            isMeasureBattery = false;
        }

        if(isConnected() && isGattExecutorAlive()) {
            stopDataSampling();
        }

        post(new Runnable() {
            @Override
            public void run() {
                EcgMonitorDevice.super.disconnect();
            }
        });
    }

    @Override
    public void onEcgSignalUpdated(final int ecgSignal) {
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
            /*post(new Runnable() {
                @Override
                public void run() {
                    listener.onEcgSignalChanged(ecgSignal);
                }
            });*/
            listener.onEcgSignalChanged(ecgSignal);
        }
    }

    @Override
    public void onEcgHrValueUpdated(final short hr) {
        if(listener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    listener.onEcgHrChanged(hr);
                }
            });
        }
    }

    @Override
    public void onEcgHrInfoUpdated(final EcgHrInfoObject hrInfoObject) {
        if(listener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    listener.onEcgHrInfoUpdated(hrInfoObject);
                }
            });
        }
    }

    @Override
    public void onNotifyEcgHrAbnormal() {
        if(listener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    listener.onNotifyHrAbnormal();
                }
            });
        }
    }

    @Override
    public void onUpdateSignalSecNum(final int second) {
        if(listener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    listener.onSignalSecNumChanged(second);
                }
            });
        }
    }

    @Override
    public void onUpdateCalibrateValue(int calibrateValue) {
        ViseLog.e("The Calibration Value is: " + calibrateValue);

        value1mVBeforeCalibrate = calibrateValue;

        updateCalibrationValue(value1mVBeforeCalibrate, value1mVAfterCalibrate);

        // 重新创建Ecg信号处理器
        createEcgSignalProcessor(); // 这里每次连接都会重新创建处理器，有问题。

        // 创建心电记录文件
        if(ecgFile == null) {
            try {
                ecgFile = EcgFile.create(sampleRate, value1mVAfterCalibrate, getMacAddress(), leadType);
            } catch (IOException e) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(), "无法记录心电信息", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // 创建心电信号记录仪
        if(ecgFile != null && signalRecorder == null) {
            signalRecorder = new EcgSignalRecorder(sampleRate, ecgFile, this);
        }

        // 初始化EcgView
        initializeEcgView(sampleRate, value1mVAfterCalibrate);

        setState(EcgMonitorState.CALIBRATED);

        stopDataSampling();

        startEcgSignalSampling();

        post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyApplication.getContext(), "设备连接成功，开始读取信号。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 创建心电信号处理器
    private void createEcgSignalProcessor() {
        EcgSignalProcessor.Builder builder = new EcgSignalProcessor.Builder();
        builder.setSampleRate(sampleRate);
        builder.setValue1mVCalibrate(value1mVBeforeCalibrate, value1mVAfterCalibrate);
        builder.setHrWarnEnabled(config.isWarnWhenHrAbnormal());
        builder.setHrWarnLimit(config.getHrLowLimit(), config.getHrHighLimit());
        builder.setEcgSignalProcessListener(this);

        EcgHrProcessor hrProcessor = null;

        if(signalProcessor != null)
            hrProcessor = signalProcessor.getHrProcessor();

        signalProcessor = builder.build();

        ecgDataProcessor.setSignalProcessor(signalProcessor);

        if(hrProcessor != null)
            signalProcessor.setHrProcessor(hrProcessor);
    }

    // 设置是否记录心电信号
    public synchronized void setEcgSignalRecord(boolean isRecord) {
        if(signalRecorder == null || signalRecorder.isRecord() == isRecord) return;

        // 当前isRecord与要设置的isRecord不同，就意味着要改变当前的isRecord状态
        signalRecorder.setRecord(isRecord);

        updateRecordStatus(isRecord);
    }

    // 转换当前状态
    public synchronized void switchSampleState() {
        switch(state) {
            case INIT:
                start1mVSampling();
                break;
            case CALIBRATED:
                stopDataSampling();
                startEcgSignalSampling();
                break;
            case SAMPLE:
                stopDataSampling();
                removeCallbacksAndMessages();
                setState(EcgMonitorState.CALIBRATED);
                break;
            default:
                break;
        }
    }

    // 添加留言内容
    public synchronized void addCommentContent(String content) {
        signalRecorder.addCommentContent(content);
    }

    // 登记心电监护仪观察者
    public void setEcgMonitorListener(OnEcgMonitorListener listener) {
        this.listener = listener;
    }

    // 删除心电监护仪观察者
    public void removeEcgMonitorListener() {
        listener = null;
    }


    /**
     * 私有函数
     */
    // 读采样率
    private void readSampleRate() {
        read(ECGMONITOR_SAMPLERATE, new IGattDataCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendMessage(MSG_SAMPLERATE_OBTAINED, (data[0] & 0xff) | ((data[1] << 8) & 0xff00));
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
                sendMessage(MSG_LEADTYPE_OBTAINED, data[0]);
            }

            @Override
            public void onFailure(GattDataException exception) {

            }
        });
    }

    // 启动ECG信号采集
    private void startEcgSignalSampling() {
        IGattDataCallback indicationCallback = new IGattDataCallback() {
            @Override
            public void onSuccess(final byte[] data) {
                    final byte[] arr = Arrays.copyOf(data, data.length);

                    receiveService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ecgDataProcessor.resolveDataPacket(arr);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            }

            @Override
            public void onFailure(GattDataException exception) {

            }
        };
        // enable ECG data indication
        indicate(ECGMONITOR_DATA_CCC, true, indicationCallback);

        write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_STARTSIGNAL, new IGattDataCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendMessage(MSG_START_SAMPLINGSIGNAL, null);
            }

            @Override
            public void onFailure(GattDataException exception) {

            }
        });
    }

    // 启动1mV定标信号采集
    private void start1mVSampling() {
        setState(EcgMonitorState.CALIBRATING);

        IGattDataCallback indicationCallback = new IGattDataCallback() {
            @Override
            public void onSuccess(final byte[] data) {
                receiveService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ecgDataProcessor.resolveDataPacket(data);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onFailure(GattDataException exception) {

            }
        };
        // enable ECG data indication
        indicate(ECGMONITOR_DATA_CCC, true, indicationCallback);

        write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_START1MV, null);
    }

    // 停止数据采集
    private void stopDataSampling() {
        // disable ECG data indication
        indicate(ECGMONITOR_DATA_CCC, false, null);

        write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_STOP, null);
    }

    // 开始电池电量测量
    private void startBatteryMeasuring() {
        readBatteryService = Executors.newSingleThreadScheduledExecutor();

        readBatteryService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                read(BATTERY_DATA, new IGattDataCallback() {
                    @Override
                    public void onSuccess(byte[] data) {
                        int battery = data[0];
                        sendMessage(MSG_BATTERY_OBTAINED, battery);
                    }

                    @Override
                    public void onFailure(GattDataException exception) {

                    }
                });
            }
        }, 0, 10000L, TimeUnit.MILLISECONDS);

    }

    // 停止电池电量测量
    private void stopBatteryMeasuring() {
        if(readBatteryService != null) {
            readBatteryService.shutdownNow();

            try {
                readBatteryService.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 解析数据包
    /*private void resolveDataPacket(byte[] data) {
        synchronized (dataBuff) {
            // 单片机发过来的是LITTLE_ENDIAN的short数据
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

            // 单片机发过来的int是两个字节的short
            for (int i = 0; i < data.length / 2; i++) {
                dataBuff.offer((int) buffer.getShort());
            }
        }
    }*/

    // 初始化EcgView
    private void initializeEcgView(int sampleRate, int calibrationValue) {
        //pixelPerGrid = DEFAULT_PIXEL_PER_GRID;                   // 每小格的像素个数
        // 计算EcgView分辨率
        xPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_GRID * sampleRate)); // 计算横向分辨率

        yValuePerPixel = calibrationValue * DEFAULT_MV_PER_GRID / pixelPerGrid; // 计算纵向分辨率
        // 更新EcgView
        updateEcgView(xPixelPerData, yValuePerPixel, pixelPerGrid);
    }

    private void updateEcgMonitorState() {
        post(new Runnable() {
            @Override
            public void run() {
                if(listener != null)
                    listener.onDeviceStateUpdated(state);
            }
        });
    }

    private void updateSampleRate(final int sampleRate) {
        post(new Runnable() {
            @Override
            public void run() {
                if(listener != null)
                    listener.onSampleRateChanged(sampleRate);
            }
        });
    }

    private void updateLeadType(final EcgLeadType leadType) {
        post(new Runnable() {
            @Override
            public void run() {
                if(listener != null)
                    listener.onLeadTypeChanged(leadType);
            }
        });
    }

    private void updateCalibrationValue(final int calibrationValueBefore, final int calibrationValueAfter) {
        post(new Runnable() {
            @Override
            public void run() {
                if(listener != null)
                    listener.onCalibrationValueChanged(calibrationValueBefore, calibrationValueAfter);
            }
        });
    }

    private void updateRecordStatus(final boolean isRecord) {
        post(new Runnable() {
            @Override
            public void run() {
                if(listener != null)
                    listener.onSignalRecordStateUpdated(isRecord);
            }
        });
    }

    private void updateEcgView(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        post(new Runnable() {
            @Override
            public void run() {
                if(listener != null)
                    listener.onEcgViewUpdated(xPixelPerData, yValuePerPixel, gridPixels);
            }
        });
    }

    private void updateBattery(final int bat) {
        setBattery(bat);

        if(listener != null)
            listener.onBatteryChanged(bat);
    }

}

package com.cmtech.android.bledevice.ecgmonitor.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.bledevice.core.BleDataOpException;
import com.cmtech.android.bledevice.core.BleGattElement;
import com.cmtech.android.bledevice.core.IBleDataOpCallback;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.CalibrateDataProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ICalibrateValueObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.IEcgSignalObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.IEcgHrAbnormalObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.IEcgHrValueObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgrecord.EcgRecorder;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgrecord.IEcgRecordObserver;
import com.cmtech.android.bledevice.core.BleDevice;
import com.cmtech.android.bledevice.core.BleDeviceBasicInfo;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.CCCUUID;
import static com.cmtech.android.bledevice.core.BleDeviceConstant.MY_BASE_UUID;


/**
 * EcgMonitorDevice: 心电带设备类
 * Created by bme on 2018/9/20.
 */

public class EcgMonitorDevice extends BleDevice implements IEcgSignalObserver, IEcgHrValueObserver, IEcgHrAbnormalObserver, IEcgRecordObserver, ICalibrateValueObserver {
    private final static String TAG = "EcgMonitorDevice";

    // 常量
    private static final int DEFAULT_SAMPLERATE = 125;                          // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEADTYPE = EcgLeadType.LEAD_I;     // 缺省导联为L1
    private static final int DEFAULT_CALIBRATIONVALUE = 65536;                  // 缺省1mV定标值
    private static final float DEFAULT_SECOND_PER_GRID = 0.04f;                 // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f;                      // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10;                       // 缺省每个栅格包含的像素个数

    // GATT消息常量
    private static final int MSG_OBTAINSAMPLERATE = 1;                          // 获取采样率
    private static final int MSG_OBTAINLEADTYPE = 2;                            // 获取导联类型
    private static final int MSG_STARTSAMPLINGSIGNAL = 3;                       // 开始采集Ecg信号

    // 心电监护仪Service UUID常量
    private static final String ecgMonitorServiceUuid       = "aa40";           // 心电监护仪服务UUID:aa40
    private static final String ecgMonitorDataUuid          = "aa41";           // ECG数据特征UUID:aa41
    private static final String ecgMonitorCtrlUuid          = "aa42";           // 测量控制UUID:aa42
    private static final String ecgMonitorSampleRateUuid    = "aa44";           // 采样率UUID:aa44
    private static final String ecgMonitorLeadTypeUuid      = "aa45";           // 导联类型UUID:aa45

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

    // ECGMONITOR_CTRL Element的控制常量
    private static final byte ECGMONITOR_CTRL_STOP =             (byte) 0x00;        // 停止采集
    private static final byte ECGMONITOR_CTRL_STARTSIGNAL =      (byte) 0x01;        // 启动采集Ecg信号
    private static final byte ECGMONITOR_CTRL_START1MV =         (byte) 0x02;        // 启动采集1mV定标


    /**
     * 变量
     */
    private int sampleRate = DEFAULT_SAMPLERATE; // 采样率
    private EcgLeadType leadType = DEFAULT_LEADTYPE; // 导联类型
    private int value1mVBeforeCalibrate = 0; // 定标之前1mV对应的数值
    private final int value1mVAfterCalibrate = DEFAULT_CALIBRATIONVALUE; // 定标之后1mV对应的数值
    private boolean isRecord = false; // 是否记录信号
    private boolean isFilter = true; // 是否对信号进行滤波
    private final int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // EcgView中每小格的像素个数
    private int xPixelPerData = 2; // EcgView的横向分辨率
    private float yValuePerPixel = 100.0f; // EcgView的纵向分辨率
    private EcgMonitorState state = EcgMonitorState.INIT; // 设备状态
    private final EcgMonitorDeviceConfig config; // 设备配置信息
    private IEcgMonitorObserver observer; // 心电设备观察者

    private EcgRecorder ecgRecorder; // 心电记录器
    private EcgSignalProcessor ecgProcessor; // 心电处理器
    private CalibrateDataProcessor caliDataProcessor; // 定标数据处理器

    private final LinkedBlockingQueue<Integer> dataBuff = new LinkedBlockingQueue<Integer>();	//数据缓存
    private final Runnable processRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    int value = dataBuff.take();
                    //ViseLog.i("Process Data in Thread: " + Thread.currentThread());
                    switch (state) {
                        case CALIBRATING:
                            caliDataProcessor.process(value);
                            break;
                        case SAMPLE:
                            ecgProcessor.process(value);
                            break;
                        default:
                            break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                dataBuff.clear();
            }
        }
    };
    // 数据处理线程
    private Thread processThread;


    public int getSampleRate() { return sampleRate; }
    public EcgLeadType getLeadType() {
        return leadType;
    }
    public int getValue1mVBeforeCalibrate() { return value1mVBeforeCalibrate; }
    public int getValue1mVAfterCalibrate() { return value1mVAfterCalibrate; }
    public boolean isRecord() {return isRecord;}
    public boolean isFilter() {return isFilter;}
    public synchronized void setFilter(boolean isFilter) {
        this.isFilter = isFilter;
    } // 加载Ecg滤波器
    public int getPixelPerGrid() { return pixelPerGrid; }
    public int getxPixelPerData() { return xPixelPerData; }
    public float getyValuePerPixel() { return yValuePerPixel; }
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
        if(ecgProcessor != null) {
            ecgProcessor.changeConfiguration(config);
            if (config.isWarnWhenHrAbnormal())
                ecgProcessor.registerHrAbnormalObserver(this);
        }
    }
    public int getRecordSecond() {
        return (ecgRecorder == null) ? 0 : ecgRecorder.getRecordSecond();
    }

    // 构造器
    public EcgMonitorDevice(BleDeviceBasicInfo basicInfo) {
        super(basicInfo);

        // 从数据库获取设备的配置信息
        List<EcgMonitorDeviceConfig> find = LitePal.where("macAddress = ?", basicInfo.getMacAddress()).find(EcgMonitorDeviceConfig.class);
        if(find == null || find.size() == 0) {
            config = new EcgMonitorDeviceConfig();
            config.setMacAddress(basicInfo.getMacAddress());
            config.save();
        } else {
            config = find.get(0);
        }

    }

    @Override
    public boolean executeAfterConnectSuccess() {
        updateSampleRate(DEFAULT_SAMPLERATE);
        updateLeadType(DEFAULT_LEADTYPE);
        updateCalibrationValue(value1mVBeforeCalibrate, value1mVAfterCalibrate);

        // 启动gattOperator
        gattOperator.start();
        // 验证Gatt Elements
        BleGattElement[] elements = new BleGattElement[]{ECGMONITOR_DATA, ECGMONITOR_DATA_CCC, ECGMONITOR_CTRL, ECGMONITOR_SAMPLERATE, ECGMONITOR_LEADTYPE};
        if(!gattOperator.checkElements(elements)) {
            return false;
        }
        // 先停止采样
        stopSampleData();
        // 读采样率
        readSampleRate();
        // 读导联类型
        readLeadType();

        processThread = new Thread(processRunnable);
        processThread.start();

        // 启动1mV采样进行定标
        startSample1mV();
        return true;
    }

    @Override
    public void executeAfterDisconnect() {
        gattOperator.stop();
        if(processThread != null) {
            processThread.interrupt();
        }
    }

    @Override
    public void executeAfterConnectFailure() {
        gattOperator.stop();
        if(processThread != null) {
            processThread.interrupt();
        }
    }

    @Override
    public synchronized void processGattMessage(Message msg)
    {
        switch (msg.what) {
            // 接收到采样率数据
            case MSG_OBTAINSAMPLERATE:
                if(msg.obj != null) {
                    sampleRate = (Integer) msg.obj;
                    updateSampleRate(sampleRate);
                    // 有了采样率，可以初始化定标数据处理器
                    caliDataProcessor = new CalibrateDataProcessor(sampleRate);
                    caliDataProcessor.registerObserver(this);
                }
                break;

            // 接收到导联类型数据
            case MSG_OBTAINLEADTYPE:
                if(msg.obj != null) {
                    Number num = (Number)msg.obj;
                    leadType = EcgLeadType.getFromCode(num.intValue());
                    updateLeadType(leadType);
                }
                break;

            // 启动采集ECG信号
            case MSG_STARTSAMPLINGSIGNAL:
                dataBuff.clear();
                setState(EcgMonitorState.SAMPLE);
                break;

            default:
                break;
        }
    }

    @Override
    public void open() {
        super.open();

        ecgRecorder = new EcgRecorder();
        ecgRecorder.registerObserver(this);
    }

    // 关闭设备
    @Override
    public void close() {
        super.close();

        if(processThread != null) {
            processThread.interrupt();
        }

        // 关闭记录器
        if(isRecord) {
            ecgRecorder.close();
            ecgRecorder.removeObserver();
            isRecord = false;
        }
        // 关闭处理器
        if(ecgProcessor != null)
            ecgProcessor.close();
    }

    @Override
    public void disconnect() {
        ViseLog.e(TAG, "disconnect()");
        if(isConnected()) {
            stopSampleData();
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                EcgMonitorDevice.super.disconnect();
            }
        }, 500);
    }

    @Override
    public void updateEcgSignal(final int ecgSignal) {
        // 记录
        if(isRecord) {
            ecgRecorder.record(ecgSignal);
        }

        // 通知观察者
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateEcgSignal(ecgSignal);
            }
        });
    }

    @Override
    public void updateHrValue(final int hr) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateEcgHr(hr);
            }
        });
    }

    @Override
    public void notifyHrAbnormal() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.notifyHrAbnormal();
            }
        });
    }

    @Override
    public void updateRecordSecond(final int second) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateRecordSecond(second);
            }
        });
    }

    @Override
    public void updateCalibrateValue(int calibrateValue) {
        value1mVBeforeCalibrate = calibrateValue;
        updateCalibrationValue(value1mVBeforeCalibrate, value1mVAfterCalibrate);

        // 创建Ecg处理器
        createEcgProcessor();

        // 创建记录
        if(isRecord) {
            ecgRecorder.create(sampleRate, value1mVAfterCalibrate, leadType, getMacAddress());
        }

        // 初始化EcgView
        initializeEcgView(sampleRate, value1mVAfterCalibrate);

        setState(EcgMonitorState.CALIBRATED);
        stopSampleData();
        startSampleEcg();
    }

    // 设置是否记录心电信号
    public synchronized void setRecord(boolean isRecord) {
        if(this.isRecord == isRecord) return;

        // 当前isRecord与要设置的isRecord不同，就意味着要改变当前的isRecord状态
        this.isRecord = isRecord;
        if(this.isRecord) {
            // 启动记录：如果已经标定了或者采样了,才可以创建新的记录，否则需要等到那时才创建记录器
            if(state == EcgMonitorState.CALIBRATED || state == EcgMonitorState.SAMPLE) {
                ecgRecorder.create(sampleRate, value1mVAfterCalibrate, leadType, getMacAddress());
            }
        } else {
            // 停止记录：保存当前记录
            ecgRecorder.save();
        }

        updateRecordStatus(isRecord);
    }

    // 转换当前状态
    public synchronized void switchSampleState() {
        switch(state) {
            case INIT:
                startSample1mV();
                break;
            case CALIBRATED:
                stopSampleData();
                startSampleEcg();
                break;
            case SAMPLE:
                stopSampleData();
                removeCallbacksAndMessages();
                setState(EcgMonitorState.CALIBRATED);
                break;
            default:
                break;
        }
    }

    // 添加有时间定位的留言
    public synchronized void addComment(int secondInEcg, String comment) {
        ecgRecorder.addComment(secondInEcg, comment);
    }

    // 获取统计直方图数据
    public int[] getHrStatistics() {
        return (ecgProcessor == null) ? null : ecgProcessor.getHistogramData();
    }

    // 重置统计直方图数据
    public void resetHrStatistics() {
        if(ecgProcessor != null)
            ecgProcessor.resetHrHistogram();
    }

    // 登记心电监护仪观察者
    public void registerEcgMonitorObserver(IEcgMonitorObserver observer) {
        this.observer = observer;
    }
    // 删除心电监护仪观察者
    public void removeEcgMonitorObserver() {
        observer = null;
    }


    /**
     * 私有函数
     */
    // 读采样率
    private void readSampleRate() {
        gattOperator.read(ECGMONITOR_SAMPLERATE, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_OBTAINSAMPLERATE, (data[0] & 0xff) | ((data[1] << 8) & 0xff00));
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });
    }

    // 读导联类型
    private void readLeadType() {
        gattOperator.read(ECGMONITOR_LEADTYPE, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_OBTAINLEADTYPE, data[0]);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });
    }

    // 启动ECG信号采集
    private void startSampleEcg() {
        IBleDataOpCallback indicationCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                resolveDataPacket(data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };
        // enable ECG data indication
        gattOperator.indicate(ECGMONITOR_DATA_CCC, true, indicationCallback);
        gattOperator.write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_STARTSIGNAL, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_STARTSAMPLINGSIGNAL, null);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });
    }

    // 启动1mV定标信号采集
    private void startSample1mV() {
        setState(EcgMonitorState.CALIBRATING);
        IBleDataOpCallback indicationCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                resolveDataPacket(data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };
        // enable ECG data indication
        gattOperator.indicate(ECGMONITOR_DATA_CCC, true, indicationCallback);
        gattOperator.write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_START1MV, null);
    }

    // 停止数据采集
    private void stopSampleData() {
        // disable ECG data indication
        gattOperator.indicate(ECGMONITOR_DATA_CCC, false, null);
        gattOperator.write(ECGMONITOR_CTRL, ECGMONITOR_CTRL_STOP, null);
    }

    // 解析数据包
    private void resolveDataPacket(byte[] data) {
        synchronized (dataBuff) {
            // 单片机发过来的是LITTLE_ENDIAN的数据
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            // 单片机发过来的int是两个字节的short
            for (int i = 0; i < data.length / 2; i++) {
                dataBuff.offer((int) buffer.getShort());
            }
        }
    }

    // 创建心电处理器
    private void createEcgProcessor() {
        EcgSignalProcessor.Builder builder = new EcgSignalProcessor.Builder();
        builder.setSampleRate(sampleRate);
        builder.setValue1mVCalibrate(value1mVBeforeCalibrate, value1mVAfterCalibrate);
        builder.setHrWarnEnabled(config.isWarnWhenHrAbnormal());
        builder.setHrWarnLimit(config.getHrLowLimit(), config.getHrHighLimit());
        ecgProcessor = builder.build();
        ecgProcessor.registerSignalObserver(this);
        ecgProcessor.registerHrValueObserver(this);
        ecgProcessor.registerHrAbnormalObserver(this);
    }

    // 初始化EcgView
    private void initializeEcgView(int sampleRate, int calibrationValue) {
        //pixelPerGrid = DEFAULT_PIXEL_PER_GRID;                   // 每小格的像素个数
        // 计算EcgView分辨率
        xPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_GRID * sampleRate));                       // 计算横向分辨率
        yValuePerPixel = calibrationValue * DEFAULT_MV_PER_GRID / pixelPerGrid;                         // 计算纵向分辨率
        // 更新EcgView
        updateEcgView(xPixelPerData, yValuePerPixel, pixelPerGrid);
    }

    private void updateEcgMonitorState() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateState(state);
            }
        });
    }

    private void updateSampleRate(final int sampleRate) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateSampleRate(sampleRate);
            }
        });
    }

    private void updateLeadType(final EcgLeadType leadType) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateLeadType(leadType);
            }
        });
    }

    private void updateCalibrationValue(final int calibrationValueBefore, final int calibrationValueAfter) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateCalibrationValue(calibrationValueBefore, calibrationValueAfter);
            }
        });
    }

    private void updateRecordStatus(final boolean isRecord) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateRecordStatus(isRecord);
            }
        });
    }

    private void updateEcgView(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateEcgView(xPixelPerData, yValuePerPixel, gridPixels);
            }
        });
    }



}

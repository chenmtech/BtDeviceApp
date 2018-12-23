package com.cmtech.android.bledevice.ecgmonitor.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.bledevice.core.BleDataOpException;
import com.cmtech.android.bledevice.core.BleGattElement;
import com.cmtech.android.bledevice.core.IBleDataOpCallback;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgfilter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgfilter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.EcgHrHistogram;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.EcgHrWarner;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.android.bledevice.core.BleDevice;
import com.cmtech.android.bledevice.core.BleDeviceBasicInfo;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.CCCUUID;
import static com.cmtech.android.bledevice.core.BleDeviceConstant.MY_BASE_UUID;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECGFILEDIR;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.IEcgHrProcessor.INVALID_HR;


/**
 * EcgMonitorDevice: 心电带设备类
 * Created by bme on 2018/9/20.
 */

public class EcgMonitorDevice extends BleDevice {
    private final static String TAG = "EcgMonitorDevice";

    // 常量
    private static final int DEFAULT_SAMPLERATE = 125;                          // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEADTYPE = EcgLeadType.LEAD_I;     // 缺省导联为L1
    private static final int DEFAULT_CALIBRATIONVALUE = 65536;                  // 缺省1mV定标值
    private static final float DEFAULT_SECOND_PER_GRID = 0.04f;                 // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f;                      // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10;                       // 缺省每个栅格包含的像素个数

    // GATT消息常量
    private static final int MSG_OBTAINDATA = 1;                                // 获取一个ECG数据包，可以是1mV定标数据，也可以是Ecg信号
    private static final int MSG_OBTAINSAMPLERATE = 2;                          // 获取采样率
    private static final int MSG_OBTAINLEADTYPE = 3;                            // 获取导联类型
    private static final int MSG_STARTSAMPLINGSIGNAL = 4;                       // 开始采集Ecg信号

    // 心电监护仪Service UUID常量
    private static final String ecgMonitorServiceUuid       = "aa40";           // 心电监护仪服务UUID:aa40
    private static final String ecgMonitorDataUuid          = "aa41";           // ECG数据特征UUID:aa41
    private static final String ecgMonitorCtrlUuid          = "aa42";           // 测量控制UUID:aa42
    private static final String ecgMonitorSampleRateUuid    = "aa44";           // 采样率UUID:aa44
    private static final String ecgMonitorLeadTypeUuid      = "aa45";           // 导联类型UUID:aa45

    // Gatt Element常量
    private static final BleGattElement ECGMONITORDATA =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, null, MY_BASE_UUID, "心电数据");
    private static final BleGattElement ECGMONITORDATACCC =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, CCCUUID, MY_BASE_UUID, "心电数据CCC");
    private static final BleGattElement ECGMONITORCTRL =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorCtrlUuid, null, MY_BASE_UUID, "心电Ctrl");
    private static final BleGattElement ECGMONITORSAMPLERATE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorSampleRateUuid, null, MY_BASE_UUID, "采样率");
    private static final BleGattElement ECGMONITORLEADTYPE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorLeadTypeUuid, null, MY_BASE_UUID, "导联类型");

    // ECGMONITORCTRL控制常量
    private static final byte ECGMONITORCTRL_STOP =             (byte) 0x00;        // 停止采集
    private static final byte ECGMONITORCTRL_STARTSIGNAL =      (byte) 0x01;        // 启动采集Ecg信号
    private static final byte ECGMONITORCTRL_START1MV =         (byte) 0x02;        // 启动采集1mV定标


    /**
     * 变量
     */
    private int sampleRate = DEFAULT_SAMPLERATE; // 采样率
    private EcgLeadType leadType = DEFAULT_LEADTYPE; // 导联类型
    private int value1mVBeforeCalibrate = 0; // 定标之前1mV对应的数值
    private final int value1mVAfterCalibrate = DEFAULT_CALIBRATIONVALUE; // 定标之后1mV对应的数值
    private final List<Integer> calibrationData = new ArrayList<>(250); // 用于保存标定用的数据
    private int currentHr = 0; // 当前心率值
    private boolean isRecord = false; // 是否记录信号
    private boolean isFilter = true; // 是否对信号进行滤波
    private final List<EcgComment> commentList = new ArrayList<>(); // 当前信号的留言表
    private long recordDataNum = 0; // 当前记录的心电数据个数
    private final int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // EcgView中每小格的像素个数
    private int xPixelPerData = 2; // EcgView的横向分辨率
    private float yValuePerPixel = 100.0f; // EcgView的纵向分辨率
    private EcgFile ecgFile = null; // 用于记录心电信号的EcgFile文件
    private IEcgCalibrator ecgCalibrator; // Ecg信号定标器
    private IEcgFilter ecgFilter; // Ecg信号滤波器
    private QrsDetector qrsDetector; // Ecg Qrs波检测器，可用于获取心率
    private EcgHrWarner hrWarner; // Ecg心率报警器
    private EcgHrHistogram hrHistogram; // Ecg心率直方图
    private EcgMonitorState state = EcgMonitorState.INIT; // 设备状态
    private final EcgMonitorDeviceConfig config; // 设备配置信息
    private IEcgMonitorObserver observer; // 设备观察者




    public int getSampleRate() { return sampleRate; }
    public EcgLeadType getLeadType() {
        return leadType;
    }
    public int getValue1mVBeforeCalibrate() { return value1mVBeforeCalibrate; }
    public int getValue1mVAfterCalibrate() { return value1mVAfterCalibrate; }
    public int getCurrentHr() { return currentHr; }
    public boolean isRecord() {return isRecord;}
    public boolean isFilter() {return isFilter;}
    public synchronized void setFilter(boolean isFilter) {
        this.isFilter = isFilter;
    } // 加载Ecg滤波器
    public int getPixelPerGrid() { return pixelPerGrid; }
    public int getxPixelPerData() { return xPixelPerData; }
    public float getyValuePerPixel() { return yValuePerPixel; }
    public int getRecordSecond() {
        return (int)(recordDataNum/sampleRate);
    } // 获取记录的时间，单位为秒
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
        changeConfiguration(config);
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
        BleGattElement[] elements = new BleGattElement[]{ECGMONITORDATA, ECGMONITORDATACCC, ECGMONITORCTRL, ECGMONITORSAMPLERATE, ECGMONITORLEADTYPE};
        if(!gattOperator.checkElements(elements)) {
            return false;
        }

        // 先停止采样
        stopSampleData();

        // 读采样率
        readSampleRate();

        // 读导联类型
        readLeadType();

        // 启动1mV采样，准备标定
        startSample1mV();

        return true;
    }

    @Override
    public void executeAfterDisconnect() {
        gattOperator.stop();
    }

    @Override
    public void executeAfterConnectFailure() {
        gattOperator.stop();
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

            // 接收到Ecg数据包：Ecg信号或者定标数据
            case MSG_OBTAINDATA:
                if(msg.obj != null) {
                    processData((byte[]) msg.obj);
                }
                break;

            // 启动采集ECG信号
            case MSG_STARTSAMPLINGSIGNAL:
                setState(EcgMonitorState.SAMPLE);
                break;

            default:
                break;
        }
    }

    // 关闭设备
    @Override
    public void close() {
        super.close();

        // 清楚hr直方图
        if(hrHistogram != null)
            hrHistogram.reset();

        // 保存EcgFile
        if(isRecord) {
            saveEcgFile();
            isRecord = false;
        }
    }

    @Override
    public void disconnect() {
        ViseLog.e(TAG, "disconnect()");
        stopSampleData();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                EcgMonitorDevice.super.disconnect();
            }
        }, 500);
    }

    // 设置是否记录心电信号
    public synchronized void setRecord(boolean isRecord) {
        if(this.isRecord == isRecord) return;

        // 当前isRecord与要设置的isRecord不同，就意味着要改变当前的isRecord状态
        if(this.isRecord) {
            saveEcgFile();              // 保存Ecg文件
        }

        this.isRecord = isRecord;

        if(this.isRecord) {
            // 如果已经标定了或者采样了,才可以开始记录心电信号，初始化EcgFile
            if(state == EcgMonitorState.CALIBRATED || state == EcgMonitorState.SAMPLE)
                initializeEcgFile(sampleRate, value1mVAfterCalibrate, leadType);
            else {
                // 否则什么都不做，会在标定后根据isRecord值初始化Ecg文件
            }
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

    // 添加没有时间定位的留言
    public synchronized void addComment(String comment) {
        long timeCreated = new Date().getTime();
        commentList.add(new EcgComment(UserAccountManager.getInstance().getUserAccount().getUserName(), timeCreated, comment));
    }

    // 添加有时间定位的留言
    public synchronized void addComment(int secondInEcg, String comment) {
        long timeCreated = new Date().getTime();
        commentList.add(new EcgComment(UserAccountManager.getInstance().getUserAccount().getUserName(), timeCreated, secondInEcg, comment));
    }

    // 获取统计直方图数据
    public int[] getHrStatistics() {
        return (hrHistogram == null) ? null : hrHistogram.getHistgram();
    }

    // 重置统计直方图数据
    public void resetHrStatistics() {
        if(hrHistogram != null) {
            hrHistogram.reset();
        }
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
    // 修改配置信息
    private void changeConfiguration(EcgMonitorDeviceConfig config) {
        if(config.isWarnWhenHrAbnormal()) {
            if(hrWarner != null) {
                hrWarner.setHrWarn(config.getHrLowLimit(), config.getHrHighLimit());
            } else {
                hrWarner = new EcgHrWarner(config.getHrLowLimit(), config.getHrHighLimit());
            }
        } else {
            hrWarner = null;
        }
    }

    // 读采样率
    private void readSampleRate() {
        gattOperator.read(ECGMONITORSAMPLERATE, new IBleDataOpCallback() {
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
        gattOperator.read(ECGMONITORLEADTYPE, new IBleDataOpCallback() {
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
                sendGattMessage(MSG_OBTAINDATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };

        // enable ECG data indication
        gattOperator.indicate(ECGMONITORDATACCC, true, null, indicationCallback);

        gattOperator.write(ECGMONITORCTRL, ECGMONITORCTRL_STARTSIGNAL, new IBleDataOpCallback() {
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
        calibrationData.clear();
        IBleDataOpCallback indicationCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_OBTAINDATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };

        // enable ECG data indication
        gattOperator.indicate(ECGMONITORDATACCC, true, null, indicationCallback);

        gattOperator.write(ECGMONITORCTRL, ECGMONITORCTRL_START1MV, null);
    }

    // 停止数据采集
    private void stopSampleData() {
        // disable ECG data indication
        gattOperator.indicate(ECGMONITORDATACCC, false, null, null);

        gattOperator.write(ECGMONITORCTRL, ECGMONITORCTRL_STOP, null);
    }

    // 处理数据
    private void processData(byte[] data) {
        ViseLog.i("Process Data in Thread: " +  Thread.currentThread());
        // 单片机发过来的是LITTLE_ENDIAN的数据
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        // 单片机发过来的int是两个字节的short
        for(int i = 0; i < data.length/2; i++) {
            switch (state) {
                case CALIBRATING:
                    processCalibrateData(buffer.getShort());
                    break;
                case SAMPLE:
                    processEcgSignal(buffer.getShort());
                    break;
                default:
                    break;
            }
        }
    }

    // 处理标定数据
    private void processCalibrateData(int tmpData) {
        // 采集1个周期的定标信号
        if (calibrationData.size() < sampleRate) {
            calibrationData.add(tmpData);
        }
        else {
            // 计算得到实际1mV定标值
            value1mVBeforeCalibrate = calculateCalibration(calibrationData);
            calibrationData.clear();

            // 初始化各种Ecg处理器
            initializeProcessor();

            setState(EcgMonitorState.CALIBRATED);
            stopSampleData();
            startSampleEcg();
        }
    }

    // 处理Ecg信号
    private void processEcgSignal(int ecgSignal) {
        // 标定后滤波处理
        ecgSignal = (int) ecgFilter.filter(ecgCalibrator.process(ecgSignal));

        // 保存到EcgFile
        if(isRecord) {
            try {
                ecgFile.writeData(ecgSignal);
                recordDataNum++;
                updateRecordSecond(getRecordSecond());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 显示心电信号
        updateEcgSignal(ecgSignal);

        // 检测Qrs波，获取心率
        currentHr = qrsDetector.outputHR(ecgSignal);

        if(currentHr != INVALID_HR) {
            // 更新心率值显示
            updateEcgHr(currentHr);

            // 处理心率值
            if(hrWarner != null) {
                hrWarner.process(currentHr);
                // 如果心率异常
                if(hrWarner.isAbnormal()) {
                    notifyHrAbnormal();
                }
            }

            if(hrHistogram != null) {
                hrHistogram.process(currentHr);
            }
        }
    }

    private int calculateCalibration(List<Integer> data) {
        Integer[] arr = data.toArray(new Integer[0]);
        Arrays.sort(arr);

        int len = (arr.length-10)/2;
        int sum1 = 0;
        int sum2 = 0;
        for(int i = 0; i < len; i++) {
            sum1 += arr[i];
            sum2 += arr[arr.length-i-1];
        }
        return (sum2-sum1)/2/len;
    }

    // 初始化各种Ecg处理器
    private void initializeProcessor() {
        // 初始化定标器
        initializeCalibrator(value1mVBeforeCalibrate, value1mVAfterCalibrate);

        // 初始化滤波器
        initializeFilter(sampleRate);

        // 初始化Ecg记录器
        if(isRecord) {
            initializeEcgFile(sampleRate, value1mVAfterCalibrate, leadType);        // 如果需要记录，就初始化Ecg文件
        }

        // 初始化Qrs波检测器
        initializeQrsDetector(sampleRate, value1mVAfterCalibrate);

        // 初始化心率处理器
        initializeHrProcessor();

        // 初始化EcgView
        initializeEcgView(sampleRate, value1mVAfterCalibrate);
    }

    // 初始化定标器
    private void initializeCalibrator(int value1mVBeforeCalibrate, int value1mVAfterCalibrate) {
        if(value1mVAfterCalibrate != DEFAULT_CALIBRATIONVALUE) {
            return;
        }
        ecgCalibrator = new EcgCalibrator65536(value1mVBeforeCalibrate);       // 初始化定标器
        updateCalibrationValue(value1mVBeforeCalibrate, value1mVAfterCalibrate);
    }

    // 初始化EcgFile
    private void initializeEcgFile(int sampleRate, int calibrationValue, EcgLeadType leadType) {
        if(ecgFile != null) return;

        ecgFile = EcgFile.create(sampleRate, calibrationValue, getMacAddress(), leadType);
        if(ecgFile != null) {
            commentList.clear();
            recordDataNum = 0;
            updateRecordSecond(0);
        }
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

    // 初始化滤波器
    private void initializeFilter(int sampleRate) {
        ecgFilter = new EcgPreFilterWith35HzNotch(sampleRate);
    }

    // 初始化Qrs波检测器
    private void initializeQrsDetector(int sampleRate, int calibrationValue) {
        qrsDetector = new QrsDetector(sampleRate, calibrationValue);
    }

    // 初始化心率处理器
    private void initializeHrProcessor() {
        if(hrHistogram == null) {
            hrHistogram = new EcgHrHistogram();
        }
        if(config.isWarnWhenHrAbnormal())
            hrWarner = new EcgHrWarner(config.getHrLowLimit(), config.getHrHighLimit());
    }

    // 保存Ecg文件
    private void saveEcgFile() {
        ViseLog.e(TAG + " " + getMacAddress() + ": saveEcgFile()");

        if (ecgFile != null) {
            try {
                if (ecgFile.getDataNum() <= 0) {     // 如果没有数据，删除文件
                    ecgFile.close();
                    FileUtil.deleteFile(ecgFile.getFile());
                } else {    // 如果有数据
                    if (!commentList.isEmpty()) {
                        ecgFile.addComments(commentList);
                    }
                    ecgFile.saveFileTail();
                    ecgFile.close();
                    ViseLog.e(ecgFile);
                    File toFile = FileUtil.getFile(ECGFILEDIR, ecgFile.getFile().getName());
                    // 将缓存区中的文件移动到ECGFILEDIR目录中
                    FileUtil.moveFile(ecgFile.getFile(), toFile);
                }
                ecgFile = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    private void updateEcgSignal(final int ecgSignal) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateEcgSignal(ecgSignal);
            }
        });
    }

    private void updateRecordSecond(final int second) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateRecordSecond(second);
            }
        });
    }

    private void updateEcgHr(final int hr) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateEcgHr(hr);
            }
        });
    }

    private void notifyHrAbnormal() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.notifyHrAbnormal();
            }
        });
    }
}

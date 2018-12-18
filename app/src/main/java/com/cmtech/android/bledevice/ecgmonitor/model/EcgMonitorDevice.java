package com.cmtech.android.bledevice.ecgmonitor.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgfilter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgfilter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.EcgHrHistogram;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.EcgHrWarner;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.android.bledevicecore.BleDataOpException;
import com.cmtech.android.bledevicecore.BleDevice;
import com.cmtech.android.bledevicecore.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.BleDeviceUtil;
import com.cmtech.android.bledevicecore.BleGattElement;
import com.cmtech.android.bledevicecore.IBleDataOpCallback;
import com.cmtech.bmefile.BmeFileDataType;
import com.cmtech.bmefile.BmeFileHead30;
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

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECGFILEDIR;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.IEcgHrProcessor.INVALID_HR;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.CACHEDIR;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.CCCUUID;


/**
 * EcgMonitorDevice: 心电带设备类
 * Created by bme on 2018/9/20.
 */

public class EcgMonitorDevice extends BleDevice {
    private final static String TAG = "EcgMonitorDevice";

    // 常量
    private static final int DEFAULT_SAMPLERATE = 125;                          // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEADTYPE = EcgLeadType.LEAD_I;     // 缺省导联为L1
    public static final int DEFAULT_CALIBRATIONVALUE = 65536;                  // 缺省1mV定标值
    private static final float DEFAULT_SECOND_PER_GRID = 0.04f;                 // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f;                      // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10;                       // 缺省每个栅格包含的像素个数

    // GATT消息常量
    private static final int MSG_OBTAINDATA = 1;                                // 获取一个ECG数据，可以是1mV定标数据，也可以是Ecg信号
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
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, null);

    private static final BleGattElement ECGMONITORDATACCC =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, CCCUUID);

    private static final BleGattElement ECGMONITORCTRL =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorCtrlUuid, null);

    private static final BleGattElement ECGMONITORSAMPLERATE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorSampleRateUuid, null);

    private static final BleGattElement ECGMONITORLEADTYPE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorLeadTypeUuid, null);

    // ECGMONITORCTRL控制常量
    private static final byte ECGMONITORCTRL_STOP =             (byte) 0x00;        // 停止采集
    private static final byte ECGMONITORCTRL_STARTSIGNAL =      (byte) 0x01;        // 启动采集Ecg信号
    private static final byte ECGMONITORCTRL_START1MV =         (byte) 0x02;        // 启动采集1mV定标


    ////////////////////////////////////////////////////////

    private int sampleRate = DEFAULT_SAMPLERATE;                    // 采样率
    public int getSampleRate() { return sampleRate; }
    private EcgLeadType leadType = DEFAULT_LEADTYPE;                // 导联类型
    public EcgLeadType getLeadType() {
        return leadType;
    }
    private int value1mVBeforeCalibrate = 0;
    public int getValue1mVBeforeCalibrate() { return value1mVBeforeCalibrate; }
    private final int value1mVAfterCalibrate = DEFAULT_CALIBRATIONVALUE;
    public int getValue1mVAfterCalibrate() { return value1mVAfterCalibrate; }
    private List<Integer> calibrationData = new ArrayList<>(250);       // 用于保存标定用的数据
    private int currentHr = 0;
    public int getCurrentHr() { return currentHr; }

    private boolean isRecord = false;                               // 是否记录信号
    public boolean isRecord() {return isRecord;}
    private boolean isFilter = true;                             // 是否对信号滤波
    public boolean isFilter() {return isFilter;}

    private EcgFile ecgFile = null;                                 // 用于保存心电信号的EcgFile文件对象
    private List<EcgComment> commentList = new ArrayList<>();   // 当前信号的留言列表

    private long recordDataNum = 0;                                 // 记录的心电数据个数
    // 获取记录的时间，单位为秒
    public int getRecordSecond() {
        return (int)(recordDataNum/sampleRate);
    }

    private final int pixelPerGrid = DEFAULT_PIXEL_PER_GRID;                   // 每小格的像素个数
    public int getPixelPerGrid() { return pixelPerGrid; }
    private int xPixelPerData = 2;     // 计算横向分辨率
    public int getxPixelPerData() { return xPixelPerData; }
    private float yValuePerPixel = 100.0f;                      // 计算纵向分辨率
    public float getyValuePerPixel() { return yValuePerPixel; }

    private IEcgCalibrator ecgCalibrator;                    // Ecg信号定标器
    private IEcgFilter ecgFilter;                            // Ecg信号滤波器
    private QrsDetector qrsDetector;                         // Ecg Qrs波检测器，可用于获取心率
    private EcgHrWarner hrWarner;                            // Ecg心率报警器
    private EcgHrHistogram hrHistogram;                      // Ecg心率直方图

    // 设备状态
    private EcgMonitorState state = EcgMonitorState.INIT;
    public EcgMonitorState getState() {
        return state;
    }
    private void setState(EcgMonitorState state) {
        this.state = state;
        updateEcgMonitorState();
    }

    // 设备配置信息
    private final EcgMonitorDeviceConfig config;
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

    private void changeConfiguration(EcgMonitorDeviceConfig config) {
        if(config.isWarnWhenHrAbnormal()) {
            if(hrWarner != null) {
                hrWarner.setHrWarn(config.getHrLowLimit(), config.getHrHighLimit());
            }
        } else {
            hrWarner = null;
        }
    }

    // 设备观察者
    private IEcgMonitorObserver observer;

    public EcgMonitorDevice(BleDeviceBasicInfo basicInfo) {
        super(basicInfo);
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

        if(!checkBasicEcgMonitorService()) {
            return false;
        }

        // 读采样率命令
        addReadCommand(ECGMONITORSAMPLERATE, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_OBTAINSAMPLERATE, (data[0] & 0xff) | ((data[1] << 8) & 0xff00));
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });

        // 读导联类型命令
        addReadCommand(ECGMONITORLEADTYPE, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_OBTAINLEADTYPE, data[0]);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });

        // 启动标定
        setState(EcgMonitorState.CALIBRATING);
        startSample1mV();

        return true;
    }

    @Override
    public void executeAfterDisconnect() {
    }

    @Override
    public void executeAfterConnectFailure() {
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

            // 接收到Ecg数据：Ecg信号或者定标数据
            case MSG_OBTAINDATA:
                if(msg.obj != null) {
                    byte[] data = (byte[]) msg.obj;
                    processData(data);
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

    // 设置是否记录心电信号
    public synchronized void setEcgRecord(boolean isRecord) {
        if(this.isRecord == isRecord) return;

        // 当前isRecord与要设置的isRecord不同，就意味着要改变当前的isRecord状态
        if(this.isRecord) {
            saveEcgFile();              // 停止记录心电信号，保存Ecg文件
        }

        this.isRecord = isRecord;

        if(this.isRecord) {
            // 如果已经标定了或者开始采样了,才可以开始记录心电信号，初始化Ecg文件
            if(state == EcgMonitorState.CALIBRATED || state == EcgMonitorState.SAMPLE)
                initializeEcgRecorder(sampleRate, value1mVAfterCalibrate, leadType);
            else {
                // 否则什么都不做，会在标定后根据isRecord值初始化Ecg文件
            }
        }

        updateRecordStatus(isRecord);
    }

    // 加载Ecg滤波器
    public synchronized void hookEcgFilter(boolean isFilter) {
        this.isFilter = isFilter;
    }

    // 转换当前状态
    public synchronized void switchSampleState() {
        switch(state) {
            case INIT:
                setState(EcgMonitorState.CALIBRATING);
                startSample1mV();
                break;
            case CALIBRATED:
                stopSampleData();
                startSampleEcg();
                break;
            case SAMPLE:
                stopSampleData();
                getHandler().removeCallbacksAndMessages(null);
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

    // 检测基本心电监护服务是否正常
    private boolean checkBasicEcgMonitorService() {
        Object ecgData = BleDeviceUtil.getGattObject(this, ECGMONITORDATA);
        Object ecgControl = BleDeviceUtil.getGattObject(this, ECGMONITORCTRL);
        Object ecgSampleRate = BleDeviceUtil.getGattObject(this, ECGMONITORSAMPLERATE);
        Object ecgLeadType = BleDeviceUtil.getGattObject(this, ECGMONITORLEADTYPE);
        Object ecgDataCCC = BleDeviceUtil.getGattObject(this, ECGMONITORDATACCC);

        if(ecgData == null || ecgControl == null || ecgSampleRate == null || ecgLeadType == null || ecgDataCCC == null) {
            ViseLog.e("EcgMonitor Services is wrong!");
            return false;
        }

        return true;
    }


    // 处理数据
    private void processData(byte[] data) {
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
            initializeEcgRecorder(sampleRate, value1mVAfterCalibrate, leadType);        // 如果需要记录，就初始化Ecg文件
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

    // 初始化Ecg记录器
    private void initializeEcgRecorder(int sampleRate, int calibrationValue, EcgLeadType leadType) {
        if(ecgFile != null) return;

        ecgFile = createEcgFile(sampleRate, calibrationValue, leadType);
        if(ecgFile != null) {
            commentList.clear();
            recordDataNum = 0;
            updateRecordSecond(0);
        }
    }

    // 创建Ecg文件
    private EcgFile createEcgFile(int sampleRate, int calibrationValue, EcgLeadType leadType) {
        EcgFile ecgFile = null;
        // 创建bmeFileHead文件头
        BmeFileHead30 bmeFileHead = new BmeFileHead30();
        bmeFileHead.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        bmeFileHead.setDataType(BmeFileDataType.INT32);
        bmeFileHead.setFs(sampleRate);
        bmeFileHead.setInfo("这是一个心电文件。");
        bmeFileHead.setCalibrationValue(calibrationValue);
        long timeInMillis = new Date().getTime();
        bmeFileHead.setCreatedTime(timeInMillis);

        // 创建ecgFileHead文件头
        String simpleMacAddress = EcgMonitorUtil.cutColonMacAddress(getMacAddress());
        EcgFileHead ecgFileHead = new EcgFileHead(UserAccountManager.getInstance().getUserAccount().getUserName(), simpleMacAddress, leadType);

        // 创建ecgFile
        String fileName = EcgMonitorUtil.createFileName(getMacAddress(), timeInMillis);
        File toFile = FileUtil.getFile(CACHEDIR, fileName);
        try {
            fileName = toFile.getCanonicalPath();
            ecgFile = EcgFile.createBmeFile(fileName, bmeFileHead, ecgFileHead);
            ViseLog.e(ecgFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ecgFile;
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
        hrWarner = new EcgHrWarner(config.getHrLowLimit(), config.getHrHighLimit());
    }

    // 保存Ecg文件
    private void saveEcgFile() {
        ViseLog.e(TAG + " " + getMacAddress() + ": saveEcgFile()");

        if (ecgFile != null) {
            try {
                if(ecgFile.getDataNum() <= 0) {     // 如果没有数据，删除文件
                    ecgFile.close();
                    FileUtil.deleteFile(ecgFile.getFile());
                } else {    // 如果有数据
                    if(!commentList.isEmpty()) {
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

        // enable ECG data notification
        addIndicateCommand(ECGMONITORDATACCC, true, null, indicationCallback);

        addWriteCommand(ECGMONITORCTRL, ECGMONITORCTRL_STARTSIGNAL, new IBleDataOpCallback() {
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
        addIndicateCommand(ECGMONITORDATACCC, true, null, indicationCallback);

        addWriteCommand(ECGMONITORCTRL, ECGMONITORCTRL_START1MV, null);
    }

    // 停止数据采集
    private void stopSampleData() {

        addWriteCommand(ECGMONITORCTRL, ECGMONITORCTRL_STOP, null);

        // disable ECG data indication
        addIndicateCommand(ECGMONITORDATACCC, false, null, null);

    }


    // 登记心电监护仪观察者
    public void registerEcgMonitorObserver(IEcgMonitorObserver observer) {
        this.observer = observer;
    }

    // 删除心电监护仪观察者
    public void removeEcgMonitorObserver() {
        observer = null;
    }

    // 关闭设备
    @Override
    public void close() {
        super.close();

        //removeEcgMonitorObserver();

        if(hrHistogram != null)
            hrHistogram.reset();

        if(isRecord) {
            saveEcgFile();
            isRecord = false;
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

package com.cmtech.android.bledevice.ecgmonitor.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcalibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcalibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfilter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfilter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecghrprocess.EcgHrProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecghrprocess.IEcgHrProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.state.EcgMonitorCalibratedState;
import com.cmtech.android.bledevice.ecgmonitor.model.state.EcgMonitorCalibratingState;
import com.cmtech.android.bledevice.ecgmonitor.model.state.EcgMonitorInitialState;
import com.cmtech.android.bledevice.ecgmonitor.model.state.EcgMonitorSampleState;
import com.cmtech.android.bledevice.ecgmonitor.model.state.IEcgMonitorState;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.android.bledevicecore.BleDataOpException;
import com.cmtech.android.bledevicecore.BleDevice;
import com.cmtech.android.bledevicecore.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.BleDeviceUtil;
import com.cmtech.android.bledevicecore.BleGattElement;
import com.cmtech.android.bledevicecore.IBleDataOpCallback;
import com.cmtech.bmefile.BmeFileDataType;
import com.cmtech.bmefile.BmeFileHead30;
import com.cmtech.bmefile.exception.FileException;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECGFILEDIR;
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
    private static final int DEFAULT_CALIBRATIONVALUE = 65536;                  // 缺省1mV定标值

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
    public int getSampleRate() {
        return sampleRate;
    }

    private EcgLeadType leadType = DEFAULT_LEADTYPE;                // 导联类型

    private int value1mV = DEFAULT_CALIBRATIONVALUE;                // 1mV定标值


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

    private IEcgCalibrator ecgCalibrator;                    // Ecg信号定标器

    private IEcgFilter ecgFilter;                            // Ecg信号滤波器

    private QrsDetector qrsDetector;                         // Ecg Qrs波检测器，可用于获取心率

    private IEcgHrProcessor hrProcessor;                     // Ecg心率处理器

    // 设备状态
    private final EcgMonitorInitialState initialState = new EcgMonitorInitialState(this);               // 初始化
    private final EcgMonitorCalibratingState calibratingState = new EcgMonitorCalibratingState(this);   // 标定中
    private final EcgMonitorCalibratedState calibratedState = new EcgMonitorCalibratedState(this);      // 标定完成
    private final EcgMonitorSampleState sampleState = new EcgMonitorSampleState(this);                  // 采样中
    public EcgMonitorInitialState getInitialState() {
        return initialState;
    }
    public EcgMonitorCalibratingState getCalibratingState() {
        return calibratingState;
    }
    public EcgMonitorCalibratedState getCalibratedState() {
        return calibratedState;
    }
    public EcgMonitorSampleState getSampleState() {
        return sampleState;
    }
    private IEcgMonitorState state = initialState;
    public void setState(IEcgMonitorState state) {
        this.state = state;
        ViseLog.i("The ecg monitor state is in " + state.getClass().getSimpleName());
        updateEcgMonitorState();
    }

    // 设备观察者
    private IEcgMonitorObserver observer;

    public EcgMonitorDevice(BleDeviceBasicInfo basicInfo) {
        super(basicInfo);
    }

    @Override
    public boolean executeAfterConnectSuccess() {
        updateSampleRate(DEFAULT_SAMPLERATE);
        updateLeadType(DEFAULT_LEADTYPE);
        updateCalibrationValue(0, DEFAULT_CALIBRATIONVALUE);

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

        setState(initialState);
        // 启动
        state.start();

        return true;
    }

    @Override
    public void executeAfterDisconnect() {
        //saveEcgFile();
    }

    @Override
    public void executeAfterConnectFailure() {
        //saveEcgFile();
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
                    initializeFilter();     // 有了采样率，可以初始化滤波器
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
                    state.onProcessData(data);
                }
                break;

            // 启动采集ECG信号
            case MSG_STARTSAMPLINGSIGNAL:
                setState(getSampleState());
                break;

            default:
                break;
        }
    }

    // 触发当前状态的启动
    public synchronized void start() {
        state.start();
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
            if(state == calibratedState || state == sampleState)        // 如果已经标定了或者开始采样了
                initializeEcgFile();                                    // 才可以开始记录心电信号，初始化Ecg文件
            else {
                // 否则什么都不做，会在标定后根据isRecord值初始化Ecg文件
            }
        }

        updateRecordStatus(isRecord);
    }

    // 停止记录心电信号
    public synchronized void stopEcgRecord() {
        setEcgRecord(false);
    }

    // 加载Ecg滤波器
    public synchronized void hookEcgFilter(boolean isFilter) {
        this.isFilter = isFilter;
    }

    // 触发当前状态的转换
    public synchronized void switchSampleState() {
        state.switchState();
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

    // 响应获取到1mV定标值后要做的事情
    public void onSetValue1mV(int value1mV) {
        // 初始化定标器
        initializeCalibrator(value1mV);

        // 初始化EcgFile
        if(isRecord) {
            initializeEcgFile();        // 如果需要记录，就初始化Ecg文件
        }

        // 初始化Qrs波检测器
        initializeQrsDetector();

        // 初始化心率处理器
        initializeHrProcessor();

        // 初始化EcgView
        initializeEcgView();
    }

    // 初始化定标器
    private void initializeCalibrator(int value1mV) {
        ecgCalibrator = new EcgCalibrator65536(value1mV);       // 初始化定标器
        updateCalibrationValue(value1mV, this.value1mV);
    }

    // 初始化Ecg文件
    private void initializeEcgFile() {
        if(ecgFile == null) {
            // 创建bmeFileHead文件头
            BmeFileHead30 bmeFileHead = new BmeFileHead30();
            bmeFileHead.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            bmeFileHead.setDataType(BmeFileDataType.INT32);
            bmeFileHead.setFs(sampleRate);
            bmeFileHead.setInfo("这是一个心电文件。");
            bmeFileHead.setCalibrationValue(value1mV);
            long timeInMillis = new Date().getTime();
            bmeFileHead.setCreatedTime(timeInMillis);

            // 创建ecgFileHead文件头
            String simpleMacAddress = EcgMonitorUtil.cutColonMacAddress(getMacAddress());
            EcgFileHead ecgFileHead = new EcgFileHead(UserAccountManager.getInstance().getUserAccount().getUserName(), simpleMacAddress, leadType);
            commentList.clear();

            // 创建ecgFile
            String fileName = EcgMonitorUtil.createFileName(getMacAddress(), timeInMillis);
            File toFile = FileUtil.getFile(CACHEDIR, fileName);
            try {
                fileName = toFile.getCanonicalPath();
                ecgFile = EcgFile.createBmeFile(fileName, bmeFileHead, ecgFileHead);
                recordDataNum = 0;
                updateRecordSecond(0);
                ViseLog.e(ecgFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 初始化EcgView
    private void initializeEcgView() {
        int pixelPerGrid = 10;                   // 每小格的像素个数
        float xSecondPerGrid = 0.04f;            // X方向每小格代表的秒数，即0.04对应于25格/秒，这是标准的ECG走纸速度
        float yMvPerGrid = 0.1f;                 // Y方向每小格代表的mV
        // 计算EcgView分辨率
        int xPixelPerData = Math.round(pixelPerGrid / (xSecondPerGrid * sampleRate));     // 计算横向分辨率
        float yValuePerPixel = value1mV * yMvPerGrid / pixelPerGrid;                      // 计算纵向分辨率
        // 更新EcgView
        updateEcgView(xPixelPerData, yValuePerPixel, pixelPerGrid);
    }

    // 初始化滤波器
    private void initializeFilter() {
        ecgFilter = new EcgPreFilterWith35HzNotch(sampleRate);
    }

    // 初始化Qrs波检测器
    private void initializeQrsDetector() {
        qrsDetector = new QrsDetector(sampleRate, value1mV);
    }

    // 初始化心率处理器
    private void initializeHrProcessor() {
        if(hrProcessor == null) {
            hrProcessor = new EcgHrProcessor();
        }
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
                        for(EcgComment comment : commentList) {
                            ecgFile.addComment(comment);
                        }
                    }
                    ecgFile.close();
                    ViseLog.e(ecgFile);
                    File toFile = FileUtil.getFile(ECGFILEDIR, ecgFile.getFile().getName());
                    // 将缓存区中的文件移动到ECGFILEDIR目录中
                    FileUtil.moveFile(ecgFile.getFile(), toFile);
                }
                ecgFile = null;
            } catch (FileException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 处理Ecg信号
    public void processEcgSignal(int ecgSignal) {
        // 标定后滤波处理
        ecgSignal = (int) ecgFilter.filter(ecgCalibrator.process(ecgSignal));

        // 保存到EcgFile
        if(isRecord) {
            try {
                ecgFile.writeData(ecgSignal);
                recordDataNum++;
                updateRecordSecond(getRecordSecond());
            } catch (FileException e) {
                e.printStackTrace();
            }
        }

        // 显示心电信号
        updateEcgSignal(ecgSignal);

        // 检测Qrs波，获取心率
        int hr = qrsDetector.outputHR(ecgSignal);

        if(hr != 0) {
            // 更新心率值显示
            updateEcgHr(hr);

            // 处理心率值
            hrProcessor.process(hr);

            // 如果需要报警
            if(((EcgHrProcessor) hrProcessor).isWarn()) {
                notifyHrWarn();
            }
        }
    }

    public int[] getHrStatistics() {
        if(hrProcessor instanceof EcgHrProcessor) {
            return ((EcgHrProcessor) hrProcessor).getHrHistgram();
        }
        return null;
    }

    // 启动ECG信号采集
    public void startSampleEcg() {

        IBleDataOpCallback notifyCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_OBTAINDATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };

        // enable ECG data notification
        addNotifyCommand(ECGMONITORDATACCC, true, null, notifyCallback);

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

    // 启动1mV信号采集
    public void startSample1mV() {
        IBleDataOpCallback notifyCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_OBTAINDATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };

        // enable ECG data notification
        addNotifyCommand(ECGMONITORDATACCC, true, null, notifyCallback);

        addWriteCommand(ECGMONITORCTRL, ECGMONITORCTRL_START1MV, null);
    }

    // 停止数据采集
    public void stopSampleData() {

        addWriteCommand(ECGMONITORCTRL, ECGMONITORCTRL_STOP, null);

        // disable ECG data notification
        addNotifyCommand(ECGMONITORDATACCC, false, null, null);

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

        removeEcgMonitorObserver();

        hrProcessor = null;

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

    private void notifyHrWarn() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.notifyHrWarn();
            }
        });
    }
}

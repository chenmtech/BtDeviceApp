package com.cmtech.android.bledevice.ecgmonitor.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.state.EcgMonitorCalibratedState;
import com.cmtech.android.bledevice.ecgmonitor.model.state.EcgMonitorCalibratingState;
import com.cmtech.android.bledevice.ecgmonitor.model.state.EcgMonitorInitialState;
import com.cmtech.android.bledevice.ecgmonitor.model.state.EcgMonitorSampleState;
import com.cmtech.android.bledevice.ecgmonitor.model.state.IEcgMonitorState;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.android.bledevicecore.model.BleDataOpException;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.model.BleGattElement;
import com.cmtech.android.bledevicecore.model.IBleDataOpCallback;
import com.cmtech.bmefile.BmeFileDataType;
import com.cmtech.bmefile.BmeFileHead30;
import com.cmtech.bmefile.exception.FileException;
import com.cmtech.dsp.filter.IIRFilter;
import com.cmtech.dsp.filter.design.DCBlockDesigner;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.structure.StructType;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.CACHEDIR;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECGFILEDIR;
import static com.cmtech.android.bledevicecore.model.BleDeviceConstant.CCCUUID;


/**
 * EcgMonitorDevice: 心电带设备类
 * Created by bme on 2018/9/20.
 */

public class EcgMonitorDevice extends BleDevice {
    // 常量
    private static final int DEFAULT_SAMPLERATE = 125;                          // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEADTYPE = EcgLeadType.LEAD_I;     // 缺省导联为L1
    private static final int DEFAULT_CALIBRATIONVALUE = 2600;                   // 缺省1mV定标值

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
    private void setValue1mV(int value1mV) {
        this.value1mV = value1mV;
        updateCalibrationValue(value1mV);
    }

    private boolean isRecord = false;                               // 是否记录信号
    public boolean isRecord() {return isRecord;}
    private boolean isEcgFilter = true;                             // 是否对信号滤波
    public boolean isEcgFilter() {return isEcgFilter;}

    private EcgFile ecgFile = null;                                 // 用于保存心电信号的EcgFile文件对象
    private List<EcgComment> commentList = new ArrayList<>();   // 当前信号的留言列表

    private long recordDataNum = 0;                                 // 记录的心电数据个数
    // 获取记录的时间，单位为秒
    public int getRecordSecond() {
        return (int)(recordDataNum/sampleRate);
    }

    private IIRFilter dcBlock = null;                               // 隔直滤波器
    private IIRFilter notch50Hz = null;                             // 50Hz工频干扰陷波器
    private IIRFilter notch35Hz = null;                             // 35Hz肌电干扰陷波器
    private QrsDetector qrsDetector = null;                         // QRS波检测器

    // 用于设置EcgWaveView的参数
    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV

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
        initializeAfterConstruction();
    }

    private void initializeAfterConstruction() {
    }

    @Override
    public boolean executeAfterConnectSuccess() {

        updateSampleRate(DEFAULT_SAMPLERATE);
        updateLeadType(DEFAULT_LEADTYPE);
        updateCalibrationValue(DEFAULT_CALIBRATIONVALUE);

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
        stopSaveEcgFile();
    }

    @Override
    public void executeAfterConnectFailure() {
        stopSaveEcgFile();
    }

    private void stopSaveEcgFile() {
        if(this.isRecord) {
            saveEcgFile();
            //this.isRecord = false;
            //updateRecordStatus(false);
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

        if(isRecord) {
            if(state == calibratedState || state == sampleState)        // 如果已经标定了或者开始采样了
                initializeEcgFile();                                    // 才可以开始记录心电信号，初始化Ecg文件
            else {
                                                                        // 否则什么都不做，会在标定后根据isRecord值初始化Ecg文件
            }
        } else {
            saveEcgFile();              // 停止记录心电信号，保存Ecg文件
        }
        this.isRecord = isRecord;
        updateRecordStatus(isRecord);
    }

    // 停止记录心电信号
    public synchronized void stopEcgRecord() {
        setEcgRecord(false);
    }

    public synchronized void setEcgFilter(boolean isEcgFilter) {
        this.isEcgFilter = isEcgFilter;
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
        Object ecgData = getGattObject(ECGMONITORDATA);
        Object ecgControl = getGattObject(ECGMONITORCTRL);
        Object ecgSampleRate = getGattObject(ECGMONITORSAMPLERATE);
        Object ecgLeadType = getGattObject(ECGMONITORLEADTYPE);
        Object ecgDataCCC = getGattObject(ECGMONITORDATACCC);

        if(ecgData == null || ecgControl == null || ecgSampleRate == null || ecgLeadType == null || ecgDataCCC == null) {
            ViseLog.e("EcgMonitor Services is wrong!");
            return false;
        }

        return true;
    }

    // 完成标定后要做的事情
    public void finishCalibration(int value1mV) {
        setValue1mV(value1mV);
        if(isRecord) {
            initializeEcgFile();        // 如果需要记录，就初始化Ecg文件
        }
        initializeQrsDetector();        // 初始化QRS波检测器
        initializeEcgView();            // 初始化EcgView
    }

    // 初始化EcgView
    private void initializeEcgView() {
        // 启动ECG View
        int xRes = Math.round(viewGridWidth / (viewXGridTime * sampleRate));   // 计算横向分辨率
        float yRes = value1mV * viewYGridmV / viewGridWidth;                     // 计算纵向分辨率
        updateEcgView(xRes, yRes, viewGridWidth);
    }

    // 初始化滤波器
    private void initializeFilter() {
        // 准备0.5Hz基线漂移滤波器
        dcBlock = DCBlockDesigner.design(0.5, sampleRate);                   // 设计隔直滤波器
        dcBlock.createStructure(StructType.IIR_DCBLOCK);                            // 创建隔直滤波器专用结构
        // 准备50Hz陷波器
        notch50Hz = NotchDesigner.design(50, 0.5, sampleRate);           // 设计陷波器
        notch50Hz.createStructure(StructType.IIR_NOTCH);                            // 创建陷波器专用结构
        // 准备35Hz陷波器
        notch35Hz = NotchDesigner.design(35, 0.5, sampleRate);           // 设计陷波器
        notch35Hz.createStructure(StructType.IIR_NOTCH);                            // 创建陷波器专用结构
    }

    // 初始化QRS波检测器
    private void initializeQrsDetector() {
        qrsDetector = new QrsDetector(sampleRate, value1mV);
    }


    // 处理Ecg信号
    public void processEcgSignal(int ecgSignal) {
        if(isEcgFilter)
            ecgSignal = (int) notch35Hz.filter(notch50Hz.filter(dcBlock.filter(ecgSignal)));

        if(isRecord) {
            try {
                ecgFile.writeData(ecgSignal);
                recordDataNum++;
                updateRecordSecond(getRecordSecond());
            } catch (FileException e) {
                e.printStackTrace();
            }
        }

        updateEcgSignal(ecgSignal);

        int hr = qrsDetector.outputHR(ecgSignal);
        if(hr != 0) {
            ViseLog.i("current HR is " + hr);
            updateEcgHr(hr);
        }
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

    // 初始化Ecg文件
    private void initializeEcgFile() {
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

    // 保存Ecg文件
    private void saveEcgFile() {
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
            } catch (FileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private void updateCalibrationValue(final int calibrationValue) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateCalibrationValue(calibrationValue);
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

    private void updateEcgView(final int xRes, final float yRes, final int viewGridWidth) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(observer != null)
                    observer.updateEcgView(xRes, yRes, viewGridWidth);
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
}

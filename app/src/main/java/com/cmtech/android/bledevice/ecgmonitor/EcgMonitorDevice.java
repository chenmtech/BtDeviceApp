package com.cmtech.android.bledevice.ecgmonitor;

import android.os.Message;
import android.util.Log;

import com.cmtech.android.bledevice.ecgmonitor.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.ecgfile.EcgFileComment;
import com.cmtech.android.bledevice.ecgmonitor.ecgfile.EcgFileHead;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.EcgMonitorCalibratingState;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.EcgMonitorCalibratedState;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.EcgMonitorInitialState;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.EcgMonitorSampleState;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.IEcgMonitorState;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.android.bledevicecore.model.BleDataOpException;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.model.BleGattElement;
import com.cmtech.android.bledevicecore.model.IBleDataOpCallback;
import com.cmtech.dsp.bmefile.BmeFile;
import com.cmtech.dsp.bmefile.BmeFileDataType;
import com.cmtech.dsp.bmefile.BmeFileHead;
import com.cmtech.dsp.bmefile.BmeFileHead30;
import com.cmtech.dsp.exception.FileException;
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

import static com.cmtech.android.bledevicecore.model.BleDeviceConstant.CCCUUID;


/**
 * 心电监护仪类
 * Created by bme on 2018/9/20.
 */

public class EcgMonitorDevice extends BleDevice {
    public static final File ECGFILEDIR = MyApplication.getContext().getExternalFilesDir("ecgSignal");

    // 常量
    private static final int DEFAULT_SAMPLERATE = 125;           // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEADTYPE = EcgLeadType.LEAD_I;     // 缺省导联为L1
    private static final int DEFAULT_CALIBRATIONVALUE = 2600;       // 缺省1mV定标值

    // GATT消息常量
    private static final int MSG_ECGDATA = 1;            // ECG数据
    private static final int MSG_READSAMPLERATE = 2;    // 读采样率
    private static final int MSG_READLEADTYPE = 3;      // 读导联类型
    private static final int MSG_STARTECGSAMPLE = 4;

    /////////////////   心电监护仪Service UUID常量////////////////
    private static final String ecgMonitorServiceUuid       = "aa40";           // 心电监护仪服务UUID:aa40
    private static final String ecgMonitorDataUuid          = "aa41";           // ECG数据特征UUID:aa41
    private static final String ecgMonitorCtrlUuid          = "aa42";           // 测量控制UUID:aa42
    private static final String ecgMonitorSampleRateUuid    = "aa44";           // 采样率UUID:aa44
    private static final String ecgMonitorLeadTypeUuid      = "aa45";           // 导联类型UUID:aa45

    // 心电监护仪Gatt Element常量
    public static final BleGattElement ECGMONITORDATA =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, null);

    public static final BleGattElement ECGMONITORDATACCC =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, CCCUUID);

    public static final BleGattElement ECGMONITORCTRL =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorCtrlUuid, null);

    public static final BleGattElement ECGMONITORSAMPLERATE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorSampleRateUuid, null);

    public static final BleGattElement ECGMONITORLEADTYPE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorLeadTypeUuid, null);
    ////////////////////////////////////////////////////////

    private int sampleRate = DEFAULT_SAMPLERATE;            // 采样率
    public int getSampleRate() {
        return sampleRate;
    }

    private EcgLeadType leadType = DEFAULT_LEADTYPE;      // 导联类型

    private int value1mV = DEFAULT_CALIBRATIONVALUE;                // 1mV定标值
    public void setValue1mV(int value1mV) {
        this.value1mV = value1mV;
        initializeQrsDetector();
        updateCalibrationValue(value1mV);
    }

    private boolean isRecord = false;                // 是否记录心电信号
    public boolean isRecord() {return isRecord;}
    private boolean isEcgFilter = true;                // 是否对信号滤波
    public boolean isEcgFilter() {return isEcgFilter;}

    private EcgFile ecgFile = null;                 // 用于保存心电信号的BmeFile文件对象
    private List<EcgFileComment> commentList = new ArrayList<>();

    private IIRFilter dcBlock = null;               // 隔直滤波器
    private IIRFilter notch = null;                 // 50Hz陷波器
    private QrsDetector qrsDetector = null;         // QRS波检测器

    // 用于设置EcgWaveView的参数
    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    // 下面两个参数可用来计算View中的xRes和yRes
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV

    // 设备状态
    private final EcgMonitorInitialState initialState = new EcgMonitorInitialState(this);
    private final EcgMonitorCalibratingState calibratingState = new EcgMonitorCalibratingState(this);
    private final EcgMonitorCalibratedState calibratedState = new EcgMonitorCalibratedState(this);
    private final EcgMonitorSampleState sampleState = new EcgMonitorSampleState(this);
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
        ViseLog.i("The device state is set as " + state.getClass().getSimpleName());
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
                sendGattMessage(MSG_READSAMPLERATE, (data[0] & 0xff) | ((data[1] << 8) & 0xff00));
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });

        // 读导联类型命令
        addReadCommand(ECGMONITORLEADTYPE, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_READLEADTYPE, data[0]);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });

        // 启动1mV数据采集
        setState(initialState);
        state.start();

        return true;
    }

    @Override
    public void executeAfterDisconnect() {
        if(this.isRecord)
            saveEcgFile();
        this.isRecord = false;
        if(observer != null)
            observer.updateRecordStatus(false);
    }

    @Override
    public void executeAfterConnectFailure() {
        if(this.isRecord)
            saveEcgFile();
        this.isRecord = false;
        if(observer != null)
            observer.updateRecordStatus(false);
    }

    @Override
    public synchronized void processGattMessage(Message msg)
    {
        switch (msg.what) {
            // 接收到采样率数据
            case MSG_READSAMPLERATE:
                if(msg.obj != null) {
                    sampleRate = (Integer) msg.obj;
                    updateSampleRate(sampleRate);
                    initializeFilter();
                }
                break;

            // 接收到导联类型数据
            case MSG_READLEADTYPE:
                if(msg.obj != null) {
                    Number num = (Number)msg.obj;
                    leadType = EcgLeadType.getFromCode(num.intValue());
                    updateLeadType(leadType);
                }
                break;

            // 接收到信号数据
            case MSG_ECGDATA:
                if(msg.obj != null) {
                    byte[] data = (byte[]) msg.obj;
                    state.onProcessData(data);
                }
                break;

            case MSG_STARTECGSAMPLE:
                setState(getSampleState());
                break;

            default:
                break;
        }
    }

    public synchronized void start() {
        state.start();
    }

    public synchronized void setEcgRecord(boolean isRecord) {
        if(!this.isRecord && isRecord) {
            initializeEcgFile();
        } else if(this.isRecord && !isRecord){
            saveEcgFile();
        }
        this.isRecord = isRecord;
    }

    public synchronized void setEcgFilter(boolean isEcgFilter) {
        this.isEcgFilter = isEcgFilter;
    }

    public synchronized void switchSampleState() {
        state.switchState();
    }

    // 检测基本心电监护服务是否正常
    private boolean checkBasicEcgMonitorService() {
        Object ecgData = getGattObject(ECGMONITORDATA);
        Object ecgControl = getGattObject(ECGMONITORCTRL);
        Object ecgSampleRate = getGattObject(ECGMONITORSAMPLERATE);
        Object ecgLeadType = getGattObject(ECGMONITORLEADTYPE);
        Object ecgDataCCC = getGattObject(ECGMONITORDATACCC);

        if(ecgData == null || ecgControl == null || ecgSampleRate == null || ecgLeadType == null || ecgDataCCC == null) {
            Log.d("EcgMonitorFragment", "can't find Gatt object of this element on the device.");
            return false;
        }

        return true;
    }

    public void initializeEcgView() {
        // 启动ECG View
        int xRes = Math.round(viewGridWidth / (viewXGridTime * sampleRate));   // 计算横向分辨率
        float yRes = value1mV * viewYGridmV / viewGridWidth;                     // 计算纵向分辨率
        updateEcgView(xRes, yRes, viewGridWidth);
    }

    private void initializeFilter() {
        // 准备隔直滤波器
        dcBlock = DCBlockDesigner.design(0.06, sampleRate);   // 设计隔直滤波器
        dcBlock.createStructure(StructType.IIR_DCBLOCK);            // 创建隔直滤波器专用结构
        // 准备陷波器
        notch = NotchDesigner.design(50, 0.5, sampleRate);
        notch.createStructure(StructType.IIR_NOTCH);
    }

    private void initializeQrsDetector() {
        qrsDetector = new QrsDetector(sampleRate, value1mV);
    }

    public void processOneEcgData(int ecgData) {
        if(isEcgFilter)
            ecgData = (int)notch.filter(dcBlock.filter(ecgData));

        if(isRecord) {
            try {
                ecgFile.writeData(ecgData);
            } catch (FileException e) {
                e.printStackTrace();
            }
        }

        updateEcgData(ecgData);

        int hr = qrsDetector.outputHR(ecgData);
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
                sendGattMessage(MSG_ECGDATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };

        // enable ECG data notification
        addNotifyCommand(ECGMONITORDATACCC, true, null, notifyCallback);

        addWriteCommand(ECGMONITORCTRL, (byte) 0x01, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_STARTECGSAMPLE, null);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });
    }


    private void initializeEcgFile() {
        // 创建bmeFileHead文件头
        BmeFileHead30 bmeFileHead = new BmeFileHead30();
        bmeFileHead.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        bmeFileHead.setDataType(BmeFileDataType.INT32);
        bmeFileHead.setFs(sampleRate);
        bmeFileHead.setInfo("Ecg Lead " + leadType.getDescription());
        bmeFileHead.setCalibrationValue(value1mV);

        // 创建ecgFileHead文件头
        String simpleMacAddress = EcgMonitorUtil.simpleMacAddress(getMacAddress());
        long timeInMillis = new Date().getTime();
        EcgFileHead ecgFileHead = new EcgFileHead(UserAccountManager.getInstance().getUserAccount().getUserName(), simpleMacAddress, timeInMillis);

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
    }


    // 启动1mV信号采集
    public void startSample1mV() {
        IBleDataOpCallback notifyCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_ECGDATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };

        // enable ECG data notification
        addNotifyCommand(ECGMONITORDATACCC, true, null, notifyCallback);

        addWriteCommand(ECGMONITORCTRL, (byte)0x02, null);
    }

    // 停止ECG数据采集
    public void stopSampleData() {

        addWriteCommand(ECGMONITORCTRL, (byte)0x00, null);

        // disable ECG data notification
        addNotifyCommand(ECGMONITORDATACCC, false, null, null);

    }

    public synchronized void addComment(String comment) {
        long timeCreated = new Date().getTime();
        commentList.add(new EcgFileComment(UserAccountManager.getInstance().getUserAccount().getUserName(), timeCreated, comment));
    }

    private void saveEcgFile() {
        if (ecgFile != null) {
            try {
                if(ecgFile.getDataNum() <= 0) {     // 如果没有数据，删除文件
                    ecgFile.close();
                    FileUtil.deleteFile(ecgFile.getFile());
                } else {    // 如果有数据
                    if(!commentList.isEmpty()) {
                        for(EcgFileComment comment : commentList) {
                            ecgFile.addComment(comment);
                        }
                        commentList.clear();
                    }
                    ecgFile.close();
                    File toFile = FileUtil.getFile(ECGFILEDIR, ecgFile.getFile().getName());
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
        if(observer != null) {
            observer.updateState(state);
        }
    }

    private void updateSampleRate(final int sampleRate) {
        if(observer != null) {
            observer.updateSampleRate(sampleRate);
        }
    }

    private void updateLeadType(final EcgLeadType leadType) {
        if(observer != null) {
            observer.updateLeadType(leadType);
        }
    }

    private void updateCalibrationValue(final int calibrationValue) {
        if(observer != null) {
            observer.updateCalibrationValue(calibrationValue);
        }
    }

    private void updateEcgView(final int xRes, final float yRes, final int viewGridWidth) {
        if(observer != null) {
            observer.updateEcgView(xRes, yRes, viewGridWidth);
        }
    }

    private void updateEcgData(int ecgData) {
        if(observer != null) {
            observer.updateEcgData(ecgData);
        }
    }

    private void updateEcgHr(final int hr) {
        if(observer != null) {
            observer.updateEcgHr(hr);
        }
    }
}

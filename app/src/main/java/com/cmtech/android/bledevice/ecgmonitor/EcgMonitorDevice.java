package com.cmtech.android.bledevice.ecgmonitor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.EcgMonitorCalibrateState;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.EcgMonitorCalibratedState;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.EcgMonitorInitialState;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.EcgMonitorSampleState;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.IEcgMonitorState;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.model.BleGattElement;
import com.cmtech.android.bledeviceapp.util.Uuid;
import com.cmtech.dsp.bmefile.BmeFile;
import com.cmtech.dsp.bmefile.BmeFileDataType;
import com.cmtech.dsp.bmefile.BmeFileHead;
import com.cmtech.dsp.bmefile.BmeFileHead10;
import com.cmtech.dsp.bmefile.BmeFileHeadFactory;
import com.cmtech.dsp.exception.FileException;
import com.cmtech.dsp.filter.IIRFilter;
import com.cmtech.dsp.filter.design.DCBlockDesigner;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.structure.StructType;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 心电监护仪类
 * Created by bme on 2018/9/20.
 */

public class EcgMonitorDevice extends BleDevice {
    // 常量
    private static final int DEFAULT_SAMPLERATE = 125;           // 缺省ECG信号采样率,Hz
    private static final EcgLeadType DEFAULT_LEADTYPE = EcgLeadType.LEAD_I;     // 缺省导联为L1
    private static final int DEFAULT_CALIBRATIONVALUE = 2600;       // 缺省1mV定标值

    // 消息常量
    private static final int MSG_ECGDATA = 1;            // ECG数据
    private static final int MSG_READSAMPLERATE = 2;    // 读采样率
    private static final int MSG_READLEADTYPE = 3;      // 读导联类型

    /////////////////   心电监护仪Service UUID常量////////////////
    private static final String ecgMonitorServiceUuid       = "aa40";           // 心电监护仪服务UUID:aa40
    private static final String ecgMonitorDataUuid          = "aa41";           // ECG数据特征UUID:aa41
    private static final String ecgMonitorCtrlUuid          = "aa42";           // 测量控制UUID:aa42
    private static final String ecgMonitorSampleRateUuid    = "aa44";           // 采样率UUID:aa44
    private static final String ecgMonitorLeadTypeUuid      = "aa45";           // 导联类型UUID:aa45

    // 一些Gatt Element常量
    public static final BleGattElement ECGMONITORDATA =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, null);

    public static final BleGattElement ECGMONITORDATACCC =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, Uuid.CCCUUID);

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
    private boolean isFilter = false;                // 是否对信号滤波

    private BmeFileHead ecgFileHead = null;         // 用于保存心电信号的BmeFile文件头，为了能在Windows下读取文件，使用BmeFileHead10版本，LITTLE_ENDIAN，数据类型为INT32
    private BmeFile ecgFile = null;                 // 用于保存心电信号的BmeFile文件对象

    private IIRFilter dcBlock = null;               // 隔直滤波器
    private IIRFilter notch = null;                 // 50Hz陷波器

    private QrsDetector qrsDetector = null;         // QRS波检测器

    // 用于设置EcgWaveView的参数
    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    // 下面两个参数可用来计算View中的xRes和yRes
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV

    private final EcgMonitorInitialState initialState = new EcgMonitorInitialState(this);
    private final EcgMonitorCalibrateState calibratingState = new EcgMonitorCalibrateState(this);
    private final EcgMonitorCalibratedState calibratedState = new EcgMonitorCalibratedState(this);
    private final EcgMonitorSampleState samplingState = new EcgMonitorSampleState(this);

    private IEcgMonitorState state = initialState;

    private IEcgMonitorObserver observer;

    public EcgMonitorDevice(BleDeviceBasicInfo basicInfo) {
        super(basicInfo);
    }

    public EcgMonitorInitialState getInitialState() {
        return initialState;
    }

    public EcgMonitorCalibrateState getCalibratingState() {
        return calibratingState;
    }

    public EcgMonitorCalibratedState getCalibratedState() {
        return calibratedState;
    }

    public EcgMonitorSampleState getSamplingState() {
        return samplingState;
    }

    public void setState(IEcgMonitorState state) {
        this.state = state;
        ViseLog.i("The device state is set as " + state.getClass().getSimpleName());
        updateEcgMonitorState();
    }

    @Override
    public void initializeAfterConstruction() {
    }

    @Override
    public void executeAfterConnectSuccess() {
        isRecord = false;
        updateRecordCheckBox(false, false);

        updateFilterCheckBox(isFilter, false);

        updateSampleRate(DEFAULT_SAMPLERATE);
        updateLeadType(DEFAULT_LEADTYPE);
        updateCalibrationValue(DEFAULT_CALIBRATIONVALUE);

        if(!checkBasicEcgMonitorService()) return;


        // 读采样率命令
        addReadCommand(ECGMONITORSAMPLERATE, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendGattCallbackMessage(MSG_READSAMPLERATE, (data[0] & 0xff) | ((data[1] << 8) & 0xff00));
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });

        // 读导联类型命令
        addReadCommand(ECGMONITORLEADTYPE, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendGattCallbackMessage(MSG_READLEADTYPE, data[0]);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });

        // 启动1mV数据采集
        setState(initialState);
        state.start();
    }

    @Override
    public void executeAfterDisconnect() {
        setEcgRecord(false);
    }

    @Override
    public void executeAfterConnectFailure() {
        setEcgRecord(false);
    }

    @Override
    public synchronized void processGattCallbackMessage(Message msg)
    {
        switch (msg.what) {
            // 接收到采样率数据
            case MSG_READSAMPLERATE:
                if(msg.obj != null) {
                    sampleRate = (Integer) msg.obj;
                    updateSampleRate(sampleRate);
                    initializeFilter();
                    updateFilterCheckBox(isFilter, true);
                }
                break;

            // 接收到导联类型数据
            case MSG_READLEADTYPE:
                if(msg.obj != null) {
                    Number num = (Number)msg.obj;
                    leadType = EcgLeadType.getFromCode(num.intValue());
                    updateLeadType(leadType);
                    initializeEcgFile();
                    updateRecordCheckBox(false, true);
                }
                break;

            // 接收到信号数据
            case MSG_ECGDATA:
                if(msg.obj != null) {
                    byte[] data = (byte[]) msg.obj;
                    state.onProcessData(data);
                }
                break;

            default:
                break;
        }
    }

    public synchronized void start() {
        state.start();
    }

    public synchronized void setEcgRecord(boolean isRecord) {
        if(this.isRecord != isRecord) {
            if (isRecord) {
                DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String fileName = getMacAddress()+" "+format.format(new Date())+".bme";
                File toFile = FileUtil.getFile(MyApplication.getContext().getExternalFilesDir("ecgSignal"), fileName);
                try {
                    fileName = toFile.getCanonicalPath();
                    ecgFile = BmeFile.createBmeFile(fileName, ecgFileHead);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (ecgFile != null) {
                    try {
                        ecgFile.close();
                        ecgFile = null;
                    } catch (FileException e) {
                        e.printStackTrace();
                    }
                }
            }

            this.isRecord = isRecord;
        }
    }

    public synchronized void setEcgFilter(boolean isFilter) {
        this.isFilter = isFilter;
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



    private void initializeEcgFile() {
        // 准备记录心电信号的文件头
        try {
            ecgFileHead = BmeFileHeadFactory.create(BmeFileHead10.VER);
            ecgFileHead.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            ecgFileHead.setDataType(BmeFileDataType.INT32);
            ecgFileHead.setFs(sampleRate);
            ecgFileHead.setInfo("Ecg Lead " + leadType.getDescription());
        } catch (FileException e) {
            e.printStackTrace();
        }
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
        if(isFilter)
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
        IBleCallback notifyCallback = new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendGattCallbackMessage(MSG_ECGDATA, data);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };

        // enable ECG data notification
        addNotifyCommand(ECGMONITORDATACCC, true, null, notifyCallback);

        addWriteCommand(ECGMONITORCTRL, (byte)0x01, null);
    }

    // 启动1mV信号采集
    public void startSample1mV() {
        IBleCallback notifyCallback = new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendGattCallbackMessage(MSG_ECGDATA, data);
            }

            @Override
            public void onFailure(BleException exception) {

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
            // 保证在主线程更新连接状态
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    observer.updateState(state);
                }
            });
        }
    }

    private void updateSampleRate(final int sampleRate) {
        if(observer != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    observer.updateSampleRate(sampleRate);
                }
            });
        }
    }

    private void updateLeadType(final EcgLeadType leadType) {
        if(observer != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    observer.updateLeadType(leadType);
                }
            });
        }
    }

    private void updateCalibrationValue(final int calibrationValue) {
        if(observer != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    observer.updateCalibrationValue(calibrationValue);
                }
            });
        }
    }

    private void updateEcgView(final int xRes, final float yRes, final int viewGridWidth) {
        if(observer != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    observer.updateEcgView(xRes, yRes, viewGridWidth);
                }
            });
        }
    }

    private void updateRecordCheckBox(final boolean isChecked, final boolean clickable) {
        if(observer != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    observer.updateRecordCheckBox(isChecked, clickable);
                }
            });
        }
    }

    private void updateFilterCheckBox(final boolean isChecked, final boolean clickable) {
        if(observer != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    observer.updateFilterCheckBox(isChecked, clickable);
                }
            });
        }
    }

    private void updateEcgData(int ecgData) {
        if(observer != null) {
            observer.updateEcgData(ecgData);
        }
    }

    private void updateEcgHr(int hr) {
        if(observer != null) {
            observer.updateEcgHr(hr);
        }
    }
}

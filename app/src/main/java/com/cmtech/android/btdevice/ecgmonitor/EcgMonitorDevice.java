package com.cmtech.android.btdevice.ecgmonitor;

import android.os.Message;
import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate.EcgMonitorCalibratedState;
import com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate.EcgMonitorCalibratingState;
import com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate.EcgMonitorInitialState;
import com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate.EcgMonitorSamplingState;
import com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate.IEcgMonitorState;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.BleGattElement;
import com.cmtech.android.btdeviceapp.util.Uuid;
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
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

public class EcgMonitorDevice extends BleDevice {
    // 常量
    private static final int DEFAULT_SAMPLERATE = 125;           // 缺省ECG信号采样率,Hz

    // 消息常量
    private static final int MSG_ECGDATA = 1;           // ECG数据
    private static final int MSG_READSAMPLERATE = 2;    // 读采样率
    private static final int MSG_READLEADTYPE = 3;      // 读导联类型
    private static final int MSG_STARTSAMPLEECG = 4;    // 启动采样

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
    private EcgLeadType leadType = EcgLeadType.LEAD_I;      // 导联类型
    private boolean isCalibrating = true;                  // 是否处于1mV标定阶段
    private ArrayList<Integer> calibrationData = new ArrayList<Integer>(250);   // 用于保存标定用的数据

    private boolean isStartSampleEcg = false;        // 是否开始采样心电信号
    private boolean isRecord = false;                // 是否记录心电信号
    private boolean isFilter = false;                // 是否对信号滤波

    private BmeFileHead ecgFileHead = null;         // 用于保存心电信号的BmeFile文件头，为了能在Windows下读取文件，使用BmeFileHead10版本，LITTLE_ENDIAN，数据类型为INT32
    private BmeFile ecgFile = null;                 // 用于保存心电信号的BmeFile文件对象

    private IIRFilter dcBlock = null;               // 隔直滤波器
    private IIRFilter notch = null;                 // 50Hz陷波器

    // 用于设置EcgWaveView的参数
    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    // 下面两个参数可用来计算View中的xRes和yRes
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV

    private final EcgMonitorInitialState initialState = new EcgMonitorInitialState(this);
    private final EcgMonitorCalibratingState calibratingState = new EcgMonitorCalibratingState(this);
    private final EcgMonitorCalibratedState calibratedState = new EcgMonitorCalibratedState(this);
    private final EcgMonitorSamplingState samplingState = new EcgMonitorSamplingState(this);

    private IEcgMonitorState state = initialState;



    public EcgMonitorDevice(BleDeviceBasicInfo basicInfo) {
        super(basicInfo);
    }

    public EcgMonitorInitialState getInitialState() {
        return initialState;
    }

    public EcgMonitorCalibratingState getCalibratingState() {
        return calibratingState;
    }

    public EcgMonitorCalibratedState getCalibratedState() {
        return calibratedState;
    }

    public EcgMonitorSamplingState getSamplingState() {
        return samplingState;
    }

    public void setState(IEcgMonitorState state) {
        this.state = state;
    }

    @Override
    public void initializeAfterConstruction() {
    }

    @Override
    public void executeAfterConnectSuccess() {

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


        // 启动1mV数据采集
        isCalibrating = true;
        isStartSampleEcg = false;
        addWriteCommand(ECGMONITORCTRL, (byte)0x02, null);
    }

    @Override
    public void executeAfterDisconnect() {

    }

    @Override
    public void executeAfterConnectFailure() {

    }

    @Override
    public synchronized void processGattCallbackMessage(Message msg)
    {
        switch (msg.what) {
            // 接收到心电信号或定标信号
            case MSG_ECGDATA:
                if(msg.obj != null) {
                    byte[] data = (byte[]) msg.obj;
                    // 单片机发过来的是LITTLE_ENDIAN的数据
                    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                    // 单片机发过来的int是两个字节的short
                    for(int i = 0; i < data.length/2; i++) {
                        int tmpData = buffer.getShort();

                        // 如果在标定阶段
                        if (isCalibrating) {
                            // 采集1个周期的定标信号
                            if (calibrationData.size() < sampleRate)
                                calibrationData.add(tmpData);
                            else {
                                // 计算1mV定标信号值
                                int value1mV = calculateCalibration(calibrationData);
                                calibrationData.clear();
                                //tvEcg1mV.setText("" + value1mV);

                                // 启动ECG View
                                int xRes = Math.round(viewGridWidth / (viewXGridTime * sampleRate));   // 计算横向分辨率
                                float yRes = value1mV * viewYGridmV / viewGridWidth;                     // 计算纵向分辨率
                                //ecgView.setRes(xRes, yRes);
                                //ecgView.setGridWidth(viewGridWidth);
                                //ecgView.setZeroLocation(0.5);
                                //ecgView.startShow();

                                // 发送开始采样消息
                                Message msg1 = new Message();
                                msg1.what = MSG_STARTSAMPLEECG;
                                handler.sendMessage(msg1);
                            }
                        } else {
                            if(isFilter)
                                tmpData = (int)notch.filter(dcBlock.filter(tmpData));

                            //ecgView.addData(tmpData);
                            if(isRecord) {
                                try {
                                    ecgFile.writeData(tmpData);
                                } catch (FileException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;

            // 接收到采样率数据
            case MSG_READSAMPLERATE:
                if(msg.obj != null) {
                    sampleRate = (Integer) msg.obj;
                    //tvEcgSampleRate.setText(""+sampleRate);
                }
                break;

            // 接收到导联类型数据
            case MSG_READLEADTYPE:
                if(msg.obj != null) {
                    leadType = EcgLeadType.getFromCode((Integer) msg.obj);
                    //tvEcgLeadType.setText(leadType.getDescription());
                }
                break;

            // 启动采样心电信号
            case MSG_STARTSAMPLEECG:

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

                // 准备隔直滤波器
                dcBlock = DCBlockDesigner.design(0.06, sampleRate);   // 设计隔直滤波器
                dcBlock.createStructure(StructType.IIR_DCBLOCK);            // 创建隔直滤波器专用结构
                // 准备陷波器
                notch = NotchDesigner.design(50, 0.5, sampleRate);
                notch.createStructure(StructType.IIR_NOTCH);

                //btnEcgStartandStop.setClickable(true);
                //cbEcgRecord.setClickable(true);
                //cbEcgFilter.setClickable(true);
                // 启动采样心电信号
                switchSampleEcg();
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
                File toFile = FileUtil.getFile(MyApplication.getContext().getExternalFilesDir("ecgSignal"), "chenm.bme");
                try {
                    String fileName = toFile.getCanonicalPath();
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


    public synchronized void switchSampleEcg() {
        if(!isStartSampleEcg) {
            startSampleEcg();
            //btnEcgStartandStop.setImageDrawable(getResources().getDrawable(R.mipmap.ic_ecg_pause_48px));
            isStartSampleEcg = true;
        } else {
            stopSampleData();
            //btnEcgStartandStop.setImageDrawable(getResources().getDrawable(R.mipmap.ic_ecg_play_48px));
            isStartSampleEcg = false;
        }
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

    // 启动ECG信号采集
    public void startSampleEcg() {
        addWriteCommand(ECGMONITORCTRL, (byte)0x01, null);
    }

    // 启动1mV信号采集
    public void startSample1mV() {
        addWriteCommand(ECGMONITORCTRL, (byte)0x02, null);
    }

    // 停止ECG数据采集
    public void stopSampleData() {
        addWriteCommand(ECGMONITORCTRL, (byte)0x00, null);
    }



    private int calculateCalibration(ArrayList<Integer> data) {
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

}

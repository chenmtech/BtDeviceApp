package com.cmtech.android.bledevice.hrm.model;

import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.DEFAULT_RECORD_VER;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.ECG;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.HR;
import static com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmConstant.AFIB_LABEL;
import static com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmConstant.RHYTHM_DESC_MAP;
import static com.cmtech.android.bledeviceapp.global.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.MY_BASE_UUID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.STANDARD_BLE_UUID;
import static com.cmtech.android.bledeviceapp.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

import android.content.Context;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.DeviceConnectState;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.record.BleHrRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordFactory;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRealTimeRhythmDetector11;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmDetectItem;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.IEcgRealTimeRhythmDetector;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.ThreadUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import ai.onnxruntime.OrtException;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrm.model
 * ClassName:      HrmDevice
 * Description:    心率计设备类
 * Author:         chenm
 * CreateDate:     2020-02-04 06:16
 * UpdateUser:     chenm
 * UpdateDate:     2020-02-04 06:16
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HrmDevice extends AbstractDevice {
    //----------------------------------------------------常量
    // 心率记录最短时间：秒。在记录心率时，如果时长小于该值，就不会保存
    private static final int HR_RECORD_SHORTEST_SECOND = 5;

    // 缺省1mV电压对应的ADU单位值，称为gain
    private static final int DEFAULT_GAIN = 164;

    // 缺省采样率: Hz
    private static final int DEFAULT_SAMPLE_RATE = 250;

    // 缺省心电导联
    private static final EcgLeadType DEFAULT_LEAD_TYPE = EcgLeadType.LEAD_I;

    // 设备工作模式：心率模式
    private static final byte HR_MODE = (byte)0x00;

    // 设备工作模式：心电模式
    private static final byte ECG_MODE = (byte)0x01;

    // 心律检测模型资源
    private static final int RHYTHM_DETECT_MODEL = R.raw.model11_keras;

    //--------------------------------------------设备用到的蓝牙相关常量
    // 心率测量的服务和特征值UUID字符串，数值见蓝牙相关协议文档
    public static final String hrMonitorServiceUuid = "180D"; // standart ble heart rate service UUID
    private static final String hrMonitorMeasUuid = "2A37"; // 心率测量特征UUID
    private static final String hrMonitorSensLocUuid = "2A38"; // 测量位置UUID
    private static final String hrMonitorCtrlPtUuid = "2A39"; // 控制点UUID

    // 心率测量的服务和特征值UUID
    private static final UUID hrMonitorServiceUUID = UuidUtil.stringToUUID(hrMonitorServiceUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorMeasUUID = UuidUtil.stringToUUID(hrMonitorMeasUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorSensLocUUID = UuidUtil.stringToUUID(hrMonitorSensLocUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorCtrlPtUUID = UuidUtil.stringToUUID(hrMonitorCtrlPtUuid, STANDARD_BLE_UUID);

    // 心率测量GATT Element, 见蓝牙相关协议文档
    private static final BleGattElement HRMONITORMEAS =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorMeasUUID, null, "heart rate measurement");
    private static final BleGattElement HRMONITORMEASCCC =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorMeasUUID, CCC_UUID, "heart rate measurement CCC");
    private static final BleGattElement HRMONITORSENSLOC =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorSensLocUUID, null, "sensor location");
    private static final BleGattElement HRMONITORCTRLPT =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorCtrlPtUUID, null, "control point for energy expire");

    // 电池服务和特征值UUID字符串，数值见蓝牙相关协议文档
    private static final String battServiceUuid = "180F";
    private static final String battLevelUuid = "2A19";

    // 电池服务和特征值UUID
    private static final UUID battServiceUUID = UuidUtil.stringToUUID(battServiceUuid, STANDARD_BLE_UUID);
    private static final UUID battLevelUUID = UuidUtil.stringToUUID(battLevelUuid, STANDARD_BLE_UUID);

    // 电池GATT Element, 见蓝牙相关协议文档
    private static final BleGattElement BATTLEVEL = new BleGattElement(battServiceUUID, battLevelUUID, null, "Battery Level");
    private static final BleGattElement BATTLEVELCCC = new BleGattElement(battServiceUUID, battLevelUUID, CCC_UUID, "Battery Level CCC");

    // 心电服务和特征值，该服务和特征值由我自己定义
    private static final String ecgServiceUuid = "AA40";
    private static final String ecgMeasUuid = "AA41";
    private static final String ecgGainUuid = "AA42";
    private static final String ecgSampleRateUuid = "AA43";
    private static final String ecgLeadTypeUuid = "AA44";
    private static final String modeStatusUuid = "AA45";

    private static final UUID ecgServiceUUID = UuidUtil.stringToUUID(ecgServiceUuid, MY_BASE_UUID);
    private static final UUID ecgMeasUUID = UuidUtil.stringToUUID(ecgMeasUuid, MY_BASE_UUID);
    private static final UUID ecgGainUUID = UuidUtil.stringToUUID(ecgGainUuid, MY_BASE_UUID);
    private static final UUID ecgSampleRateUUID = UuidUtil.stringToUUID(ecgSampleRateUuid, MY_BASE_UUID);
    private static final UUID ecgLeadTypeUUID = UuidUtil.stringToUUID(ecgLeadTypeUuid, MY_BASE_UUID);
    private static final UUID modeStatusUUID = UuidUtil.stringToUUID(modeStatusUuid, MY_BASE_UUID);

    private static final BleGattElement ECGMEAS = new BleGattElement(ecgServiceUUID, ecgMeasUUID, null, "ECG Data Packet");
    private static final BleGattElement ECGMEASCCC = new BleGattElement(ecgServiceUUID, ecgMeasUUID, CCC_UUID, "ECG Data Packet CCC");
    private static final BleGattElement ECGGAIN = new BleGattElement(ecgServiceUUID, ecgGainUUID, null, "ECG 1mV ADU, Gain");
    private static final BleGattElement ECGSAMPLERATE = new BleGattElement(ecgServiceUUID, ecgSampleRateUUID, null, "ECG Sample Rate");
    private static final BleGattElement ECGLEADTYPE = new BleGattElement(ecgServiceUUID, ecgLeadTypeUUID, null, "ECG Lead Type");
    private static final BleGattElement MODESTATUS = new BleGattElement(ecgServiceUUID, modeStatusUUID, null, "Work Mode Status");

    //------------------------------------------------------实例变量
    // 采样率，单位Hz
    private int sampleRate = DEFAULT_SAMPLE_RATE;

    // 增益，gain
    private int gain = DEFAULT_GAIN; // 1mV ADU

    // ECG导联类型
    private EcgLeadType leadType = DEFAULT_LEAD_TYPE;

    // 该设备是否包含电池服务
    private boolean battService = false;

    // 设备是否工作在心率模式
    private boolean hrMode = true;

    // HR心率记录状态
    private boolean hrRecordStatus = false;

    // ECG信号记录状态
    private boolean ecgRecordStatus = false;

    // 心电功能是否已启动
    private boolean ecgOn = false;

    // HRM设备配置
    private final HrmCfg config;

    // 心率播报器
    private final HRSpeaker speaker = new HRSpeaker();

    // ECG数据包解析器
    private EcgDataPacketParser ecgDataPacketParser;

    // 心率记录实例
    private BleHrRecord hrRecord;

    // 心电信号记录实例
    private BleEcgRecord ecgRecord;

    // HRM设备监听器
    private OnHrmListener listener;

    // 心律实时检测器
    private IEcgRealTimeRhythmDetector rhythmDetector;

    // 每次当设备断开后重新连接，需要插入的信号零值个数
    private int insertZeroWhenReconnect = 2*DEFAULT_SAMPLE_RATE;

    //-----------------------------------------------静态类
    // 心率播报器类
    private static class HRSpeaker {
        private volatile boolean on = false;
        private long speakPeriod = 0; // 播报周期，ms
        private volatile long lastSpeakTime = 0; // 上次播报时间

        public void start(int periodS) {
            speakPeriod = periodS*60000L;
            lastSpeakTime = new Date().getTime();
            on = true;
        }

        public void stop() {
            on = false;
        }

        public void speak(int hr) {
            if(on) {
                long currentTime = new Date().getTime();
                if ((currentTime - lastSpeakTime) > speakPeriod) {
                    String currentHr = MyApplication.getStr(R.string.current_hr) + hr;
                    String currentStr = "现在时间" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "点"
                            + Calendar.getInstance().get(Calendar.MINUTE) + "分";
                    MyApplication.getTts().speak(currentHr + currentStr);
                    lastSpeakTime = currentTime;
                    ViseLog.e("speak: " + hr);
                }
            }
        }
    }

    //---------------------------------------------------------构造器
    /**
     * HRM设备构造器
     * @param context：上下文
     * @param registerInfo：设备的注册信息
     */
    public HrmDevice(Context context, DeviceCommonInfo registerInfo) {
        super(context, registerInfo);

        // 获取或产生设备配置
        HrmCfg config = LitePal.where("address = ?", getAddress()).findFirst(HrmCfg.class);
        if (config == null) {
            config = new HrmCfg();
            config.setAddress(getAddress());
            config.save();
        }
        this.config = config;
    }

    //---------------------------------------------------公有方法

    /**
     * 设置心电记录的备注信息
     * @param comment 备注
     */
    public void setEcgRecordComment(String comment) {
        if(ecgRecordStatus && ecgRecord != null) {
            ecgRecord.setComment(comment);
            ViseLog.e(comment);
        }
    }

    /**
     * 设置心率记录状态
     * @param record: 是否开始记录。true-开始记录，false-停止记录
     */
    public void setHrRecord(boolean record) {
        if(this.hrRecordStatus == record) return;

        this.hrRecordStatus = record;
        // 开始记录
        if(record) {
            hrRecord = (BleHrRecord) RecordFactory.create(HR, DEFAULT_RECORD_VER, MyApplication.getAccountId(), new Date().getTime(), getAddress());
            if(listener != null && hrRecord != null) {
                recordingRecord = hrRecord;
                listener.onHRStatisticInfoUpdated(hrRecord);
                Toast.makeText(getContext(), R.string.start_record, Toast.LENGTH_SHORT).show();
            }
        }
        // 停止记录
        else {
            if(hrRecord != null) {
                // 心率记录时长不够
                if (hrRecord.getHrList().size() < HR_RECORD_SHORTEST_SECOND) {
                    Toast.makeText(getContext(), R.string.record_too_short, Toast.LENGTH_SHORT).show();
                }
                // 记录时长已够
                else {
                    // 生成心率区间直方图
                    hrRecord.getHrHist().clear();
                    for(int i = 0; i < hrRecord.getHrHistogram().size(); i++) {
                        hrRecord.getHrHist().add(hrRecord.getHrHistogram().get(i).getValue());
                    }

                    // 获取总的记录时长
                    int sum = 0;
                    for(int num : hrRecord.getHrHist()) {
                        sum += num;
                    }

                    hrRecord.setSigSecond(sum);
                    hrRecord.save();
                    Toast.makeText(getContext(), R.string.save_record_success, Toast.LENGTH_SHORT).show();
                }
                recordingRecord = null;
            }
        }

        /*if(hrRecord != null) {
            ViseLog.e(hrRecord);
        }*/

        // 通知监听器更新心率记录状态
        if(listener != null) {
            listener.onHRRecordStatusUpdated(this.hrRecordStatus);
        }
    }

    /**
     * 设置ECG信号记录状态
     * @param record: 是否开始记录。true-开始记录，false-停止记录
     */
    public void setEcgRecord(boolean record) {
        if(this.ecgRecordStatus == record) return;

        // 当请求开始记录心电，但是设备的心电功能没有启动，则不允许开始记录心电
        if(record && !ecgOn) {
            ThreadUtil.showToastInMainThread(getContext(), R.string.pls_turn_on_ecg_firstly, Toast.LENGTH_SHORT);
            if(listener != null) {
                listener.onEcgSignalRecordStatusUpdated(false);
            }
            return;
        }

        // 开始记录
        if(record) {
            ecgRecord = (BleEcgRecord) RecordFactory.create(ECG, DEFAULT_RECORD_VER, MyApplication.getAccountId(), new Date().getTime(), getAddress());
            if(ecgRecord != null) {
                try {
                    ecgRecord.createSigFile();
                } catch (IOException e) {
                    ecgRecord = null;
                    ThreadUtil.showToastInMainThread(getContext(), "创建记录失败", Toast.LENGTH_SHORT);
                    return;
                }
                ecgRecord.setSampleRate(sampleRate);
                ecgRecord.setGain(gain);
                ecgRecord.setLeadTypeCode(leadType.getCode());
                ecgRecord.setInterrupt(true);
                ecgRecord.save();
                recordingRecord = ecgRecord;
                ThreadUtil.showToastInMainThread(getContext(), R.string.pls_be_quiet_when_record, Toast.LENGTH_SHORT);

                if(rhythmDetector != null)
                    rhythmDetector.reset();
            }
            this.ecgRecordStatus = record;
        }
        // 停止记录
        else {
            this.ecgRecordStatus = record;

            if(ecgRecord != null) {
                int second = ecgRecord.getDataNum() / ecgRecord.getSampleRate();

                ecgRecord.setSigSecond(second);
                ecgRecord.createReport(rhythmDetector);
                ecgRecord.closeSigFile();
                ecgRecord.save();
                recordingRecord = null;
                ThreadUtil.showToastInMainThread(getContext(), R.string.save_record_success, Toast.LENGTH_SHORT);
            }
        }

        // 通知监听器，心电信号记录状态已更新
        if(listener != null) {
            listener.onEcgSignalRecordStatusUpdated(this.ecgRecordStatus);
        }
    }

    /**
     * 设置设备工作模式
     * @param isHrMode: 设置是否工作于心率模式，true-设置心率模式，false-设置心电模式
     */
    public void setMode(final boolean isHrMode) {
        if(this.hrMode == isHrMode) return;

        byte data = (isHrMode) ? HR_MODE : ECG_MODE;

        ((BleConnector) connector).write(MODESTATUS, data, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                HrmDevice.this.hrMode = isHrMode;
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    /**
     * 设置是否启动心电功能
     * @param ecgOn：是否启动心电功能，true-启动，false-停止
     */
    public void setEcgOn(boolean ecgOn) {
        // 如果设备工作在心率模式，则操作无效
        if(hrMode) {
            if(listener != null)
                listener.onEcgOnStatusUpdated(false);
            return;
        }

        // 如果想停止心电，但是此时正在记录心电，则操作无效
        if(!ecgOn && ecgRecordStatus) {
            ThreadUtil.showToastInMainThread(getContext(), R.string.pls_stop_record_firstly, Toast.LENGTH_SHORT);
            if(listener != null) listener.onEcgOnStatusUpdated(true);
            return;
        }

        this.ecgOn = ecgOn;
        // 启动心电功能
        if(ecgOn) {
            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    ecgDataPacketParser.parse(data);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            };
            ((BleConnector)connector).notify(ECGMEASCCC, true, notifyCallback);
        }
        // 停止心电功能
        else {
            if(ecgDataPacketParser != null)
                ecgDataPacketParser.stop();

            ((BleConnector)connector).notify(ECGMEASCCC, false, null);
        }

        // 通知监听器，更新心电功能状态
        if(listener != null) {
            listener.onEcgOnStatusUpdated(this.ecgOn);
        }
    }

    /**
     * 显示一个心电信号值
     * @param ecgSignal：一个心电信号值
     */
    private void showEcgSignal(int ecgSignal) {
        if(MyApplication.isRunInForeground()) {
            if (listener != null) {
                listener.onEcgSignalShowed(ecgSignal);
            }
        }
    }

    /**
     * 处理一个心电信号值，包括显示信号、记录信号值和进行心电信号异常检测等处理
     * @param ecgSignal：一个心电信号值
     */
    public void processEcgSignal(int ecgSignal) {
        if(insertZeroWhenReconnect != 0) {
            ecgSignal = 0;
            insertZeroWhenReconnect--;
        }

        // 显示信号
        showEcgSignal(ecgSignal);

        // 记录信号
        if(ecgRecordStatus && ecgRecord != null) {
            ecgRecord.record((short)ecgSignal);

            // 每记录一秒钟，就修改一次心电记录时间值
            if(ecgRecord.getDataNum() % sampleRate == 0 && listener != null) {
                int second = ecgRecord.getDataNum()/sampleRate;
                listener.onEcgRecordTimeUpdated(second);

                // 每记录一分钟就自动保存一次记录，防止数据异常丢失太多
                if(second % 60 == 0) {
                    ecgRecord.setSigSecond(second);
                    ecgRecord.save();
                }
            }
        }

        // 心律检测
        if(rhythmDetector != null) {
            rhythmDetector.process((float)ecgSignal/gain);
        }

        /*
        if(qrsDetector != null) {
            int rrInterval = qrsDetector.outputRRInterval(ecgSignal);

            if(rhythmDetector != null && rrInterval != 0) {
                ViseLog.e("RRInterval:" + rrInterval);
                Random random = new Random();
                float[][] d = new float[1][64];
                for (int i = 0; i < 64; i++)
                    d[0][i] = random.nextFloat();
                try {
                    OnnxTensor t = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), d);
                    Map<String, OnnxTensor> inputs = new HashMap<>();
                    inputs.put("input_x", t);
                    OrtSession.Result rlt = rhythmDetector.run(inputs);
                    long[] output = (long[]) rlt.get(0).getValue();
                    ViseLog.e("output:"+output[0]);
                } catch (OrtException e) {
                    e.printStackTrace();
                }
            }
        }
        */
    }

    /**
     * 更新心律检测条目
     * @param rhythmItem 一条心律异常检测条目
     */
    private void updateRhythmDetectItem(EcgRhythmDetectItem rhythmItem) {
        if(MyApplication.isRunInForeground()) {
            if (listener != null) {
                int label = rhythmItem.getLabel();
                listener.onEcgRhythmDetectInfoUpdated(label, RHYTHM_DESC_MAP.get(label));
                if(label == AFIB_LABEL) {
                    MyApplication.getTts().speak("发现房颤");
                }
            }
        }

        if(ecgRecordStatus && ecgRecord != null) {
            ecgRecord.addRhythmItem(rhythmItem);
        }
    }

    @Override
    public void open() {
        super.open();
    }

    @Override
    public void close() {
        super.close();

        if(hrRecordStatus) {
            setHrRecord(false);
        }

        if(ecgRecordStatus) {
            setEcgRecord(false);
        }

        if(speaker != null)
            speaker.stop();

        if(rhythmDetector != null) {
            rhythmDetector.destroy();
            rhythmDetector = null;
        }
    }

    @Override
    public boolean onConnectSuccess() {
        BleConnector connector = (BleConnector)this.connector;

        BleGattElement[] elements = new BleGattElement[]{HRMONITORMEAS, HRMONITORMEASCCC};
        if(connector.containGattElements(elements)) {
            readSensorLocation();
            setHRMeasure(true);
        } else {
            return false;
        }

        elements = new BleGattElement[]{BATTLEVEL, BATTLEVELCCC};
        if(connector.containGattElements(elements)) {
            battService = true;
            readBatteryLevel();
            setBatteryMeasure(true);
        }

        elements = new BleGattElement[]{ECGMEAS, ECGMEASCCC, ECGGAIN, ECGSAMPLERATE, ECGLEADTYPE, MODESTATUS};
        if(connector.containGattElements(elements)) {
            readModeStatus();
            connector.runInstantly(new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    if(hrMode) {
                        if (listener != null)
                            listener.onUIUpdated(sampleRate, gain, DEFAULT_ZERO_LOCATION, true);

                        setEcgOn(false);
                    }
                    else {
                        initEcgService();
                        setEcgOn(true);
                    }
                }

                @Override
                public void onFailure(BleException exception) {

                }
            });
        }

        if(config.isSpeak())
            speaker.start(config.getSpeakPeriod());

        return true;
    }

    @Override
    public void onConnectFailure() {
        if(ecgDataPacketParser != null) {
            ecgDataPacketParser.stop();
        }

        speaker.stop();

        if(ecgRecordStatus && ecgRecord != null) {
            ecgRecord.setInterrupt(true);
        }
        //setEcgRecord(false);
        //setEcgOn(false);
    }

    @Override
    public void onDisconnect() {
        if(ecgDataPacketParser != null) {
            ecgDataPacketParser.stop();
        }

        speaker.stop();

        if(ecgRecordStatus && ecgRecord != null) {
            ecgRecord.setInterrupt(true);
        }
        //setEcgRecord(false);

        //setEcgOn(false);
    }

    @Override
    public void disconnect(final boolean forever) {
        setHRMeasure(false);
        setBatteryMeasure(false);

        //setEcgOn(false);
        ((BleConnector)connector).runInstantly(new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                HrmDevice.super.disconnect(forever);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
        super.disconnect(forever);
    }

    public BleHrRecord getHrRecord() {
        return hrRecord;
    }

    public final int getSampleRate() {
        return sampleRate;
    }

    public final int getGain() {
        return gain;
    }

    public final boolean isHrMode() {
        return hrMode;
    }

    public final HrmCfg getConfig() {
        return config;
    }

    public void updateConfig(HrmCfg config) {
        boolean isSpeakChanged = (this.config.isSpeak() != config.isSpeak() || this.config.getSpeakPeriod() != config.getSpeakPeriod());
        this.config.copyFrom(config);
        this.config.save();
        if(isSpeakChanged && getConnectState() == DeviceConnectState.CONNECT) {
            if(this.config.isSpeak())
                speaker.start(this.config.getSpeakPeriod());
            else {
                speaker.stop();
            }
        }
    }

    public void setListener(OnHrmListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }


    private void readSensorLocation() {
        ((BleConnector)connector).read(HRMONITORSENSLOC, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                if(listener != null) {
                    listener.onHRSensLocUpdated(data[0]);
                }
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void setHRMeasure(boolean start) {
        if(start) {
            ((BleConnector)connector).notify(HRMONITORMEASCCC, false, null);

            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    try {
                        BleHeartRateData heartRateData = new BleHeartRateData(data);

                        int bpm = heartRateData.getBpm();

                        if(ecgRecordStatus && ecgRecord != null) {
                            ecgRecord.addOneHr(bpm);
                        }

                        if(config.needWarn()) {
                            if(bpm > config.getHrHigh())
                                MyApplication.getTts().speak(R.string.hr_too_high);
                            else if(bpm < config.getHrLow()) {
                                MyApplication.getTts().speak(R.string.hr_too_low);
                            }
                        }

                        String currentHr = MyApplication.getStr(R.string.current_hr) + bpm;
                        setNotificationInfo(currentHr);
                        speaker.speak(bpm);

                        boolean hrStatisticUpdated = (hrRecordStatus && hrRecord.record((short) bpm, heartRateData.getTime()));
                        if (MyApplication.isRunInForeground()) {
                            if (listener != null) {
                                listener.onHRUpdated(heartRateData);

                                if (hrStatisticUpdated) {
                                    listener.onHRStatisticInfoUpdated(hrRecord);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(BleException exception) {

                }
            };
            ((BleConnector)connector).notify(HRMONITORMEASCCC, true, notifyCallback);
        } else {
            ((BleConnector)connector).notify(HRMONITORMEASCCC, false, null);
        }

    }

    private void setBatteryMeasure(boolean start) {
        if(!battService) return;

        //((BleConnector)connector).notify(BATTLEVELCCC, false, null);
        if(start) {
            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    setBatteryLevel(data[0]);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            };
            ((BleConnector)connector).notify(BATTLEVELCCC, true, notifyCallback);
        } else {
            ((BleConnector)connector).notify(BATTLEVELCCC, false, null);
        }
    }

    private void readBatteryLevel() {
        ((BleConnector)connector).read(BATTLEVEL, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                setBatteryLevel(data[0]);
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void readModeStatus() {
        ((BleConnector)connector).read(MODESTATUS, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                hrMode = (data[0] == HR_MODE);
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    /**
     * 初始化心电服务
     * 当设备连接成功后，会检测设备的工作模式是否是心电模式。如果是心电模式，就会调用此函数
     */
    private void initEcgService() {
        // 读采样率
        readSampleRate();

        // 读增益值
        readGain();

        // 读导联类型
        readLeadType();

        ((BleConnector)connector).runInstantly(new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                // 生成并启动心电数据处理器
                ecgDataPacketParser = new EcgDataPacketParser(HrmDevice.this);
                ecgDataPacketParser.start();

                // 生成QRS波检测器
                //qrsDetector = new QrsDetector(sampleRate);

                // 启动心律检测器
                if(rhythmDetector == null) {
                    try {
                        //rhythmDetector = new EcgRealTimeRhythmDetector(RHYTHM_DETECT_MODEL, item -> updateRhythmDetectItem(item));
                        rhythmDetector = new EcgRealTimeRhythmDetector11(RHYTHM_DETECT_MODEL, item -> updateRhythmDetectItem(item));
                    } catch (OrtException e) {
                        rhythmDetector = null;
                        ThreadUtil.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "心律检测模型加载失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    rhythmDetector.reset();
                }

                // 更新设备监听器
                if (listener != null)
                    listener.onUIUpdated(sampleRate, gain, DEFAULT_ZERO_LOCATION, hrMode);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    private void readSampleRate() {
        ((BleConnector)connector).read(ECGSAMPLERATE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                sampleRate = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(data));
                insertZeroWhenReconnect = 2*sampleRate;
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void readGain() {
        ((BleConnector)connector).read(ECGGAIN, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                gain = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(data));
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void readLeadType() {
        ((BleConnector)connector).read(ECGLEADTYPE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                leadType = EcgLeadType.getFromCode(UnsignedUtil.getUnsignedByte(data[0]));
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }
}

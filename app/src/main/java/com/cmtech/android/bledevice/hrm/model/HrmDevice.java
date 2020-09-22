package com.cmtech.android.bledevice.hrm.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.DeviceConnectState;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledevice.record.BleHrRecord10;
import com.cmtech.android.bledevice.record.RecordFactory;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.util.Date;
import java.util.UUID;

import static com.cmtech.android.bledevice.record.RecordType.ECG;
import static com.cmtech.android.bledevice.record.RecordType.HR;
import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;
import static com.cmtech.android.bledeviceapp.global.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.MY_BASE_UUID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.STANDARD_BLE_UUID;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      HRMonitorDevice
 * Description:    BLE heart rate monitor device
 * Author:         chenm
 * CreateDate:     2020-02-04 06:16
 * UpdateUser:     chenm
 * UpdateDate:     2020-02-04 06:16
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HrmDevice extends AbstractDevice {
    public static final short INVALID_HEART_RATE = -1;
    private static final int ECG_RECORD_MAX_SECOND = 30;
    private static final int RECORD_MIN_SECOND = 5;

    private static final int DEFAULT_CALI_1MV = 164; // default 1mV calibration value
    private static final int DEFAULT_SAMPLE_RATE = 125; // default sample rate, unit: Hz
    private static final EcgLeadType DEFAULT_LEAD_TYPE = EcgLeadType.LEAD_I; // default lead type

    private static final byte HR_MODE = (byte)0x00; // heart rate mode
    private static final byte ECG_MODE = (byte)0x01; // ecg mode

    // heart rate measurement service
    private static final String hrMonitorServiceUuid = "180D"; // standart ble heart rate service UUID
    private static final String hrMonitorMeasUuid = "2A37"; // 心率测量特征UUID
    private static final String hrMonitorSensLocUuid = "2A38"; // 测量位置UUID
    private static final String hrMonitorCtrlPtUuid = "2A39"; // 控制点UUID

    private static final UUID hrMonitorServiceUUID = UuidUtil.stringToUUID(hrMonitorServiceUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorMeasUUID = UuidUtil.stringToUUID(hrMonitorMeasUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorSensLocUUID = UuidUtil.stringToUUID(hrMonitorSensLocUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorCtrlPtUUID = UuidUtil.stringToUUID(hrMonitorCtrlPtUuid, STANDARD_BLE_UUID);

    private static final BleGattElement HRMONITORMEAS =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorMeasUUID, null, "heart rate measurement");
    private static final BleGattElement HRMONITORMEASCCC =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorMeasUUID, CCC_UUID, "heart rate measurement CCC");
    private static final BleGattElement HRMONITORSENSLOC =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorSensLocUUID, null, "sensor location");
    private static final BleGattElement HRMONITORCTRLPT =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorCtrlPtUUID, null, "control point for energy expire");

    // battery service
    private static final String battServiceUuid = "180F";
    private static final String battLevelUuid = "2A19";
    private static final UUID battServiceUUID = UuidUtil.stringToUUID(battServiceUuid, STANDARD_BLE_UUID);
    private static final UUID battLevelUUID = UuidUtil.stringToUUID(battLevelUuid, STANDARD_BLE_UUID);
    private static final BleGattElement BATTLEVEL = new BleGattElement(battServiceUUID, battLevelUUID, null, "Battery Level");
    private static final BleGattElement BATTLEVELCCC = new BleGattElement(battServiceUUID, battLevelUUID, CCC_UUID, "Battery Level CCC");

    // ecg service
    private static final String ecgServiceUuid = "AA40";
    private static final String ecgMeasUuid = "AA41";
    private static final String ecg1mVCaliUuid = "AA42";
    private static final String ecgSampleRateUuid = "AA43";
    private static final String ecgLeadTypeUuid = "AA44";
    private static final String modeStatusUuid = "AA45";
    private static final UUID ecgServiceUUID = UuidUtil.stringToUUID(ecgServiceUuid, MY_BASE_UUID);
    private static final UUID ecgMeasUUID = UuidUtil.stringToUUID(ecgMeasUuid, MY_BASE_UUID);
    private static final UUID ecg1mVCaliUUID = UuidUtil.stringToUUID(ecg1mVCaliUuid, MY_BASE_UUID);
    private static final UUID ecgSampleRateUUID = UuidUtil.stringToUUID(ecgSampleRateUuid, MY_BASE_UUID);
    private static final UUID ecgLeadTypeUUID = UuidUtil.stringToUUID(ecgLeadTypeUuid, MY_BASE_UUID);
    private static final UUID modeStatusUUID = UuidUtil.stringToUUID(modeStatusUuid, MY_BASE_UUID);

    private static final BleGattElement ECGMEAS = new BleGattElement(ecgServiceUUID, ecgMeasUUID, null, "ECG Data Packet");
    private static final BleGattElement ECGMEASCCC = new BleGattElement(ecgServiceUUID, ecgMeasUUID, CCC_UUID, "ECG Data Packet CCC");
    private static final BleGattElement ECG1MVCALI = new BleGattElement(ecgServiceUUID, ecg1mVCaliUUID, null, "ECG 1mV Calibration");
    private static final BleGattElement ECGSAMPLERATE = new BleGattElement(ecgServiceUUID, ecgSampleRateUUID, null, "ECG Sample Rate");
    private static final BleGattElement ECGLEADTYPE = new BleGattElement(ecgServiceUUID, ecgLeadTypeUUID, null, "ECG Lead Type");
    private static final BleGattElement MODESTATUS = new BleGattElement(ecgServiceUUID, modeStatusUUID, null, "Work Mode Status");


    private int sampleRate = DEFAULT_SAMPLE_RATE; // sample rate
    private int caliValue = DEFAULT_CALI_1MV; // 1mV calibration value
    private EcgLeadType leadType = DEFAULT_LEAD_TYPE; // lead type

    private boolean battService = false; // does it include battery service?
    private boolean hrMode = true; // does it work in HR Mode?
    private boolean hrRecording = false; // is HR being recorded
    private boolean ecgRecording = false; // is ECG being recorded
    private boolean ecgOn = false; // is ecg function on

    private final HrmCfg config; // HRM device configuration
    private final HRSpeaker speaker = new HRSpeaker(); // HR Speaker

    private EcgDataProcessor ecgProcessor; // ecg signal processor
    private BleHrRecord10 hrRecord; // HR record
    private BleEcgRecord10 ecgRecord; // ECG record

    private OnHrmListener listener; // HRM device listener

    public HrmDevice(Context context, DeviceCommonInfo registerInfo) {
        super(context, registerInfo);
        HrmCfg config = LitePal.where("address = ?", getAddress()).findFirst(HrmCfg.class);
        if (config == null) {
            config = new HrmCfg();
            config.setAddress(getAddress());
            config.save();
        }
        this.config = config;
    }

    public void setHrRecording(boolean hrRecording) {
        if(this.hrRecording == hrRecording) return;

        this.hrRecording = hrRecording;
        if(hrRecording) {
            hrRecord = (BleHrRecord10) RecordFactory.create(HR, new Date().getTime(), getAddress(), MyApplication.getAccount(), "");
            if(listener != null && hrRecord != null) {
                listener.onHRStatisticInfoUpdated(hrRecord);
                Toast.makeText(getContext(), R.string.start_record, Toast.LENGTH_SHORT).show();
            }
        } else {
            if(hrRecord != null) {
                if (hrRecord.getHrList().size() < RECORD_MIN_SECOND) {
                    Toast.makeText(getContext(), R.string.record_too_short, Toast.LENGTH_SHORT).show();
                } else {
                    hrRecord.setCreateTime(new Date().getTime());
                    hrRecord.getHrHist().clear();
                    for(int i = 0; i < hrRecord.getHrHistogram().size(); i++) {
                        hrRecord.getHrHist().add(hrRecord.getHrHistogram().get(i).getValue());
                    }
                    int sum = 0;
                    for(int num : hrRecord.getHrHist()) {
                        sum += num;
                    }
                    hrRecord.setRecordSecond(sum);
                    hrRecord.save();
                    Toast.makeText(getContext(), R.string.save_record_success, Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(hrRecord != null) {
            ViseLog.e(hrRecord);
        }
        if(listener != null) {
            listener.onHrRecordStatusUpdated(this.hrRecording);
        }
    }

    public void setEcgRecording(boolean ecgRecording) {
        if(this.ecgRecording == ecgRecording) return;

        if(ecgRecording && !ecgOn) {
            Toast.makeText(getContext(), R.string.pls_turn_on_ecg_firstly, Toast.LENGTH_SHORT).show();
            if(listener != null) {
                listener.onEcgSignalRecordStatusUpdated(false);
            }
            return;
        }

        this.ecgRecording = ecgRecording;
        if(ecgRecording) {
            ecgRecord = (BleEcgRecord10) RecordFactory.create(ECG, new Date().getTime(), getAddress(), MyApplication.getAccount(), "");
            if(ecgRecord != null) {
                ecgRecord.setSampleRate(sampleRate);
                ecgRecord.setCaliValue(caliValue);
                ecgRecord.setLeadTypeCode(leadType.getCode());
                Toast.makeText(getContext(), R.string.pls_be_quiet_when_record, Toast.LENGTH_SHORT).show();
            }
        } else {
            if(ecgRecord != null) {
                int recordSecond = ecgRecord.getDataNum() / ecgRecord.getSampleRate();
                if (recordSecond < RECORD_MIN_SECOND) {
                    Toast.makeText(getContext(), R.string.record_too_short, Toast.LENGTH_SHORT).show();
                } else {
                    ecgRecord.setCreateTime(new Date().getTime());
                    ecgRecord.setRecordSecond(recordSecond);
                    ecgRecord.save();
                    Toast.makeText(getContext(), R.string.save_record_success, Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(listener != null) {
            listener.onEcgSignalRecordStatusUpdated(this.ecgRecording);
        }
    }

    public void setMode(final boolean workInHrMode) {
        if(this.hrMode == workInHrMode) return;

        byte data = (workInHrMode) ? HR_MODE : ECG_MODE;

        ((BleConnector) connector).write(MODESTATUS, data, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                HrmDevice.this.hrMode = workInHrMode;
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    public void setEcgOn(boolean ecgOn) {
        if(hrMode) {
            if(listener != null)
                listener.onEcgOnStatusUpdated(false);
            return;
        }

        if(ecgRecording && !ecgOn) {
            Toast.makeText(getContext(), R.string.pls_stop_record_firstly, Toast.LENGTH_SHORT).show();
            if(listener != null) listener.onEcgOnStatusUpdated(true);
            return;
        }

        //((BleConnector)connector).notify(ECGMEASCCC, false, null);

        this.ecgOn = ecgOn;
        if(ecgOn) {
            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    //ViseLog.i("ecg data: " + Arrays.toString(data));
                    ecgProcessor.takeData(data);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            };
            ((BleConnector)connector).notify(ECGMEASCCC, true, notifyCallback);
        } else {
            if(ecgProcessor != null)
                ecgProcessor.stop();

            ((BleConnector)connector).notify(ECGMEASCCC, false, null);
        }
        if(listener != null) {
            listener.onEcgOnStatusUpdated(this.ecgOn);
        }
    }

    @Override
    public void open() {
        super.open();
    }

    @Override
    public void close() {
        super.close();

        if(hrRecording) {
            setHrRecording(false);
        }

        if(ecgRecording) {
            setEcgRecording(false);
        }

        if(speaker != null)
            speaker.stop();
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

        elements = new BleGattElement[]{ECGMEAS, ECGMEASCCC, ECG1MVCALI, ECGSAMPLERATE, ECGLEADTYPE, MODESTATUS};
        if(connector.containGattElements(elements)) {
            readModeStatus();
            connector.runInstantly(new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    if(hrMode) {
                        if (listener != null)
                            listener.onFragmentUpdated(sampleRate, caliValue, DEFAULT_ZERO_LOCATION, true);

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
        if(ecgProcessor != null) {
            ecgProcessor.stop();
        }

        speaker.stop();

        setEcgRecording(false);
        //setEcgOn(false);
    }

    @Override
    public void onDisconnect() {
        if(ecgProcessor != null) {
            ecgProcessor.stop();
        }

        speaker.stop();

        setEcgRecording(false);

        //setEcgOn(false);
    }

    @Override
    public void disconnect(final boolean forever) {
        setHRMeasure(false);
        setBatteryMeasure(false);
        setEcgRecording(false);
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

    public final int getSampleRate() {
        return sampleRate;
    }

    public final int getCaliValue() {
        return caliValue;
    }

    public final boolean inHrMode() {
        return hrMode;
    }

    public boolean isEcgOn() {
        return ecgOn;
    }

    public boolean isEcgRecording() {
        return ecgRecording;
    }

    public final boolean isHrRecording() {
        return hrRecording;
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


    private static class HRSpeaker {
        private volatile boolean on = false;
        private long periodMs = 0;
        private volatile long lastSpeakTime = 0;

        public void start(int periodS) {
            periodMs = periodS*60000L;
            lastSpeakTime = new Date().getTime();
            on = true;
        }

        public void stop() {
            on = false;
        }

        public void speak(String hr) {
            if(on) {
                long currentTime = new Date().getTime();
                if ((currentTime - lastSpeakTime) > periodMs) {
                    MyApplication.getTts().speak(hr);
                    lastSpeakTime = currentTime;
                    ViseLog.e("speak: " + hr);
                }
            }
        }
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
                        if(config.needWarn()) {
                            if(bpm > config.getHrHigh())
                                MyApplication.getTts().speak(R.string.hr_too_high);
                            else if(bpm < config.getHrLow()) {
                                MyApplication.getTts().speak(R.string.hr_too_low);
                            }
                        }

                        String currentHr = MyApplication.getStr(R.string.current_hr) + bpm;
                        setNotificationInfo(currentHr);
                        speaker.speak(currentHr);

                        boolean hrStatisticUpdated = (hrRecording && hrRecord.record((short) bpm, heartRateData.getTime()));
                        if (!MyApplication.isRunInBackground()) {
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

    private void initEcgService() {
        readSampleRate();
        read1mVCali();
        readLeadType();
        ((BleConnector)connector).runInstantly(new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                ecgProcessor = new EcgDataProcessor(HrmDevice.this);
                ecgProcessor.start();

                if (listener != null)
                    listener.onFragmentUpdated(sampleRate, caliValue, DEFAULT_ZERO_LOCATION, hrMode);
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
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void read1mVCali() {
        ((BleConnector)connector).read(ECG1MVCALI, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                caliValue = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(data));
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

    public void showEcgSignal(int ecgSignal) {
        if(!MyApplication.isRunInBackground()) {
            if (listener != null) {
                listener.onEcgSignalShowed(ecgSignal);
            }
        }
    }

    public void recordEcgSignal(int ecgSignal) {
        if(ecgRecording && ecgRecord != null) {
            ecgRecord.process((short)ecgSignal);
            if(ecgRecord.getDataNum() % sampleRate == 0 && listener != null) {
                int second = ecgRecord.getDataNum()/sampleRate;
                listener.onEcgRecordTimeUpdated(second);
                if(second >= ECG_RECORD_MAX_SECOND) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            setEcgRecording(false);
                        }
                    });
                }
            }
        }
    }
}

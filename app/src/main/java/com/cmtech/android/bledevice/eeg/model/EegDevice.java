package com.cmtech.android.bledevice.eeg.model;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.eeg.model
 * ClassName:      EegDevice
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/6/11 下午3:28
 * UpdateUser:     更新者
 * UpdateDate:     2020/6/11 下午3:28
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

import android.content.Context;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledevice.record.BleEegRecord10;
import com.cmtech.android.bledevice.record.RecordFactory;
import com.cmtech.android.bledevice.record.RecordWebAsyncTask;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;

import java.util.Date;
import java.util.UUID;

import static com.cmtech.android.bledevice.record.RecordType.EEG;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.CODE_SUCCESS;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.RECORD_UPLOAD_CMD;
import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;
import static com.cmtech.android.bledeviceapp.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.MY_BASE_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.STANDARD_BLE_UUID;

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
public class EegDevice extends AbstractDevice {
    private static final int DEFAULT_CALI_1MV = 40960; // default 1mV calibration value
    private static final int DEFAULT_SAMPLE_RATE = 250; // default sample rate, unit: Hz
    private static final EegLeadType DEFAULT_LEAD_TYPE = EegLeadType.LEAD_I; // default lead type

    // eeg service
    private static final String eegServiceUuid = "AAA0";
    private static final String eegMeasUuid = "AAA1";
    private static final String eeg1mVCaliUuid = "AAA2";
    private static final String eegSampleRateUuid = "AAA3";
    private static final String eegLeadTypeUuid = "AAA4";
    private static final UUID eegServiceUUID = UuidUtil.stringToUUID(eegServiceUuid, MY_BASE_UUID);
    private static final UUID eegMeasUUID = UuidUtil.stringToUUID(eegMeasUuid, MY_BASE_UUID);
    private static final UUID eeg1mVCaliUUID = UuidUtil.stringToUUID(eeg1mVCaliUuid, MY_BASE_UUID);
    private static final UUID eegSampleRateUUID = UuidUtil.stringToUUID(eegSampleRateUuid, MY_BASE_UUID);
    private static final UUID eegLeadTypeUUID = UuidUtil.stringToUUID(eegLeadTypeUuid, MY_BASE_UUID);

    private static final BleGattElement EEGMEAS = new BleGattElement(eegServiceUUID, eegMeasUUID, null, "EEG Data Packet");
    private static final BleGattElement EEGMEASCCC = new BleGattElement(eegServiceUUID, eegMeasUUID, CCC_UUID, "EEG Data Packet CCC");
    private static final BleGattElement EEG1MVCALI = new BleGattElement(eegServiceUUID, eeg1mVCaliUUID, null, "EEG 1mV Calibration");
    private static final BleGattElement EEGSAMPLERATE = new BleGattElement(eegServiceUUID, eegSampleRateUUID, null, "EEG Sample Rate");
    private static final BleGattElement EEGLEADTYPE = new BleGattElement(eegServiceUUID, eegLeadTypeUUID, null, "EEG Lead Type");

    // battery service
    private static final String battServiceUuid = "180F";
    private static final String battLevelUuid = "2A19";
    private static final UUID battServiceUUID = UuidUtil.stringToUUID(battServiceUuid, STANDARD_BLE_UUID);
    private static final UUID battLevelUUID = UuidUtil.stringToUUID(battLevelUuid, STANDARD_BLE_UUID);
    private static final BleGattElement BATTLEVEL = new BleGattElement(battServiceUUID, battLevelUUID, null, "电池电量百分比");
    private static final BleGattElement BATTLEVELCCC = new BleGattElement(battServiceUUID, battLevelUUID, CCC_UUID, "电池电量CCC");


    private int sampleRate = DEFAULT_SAMPLE_RATE; // sample rate
    private int caliValue = DEFAULT_CALI_1MV; // 1mV calibration value
    private EegLeadType leadType = DEFAULT_LEAD_TYPE; // lead type

    private boolean hasBattService = false; // has battery service
    private EegDataProcessor eegProcessor; // eeg processor

    private OnEegListener listener; // device listener

    private BleEegRecord10 eegRecord;
    private boolean isEegRecord = false; // is recording eeg

    private Context context;

    private volatile boolean isUploadingEegRecord = false;

    public EegDevice(DeviceInfo registerInfo) {
        super(registerInfo);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setEegRecord(final boolean isRecord) {
        if(isEegRecord == isRecord || isUploadingEegRecord) return;

        isEegRecord = isRecord;

        if(isRecord) {
            eegRecord = (BleEegRecord10) RecordFactory.create(EEG, new Date().getTime(), getAddress(), AccountManager.getAccount(), "");
            eegRecord.setSampleRate(sampleRate);
            eegRecord.setCaliValue(caliValue);
            eegRecord.setLeadTypeCode(leadType.getCode());
            MyApplication.showMessageUsingShortToast("记录时请保持安静。");
        } else {
            if(eegRecord == null) return;

            eegRecord.setCreateTime(new Date().getTime());
            eegRecord.setRecordSecond(eegRecord.getEegData().size()/sampleRate);
            eegRecord.save();
            isUploadingEegRecord = true;
            new RecordWebAsyncTask(context, RECORD_UPLOAD_CMD, new RecordWebAsyncTask.RecordWebCallback() {
                @Override
                public void onFinish(int code, final Object rlt) {
                    int strId = (code == CODE_SUCCESS) ? R.string.save_record_success : R.string.operation_failure;
                    Toast.makeText(context, strId, Toast.LENGTH_SHORT).show();
                    if(code == CODE_SUCCESS) {
                        eegRecord.setModified(false);
                        eegRecord.save();
                    }
                    isUploadingEegRecord = false;
                }
            }).execute(eegRecord);

            eegRecord = null;
        }

        if(listener != null) {
            listener.onEegSignalRecorded(isRecord);
        }
    }

    public void setEegOn(boolean isOn) {
        //((BleConnector)connector).notify(EEGMEASCCC, false, null);

        if(isOn) {
            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    //ViseLog.i("eeg data: " + Arrays.toString(data));
                    eegProcessor.processData(data);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            };
            ((BleConnector)connector).notify(EEGMEASCCC, true, notifyCallback);
        } else {
            if(eegProcessor != null)
                eegProcessor.stop();

            ((BleConnector)connector).notify(EEGMEASCCC, false, null);
        }
    }

    @Override
    public void open(Context context) {
        super.open(context);
    }

    @Override
    public void close() {
        super.close();

        if(isEegRecord) {
            setEegRecord(false);
        }
    }

    @Override
    public boolean onConnectSuccess() {
        BleConnector connector = (BleConnector)this.connector;

        BleGattElement[] elements = new BleGattElement[]{EEGMEAS, EEGMEASCCC, EEG1MVCALI, EEGSAMPLERATE, EEGLEADTYPE};
        if(connector.containGattElements(elements)) {
            initEegService();

            setEegOn(true);
        } else {
            return false;
        }

        elements = new BleGattElement[]{BATTLEVEL, BATTLEVELCCC};
        if(connector.containGattElements(elements)) {
            hasBattService = true;
            readBatteryLevel();
            setBatteryMeasure(true);
        }

        return true;
    }

    @Override
    public void onConnectFailure() {
        if(eegProcessor != null) {
            eegProcessor.stop();
        }
        setEegRecord(false);
    }

    @Override
    public void onDisconnect() {
        if(eegProcessor != null) {
            eegProcessor.stop();
        }
        setEegRecord(false);
    }

    @Override
    public void disconnect(final boolean forever) {
        setBatteryMeasure(false);
        setEegRecord(false);
        super.disconnect(forever);
    }

    public final int getSampleRate() {
        return sampleRate;
    }

    public final int getCaliValue() {
        return caliValue;
    }

    public boolean isEegRecord() {
        return isEegRecord;
    }

    public void setListener(OnEegListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    private void setBatteryMeasure(boolean isStart) {
        if(!hasBattService) return;

        //((BleConnector)connector).notify(BATTLEVELCCC, false, null);
        if(isStart) {
            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    setBattery(data[0]);
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
                setBattery(data[0]);
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void initEegService() {
        readSampleRate();
        read1mVCali();
        readLeadType();
        ((BleConnector)connector).runInstantly(new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                eegProcessor = new EegDataProcessor(EegDevice.this);
                eegProcessor.start();

                if (listener != null)
                    listener.onFragmentUpdated(sampleRate, caliValue, DEFAULT_ZERO_LOCATION);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    private void readSampleRate() {
        ((BleConnector)connector).read(EEGSAMPLERATE, new IBleDataCallback() {
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
        ((BleConnector)connector).read(EEG1MVCALI, new IBleDataCallback() {
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
        ((BleConnector)connector).read(EEGLEADTYPE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                leadType = EegLeadType.getFromCode(UnsignedUtil.getUnsignedByte(data[0]));
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    public void showEegSignal(int eegSignal) {
        if (listener != null) {
            listener.onEegSignalShowed(eegSignal);
        }
    }

    public void recordEegSignal(int eegSignal) {
        if(isEegRecord && eegRecord != null) {
            eegRecord.process((short)eegSignal);
            if(eegRecord.getDataNum() % sampleRate == 0 && listener != null) {
                int second = eegRecord.getDataNum()/sampleRate;
                listener.onEegRecordTimeUpdated(second);
            }
        }
    }
}
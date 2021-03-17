package com.cmtech.android.bledevice.ppg.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BlePpgRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordFactory;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;

import java.util.Date;
import java.util.UUID;

import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.DEFAULT_RECORD_VER;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.PPG;
import static com.cmtech.android.bledeviceapp.global.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.MY_BASE_UUID;
import static com.cmtech.android.bledeviceapp.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.eeg.model
 * ClassName:      EegDevice
 * Description:    脑电设备
 * Author:         作者名
 * CreateDate:     2020/6/11 下午3:28
 * UpdateUser:     更新者
 * UpdateDate:     2020/6/11 下午3:28
 * UpdateRemark:   更新说明
 * Version:        1.0
 */


public class PpgDevice extends AbstractDevice {
    private static final int DEFAULT_CALI_VALUE = 100; // default calibration value
    private static final int DEFAULT_SAMPLE_RATE = 200; // default sample rate, unit: Hz
    private static final int PPG_RECORD_MAX_SECOND = 30;

    // ppg service
    private static final String ppgServiceUuid = "AAB0";
    private static final String ppgMeasUuid = "AAB1";
    private static final String ppgSampleRateUuid = "AAB2";
    private static final UUID ppgServiceUUID = UuidUtil.stringToUUID(ppgServiceUuid, MY_BASE_UUID);
    private static final UUID ppgMeasUUID = UuidUtil.stringToUUID(ppgMeasUuid, MY_BASE_UUID);
    private static final UUID ppgSampleRateUUID = UuidUtil.stringToUUID(ppgSampleRateUuid, MY_BASE_UUID);

    private static final BleGattElement PPGMEAS = new BleGattElement(ppgServiceUUID, ppgMeasUUID, null, "PPG Data Packet");
    private static final BleGattElement PPGMEASCCC = new BleGattElement(ppgServiceUUID, ppgMeasUUID, CCC_UUID, "PPG Data Packet CCC");
    private static final BleGattElement PPGSAMPLERATE = new BleGattElement(ppgServiceUUID, ppgSampleRateUUID, null, "PPG Sample Rate");

    private int sampleRate = DEFAULT_SAMPLE_RATE; // sample rate
    private int caliValue = DEFAULT_CALI_VALUE; // calibration value

    private PpgDataProcessor dataProcessor; // ppg data processor

    private OnPpgListener listener; // device listener

    private BlePpgRecord record;
    private boolean isRecording = false; // is recording

    public PpgDevice(Context context, DeviceCommonInfo registerInfo) {
        super(context, registerInfo);
    }

    public final int getSampleRate() {
        return sampleRate;
    }

    public final int getCaliValue() {
        return caliValue;
    }

    public void setListener(OnPpgListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    @Override
    public void open() {
        super.open();
    }

    @Override
    public void close() {
        super.close();

        if(isRecording) {
            setRecord(false);
        }
    }

    @Override
    public boolean onConnectSuccess() {
        BleConnector connector = (BleConnector)this.connector;

        BleGattElement[] elements = new BleGattElement[]{PPGMEAS, PPGMEASCCC, PPGSAMPLERATE};
        if(connector.containGattElements(elements)) {
            readSampleRate();

            ((BleConnector)connector).runInstantly(new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    if (listener != null)
                        listener.onFragmentUpdated(sampleRate, caliValue, DEFAULT_ZERO_LOCATION);

                    updateSignalShowState(true);

                    dataProcessor = new PpgDataProcessor(PpgDevice.this);
                    dataProcessor.start();
                }

                @Override
                public void onFailure(BleException exception) {

                }
            });

            enablePpgSampling(true);
        } else {
            return false;
        }

        return true;
    }

    @Override
    public void onConnectFailure() {
        if(dataProcessor != null) {
            dataProcessor.stop();
        }

        updateSignalShowState(false);

        setRecord(false);
    }

    @Override
    public void onDisconnect() {
        if(dataProcessor != null) {
            dataProcessor.stop();
        }

        updateSignalShowState(false);

        setRecord(false);
    }

    @Override
    public void disconnect(final boolean forever) {
        enablePpgSampling(false);
        setRecord(false);
        super.disconnect(forever);
    }

    public void setRecord(final boolean isRecord) {
        if(isRecording == isRecord) return;

        isRecording = isRecord;
        if(isRecord) {
            this.record = (BlePpgRecord) RecordFactory.create(PPG, DEFAULT_RECORD_VER, new Date().getTime(), getAddress(), MyApplication.getAccountId());
            if(this.record != null) {
                this.record.setSampleRate(sampleRate);
                this.record.setCaliValue(caliValue);
                Toast.makeText(getContext(), R.string.pls_be_quiet_when_record, Toast.LENGTH_SHORT).show();
            }
        } else {
            if(this.record == null) return;

            this.record.setCreateTime(new Date().getTime());
            this.record.setRecordSecond(this.record.getPpgData().size()/sampleRate);
            this.record.save();
            Toast.makeText(getContext(), R.string.save_record_success, Toast.LENGTH_SHORT).show();
        }

        if(listener != null) {
            listener.onPpgSignalRecordStatusChanged(isRecording);
        }
    }

    public void showPpgSignal(int ppgSignal) {
        if (listener != null) {
            listener.onPpgSignalShowed(ppgSignal);
        }
    }

    public void recordPpgSignal(int ppgSignal) {
        if(isRecording && record != null) {
            record.process(ppgSignal);
            if(record.getDataNum() % sampleRate == 0 && listener != null) {
                int second = record.getDataNum()/sampleRate;
                listener.onPpgSignalRecordTimeUpdated(second);
                if(second >= PPG_RECORD_MAX_SECOND) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            setRecord(false);
                        }
                    });
                }
            }
        }
    }

    private void enablePpgSampling(boolean enable) {
        //((BleConnector)connector).notify(PPGMEASCCC, false, null);

        if(enable) {
            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    if(dataProcessor != null)
                        dataProcessor.processData(data);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            };
            ((BleConnector)connector).notify(PPGMEASCCC, true, notifyCallback);
        } else {
            ((BleConnector)connector).notify(PPGMEASCCC, false, null);

            if(dataProcessor != null)
                dataProcessor.stop();

        }
    }

    private void readSampleRate() {
        ((BleConnector)connector).read(PPGSAMPLERATE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                sampleRate = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(data));
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void updateSignalShowState(boolean isShow) {
        if (listener != null) {
            listener.onPpgSignalShowStatusUpdated(isShow);
        }
    }
}